# Placement Portal backend architecture

This document describes the implemented backend, its trust boundaries, component responsibilities, and runtime data stores. Detailed request sequences are in [api-flows.md](api-flows.md), and the relational model is in [data-model.md](data-model.md).

## System context

```mermaid
flowchart LR
    Student[Student browser\nReact + Vite]
    Admin[Admin browser\nReact + Vite]
    API[Spring Boot REST API\n/api/v1]
    PG[(PostgreSQL\nsource of truth)]
    Redis[(Redis\naccess sessions\nrate limits\nread caches)]
    Objects[(Object storage\ndocument bytes)]
    Mail[Planned mail adapter\nnot currently wired]
    Docs[OpenAPI and Swagger]

    Student -->|HTTPS + bearer JWT| API
    Admin -->|HTTPS + bearer JWT| API
    API -->|JPA transactions| PG
    API -->|session checks and cache operations| Redis
    API -->|validated upload/download operations| Objects
    API -.->|required delivery integration| Mail
    API --> Docs
```

PostgreSQL owns durable business state. Redis is deliberately not the source of truth for students, drives, applications, offers, or outcomes. It is the authority for whether an access JWT is currently active and is also used for distributed counters and disposable read caches. Object storage owns file bytes; PostgreSQL stores only document metadata and object keys.

## Internal component layers

```mermaid
flowchart TB
    subgraph Edge[HTTP and security edge]
        CORS[CORS configuration]
        Filter[JwtAuthenticationFilter]
        Chain[Spring Security filter chain]
        MethodAuth[Method and route authorization]
        Validation[Bean Validation]
        Errors[GlobalExceptionHandler\nRFC 7807]
    end

    subgraph Controllers[REST controllers]
        AuthC[Auth and /me]
        StudentC[Student self-service]
        DiscoveryC[Companies, drives and Q&A]
        ApplicationC[Applications and offers]
        CommunicationC[Notifications and announcements]
        AdminC[Admin management, rounds and analytics]
    end

    subgraph Domain[Transactional domain services]
        AuthS[AuthService]
        Current[CurrentUserService]
        StudentS[Student and Document services]
        CompanyS[Company services]
        DriveS[Drive services]
        Eligibility[EligibilityEngine]
        ApplicationS[Application services\nand status policy]
        RoundS[RoundService]
        OfferS[OfferService]
        CommsS[Notification and Announcement services]
        AnalyticsS[AnalyticsService]
        AuditS[AuditService]
        RateS[RateLimitService]
    end

    subgraph Persistence[Persistence and infrastructure adapters]
        Repos[Spring Data JPA repositories]
        SessionStore[AccessSessionStore]
        Cache[Spring Cache abstractions]
        ObjectAdapter[ObjectStorageService]
        Jwt[JwtUtil]
    end

    CORS --> Filter --> Chain --> MethodAuth --> Controllers
    Controllers --> Validation
    Controllers --> Domain
    Domain --> Persistence
    Domain --> Errors
    Repos --> PG[(PostgreSQL)]
    SessionStore --> R[(Redis)]
    Cache --> R
    RateS --> R
    ObjectAdapter --> OS[(Object storage)]
    Jwt --> Filter
```

Controllers accept validated request DTOs and return response DTOs. They do not expose JPA entities. Services enforce ownership, institution scope, state transitions, deadlines, idempotency, and transaction boundaries. Repositories perform filtered persistence operations but are not treated as authorization boundaries.

## Authenticated request pipeline

```mermaid
flowchart TD
    Request[HTTP request] --> Cors{Origin allowed?}
    Cors -->|No| CorsReject[CORS rejection]
    Cors -->|Yes or non-browser| Header{Bearer header present?}
    Header -->|No| Public{Public route?}
    Public -->|Yes| Controller
    Public -->|No| U401[401 Problem Details]

    Header -->|Yes| Parse[Verify JWT signature\nand expiration]
    Parse -->|Invalid or expired| U401
    Parse --> Claims[Extract uid, iid, role and jti]
    Claims --> User[(Load User from PostgreSQL)]
    User --> Active{User exists and active?}
    Active -->|No| U401
    Active -->|Yes| Session{Redis access session exists?}
    Session -->|No or Redis unavailable| U401
    Session -->|Yes| Principal[Create PortalPrincipal\nuserId, institutionId, username, role]
    Principal --> RouteRole{Route role permitted?}
    RouteRole -->|No| F403[403 Problem Details]
    RouteRole -->|Yes| Controller[Controller + Bean Validation]
    Controller --> Scope[Service ownership and\ninstitution checks]
    Scope -->|Not owned or out of scope| Safe404[404 or 403 Problem Details]
    Scope -->|Allowed| Tx[Transactional workflow]
    Tx --> DTO[DTO response]
```

The route-level checks and service-level checks solve different problems:

- Spring Security decides whether a role may enter a route family.
- Services prevent IDOR by resolving owner resources from `PortalPrincipal` or querying by both resource ID and institution/user ID.
- Repository filters support those checks but never replace them.

## Authorization model

```mermaid
flowchart LR
    Principal[PortalPrincipal]
    StudentRole[STUDENT]
    AdminRole[ADMIN]
    Future[PLACEMENT_OFFICER\nCOORDINATOR]
    Self[students/me resources\nown profile, documents, applications, offers]
    Shared[authenticated discovery\ncompanies, published drives, announcements]
    Admin[admin resources\ninstitution-scoped management]

    Principal --> StudentRole --> Self
    StudentRole --> Shared
    Principal --> AdminRole --> Admin
    AdminRole --> Shared
    Principal --> Future --> Admin
```

All tenant-aware records are scoped by `institutionId`. `PLACEMENT_OFFICER` and `COORDINATOR` are already accepted by the admin route family so delegated roles can be refined later without redesigning authentication.

## Runtime state ownership

| State | Owner | Retention and behavior |
|---|---|---|
| Users, profiles, academics, drives, applications, offers and outcomes | PostgreSQL | Durable, transactionally updated, migrated by Flyway |
| Hashed refresh tokens | PostgreSQL | Durable, rotated on refresh and revoked on logout/password reset |
| Active access JWT sessions | Redis | Keyed by institution, user and JTI; TTL equals JWT lifetime |
| Login/reset/verification counters | Redis | Atomic fixed-window counters; keys hash identifying input |
| Companies, drives, analytics and announcements cache | Redis | Institution/role scoped and evicted after relevant mutations |
| Unread notification count cache | Redis | Institution/user scoped; evicted on notify/read operations |
| Resume/document bytes | Object storage | Validated before upload; only metadata/object key is in PostgreSQL |
| Audit history and application status history | PostgreSQL | Append-oriented permanent records |

## Redis key families

```text
placement:auth:session:{institutionId}:{userId}:{jti}
placement:auth:user-sessions:{institutionId}:{userId}
placement:rate-limit:{sha256-of-logical-key}
companies::{institution-and-role-aware-cache-key}
drives::{institution-and-role-aware-cache-key}
analytics::{institution-and-role-aware-cache-key}
announcements::{institution-and-role-aware-cache-key}
notification-unread::{institution-and-user-aware-cache-key}
```

The user-session set permits logout-all/password-reset revocation without using the Redis `KEYS` command. If Redis is unavailable, protected requests fail closed because the session cannot be proven active. `APP_REDIS_ENABLED=false` selects explicit single-process in-memory implementations only for tests and isolated development.

## Component catalog

| Area | Controllers | Primary services | Main durable records |
|---|---|---|---|
| Identity | `AuthController`, `MeController` | `AuthService`, `UserService`, `CurrentUserService`, `RateLimitService` | `Institution`, `User`, `RefreshToken`, `PasswordResetToken` |
| Student self-service | `StudentSelfController`, `StudentEventController` | `StudentService`, `DocumentService`, `StudentEventService` | `Student`, `AcademicRecord`, `Skill`, `StudentSkill`, `StudentDocument` |
| Companies and Q&A | `CompanyController`, `AdminCompanyController` | `CompanyService`, `AdminCompanyService` | `Company`, `CompanyContact`, `CompanyFaq`, `CompanyQuestion`, `QuestionReply` |
| Drives and applications | `DriveController`, `AdminDriveController`, `StudentApplicationController`, `AdminApplicationController` | `DriveDiscoveryService`, `AdminDriveService`, `EligibilityEngine`, `ApplicationService`, `AdminApplicationService`, `ApplicationStatusPolicy` | `CampusDrive`, `DriveRole`, `EligibilityRule`, `JobApplication`, `ApplicationStatusHistory` |
| Selection and placement | `RoundController`, `OfferController` | `RoundService`, `OfferService` | `SelectionRound`, `RoundParticipant`, `RoundResult`, `Offer`, `PlacementOutcome` |
| Communications | `CommunicationController` | `NotificationService`, `AnnouncementService` | `Notification`, `Announcement` |
| Administration | Admin controllers and analytics controller | `AdminStudentService`, `AnalyticsService`, `AuditService` | `AuditLog`, `IdempotencyRecord` plus domain records above |
| Infrastructure | Security/configuration classes | `ObjectStorageService`, `LocalObjectStorageService` | Redis state and external object bytes |

## Cross-cutting consistency rules

- Services that change applications, round results, offers, or drive configuration are transactional.
- Application eligibility is evaluated server-side and copied into an immutable application snapshot.
- Every application status change creates a separate history row.
- Frequently edited drives, applications, documents, rounds, offers, and other audited records use optimistic versions.
- Published drives, applications, results, offers, companies, and announcements use state transitions or archive flags instead of destructive deletion where history matters.
- CSV exports reuse the same institution scope and filters as list operations.
- Domain, validation, security, malformed request, integrity, and optimistic locking failures are returned as RFC 7807 Problem Details.

## Deployment view

```mermaid
flowchart TB
    LB[HTTPS reverse proxy or load balancer]
    A1[Spring Boot instance A]
    A2[Spring Boot instance B]
    PG[(PostgreSQL primary)]
    Redis[(Shared Redis)]
    Blob[(Managed object storage)]

    LB --> A1
    LB --> A2
    A1 --> PG
    A2 --> PG
    A1 --> Redis
    A2 --> Redis
    A1 --> Blob
    A2 --> Blob
```

Because access sessions and rate counters are shared in Redis, a JWT issued by instance A can be validated or revoked by instance B. PostgreSQL constraints remain the final defense for duplicates and referential integrity.
