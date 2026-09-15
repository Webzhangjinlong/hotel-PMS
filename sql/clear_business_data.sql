-- ============================================
-- PMS 酒店管理系统 - 业务数据清理脚本
-- 执行前请确保已备份数据库！
-- 执行顺序：先删子表，再删父表（避免外键约束）
-- ============================================

-- 临时禁用外键约束检查（PostgreSQL）
SET session_replication_role = 'replica';

-- ============================================
-- 1. 交易流水和账务相关（最底层数据）
-- ============================================
DELETE FROM team_folio_payment;      -- 团队账务付款记录
DELETE FROM fin_transaction;         -- 财务交易流水
DELETE FROM team_folio;              -- 团队账务单
DELETE FROM folio;                   -- 散客账务单
DELETE FROM reservation_prepayment;  -- 预付记录

-- ============================================
-- 2. 入住和预订相关
-- ============================================
DELETE FROM team_reservation_room;   -- 团队预订房间明细
DELETE FROM team_reservation;        -- 团队预订
DELETE FROM stay;                    -- 入住单
DELETE FROM reservation;             -- 散客预订

-- ============================================
-- 3. 客人信息
-- ============================================
DELETE FROM guest;                   -- 客人档案

-- ============================================
-- 4. 夜审相关
-- ============================================
DELETE FROM night_audit_step;        -- 夜审步骤
DELETE FROM night_audit_archive;     -- 夜审归档
DELETE FROM night_audit;             -- 夜审记录

-- ============================================
-- 5. 交班相关
-- ============================================
DELETE FROM sys_shift_message;       -- 交班消息
DELETE FROM sys_shift;               -- 交班记录

-- ============================================
-- 6. 操作日志
-- ============================================
DELETE FROM operation_log;           -- 操作日志

-- ============================================
-- 7. 房价相关
-- ============================================
DELETE FROM room_price_rule;         -- 房价规则
DELETE FROM room_price;              -- 房价日历
DELETE FROM room_price_plan_detail;  -- 房价码明细
DELETE FROM room_price_plan;         -- 房价码

-- ============================================
-- 8. 房间和楼层（基础数据，谨慎删除）
-- ============================================
DELETE FROM room;                    -- 房间
DELETE FROM hotel_floor;             -- 楼层

-- ============================================
-- 9. 房型（基础数据，谨慎删除）
-- ============================================
DELETE FROM room_type;               -- 房型

-- ============================================
-- 10. 系统配置
-- ============================================
DELETE FROM hotel_config;            -- 酒店配置

-- ============================================
-- 11. 权限和用户（系统数据，谨慎删除）
-- ============================================
DELETE FROM sys_user_role;           -- 用户角色关联
DELETE FROM sys_role_permission;     -- 角色权限关联
DELETE FROM sys_permission;          -- 权限
DELETE FROM sys_role;                -- 角色
DELETE FROM sys_account;             -- 系统账号

-- ============================================
-- 12. 挂账公司
-- ============================================
DELETE FROM credit_company;          -- 挂账公司

-- ============================================
-- 13. 酒店（主表，最后删除）
-- ============================================
DELETE FROM hotel;                   -- 酒店

-- 恢复外键约束检查
SET session_replication_role = 'origin';

-- 重置序列（自增ID）
ALTER SEQUENCE IF EXISTS hotel_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS hotel_floor_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS room_type_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS room_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS room_price_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS guest_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS reservation_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS stay_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS folio_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS fin_transaction_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS team_reservation_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS team_folio_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS night_audit_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS sys_account_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS sys_role_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS sys_permission_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS credit_company_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS operation_log_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS reservation_prepayment_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS hotel_config_id_seq RESTART WITH 1;

SELECT '数据清理完成！' AS result;
