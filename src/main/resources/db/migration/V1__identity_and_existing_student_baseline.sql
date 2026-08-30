CREATE TABLE IF NOT EXISTS institutions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

INSERT INTO institutions(code, name) VALUES ('DEFAULT', 'Default Institution')
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    institution_id BIGINT REFERENCES institutions(id),
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
ALTER TABLE users ADD COLUMN IF NOT EXISTS institution_id BIGINT REFERENCES institutions(id);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
UPDATE users SET institution_id = (SELECT id FROM institutions WHERE code = 'DEFAULT') WHERE institution_id IS NULL;
ALTER TABLE users ALTER COLUMN institution_id SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_institution_username ON users(institution_id, lower(username));
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_institution_email ON users(institution_id, lower(email));
CREATE INDEX IF NOT EXISTS idx_users_institution ON users(institution_id);

CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    college_name VARCHAR(255), first_name VARCHAR(100), last_name VARCHAR(100), mobile_no VARCHAR(20),
    roll_number VARCHAR(50), branch VARCHAR(100), year_of_passing INTEGER, cgpa NUMERIC(4,2),
    tenth_percentage NUMERIC(5,2), twelfth_percentage NUMERIC(5,2), diploma_percentage NUMERIC(5,2),
    active_backlogs INTEGER DEFAULT 0, resume_url VARCHAR(500), linkedin_url VARCHAR(500), github_url VARCHAR(500),
    placement_status VARCHAR(30) DEFAULT 'NOT_PLACED', profile_completion INTEGER, date_of_birth DATE, gender VARCHAR(30),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
ALTER TABLE students ADD COLUMN IF NOT EXISTS tenth_percentage NUMERIC(5,2);
ALTER TABLE students ADD COLUMN IF NOT EXISTS twelfth_percentage NUMERIC(5,2);
ALTER TABLE students ADD COLUMN IF NOT EXISTS diploma_percentage NUMERIC(5,2);
ALTER TABLE students ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE students ADD COLUMN IF NOT EXISTS gender VARCHAR(30);
ALTER TABLE students ADD COLUMN IF NOT EXISTS profile_completion INTEGER;
ALTER TABLE students ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_students_roll_number ON students(roll_number);
CREATE INDEX IF NOT EXISTS idx_students_branch_year ON students(branch, year_of_passing);

CREATE TABLE IF NOT EXISTS student_skills (
    student_id BIGINT NOT NULL REFERENCES students(id), skill VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS job_applications (
    id BIGSERIAL PRIMARY KEY, student_id BIGINT NOT NULL REFERENCES students(id), drive_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL, applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES users(id), token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMPTZ, replaced_by_hash VARCHAR(64)
);
CREATE INDEX IF NOT EXISTS idx_refresh_user ON refresh_tokens(user_id);

CREATE TABLE IF NOT EXISTS account_tokens (
    id BIGSERIAL PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES users(id), token_hash VARCHAR(64) NOT NULL UNIQUE,
    purpose VARCHAR(30) NOT NULL, expires_at TIMESTAMPTZ NOT NULL, used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_account_token_user ON account_tokens(user_id);
