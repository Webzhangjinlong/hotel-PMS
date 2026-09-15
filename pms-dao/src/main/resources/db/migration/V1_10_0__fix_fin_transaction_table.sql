-- V1_10_0__fix_fin_transaction_table.sql
-- 修复 fin_transaction 表，添加缺失的字段

-- 添加 updated_at 字段
ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 添加 deleted 字段
ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

-- 添加 version 字段
ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

-- 添加 refund_transaction_id 字段（如果不存在）
ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS refund_transaction_id BIGINT;

-- 添加 credit_company_id 字段（如果不存在）
ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS credit_company_id BIGINT;

-- 添加 credit_guest_id 字段（如果不存在）
ALTER TABLE fin_transaction ADD COLUMN IF NOT EXISTS credit_guest_id BIGINT;

-- 添加注释
COMMENT ON COLUMN fin_transaction.updated_at IS '更新时间';
COMMENT ON COLUMN fin_transaction.deleted IS '逻辑删除标记';
COMMENT ON COLUMN fin_transaction.version IS '乐观锁版本号';
COMMENT ON COLUMN fin_transaction.refund_transaction_id IS '退款原交易ID';
COMMENT ON COLUMN fin_transaction.credit_company_id IS '挂账公司ID';
COMMENT ON COLUMN fin_transaction.credit_guest_id IS '挂账客人ID';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_fin_transaction_credit_company ON fin_transaction(credit_company_id) WHERE credit_company_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_fin_transaction_credit_guest ON fin_transaction(credit_guest_id) WHERE credit_guest_id IS NOT NULL;
