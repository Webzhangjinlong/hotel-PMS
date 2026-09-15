-- 公安上传记录表
CREATE TABLE IF NOT EXISTS police_upload_record (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    stay_id BIGINT,
    stay_no VARCHAR(30),
    guest_id BIGINT,
    guest_name VARCHAR(50),
    id_type VARCHAR(20) DEFAULT 'ID_CARD',
    id_no VARCHAR(30),
    phone VARCHAR(20),
    gender VARCHAR(10),
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    room_no VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    upload_time TIMESTAMP,
    error_message VARCHAR(500),
    retry_count INTEGER DEFAULT 0,
    is_manual BOOLEAN DEFAULT FALSE,
    operator_id BIGINT,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 1,
    CONSTRAINT fk_police_upload_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

-- 添加注释
COMMENT ON TABLE police_upload_record IS '公安上传记录表';
COMMENT ON COLUMN police_upload_record.hotel_id IS '酒店ID';
COMMENT ON COLUMN police_upload_record.stay_id IS '入住单ID';
COMMENT ON COLUMN police_upload_record.stay_no IS '入住单号';
COMMENT ON COLUMN police_upload_record.guest_id IS '客人ID';
COMMENT ON COLUMN police_upload_record.guest_name IS '客人姓名';
COMMENT ON COLUMN police_upload_record.id_type IS '证件类型';
COMMENT ON COLUMN police_upload_record.id_no IS '证件号码';
COMMENT ON COLUMN police_upload_record.phone IS '手机号';
COMMENT ON COLUMN police_upload_record.gender IS '性别';
COMMENT ON COLUMN police_upload_record.check_in_time IS '入住时间';
COMMENT ON COLUMN police_upload_record.check_out_time IS '预计离店时间';
COMMENT ON COLUMN police_upload_record.room_no IS '房间号';
COMMENT ON COLUMN police_upload_record.status IS '上传状态：PENDING-待上传/UPLOADING-上传中/SUCCESS-成功/FAILED-失败';
COMMENT ON COLUMN police_upload_record.upload_time IS '上传时间';
COMMENT ON COLUMN police_upload_record.error_message IS '失败原因';
COMMENT ON COLUMN police_upload_record.retry_count IS '重试次数';
COMMENT ON COLUMN police_upload_record.is_manual IS '是否人工补传';
COMMENT ON COLUMN police_upload_record.operator_id IS '操作员ID';
COMMENT ON COLUMN police_upload_record.remark IS '备注';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_police_upload_hotel_id ON police_upload_record(hotel_id);
CREATE INDEX IF NOT EXISTS idx_police_upload_status ON police_upload_record(status);
CREATE INDEX IF NOT EXISTS idx_police_upload_created_at ON police_upload_record(created_at);
