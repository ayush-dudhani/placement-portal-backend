# Important API flows

These sequence diagrams show the implemented call paths, data stores, filters, failure branches, and transaction boundaries. Component definitions are in [architecture.md](architecture.md); entity relationships are in [data-model.md](data-model.md).

## 1. Registration

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant C as AuthController
    participant RL as RateLimitService
    participant R as Redis
    participant A as AuthService
    participant PG as PostgreSQL
    participant P as BCrypt
    participant Mail as Planned mail adapter

    Client->>C: POST /api/v1/auth/register
    C->>C: Validate email, username and password
    C->>A: signup(request, client IP)
    A->>RL: check register limit
    RL->>R: atomic INCR + PEXPIRE
    alt limit exceeded
        RL-->>Client: 429 RATE_LIMITED
    end
    A->>PG: Resolve active institution
    A->>PG: Check institution-scoped username/email uniqueness
    alt account exists
        A-->>Client: 409 ACCOUNT_EXISTS
    else new account
        A->>P: BCrypt hash password
        A->>PG: Insert User(role=STUDENT)
        A->>PG: Insert linked Student
        A->>PG: Insert hashed email-verification token
        A-.->Mail: Planned delivery integration, not currently wired
        A-->>Client: 201 UserSummary DTO
    end
```

Public registration cannot select an administrative role. The raw verification token is not returned or logged. A mail adapter/event handoff is still required; token delivery is not currently wired.

## 2. Login and access-session creation

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant C as AuthController
    participant RL as RateLimitService
    participant R as Redis
    participant A as AuthService
    participant PG as PostgreSQL
    participant B as BCrypt
    participant J as JwtUtil

    Client->>C: POST /api/v1/auth/login
    C->>C: Bean Validation
    C->>A: login(identifier, password, institutionCode, IP)
    A->>RL: check login:{IP}:{identifier}
    RL->>R: atomic counter, 15-minute TTL
    alt over 10 attempts
        A-->>Client: 429 RATE_LIMITED
    end
    A->>PG: Resolve institution and find user by username/email
    A->>B: matches(rawPassword, passwordHash)
    alt missing, inactive, or password mismatch
        A-->>Client: 401 INVALID_CREDENTIALS
    else credentials valid
        rect rgb(235, 245, 255)
            Note over A,PG: Transactional session issuance
            A->>PG: Insert SHA-256 hashed refresh token
            A->>J: Generate signed JWT with uid, iid, role and jti
            A->>R: SET active access session with JWT TTL
        end
        alt Redis write fails
            Note over A,PG: Authentication fails; DB transaction rolls back
            A-->>Client: 500; no usable session is issued
        else session created
            A-->>C: access token + user DTO + raw refresh token
            C-->>Client: 200 JSON + HttpOnly refresh cookie
        end
    end
```

The JWT itself is not stored as the Redis value. Redis stores an `active` marker keyed by institution, user, and JTI, avoiding token disclosure in cache data.

Login scenario matrix:

| Scenario | Components reached | Result |
|---|---|---|
| Malformed or missing request fields | Controller validation | `400 VALIDATION_FAILED` |
| Rate window exceeded | `RateLimitService` and Redis | `429 RATE_LIMITED` before password work |
| Institution/account missing | `AuthService` and PostgreSQL | `401 INVALID_CREDENTIALS` |
| User inactive | `AuthService` and PostgreSQL | `401 INVALID_CREDENTIALS` |
| Wrong password | PostgreSQL lookup and BCrypt | `401 INVALID_CREDENTIALS` |
| Redis unavailable during rate check/session registration | Redis and exception translation | Request fails; no usable authenticated session is returned |
| Valid credentials | PostgreSQL, BCrypt, JWT utility and Redis | `200`, access token, user DTO and refresh cookie |

## 3. Bearer-token validation for every protected route

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant F as JwtAuthenticationFilter
    participant J as JwtUtil
    participant PG as UserRepository
    participant R as AccessSessionStore / Redis
    participant S as Spring Security
    participant C as Controller
    participant D as Domain service

    Client->>F: Request + Authorization: Bearer JWT
    F->>J: Verify signature and expiration
    alt malformed, invalid signature, or expired
        F->>F: Clear SecurityContext
        S-->>Client: 401 UNAUTHENTICATED
    else token cryptographically valid
        F->>J: Extract uid, iid, role, jti
        F->>PG: Load current User
        F->>R: EXISTS session:{iid}:{uid}:{jti}
        alt user missing/inactive, session missing, or Redis unavailable
            F->>F: Do not authenticate
            S-->>Client: 401 UNAUTHENTICATED
        else active session
            F->>F: Build PortalPrincipal from current DB user
            F->>S: Continue filter chain
            alt route role not allowed
                S-->>Client: 403 FORBIDDEN
            else route role allowed
                S->>C: Invoke controller
                C->>D: Invoke scoped service
                D-->>Client: DTO response
            end
        end
    end
```

Loading the user on every authenticated request ensures account deactivation and role changes take effect without waiting for JWT expiration. Redis failure is fail-closed for protected routes.

Protected-request scenario matrix:

| Scenario | Decision point | Result |
|---|---|---|
| No bearer token on protected route | Security filter chain | `401 UNAUTHENTICATED` |
| Invalid signature or malformed JWT | `JwtUtil` | `401` |
| Expired JWT | `JwtUtil` | `401` |
| Valid JWT but missing/revoked Redis JTI | `AccessSessionStore` | `401` |
| Redis unavailable | `AccessSessionStore` | Fail closed with `401` for protected route |
| User deleted or inactive | PostgreSQL user lookup | `401` |
| Current DB role cannot access route | Spring Security authorization | `403 FORBIDDEN` |
| Role allowed but resource belongs to another user/institution | Domain service scoped lookup | Safe `404` or `403` |
| All checks pass | Controller and service | DTO response |

## 4. Refresh-token rotation

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant C as AuthController
    participant A as AuthService
    participant PG as PostgreSQL
    participant J as JwtUtil
    participant R as Redis

    Client->>C: POST /api/v1/auth/refresh + HttpOnly cookie
    C->>A: refresh(rawRefreshToken)
    A->>PG: Lookup SHA-256 token hash
    alt missing, expired, revoked, or user inactive
        A-->>Client: 401 INVALID_REFRESH_TOKEN
    else usable
        rect rgb(235, 245, 255)
            A->>PG: Mark old refresh token revoked
            A->>PG: Insert replacement refresh-token hash
            A->>J: Generate new access JWT and JTI
            A->>R: Register new active access session
        end
        A-->>Client: 200 new access token + rotated cookie
    end
```

Refresh rotation does not silently delete other active access sessions. Explicit logout revokes the current access session; password reset/change revokes all sessions.

## 5. Logout and immediate JWT rejection

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant F as JwtAuthenticationFilter
    participant C as AuthController
    participant A as AuthService
    participant PG as PostgreSQL
    participant R as Redis

    Client->>F: POST /api/v1/auth/logout + bearer JWT + refresh cookie
    F->>R: Confirm JWT session is active
    F->>C: Authenticated request
    C->>A: logout(refresh, access JWT)
    A->>PG: Mark matching refresh token revoked
    A->>R: DEL access session key and remove JTI from user set
    C-->>Client: 204 + expired refresh cookie

    Client->>F: Reuse the same JWT
    F->>R: Session key lookup
    R-->>F: Missing
    F-->>Client: 401 UNAUTHENTICATED
```

The frontend should send both cookie and bearer token on logout. If it sends only the cookie, refresh is revoked but the current access-session key cannot be identified and remains valid until its short TTL.

## 6. Forgot password, reset password, and verify email

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant C as AuthController
    participant RL as RateLimitService
    participant R as Redis
    participant A as AuthService
    participant PG as PostgreSQL
    participant Mail as Mail adapter boundary

    Client->>C: POST /api/v1/auth/forgot-password
    C->>RL: Rate-limit by client IP
    RL->>R: Atomic counter
    C->>A: forgotPassword(email, institution)
    A->>PG: Lookup active account
    opt account exists
        A->>PG: Store hashed, expiring, single-use reset token
        A-.->Mail: Planned delivery integration, not currently wired
    end
    C-->>Client: 202 generic message
    Note over Client,C: Response does not reveal whether the account exists

    Client->>C: POST /api/v1/auth/reset-password with raw token
    C->>RL: Rate-limit
    C->>A: Consume SHA-256 token hash
    A->>PG: Verify purpose, expiry, and unused state
    A->>PG: BCrypt-update password and revoke all refresh tokens
    A->>R: Delete every active access session for user
    C-->>Client: 204

    Client->>C: POST /api/v1/auth/verify-email with raw token
    C->>RL: Rate-limit
    C->>A: Consume verification token
    A->>PG: Mark email verified
    C-->>Client: 204
```

The durable token lifecycle is implemented, but raw reset/verification token delivery is a known integration boundary. A production mail adapter must receive the raw token without logging or persisting it in plaintext.

## 7. `/me` routes

### GET `/api/v1/me`

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant F as JWT + Redis filter
    participant C as MeController
    participant U as UserService
    participant CU as CurrentUserService
    participant PG as UserRepository

    Client->>F: GET /api/v1/me + bearer JWT
    F->>F: Validate JWT, user, and Redis session
    F->>C: PortalPrincipal in SecurityContext
    C->>U: me()
    U->>CU: getCurrentUser()
    CU->>PG: findById(principal.userId)
    PG-->>U: User + Institution
    U-->>Client: CurrentUserDto
```

The request never accepts a user ID. Identity always comes from the validated `PortalPrincipal`.

### PATCH `/api/v1/me`

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant F as JWT + Redis filter
    participant C as MeController
    participant U as UserService
    participant PG as PostgreSQL

    Client->>F: PATCH /me {username?, email?}
    F->>C: Authenticated principal
    C->>C: Validate username and email format
    C->>U: update(request)
    U->>PG: Load principal user
    U->>PG: Check institution-scoped uniqueness
    alt username or email already used
        U-->>Client: 409 USERNAME_EXISTS or EMAIL_EXISTS
    else accepted
        U->>PG: Update user
        opt email changed
            U->>PG: Set email_verified=false
        end
        U-->>Client: Updated CurrentUserDto
    end
```

### PUT `/api/v1/me/password`

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant F as JWT + Redis filter
    participant A as AuthService
    participant B as BCrypt
    participant PG as PostgreSQL
    participant R as Redis

    Client->>F: PUT /me/password
    F->>A: Authenticated request
    A->>PG: Load principal user
    A->>B: Verify old password
    alt old password incorrect
        A-->>Client: 422 INCORRECT_PASSWORD
    else correct
        A->>B: Hash new password
        A->>PG: Update password hash
        A->>PG: Revoke every refresh token
        A->>R: Delete all access sessions for user
        A-->>Client: 204
    end
```

The JWT used for the password-change request becomes invalid immediately after the transaction succeeds.

`/me` scenario matrix:

| Route/scenario | Durable writes | Session effect | Result |
|---|---|---|---|
| `GET /me` | None | None | Current user DTO |
| `PATCH /me` valid username | Update `users.username` | Existing sessions remain active | Updated DTO |
| `PATCH /me` valid email | Update email and set `email_verified=false` | Existing sessions remain active | Updated DTO |
| Duplicate username/email in same institution | None | None | `409` |
| Invalid email/username format | None | None | `400` |
| Password change with wrong old password | None | None | `422 INCORRECT_PASSWORD` |
| Successful password change | Update password; revoke all refresh tokens | Delete every Redis access session | `204`; re-login required |

## 8. Student profile, academics, skills, and completion

```mermaid
flowchart LR
    Req[students/me request] --> Filter[JWT + Redis validation]
    Filter --> StudentRole{ROLE_STUDENT?}
    StudentRole -->|No| Denied[403]
    StudentRole -->|Yes| Principal[Resolve User and Student by principal userId]
    Principal --> Operation{Operation}
    Operation --> Profile[Profile read/update]
    Operation --> Academics[Academic record read/update]
    Operation --> Skills[Replace normalized skill assignments]
    Operation --> Completion[Calculate profile completion]
    Profile --> PG[(PostgreSQL)]
    Academics --> PG
    Skills --> PG
    Completion --> PG
```

Student self-service routes do not accept `studentId`; therefore a student cannot switch ownership by changing a path parameter. Admin student routes use explicit student IDs but query them with the principal institution ID.

## 9. Document upload and verification

```mermaid
sequenceDiagram
    autonumber
    actor Student
    actor Admin
    participant C as StudentSelfController
    participant D as DocumentService
    participant OS as ObjectStorageService
    participant PG as PostgreSQL
    participant AC as AdminStudentController
    participant AS as AdminStudentService
    participant AU as AuditService

    Student->>C: multipart documentType + file
    C->>D: upload(type, file)
    D->>D: Validate non-empty, max size, media type, magic bytes and safe filename
    alt invalid content
        D-->>Student: 400, 413, or 415 Problem Details
    else valid
        D->>OS: Store bytes under institution/student prefix
        D->>PG: Insert metadata + object key + PENDING status
        D-->>Student: 201 StudentDocumentDto
    end

    Admin->>AC: PATCH /admin/students/{id}/documents/{id}/verification
    AC->>AS: verify within admin institution
    AS->>PG: Load document by documentId + studentId + institutionId
    AS->>PG: Set VERIFIED or REJECTED, note, verifiedBy
    AS->>AU: Immutable audit entry
    AS-->>Admin: StudentDocumentDto
```

Verified documents cannot be deleted by students. Large file bytes never enter PostgreSQL.

## 10. Drive discovery and eligibility preview

```mermaid
sequenceDiagram
    autonumber
    actor Student
    participant C as DriveController
    participant Cache as Redis drive cache
    participant D as DriveDiscoveryService
    participant PG as PostgreSQL
    participant E as EligibilityEngine

    Student->>C: GET /drives or /drives/{id}
    C->>Cache: Institution/role/filter cache lookup
    alt cache hit
        Cache-->>Student: Cached DTO
    else cache miss
        C->>D: Discover visible, non-archived drives
        D->>PG: Institution-scoped query
        D-->>Cache: Cache DTO for 1 minute
        D-->>Student: Paged DTO
    end

    Student->>C: GET /drives/{drive}/roles/{role}/eligibility
    C->>D: eligibility(drive, role)
    D->>PG: Load visible drive, active role, principal student, academics and rules
    D->>E: evaluate(student, academics, applicable rules)
    E-->>Student: eligible + explanations + current snapshot
```

Eligibility previews are not cached because they depend on the current student's academic/profile state.

## 11. Apply to a drive role

```mermaid
sequenceDiagram
    autonumber
    actor Student
    participant C as DriveController
    participant A as ApplicationService
    participant PG as PostgreSQL
    participant E as EligibilityEngine
    participant Cache as Analytics cache

    Student->>C: POST /drives/{drive}/roles/{role}/applications + Idempotency-Key
    C->>A: apply(driveId, roleId, key)
    rect rgb(235, 245, 255)
        Note over A,PG: Transaction with persisted ineligible decisions
        A->>PG: Resolve principal Student
        A->>PG: Check prior application for same student/key
        alt same key and same role
            PG-->>Student: Replay existing ApplicationDto
        else key reused for another role
            A-->>Student: 409 IDEMPOTENCY_KEY_REUSED
        end
        A->>PG: Load institution-scoped drive and active role
        A->>A: Check published/open state and opening/deadline
        alt closed, early, or late
            A-->>Student: 409 APPLICATIONS_NOT_OPEN or APPLICATION_DEADLINE_PASSED
        end
        A->>PG: Check unique role/student application
        alt duplicate
            A-->>Student: 409 DUPLICATE_APPLICATION
        end
        A->>PG: Load academics and applicable rules
        A->>E: Evaluate eligibility
        A->>PG: Insert application with eligibility snapshot
        A->>PG: Insert immutable initial status history
        alt ineligible
            Note over A,PG: noRollbackFor preserves decision and explanation
            A-->>Student: 422 ELIGIBILITY_FAILED
        else eligible
            A-->>Student: 201 ApplicationDto status=ELIGIBLE
        end
    end
    A->>Cache: Evict analytics
```

The database partial unique constraint on `(drive_role_id, student_id)` is the final concurrency defense after the service-level duplicate check.

## 12. Admin drive configuration and publication

```mermaid
flowchart TD
    Admin[Admin request] --> Security[Admin role + institution scope]
    Security --> Draft[Create/update draft drive]
    Draft --> Company{Active company assigned?}
    Company -->|No| Reject[422 DRIVE_NOT_PUBLISHABLE]
    Company -->|Yes| Owner{Owner/POC assigned?}
    Owner -->|No| Reject
    Owner -->|Yes| Dates{Opening, deadline, start and end valid?}
    Dates -->|No| Reject
    Dates -->|Yes| Roles{At least one active role?}
    Roles -->|No| Reject
    Roles -->|Yes| Rules{At least one active eligibility rule?}
    Rules -->|No| Reject
    Rules -->|Yes| Publish[Set PUBLISHED or APPLICATIONS_OPEN]
    Publish --> Audit[(AuditLog)]
    Publish --> Evict[Evict drive and analytics caches]
```

Once a drive moves beyond configurable states, role and rule edits are rejected. Archive/cancel/status transitions preserve historical applications and results.

## 13. Admin application status and bulk imports

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant C as AdminApplicationController
    participant A as AdminApplicationService
    participant P as ApplicationStatusPolicy
    participant PG as PostgreSQL
    participant AU as AuditService

    Admin->>C: PATCH status or POST bulk-status-update
    C->>A: Validated request
    A->>PG: Load every application inside admin institution
    A->>P: Validate each transition
    alt any invalid transition or out-of-scope ID
        Note over A,PG: Entire bulk transaction rolls back
        A-->>Admin: 4xx Problem Details
    else all valid
        loop each application
            A->>PG: Update status/version
            A->>PG: Insert immutable status-history row
        end
        A->>AU: One audited admin operation
        A-->>Admin: Updated DTO/count
    end
```

CSV result/student/offer imports require `Idempotency-Key`. The stored idempotency scope is `(institution, actor, operation, key)` plus a request hash. Same key and same bytes replay the response; same key and different bytes returns 409.

## 14. Selection-round result publication

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant C as RoundController
    participant R as RoundService
    participant PG as PostgreSQL
    participant N as NotificationService
    participant AU as AuditService

    Admin->>C: POST /admin/drives/{drive}/rounds/{round}/results
    C->>R: saveResults(all participant results)
    R->>PG: Validate round and every participant belongs to it
    R->>PG: Upsert scores, attendance and result status in one transaction
    R->>AU: Audit bulk result update
    R-->>Admin: updated count

    Admin->>C: POST .../publish-results
    C->>R: publish(round)
    R->>PG: Require a result for every participant
    alt any result missing
        R-->>Admin: 422 ROUND_RESULTS_INCOMPLETE; rollback
    else complete
        loop participants
            R->>PG: Mark result published
            R->>PG: Transition application through status policy
            R->>PG: Insert status-history row
            R->>N: Create durable notification
        end
        R->>PG: Mark round resultsPublished=true
        R->>AU: Audit publication
        R-->>Admin: RoundDto
    end
```

## 15. Offer acceptance and placement outcome

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    actor Student
    participant O as OfferService
    participant PG as PostgreSQL
    participant N as NotificationService
    participant AU as AuditService
    participant Cache as Redis caches

    Admin->>O: Create offer for SELECTED application
    O->>PG: Validate institution and application status
    O->>PG: Store numeric package + ISO currency + verification state
    opt release requested
        O->>PG: Set status=RELEASED and offeredAt
        O->>N: Create offer notification
    end
    O->>AU: Audit offer change

    Student->>O: POST /students/me/offers/{id}/accept
    O->>PG: Load offer by offerId + principal studentId
    O->>O: Require RELEASED, before deadline, and verified
    alt not respondable or not verified
        O-->>Student: 409 or 422 Problem Details
    else valid
        rect rgb(235, 245, 255)
            O->>PG: Set offer ACCEPTED
            O->>PG: Set application OFFER_ACCEPTED
            O->>PG: Insert application history
            O->>PG: Insert unique PlacementOutcome
            O->>PG: Derive Student placement status=PLACED
            O->>AU: Audit outcome creation
        end
        O->>Cache: Evict analytics and company outcome caches
        O-->>Student: OfferDto
    end
```

`PLACED` is derived only from a verified accepted offer and a persisted `PlacementOutcome`; it is not inferred from application counts.

## 16. Notifications, announcements, and cache behavior

```mermaid
flowchart LR
    Mutation[Offer/result/admin mutation] --> PG[(Persist durable state)]
    Mutation --> Notify[Insert Notification]
    Notify --> EvictUnread[Evict unread-count cache]
    Read[Read/mark notifications] --> PG
    Read --> EvictUnread

    CompanyWrite[Company/Q&A write] --> EvictCompanies[Evict company cache]
    DriveWrite[Drive/role/rule write] --> EvictDrives[Evict drive cache]
    DomainWrite[Application/document/offer/admin write] --> EvictAnalytics[Evict analytics cache]
    AnnouncementWrite[Announcement write/publish/archive] --> EvictAnnouncements[Evict announcement cache]
```

Cache values are DTOs, not managed entities. Cache keys include institution and role, or institution and user for unread counts. Cache eviction may clear all entries in a named cache; this is safe across institutions but can be refined later for performance.

## 17. List and export endpoints

```mermaid
flowchart TD
    Request[List or export request] --> Filter[JWT, Redis session and role]
    Filter --> Params[Parse allowed filters and pagination]
    Params --> Scope[Inject institution/user scope from principal]
    Scope --> Query[Use the same filtered repository/service query]
    Query --> Choice{Response type}
    Choice -->|List| Page[PagedResponse DTO]
    Choice -->|Export| CSV[CSV bytes + Content-Disposition]
```

An export does not widen authorization or bypass filters. Student exports and applicant exports reuse the corresponding list-service scope and filter semantics.

## 18. Error translation

```mermaid
flowchart LR
    Validation[Bean validation] --> E[GlobalExceptionHandler]
    Domain[DomainException] --> E
    Integrity[DB constraint violation] --> E
    Lock[Optimistic lock failure] --> E
    Malformed[Invalid JSON, enum or date] --> E
    E --> Problem[RFC 7807 Problem Details\ntype, title, status, detail, instance, code, fieldErrors]
```

Authentication-entry-point and access-denied failures are also rendered as Problem Details by the security configuration, before a controller is invoked.
