#!/usr/bin/env bash
set -u

BASE_URL="http://localhost:8080"
DB_HOST="localhost"
DB_USER="tuniway_admin"
DB_NAME="tuniway_db"
export PGPASSWORD='L30NSK3nNedY'

REPORT="/home/chazar/Documents/GitHub/TuniWay-Connect-Back/docs/reports/admin-all-endpoints-e2e-report.md"

json_escape() {
  python3 - <<'PY'
import json,sys
print(json.dumps(sys.stdin.read()))
PY
}

gen_otp() {
  python3 - <<'PY'
import base64,hmac,hashlib,struct,time
s='JBSWY3DPEHPK3PXP'
k=base64.b32decode(s, casefold=True)
c=int(time.time())//30
m=struct.pack('>Q', c)
h=hmac.new(k,m,hashlib.sha1).digest()
o=h[-1]&15
otp=((struct.unpack('>I',h[o:o+4])[0] & 0x7fffffff) % 1000000)
print(f"{otp:06d}")
PY
}

run_api() {
  local method="$1"
  local url="$2"
  local token="$3"
  local body="${4:-}"

  if [[ -n "$body" ]]; then
    curl -s -w '\nHTTP_STATUS:%{http_code}' -X "$method" "$url" \
      -H "Authorization: Bearer $token" \
      -H 'Content-Type: application/json' \
      -d "$body"
  else
    curl -s -w '\nHTTP_STATUS:%{http_code}' -X "$method" "$url" \
      -H "Authorization: Bearer $token"
  fi
}

extract_status() {
  echo "$1" | sed -n 's/^HTTP_STATUS://p'
}

extract_body() {
  echo "$1" | sed '/^HTTP_STATUS:/d'
}

uuid_now() {
  cat /proc/sys/kernel/random/uuid
}

# Ensure API is up
probe=$(curl -s -o /tmp/admin_all_probe.json -w "%{http_code}" "$BASE_URL/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"probe","password_hash":"probe"}')
if [[ "$probe" != "400" ]]; then
  echo "API not reachable on $BASE_URL" >&2
  exit 1
fi

# Admin auth
otp=$(gen_otp)
admin_login=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" -H 'Content-Type: application/json' \
  -d '{"email":"admin.test@tuniway.local","password_hash":"AdminPass123!"}')
admin_temp=$(echo "$admin_login" | jq -r '.tempToken')
admin_verify=$(curl -s -X POST "$BASE_URL/api/v1/auth/2fa/verify" -H 'Content-Type: application/json' \
  -d "{\"tempToken\":\"$admin_temp\",\"totpCode\":\"$otp\"}")
ADMIN_TOKEN=$(echo "$admin_verify" | jq -r '.accessToken')
if [[ -z "$ADMIN_TOKEN" || "$ADMIN_TOKEN" == "null" ]]; then
  echo "Failed to get admin token" >&2
  echo "$admin_verify" >&2
  exit 1
fi

NOW_TS=$(date +%s)
STAFF_EMAIL="staff.e2e.${NOW_TS}@tuniway.local"
STAFF_EMP_CODE="EMP-E2E-${NOW_TS}"
STAFF_LICENSE="LIC-E2E-${NOW_TS}"
STAFF_ADMIN_CODE="ADM-E2E-${NOW_TS}"

TRANSPORT_CODE_A="TR-A-${NOW_TS}"
TRANSPORT_CODE_B="TR-B-${NOW_TS}"

# 1) GET /admin/dashboard
cmd_dashboard="curl -s -X GET $BASE_URL/api/v1/admin/dashboard -H 'Authorization: Bearer <ADMIN_TOKEN>'"
res_dashboard=$(run_api "GET" "$BASE_URL/api/v1/admin/dashboard" "$ADMIN_TOKEN")

# 2) POST /admin/staff-accounts
body_create_staff=$(cat <<JSON
{"email":"$STAFF_EMAIL","password_hash":"StaffPass123!","fullName":"Staff E2E","phone":"55000111","license_number":"$STAFF_LICENSE","employee_code":"$STAFF_EMP_CODE","role":"EMPLOYEE"}
JSON
)
cmd_create_staff="curl -s -X POST $BASE_URL/api/v1/admin/staff-accounts -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_create_staff'"
res_create_staff=$(run_api "POST" "$BASE_URL/api/v1/admin/staff-accounts" "$ADMIN_TOKEN" "$body_create_staff")
staff_id=$(extract_body "$res_create_staff" | jq -r '.id')

# 3) PATCH /admin/staff-accounts/{id}
body_update_staff='{"fullName":"Staff E2E Updated","phone":"55999888"}'
cmd_update_staff="curl -s -X PATCH $BASE_URL/api/v1/admin/staff-accounts/$staff_id -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_update_staff'"
res_update_staff=$(run_api "PATCH" "$BASE_URL/api/v1/admin/staff-accounts/$staff_id" "$ADMIN_TOKEN" "$body_update_staff")

# 4) PATCH /admin/staff-accounts/{id}/status
body_patch_status='{"status":"INACTIVE"}'
cmd_patch_status="curl -s -X PATCH $BASE_URL/api/v1/admin/staff-accounts/$staff_id/status -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_patch_status'"
res_patch_status=$(run_api "PATCH" "$BASE_URL/api/v1/admin/staff-accounts/$staff_id/status" "$ADMIN_TOKEN" "$body_patch_status")

# 5) POST /admin/staff-accounts/{id}/status
body_post_status='{"status":"ACTIVE"}'
cmd_post_status="curl -s -X POST $BASE_URL/api/v1/admin/staff-accounts/$staff_id/status -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_post_status'"
res_post_status=$(run_api "POST" "$BASE_URL/api/v1/admin/staff-accounts/$staff_id/status" "$ADMIN_TOKEN" "$body_post_status")

# 6) DELETE /admin/staff-accounts/{id}
cmd_delete_staff="curl -s -X DELETE $BASE_URL/api/v1/admin/staff-accounts/$staff_id -H 'Authorization: Bearer <ADMIN_TOKEN>'"
res_delete_staff=$(run_api "DELETE" "$BASE_URL/api/v1/admin/staff-accounts/$staff_id" "$ADMIN_TOKEN")

# 7) POST /admin/transports (A)
body_create_transport_a=$(cat <<JSON
{"code":"$TRANSPORT_CODE_A","name":"Transport A","transportType":"BUS","route_name":"Route-A","start_point":"A1","end_point":"A2","operating_zone":"OPS-A","zone":"ZONE-A","is_active":true}
JSON
)
cmd_create_transport_a="curl -s -X POST $BASE_URL/api/v1/admin/transports -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_create_transport_a'"
res_create_transport_a=$(run_api "POST" "$BASE_URL/api/v1/admin/transports" "$ADMIN_TOKEN" "$body_create_transport_a")
transport_a_id=$(psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT id FROM transport WHERE code='$TRANSPORT_CODE_A' LIMIT 1;")

# 8) POST /admin/transports (B) for reassign
body_create_transport_b=$(cat <<JSON
{"code":"$TRANSPORT_CODE_B","name":"Transport B","transportType":"METRO","route_name":"Route-B","start_point":"B1","end_point":"B2","operating_zone":"OPS-B","zone":"ZONE-B","is_active":true}
JSON
)
cmd_create_transport_b="curl -s -X POST $BASE_URL/api/v1/admin/transports -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_create_transport_b'"
res_create_transport_b=$(run_api "POST" "$BASE_URL/api/v1/admin/transports" "$ADMIN_TOKEN" "$body_create_transport_b")
transport_b_id=$(psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT id FROM transport WHERE code='$TRANSPORT_CODE_B' LIMIT 1;")

# 9) PATCH /admin/transports/{id}/route
body_patch_route='{"route":"Route-A-Updated"}'
cmd_patch_route="curl -s -X PATCH $BASE_URL/api/v1/admin/transports/$transport_a_id/route -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_patch_route'"
res_patch_route=$(run_api "PATCH" "$BASE_URL/api/v1/admin/transports/$transport_a_id/route" "$ADMIN_TOKEN" "$body_patch_route")

# 10) PATCH /admin/transports/{id}/zone
body_patch_zone='{"zone":"ZONE-A2","operating_zone":"OPS-A2"}'
cmd_patch_zone="curl -s -X PATCH $BASE_URL/api/v1/admin/transports/$transport_a_id/zone -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_patch_zone'"
res_patch_zone=$(run_api "PATCH" "$BASE_URL/api/v1/admin/transports/$transport_a_id/zone" "$ADMIN_TOKEN" "$body_patch_zone")

# 11) PATCH /admin/transports/{id}/stops
body_patch_stops='{"stops":[{"stopName":"Stop A1","zone":"Z1","active":true,"stopOrder":1},{"stopName":"Stop A2","zone":"Z2","active":true,"stopOrder":2}]}'
cmd_patch_stops="curl -s -X PATCH $BASE_URL/api/v1/admin/transports/$transport_a_id/stops -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_patch_stops'"
res_patch_stops=$(run_api "PATCH" "$BASE_URL/api/v1/admin/transports/$transport_a_id/stops" "$ADMIN_TOKEN" "$body_patch_stops")

stop1_id=$(psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT stop_id FROM transport_route_stops WHERE transport_id='$transport_a_id' ORDER BY stop_order LIMIT 1;")
stop2_id=$(psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT stop_id FROM transport_route_stops WHERE transport_id='$transport_a_id' ORDER BY stop_order OFFSET 1 LIMIT 1;")

# 12) PATCH /admin/transports/{id}/departures
body_patch_departures=$(cat <<JSON
{"departures":[{"stopId":"$stop1_id","dayOfWeek":"MONDAY","departureTime":"08:15:00","active":true,"stopOrder":1},{"stopId":"$stop2_id","dayOfWeek":"MONDAY","departureTime":"09:00:00","active":true,"stopOrder":2}]}
JSON
)
cmd_patch_departures="curl -s -X PATCH $BASE_URL/api/v1/admin/transports/$transport_a_id/departures -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_patch_departures'"
res_patch_departures=$(run_api "PATCH" "$BASE_URL/api/v1/admin/transports/$transport_a_id/departures" "$ADMIN_TOKEN" "$body_patch_departures")

# 13) PATCH /admin/transports/{id}
body_patch_transport=$(cat <<JSON
{"name":"Transport A Updated","code":"$TRANSPORT_CODE_A-U","transportType":"TRAIN","route_name":"Route-A-U","start_point":"A1U","end_point":"A2U","operating_zone":"OPS-AU","zone":"ZONE-AU","is_active":true}
JSON
)
cmd_patch_transport="curl -s -X PATCH $BASE_URL/api/v1/admin/transports/$transport_a_id -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_patch_transport'"
res_patch_transport=$(run_api "PATCH" "$BASE_URL/api/v1/admin/transports/$transport_a_id" "$ADMIN_TOKEN" "$body_patch_transport")

# Prepare scheduled shift for shift endpoints
shift_id=$(uuid_now)
shift_start='2026-04-10T08:00:00Z'
shift_end='2026-04-10T12:00:00Z'
psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "INSERT INTO workshifts (id, employee_id, transport_id, schedule_start, schedule_end, status) VALUES ('$shift_id','11111111-1111-1111-1111-111111111111','$transport_a_id','$shift_start','$shift_end','SCHEDULED') ON CONFLICT (id) DO NOTHING;" >/dev/null

# 14) PATCH /admin/shifts/{id}
body_patch_shift=$(cat <<JSON
{"newStart":"2026-04-10T08:30:00Z","newEnd":"2026-04-10T12:30:00Z","transportId":"$transport_a_id"}
JSON
)
cmd_patch_shift="curl -s -X PATCH $BASE_URL/api/v1/admin/shifts/$shift_id -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_patch_shift'"
res_patch_shift=$(run_api "PATCH" "$BASE_URL/api/v1/admin/shifts/$shift_id" "$ADMIN_TOKEN" "$body_patch_shift")

# 15) PATCH /admin/shifts/{id}/reassign-transport
body_reassign=$(cat <<JSON
{"newTransportId":"$transport_b_id"}
JSON
)
cmd_reassign="curl -s -X PATCH $BASE_URL/api/v1/admin/shifts/$shift_id/reassign-transport -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_reassign'"
res_reassign=$(run_api "PATCH" "$BASE_URL/api/v1/admin/shifts/$shift_id/reassign-transport" "$ADMIN_TOKEN" "$body_reassign")

# 16) POST /admin/planning/publish
body_publish=$(cat <<JSON
{"changes":[{"shiftId":"$shift_id","newStart":"2026-04-10T09:00:00Z","newEnd":"2026-04-10T13:00:00Z","transportId":"$transport_b_id"}]}
JSON
)
cmd_publish="curl -s -X POST $BASE_URL/api/v1/admin/planning/publish -H 'Authorization: Bearer <ADMIN_TOKEN>' -H 'Content-Type: application/json' -d '$body_publish'"
res_publish=$(run_api "POST" "$BASE_URL/api/v1/admin/planning/publish" "$ADMIN_TOKEN" "$body_publish")

write_section() {
  local title="$1"
  local endpoint="$2"
  local cmd="$3"
  local input="$4"
  local raw="$5"

  local body status
  body=$(extract_body "$raw")
  status=$(extract_status "$raw")

  cat >> "$REPORT" <<EOF
## $title

Endpoint: $endpoint

Command used:
\
$cmd
\

Input:
\
$input
\

Output body:
\
$body
\

HTTP status: $status

---

EOF
}

cat > "$REPORT" <<EOF
# Admin Endpoints Full E2E Report

Date: $(date -u +"%Y-%m-%d %H:%M:%SZ")
Base URL: $BASE_URL

Scope: all currently implemented admin endpoints in AdminController.

Authentication setup command (used to get ADMIN_TOKEN):
\
otp=\$(python3 - <<'PY'
import base64,hmac,hashlib,struct,time
s='JBSWY3DPEHPK3PXP';k=base64.b32decode(s, casefold=True);c=int(time.time())//30
m=struct.pack('>Q', c);h=hmac.new(k,m,hashlib.sha1).digest();o=h[-1]&15
print(f"{((struct.unpack('>I',h[o:o+4])[0]&0x7fffffff)%1000000):06d}")
PY
)
admin_temp=\$(curl -s -X POST $BASE_URL/api/v1/auth/login -H 'Content-Type: application/json' -d '{"email":"admin.test@tuniway.local","password_hash":"AdminPass123!"}' | jq -r '.tempToken')
ADMIN_TOKEN=\$(curl -s -X POST $BASE_URL/api/v1/auth/2fa/verify -H 'Content-Type: application/json' -d "{\"tempToken\":\"\$admin_temp\",\"totpCode\":\"\$otp\"}" | jq -r '.accessToken')
\

EOF

write_section "1) Get Dashboard" "/api/v1/admin/dashboard" "$cmd_dashboard" "none" "$res_dashboard"
write_section "2) Create Staff Account" "/api/v1/admin/staff-accounts" "$cmd_create_staff" "$body_create_staff" "$res_create_staff"
write_section "3) Update Staff Account" "/api/v1/admin/staff-accounts/{id}" "$cmd_update_staff" "$body_update_staff" "$res_update_staff"
write_section "4) Change Staff Status (PATCH)" "/api/v1/admin/staff-accounts/{id}/status" "$cmd_patch_status" "$body_patch_status" "$res_patch_status"
write_section "5) Change Staff Status (POST)" "/api/v1/admin/staff-accounts/{id}/status" "$cmd_post_status" "$body_post_status" "$res_post_status"
write_section "6) Delete Staff Account" "/api/v1/admin/staff-accounts/{id}" "$cmd_delete_staff" "none" "$res_delete_staff"
write_section "7) Create Transport A" "/api/v1/admin/transports" "$cmd_create_transport_a" "$body_create_transport_a" "$res_create_transport_a"
write_section "8) Create Transport B" "/api/v1/admin/transports" "$cmd_create_transport_b" "$body_create_transport_b" "$res_create_transport_b"
write_section "9) Update Transport Route" "/api/v1/admin/transports/{id}/route" "$cmd_patch_route" "$body_patch_route" "$res_patch_route"
write_section "10) Update Transport Zone" "/api/v1/admin/transports/{id}/zone" "$cmd_patch_zone" "$body_patch_zone" "$res_patch_zone"
write_section "11) Update Transport Stops" "/api/v1/admin/transports/{id}/stops" "$cmd_patch_stops" "$body_patch_stops" "$res_patch_stops"
write_section "12) Update Transport Departures" "/api/v1/admin/transports/{id}/departures" "$cmd_patch_departures" "$body_patch_departures" "$res_patch_departures"
write_section "13) Update Transport (Generic)" "/api/v1/admin/transports/{id}" "$cmd_patch_transport" "$body_patch_transport" "$res_patch_transport"
write_section "14) Update Shift" "/api/v1/admin/shifts/{id}" "$cmd_patch_shift" "$body_patch_shift" "$res_patch_shift"
write_section "15) Reassign Shift Transport" "/api/v1/admin/shifts/{id}/reassign-transport" "$cmd_reassign" "$body_reassign" "$res_reassign"
write_section "16) Publish Planning" "/api/v1/admin/planning/publish" "$cmd_publish" "$body_publish" "$res_publish"

cat >> "$REPORT" <<EOF
## Runtime IDs used

- Created staff id: $staff_id
- Transport A id: $transport_a_id
- Transport B id: $transport_b_id
- Stop 1 id: $stop1_id
- Stop 2 id: $stop2_id
- Shift id: $shift_id

## Quick status summary

EOF

summary_line() {
  local name="$1" raw="$2"
  echo "- $name -> HTTP $(extract_status "$raw")" >> "$REPORT"
}

summary_line "GET /admin/dashboard" "$res_dashboard"
summary_line "POST /admin/staff-accounts" "$res_create_staff"
summary_line "PATCH /admin/staff-accounts/{id}" "$res_update_staff"
summary_line "PATCH /admin/staff-accounts/{id}/status" "$res_patch_status"
summary_line "POST /admin/staff-accounts/{id}/status" "$res_post_status"
summary_line "DELETE /admin/staff-accounts/{id}" "$res_delete_staff"
summary_line "POST /admin/transports (A)" "$res_create_transport_a"
summary_line "POST /admin/transports (B)" "$res_create_transport_b"
summary_line "PATCH /admin/transports/{id}/route" "$res_patch_route"
summary_line "PATCH /admin/transports/{id}/zone" "$res_patch_zone"
summary_line "PATCH /admin/transports/{id}/stops" "$res_patch_stops"
summary_line "PATCH /admin/transports/{id}/departures" "$res_patch_departures"
summary_line "PATCH /admin/transports/{id}" "$res_patch_transport"
summary_line "PATCH /admin/shifts/{id}" "$res_patch_shift"
summary_line "PATCH /admin/shifts/{id}/reassign-transport" "$res_reassign"
summary_line "POST /admin/planning/publish" "$res_publish"

echo "Report generated at: $REPORT"
