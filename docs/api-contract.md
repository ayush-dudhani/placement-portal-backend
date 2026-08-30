# Placement Portal API contract

## Runtime contract

- Base URL: `/api/v1`
- Authentication: `Authorization: Bearer <accessToken>` except public auth operations and actuator health/info.
- Access tokens default to 15 minutes. Every issued token also has an institution/user/JTI-scoped active session in Redis with the same TTL, and a bearer token is rejected when that session is absent. Rotating opaque refresh tokens are stored hashed in PostgreSQL and sent as an `HttpOnly`, `SameSite=Strict` cookie. Set `AUTH_REFRESH_COOKIE_SECURE=true` under HTTPS.
- IDs remain numeric because that was the repository's established standard.
- Times are UTC ISO-8601 instants; dates are ISO `yyyy-MM-dd`.
- Money is `{ "packageAmount": 2800000.00, "currency": "INR" }`.
- Live OpenAPI: `/v3/api-docs`; Swagger UI: `/swagger-ui.html`.
- Canonical success responses are DTOs. Empty mutation responses use `204 No Content`.

Paginated response:

```json
{"content":[],"page":0,"size":20,"totalElements":0,"totalPages":0}
```

RFC 7807 error:

```json
{
  "type":"https://api.example.com/problems/eligibility-failed",
  "title":"ELIGIBILITY FAILED",
  "status":422,
  "detail":"CGPA is 7.5; minimum 8.0",
  "instance":"/api/v1/drives/1/roles/2/applications",
  "code":"ELIGIBILITY_FAILED",
  "fieldErrors":[]
}
```

List endpoints accept the applicable subset of `page`, `size`, `sort`, `query`, `branch`, `graduationYear`, `status`, `companyId`, `driveId`, and `roleId`. Export endpoints reuse the same service filters as their corresponding lists.

## Authentication and compatibility

| Method | Path | Access |
|---|---|---|
| POST | `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/verify-email` | Public/cookie as applicable |
| GET | `/auth/session` | Authenticated |
| GET, PATCH | `/me` | Authenticated owner |
| PUT | `/me/password` | Authenticated owner |

`register` accepts `email`, `username`, `password`, and optional `institutionCode` (`DEFAULT` when omitted). Login accepts `username` (username or email), `password`, and optional `institutionCode`.

Preferred login response:

```json
{
  "accessToken":"...",
  "tokenType":"Bearer",
  "expiresIn":900,
  "user":{"id":1,"username":"asha","email":"asha@example.com","role":"STUDENT"},
  "token":"...",
  "username":"asha",
  "email":"asha@example.com",
  "role":"STUDENT"
}
```

The login response is canonical and contains only `accessToken`, `tokenType`, `expiresIn`, and the nested `user` object. Legacy aliases and non-versioned compatibility routes have been removed; clients must use the `/api/v1` contract.

Forgot-password and verification create hashed, expiring single-use tokens. Raw-token delivery is intentionally delegated to a mail adapter and tokens are never returned or logged.

Login, registration, forgot-password, password-reset, and email-verification limits use atomic Redis fixed-window counters with hashed keys. Logout should send both the refresh cookie and the current bearer token: the refresh token is revoked durably, the Redis access session is deleted, and subsequent use of that JWT returns 401 immediately. Password changes and password resets revoke every refresh token and every Redis access session for the user.

If Redis cannot be reached, operations that require Redis return `503 REDIS_UNAVAILABLE`; the backend does not silently issue an access token without a live session authority. The health endpoint also reports the Redis dependency state when Redis is enabled.

Deployment note: access tokens issued before Redis session enforcement do not have a corresponding active-session key and will require users to log in again. This is an intentional fail-closed security transition.

## Student workspace

- Profile: `GET|PUT /students/me/profile`, `GET /students/me/profile/completion`, `GET /students/me/dashboard`, `GET /students/me/eligibility-summary`.
- Academics: `GET|PUT /students/me/academics`.
- Skills: `GET|PUT /students/me/skills`.
- Documents: `GET|POST /students/me/documents`, `GET|DELETE /students/me/documents/{documentId}`, `POST /students/me/documents/{documentId}/set-primary`.
- Applications: `GET /students/me/applications`, `GET /students/me/applications/{id}`, `GET /students/me/applications/{id}/history`, `POST /students/me/applications/{id}/withdraw`.
- Events: `GET /students/me/events`, `GET /students/me/calendar`.
- Offers: `GET /students/me/offers`, `GET /students/me/offers/{id}`, `POST /students/me/offers/{id}/accept`, `POST /students/me/offers/{id}/decline`.

Document upload is multipart with `documentType` and `file`. Allowed content is PDF, DOCX, PNG, or JPEG, validated using declared media type, size, and magic bytes. PostgreSQL stores metadata/object keys only. The local storage adapter is replaceable by S3/GCS/Azure Blob. `GET /documents/{id}` returns protected metadata; a signed object-download adapter is the intended production delivery mechanism.

Students never supply a student ID for owner resources. Services resolve it from the validated JWT principal.

## Companies, drives, and applying

- Companies: `GET /companies`, `GET /companies/{id}`, and `/outcomes`, `/alumni`, `/faqs`, `/questions`; `POST /companies/{id}/questions`; `POST /companies/{id}/questions/{questionId}/replies`.
- Drives: `GET /drives`, `GET /drives/{id}`, `GET /drives/{id}/roles`, `GET /drives/{id}/roles/{roleId}`, `GET /drives/{id}/roles/{roleId}/eligibility`.
- Apply: `POST /drives/{driveId}/roles/{roleId}/applications` with optional `Idempotency-Key`.

Eligibility rule configuration examples:

```json
[
  {"ruleType":"MIN_CGPA","configuration":{"value":8.0},"mandatory":true,"active":true},
  {"ruleType":"MAX_ACTIVE_BACKLOGS","configuration":{"value":0},"mandatory":true,"active":true},
  {"ruleType":"ALLOWED_BRANCHES","configuration":{"values":["CSE","IT"]},"mandatory":true,"active":true},
  {"ruleType":"GRADUATION_YEARS","configuration":{"values":["2027"]},"mandatory":true,"active":true}
]
```

`GENDER_RULE` additionally requires `"institutionallyApproved": true`. `CUSTOM_RULE` fails closed until a custom evaluator is registered.

The backend enforces drive state/opening/deadline, one application per `(driveRole, student)`, and stores the evaluated profile/rule versions plus explanations in an immutable JSONB snapshot. An ineligible attempt is persisted with `INELIGIBLE` and returns 422. Later profile edits never rewrite that decision.

## Communications

- Notifications: `GET /notifications`, `GET /notifications/unread-count`, `PATCH /notifications/{id}/read`, `POST /notifications/read-all`.
- Announcements: `GET /announcements`, `GET /announcements/{id}`.

## Admin workspace

- Dashboard: `GET /admin/dashboard`, `/admin/action-items`, `/admin/activity`.
- Companies: `POST /admin/companies`, `PUT|DELETE /admin/companies/{id}`, `POST /admin/companies/{id}/contacts`, `PUT /admin/companies/{id}/contacts/{contactId}`.
- Drives: `GET|POST /admin/drives`, `GET|PUT|DELETE /admin/drives/{id}`, plus `publish`, `close-applications`, `cancel`, `clone`, role CRUD, `PUT eligibility-rules`, `GET eligibility-preview`, `GET applicants`, and `GET applicants/export`.
- Applications: `GET /admin/applications`, `GET /admin/applications/{id}`, `PATCH /admin/applications/{id}/status`, `POST bulk-eligibility-check`, `POST bulk-status-update`, `POST import-results`.
- Rounds: CRUD under `/admin/drives/{driveId}/rounds`; participant list, transactional result update, and result publication.
- Offers: `GET|POST /admin/offers`, `PUT /admin/offers/{id}`, `POST /admin/offers/import`.
- Students: `GET /admin/students`, `GET /admin/students/{id}`, placement-status patch, application/document views, document verification, export, and import.
- Analytics: `/admin/analytics/overview`, `/applications`, `/placements-by-branch`, `/packages`, `/company-performance`, `/export`.
- Announcements: `POST /admin/announcements`, `PUT|DELETE /admin/announcements/{id}`, `POST /admin/announcements/{id}/publish`.

Admin routes accept `ADMIN`, `PLACEMENT_OFFICER`, or `COORDINATOR`; public registration can only create `STUDENT`. This permits future delegated admin roles without changing controller authorization structure.

A drive cannot publish without an active company, owner, opening/deadline and start/end dates, an active role, and an active eligibility rule. Published resources are archived/status-transitioned instead of destructively deleted. Sensitive mutations write immutable institution-scoped audit records.

Bulk result/student/offer imports require `Idempotency-Key`. Reusing a key with identical bytes replays the saved result; using it with different content returns 409. CSV processing is synchronous and strict; large production imports should use a queued job.

`PLACED` is never inferred from application counts or manually assigned. It is derived only when a verified, released offer is accepted, which creates a `PlacementOutcome`.

## Operational and deployment

- `GET /actuator/health`
- `GET /actuator/info`

Redis also backs institution/role-scoped company, drive, announcement, and analytics caches and user-scoped unread-notification counters. Cache TTLs are 5 minutes for companies, 1 minute for drives/analytics/announcements, and 30 seconds for unread counts. Relevant mutations evict cached values; PostgreSQL remains the source of truth.

Required production configuration: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATA_REDIS_HOST`, Redis credentials/TLS as applicable, a strong `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `AUTH_REFRESH_COOKIE_SECURE=true`, and a production object-storage implementation/configuration. All previously committed database/Redis credentials must be rotated. `APP_REDIS_ENABLED=false` is an explicit test/development-only fallback and must not be used for horizontally scaled production instances.

## Curl examples

```bash
# Register and login (save refresh cookie)
curl -X POST http://localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"asha","email":"asha@example.com","password":"StrongPass1!","institutionCode":"DEFAULT"}'
curl -c cookies.txt -X POST http://localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"asha","password":"StrongPass1!","institutionCode":"DEFAULT"}'

# Academic record
curl -X PUT http://localhost:8080/api/v1/students/me/academics -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"cgpa":8.6,"tenthPercentage":91,"twelfthPercentage":88,"activeBacklogs":0,"branch":"CSE","graduationYear":2027}'

# Apply idempotently
curl -X POST http://localhost:8080/api/v1/drives/12/roles/34/applications \
  -H "Authorization: Bearer $TOKEN" -H 'Idempotency-Key: asha-drive12-role34'

# Publish a configured drive
curl -X POST http://localhost:8080/api/v1/admin/drives/12/publish -H "Authorization: Bearer $ADMIN_TOKEN"
```
