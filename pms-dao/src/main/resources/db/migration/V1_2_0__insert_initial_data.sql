-- V1_2_0__insert_initial_data.sql
-- 插入初始楼层和房间数据

-- 插入默认楼层
INSERT INTO hotel_floor (id, hotel_id, floor_no, name, status)
VALUES 
    (1, 1, 1, '1楼', 'ACTIVE'),
    (2, 1, 2, '2楼', 'ACTIVE'),
    (3, 1, 3, '3楼', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- 插入默认房间
INSERT INTO room (id, hotel_id, room_type_id, floor_id, room_no, status, description)
VALUES 
    (1, 1, 1, 1, '101', 'AVAILABLE', '标准单人间-1楼'),
    (2, 1, 1, 1, '102', 'AVAILABLE', '标准单人间-1楼'),
    (3, 1, 2, 1, '103', 'AVAILABLE', '标准双人间-1楼'),
    (4, 1, 2, 2, '201', 'AVAILABLE', '标准双人间-2楼'),
    (5, 1, 3, 2, '202', 'OCCUPIED', '豪华大床房-2楼'),
    (6, 1, 3, 3, '301', 'AVAILABLE', '豪华大床房-3楼'),
    (7, 1, 4, 3, '302', 'DIRTY', '商务套房-3楼'),
    (8, 1, 4, 3, '303', 'MAINTENANCE', '商务套房-3楼')
ON CONFLICT DO NOTHING;