-- 为酒店表添加酒店标识码字段
-- 支持多酒店系统，通过标识码区分不同酒店

-- 添加 hotel_code 字段（先允许NULL）
ALTER TABLE hotel ADD COLUMN IF NOT EXISTS hotel_code VARCHAR(50);

-- 为所有现有酒店分配默认标识码（使用ID生成）
UPDATE hotel SET hotel_code = 'H' || LPAD(id::text, 3, '0') WHERE hotel_code IS NULL;

-- 添加唯一约束
ALTER TABLE hotel ADD CONSTRAINT uk_hotel_code UNIQUE (hotel_code);

-- 设置为 NOT NULL（在填充默认值后）
ALTER TABLE hotel ALTER COLUMN hotel_code SET NOT NULL;

-- 添加注释
COMMENT ON COLUMN hotel.hotel_code IS '酒店标识码，用于登录时区分不同酒店';
