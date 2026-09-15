-- 为stay表添加锁定字段
ALTER TABLE stay ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE stay ADD COLUMN IF NOT EXISTS locked_by_audit_id BIGINT;

-- 为folio表添加锁定字段
ALTER TABLE folio ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE folio ADD COLUMN IF NOT EXISTS locked_by_audit_id BIGINT;

-- 为hotel表添加夜审配置字段
ALTER TABLE hotel ADD COLUMN IF NOT EXISTS audit_time TIME DEFAULT '04:00:00';
ALTER TABLE hotel ADD COLUMN IF NOT EXISTS auto_audit_enabled BOOLEAN DEFAULT TRUE;

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_stay_is_locked ON stay(hotel_id, is_locked) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_folio_is_locked ON folio(hotel_id, is_locked) WHERE deleted = FALSE;
