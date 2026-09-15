-- PMS 酒店管理系统数据库初始化脚本
-- 版本：V1.0.0
-- 创建时间：2026-08-08

-- ========== 酒店表 ==========
CREATE TABLE IF NOT EXISTS hotel (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(20),
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0
);

COMMENT ON TABLE hotel IS '酒店表';
COMMENT ON COLUMN hotel.id IS '主键ID';
COMMENT ON COLUMN hotel.name IS '酒店名称';
COMMENT ON COLUMN hotel.address IS '酒店地址';
COMMENT ON COLUMN hotel.phone IS '联系电话';
COMMENT ON COLUMN hotel.timezone IS '时区';
COMMENT ON COLUMN hotel.status IS '状态：ACTIVE-启用/INACTIVE-停用';
COMMENT ON COLUMN hotel.created_at IS '创建时间';
COMMENT ON COLUMN hotel.updated_at IS '更新时间';
COMMENT ON COLUMN hotel.deleted IS '逻辑删除：FALSE-未删除/TRUE-已删除';
COMMENT ON COLUMN hotel.version IS '乐观锁版本号';

-- ========== 楼层表 ==========
CREATE TABLE IF NOT EXISTS hotel_floor (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    floor_no INTEGER NOT NULL,
    name VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_hotel_floor_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

COMMENT ON TABLE hotel_floor IS '楼层表';
CREATE INDEX IF NOT EXISTS idx_hotel_floor_hotel_id ON hotel_floor(hotel_id) WHERE deleted = FALSE;

-- ========== 房型表 ==========
CREATE TABLE IF NOT EXISTS room_type (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(20) NOT NULL,
    bed_type VARCHAR(50),
    max_guests INTEGER DEFAULT 2,
    base_price DECIMAL(10,2),
    description VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_room_type_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_room_type_code UNIQUE (hotel_id, code)
);

COMMENT ON TABLE room_type IS '房型表';
CREATE INDEX IF NOT EXISTS idx_room_type_hotel_id ON room_type(hotel_id) WHERE deleted = FALSE;

-- ========== 房间表 ==========
CREATE TABLE IF NOT EXISTS room (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    floor_id BIGINT,
    room_no VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_room_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT fk_room_floor FOREIGN KEY (floor_id) REFERENCES hotel_floor(id),
    CONSTRAINT uk_room_no UNIQUE (hotel_id, room_no)
);

COMMENT ON TABLE room IS '房间表';
CREATE INDEX IF NOT EXISTS idx_room_hotel_id ON room(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_room_status ON room(hotel_id, status) WHERE deleted = FALSE;

-- ========== 房价表 ==========
CREATE TABLE IF NOT EXISTS room_price (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    price_date DATE NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_room_price_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_room_price_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT uk_room_price_date UNIQUE (hotel_id, room_type_id, price_date)
);

COMMENT ON TABLE room_price IS '房价表';
CREATE INDEX IF NOT EXISTS idx_room_price_hotel_id ON room_price(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_room_price_date ON room_price(hotel_id, price_date) WHERE deleted = FALSE;

-- ========== 系统账号表 ==========
CREATE TABLE IF NOT EXISTS sys_account (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login_time TIMESTAMP,
    login_fail_count INTEGER DEFAULT 0,
    lock_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_sys_account_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_sys_account_username UNIQUE (hotel_id, username)
);

COMMENT ON TABLE sys_account IS '系统账号表';
CREATE INDEX IF NOT EXISTS idx_sys_account_hotel_id ON sys_account(hotel_id) WHERE deleted = FALSE;

-- ========== 预订单表 ==========
CREATE TABLE IF NOT EXISTS reservation (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    reservation_no VARCHAR(30) NOT NULL,
    source VARCHAR(20) DEFAULT 'WALK_IN',
    guest_name VARCHAR(50),
    guest_phone VARCHAR(20),
    room_type_id BIGINT,
    room_id BIGINT,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    nights INTEGER,
    total_amount DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    special_requests VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_reservation_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_reservation_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT uk_reservation_no UNIQUE (hotel_id, reservation_no)
);

COMMENT ON TABLE reservation IS '预订单表';
CREATE INDEX IF NOT EXISTS idx_reservation_hotel_id ON reservation(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_reservation_status ON reservation(hotel_id, status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_reservation_check_in_date ON reservation(hotel_id, check_in_date) WHERE deleted = FALSE;

-- ========== 入住单表 ==========
CREATE TABLE IF NOT EXISTS stay (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    stay_no VARCHAR(30) NOT NULL,
    reservation_id BIGINT,
    room_id BIGINT NOT NULL,
    guest_id BIGINT,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    actual_check_out_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'CHECKED_IN',
    total_amount DECIMAL(10,2),
    paid_amount DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_stay_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_stay_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(id),
    CONSTRAINT fk_stay_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT uk_stay_no UNIQUE (hotel_id, stay_no),
    CONSTRAINT uk_stay_room_active UNIQUE (room_id, status) 
);

COMMENT ON TABLE stay IS '入住单表';
CREATE INDEX IF NOT EXISTS idx_stay_hotel_id ON stay(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_stay_status ON stay(hotel_id, status) WHERE deleted = FALSE;

-- ========== 账务单表 ==========
CREATE TABLE IF NOT EXISTS folio (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    folio_no VARCHAR(30) NOT NULL,
    stay_id BIGINT,
    guest_id BIGINT,
    total_amount DECIMAL(10,2) DEFAULT 0,
    paid_amount DECIMAL(10,2) DEFAULT 0,
    balance DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_folio_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_folio_stay FOREIGN KEY (stay_id) REFERENCES stay(id),
    CONSTRAINT uk_folio_no UNIQUE (hotel_id, folio_no)
);

COMMENT ON TABLE folio IS '账务单表';
CREATE INDEX IF NOT EXISTS idx_folio_hotel_id ON folio(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_folio_stay_id ON folio(stay_id) WHERE deleted = FALSE;

-- ========== 交易流水表 ==========
CREATE TABLE IF NOT EXISTS fin_transaction (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    transaction_no VARCHAR(30) NOT NULL,
    folio_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20),
    description VARCHAR(200),
    reversal_transaction_id BIGINT,
    operator_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fin_transaction_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_fin_transaction_folio FOREIGN KEY (folio_id) REFERENCES folio(id),
    CONSTRAINT uk_fin_transaction_no UNIQUE (hotel_id, transaction_no)
);

COMMENT ON TABLE fin_transaction IS '交易流水表（只增不改）';
CREATE INDEX IF NOT EXISTS idx_fin_transaction_hotel_id ON fin_transaction(hotel_id);
CREATE INDEX IF NOT EXISTS idx_fin_transaction_folio_id ON fin_transaction(folio_id);

-- ========== 夜审表 ==========
CREATE TABLE IF NOT EXISTS night_audit (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    audit_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    total_rooms INTEGER,
    occupied_rooms INTEGER,
    available_rooms INTEGER,
    total_revenue DECIMAL(10,2),
    room_revenue DECIMAL(10,2),
    extra_revenue DECIMAL(10,2),
    occupancy_rate DECIMAL(5,2),
    adr DECIMAL(10,2),
    revpar DECIMAL(10,2),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_night_audit_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_night_audit_date UNIQUE (hotel_id, audit_date)
);

COMMENT ON TABLE night_audit IS '夜审表';
CREATE INDEX IF NOT EXISTS idx_night_audit_hotel_id ON night_audit(hotel_id);

-- ========== 操作日志表 ==========
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    module VARCHAR(50),
    action VARCHAR(50),
    target_type VARCHAR(50),
    target_id BIGINT,
    content TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operation_log_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

COMMENT ON TABLE operation_log IS '操作日志表';
CREATE INDEX IF NOT EXISTS idx_operation_log_hotel_id ON operation_log(hotel_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_created_at ON operation_log(hotel_id, created_at);

-- ========== 初始数据 ==========
-- 插入默认酒店
INSERT INTO hotel (id, name, address, phone, timezone, status) 
VALUES (1, '默认酒店', '待填写', '待填写', 'Asia/Shanghai', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- 插入默认管理员账号（密码：admin123，BCrypt加密）
INSERT INTO sys_account (id, hotel_id, username, password, real_name, role, status)
VALUES (1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- 插入默认房型
INSERT INTO room_type (id, hotel_id, name, code, bed_type, max_guests, base_price, status)
VALUES 
    (1, 1, '标准单人间', 'STD_S', '单人床', 1, 288.00, 'ACTIVE'),
    (2, 1, '标准双人间', 'STD_D', '双人床', 2, 388.00, 'ACTIVE'),
    (3, 1, '豪华大床房', 'DLX_K', '大床', 2, 488.00, 'ACTIVE'),
    (4, 1, '商务套房', 'BIZ_S', '大床', 2, 688.00, 'ACTIVE')
ON CONFLICT DO NOTHING;