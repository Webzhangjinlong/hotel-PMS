-- V1_32_0__add_member_tables.sql
-- 会员管理模块：会员等级配置表、会员表、积分流水表

-- 1. 会员等级配置表
CREATE TABLE IF NOT EXISTS member_level (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    level_code VARCHAR(20) NOT NULL,
    level_name VARCHAR(50) NOT NULL,
    discount_rate DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    points_multiplier DECIMAL(3,1) NOT NULL DEFAULT 1.0,
    min_total_consumption DECIMAL(12,2) NOT NULL DEFAULT 0,
    min_stay_count INTEGER NOT NULL DEFAULT 0,
    benefits_desc TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_member_level_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_member_level_code UNIQUE (hotel_id, level_code)
);

CREATE INDEX IF NOT EXISTS idx_member_level_hotel_id ON member_level(hotel_id) WHERE deleted = FALSE;

COMMENT ON TABLE member_level IS '会员等级配置表';
COMMENT ON COLUMN member_level.level_code IS '等级编码：NORMAL-普通/SILVER-银卡/GOLD-金卡/DIAMOND-钻石卡';
COMMENT ON COLUMN member_level.discount_rate IS '房价折扣率（100=原价，95=95折）';
COMMENT ON COLUMN member_level.points_multiplier IS '积分倍率（每消费1元获得的积分为1*倍率）';
COMMENT ON COLUMN member_level.min_total_consumption IS '升级所需最低累计消费金额';

-- 2. 会员表
CREATE TABLE IF NOT EXISTS member (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    guest_id BIGINT NOT NULL,
    member_no VARCHAR(30) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    level_id BIGINT NOT NULL,
    level_code VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    total_points INTEGER NOT NULL DEFAULT 0,
    used_points INTEGER NOT NULL DEFAULT 0,
    total_consumption DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_stay_count INTEGER NOT NULL DEFAULT 0,
    register_source VARCHAR(20) NOT NULL DEFAULT 'AUTO',
    register_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_stay_time TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    remark TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_member_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_member_guest FOREIGN KEY (guest_id) REFERENCES guest(id),
    CONSTRAINT fk_member_level FOREIGN KEY (level_id) REFERENCES member_level(id),
    CONSTRAINT uk_member_phone UNIQUE (hotel_id, phone),
    CONSTRAINT uk_member_no UNIQUE (hotel_id, member_no)
);

CREATE INDEX IF NOT EXISTS idx_member_hotel_id ON member(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_member_guest_id ON member(guest_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_member_level_code ON member(hotel_id, level_code) WHERE deleted = FALSE;

COMMENT ON TABLE member IS '会员表';
COMMENT ON COLUMN member.member_no IS '会员编号（M+酒店编码后4位+6位序号）';
COMMENT ON COLUMN member.phone IS '手机号（电子会员卡号）';
COMMENT ON COLUMN member.total_points IS '当前可用积分';
COMMENT ON COLUMN member.used_points IS '已使用积分';
COMMENT ON COLUMN member.total_consumption IS '累计消费金额';
COMMENT ON COLUMN member.register_source IS '注册来源：AUTO-自动/MANUAL-手动';

-- 3. 积分流水表
CREATE TABLE IF NOT EXISTS member_points_log (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    points INTEGER NOT NULL,
    before_points INTEGER NOT NULL DEFAULT 0,
    after_points INTEGER NOT NULL DEFAULT 0,
    related_stay_id BIGINT,
    related_transaction_id BIGINT,
    description VARCHAR(200),
    operator_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_points_log_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_points_log_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE INDEX IF NOT EXISTS idx_points_log_member_id ON member_points_log(member_id);
CREATE INDEX IF NOT EXISTS idx_points_log_hotel_id ON member_points_log(hotel_id);

COMMENT ON TABLE member_points_log IS '积分流水表';
COMMENT ON COLUMN member_points_log.change_type IS '变动类型：EARN-获得/EXCHANGE-兑换/REFUND-退还/ADJUST-调整';
COMMENT ON COLUMN member_points_log.points IS '变动积分（正数=获得，负数=消耗）';

-- 4. 为 stay 表增加 member_id 字段
ALTER TABLE stay ADD COLUMN IF NOT EXISTS member_id BIGINT;
COMMENT ON COLUMN stay.member_id IS '关联会员ID';
CREATE INDEX IF NOT EXISTS idx_stay_member_id ON stay(member_id) WHERE member_id IS NOT NULL;

-- 5. 为 reservation 表增加 member_id 字段
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS member_id BIGINT;
COMMENT ON COLUMN reservation.member_id IS '关联会员ID';
CREATE INDEX IF NOT EXISTS idx_reservation_member_id ON reservation(member_id) WHERE member_id IS NOT NULL;

-- 6. 为 fin_transaction 表增加 points_used 字段
ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS points_used INTEGER DEFAULT 0;
COMMENT ON COLUMN fin_transaction.points_used IS '积分抵扣使用的积分数';
