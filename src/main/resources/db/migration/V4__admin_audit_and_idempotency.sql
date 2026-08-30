CREATE TABLE audit_logs (
 id BIGSERIAL PRIMARY KEY,institution_id BIGINT NOT NULL REFERENCES institutions(id),actor_id BIGINT NOT NULL REFERENCES users(id),
 action VARCHAR(100) NOT NULL,resource_type VARCHAR(100) NOT NULL,resource_id VARCHAR(100),details_json JSONB NOT NULL DEFAULT '{}'::jsonb,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_institution_created ON audit_logs(institution_id,created_at DESC);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type,resource_id);
CREATE TABLE idempotency_records (
 id BIGSERIAL PRIMARY KEY,institution_id BIGINT NOT NULL REFERENCES institutions(id),actor_id BIGINT NOT NULL REFERENCES users(id),
 operation VARCHAR(100) NOT NULL,idempotency_key VARCHAR(100) NOT NULL,request_hash VARCHAR(64) NOT NULL,response_json TEXT,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT uk_idempotency_scope UNIQUE(institution_id,actor_id,operation,idempotency_key)
);
