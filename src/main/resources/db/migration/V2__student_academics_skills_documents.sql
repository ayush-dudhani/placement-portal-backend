ALTER TABLE students ADD COLUMN IF NOT EXISTS institution_id BIGINT REFERENCES institutions(id);
UPDATE students s SET institution_id = u.institution_id FROM users u WHERE s.user_id = u.id AND s.institution_id IS NULL;
ALTER TABLE students ALTER COLUMN institution_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_students_institution ON students(institution_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_students_institution_roll ON students(institution_id, lower(roll_number)) WHERE roll_number IS NOT NULL;

CREATE TABLE academic_records (
    id BIGSERIAL PRIMARY KEY, student_id BIGINT NOT NULL UNIQUE REFERENCES students(id), cgpa NUMERIC(4,2),
    tenth_percentage NUMERIC(5,2), twelfth_percentage NUMERIC(5,2), diploma_percentage NUMERIC(5,2),
    active_backlogs INTEGER NOT NULL DEFAULT 0, branch VARCHAR(100) NOT NULL, graduation_year INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_academic_cgpa CHECK (cgpa IS NULL OR cgpa BETWEEN 0 AND 10),
    CONSTRAINT ck_academic_backlogs CHECK (active_backlogs >= 0)
);
CREATE INDEX idx_academic_branch_year ON academic_records(branch, graduation_year);

CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY, institution_id BIGINT NOT NULL REFERENCES institutions(id), name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_skill_institution_name UNIQUE(institution_id, normalized_name)
);
CREATE INDEX idx_skills_institution ON skills(institution_id);

CREATE TABLE student_skill_assignments (
    id BIGSERIAL PRIMARY KEY, student_id BIGINT NOT NULL REFERENCES students(id), skill_id BIGINT NOT NULL REFERENCES skills(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0, CONSTRAINT uk_student_skill UNIQUE(student_id, skill_id)
);

CREATE TABLE student_documents (
    id BIGSERIAL PRIMARY KEY, student_id BIGINT NOT NULL REFERENCES students(id), document_type VARCHAR(40) NOT NULL,
    object_key VARCHAR(500) NOT NULL UNIQUE, original_filename VARCHAR(255) NOT NULL, content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL, is_primary BOOLEAN NOT NULL DEFAULT FALSE, verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verification_note VARCHAR(500), verified_by BIGINT REFERENCES users(id), created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_documents_student ON student_documents(student_id);
CREATE INDEX idx_documents_verification ON student_documents(verification_status);
CREATE UNIQUE INDEX uk_primary_resume_per_student ON student_documents(student_id)
    WHERE document_type = 'RESUME' AND is_primary = TRUE;
