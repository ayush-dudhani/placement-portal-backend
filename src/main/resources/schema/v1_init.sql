CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    college_name VARCHAR(255) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),

    mobile_no VARCHAR(20),

    roll_number VARCHAR(50) NOT NULL UNIQUE,
    branch VARCHAR(100) NOT NULL,

    year_of_passing INTEGER NOT NULL,

    cgpa NUMERIC(4,2),

    active_backlogs INTEGER DEFAULT 0,

    resume_url VARCHAR(500),

    linkedin_url VARCHAR(500),
    github_url VARCHAR(500),

    placement_status VARCHAR(30) DEFAULT 'ELIGIBLE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

ALTER TABLE students
ADD COLUMN tenth_percentage NUMERIC(5,2),

ADD COLUMN twelfth_percentage NUMERIC(5,2),

ADD COLUMN diploma_percentage NUMERIC(5,2),

ADD COLUMN date_of_birth DATE,


CREATE TABLE job_applications (
    id BIGSERIAL PRIMARY KEY,

    student_id BIGINT NOT NULL,

    drive_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL,

    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_application_student
        FOREIGN KEY (student_id)
        REFERENCES students(id)
);