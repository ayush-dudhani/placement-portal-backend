# Domain data model

This document groups the implemented PostgreSQL model into readable diagrams. `JobApplication` is the Java/entity name retained for compatibility; it represents the product's application aggregate.

## Identity and student records

```mermaid
erDiagram
    INSTITUTION ||--o{ USER : contains
    INSTITUTION ||--o{ STUDENT : scopes
    USER ||--o| STUDENT : owns
    USER ||--o{ REFRESH_TOKEN : rotates
    USER ||--o{ PASSWORD_RESET_TOKEN : receives
    USER ||--o{ NOTIFICATION : receives
    USER |o--o{ STUDENT_DOCUMENT : verifies
    STUDENT ||--o| ACADEMIC_RECORD : has
    STUDENT ||--o{ STUDENT_DOCUMENT : uploads
    STUDENT ||--o{ STUDENT_SKILL : assigned
    SKILL ||--o{ STUDENT_SKILL : referenced_by
    INSTITUTION ||--o{ SKILL : catalogs

    INSTITUTION {
        bigint id PK
        varchar code UK
        varchar name
        boolean active
    }
    USER {
        bigint id PK
        bigint institution_id FK
        varchar username
        varchar email
        varchar password_hash
        varchar role
        boolean active
        boolean email_verified
    }
    STUDENT {
        bigint id PK
        bigint user_id FK
        bigint institution_id FK
        varchar roll_number
        varchar branch
        integer year_of_passing
        varchar placement_status
    }
    ACADEMIC_RECORD {
        bigint id PK
        bigint student_id FK
        numeric cgpa
        numeric tenth_percentage
        numeric twelfth_percentage
        numeric diploma_percentage
        integer active_backlogs
        varchar branch
        integer graduation_year
    }
    SKILL {
        bigint id PK
        bigint institution_id FK
        varchar name
        varchar normalized_name
    }
    STUDENT_SKILL {
        bigint id PK
        bigint student_id FK
        bigint skill_id FK
    }
    STUDENT_DOCUMENT {
        bigint id PK
        bigint student_id FK
        varchar document_type
        varchar object_key UK
        varchar verification_status
        bigint verified_by FK
        boolean primary_document
        bigint version
    }
    REFRESH_TOKEN {
        bigint id PK
        bigint user_id FK
        varchar token_hash UK
        timestamp expires_at
        timestamp revoked_at
        varchar replaced_by_hash
    }
    PASSWORD_RESET_TOKEN {
        bigint id PK
        bigint user_id FK
        varchar token_hash UK
        varchar purpose
        timestamp expires_at
        timestamp used_at
    }
    NOTIFICATION {
        bigint id PK
        bigint user_id FK
        varchar type
        varchar title
        text message
        timestamp read_at
    }
```

Important constraints:

- Username and email are unique case-insensitively inside an institution, not globally.
- A `User` has at most one `Student` record.
- Roll number is unique case-insensitively inside an institution when present.
- A student has at most one academic record and one primary resume.
- `StudentSkill` is unique by `(student_id, skill_id)`.
- Only refresh/account token hashes are stored; raw tokens are not persisted.

## Companies, drives, and applications

```mermaid
erDiagram
    INSTITUTION ||--o{ COMPANY : scopes
    COMPANY ||--o{ COMPANY_CONTACT : has
    COMPANY ||--o{ COMPANY_FAQ : has
    COMPANY ||--o{ COMPANY_QUESTION : receives
    STUDENT ||--o{ COMPANY_QUESTION : asks
    COMPANY_QUESTION ||--o{ QUESTION_REPLY : has
    USER ||--o{ QUESTION_REPLY : authors

    INSTITUTION ||--o{ CAMPUS_DRIVE : scopes
    COMPANY ||--o{ CAMPUS_DRIVE : runs
    USER ||--o{ CAMPUS_DRIVE : owns
    CAMPUS_DRIVE ||--o{ DRIVE_ROLE : offers
    CAMPUS_DRIVE ||--o{ ELIGIBILITY_RULE : defines
    DRIVE_ROLE |o--o{ ELIGIBILITY_RULE : optionally_targets
    DRIVE_ROLE ||--o{ JOB_APPLICATION : receives
    STUDENT ||--o{ JOB_APPLICATION : submits
    JOB_APPLICATION ||--o{ APPLICATION_STATUS_HISTORY : records
    USER |o--o{ APPLICATION_STATUS_HISTORY : changes

    COMPANY {
        bigint id PK
        bigint institution_id FK
        varchar name
        varchar industry
        boolean active
        bigint version
    }
    CAMPUS_DRIVE {
        bigint id PK
        bigint institution_id FK
        bigint company_id FK
        bigint owner_id FK
        varchar title
        varchar status
        timestamp applications_open_at
        timestamp application_deadline
        timestamp starts_at
        timestamp ends_at
        boolean archived
        bigint version
    }
    DRIVE_ROLE {
        bigint id PK
        bigint drive_id FK
        varchar title
        integer positions
        numeric package_amount
        varchar currency
        timestamp application_deadline
        boolean active
        bigint version
    }
    ELIGIBILITY_RULE {
        bigint id PK
        bigint drive_id FK
        bigint drive_role_id FK
        varchar rule_type
        jsonb configuration
        boolean mandatory
        boolean active
    }
    JOB_APPLICATION {
        bigint id PK
        bigint institution_id FK
        bigint student_id FK
        bigint drive_role_id FK
        varchar status
        boolean eligibility_passed
        jsonb eligibility_snapshot
        varchar idempotency_key
        timestamp applied_at
        bigint version
    }
    APPLICATION_STATUS_HISTORY {
        bigint id PK
        bigint application_id FK
        varchar from_status
        varchar to_status
        bigint changed_by FK
        varchar reason
        timestamp created_at
    }
    COMPANY_QUESTION {
        bigint id PK
        bigint company_id FK
        bigint student_id FK
        text question
        boolean anonymous
        boolean closed
    }
```

Important constraints and invariants:

- Company names are unique inside an institution.
- A drive belongs to one institution and optionally references its company/owner while in draft.
- Eligibility rules use JSONB only for flexible rule configuration; the drive, role, application, and academic model remain relational.
- An eligibility rule may apply to the whole drive or a particular role.
- `(drive_role_id, student_id)` is unique for applications.
- `(student_id, idempotency_key)` is unique when an application idempotency key is supplied.
- Eligibility snapshot JSONB is immutable evidence of the input/rule decision at application time.
- Status history is append-only from the domain workflow perspective.

## Rounds, offers, outcomes, and administration

```mermaid
erDiagram
    CAMPUS_DRIVE ||--o{ SELECTION_ROUND : contains
    SELECTION_ROUND ||--o{ ROUND_PARTICIPANT : enrolls
    JOB_APPLICATION ||--o{ ROUND_PARTICIPANT : participates_as
    ROUND_PARTICIPANT ||--o| ROUND_RESULT : receives

    JOB_APPLICATION ||--o| OFFER : results_in
    STUDENT ||--o{ OFFER : receives
    COMPANY ||--o{ OFFER : issues
    OFFER ||--o| PLACEMENT_OUTCOME : materializes
    STUDENT ||--o{ PLACEMENT_OUTCOME : achieves
    COMPANY ||--o{ PLACEMENT_OUTCOME : records

    INSTITUTION ||--o{ ANNOUNCEMENT : publishes
    USER ||--o{ ANNOUNCEMENT : creates
    INSTITUTION ||--o{ AUDIT_LOG : owns
    USER ||--o{ AUDIT_LOG : acts
    INSTITUTION ||--o{ IDEMPOTENCY_RECORD : owns
    USER ||--o{ IDEMPOTENCY_RECORD : submits

    SELECTION_ROUND {
        bigint id PK
        bigint drive_id FK
        varchar name
        varchar round_type
        integer round_order
        timestamp starts_at
        timestamp ends_at
        boolean results_published
        boolean archived
        bigint version
    }
    ROUND_PARTICIPANT {
        bigint id PK
        bigint round_id FK
        bigint application_id FK
        varchar attendance_status
        bigint version
    }
    ROUND_RESULT {
        bigint id PK
        bigint participant_id FK
        varchar status
        numeric score
        varchar notes
        boolean published
        bigint version
    }
    OFFER {
        bigint id PK
        bigint institution_id FK
        bigint application_id FK
        bigint student_id FK
        bigint company_id FK
        numeric package_amount
        varchar currency
        varchar status
        boolean verified
        timestamp response_deadline
        bigint version
    }
    PLACEMENT_OUTCOME {
        bigint id PK
        bigint offer_id FK
        bigint student_id FK
        bigint company_id FK
        numeric package_amount
        varchar currency
        timestamp placed_at
    }
    ANNOUNCEMENT {
        bigint id PK
        bigint institution_id FK
        bigint created_by FK
        varchar audience_role
        timestamp published_at
        timestamp expires_at
        boolean archived
        bigint version
    }
    AUDIT_LOG {
        bigint id PK
        bigint institution_id FK
        bigint actor_id FK
        varchar action
        varchar resource_type
        varchar resource_id
        jsonb details
        timestamp created_at
    }
    IDEMPOTENCY_RECORD {
        bigint id PK
        bigint institution_id FK
        bigint actor_id FK
        varchar operation
        varchar idempotency_key
        varchar request_hash
        text response_json
    }
```

Important constraints and invariants:

- Round order is unique inside a drive.
- A given application participates at most once in a round.
- A participant has at most one result.
- An application has at most one offer in the current model.
- An offer has at most one placement outcome.
- Package values are numeric and currency is a separate three-letter code.
- Placement outcome creation requires a verified, accepted offer; this is what derives the student's placed status.
- Idempotency records are unique by `(institution, actor, operation, idempotency_key)` and store a request hash for mismatch detection.
- Audit logs record sensitive administrative mutations without storing credentials, tokens, or document bytes.

## Aggregate and transaction boundaries

```mermaid
flowchart TB
    Identity[Identity aggregate\nUser + refresh/account tokens]
    Student[Student aggregate\nprofile + academics + skills + documents]
    Drive[Drive aggregate\ndrive + roles + rules]
    Application[Application aggregate\napplication + immutable history + eligibility snapshot]
    Selection[Selection aggregate\nround + participants + results]
    Placement[Placement aggregate\noffer + outcome]

    Identity --> Student
    Student --> Application
    Drive --> Application
    Application --> Selection
    Application --> Placement
```

- Changes within a workflow occur through services rather than direct controller/repository composition.
- Database unique constraints protect the final write against races after service pre-checks.
- Optimistic `version` columns detect lost updates for frequently edited records.
- Audit and history rows are written in the same transaction as the sensitive state change.
- Notifications generated by result/offer workflows are durable PostgreSQL rows; Redis only accelerates unread counts.

## Institution-scope rule

The institution ID is carried in the authenticated `PortalPrincipal`, but services still obtain current records from PostgreSQL and query protected resources with an institution or owner predicate. A client-supplied institution ID is never trusted to widen access.

```text
Student self-service: principal.userId -> Student -> owned record
Admin resource:       principal.institutionId + requested resourceId -> scoped record
Shared discovery:     principal.institutionId + visible/published state -> DTO
```
