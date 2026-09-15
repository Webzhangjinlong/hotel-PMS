-- V1_8_0__team_folio_enhancement.sql
-- 团队账务单增强 + 收款明细表

-- 1. team_folio 增加字段
ALTER TABLE team_folio ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20);
COMMENT ON COLUMN team_folio.payment_method IS '支付方式：CASH-现金/WECHAT-微信/ALIPAY-支付宝/BANK_CARD-银行卡/CREDIT-挂账';

ALTER TABLE team_folio ADD COLUMN IF NOT EXISTS remark TEXT;
COMMENT ON COLUMN team_folio.remark IS '备注';

-- 2. 创建团队收款明细表
CREATE TABLE IF NOT EXISTS team_folio_payment (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    team_folio_id BIGINT NOT NULL,
    payment_no VARCHAR(30) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_time TIMESTAMP NOT NULL,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_team_payment_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_team_payment_folio FOREIGN KEY (team_folio_id) REFERENCES team_folio(id),
    CONSTRAINT uk_team_payment_no UNIQUE (hotel_id, payment_no)
);

CREATE INDEX IF NOT EXISTS idx_team_payment_folio_id ON team_folio_payment(team_folio_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_team_payment_time ON team_folio_payment(hotel_id, payment_time) WHERE deleted = FALSE;

COMMENT ON TABLE team_folio_payment IS '团队收款明细';
COMMENT ON COLUMN team_folio_payment.payment_method IS '支付方式：CASH-现金/WECHAT-微信/ALIPAY-支付宝/BANK_CARD-银行卡/CREDIT-挂账';