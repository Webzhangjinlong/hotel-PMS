-- V1_28_0__add_ota_tables.sql
-- OTA订单接入功能：创建OTA渠道配置表、OTA订单表、OTA事件日志表

-- 1. 创建OTA渠道配置表
CREATE TABLE IF NOT EXISTS ota_channel (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    channel_name VARCHAR(50) NOT NULL,
    channel_code VARCHAR(50) NOT NULL,
    channel_type VARCHAR(20) DEFAULT 'OTA',
    
    -- API配置
    app_key VARCHAR(100),
    app_secret VARCHAR(200),
    api_url VARCHAR(500),
    callback_url VARCHAR(500),
    
    -- 认证信息
    auth_type VARCHAR(20) DEFAULT 'API_KEY',
    auth_token VARCHAR(500),
    token_expire_time TIMESTAMP,
    
    -- 配置信息
    config_json TEXT,
    
    -- 状态
    status VARCHAR(20) DEFAULT 'ACTIVE',
    remark TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    
    CONSTRAINT fk_ota_channel_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_ota_channel_code UNIQUE (hotel_id, channel_code)
);

CREATE INDEX IF NOT EXISTS idx_ota_channel_hotel_id ON ota_channel(hotel_id) WHERE deleted = FALSE;
COMMENT ON TABLE ota_channel IS 'OTA渠道配置表';
COMMENT ON COLUMN ota_channel.channel_type IS '渠道类型：OTA-在线旅行社/DIRECT-直销/CORPORATE-协议单位';
COMMENT ON COLUMN ota_channel.auth_type IS '认证类型：API_KEY/OAUTH2/BASIC';

-- 2. 创建OTA订单表
CREATE TABLE IF NOT EXISTS ota_order (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    ota_order_no VARCHAR(100) NOT NULL,
    ota_reservation_no VARCHAR(100),
    
    -- 关联信息
    reservation_id BIGINT,
    stay_id BIGINT,
    
    -- 客人信息
    guest_name VARCHAR(100) NOT NULL,
    guest_phone VARCHAR(20),
    guest_email VARCHAR(100),
    guest_id_type VARCHAR(20),
    guest_id_no VARCHAR(50),
    
    -- 房间信息
    room_type_id BIGINT,
    room_type_name VARCHAR(50),
    room_count INTEGER DEFAULT 1,
    
    -- 日期信息
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    nights INTEGER NOT NULL,
    
    -- 金额信息
    room_price DECIMAL(10,2),
    total_amount DECIMAL(12,2),
    commission_rate DECIMAL(5,2),
    commission_amount DECIMAL(10,2),
    net_amount DECIMAL(12,2),
    
    -- 状态信息
    order_status VARCHAR(20) DEFAULT 'PENDING',
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    
    -- 特殊要求
    special_requests TEXT,
    
    -- 原始数据
    raw_data TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    
    CONSTRAINT fk_ota_order_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_ota_order_channel FOREIGN KEY (channel_id) REFERENCES ota_channel(id),
    CONSTRAINT uk_ota_order_no UNIQUE (channel_id, ota_order_no)
);

CREATE INDEX IF NOT EXISTS idx_ota_order_hotel_id ON ota_order(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_ota_order_channel_id ON ota_order(channel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_ota_order_check_in ON ota_order(check_in_date) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_ota_order_status ON ota_order(order_status) WHERE deleted = FALSE;
COMMENT ON TABLE ota_order IS 'OTA订单表';
COMMENT ON COLUMN ota_order.order_status IS '订单状态：PENDING-待确认/CONFIRMED-已确认/CHECKED_IN-已入住/CANCELLED-已取消/NO_SHOW-未到店';
COMMENT ON COLUMN ota_order.payment_status IS '支付状态：UNPAID-未支付/PAID-已支付/REFUNDED-已退款';

-- 3. 创建OTA事件日志表
CREATE TABLE IF NOT EXISTS ota_event_log (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    channel_id BIGINT,
    ota_order_id BIGINT,
    
    -- 事件信息
    event_type VARCHAR(50) NOT NULL,
    event_source VARCHAR(50),
    event_time TIMESTAMP NOT NULL,
    
    -- 事件内容
    request_data TEXT,
    response_data TEXT,
    
    -- 处理信息
    process_status VARCHAR(20) DEFAULT 'PENDING',
    process_result TEXT,
    error_message TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ota_event_log_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_ota_event_log_channel FOREIGN KEY (channel_id) REFERENCES ota_channel(id),
    CONSTRAINT fk_ota_event_log_order FOREIGN KEY (ota_order_id) REFERENCES ota_order(id)
);

CREATE INDEX IF NOT EXISTS idx_ota_event_log_hotel_id ON ota_event_log(hotel_id);
CREATE INDEX IF NOT EXISTS idx_ota_event_log_channel_id ON ota_event_log(channel_id);
CREATE INDEX IF NOT EXISTS idx_ota_event_log_order_id ON ota_event_log(ota_order_id);
CREATE INDEX IF NOT EXISTS idx_ota_event_log_event_type ON ota_event_log(event_type);
CREATE INDEX IF NOT EXISTS idx_ota_event_log_process_status ON ota_event_log(process_status);
COMMENT ON TABLE ota_event_log IS 'OTA事件日志表';
COMMENT ON COLUMN ota_event_log.event_type IS '事件类型：NEW_ORDER-新订单/CANCEL_ORDER-取消订单/UPDATE_ORDER-修改订单/ROOM_UPDATE-房态更新';
COMMENT ON COLUMN ota_event_log.process_status IS '处理状态：PENDING-待处理/PROCESSING-处理中/SUCCESS-成功/FAILED-失败';
