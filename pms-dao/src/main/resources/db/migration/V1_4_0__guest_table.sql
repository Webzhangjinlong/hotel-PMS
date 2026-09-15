-- V1_4_0__guest_table.sql
-- 创建客人档案表

CREATE TABLE IF NOT EXISTS guest (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    id_type VARCHAR(20) DEFAULT 'ID_CARD',
    id_no VARCHAR(50),
    phone VARCHAR(20),
    gender VARCHAR(10),
    nationality VARCHAR(50) DEFAULT '中国',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_guest_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

COMMENT ON TABLE guest IS '客人档案表';
COMMENT ON COLUMN guest.id IS '主键ID';
COMMENT ON COLUMN guest.hotel_id IS '酒店ID';
COMMENT ON COLUMN guest.name IS '客人姓名';
COMMENT ON COLUMN guest.id_type IS '证件类型：ID_CARD-身份证/PASSPORT-护照';
COMMENT ON COLUMN guest.id_no IS '证件号码';
COMMENT ON COLUMN guest.phone IS '手机号码';
COMMENT ON COLUMN guest.gender IS '性别';
COMMENT ON COLUMN guest.nationality IS '国籍';
COMMENT ON COLUMN guest.created_at IS '创建时间';
COMMENT ON COLUMN guest.updated_at IS '更新时间';
COMMENT ON COLUMN guest.deleted IS '逻辑删除：FALSE-未删除/TRUE-已删除';
COMMENT ON COLUMN guest.version IS '乐观锁版本号';

CREATE INDEX IF NOT EXISTS idx_guest_hotel_id ON guest(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_guest_phone ON guest(hotel_id, phone) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_guest_id_no ON guest(hotel_id, id_no) WHERE deleted = FALSE;
