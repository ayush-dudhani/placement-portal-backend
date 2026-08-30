CREATE TABLE companies (
 id BIGSERIAL PRIMARY KEY,institution_id BIGINT NOT NULL REFERENCES institutions(id),name VARCHAR(255) NOT NULL,industry VARCHAR(100),
 description TEXT,website_url VARCHAR(500),logo_url VARCHAR(500),is_active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT uk_company_institution_name UNIQUE(institution_id,name)
);
CREATE INDEX idx_companies_institution ON companies(institution_id);
CREATE TABLE company_contacts (
 id BIGSERIAL PRIMARY KEY,company_id BIGINT NOT NULL REFERENCES companies(id),name VARCHAR(150) NOT NULL,email VARCHAR(255) NOT NULL,phone VARCHAR(30),designation VARCHAR(100),is_primary BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE company_faqs (
 id BIGSERIAL PRIMARY KEY,company_id BIGINT NOT NULL REFERENCES companies(id),question VARCHAR(500) NOT NULL,answer TEXT NOT NULL,display_order INTEGER NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE company_questions (
 id BIGSERIAL PRIMARY KEY,company_id BIGINT NOT NULL REFERENCES companies(id),student_id BIGINT NOT NULL REFERENCES students(id),question TEXT NOT NULL,is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,is_closed BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE question_replies (
 id BIGSERIAL PRIMARY KEY,question_id BIGINT NOT NULL REFERENCES company_questions(id),author_id BIGINT NOT NULL REFERENCES users(id),body TEXT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE campus_drives (
 id BIGSERIAL PRIMARY KEY,institution_id BIGINT NOT NULL REFERENCES institutions(id),company_id BIGINT REFERENCES companies(id),owner_id BIGINT REFERENCES users(id),
 title VARCHAR(255) NOT NULL,description TEXT,status VARCHAR(40) NOT NULL DEFAULT 'DRAFT',starts_at TIMESTAMPTZ,ends_at TIMESTAMPTZ,
 applications_open_at TIMESTAMPTZ,application_deadline TIMESTAMPTZ,archived BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_drive_status ON campus_drives(status);CREATE INDEX idx_drive_deadline ON campus_drives(application_deadline);CREATE INDEX idx_drive_institution ON campus_drives(institution_id);
CREATE TABLE drive_roles (
 id BIGSERIAL PRIMARY KEY,drive_id BIGINT NOT NULL REFERENCES campus_drives(id),title VARCHAR(255) NOT NULL,description TEXT,positions INTEGER NOT NULL,
 employment_type VARCHAR(50),location VARCHAR(255),package_amount NUMERIC(14,2),currency VARCHAR(3) NOT NULL DEFAULT 'INR',application_deadline TIMESTAMPTZ,is_active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_role_positions CHECK(positions>0),CONSTRAINT ck_role_package CHECK(package_amount IS NULL OR package_amount>=0)
);
CREATE INDEX idx_role_drive ON drive_roles(drive_id);
CREATE TABLE eligibility_rules (
 id BIGSERIAL PRIMARY KEY,drive_id BIGINT NOT NULL REFERENCES campus_drives(id),drive_role_id BIGINT REFERENCES drive_roles(id),rule_type VARCHAR(50) NOT NULL,
 configuration_json JSONB NOT NULL,mandatory BOOLEAN NOT NULL DEFAULT TRUE,is_active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_rule_drive ON eligibility_rules(drive_id);

ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS institution_id BIGINT REFERENCES institutions(id);
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS drive_role_id BIGINT REFERENCES drive_roles(id);
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS eligibility_passed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS eligibility_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb;
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS withdrawal_reason VARCHAR(500);
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
UPDATE job_applications a SET institution_id=s.institution_id FROM students s WHERE a.student_id=s.id AND a.institution_id IS NULL;
ALTER TABLE job_applications ALTER COLUMN institution_id SET NOT NULL;
CREATE UNIQUE INDEX uk_application_role_student ON job_applications(drive_role_id,student_id) WHERE drive_role_id IS NOT NULL;
CREATE UNIQUE INDEX uk_application_student_idempotency ON job_applications(student_id,idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE INDEX idx_application_status ON job_applications(status);CREATE INDEX idx_application_institution ON job_applications(institution_id);

CREATE TABLE application_status_history (
 id BIGSERIAL PRIMARY KEY,application_id BIGINT NOT NULL REFERENCES job_applications(id),from_status VARCHAR(40),to_status VARCHAR(40) NOT NULL,
 changed_by BIGINT REFERENCES users(id),reason VARCHAR(500),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_history_application ON application_status_history(application_id);
