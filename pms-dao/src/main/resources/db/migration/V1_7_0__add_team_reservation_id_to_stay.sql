-- V1_7_0__add_team_reservation_id_to_stay.sql
-- 为入住单表添加团队预订关联字段

-- 添加团队预订ID字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'stay' AND column_name = 'team_reservation_id') THEN
        ALTER TABLE stay ADD COLUMN team_reservation_id BIGINT;
        COMMENT ON COLUMN stay.team_reservation_id IS '团队预订ID（团队入住时关联）';
    END IF;
END $$;

-- 添加入住类型字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'stay' AND column_name = 'check_in_type') THEN
        ALTER TABLE stay ADD COLUMN check_in_type VARCHAR(20) DEFAULT 'INDIVIDUAL';
        COMMENT ON COLUMN stay.check_in_type IS '入住类型：INDIVIDUAL-散客入住/TEAM-团队入住';
    END IF;
END $$;
