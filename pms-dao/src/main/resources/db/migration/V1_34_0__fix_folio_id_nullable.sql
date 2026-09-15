-- V1_32_0__fix_folio_id_nullable.sql
-- 修复 fin_transaction 表，允许 folio_id 为空

-- 修改 folio_id 字段允许为空
ALTER TABLE fin_transaction ALTER COLUMN folio_id DROP NOT NULL;

-- 添加注释
COMMENT ON COLUMN fin_transaction.folio_id IS '账务单ID（押金交易时可为空）';
