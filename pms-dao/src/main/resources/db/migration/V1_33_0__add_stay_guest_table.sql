-- V1_33_0__add_stay_guest_table.sql
-- 入住同住人关联表：支持一间房登记多位客人

CREATE TABLE IF NOT EXISTS stay_guest (
    id BIGSERIAL PRIMARY KEY,
    stay_id BIGINT NOT NULL,
    guest_id BIGINT,
    guest_name VARCHAR(50) NOT NULL,
    id_type VARCHAR(20) DEFAULT 'ID_CARD',
    id_no VARCHAR(50),
    phone VARCHAR(20),
    gender VARCHAR(10),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_stay_guest_stay FOREIGN KEY (stay_id) REFERENCES stay(id),
    CONSTRAINT fk_stay_guest_guest FOREIGN KEY (guest_id) REFERENCES guest(id)
);

CREATE INDEX IF NOT EXISTS idx_stay_guest_stay_id ON stay_guest(stay_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_stay_guest_guest_id ON stay_guest(guest_id) WHERE deleted = FALSE;

COMMENT ON TABLE stay_guest IS '入住同住人关联表';
COMMENT ON COLUMN stay_guest.stay_id IS '入住单ID';
COMMENT ON COLUMN stay_guest.guest_id IS '客人档案ID（可为空，散客未建档时）';
COMMENT ON COLUMN stay_guest.guest_name IS '客人姓名';
COMMENT ON COLUMN stay_guest.id_type IS '证件类型：ID_CARD-身份证/PASSPORT-护照';
COMMENT ON COLUMN stay_guest.id_no IS '证件号码';
COMMENT ON COLUMN stay_guest.phone IS '手机号';
COMMENT ON COLUMN stay_guest.gender IS '性别';
COMMENT ON COLUMN stay_guest.is_primary IS '是否主客人（负责结账的人）';
