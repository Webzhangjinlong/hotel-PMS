-- V1_5_0__team_reservation_tables.sql
-- 团队预订相关表

-- 团队预订单表
CREATE TABLE IF NOT EXISTS team_reservation (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    team_reservation_no VARCHAR(30) NOT NULL,
    team_name VARCHAR(100) NOT NULL,
    contact_name VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    contact_id_no VARCHAR(30),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    nights INTEGER NOT NULL,
    total_rooms INTEGER NOT NULL,
    total_amount DECIMAL(10,2) DEFAULT 0,
    settlement_type VARCHAR(20) NOT NULL DEFAULT 'UNIFIED',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    special_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_team_reservation_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_team_reservation_no UNIQUE (hotel_id, team_reservation_no)
);

CREATE INDEX IF NOT EXISTS idx_team_reservation_hotel_id ON team_reservation(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_team_reservation_status ON team_reservation(hotel_id, status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_team_reservation_check_in ON team_reservation(hotel_id, check_in_date) WHERE deleted = FALSE;

COMMENT ON TABLE team_reservation IS '团队预订单';
COMMENT ON COLUMN team_reservation.settlement_type IS '结算方式：UNIFIED-统一结算/SEPARATE-分开结算';
COMMENT ON COLUMN team_reservation.status IS '状态：PENDING/CONFIRMED/CHECKED_IN/CHECKED_OUT/CANCELLED';

-- 团队预订房间明细表
CREATE TABLE IF NOT EXISTS team_reservation_room (
    id BIGSERIAL PRIMARY KEY,
    team_reservation_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    room_id BIGINT,
    guest_name VARCHAR(50),
    guest_phone VARCHAR(20),
    guest_id_no VARCHAR(30),
    guest_gender VARCHAR(10),
    amount DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    stay_id BIGINT,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_team_room_reservation FOREIGN KEY (team_reservation_id) REFERENCES team_reservation(id),
    CONSTRAINT fk_team_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT fk_team_room_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT fk_team_room_stay FOREIGN KEY (stay_id) REFERENCES stay(id)
);

CREATE INDEX IF NOT EXISTS idx_team_room_reservation_id ON team_reservation_room(team_reservation_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_team_room_status ON team_reservation_room(team_reservation_id, status) WHERE deleted = FALSE;

COMMENT ON TABLE team_reservation_room IS '团队预订房间明细';
COMMENT ON COLUMN team_reservation_room.status IS '状态：PENDING/CHECKED_IN/CHECKED_OUT';

-- 团队账务单表
CREATE TABLE IF NOT EXISTS team_folio (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    team_reservation_id BIGINT NOT NULL,
    folio_no VARCHAR(30) NOT NULL,
    total_amount DECIMAL(10,2) DEFAULT 0,
    paid_amount DECIMAL(10,2) DEFAULT 0,
    balance DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_team_folio_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_team_folio_reservation FOREIGN KEY (team_reservation_id) REFERENCES team_reservation(id),
    CONSTRAINT uk_team_folio_no UNIQUE (hotel_id, folio_no)
);

CREATE INDEX IF NOT EXISTS idx_team_folio_reservation_id ON team_folio(team_reservation_id) WHERE deleted = FALSE;

COMMENT ON TABLE team_folio IS '团队账务单';
COMMENT ON COLUMN team_folio.status IS '状态：OPEN/CLOSED';
