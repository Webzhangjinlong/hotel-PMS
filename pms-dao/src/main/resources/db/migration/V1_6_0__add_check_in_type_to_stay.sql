-- V1_6_0__add_check_in_type_to_stay.sql
-- 为入住单表添加入住类型字段

ALTER TABLE stay ADD COLUMN IF NOT EXISTS check_in_type VARCHAR(20) DEFAULT 'INDIVIDUAL';

COMMENT ON COLUMN stay.check_in_type IS '入住类型：INDIVIDUAL-散客入住/TEAM-团队入住';

-- 更新现有数据的默认值
UPDATE stay SET check_in_type = 'INDIVIDUAL' WHERE check_in_type IS NULL;
