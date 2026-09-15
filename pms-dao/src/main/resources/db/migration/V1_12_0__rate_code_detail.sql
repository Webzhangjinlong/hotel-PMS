-- 房价码房型明细表
CREATE TABLE IF NOT EXISTS room_price_plan_detail (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    base_price DECIMAL(10,2),
    discount_type VARCHAR(20) DEFAULT 'NONE',
    discount_value DECIMAL(10,2) DEFAULT 0,
    final_price DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_plan_detail_plan FOREIGN KEY (plan_id) REFERENCES room_price_plan(id),
    CONSTRAINT fk_plan_detail_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT uk_plan_detail UNIQUE (plan_id, room_type_id)
);

COMMENT ON TABLE room_price_plan_detail IS '房价码房型明细表';
CREATE INDEX IF NOT EXISTS idx_plan_detail_plan_id ON room_price_plan_detail(plan_id) WHERE deleted = FALSE;

-- 房价码主表新增有效期字段
ALTER TABLE room_price_plan ADD COLUMN IF NOT EXISTS valid_from DATE;
ALTER TABLE room_price_plan ADD COLUMN IF NOT EXISTS valid_to DATE;

-- 预订表新增房价码关联
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS price_plan_id BIGINT;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reservation_price_plan') THEN
        ALTER TABLE reservation ADD CONSTRAINT fk_reservation_price_plan FOREIGN KEY (price_plan_id) REFERENCES room_price_plan(id);
    END IF;
END $$;

-- 入住表新增房价码关联
ALTER TABLE stay ADD COLUMN IF NOT EXISTS price_plan_id BIGINT;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stay_price_plan') THEN
        ALTER TABLE stay ADD CONSTRAINT fk_stay_price_plan FOREIGN KEY (price_plan_id) REFERENCES room_price_plan(id);
    END IF;
END $$;