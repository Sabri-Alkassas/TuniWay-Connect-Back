# Client Endpoints E2E Report

Date: 2026-04-09 17:56:49Z
Base URL: http://localhost:8080

Scope:
- `GET /api/v1/client/dashboard`
- `GET /api/v1/client/account`
- `PATCH /api/v1/client/account`

Test account used:
- email: `refreshclient@test.com`
- password_hash: `refreshpass`
- role: `CLIENT`

Note:
- The account is a local seeded dev account.
- The profile was restored to its original values at the end of the test.

## 1) Auth Command Used

Command used:
```bash
CLIENT_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"refreshclient@test.com","password_hash":"refreshpass"}' \
  | jq -r '.accessToken')
```

Login output:
```json
{"id":"33333333-3333-3333-3333-333333333333","email":"refreshclient@test.com","role":"CLIENT","status":"ACTIVE","lastLoginAt":"2026-04-09T17:51:42.862058553Z","authenticated":true,"twoFactorRequired":false,"tempToken":null,"accessToken":"<JWT>","refreshToken":"a5776bf7-5f1c-47f0-9e30-426743fc0ad6.821bf966-ac7a-4bdf-a966-99b028f53e07","message":"Login successful"}
```

Pass condition:
- `authenticated = true`
- `role = CLIENT`
- `accessToken` is not null

## 2) Security Check Without Token

Endpoint:
- `GET /api/v1/client/account`

Command used:
```bash
curl -s -i -X GET http://localhost:8080/api/v1/client/account
```

Output:
```http
HTTP/1.1 403
```

Pass condition:
- request is blocked without JWT

## 3) Initial DB Snapshot Before Update

Command used:
```bash
PGPASSWORD='L30NSK3nNedY' psql -h localhost -U tuniway_admin -d tuniway_db -c "
SELECT u.id, u.email, u.status, u.last_login_at, cp.username, cp.first_name, cp.last_name, cp.phone, cp.birth_date
FROM users u
JOIN client_profiles cp ON cp.user_id = u.id
WHERE u.email = 'refreshclient@test.com';
"
```

Output:
```text
33333333-3333-3333-3333-333333333333 | refreshclient@test.com | ACTIVE | 2026-03-22 16:29:23.201409 | refreshclient | Refresh | Client | 222 | 2026-03-21 22:35:21.651742
```

## 4) GET /api/v1/client/dashboard

Command used:
```bash
curl -s -i -X GET http://localhost:8080/api/v1/client/dashboard \
  -H "Authorization: Bearer $CLIENT_TOKEN"
```

Input:
- no body

Output:
```http
HTTP/1.1 200
```

```json
{"success":true,"message":"Client dashboard retrieved successfully","clientId":"33333333-3333-3333-3333-333333333333","email":"refreshclient@test.com","username":"refreshclient","displayName":"Refresh Client","status":"ACTIVE","createdAt":"2026-03-21T22:35:21.651742Z","lastLoginAt":"2026-04-09T17:51:42.862059Z","emailVerified":true,"profileComplete":true,"missingProfileFields":[]}
```

Pass condition:
- `success = true`
- `clientId` matches logged-in client
- `profileComplete = true`
- `missingProfileFields` is empty

## 5) GET /api/v1/client/account

Command used:
```bash
curl -s -i -X GET http://localhost:8080/api/v1/client/account \
  -H "Authorization: Bearer $CLIENT_TOKEN"
```

Input:
- no body

Output:
```http
HTTP/1.1 200
```

```json
{"success":true,"message":"Client account retrieved successfully","id":"33333333-3333-3333-3333-333333333333","email":"refreshclient@test.com","role":"CLIENT","status":"ACTIVE","createdAt":"2026-03-21T22:35:21.651742Z","lastLoginAt":"2026-04-09T17:51:42.862059Z","username":"refreshclient","firstName":"Refresh","lastName":"Client","phone":"222","birthDate":"2026-03-21T22:35:21.651742Z"}
```

Pass condition:
- `success = true`
- `role = CLIENT`
- returned profile fields match the DB snapshot

## 6) PATCH /api/v1/client/account Happy Path

Temporary update values used:
```json
{"firstName":"RefreshUpdated","phone":"223"}
```

Command used:
```bash
curl -s -i -X PATCH http://localhost:8080/api/v1/client/account \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"RefreshUpdated","phone":"223"}'
```

Output:
```http
HTTP/1.1 200
```

```json
{"success":true,"message":"Client account updated successfully","id":"33333333-3333-3333-3333-333333333333","email":"refreshclient@test.com","role":"CLIENT","status":"ACTIVE","createdAt":"2026-03-21T22:35:21.651742Z","lastLoginAt":"2026-04-09T17:51:42.862059Z","username":"refreshclient","firstName":"RefreshUpdated","lastName":"Client","phone":"223","birthDate":"2026-03-21T22:35:21.651742Z"}
```

Pass condition:
- `success = true`
- `firstName` becomes `RefreshUpdated`
- `phone` becomes `223`

## 7) GET /api/v1/client/account After Update

Command used:
```bash
curl -s -i -X GET http://localhost:8080/api/v1/client/account \
  -H "Authorization: Bearer $CLIENT_TOKEN"
```

Output:
```http
HTTP/1.1 200
```

```json
{"success":true,"message":"Client account retrieved successfully","id":"33333333-3333-3333-3333-333333333333","email":"refreshclient@test.com","role":"CLIENT","status":"ACTIVE","createdAt":"2026-03-21T22:35:21.651742Z","lastLoginAt":"2026-04-09T17:51:42.862059Z","username":"refreshclient","firstName":"RefreshUpdated","lastName":"Client","phone":"223","birthDate":"2026-03-21T22:35:21.651742Z"}
```

Pass condition:
- the GET response reflects the updated values

## 8) PATCH /api/v1/client/account With Empty Body

Command used:
```bash
curl -s -i -X PATCH http://localhost:8080/api/v1/client/account \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{}'
```

Output:
```http
HTTP/1.1 400
```

```json
{"success":false,"message":"At least one field is required","id":null,"email":null,"role":null,"status":null,"createdAt":null,"lastLoginAt":null,"username":null,"firstName":null,"lastName":null,"phone":null,"birthDate":null}
```

Pass condition:
- request is rejected with `400`
- message is `At least one field is required`

## 9) PATCH /api/v1/client/account With Duplicate Username

Existing username found in DB:
- `client2fa`

Command used:
```bash
curl -s -i -X PATCH http://localhost:8080/api/v1/client/account \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"username":"client2fa"}'
```

Output:
```http
HTTP/1.1 400
```

```json
{"success":false,"message":"A client with this username already exists","id":null,"email":null,"role":null,"status":null,"createdAt":null,"lastLoginAt":null,"username":null,"firstName":null,"lastName":null,"phone":null,"birthDate":null}
```

Pass condition:
- request is rejected with `400`
- duplicate username validation works

## 10) Restore Original Profile Values

Restore body used:
```json
{"firstName":"Refresh","phone":"222"}
```

Command used:
```bash
curl -s -i -X PATCH http://localhost:8080/api/v1/client/account \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Refresh","phone":"222"}'
```

Output:
```http
HTTP/1.1 200
```

```json
{"success":true,"message":"Client account updated successfully","id":"33333333-3333-3333-3333-333333333333","email":"refreshclient@test.com","role":"CLIENT","status":"ACTIVE","createdAt":"2026-03-21T22:35:21.651742Z","lastLoginAt":"2026-04-09T17:51:42.862059Z","username":"refreshclient","firstName":"Refresh","lastName":"Client","phone":"222","birthDate":"2026-03-21T22:35:21.651742Z"}
```

## 11) Final Verification After Restore

### 11.1 API State

Command used:
```bash
curl -s -i -X GET http://localhost:8080/api/v1/client/account \
  -H "Authorization: Bearer $CLIENT_TOKEN"
```

Output:
```http
HTTP/1.1 200
```

```json
{"success":true,"message":"Client account retrieved successfully","id":"33333333-3333-3333-3333-333333333333","email":"refreshclient@test.com","role":"CLIENT","status":"ACTIVE","createdAt":"2026-03-21T22:35:21.651742Z","lastLoginAt":"2026-04-09T17:51:42.862059Z","username":"refreshclient","firstName":"Refresh","lastName":"Client","phone":"222","birthDate":"2026-03-21T22:35:21.651742Z"}
```

### 11.2 DB State

Command used:
```bash
PGPASSWORD='L30NSK3nNedY' psql -h localhost -U tuniway_admin -d tuniway_db -c "
SELECT u.email, cp.username, cp.first_name, cp.last_name, cp.phone, cp.birth_date
FROM users u
JOIN client_profiles cp ON cp.user_id = u.id
WHERE u.email = 'refreshclient@test.com';
"
```

Output:
```text
refreshclient@test.com | refreshclient | Refresh | Client | 222 | 2026-03-21 22:35:21.651742
```

Pass condition:
- API response and DB row are aligned
- original seeded values are restored

## 12) Conclusion

Result:
- `GET /api/v1/client/dashboard` works
- `GET /api/v1/client/account` works
- `PATCH /api/v1/client/account` works
- route protection works
- validation for empty PATCH body works
- validation for duplicate username works

Overall status:
- PASS
