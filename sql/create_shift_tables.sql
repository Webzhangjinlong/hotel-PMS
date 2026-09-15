-- 交班记录表
CREATE TABLE IF NOT EXISTS sys_shift (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    shift_no VARCHAR(32) NOT NULL,
    operator_id BIGINT NOT NULL,
    receiver_id BIGINT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0,
    cash_amount DECIMAL(12,2) DEFAULT 0,
    pos_amount DECIMAL(12,2) DEFAULT 0,
    wechat_amount DECIMAL(12,2) DEFAULT 0,
    alipay_amount DECIMAL(12,2) DEFAULT 0,
    credit_amount DECIMAL(12,2) DEFAULT 0,
    refund_amount DECIMAL(12,2) DEFAULT 0,
    actual_cash DECIMAL(12,2) DEFAULT 0,
    actual_pos DECIMAL(12,2) DEFAULT 0,
    actual_wechat DECIMAL(12,2) DEFAULT 0,
    actual_alipay DECIMAL(12,2) DEFAULT 0,
    checkin_count INT DEFAULT 0,
    checkout_count INT DEFAULT 0,
    transaction_count INT DEFAULT 0,
    submit_time TIMESTAMP,
    accept_time TIMESTAMP,
    confirm_time TIMESTAMP,
    reject_time TIMESTAMP,
    reject_reason VARCHAR(500),
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 交班消息推送表
CREATE TABLE IF NOT EXISTS sys_shift_message (
    id BIGSERIAL PRIMARY KEY,
    shift_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    message_type VARCHAR(30) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 交班推送配置表
CREATE TABLE IF NOT EXISTS sys_shift_notify_config (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    notify_type VARCHAR(20) DEFAULT 'ALL',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
