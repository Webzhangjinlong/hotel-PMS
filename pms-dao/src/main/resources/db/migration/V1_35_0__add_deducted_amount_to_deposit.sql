-- V1_35_0__add_deducted_amount_to_deposit.sql
-- 抵扣房费：新增已抵扣金额字段

ALTER TABLE deposit ADD COLUMN IF NOT EXISTS deducted_amount DECIMAL(10,2) DEFAULT 0;
COMMENT ON COLUMN deposit.deducted_amount IS '已抵扣金额';
