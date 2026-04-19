# TuniWay Connect Backend

Spring Boot API for TuniWay Connect.

## Requirements

- Java 17+
- Maven 3.6+

## Run
mvn spring-boot:run
```

The API will be available at **http://localhost:8080**.

### Installing Maven (if `mvn` is not recognized)

Maven is being installed to `%USERPROFILE%\maven` (download may still be in progress). When done, either:

- Use **Option A** above (no PATH change), or  
- Add Maven to your user PATH: add `%USERPROFILE%\maven\bin` to the Path environment variable in Windows (Settings → Environment variables → User variables → Path → Edit → New).

## Endpoints

- `GET /api/health` — Health check; returns status and a welcome message.

## CORS

The backend allows requests from the frontend dev server (e.g. `http://localhost:1234`). Adjust `WebConfig.java` if your frontend runs on another origin or port.


###### Pour activer le back-end de project i doit executer les commandes ci-dessous:

CREATE ROLE tuniway\_admin WITH LOGIN PASSWORD 'L30NSK3nNedY';

CREATE DATABASE tuniway\_db OWNER tuniway\_admin;

GRANT ALL PRIVILEGES ON DATABASE tuniway\_db TO tuniway\_admin;

###### 

Ces commandes SQL doivent être exécutées avec un compte PostgreSQL qui a les droits suffisants, souvent l’utilisateur postgres, pas forcément tuniway_admin.
###### Puis executer le command mvn spring-boot:run
