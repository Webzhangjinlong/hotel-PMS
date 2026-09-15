-- V1_27_0__add_invoice_tables.sql
-- 发票管理功能：创建发票主表和发票明细表

-- 1. 创建发票主表
CREATE TABLE IF NOT EXISTS invoice (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    invoice_no VARCHAR(50) NOT NULL,
    invoice_code VARCHAR(50),
    invoice_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    invoice_status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    invoice_date DATE NOT NULL,
    
    -- 购买方信息
    buyer_name VARCHAR(200) NOT NULL,
    buyer_tax_no VARCHAR(50),
    buyer_address VARCHAR(500),
    buyer_phone VARCHAR(50),
    buyer_bank VARCHAR(200),
    buyer_bank_account VARCHAR(50),
    
    -- 销售方信息
    seller_name VARCHAR(200),
    seller_tax_no VARCHAR(50),
    seller_address VARCHAR(500),
    seller_phone VARCHAR(50),
    seller_bank VARCHAR(200),
    seller_bank_account VARCHAR(50),
    
    -- 金额信息
    amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    
    -- 关联信息
    stay_id BIGINT,
    stay_no VARCHAR(50),
    guest_name VARCHAR(100),
    guest_phone VARCHAR(20),
    
    -- 备注信息
    remark TEXT,
    void_reason TEXT,
    
    -- 操作信息
    operator_id BIGINT,
    operator_name VARCHAR(50),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    
    CONSTRAINT fk_invoice_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_invoice_no UNIQUE (hotel_id, invoice_no)
);

CREATE INDEX IF NOT EXISTS idx_invoice_hotel_id ON invoice(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_invoice_stay_id ON invoice(stay_id) WHERE stay_id IS NOT NULL AND deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_invoice_date ON invoice(invoice_date) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_invoice_status ON invoice(invoice_status) WHERE deleted = FALSE;
COMMENT ON TABLE invoice IS '发票主表';
COMMENT ON COLUMN invoice.invoice_type IS '发票类型：NORMAL-普通发票/SPECIAL-增值税专用发票/ELECTRONIC-电子发票';
COMMENT ON COLUMN invoice.invoice_status IS '发票状态：NORMAL-正常/VOID-作废/RED-红冲';

-- 2. 创建发票明细表
CREATE TABLE IF NOT EXISTS invoice_item (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    
    -- 商品/服务信息
    item_name VARCHAR(200) NOT NULL,
    item_spec VARCHAR(100),
    item_unit VARCHAR(20),
    item_quantity DECIMAL(10,2) DEFAULT 1,
    item_price DECIMAL(12,2) NOT NULL,
    
    -- 金额信息
    amount DECIMAL(12,2) NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    
    -- 备注
    remark TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_invoice_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(id),
    CONSTRAINT fk_invoice_item_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

CREATE INDEX IF NOT EXISTS idx_invoice_item_invoice_id ON invoice_item(invoice_id) WHERE deleted = FALSE;
COMMENT ON TABLE invoice_item IS '发票明细表';
