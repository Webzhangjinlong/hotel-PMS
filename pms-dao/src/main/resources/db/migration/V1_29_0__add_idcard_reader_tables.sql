-- V1_29_0__add_idcard_reader_tables.sql
-- 身份证阅读器功能：创建设备配置表、读取记录表、设备日志表

-- 1. 创建身份证阅读器设备配置表
CREATE TABLE IF NOT EXISTS idcard_reader_config (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    device_model VARCHAR(100),
    device_port VARCHAR(50),
    device_ip VARCHAR(50),
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
    
    CONSTRAINT fk_idcard_reader_config_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

CREATE INDEX IF NOT EXISTS idx_idcard_reader_config_hotel_id ON idcard_reader_config(hotel_id) WHERE deleted = FALSE;
COMMENT ON TABLE idcard_reader_config IS '身份证阅读器设备配置表';
COMMENT ON COLUMN idcard_reader_config.device_status IS '设备状态：OFFLINE-离线/ONLINE-在线/ERROR-故障';
COMMENT ON COLUMN idcard_reader_config.status IS '配置状态：ACTIVE-启用/INACTIVE-停用';

-- 2. 创建身份证读取记录表
CREATE TABLE IF NOT EXISTS idcard_read_record (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    device_id BIGINT,
    
    -- 证件信息
    card_type VARCHAR(20) DEFAULT 'ID_CARD',
    card_no VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    nation VARCHAR(50),
    birth_date DATE,
    address VARCHAR(500),
    issue_org VARCHAR(200),
    valid_start DATE,
    valid_end DATE,
    
    -- 照片信息
    photo_path VARCHAR(500),
    photo_data TEXT,
    
    -- 关联信息
    guest_id BIGINT,
    stay_id BIGINT,
    
    -- 操作信息
    operator_id BIGINT,
    operator_name VARCHAR(50),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_idcard_read_record_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_idcard_read_record_device FOREIGN KEY (device_id) REFERENCES idcard_reader_config(id)
);

CREATE INDEX IF NOT EXISTS idx_idcard_read_record_hotel_id ON idcard_read_record(hotel_id);
CREATE INDEX IF NOT EXISTS idx_idcard_read_record_card_no ON idcard_read_record(card_no);
CREATE INDEX IF NOT EXISTS idx_idcard_read_record_guest_id ON idcard_read_record(guest_id) WHERE guest_id IS NOT NULL;
COMMENT ON TABLE idcard_read_record IS '身份证读取记录表';
COMMENT ON COLUMN idcard_read_record.card_type IS '证件类型：ID_CARD-身份证/PASSPORT-护照/DRIVER_LICENSE-驾驶证';

-- 3. 创建身份证阅读器设备日志表
CREATE TABLE IF NOT EXISTS idcard_reader_log (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    device_id BIGINT,
    
    -- 日志信息
    log_type VARCHAR(20) NOT NULL,
    log_content TEXT,
    log_level VARCHAR(10) DEFAULT 'INFO',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_idcard_reader_log_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_idcard_reader_log_device FOREIGN KEY (device_id) REFERENCES idcard_reader_config(id)
);

CREATE INDEX IF NOT EXISTS idx_idcard_reader_log_hotel_id ON idcard_reader_log(hotel_id);
CREATE INDEX IF NOT EXISTS idx_idcard_reader_log_device_id ON idcard_reader_log(device_id);
COMMENT ON TABLE idcard_reader_log IS '身份证阅读器设备日志表';
COMMENT ON COLUMN idcard_reader_log.log_type IS '日志类型：CONNECT-连接/READ-读取/ERROR-错误/INFO-信息';
COMMENT ON COLUMN idcard_reader_log.log_level IS '日志级别：INFO-信息/WARN-警告/ERROR-错误';
