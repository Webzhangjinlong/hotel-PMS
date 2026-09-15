# PMS 酒店管理系统 SQL 脚本记录

**版本**：v1.0  
**最后更新**：2026-08-20  
**数据库**：PostgreSQL 17  
**数据库名**：hotel_pms

---

## 1. 建表脚本

### 1.1 酒店表 (hotel) - 酒店主数据表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| name | VARCHAR(100) | 酒店名称 |
| address | VARCHAR(500) | 酒店地址 |
| phone | VARCHAR(20) | 联系电话 |
| timezone | VARCHAR(50) | 时区，默认Asia/Shanghai |
| status | VARCHAR(20) | 状态：ACTIVE-启用/INACTIVE-停用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE hotel (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(20),
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE hotel IS '酒店主数据表';
COMMENT ON COLUMN hotel.id IS '主键ID';
COMMENT ON COLUMN hotel.name IS '酒店名称';
COMMENT ON COLUMN hotel.address IS '酒店地址';
COMMENT ON COLUMN hotel.phone IS '联系电话';
COMMENT ON COLUMN hotel.timezone IS '时区，默认Asia/Shanghai';
COMMENT ON COLUMN hotel.status IS '状态：ACTIVE-启用/INACTIVE-停用';
COMMENT ON COLUMN hotel.created_at IS '创建时间';
COMMENT ON COLUMN hotel.updated_at IS '更新时间';
COMMENT ON COLUMN hotel.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.2 楼层表 (hotel_floor) - 酒店楼层表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID，关联hotel表 |
| floor_no | INTEGER | 楼层号 |
| name | VARCHAR(50) | 楼层名称 |
| status | VARCHAR(20) | 状态：ACTIVE-启用/INACTIVE-停用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE hotel_floor (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    floor_no INTEGER NOT NULL,
    name VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_hotel_floor_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

COMMENT ON TABLE hotel_floor IS '酒店楼层表';
COMMENT ON COLUMN hotel_floor.id IS '主键ID';
COMMENT ON COLUMN hotel_floor.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN hotel_floor.floor_no IS '楼层号';
COMMENT ON COLUMN hotel_floor.name IS '楼层名称';
COMMENT ON COLUMN hotel_floor.status IS '状态：ACTIVE-启用/INACTIVE-停用';
COMMENT ON COLUMN hotel_floor.created_at IS '创建时间';
COMMENT ON COLUMN hotel_floor.updated_at IS '更新时间';
COMMENT ON COLUMN hotel_floor.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.3 房型表 (room_type) - 房型表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID，关联hotel表 |
| name | VARCHAR(50) | 房型名称 |
| code | VARCHAR(20) | 房型编码，酒店内唯一 |
| bed_type | VARCHAR(50) | 床型 |
| max_guests | INTEGER | 最大入住人数 |
| base_price | DECIMAL(10,2) | 基础价格 |
| description | VARCHAR(500) | 房型描述 |
| status | VARCHAR(20) | 状态：ACTIVE-启用/INACTIVE-停用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE room_type (
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
    CONSTRAINT fk_room_type_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_room_type_code UNIQUE (hotel_id, code)
);

COMMENT ON TABLE room_type IS '房型表';
COMMENT ON COLUMN room_type.id IS '主键ID';
COMMENT ON COLUMN room_type.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN room_type.name IS '房型名称';
COMMENT ON COLUMN room_type.code IS '房型编码，酒店内唯一';
COMMENT ON COLUMN room_type.bed_type IS '床型：单人床/双人床/大床等';
COMMENT ON COLUMN room_type.max_guests IS '最大入住人数';
COMMENT ON COLUMN room_type.base_price IS '基础价格';
COMMENT ON COLUMN room_type.description IS '房型描述';
COMMENT ON COLUMN room_type.status IS '状态：ACTIVE-启用/INACTIVE-停用';
COMMENT ON COLUMN room_type.created_at IS '创建时间';
COMMENT ON COLUMN room_type.updated_at IS '更新时间';
COMMENT ON COLUMN room_type.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.4 房间表 (room) - 房间表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID，关联hotel表 |
| room_type_id | BIGINT | 房型ID，关联room_type表 |
| floor_id | BIGINT | 楼层ID，关联hotel_floor表 |
| room_no | VARCHAR(20) | 房间号，酒店内唯一 |
| status | VARCHAR(20) | 状态：AVAILABLE-空闲/OCCUPIED-在住/DIRTY-脏房/MAINTENANCE-维修/OOO-停用/RESERVED-预留 |
| description | VARCHAR(200) | 房间描述 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE room (
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
    CONSTRAINT fk_room_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT fk_room_floor FOREIGN KEY (floor_id) REFERENCES hotel_floor(id),
    CONSTRAINT uk_room_no UNIQUE (hotel_id, room_no)
);

COMMENT ON TABLE room IS '房间表';
COMMENT ON COLUMN room.id IS '主键ID';
COMMENT ON COLUMN room.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN room.room_type_id IS '房型ID，关联room_type表';
COMMENT ON COLUMN room.floor_id IS '楼层ID，关联hotel_floor表';
COMMENT ON COLUMN room.room_no IS '房间号，酒店内唯一';
COMMENT ON COLUMN room.status IS '状态：AVAILABLE-空闲/OCCUPIED-在住/DIRTY-脏房/MAINTENANCE-维修/OOO-停用/RESERVED-预留';
COMMENT ON COLUMN room.description IS '房间描述';
COMMENT ON COLUMN room.created_at IS '创建时间';
COMMENT ON COLUMN room.updated_at IS '更新时间';
COMMENT ON COLUMN room.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.5 房价表 (room_price) - 房价日历表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID，关联hotel表 |
| room_type_id | BIGINT | 房型ID，关联room_type表 |
| price_date | DATE | 价格日期 |
| price | DECIMAL(10,2) | 价格 |
| status | VARCHAR(20) | 状态：ACTIVE-启用/INACTIVE-停用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE room_price (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    price_date DATE NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_room_price_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_room_price_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT uk_room_price_date UNIQUE (hotel_id, room_type_id, price_date)
);

COMMENT ON TABLE room_price IS '房价日历表';
COMMENT ON COLUMN room_price.id IS '主键ID';
COMMENT ON COLUMN room_price.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN room_price.room_type_id IS '房型ID，关联room_type表';
COMMENT ON COLUMN room_price.price_date IS '价格日期';
COMMENT ON COLUMN room_price.price IS '价格';
COMMENT ON COLUMN room_price.status IS '状态：ACTIVE-启用/INACTIVE-停用';
COMMENT ON COLUMN room_price.created_at IS '创建时间';
COMMENT ON COLUMN room_price.updated_at IS '更新时间';
COMMENT ON COLUMN room_price.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.6 房态变更日志表 (room_status_log)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID，关联hotel表 |
| room_id | BIGINT | 房间ID，关联room表 |
| old_status | VARCHAR(20) | 原状态 |
| new_status | VARCHAR(20) | 新状态 |
| change_reason | VARCHAR(200) | 变更原因 |
| operator_id | BIGINT | 操作人ID |
| created_at | TIMESTAMP | 创建时间 |

```sql
CREATE TABLE room_status_log (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    change_reason VARCHAR(200),
    operator_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_status_log_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_room_status_log_room FOREIGN KEY (room_id) REFERENCES room(id)
);

COMMENT ON TABLE room_status_log IS '房态变更日志表';
COMMENT ON COLUMN room_status_log.id IS '主键ID';
COMMENT ON COLUMN room_status_log.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN room_status_log.room_id IS '房间ID，关联room表';
COMMENT ON COLUMN room_status_log.old_status IS '原状态';
COMMENT ON COLUMN room_status_log.new_status IS '新状态';
COMMENT ON COLUMN room_status_log.change_reason IS '变更原因';
COMMENT ON COLUMN room_status_log.operator_id IS '操作人ID，关联sys_account表';
COMMENT ON COLUMN room_status_log.created_at IS '创建时间';
```

### 1.7 预订单表 (reservation) - 预订单表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID，关联hotel表 |
| reservation_no | VARCHAR(30) | 预订单号，酒店内唯一 |
| source | VARCHAR(20) | 来源：WALK_IN-散客/PHONE-电话/OTA-线上渠道 |
| guest_name | VARCHAR(50) | 客人姓名 |
| guest_phone | VARCHAR(20) | 客人电话 |
| room_type_id | BIGINT | 房型ID |
| room_id | BIGINT | 房间ID |
| check_in_date | DATE | 入住日期 |
| check_out_date | DATE | 离店日期 |
| nights | INTEGER | 间夜数 |
| total_amount | DECIMAL(10,2) | 总金额 |
| status | VARCHAR(20) | 状态：PENDING-待确认/CONFIRMED-已确认/CHECKED_IN-已入住/CHECKED_OUT-已离店/CANCELLED-已取消/NO_SHOW-未到店 |
| special_requests | VARCHAR(500) | 特殊要求 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE reservation (
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
    CONSTRAINT fk_reservation_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_reservation_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id),
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT uk_reservation_no UNIQUE (hotel_id, reservation_no)
);

COMMENT ON TABLE reservation IS '预订单表';
COMMENT ON COLUMN reservation.id IS '主键ID';
COMMENT ON COLUMN reservation.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN reservation.reservation_no IS '预订单号，酒店内唯一';
COMMENT ON COLUMN reservation.source IS '来源：WALK_IN-散客/PHONE-电话/OTA-线上渠道';
COMMENT ON COLUMN reservation.guest_name IS '客人姓名';
COMMENT ON COLUMN reservation.guest_phone IS '客人电话';
COMMENT ON COLUMN reservation.room_type_id IS '房型ID，关联room_type表';
COMMENT ON COLUMN reservation.room_id IS '房间ID，关联room表';
COMMENT ON COLUMN reservation.check_in_date IS '入住日期';
COMMENT ON COLUMN reservation.check_out_date IS '离店日期';
COMMENT ON COLUMN reservation.nights IS '间夜数';
COMMENT ON COLUMN reservation.total_amount IS '总金额';
COMMENT ON COLUMN reservation.status IS '状态：PENDING-待确认/CONFIRMED-已确认/CHECKED_IN-已入住/CHECKED_OUT-已离店/CANCELLED-已取消/NO_SHOW-未到店';
COMMENT ON COLUMN reservation.special_requests IS '特殊要求';
COMMENT ON COLUMN reservation.created_at IS '创建时间';
COMMENT ON COLUMN reservation.updated_at IS '更新时间';
COMMENT ON COLUMN reservation.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.8 入住单表 (stay) - 入住单表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID，关联hotel表 |
| stay_no | VARCHAR(30) | 入住单号，酒店内唯一 |
| reservation_id | BIGINT | 预订单ID |
| room_id | BIGINT | 房间ID |
| guest_id | BIGINT | 主客人ID |
| check_in_time | TIMESTAMP | 入住时间 |
| check_out_time | TIMESTAMP | 预计离店时间 |
| actual_check_out_time | TIMESTAMP | 实际离店时间 |
| status | VARCHAR(20) | 状态：CHECKED_IN-在住/CHECKED_OUT-已离店 |
| total_amount | DECIMAL(10,2) | 总金额 |
| paid_amount | DECIMAL(10,2) | 已付金额 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE stay (
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
    CONSTRAINT fk_stay_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_stay_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(id),
    CONSTRAINT fk_stay_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT uk_stay_no UNIQUE (hotel_id, stay_no)
);

COMMENT ON TABLE stay IS '入住单表';
COMMENT ON COLUMN stay.id IS '主键ID';
COMMENT ON COLUMN stay.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN stay.stay_no IS '入住单号，酒店内唯一';
COMMENT ON COLUMN stay.reservation_id IS '预订单ID，关联reservation表';
COMMENT ON COLUMN stay.room_id IS '房间ID，关联room表';
COMMENT ON COLUMN stay.guest_id IS '主客人ID，关联guest表';
COMMENT ON COLUMN stay.check_in_time IS '入住时间';
COMMENT ON COLUMN stay.check_out_time IS '预计离店时间';
COMMENT ON COLUMN stay.actual_check_out_time IS '实际离店时间';
COMMENT ON COLUMN stay.status IS '状态：CHECKED_IN-在住/CHECKED_OUT-已离店';
COMMENT ON COLUMN stay.total_amount IS '总金额';
COMMENT ON COLUMN stay.paid_amount IS '已付金额';
COMMENT ON COLUMN stay.created_at IS '创建时间';
COMMENT ON COLUMN stay.updated_at IS '更新时间';
COMMENT ON COLUMN stay.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.9 入住人表 (stay_guest) - 入住人关联表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| stay_id | BIGINT | 入住单ID |
| guest_id | BIGINT | 客人ID |
| is_primary | BOOLEAN | 是否主客人 |
| created_at | TIMESTAMP | 创建时间 |

```sql
CREATE TABLE stay_guest (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    stay_id BIGINT NOT NULL,
    guest_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stay_guest_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_stay_guest_stay FOREIGN KEY (stay_id) REFERENCES stay(id),
    CONSTRAINT fk_stay_guest_guest FOREIGN KEY (guest_id) REFERENCES guest(id)
);

COMMENT ON TABLE stay_guest IS '入住人关联表';
COMMENT ON COLUMN stay_guest.id IS '主键ID';
COMMENT ON COLUMN stay_guest.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN stay_guest.stay_id IS '入住单ID，关联stay表';
COMMENT ON COLUMN stay_guest.guest_id IS '客人ID，关联guest表';
COMMENT ON COLUMN stay_guest.is_primary IS '是否主客人：TRUE-是/FALSE-否';
COMMENT ON COLUMN stay_guest.created_at IS '创建时间';
```

### 1.10 客人档案表 (guest) - 客人档案表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| name | VARCHAR(50) | 客人姓名 |
| gender | VARCHAR(10) | 性别：MALE-男/FEMALE-女 |
| id_type | VARCHAR(20) | 证件类型：ID_CARD-身份证/PASSPORT-护照 |
| id_no | VARCHAR(100) | 证件号（加密存储） |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| nationality | VARCHAR(50) | 国籍 |
| address | VARCHAR(500) | 地址 |
| vip_level | INTEGER | VIP等级：0-普通/1-银卡/2-金卡/3-铂金 |
| stay_count | INTEGER | 累计入住次数 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE guest (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    id_type VARCHAR(20),
    id_no VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    nationality VARCHAR(50),
    address VARCHAR(500),
    vip_level INTEGER DEFAULT 0,
    stay_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_guest_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

COMMENT ON TABLE guest IS '客人档案表';
COMMENT ON COLUMN guest.id IS '主键ID';
COMMENT ON COLUMN guest.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN guest.name IS '客人姓名';
COMMENT ON COLUMN guest.gender IS '性别：MALE-男/FEMALE-女';
COMMENT ON COLUMN guest.id_type IS '证件类型：ID_CARD-身份证/PASSPORT-护照/OTHER-其他';
COMMENT ON COLUMN guest.id_no IS '证件号（AES-GCM加密存储）';
COMMENT ON COLUMN guest.phone IS '手机号';
COMMENT ON COLUMN guest.email IS '邮箱';
COMMENT ON COLUMN guest.nationality IS '国籍';
COMMENT ON COLUMN guest.address IS '地址';
COMMENT ON COLUMN guest.vip_level IS 'VIP等级：0-普通/1-银卡/2-金卡/3-铂金';
COMMENT ON COLUMN guest.stay_count IS '累计入住次数';
COMMENT ON COLUMN guest.created_at IS '创建时间';
COMMENT ON COLUMN guest.updated_at IS '更新时间';
COMMENT ON COLUMN guest.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.11 公安上传记录表 (police_upload_log)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| stay_id | BIGINT | 入住单ID |
| guest_id | BIGINT | 客人ID |
| upload_type | VARCHAR(20) | 上传类型：CHECK_IN-入住/CHECK_OUT-退房 |
| status | VARCHAR(20) | 状态：PENDING-待上传/SUCCESS-成功/FAILED-失败 |
| response_code | VARCHAR(20) | 响应码 |
| response_message | VARCHAR(500) | 响应消息 |
| retry_count | INTEGER | 已重试次数 |
| next_retry_time | TIMESTAMP | 下次重试时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

```sql
CREATE TABLE police_upload_log (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    stay_id BIGINT NOT NULL,
    guest_id BIGINT NOT NULL,
    upload_type VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    response_code VARCHAR(20),
    response_message VARCHAR(500),
    retry_count INTEGER DEFAULT 0,
    next_retry_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_police_upload_log_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_police_upload_log_stay FOREIGN KEY (stay_id) REFERENCES stay(id),
    CONSTRAINT fk_police_upload_log_guest FOREIGN KEY (guest_id) REFERENCES guest(id)
);

COMMENT ON TABLE police_upload_log IS '公安上传记录表';
COMMENT ON COLUMN police_upload_log.id IS '主键ID';
COMMENT ON COLUMN police_upload_log.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN police_upload_log.stay_id IS '入住单ID，关联stay表';
COMMENT ON COLUMN police_upload_log.guest_id IS '客人ID，关联guest表';
COMMENT ON COLUMN police_upload_log.upload_type IS '上传类型：CHECK_IN-入住/CHECK_OUT-退房';
COMMENT ON COLUMN police_upload_log.status IS '状态：PENDING-待上传/SUCCESS-成功/FAILED-失败';
COMMENT ON COLUMN police_upload_log.response_code IS '响应码';
COMMENT ON COLUMN police_upload_log.response_message IS '响应消息';
COMMENT ON COLUMN police_upload_log.retry_count IS '已重试次数';
COMMENT ON COLUMN police_upload_log.next_retry_time IS '下次重试时间';
COMMENT ON COLUMN police_upload_log.created_at IS '创建时间';
COMMENT ON COLUMN police_upload_log.updated_at IS '更新时间';
```

### 1.12 账务单表 (folio)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| folio_no | VARCHAR(30) | 账务单号 |
| stay_id | BIGINT | 入住单ID |
| guest_id | BIGINT | 客人ID |
| total_amount | DECIMAL(10,2) | 应收总额 |
| paid_amount | DECIMAL(10,2) | 已收金额 |
| balance | DECIMAL(10,2) | 余额 |
| status | VARCHAR(20) | 状态：OPEN-未结/CLOSED-已结 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE folio (
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
    CONSTRAINT fk_folio_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_folio_stay FOREIGN KEY (stay_id) REFERENCES stay(id),
    CONSTRAINT fk_folio_guest FOREIGN KEY (guest_id) REFERENCES guest(id),
    CONSTRAINT uk_folio_no UNIQUE (hotel_id, folio_no)
);

COMMENT ON TABLE folio IS '账务单表';
COMMENT ON COLUMN folio.id IS '主键ID';
COMMENT ON COLUMN folio.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN folio.folio_no IS '账务单号，酒店内唯一';
COMMENT ON COLUMN folio.stay_id IS '入住单ID，关联stay表';
COMMENT ON COLUMN folio.guest_id IS '客人ID，关联guest表';
COMMENT ON COLUMN folio.total_amount IS '应收总额';
COMMENT ON COLUMN folio.paid_amount IS '已收金额';
COMMENT ON COLUMN folio.balance IS '余额（应收-已收）';
COMMENT ON COLUMN folio.status IS '状态：OPEN-未结/CLOSED-已结';
COMMENT ON COLUMN folio.created_at IS '创建时间';
COMMENT ON COLUMN folio.updated_at IS '更新时间';
COMMENT ON COLUMN folio.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.13 交易流水表 (fin_transaction) - 只增不改

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| transaction_no | VARCHAR(30) | 交易号 |
| folio_id | BIGINT | 账务单ID |
| type | VARCHAR(20) | 类型：DEPOSIT-押金/ROOM_FEE-房费/EXTRA-杂费/PAYMENT-付款/REFUND-退款/REVERSAL-冲账 |
| amount | DECIMAL(10,2) | 金额 |
| payment_method | VARCHAR(20) | 支付方式：CASH-现金/WECHAT-微信/ALIPAY-支付宝/POS-刷卡 |
| description | VARCHAR(200) | 交易描述 |
| reversal_transaction_id | BIGINT | 冲账交易ID |
| operator_id | BIGINT | 操作人ID |
| created_at | TIMESTAMP | 创建时间 |

```sql
CREATE TABLE fin_transaction (
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

COMMENT ON TABLE fin_transaction IS '交易流水表（只增不改，错误用冲账纠正）';
COMMENT ON COLUMN fin_transaction.id IS '主键ID';
COMMENT ON COLUMN fin_transaction.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN fin_transaction.transaction_no IS '交易号，酒店内唯一';
COMMENT ON COLUMN fin_transaction.folio_id IS '账务单ID，关联folio表';
COMMENT ON COLUMN fin_transaction.type IS '类型：DEPOSIT-押金/ROOM_FEE-房费/EXTRA-杂费/PAYMENT-付款/REFUND-退款/REVERSAL-冲账';
COMMENT ON COLUMN fin_transaction.amount IS '金额（正数为收入，负数为支出）';
COMMENT ON COLUMN fin_transaction.payment_method IS '支付方式：CASH-现金/WECHAT-微信/ALIPAY-支付宝/POS-刷卡';
COMMENT ON COLUMN fin_transaction.description IS '交易描述';
COMMENT ON COLUMN fin_transaction.reversal_transaction_id IS '冲账交易ID（仅REVERSAL类型有值）';
COMMENT ON COLUMN fin_transaction.operator_id IS '操作人ID，关联sys_account表';
COMMENT ON COLUMN fin_transaction.created_at IS '创建时间';
```

### 1.14 支付记录表 (payment)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| payment_no | VARCHAR(30) | 支付号 |
| fin_transaction_id | BIGINT | 交易流水ID |
| payment_method | VARCHAR(20) | 支付方式 |
| amount | DECIMAL(10,2) | 支付金额 |
| status | VARCHAR(20) | 状态：PENDING-待支付/SUCCESS-成功/FAILED-失败 |
| third_party_no | VARCHAR(100) | 第三方交易号 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

```sql
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    payment_no VARCHAR(30) NOT NULL,
    fin_transaction_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    third_party_no VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_payment_fin_transaction FOREIGN KEY (fin_transaction_id) REFERENCES fin_transaction(id),
    CONSTRAINT uk_payment_no UNIQUE (hotel_id, payment_no)
);

COMMENT ON TABLE payment IS '支付记录表';
COMMENT ON COLUMN payment.id IS '主键ID';
COMMENT ON COLUMN payment.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN payment.payment_no IS '支付号，酒店内唯一';
COMMENT ON COLUMN payment.fin_transaction_id IS '交易流水ID，关联fin_transaction表';
COMMENT ON COLUMN payment.payment_method IS '支付方式：CASH-现金/WECHAT-微信/ALIPAY-支付宝/POS-刷卡';
COMMENT ON COLUMN payment.amount IS '支付金额';
COMMENT ON COLUMN payment.status IS '状态：PENDING-待支付/SUCCESS-成功/FAILED-失败';
COMMENT ON COLUMN payment.third_party_no IS '第三方交易号';
COMMENT ON COLUMN payment.created_at IS '创建时间';
COMMENT ON COLUMN payment.updated_at IS '更新时间';
```

### 1.15 夜审表 (night_audit)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| audit_date | DATE | 审计日期 |
| status | VARCHAR(20) | 状态：PENDING-待处理/PROCESSING-处理中/COMPLETED-已完成/FAILED-失败 |
| total_rooms | INTEGER | 总房间数 |
| occupied_rooms | INTEGER | 在住房间数 |
| available_rooms | INTEGER | 可用房间数 |
| total_revenue | DECIMAL(10,2) | 总营收 |
| room_revenue | DECIMAL(10,2) | 房费收入 |
| extra_revenue | DECIMAL(10,2) | 杂费收入 |
| occupancy_rate | DECIMAL(5,2) | 入住率 |
| adr | DECIMAL(10,2) | 平均房价 |
| revpar | DECIMAL(10,2) | 每间可售房收入 |
| started_at | TIMESTAMP | 开始时间 |
| completed_at | TIMESTAMP | 完成时间 |
| error_message | VARCHAR(1000) | 错误信息 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

```sql
CREATE TABLE night_audit (
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
    CONSTRAINT fk_night_audit_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_night_audit_date UNIQUE (hotel_id, audit_date)
);

COMMENT ON TABLE night_audit IS '夜审记录表';
COMMENT ON COLUMN night_audit.id IS '主键ID';
COMMENT ON COLUMN night_audit.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN night_audit.audit_date IS '审计日期';
COMMENT ON COLUMN night_audit.status IS '状态：PENDING-待处理/PROCESSING-处理中/COMPLETED-已完成/FAILED-失败';
COMMENT ON COLUMN night_audit.total_rooms IS '总房间数';
COMMENT ON COLUMN night_audit.occupied_rooms IS '在住房间数';
COMMENT ON COLUMN night_audit.available_rooms IS '可用房间数';
COMMENT ON COLUMN night_audit.total_revenue IS '总营收';
COMMENT ON COLUMN night_audit.room_revenue IS '房费收入';
COMMENT ON COLUMN night_audit.extra_revenue IS '杂费收入';
COMMENT ON COLUMN night_audit.occupancy_rate IS '入住率（百分比）';
COMMENT ON COLUMN night_audit.adr IS '平均房价（Average Daily Rate）';
COMMENT ON COLUMN night_audit.revpar IS '每间可售房收入（Revenue Per Available Room）';
COMMENT ON COLUMN night_audit.started_at IS '开始时间';
COMMENT ON COLUMN night_audit.completed_at IS '完成时间';
COMMENT ON COLUMN night_audit.error_message IS '错误信息';
COMMENT ON COLUMN night_audit.created_at IS '创建时间';
COMMENT ON COLUMN night_audit.updated_at IS '更新时间';
```

### 1.16 交接班表 (shift)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| shift_no | VARCHAR(30) | 班次号 |
| operator_id | BIGINT | 操作人ID |
| start_time | TIMESTAMP | 开始时间 |
| end_time | TIMESTAMP | 结束时间 |
| status | VARCHAR(20) | 状态：OPEN-进行中/CLOSED-已结束 |
| total_cash | DECIMAL(10,2) | 现金收入合计 |
| total_wechat | DECIMAL(10,2) | 微信收入合计 |
| total_alipay | DECIMAL(10,2) | 支付宝收入合计 |
| total_pos | DECIMAL(10,2) | POS刷卡收入合计 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

```sql
CREATE TABLE shift (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    shift_no VARCHAR(30) NOT NULL,
    operator_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'OPEN',
    total_cash DECIMAL(10,2) DEFAULT 0,
    total_wechat DECIMAL(10,2) DEFAULT 0,
    total_alipay DECIMAL(10,2) DEFAULT 0,
    total_pos DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shift_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_shift_no UNIQUE (hotel_id, shift_no)
);

COMMENT ON TABLE shift IS '交接班记录表';
COMMENT ON COLUMN shift.id IS '主键ID';
COMMENT ON COLUMN shift.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN shift.shift_no IS '班次号，酒店内唯一';
COMMENT ON COLUMN shift.operator_id IS '操作人ID，关联sys_account表';
COMMENT ON COLUMN shift.start_time IS '班次开始时间';
COMMENT ON COLUMN shift.end_time IS '班次结束时间';
COMMENT ON COLUMN shift.status IS '状态：OPEN-进行中/CLOSED-已结束';
COMMENT ON COLUMN shift.total_cash IS '现金收入合计';
COMMENT ON COLUMN shift.total_wechat IS '微信收入合计';
COMMENT ON COLUMN shift.total_alipay IS '支付宝收入合计';
COMMENT ON COLUMN shift.total_pos IS 'POS刷卡收入合计';
COMMENT ON COLUMN shift.created_at IS '创建时间';
COMMENT ON COLUMN shift.updated_at IS '更新时间';
```

### 1.17 操作日志表 (operation_log)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| operator_id | BIGINT | 操作人ID |
| operator_name | VARCHAR(50) | 操作人姓名 |
| module | VARCHAR(50) | 操作模块 |
| action | VARCHAR(50) | 操作动作 |
| target_type | VARCHAR(50) | 目标类型 |
| target_id | BIGINT | 目标ID |
| content | TEXT | 操作内容 |
| ip_address | VARCHAR(50) | IP地址 |
| created_at | TIMESTAMP | 创建时间 |

```sql
CREATE TABLE operation_log (
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
COMMENT ON COLUMN operation_log.id IS '主键ID';
COMMENT ON COLUMN operation_log.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN operation_log.operator_id IS '操作人ID，关联sys_account表';
COMMENT ON COLUMN operation_log.operator_name IS '操作人姓名';
COMMENT ON COLUMN operation_log.module IS '操作模块：RESERVATION-预订/STAY-入住/FOLIO-账务等';
COMMENT ON COLUMN operation_log.action IS '操作动作：CREATE-创建/UPDATE-更新/DELETE-删除/LOGIN-登录等';
COMMENT ON COLUMN operation_log.target_type IS '目标类型：reservation/stay/folio等';
COMMENT ON COLUMN operation_log.target_id IS '目标ID';
COMMENT ON COLUMN operation_log.content IS '操作内容详情（JSON格式）';
COMMENT ON COLUMN operation_log.ip_address IS '操作人IP地址';
COMMENT ON COLUMN operation_log.created_at IS '创建时间';
```

### 1.18 系统账号表 (sys_account)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| username | VARCHAR(50) | 登录用户名 |
| password | VARCHAR(100) | 登录密码（BCrypt加密） |
| real_name | VARCHAR(50) | 真实姓名 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| role | VARCHAR(20) | 角色：ADMIN-管理员/MANAGER-店长/RECEPTIONIST-前台 |
| status | VARCHAR(20) | 状态：ACTIVE-启用/INACTIVE-停用 |
| last_login_time | TIMESTAMP | 最后登录时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE sys_account (
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_sys_account_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_sys_account_username UNIQUE (hotel_id, username)
);

COMMENT ON TABLE sys_account IS '系统账号表';
COMMENT ON COLUMN sys_account.id IS '主键ID';
COMMENT ON COLUMN sys_account.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN sys_account.username IS '登录用户名，酒店内唯一';
COMMENT ON COLUMN sys_account.password IS '登录密码（BCrypt加密）';
COMMENT ON COLUMN sys_account.real_name IS '真实姓名';
COMMENT ON COLUMN sys_account.phone IS '手机号';
COMMENT ON COLUMN sys_account.email IS '邮箱';
COMMENT ON COLUMN sys_account.role IS '角色：ADMIN-管理员/MANAGER-店长/RECEPTIONIST-前台';
COMMENT ON COLUMN sys_account.status IS '状态：ACTIVE-启用/INACTIVE-停用';
COMMENT ON COLUMN sys_account.last_login_time IS '最后登录时间';
COMMENT ON COLUMN sys_account.created_at IS '创建时间';
COMMENT ON COLUMN sys_account.updated_at IS '更新时间';
COMMENT ON COLUMN sys_account.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.19 集成发件箱表 (integration_outbox)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| event_id | VARCHAR(50) | 事件ID，全局唯一 |
| event_type | VARCHAR(50) | 事件类型 |
| payload | JSONB | 事件数据 |
| status | VARCHAR(20) | 状态：PENDING-待投递/DELIVERED-已投递/FAILED-失败 |
| retry_count | INTEGER | 已重试次数 |
| next_retry_time | TIMESTAMP | 下次重试时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

```sql
CREATE TABLE integration_outbox (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    event_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    retry_count INTEGER DEFAULT 0,
    next_retry_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_integration_outbox_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_integration_outbox_event_id UNIQUE (event_id)
);

COMMENT ON TABLE integration_outbox IS '集成事件发件箱';
COMMENT ON COLUMN integration_outbox.id IS '主键ID';
COMMENT ON COLUMN integration_outbox.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN integration_outbox.event_id IS '事件ID，全局唯一';
COMMENT ON COLUMN integration_outbox.event_type IS '事件类型：HotelChanged/RoomTypeChanged/RoomChanged/PriceChanged/RoomStatusChanged/StayCheckedIn/StayCheckedOut/ReservationChanged';
COMMENT ON COLUMN integration_outbox.payload IS '事件数据（JSON格式）';
COMMENT ON COLUMN integration_outbox.status IS '状态：PENDING-待投递/DELIVERED-已投递/FAILED-投递失败';
COMMENT ON COLUMN integration_outbox.retry_count IS '已重试次数';
COMMENT ON COLUMN integration_outbox.next_retry_time IS '下次重试时间';
COMMENT ON COLUMN integration_outbox.created_at IS '创建时间';
COMMENT ON COLUMN integration_outbox.updated_at IS '更新时间';
```

### 1.20 集成事件日志表 (integration_event_log)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| event_id | VARCHAR(50) | 事件ID |
| event_type | VARCHAR(50) | 事件类型 |
| direction | VARCHAR(10) | 方向：INBOUND-入站/OUTBOUND-出站 |
| channel | VARCHAR(50) | 渠道编码 |
| payload | JSONB | 事件数据 |
| status | VARCHAR(20) | 处理状态 |
| error_message | VARCHAR(1000) | 错误信息 |
| created_at | TIMESTAMP | 创建时间 |

```sql
CREATE TABLE integration_event_log (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    event_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    channel VARCHAR(50),
    payload JSONB,
    status VARCHAR(20),
    error_message VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_integration_event_log_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

COMMENT ON TABLE integration_event_log IS '集成事件日志表';
COMMENT ON COLUMN integration_event_log.id IS '主键ID';
COMMENT ON COLUMN integration_event_log.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN integration_event_log.event_id IS '事件ID';
COMMENT ON COLUMN integration_event_log.event_type IS '事件类型';
COMMENT ON COLUMN integration_event_log.direction IS '方向：INBOUND-入站/OUTBOUND-出站';
COMMENT ON COLUMN integration_event_log.channel IS '渠道编码';
COMMENT ON COLUMN integration_event_log.payload IS '事件数据（JSON格式）';
COMMENT ON COLUMN integration_event_log.status IS '处理状态';
COMMENT ON COLUMN integration_event_log.error_message IS '错误信息';
COMMENT ON COLUMN integration_event_log.created_at IS '创建时间';
```

### 1.21 集成渠道配置表 (integration_channel_config)

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| channel_code | VARCHAR(50) | 渠道编码 |
| channel_name | VARCHAR(100) | 渠道名称 |
| app_id | VARCHAR(100) | 应用ID |
| app_secret | VARCHAR(200) | 应用密钥（加密存储） |
| webhook_url | VARCHAR(500) | Webhook回调地址 |
| status | VARCHAR(20) | 状态：ACTIVE-启用/INACTIVE-停用 |
| config | JSONB | 扩展配置 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE integration_channel_config (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    channel_code VARCHAR(50) NOT NULL,
    channel_name VARCHAR(100),
    app_id VARCHAR(100),
    app_secret VARCHAR(200),
    webhook_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    config JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_integration_channel_config_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_integration_channel_config_code UNIQUE (hotel_id, channel_code)
);

COMMENT ON TABLE integration_channel_config IS '集成渠道配置表';
COMMENT ON COLUMN integration_channel_config.id IS '主键ID';
COMMENT ON COLUMN integration_channel_config.hotel_id IS '酒店ID，关联hotel表';
COMMENT ON COLUMN integration_channel_config.channel_code IS '渠道编码，酒店内唯一';
COMMENT ON COLUMN integration_channel_config.channel_name IS '渠道名称';
COMMENT ON COLUMN integration_channel_config.app_id IS '应用ID';
COMMENT ON COLUMN integration_channel_config.app_secret IS '应用密钥（AES-GCM加密存储）';
COMMENT ON COLUMN integration_channel_config.webhook_url IS 'Webhook回调地址';
COMMENT ON COLUMN integration_channel_config.status IS '状态：ACTIVE-启用/INACTIVE-停用';
COMMENT ON COLUMN integration_channel_config.config IS '扩展配置（JSON格式）';
COMMENT ON COLUMN integration_channel_config.created_at IS '创建时间';
COMMENT ON COLUMN integration_channel_config.updated_at IS '更新时间';
COMMENT ON COLUMN integration_channel_config.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

### 1.22 系统配置表 (sys_config) - 系统参数配置表

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID（NULL表示全局配置） |
| config_key | VARCHAR(100) | 配置键 |
| config_value | TEXT | 配置值 |
| config_type | VARCHAR(20) | 配置类型：STRING/NUMBER/BOOLEAN/JSON |
| description | VARCHAR(500) | 配置说明 |
| is_system | BOOLEAN | 是否系统配置 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除标记 |

```sql
CREATE TABLE sys_config (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(20) DEFAULT 'STRING',
    description VARCHAR(500),
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT uk_sys_config_key UNIQUE (hotel_id, config_key)
);

COMMENT ON TABLE sys_config IS '系统参数配置表';
COMMENT ON COLUMN sys_config.id IS '主键ID';
COMMENT ON COLUMN sys_config.hotel_id IS '酒店ID（NULL表示全局配置）';
COMMENT ON COLUMN sys_config.config_key IS '配置键，酒店内唯一';
COMMENT ON COLUMN sys_config.config_value IS '配置值';
COMMENT ON COLUMN sys_config.config_type IS '配置类型：STRING-字符串/NUMBER-数字/BOOLEAN-布尔/JSON-JSON对象';
COMMENT ON COLUMN sys_config.description IS '配置说明';
COMMENT ON COLUMN sys_config.is_system IS '是否系统配置：TRUE-系统配置（不可删除）/FALSE-业务配置';
COMMENT ON COLUMN sys_config.created_at IS '创建时间';
COMMENT ON COLUMN sys_config.updated_at IS '更新时间';
COMMENT ON COLUMN sys_config.deleted IS '逻辑删除标记：FALSE-未删除/TRUE-已删除';
```

---

## 2. 索引创建脚本

### 2.1 酒店表索引
```sql
CREATE INDEX idx_hotel_status ON hotel(status) WHERE deleted = FALSE;
```

### 2.2 楼层表索引
```sql
CREATE INDEX idx_hotel_floor_hotel_id ON hotel_floor(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_hotel_floor_floor_no ON hotel_floor(hotel_id, floor_no) WHERE deleted = FALSE;
```

### 2.3 房型表索引
```sql
CREATE INDEX idx_room_type_hotel_id ON room_type(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_room_type_status ON room_type(hotel_id, status) WHERE deleted = FALSE;
```

### 2.4 房间表索引
```sql
CREATE INDEX idx_room_hotel_id ON room(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_room_room_type_id ON room(room_type_id) WHERE deleted = FALSE;
CREATE INDEX idx_room_floor_id ON room(floor_id) WHERE deleted = FALSE;
CREATE INDEX idx_room_status ON room(hotel_id, status) WHERE deleted = FALSE;
```

### 2.5 房价表索引
```sql
CREATE INDEX idx_room_price_hotel_id ON room_price(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_room_price_room_type_id ON room_price(room_type_id) WHERE deleted = FALSE;
CREATE INDEX idx_room_price_date ON room_price(hotel_id, price_date) WHERE deleted = FALSE;
```

### 2.6 房态变更日志表索引
```sql
CREATE INDEX idx_room_status_log_hotel_id ON room_status_log(hotel_id);
CREATE INDEX idx_room_status_log_room_id ON room_status_log(room_id);
CREATE INDEX idx_room_status_log_created_at ON room_status_log(hotel_id, created_at);
```

### 2.7 预订单表索引
```sql
CREATE INDEX idx_reservation_hotel_id ON reservation(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_reservation_status ON reservation(hotel_id, status) WHERE deleted = FALSE;
CREATE INDEX idx_reservation_check_in_date ON reservation(hotel_id, check_in_date) WHERE deleted = FALSE;
CREATE INDEX idx_reservation_guest_phone ON reservation(hotel_id, guest_phone) WHERE deleted = FALSE;
```

### 2.8 入住单表索引
```sql
CREATE INDEX idx_stay_hotel_id ON stay(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_stay_room_id ON stay(room_id) WHERE deleted = FALSE;
CREATE INDEX idx_stay_reservation_id ON stay(reservation_id) WHERE deleted = FALSE;
CREATE INDEX idx_stay_status ON stay(hotel_id, status) WHERE deleted = FALSE;
CREATE INDEX idx_stay_check_in_time ON stay(hotel_id, check_in_time) WHERE deleted = FALSE;
CREATE UNIQUE INDEX uk_stay_room_active ON stay(room_id, status) WHERE status = 'CHECKED_IN' AND deleted = FALSE;
```

### 2.9 入住人表索引
```sql
CREATE INDEX idx_stay_guest_hotel_id ON stay_guest(hotel_id);
CREATE INDEX idx_stay_guest_stay_id ON stay_guest(stay_id);
CREATE INDEX idx_stay_guest_guest_id ON stay_guest(guest_id);
```

### 2.10 客人档案表索引
```sql
CREATE INDEX idx_guest_hotel_id ON guest(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_guest_phone ON guest(hotel_id, phone) WHERE deleted = FALSE;
CREATE INDEX idx_guest_id_no ON guest(hotel_id, id_no) WHERE deleted = FALSE;
```

### 2.11 公安上传记录表索引
```sql
CREATE INDEX idx_police_upload_log_hotel_id ON police_upload_log(hotel_id);
CREATE INDEX idx_police_upload_log_stay_id ON police_upload_log(stay_id);
CREATE INDEX idx_police_upload_log_status ON police_upload_log(hotel_id, status);
CREATE INDEX idx_police_upload_log_next_retry ON police_upload_log(next_retry_time) WHERE status = 'PENDING';
```

### 2.12 账务单表索引
```sql
CREATE INDEX idx_folio_hotel_id ON folio(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_folio_stay_id ON folio(stay_id) WHERE deleted = FALSE;
CREATE INDEX idx_folio_status ON folio(hotel_id, status) WHERE deleted = FALSE;
```

### 2.13 交易流水表索引
```sql
CREATE INDEX idx_fin_transaction_hotel_id ON fin_transaction(hotel_id);
CREATE INDEX idx_fin_transaction_folio_id ON fin_transaction(folio_id);
CREATE INDEX idx_fin_transaction_type ON fin_transaction(hotel_id, type);
CREATE INDEX idx_fin_transaction_created_at ON fin_transaction(hotel_id, created_at);
```

### 2.14 支付记录表索引
```sql
CREATE INDEX idx_payment_hotel_id ON payment(hotel_id);
CREATE INDEX idx_payment_fin_transaction_id ON payment(fin_transaction_id);
CREATE INDEX idx_payment_status ON payment(hotel_id, status);
```

### 2.15 夜审表索引
```sql
CREATE INDEX idx_night_audit_hotel_id ON night_audit(hotel_id);
CREATE INDEX idx_night_audit_status ON night_audit(hotel_id, status);
CREATE INDEX idx_night_audit_audit_date ON night_audit(hotel_id, audit_date);
```

### 2.16 交接班表索引
```sql
CREATE INDEX idx_shift_hotel_id ON shift(hotel_id);
CREATE INDEX idx_shift_operator_id ON shift(operator_id);
CREATE INDEX idx_shift_status ON shift(hotel_id, status);
CREATE INDEX idx_shift_start_time ON shift(hotel_id, start_time);
```

### 2.17 操作日志表索引
```sql
CREATE INDEX idx_operation_log_hotel_id ON operation_log(hotel_id);
CREATE INDEX idx_operation_log_operator_id ON operation_log(operator_id);
CREATE INDEX idx_operation_log_module ON operation_log(hotel_id, module);
CREATE INDEX idx_operation_log_created_at ON operation_log(hotel_id, created_at);
```

### 2.18 系统账号表索引
```sql
CREATE INDEX idx_sys_account_hotel_id ON sys_account(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_sys_account_role ON sys_account(hotel_id, role) WHERE deleted = FALSE;
CREATE INDEX idx_sys_account_status ON sys_account(hotel_id, status) WHERE deleted = FALSE;
```

### 2.19 集成发件箱表索引
```sql
CREATE INDEX idx_integration_outbox_hotel_id ON integration_outbox(hotel_id);
CREATE INDEX idx_integration_outbox_status ON integration_outbox(hotel_id, status);
CREATE INDEX idx_integration_outbox_next_retry ON integration_outbox(next_retry_time) WHERE status = 'PENDING';
```

### 2.20 集成事件日志表索引
```sql
CREATE INDEX idx_integration_event_log_hotel_id ON integration_event_log(hotel_id);
CREATE INDEX idx_integration_event_log_event_id ON integration_event_log(event_id);
CREATE INDEX idx_integration_event_log_direction ON integration_event_log(hotel_id, direction);
CREATE INDEX idx_integration_event_log_created_at ON integration_event_log(hotel_id, created_at);
```

### 2.21 集成渠道配置表索引
```sql
CREATE INDEX idx_integration_channel_config_hotel_id ON integration_channel_config(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_integration_channel_config_status ON integration_channel_config(hotel_id, status) WHERE deleted = FALSE;
```

### 2.22 系统配置表索引
```sql
CREATE INDEX idx_sys_config_hotel_id ON sys_config(hotel_id) WHERE deleted = FALSE;
CREATE INDEX idx_sys_config_config_key ON sys_config(config_key) WHERE deleted = FALSE;
```

---

## 3. 系统参数配置数据脚本

### 3.1 全局系统参数配置数据

```sql
INSERT INTO sys_config (hotel_id, config_key, config_value, config_type, description, is_system) VALUES
-- 系统基础配置
(NULL, 'system.name', 'PMS酒店管理系统', 'STRING', '系统名称', TRUE),
(NULL, 'system.version', '1.0.0', 'STRING', '系统版本', TRUE),
(NULL, 'system.timezone', 'Asia/Shanghai', 'STRING', '系统时区', TRUE),
(NULL, 'system.language', 'zh_CN', 'STRING', '系统语言', TRUE),

-- 酒店运营配置
(NULL, 'hotel.check_in_time', '14:00', 'STRING', '默认入住时间', FALSE),
(NULL, 'hotel.check_out_time', '12:00', 'STRING', '默认离店时间', FALSE),
(NULL, 'hotel.late_check_out_half_day', '18:00', 'STRING', '超时半日租截止时间', FALSE),
(NULL, 'hotel.late_check_out_full_day', '24:00', 'STRING', '超时全日租截止时间', FALSE),
(NULL, 'hotel.early_check_in_time', '06:00', 'STRING', '提前入住时间', FALSE),
(NULL, 'hotel.night_audit_time', '04:00', 'STRING', '夜审执行时间', FALSE),
(NULL, 'hotel.max_guests_per_room', '4', 'NUMBER', '每间房最大入住人数', FALSE),

-- 房态配置
(NULL, 'room.status.available', '空闲', 'STRING', '房态-空闲显示名称', FALSE),
(NULL, 'room.status.occupied', '在住', 'STRING', '房态-在住显示名称', FALSE),
(NULL, 'room.status.dirty', '脏房', 'STRING', '房态-脏房显示名称', FALSE),
(NULL, 'room.status.maintenance', '维修', 'STRING', '房态-维修显示名称', FALSE),
(NULL, 'room.status.out_of_order', 'OOO', 'STRING', '房态-停用显示名称', FALSE),
(NULL, 'room.status.reserved', '预留', 'STRING', '房态-预留显示名称', FALSE),

-- 预订配置
(NULL, 'reservation.auto_cancel_hours', '24', 'NUMBER', '预订自动取消时间（小时）', FALSE),
(NULL, 'reservation.no_show_grace_hours', '2', 'NUMBER', 'No-Show宽限时间（小时）', FALSE),
(NULL, 'reservation.max_advance_days', '365', 'NUMBER', '最大提前预订天数', FALSE),

-- 账务配置
(NULL, 'finance.currency', 'CNY', 'STRING', '货币类型', FALSE),
(NULL, 'finance.decimal_places', '2', 'NUMBER', '金额小数位数', FALSE),
(NULL, 'finance.deposit_min_amount', '100', 'NUMBER', '最低押金金额', FALSE),

-- 公安上传配置
(NULL, 'police.upload_enabled', 'true', 'BOOLEAN', '是否启用公安上传', FALSE),
(NULL, 'police.retry_max_count', '5', 'NUMBER', '最大重试次数', FALSE),
(NULL, 'police.retry_interval_seconds', '60', 'NUMBER', '重试间隔（秒）', FALSE),

-- 集成配置
(NULL, 'integration.outbox_batch_size', '100', 'NUMBER', '发件箱批次大小', FALSE),
(NULL, 'integration.outbox_retry_max_count', '5', 'NUMBER', '发件箱最大重试次数', FALSE),
(NULL, 'integration.outbox_retry_interval_seconds', '60', 'NUMBER', '发件箱重试间隔（秒）', FALSE),
(NULL, 'integration.inbound_enabled', 'false', 'BOOLEAN', '是否启用入站订单', FALSE),

-- 报表配置
(NULL, 'report.occupancy_rate_precision', '2', 'NUMBER', '入住率小数位数', FALSE),
(NULL, 'report.adr_precision', '2', 'NUMBER', 'ADR小数位数', FALSE),
(NULL, 'report.revpar_precision', '2', 'NUMBER', 'RevPAR小数位数', FALSE),

-- 安全配置
(NULL, 'security.password_min_length', '8', 'NUMBER', '密码最小长度', FALSE),
(NULL, 'security.password_max_age_days', '90', 'NUMBER', '密码有效期（天）', FALSE),
(NULL, 'security.login_max_attempts', '5', 'NUMBER', '最大登录尝试次数', FALSE),
(NULL, 'security.lockout_duration_minutes', '30', 'NUMBER', '账户锁定时间（分钟）', FALSE),
(NULL, 'security.session_timeout_minutes', '480', 'NUMBER', '会话超时时间（分钟）', FALSE),

-- 性能配置
(NULL, 'performance.room_status_load_timeout_seconds', '3', 'NUMBER', '房态看板加载超时（秒）', FALSE),
(NULL, 'performance.operation_timeout_seconds', '1', 'NUMBER', '前台操作超时（秒）', FALSE),
(NULL, 'performance.night_audit_timeout_seconds', '60', 'NUMBER', '夜审超时（秒）', FALSE),
(NULL, 'performance.max_concurrent_users', '500', 'NUMBER', '最大并发用户数', FALSE);
```

### 3.2 酒店级参数配置数据示例

```sql
INSERT INTO sys_config (hotel_id, config_key, config_value, config_type, description, is_system) VALUES
(1, 'hotel.check_in_time', '15:00', 'STRING', '入住时间', FALSE),
(1, 'hotel.check_out_time', '13:00', 'STRING', '离店时间', FALSE),
(1, 'hotel.night_audit_time', '03:00', 'STRING', '夜审时间', FALSE),
(1, 'finance.deposit_min_amount', '200', 'NUMBER', '最低押金', FALSE),
(1, 'police.upload_enabled', 'true', 'BOOLEAN', '公安上传', FALSE);
```

---

## 4. 修改表脚本（预留）

### 4.1 添加字段示例
```sql
ALTER TABLE hotel ADD COLUMN email VARCHAR(100);
COMMENT ON COLUMN hotel.email IS '酒店邮箱';

ALTER TABLE room ADD COLUMN area DECIMAL(8,2);
COMMENT ON COLUMN room.area IS '房间面积（平方米）';
```

### 4.2 修改字段示例
```sql
ALTER TABLE hotel ALTER COLUMN name TYPE VARCHAR(200);
ALTER TABLE room ALTER COLUMN status SET DEFAULT 'AVAILABLE';
```

### 4.3 添加索引示例
```sql
CREATE INDEX idx_reservation_guest_name ON reservation(hotel_id, guest_name) WHERE deleted = FALSE;
```

---

## 5. 数据初始化脚本

### 5.1 初始化酒店数据
```sql
INSERT INTO hotel (name, address, phone, timezone, status) VALUES
('默认酒店', '待填写', '待填写', 'Asia/Shanghai', 'ACTIVE');
```

### 5.2 初始化管理员账号
```sql
INSERT INTO sys_account (hotel_id, username, password, real_name, role, status) VALUES
(1, 'admin', 'BCrypt加密后的密码', '系统管理员', 'ADMIN', 'ACTIVE');
```

### 5.3 初始化房型数据
```sql
INSERT INTO room_type (hotel_id, name, code, bed_type, max_guests, base_price, description, status) VALUES
(1, '标准单人间', 'STD_S', '单人床', 1, 288.00, '标准单人客房', 'ACTIVE'),
(1, '标准双人间', 'STD_D', '双人床', 2, 388.00, '标准双人客房', 'ACTIVE'),
(1, '豪华大床房', 'DLX_K', '大床', 2, 488.00, '豪华大床客房', 'ACTIVE'),
(1, '商务套房', 'BIZ_S', '大床', 2, 688.00, '商务套房', 'ACTIVE'),
(1, '总统套房', 'PRE_S', '大床', 2, 1288.00, '总统套房', 'ACTIVE');
```

### 5.4 初始化楼层数据
```sql
INSERT INTO hotel_floor (hotel_id, floor_no, name, status) VALUES
(1, 1, '1楼', 'ACTIVE'),
(1, 2, '2楼', 'ACTIVE'),
(1, 3, '3楼', 'ACTIVE'),
(1, 4, '4楼', 'ACTIVE'),
(1, 5, '5楼', 'ACTIVE');
```

### 5.5 初始化房间数据
```sql
INSERT INTO room (hotel_id, room_type_id, floor_id, room_no, status, description) VALUES
(1, 1, 1, '101', 'AVAILABLE', '标准单人间'),
(1, 1, 1, '102', 'AVAILABLE', '标准单人间'),
(1, 1, 1, '103', 'AVAILABLE', '标准单人间'),
(1, 2, 1, '104', 'AVAILABLE', '标准双人间'),
(1, 2, 1, '105', 'AVAILABLE', '标准双人间'),
(1, 2, 1, '106', 'AVAILABLE', '标准双人间'),
(1, 3, 1, '107', 'AVAILABLE', '豪华大床房'),
(1, 3, 1, '108', 'AVAILABLE', '豪华大床房'),
(1, 4, 1, '109', 'AVAILABLE', '商务套房'),
(1, 4, 1, '110', 'AVAILABLE', '商务套房'),
(1, 1, 2, '201', 'AVAILABLE', '标准单人间'),
(1, 1, 2, '202', 'AVAILABLE', '标准单人间'),
(1, 1, 2, '203', 'AVAILABLE', '标准单人间'),
(1, 2, 2, '204', 'AVAILABLE', '标准双人间'),
(1, 2, 2, '205', 'AVAILABLE', '标准双人间'),
(1, 2, 2, '206', 'AVAILABLE', '标准双人间'),
(1, 3, 2, '207', 'AVAILABLE', '豪华大床房'),
(1, 3, 2, '208', 'AVAILABLE', '豪华大床房'),
(1, 4, 2, '209', 'AVAILABLE', '商务套房'),
(1, 4, 2, '210', 'AVAILABLE', '商务套房'),
(1, 1, 3, '301', 'AVAILABLE', '标准单人间'),
(1, 1, 3, '302', 'AVAILABLE', '标准单人间'),
(1, 1, 3, '303', 'AVAILABLE', '标准单人间'),
(1, 2, 3, '304', 'AVAILABLE', '标准双人间'),
(1, 2, 3, '305', 'AVAILABLE', '标准双人间'),
(1, 2, 3, '306', 'AVAILABLE', '标准双人间'),
(1, 3, 3, '307', 'AVAILABLE', '豪华大床房'),
(1, 3, 3, '308', 'AVAILABLE', '豪华大床房'),
(1, 4, 3, '309', 'AVAILABLE', '商务套房'),
(1, 4, 3, '310', 'AVAILABLE', '商务套房'),
(1, 1, 4, '401', 'AVAILABLE', '标准单人间'),
(1, 1, 4, '402', 'AVAILABLE', '标准单人间'),
(1, 1, 4, '403', 'AVAILABLE', '标准单人间'),
(1, 2, 4, '404', 'AVAILABLE', '标准双人间'),
(1, 2, 4, '405', 'AVAILABLE', '标准双人间'),
(1, 2, 4, '406', 'AVAILABLE', '标准双人间'),
(1, 3, 4, '407', 'AVAILABLE', '豪华大床房'),
(1, 3, 4, '408', 'AVAILABLE', '豪华大床房'),
(1, 4, 4, '409', 'AVAILABLE', '商务套房'),
(1, 4, 4, '410', 'AVAILABLE', '商务套房'),
(1, 1, 5, '501', 'AVAILABLE', '标准单人间'),
(1, 1, 5, '502', 'AVAILABLE', '标准单人间'),
(1, 1, 5, '503', 'AVAILABLE', '标准单人间'),
(1, 2, 5, '504', 'AVAILABLE', '标准双人间'),
(1, 2, 5, '505', 'AVAILABLE', '标准双人间'),
(1, 2, 5, '506', 'AVAILABLE', '标准双人间'),
(1, 3, 5, '507', 'AVAILABLE', '豪华大床房'),
(1, 3, 5, '508', 'AVAILABLE', '豪华大床房'),
(1, 5, 5, '509', 'AVAILABLE', '总统套房'),
(1, 5, 5, '510', 'AVAILABLE', '总统套房');
```

---

## 6. Flyway 迁移脚本命名规范

```
V1__init_schema.sql          # 初始建表
V2__create_indexes.sql       # 创建索引
V3__init_system_config.sql   # 初始化系统配置
V4__init_base_data.sql       # 初始化基础数据
V5__add_xxx_field.sql        # 添加字段
V6__modify_xxx_table.sql     # 修改表结构
```

---

## 7. 注意事项

1. **金额字段**：统一使用 DECIMAL(10,2)，禁止使用浮点数
2. **时间字段**：统一使用 TIMESTAMP 类型，默认值为 CURRENT_TIMESTAMP
3. **逻辑删除**：使用 deleted BOOLEAN DEFAULT FALSE 字段
4. **敏感数据**：身份证号、渠道密钥等使用 AES-GCM 加密存储
5. **唯一索引**：房间在住状态使用部分唯一索引防超卖
6. **外键约束**：所有外键字段都建立索引
7. **字符集**：使用 UTF-8 编码
8. **时区**：统一使用 Asia/Shanghai
9. **注释规范**：所有表和字段都必须添加COMMENT注释

---



---

## 7. RBAC权限管理表

### 7.1 角色表 (sys_role)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGSERIAL | 主键ID |
| hotel_id | BIGINT | 酒店ID |
| role_name | VARCHAR(50) | 角色名称 |
| role_code | VARCHAR(50) | 角色编码 |
| description | VARCHAR(200) | 角色描述 |
| sort_order | INTEGER | 排序号 |
| status | VARCHAR(20) | 状态：ACTIVE/INACTIVE |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除 |
| version | INTEGER | 乐观锁版本号 |

```sql
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort_order INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_sys_role_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_sys_role_code UNIQUE (hotel_id, role_code)
);

COMMENT ON TABLE sys_role IS '角色表';
CREATE INDEX idx_sys_role_hotel_id ON sys_role(hotel_id) WHERE deleted = FALSE;
```

### 7.2 权限表 (sys_permission)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGSERIAL | 主键ID |
| permission_name | VARCHAR(100) | 权限名称 |
| permission_code | VARCHAR(100) | 权限编码 |
| resource_type | VARCHAR(20) | 资源类型：MENU/BUTTON/API |
| resource_path | VARCHAR(200) | 资源路径 |
| parent_id | BIGINT | 父权限ID |
| sort_order | INTEGER | 排序号 |
| icon | VARCHAR(50) | 菜单图标 |
| is_visible | BOOLEAN | 是否可见 |
| status | VARCHAR(20) | 状态 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | 逻辑删除 |
| version | INTEGER | 乐观锁版本号 |

```sql
CREATE TABLE sys_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    resource_path VARCHAR(200),
    parent_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    icon VARCHAR(50),
    is_visible BOOLEAN DEFAULT TRUE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_sys_permission_parent FOREIGN KEY (parent_id) REFERENCES sys_permission(id),
    CONSTRAINT uk_sys_permission_code UNIQUE (permission_code)
);

COMMENT ON TABLE sys_permission IS '权限表';
CREATE INDEX idx_sys_permission_parent_id ON sys_permission(parent_id) WHERE deleted = FALSE;
```

### 7.3 用户角色关联表 (sys_user_role)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGSERIAL | 主键ID |
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |
| created_at | TIMESTAMP | 创建时间 |

```sql
CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_account(id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';
CREATE INDEX idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role(role_id);
```

### 7.4 角色权限关联表 (sys_role_permission)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGSERIAL | 主键ID |
| role_id | BIGINT | 角色ID |
| permission_id | BIGINT | 权限ID |
| created_at | TIMESTAMP | 创建时间 |

```sql
CREATE TABLE sys_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
    CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id),
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id)
);

COMMENT ON TABLE sys_role_permission IS '角色权限关联表';
CREATE INDEX idx_sys_role_permission_role_id ON sys_role_permission(role_id);
CREATE INDEX idx_sys_role_permission_permission_id ON sys_role_permission(permission_id);
```

### 7.5 默认角色数据

```sql
INSERT INTO sys_role (id, hotel_id, role_name, role_code, description, sort_order, status) VALUES
    (1, 1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, 'ACTIVE'),
    (2, 1, '酒店管理员', 'HOTEL_ADMIN', '酒店管理员，管理酒店日常运营', 2, 'ACTIVE'),
    (3, 1, '店长', 'MANAGER', '店长，管理前台和房务', 3, 'ACTIVE'),
    (4, 1, '前台接待', 'RECEPTIONIST', '前台接待，处理预订和入住', 4, 'ACTIVE'),
    (5, 1, '财务人员', 'FINANCE', '财务人员，管理账务和报表', 5, 'ACTIVE');
```



---

### V1_14_0__add_price_plan_to_team_reservation.sql

**日期**：2026-08-14  
**描述**：为团队预订表添加房价码字段

```sql
-- 为团队预订表添加房价码ID字段
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'team_reservation' AND column_name = 'price_plan_id') THEN
        ALTER TABLE team_reservation ADD COLUMN price_plan_id BIGINT;
        COMMENT ON COLUMN team_reservation.price_plan_id IS '房价码ID';
    END IF;
END $$;
```

**说明**：支持团队预订时选择房价码，按房价码标准计算费用
### V1_15_0__night_audit_step.sql

**日期**：2026-08-15  
**描述**：创建夜审步骤表

```sql
CREATE TABLE IF NOT EXISTS night_audit_step (
    id BIGSERIAL PRIMARY KEY,
    night_audit_id BIGINT NOT NULL,
    step_name VARCHAR(50) NOT NULL,
    step_order INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message VARCHAR(1000),
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_night_audit_step_audit FOREIGN KEY (night_audit_id) REFERENCES night_audit(id),
    CONSTRAINT uk_night_audit_step_order UNIQUE (night_audit_id, step_order)
);
```

**说明**：记录夜审每个步骤的执行状态，支持失败重试

### V1_16_0__night_audit_enhancement.sql

**日期**：2026-08-15  
**描述**：夜审增强字段

```sql
ALTER TABLE stay ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE stay ADD COLUMN IF NOT EXISTS locked_by_audit_id BIGINT;
ALTER TABLE folio ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE folio ADD COLUMN IF NOT EXISTS locked_by_audit_id BIGINT;
ALTER TABLE hotel ADD COLUMN IF NOT EXISTS audit_time TIME DEFAULT '04:00:00';
ALTER TABLE hotel ADD COLUMN IF NOT EXISTS auto_audit_enabled BOOLEAN DEFAULT TRUE;
```

**说明**：支持数据锁定和夜审配置

### V1_18_0__hotel_config_table.sql

**日期**：2026-08-15  
**描述**：酒店配置表

```sql
CREATE TABLE IF NOT EXISTS hotel_config (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value VARCHAR(500),
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hotel_config_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT uk_hotel_config_key UNIQUE (hotel_id, config_key)
);
```

**说明**：存储酒店配置参数，如超时离店规则

### V1_22_0__night_audit_archive.sql

**日期**：2026-08-15  
**描述**：夜审归档表

```sql
CREATE TABLE IF NOT EXISTS night_audit_archive (
    id BIGSERIAL PRIMARY KEY,
    original_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    audit_date DATE NOT NULL,
    status VARCHAR(30),
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
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    archived_by VARCHAR(50)
);
```

**说明**：归档历史夜审数据，优化查询性能

### V1_24_0__add_hotel_code.sql

**日期**：2026-08-17  
**描述**：酒店添加标识码字段

```sql
ALTER TABLE hotel ADD COLUMN IF NOT EXISTS hotel_code VARCHAR(50);
UPDATE hotel SET hotel_code = 'H' || LPAD(id::text, 3, '0') WHERE hotel_code IS NULL;
ALTER TABLE hotel ADD CONSTRAINT uk_hotel_code UNIQUE (hotel_code);
ALTER TABLE hotel ALTER COLUMN hotel_code SET NOT NULL;
COMMENT ON COLUMN hotel.hotel_code IS '酒店标识码，用于登录时区分不同酒店';
```

**说明**：支持多酒店系统，通过标识码区分不同酒店

---

**文档维护人**：系统管理员  
**最后更新**：2026-08-20
---

## 2. 索引设计

### 2.1 主键索引
所有表都使用自增主键：
`sql
id BIGSERIAL PRIMARY KEY
`

### 2.2 外键索引
| 表名 | 字段 | 索引名 | 说明 |
|------|------|--------|------|
| hotel_floor | hotel_id | fk_hotel_floor_hotel | 楼层所属酒店 |
| room_type | hotel_id | fk_room_type_hotel | 房型所属酒店 |
| room | hotel_id | fk_room_hotel | 房间所属酒店 |
| room | room_type_id | fk_room_room_type | 房间房型 |
| room | floor_id | fk_room_floor | 房间楼层 |
| reservation | hotel_id | fk_reservation_hotel | 预订所属酒店 |
| stay | hotel_id | fk_stay_hotel | 入住所属酒店 |
| stay | room_id | fk_stay_room | 入住房间 |
| folio | hotel_id | fk_folio_hotel | 账务所属酒店 |
| folio | stay_id | fk_folio_stay | 账务入住单 |

### 2.3 唯一索引
| 表名 | 字段 | 索引名 | 说明 |
|------|------|--------|------|
| room_type | hotel_id, code | uk_room_type_code | 房型编码酒店内唯一 |
| room | hotel_id, room_no | uk_room_no | 房间号酒店内唯一 |
| reservation | reservation_no | uk_reservation_no | 预订号唯一 |
| stay | stay_no | uk_stay_no | 入住单号唯一 |
| folio | folio_no | uk_folio_no | 账务单号唯一 |
| night_audit | hotel_id, audit_date | uk_night_audit_date | 夜审日期唯一 |

### 2.4 普通索引
| 表名 | 字段 | 索引名 | 说明 |
|------|------|--------|------|
| room | hotel_id, status | idx_room_hotel_status | 按酒店查房间状态 |
| reservation | hotel_id, status | idx_reservation_hotel_status | 按酒店查预订状态 |
| reservation | hotel_id, check_in_date | idx_reservation_check_in | 按入住日期查预订 |
| stay | hotel_id, status | idx_stay_hotel_status | 按酒店查入住状态 |
| stay | hotel_id, check_in_time | idx_stay_check_in | 按入住时间查入住 |
| folio | hotel_id, status | idx_folio_hotel_status | 按酒店查账务状态 |
| fin_transaction | folio_id | idx_transaction_folio | 按账务单查交易 |
| operation_log | hotel_id, created_at | idx_operation_log_time | 按时间查操作日志 |

### 2.5 复合索引
| 表名 | 字段 | 索引名 | 说明 |
|------|------|--------|------|
| room_price | hotel_id, room_type_id, price_date | idx_price_date | 按日期查房价 |
| stay | hotel_id, room_id, status | idx_stay_room_status | 按房间查在住状态 |
| reservation | hotel_id, room_type_id, check_in_date, check_out_date | idx_reservation_availability | 查房间可用性 |

### 2.6 索引使用原则
1. **选择性原则**：选择区分度高的字段建索引
2. **最左前缀**：复合索引遵循最左前缀原则
3. **覆盖索引**：尽量使用覆盖索引减少回表
4. **避免过度**：不要创建过多索引，影响写入性能

---

**文档维护人**：系统管理员  
**最后更新**：2026-08-20

