-- 房价方案表
CREATE TABLE IF NOT EXISTS room_price_plan (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    room_type_id BIGINT,
    base_price DECIMAL(10,2),
    discount_type VARCHAR(20) DEFAULT 'NONE',
    discount_value DECIMAL(10,2) DEFAULT 0,
    description VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_price_plan_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_price_plan_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT uk_price_plan_code UNIQUE (hotel_id, code)
);

COMMENT ON TABLE room_price_plan IS '房价方案表';
CREATE INDEX IF NOT EXISTS idx_price_plan_hotel_id ON room_price_plan(hotel_id) WHERE deleted = FALSE;

-- 房价规则表
CREATE TABLE IF NOT EXISTS room_price_rule (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    rule_type VARCHAR(20) NOT NULL,
    room_type_id BIGINT,
    adjust_type VARCHAR(20) NOT NULL,
    adjust_value DECIMAL(10,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_price_rule_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_price_rule_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)
);

COMMENT ON TABLE room_price_rule IS '房价规则表';
CREATE INDEX IF NOT EXISTS idx_price_rule_hotel_id ON room_price_rule(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_price_rule_type ON room_price_rule(hotel_id, rule_type) WHERE deleted = FALSE;