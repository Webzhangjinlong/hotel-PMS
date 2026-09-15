-- 为night_audit_step表添加deleted和version字段
ALTER TABLE night_audit_step ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE night_audit_step ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;