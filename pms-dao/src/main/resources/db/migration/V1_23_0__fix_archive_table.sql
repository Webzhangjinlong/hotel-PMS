-- 为 night_audit_archive 表添加 deleted 和 version 字段
ALTER TABLE night_audit_archive ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE night_audit_archive ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

-- 更新现有记录
UPDATE night_audit_archive SET deleted = FALSE, version = 0 WHERE deleted IS NULL;