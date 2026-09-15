-- 修复 night_audit 表的 status 字段长度
ALTER TABLE night_audit ALTER COLUMN status TYPE VARCHAR(30);

-- 修复 night_audit_step 表的 status 字段长度
ALTER TABLE night_audit_step ALTER COLUMN status TYPE VARCHAR(30);
