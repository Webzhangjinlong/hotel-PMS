-- 团队预订表添加来源/渠道字段
ALTER TABLE team_reservation ADD COLUMN IF NOT EXISTS source VARCHAR(20);
COMMENT ON COLUMN team_reservation.source IS '来源/渠道：WALK_IN/PHONE/OTA';