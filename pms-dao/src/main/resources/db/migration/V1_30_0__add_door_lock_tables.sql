-- V1_30_0__add_door_lock_tables.sql
-- 门锁/房卡对接功能：创建门锁设备配置表、房卡记录表、发卡记录表

-- 1. 创建门锁设备配置表
CREATE TABLE IF NOT EXISTS door_lock_config (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    device_model VARCHAR(100),
    device_ip VARCHAR(50),
    device_port VARCHAR(50),
    device_status VARCHAR(20) DEFAULT 'OFFLINE',
    
    -- 配置信息
    config_json TEXT,
    
    -- 状态
    status VARCHAR(20) DEFAULT 'ACTIVE',
    remark TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    
    CONSTRAINT fk_door_lock_config_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

CREATE INDEX IF NOT EXISTS idx_door_lock_config_hotel_id ON door_lock_config(hotel_id) WHERE deleted = FALSE;
COMMENT ON TABLE door_lock_config IS '门锁设备配置表';
COMMENT ON COLUMN door_lock_config.device_status IS '设备状态：OFFLINE-离线/ONLINE-在线/ERROR-故障';
COMMENT ON COLUMN door_lock_config.status IS '配置状态：ACTIVE-启用/INACTIVE-停用';

-- 2. 创建房卡记录表
CREATE TABLE IF NOT EXISTS room_card (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    device_id BIGINT,
    
    -- 卡信息
    card_no VARCHAR(50) NOT NULL,
    card_type VARCHAR(20) DEFAULT 'GUEST_CARD',
    
    -- 关联信息
    room_id BIGINT,
    room_no VARCHAR(20),
    guest_id BIGINT,
    guest_name VARCHAR(100),
    stay_id BIGINT,
    stay_no VARCHAR(50),
    
    -- 有效期
    valid_start TIMESTAMP NOT NULL,
    valid_end TIMESTAMP NOT NULL,
    
    -- 状态
    card_status VARCHAR(20) DEFAULT 'ACTIVE',
    
    -- 操作信息
    operator_id BIGINT,
    operator_name VARCHAR(50),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    
    CONSTRAINT fk_room_card_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_room_card_device FOREIGN KEY (device_id) REFERENCES door_lock_config(id)
);

CREATE INDEX IF NOT EXISTS idx_room_card_hotel_id ON room_card(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_room_card_card_no ON room_card(card_no) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_room_card_room_id ON room_card(room_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_room_card_guest_id ON room_card(guest_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_room_card_stay_id ON room_card(stay_id) WHERE deleted = FALSE;
COMMENT ON TABLE room_card IS '房卡记录表';
COMMENT ON COLUMN room_card.card_type IS '卡类型：GUEST_CARD-客人卡/STAFF_CARD-员工卡/MASTER_CARD-总控卡';
COMMENT ON COLUMN room_card.card_status IS '卡状态：ACTIVE-有效/EXPIRED-过期/LOST-挂失/CANCELLED-注销';

-- 3. 创建发卡记录表
CREATE TABLE IF NOT EXISTS card_issue_log (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    device_id BIGINT,
    card_id BIGINT,
    
    -- 发卡信息
    issue_type VARCHAR(20) NOT NULL,
    issue_time TIMESTAMP NOT NULL,
    
    -- 结果信息
    result_status VARCHAR(20) NOT NULL,
    result_message TEXT,
    
    -- 操作信息
    operator_id BIGINT,
    operator_name VARCHAR(50),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_card_issue_log_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_card_issue_log_device FOREIGN KEY (device_id) REFERENCES door_lock_config(id),
    CONSTRAINT fk_card_issue_log_card FOREIGN KEY (card_id) REFERENCES room_card(id)
);

CREATE INDEX IF NOT EXISTS idx_card_issue_log_hotel_id ON card_issue_log(hotel_id);
CREATE INDEX IF NOT EXISTS idx_card_issue_log_device_id ON card_issue_log(device_id);
CREATE INDEX IF NOT EXISTS idx_card_issue_log_card_id ON card_issue_log(card_id);
COMMENT ON TABLE card_issue_log IS '发卡记录表';
COMMENT ON COLUMN card_issue_log.issue_type IS '发卡类型：NEW-新卡/REPLACE-补卡/EXTEND-续卡/CANCEL-注销';
COMMENT ON COLUMN card_issue_log.result_status IS '结果状态：SUCCESS-成功/FAILED-失败';
