# Backend audit and gap analysis

## Audited baseline

The original repository contained three JPA entities (`User`, `Student`, and a skeletal `JobApplication`), two repositories, a JWT utility/filter, BCrypt, Redis-backed access-token sessions, four auth operations, two unversioned student-profile routes, one incomplete SQL file, and one mocked context test. All Java sources, resources, build/deployment files, and tests were reviewed before implementation.

Reusable behavior retained:

- numeric `BIGINT` identifiers, avoiding an unsafe UUID conversion;
- `users`, `students`, and `job_applications` tables/columns;
- BCrypt password hashes and the JWT claim shape needed by the canonical v1 security filter;
- username-or-email login and default `STUDENT` registration;
- Canonical `/api/v1` authentication and student profile routes;
- existing student profile columns, synchronized with normalized academic/skill/document records.

## Baseline gaps

- Secrets were committed in `application.properties`; those credentials must be rotated because removing them does not invalidate Git history.
- Hibernate `ddl-auto=update` was the schema manager. The SQL file was not a migration and ended with invalid syntax.
- JWT claims were trusted without loading an active user; paths did not match `/api/v1`; refresh/reset/verification/session and tenant scope were absent.
- No RFC 7807 errors, OpenAPI, pagination envelope, optimistic locking, idempotency, audit log, object-storage abstraction, or business workflow authorization existed.
- Every requested domain after the basic profile was absent.
- The only test bypassed persistence and did not test security or behavior.

## Implemented result

Flyway V1–V5 now manage the additive schema. The original JPA entities were expanded rather than duplicated, all API resources use DTOs, and tenant ownership is checked in service/repository lookups. The full requested route inventory is implemented; integration boundaries and deviations are recorded in [api-contract.md](api-contract.md).
