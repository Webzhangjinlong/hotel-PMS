-- V1_37_0__add_reservation_credit_company.sql
-- 预订增强：预订单关联协议单位（协议价来源）

ALTER TABLE reservation ADD COLUMN IF NOT EXISTS credit_company_id BIGINT;
COMMENT ON COLUMN reservation.credit_company_id IS '协议单位ID（协议价时使用，可为空）';

CREATE INDEX IF NOT EXISTS idx_reservation_credit_company ON reservation(hotel_id, credit_company_id) WHERE deleted = FALSE;
