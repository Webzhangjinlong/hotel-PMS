-- 添加预订单身份证号字段
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS id_card_no VARCHAR(18);

-- 添加字段注释
COMMENT ON COLUMN reservation.id_card_no IS '身份证号';
