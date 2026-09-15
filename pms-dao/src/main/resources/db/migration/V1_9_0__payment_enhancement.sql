-- V1_9_0__payment_enhancement.sql
-- 支付系统增强：扩展交易表、新增挂账公司表、新增预订预付款表

-- 1. fin_transaction 表增加字段
ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20);
COMMENT ON COLUMN fin_transaction.payment_method IS '支付方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER/CREDIT';

ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS credit_company_id BIGINT;
COMMENT ON COLUMN fin_transaction.credit_company_id IS '挂账公司ID（挂账时使用）';

ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS credit_guest_id BIGINT;
COMMENT ON COLUMN fin_transaction.credit_guest_id IS '挂账客人ID（挂账时使用）';

ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS refund_transaction_id BIGINT;
COMMENT ON COLUMN fin_transaction.refund_transaction_id IS '退款原交易ID（退款时使用）';

-- 2. 创建挂账公司表
CREATE TABLE IF NOT EXISTS credit_company (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    credit_limit DECIMAL(12,2) DEFAULT 0,
    current_balance DECIMAL(12,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_credit_company_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_credit_company_name UNIQUE (hotel_id, company_name)
);

CREATE INDEX IF NOT EXISTS idx_credit_company_hotel_id ON credit_company(hotel_id) WHERE deleted = FALSE;
COMMENT ON TABLE credit_company IS '挂账公司/单位';

-- 3. 创建预订预付款表
CREATE TABLE IF NOT EXISTS reservation_prepayment (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    reservation_id BIGINT,
    team_reservation_id BIGINT,
    prepayment_type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_time TIMESTAMP NOT NULL,
    transaction_id BIGINT,
    status VARCHAR(20) DEFAULT 'PAID',
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_prepayment_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_prepayment_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(id),
    CONSTRAINT fk_prepayment_team_reservation FOREIGN KEY (team_reservation_id) REFERENCES team_reservation(id)
);

CREATE INDEX IF NOT EXISTS idx_prepayment_reservation ON reservation_prepayment(reservation_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_prepayment_team_reservation ON reservation_prepayment(team_reservation_id) WHERE deleted = FALSE;
COMMENT ON TABLE reservation_prepayment IS '预订预付款';
COMMENT ON COLUMN reservation_prepayment.prepayment_type IS '预付类型：FULL-全额/PARTIAL-部分/DEPOSIT-押金';

-- 4. 创建挂账明细索引
CREATE INDEX IF NOT EXISTS idx_fin_transaction_credit_company ON fin_transaction(credit_company_id) WHERE credit_company_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_fin_transaction_credit_guest ON fin_transaction(credit_guest_id) WHERE credit_guest_id IS NOT NULL;
