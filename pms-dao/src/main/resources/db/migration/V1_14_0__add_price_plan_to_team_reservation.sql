-- V1_10_0__add_price_plan_to_team_reservation.sql
-- 为团队预订表添加房价码字段

-- 添加房价码ID字段
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'team_reservation' AND column_name = 'price_plan_id') THEN
        ALTER TABLE team_reservation ADD COLUMN price_plan_id BIGINT;
        COMMENT ON COLUMN team_reservation.price_plan_id IS '房价码ID';
    END IF;
END $$;