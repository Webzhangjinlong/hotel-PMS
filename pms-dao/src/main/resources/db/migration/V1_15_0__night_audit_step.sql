-- 夜审步骤表
CREATE TABLE IF NOT EXISTS night_audit_step (
    id BIGSERIAL PRIMARY KEY,
    night_audit_id BIGINT NOT NULL,
    step_name VARCHAR(50) NOT NULL,
    step_order INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message VARCHAR(1000),
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_night_audit_step_audit FOREIGN KEY (night_audit_id) REFERENCES night_audit(id),
    CONSTRAINT uk_night_audit_step_order UNIQUE (night_audit_id, step_order)
);

COMMENT ON TABLE night_audit_step IS '夜审步骤表';
CREATE INDEX IF NOT EXISTS idx_night_audit_step_audit_id ON night_audit_step(night_audit_id);
