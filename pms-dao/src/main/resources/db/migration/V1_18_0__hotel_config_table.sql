-- 酒店配置表
CREATE TABLE IF NOT EXISTS hotel_config (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value VARCHAR(500),
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_hotel_config_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_hotel_config_key UNIQUE (hotel_id, config_key)
);

COMMENT ON TABLE hotel_config IS '酒店配置表';
COMMENT ON COLUMN hotel_config.hotel_id IS '酒店ID';
COMMENT ON COLUMN hotel_config.config_key IS '配置键';
COMMENT ON COLUMN hotel_config.config_value IS '配置值';
COMMENT ON COLUMN hotel_config.description IS '配置描述';

-- 插入默认配置
INSERT INTO hotel_config (hotel_id, config_key, config_value, description) VALUES
(1, 'CHECKOUT_TIME', '12:00', '标准离店时间'),
(1, 'LATE_CHECKOUT_HALF_DAY_TIME', '18:00', '半日租截止时间'),
(1, 'LATE_CHECKOUT_HALF_DAY_RATE', '0.5', '半日租费率（相对当日房价的比例）'),
(1, 'LATE_CHECKOUT_FULL_DAY_RATE', '1.0', '全日租费率（相对当日房价的比例）')
ON CONFLICT (hotel_id, config_key) DO NOTHING;
