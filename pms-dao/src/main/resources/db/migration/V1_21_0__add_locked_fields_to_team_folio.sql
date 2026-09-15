-- 为 team_folio 表添加锁定字段
ALTER TABLE team_folio ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE team_folio ADD COLUMN IF NOT EXISTS locked_by_audit_id BIGINT;

-- 添加外键约束（如果需要）
-- ALTER TABLE team_folio ADD CONSTRAINT fk_team_folio_locked_audit FOREIGN KEY (locked_by_audit_id) REFERENCES night_audit(id);