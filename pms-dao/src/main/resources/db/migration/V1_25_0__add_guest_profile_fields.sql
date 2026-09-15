-- 添加客人档案管理相关字段
-- V1_25_0__add_guest_profile_fields.sql

-- 添加常住客人标记
ALTER TABLE guest ADD COLUMN IF NOT EXISTS is_vip BOOLEAN DEFAULT FALSE;

-- 添加黑名单标记
ALTER TABLE guest ADD COLUMN IF NOT EXISTS is_blacklisted BOOLEAN DEFAULT FALSE;

-- 添加黑名单原因
ALTER TABLE guest ADD COLUMN IF NOT EXISTS blacklist_reason VARCHAR(500);

-- 添加备注
ALTER TABLE guest ADD COLUMN IF NOT EXISTS remark VARCHAR(500);

-- 添加入住次数
ALTER TABLE guest ADD COLUMN IF NOT EXISTS stay_count INTEGER DEFAULT 0;

-- 添加最后入住时间
ALTER TABLE guest ADD COLUMN IF NOT EXISTS last_stay_time TIMESTAMP;

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_guest_is_vip ON guest(hotel_id, is_vip);
CREATE INDEX IF NOT EXISTS idx_guest_is_blacklisted ON guest(hotel_id, is_blacklisted);
CREATE INDEX IF NOT EXISTS idx_guest_phone ON guest(hotel_id, phone);
