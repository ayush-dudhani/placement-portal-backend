# Placement Portal backend

Spring Boot 3 / Java 17 REST backend for institution-scoped student and placement administration workspaces.

## Run locally

Prerequisites: JDK 17, PostgreSQL, and Redis. Docker Compose provides both data services and can be started with `docker compose up -d postgres redis`.

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/placementdb'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$env:SPRING_DATASOURCE_PASSWORD = 'postgres'
$env:SPRING_DATA_REDIS_HOST = 'localhost'
$env:SPRING_DATA_REDIS_PORT = '6379'
$env:JWT_SECRET = 'replace-with-at-least-32-random-bytes'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
.\mvnw.cmd spring-boot:run
```

Flyway applies all schema changes. Hibernate runs in `validate` mode and never mutates the production schema.

Redis is required in normal runtime mode. It stores active access-token sessions, distributed rate-limit counters, and short-lived read caches. Set `APP_REDIS_ENABLED=false` only for isolated single-process development or tests; that activates explicit in-memory fallbacks and is not suitable for a multi-instance deployment.

If Redis is unavailable, authentication-dependent operations return `503 REDIS_UNAVAILABLE` rather than a generic internal error. For local development, start the bundled service with `docker compose up -d postgres redis`; for a deployed instance, set `APP_REDIS_ENABLED=true`, `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`, credentials, and TLS settings for your managed Redis provider.

## Verify

```powershell
.\mvnw.cmd test
```

API documentation is available at `http://localhost:8080/swagger-ui.html` and `/v3/api-docs`.

Documentation:

- [Internal architecture](docs/architecture.md)
- [Important API sequence flows](docs/api-flows.md)
- [Domain data model](docs/data-model.md)
- [API contract](docs/api-contract.md)
- [Baseline gap analysis](docs/gap-analysis.md)
- [Implementation phase report](docs/phase-report.md)

## Production checklist

- Rotate the database and Redis credentials that existed in earlier Git history.
- Use a strong external `JWT_SECRET` and HTTPS with `AUTH_REFRESH_COOKIE_SECURE=true`.
- Configure exact comma-separated `CORS_ALLOWED_ORIGINS` values.
- Configure authenticated/TLS Redis through `SPRING_DATA_REDIS_*`; do not expose port 6379 publicly.
- Replace/configure `ObjectStorageService` for managed object storage.
- Apply Flyway migrations to staging PostgreSQL and restore-test a production backup before deployment.
- Connect a mail adapter for password-reset and email-verification token delivery.
