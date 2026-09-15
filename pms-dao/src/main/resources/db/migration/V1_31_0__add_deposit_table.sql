-- 押金表
CREATE TABLE IF NOT EXISTS deposit (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    deposit_no VARCHAR(32) NOT NULL,
    stay_id BIGINT,
    reservation_id BIGINT,
    guest_id BIGINT NOT NULL,
    guest_name VARCHAR(50) NOT NULL,
    room_no VARCHAR(20),
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COLLECTED',
    collected_at TIMESTAMP NOT NULL,
    collected_by BIGINT NOT NULL,
    refunded_amount DECIMAL(10,2) DEFAULT 0,
    refunded_at TIMESTAMP,
    refunded_by BIGINT,
    refund_method VARCHAR(20),
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_deposit_no UNIQUE (deposit_no)
);

COMMENT ON TABLE deposit IS '押金表';
COMMENT ON COLUMN deposit.id IS '主键ID';
COMMENT ON COLUMN deposit.hotel_id IS '酒店ID';
COMMENT ON COLUMN deposit.deposit_no IS '押金单号';
COMMENT ON COLUMN deposit.stay_id IS '入住单ID';
COMMENT ON COLUMN deposit.reservation_id IS '预订ID';
COMMENT ON COLUMN deposit.guest_id IS '客人ID';
COMMENT ON COLUMN deposit.guest_name IS '客人姓名';
COMMENT ON COLUMN deposit.room_no IS '房间号';
COMMENT ON COLUMN deposit.amount IS '押金金额';
COMMENT ON COLUMN deposit.payment_method IS '支付方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER';
COMMENT ON COLUMN deposit.status IS '押金状态：COLLECTED-已收取/REFUNDED-已退还/PARTIAL_REFUND-部分退还';
COMMENT ON COLUMN deposit.collected_at IS '收取时间';
COMMENT ON COLUMN deposit.collected_by IS '收取人ID';
COMMENT ON COLUMN deposit.refunded_amount IS '已退金额';
COMMENT ON COLUMN deposit.refunded_at IS '退还时间';
COMMENT ON COLUMN deposit.refunded_by IS '退还人ID';
COMMENT ON COLUMN deposit.refund_method IS '退还方式';
COMMENT ON COLUMN deposit.remark IS '备注';
COMMENT ON COLUMN deposit.created_at IS '创建时间';
COMMENT ON COLUMN deposit.updated_at IS '更新时间';
COMMENT ON COLUMN deposit.deleted IS '逻辑删除';
COMMENT ON COLUMN deposit.version IS '乐观锁版本号';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_deposit_hotel_id ON deposit(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_deposit_stay_id ON deposit(stay_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_deposit_guest_id ON deposit(guest_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_deposit_status ON deposit(status) WHERE deleted = FALSE;
