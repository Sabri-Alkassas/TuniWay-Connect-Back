param(
    [string]$EnvFilePath = ".env",
    [string]$ContainerName = "tuniway-postgres",
    [string]$PostgresImage = "postgres:16",
    [switch]$RecreateContainer,
    [switch]$SkipAppRun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Read-EnvFile {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        throw "Env file not found: $Path"
    }

    $map = @{}
    Get-Content -Path $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#")) {
            return
        }

        $idx = $line.IndexOf("=")
        if ($idx -lt 1) {
            return
        }

        $key = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        $map[$key] = $value
    }

    return $map
}

function Parse-DbUrl {
    param([string]$JdbcUrl)

    $pattern = '^jdbc:postgresql://(?<host>[^:/?#]+)(:(?<port>\d+))?/(?<db>[^?]+)'
    $match = [regex]::Match($JdbcUrl, $pattern)
    if (-not $match.Success) {
        throw "Unsupported DB_URL format: $JdbcUrl"
    }

    $dbHost = $match.Groups["host"].Value
    $port = if ($match.Groups["port"].Success) { [int]$match.Groups["port"].Value } else { 5432 }
    $dbName = $match.Groups["db"].Value

    return @{
        Host = $dbHost
        Port = $port
        DbName = $dbName
    }
}

function Assert-DockerDaemonRunning {
    docker version | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Docker daemon is not running. Start Docker Desktop, wait until it is ready, then rerun this script."
    }
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

Write-Host "[1/6] Reading .env and DB config..."
$envMap = Read-EnvFile -Path $EnvFilePath

$dbUrl = $envMap["DB_URL"]
$dbUser = $envMap["DB_USERNAME"]
$dbPassword = $envMap["DB_PASSWORD"]

if ([string]::IsNullOrWhiteSpace($dbUrl) -or [string]::IsNullOrWhiteSpace($dbUser) -or [string]::IsNullOrWhiteSpace($dbPassword)) {
    throw "DB_URL, DB_USERNAME or DB_PASSWORD is missing in $EnvFilePath"
}

$dbInfo = Parse-DbUrl -JdbcUrl $dbUrl
$dbHost = $dbInfo.Host
$port = $dbInfo.Port
$dbName = $dbInfo.DbName

if ($dbHost -notin @("localhost", "127.0.0.1")) {
    throw "This script manages local Docker PostgreSQL only. DB_URL host must be localhost or 127.0.0.1. Current host: $dbHost"
}

Write-Host "[2/6] Checking Docker availability..."
$docker = Get-Command docker -ErrorAction SilentlyContinue
if ($null -eq $docker) {
    throw "Docker is not installed or not available in PATH. Install Docker Desktop first."
}
Assert-DockerDaemonRunning

Write-Host "[3/6] Creating or starting PostgreSQL container..."
$existing = docker ps -a --filter "name=^/$ContainerName$" --format "{{.Names}}"

if ($RecreateContainer -and $existing) {
    docker rm -f $ContainerName | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to remove existing container '$ContainerName'."
    }
    $existing = $null
}

if (-not $existing) {
    $portBusy = netstat -ano | Select-String ":$port"
    if ($portBusy) {
        throw "Port $port is already in use. Free this port or change DB_URL in .env"
    }

    docker run --name $ContainerName -e POSTGRES_USER=$dbUser -e POSTGRES_PASSWORD=$dbPassword -e POSTGRES_DB=$dbName -p "${port}:5432" -d $PostgresImage | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create PostgreSQL container '$ContainerName'."
    }
} else {
    $running = docker ps --filter "name=^/$ContainerName$" --format "{{.Names}}"
    if (-not $running) {
        docker start $ContainerName | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to start existing container '$ContainerName'."
        }
    }
}

Write-Host "[4/6] Waiting for PostgreSQL readiness..."
$ready = $false
for ($i = 1; $i -le 60; $i++) {
    docker exec $ContainerName pg_isready -U $dbUser -d $dbName 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) {
        $ready = $true
        break
    }
    Start-Sleep -Seconds 1
}

if (-not $ready) {
    throw "PostgreSQL container did not become ready in time."
}

Write-Host "[5/6] Enabling required extension (pgcrypto)..."
docker exec $ContainerName psql -U $dbUser -d $dbName -v ON_ERROR_STOP=1 -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;" | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "Failed to enable pgcrypto extension."
}

$mvnCommand = "mvn"
if (Test-Path (Join-Path $repoRoot "mvnw.cmd")) {
    $mvnCommand = ".\\mvnw.cmd"
}

Write-Host "[6/6] Running Flyway migrations..."
Invoke-Expression "$mvnCommand process-resources flyway:migrate"
if ($LASTEXITCODE -ne 0) {
    throw "Flyway migration failed."
}

if (-not $SkipAppRun) {
    Write-Host "Starting Spring Boot app..."
    Invoke-Expression "$mvnCommand spring-boot:run"
}

Write-Host "Done. Database is ready and migrations are applied."
