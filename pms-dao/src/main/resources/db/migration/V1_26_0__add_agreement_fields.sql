-- V1_26_0__add_agreement_fields.sql
-- 协议单位功能：扩展挂账公司表，添加协议相关字段

-- 1. 扩展credit_company表，添加协议相关字段
ALTER TABLE credit_company ADD COLUMN IF NOT EXISTS agreement_no VARCHAR(50);
COMMENT ON COLUMN credit_company.agreement_no IS '协议编号';

ALTER TABLE credit_company ADD COLUMN IF NOT EXISTS company_type VARCHAR(20) DEFAULT 'ENTERPRISE';
COMMENT ON COLUMN credit_company.company_type IS '公司类型：ENTERPRISE-企业/TRAVEL_AGENCY-旅行社/GOVERNMENT-政府/OTHER-其他';

ALTER TABLE credit_company ADD COLUMN IF NOT EXISTS agreement_start_date DATE;
COMMENT ON COLUMN credit_company.agreement_start_date IS '协议开始日期';

ALTER TABLE credit_company ADD COLUMN IF NOT EXISTS agreement_end_date DATE;
COMMENT ON COLUMN credit_company.agreement_end_date IS '协议结束日期';

ALTER TABLE credit_company ADD COLUMN IF NOT EXISTS agreement_status VARCHAR(20) DEFAULT 'ACTIVE';
COMMENT ON COLUMN credit_company.agreement_status IS '协议状态：ACTIVE-有效/EXPIRED-过期/TERMINATED-终止';

ALTER TABLE credit_company ADD COLUMN IF NOT EXISTS address TEXT;
COMMENT ON COLUMN credit_company.address IS '公司地址';

ALTER TABLE credit_company ADD COLUMN IF NOT EXISTS email VARCHAR(100);
COMMENT ON COLUMN credit_company.email IS '联系邮箱';

ALTER TABLE credit_company ADD COLUMN IF NOT EXISTS fax VARCHAR(20);
COMMENT ON COLUMN credit_company.fax IS '传真号码';

-- 2. 创建协议价表
CREATE TABLE IF NOT EXISTS agreement_price (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    credit_company_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    discount_rate DECIMAL(5,2) DEFAULT 100,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_agreement_price_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_agreement_price_company FOREIGN KEY (credit_company_id) REFERENCES credit_company(id),
    CONSTRAINT fk_agreement_price_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT uk_agreement_price UNIQUE (hotel_id, credit_company_id, room_type_id, start_date, end_date)
);

CREATE INDEX IF NOT EXISTS idx_agreement_price_company ON agreement_price(credit_company_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_agreement_price_room_type ON agreement_price(room_type_id) WHERE deleted = FALSE;
COMMENT ON TABLE agreement_price IS '协议价表';
COMMENT ON COLUMN agreement_price.price IS '协议价格';
COMMENT ON COLUMN agreement_price.discount_rate IS '折扣比例（100表示原价，90表示9折）';
