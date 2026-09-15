-- 夜审历史数据管理
-- 1. 为夜审表添加索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_night_audit_hotel_date ON night_audit(hotel_id, audit_date);
CREATE INDEX IF NOT EXISTS idx_night_audit_status ON night_audit(status);
CREATE INDEX IF NOT EXISTS idx_night_audit_created ON night_audit(created_at);

-- 2. 为夜审步骤表添加索引
CREATE INDEX IF NOT EXISTS idx_night_audit_step_audit_id ON night_audit_step(night_audit_id);
CREATE INDEX IF NOT EXISTS idx_night_audit_step_status ON night_audit_step(status);

-- 3. 创建夜审归档表（用于存储历史数据）
CREATE TABLE IF NOT EXISTS night_audit_archive (
    id BIGSERIAL PRIMARY KEY,
    original_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    audit_date DATE NOT NULL,
    status VARCHAR(30),
    total_rooms INTEGER,
    occupied_rooms INTEGER,
    available_rooms INTEGER,
    total_revenue DECIMAL(10,2),
    room_revenue DECIMAL(10,2),
    extra_revenue DECIMAL(10,2),
    occupancy_rate DECIMAL(5,2),
    adr DECIMAL(10,2),
    revpar DECIMAL(10,2),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message VARCHAR(1000),
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    archived_by VARCHAR(50)
);

COMMENT ON TABLE night_audit_archive IS '夜审归档表';
CREATE INDEX IF NOT EXISTS idx_night_audit_archive_hotel_date ON night_audit_archive(hotel_id, audit_date);

-- 4. 创建夜审步骤归档表
CREATE TABLE IF NOT EXISTS night_audit_step_archive (
    id BIGSERIAL PRIMARY KEY,
    original_id BIGINT NOT NULL,
    night_audit_id BIGINT NOT NULL,
    step_name VARCHAR(50),
    step_order INTEGER,
    status VARCHAR(30),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message VARCHAR(1000),
    retry_count INTEGER DEFAULT 0,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE night_audit_step_archive IS '夜审步骤归档表';