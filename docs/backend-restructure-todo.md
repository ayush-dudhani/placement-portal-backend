# Backend restructuring and API review TODO

## Current state

- 149 production Java files are arranged primarily by technical type: `entity` (29), `repository` (28), `service` (24), `controller` (15), plus cross-cutting `config`, `security`, `filter`, `exception`, `api`, `models`, `enums`, and `util` packages.
- There are currently 83 controller mappings. Several controllers are compressed into one-line nested classes (`AnalyticsController`, `CommunicationController`, `OfferController`), which makes review and ownership difficult.
- The frontend now consumes canonical authentication plus student companies, drives, applications, profile, academics, and skills APIs. Most admin screens and several detailed student screens still use mock data.
- No endpoint should be removed solely because it is not currently called by the frontend. First classify it as required contract, backend-only workflow, future workflow, or candidate for removal.

## Target package layout

Use feature-first packages with a small shared kernel. Each feature may contain `web` (controllers and request/response DTOs), `application` (use cases/services), `domain` (entities, value objects, enums, policies), and `persistence` (repositories and mappers).

```text
com.keepcalm.placementportal
├── common
│   ├── api              # pagination, problem details, response conventions
│   ├── config           # Spring, Redis, OpenAPI, storage configuration
│   ├── security         # JWT, principals, authorization policies
│   ├── error            # domain exceptions and global handlers
│   └── audit            # audit event contract and writer
├── auth                 # registration, login, refresh, logout, password lifecycle
├── institution          # institution/college scope and administration
├── student              # profile, academics, skills, documents, student dashboard
├── company              # directory, contacts, FAQs, Q&A
├── drive                # campus drives, roles, eligibility rules
├── application          # submission, status transitions, history, bulk operations
├── selection            # rounds, participants, attendance, results
├── offer                # offers and placement outcomes
├── communication        # notifications and announcements
├── analytics             # dashboards, reports, exports
└── PlacementPortalApplication.java
```

Do not create duplicate entities during the move. Move one aggregate at a time, preserve table/column mappings, and keep Flyway migrations independent of Java package names.

## Migration sequence

- [ ] Freeze the canonical `/api/v1` contract and record every endpoint in an inventory.
- [ ] Add an endpoint ownership table: method, path, role, frontend consumer, service, entity/tables, status, and decision.
- [ ] Split compressed controllers into one class per resource without changing paths.
- [ ] Move cross-cutting types first: `api`, `exception`, `config`, `security`, and `filter` into `common`.
- [ ] Move `auth` and `institution` as the first vertical slices; run auth integration/security tests.
- [ ] Move `student` and document storage; run ownership/IDOR and upload tests.
- [ ] Move `company` and `drive`; run eligibility, deadline, publication, and pagination tests.
- [ ] Move `application`; verify unique `(drive_role_id, student_id)`, immutable history, idempotency, and optimistic locking.
- [ ] Move `selection`, `offer`, `communication`, and `analytics` in separate changes.
- [ ] Delete technically unused classes only after compile, OpenAPI snapshot, and endpoint tests prove no references remain.
- [ ] Update package names/imports in one bounded change; run `mvn test` after every feature move.

## Endpoint review checklist

For each endpoint, verify:

- [ ] It is in the canonical `/api/v1` contract or explicitly marked backend-only.
- [ ] It has one owning feature package and one application service/use case.
- [ ] It returns a DTO, not a JPA entity.
- [ ] It has authentication, role, institution-scope, and ownership checks.
- [ ] List endpoints support the required filters and the standard paginated response.
- [ ] Mutations validate input, use transactions where required, and produce audit/history records.
- [ ] Errors map to RFC 7807 Problem Details with stable `code` values.
- [ ] Idempotency and optimistic locking are applied where the contract requires them.
- [ ] There is a controller/security/integration test for the important success and failure paths.
- [ ] The frontend consumer is wired, or the endpoint is documented as not yet consumed.

## Initial endpoint ownership buckets

| Feature | Canonical endpoints | Current frontend state |
|---|---|---|
| Auth/me | `/api/v1/auth/**`, `/api/v1/me/**` | Consumed by login/logout; profile identity partly consumed |
| Student | `/api/v1/students/me/**` | Profile, academics, skills, drives, applications being wired; documents/events pending |
| Companies | `/api/v1/companies/**` | Directory being wired; details/Q&A pending |
| Drives | `/api/v1/drives/**` | Discovery being wired; eligibility/application submission pending UI wiring |
| Applications | `/api/v1/students/me/applications/**` | List being wired; detail/history/withdraw pending |
| Admin | `/api/v1/admin/**` | Mostly mock UI; review before frontend integration |
| Selection/offers | `/api/v1/admin/**/rounds/**`, `/api/v1/**/offers/**` | Not yet consumed |
| Communication | `/api/v1/notifications/**`, `/api/v1/announcements/**` | Notifications partly present; announcement UI pending |
| Analytics | `/api/v1/admin/analytics/**` | Mock dashboard; integration pending |

## Rules for cleanup

- Do not remove database columns or rename tables during package restructuring.
- Do not silently change URL paths, JSON names, status values, or pagination fields.
- Prefer package moves and thin adapters over duplicate implementations.
- Keep static frontend fallback data only behind an API-failure branch; a successful empty API response must remain empty.
- Remove an endpoint only after checking OpenAPI, Postman, frontend references, tests, and documented product workflows.
