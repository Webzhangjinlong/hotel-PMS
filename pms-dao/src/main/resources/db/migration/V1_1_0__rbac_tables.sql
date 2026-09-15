-- RBAC 多角色多权限系统数据库迁移脚本
-- 版本：V1_1_0
-- 创建时间：2026-08-08

-- ========== 角色表 ==========
CREATE TABLE IF NOT EXISTS sys_role (
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
COMMENT ON COLUMN sys_role.id IS '主键ID';
COMMENT ON COLUMN sys_role.hotel_id IS '酒店ID';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_code IS '角色编码';
COMMENT ON COLUMN sys_role.description IS '角色描述';
COMMENT ON COLUMN sys_role.sort_order IS '排序号';
COMMENT ON COLUMN sys_role.status IS '状态：ACTIVE-启用/INACTIVE-停用';
COMMENT ON COLUMN sys_role.created_at IS '创建时间';
COMMENT ON COLUMN sys_role.updated_at IS '更新时间';
COMMENT ON COLUMN sys_role.deleted IS '逻辑删除';
COMMENT ON COLUMN sys_role.version IS '乐观锁版本号';

CREATE INDEX IF NOT EXISTS idx_sys_role_hotel_id ON sys_role(hotel_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_sys_role_status ON sys_role(hotel_id, status) WHERE deleted = FALSE;

-- ========== 权限表 ==========
CREATE TABLE IF NOT EXISTS sys_permission (
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
COMMENT ON COLUMN sys_permission.id IS '主键ID';
COMMENT ON COLUMN sys_permission.permission_name IS '权限名称';
COMMENT ON COLUMN sys_permission.permission_code IS '权限编码，如：user:create、menu:dashboard';
COMMENT ON COLUMN sys_permission.resource_type IS '资源类型：MENU-菜单/BUTTON-按钮/API-接口';
COMMENT ON COLUMN sys_permission.resource_path IS '资源路径：菜单路径或API路径';
COMMENT ON COLUMN sys_permission.parent_id IS '父权限ID，用于菜单层级';
COMMENT ON COLUMN sys_permission.sort_order IS '排序号';
COMMENT ON COLUMN sys_permission.icon IS '菜单图标';
COMMENT ON COLUMN sys_permission.is_visible IS '是否可见：TRUE-可见/FALSE-隐藏';
COMMENT ON COLUMN sys_permission.status IS '状态：ACTIVE-启用/INACTIVE-停用';
COMMENT ON COLUMN sys_permission.created_at IS '创建时间';
COMMENT ON COLUMN sys_permission.updated_at IS '更新时间';
COMMENT ON COLUMN sys_permission.deleted IS '逻辑删除';
COMMENT ON COLUMN sys_permission.version IS '乐观锁版本号';

CREATE INDEX IF NOT EXISTS idx_sys_permission_parent_id ON sys_permission(parent_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_sys_permission_type ON sys_permission(resource_type) WHERE deleted = FALSE;

-- ========== 用户角色关联表 ==========
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_account(id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';
COMMENT ON COLUMN sys_user_role.id IS '主键ID';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';
COMMENT ON COLUMN sys_user_role.created_at IS '创建时间';

CREATE INDEX IF NOT EXISTS idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_id ON sys_user_role(role_id);

-- ========== 角色权限关联表 ==========
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
    CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id),
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id)
);

COMMENT ON TABLE sys_role_permission IS '角色权限关联表';
COMMENT ON COLUMN sys_role_permission.id IS '主键ID';
COMMENT ON COLUMN sys_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_permission.permission_id IS '权限ID';
COMMENT ON COLUMN sys_role_permission.created_at IS '创建时间';

CREATE INDEX IF NOT EXISTS idx_sys_role_permission_role_id ON sys_role_permission(role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_permission_id ON sys_role_permission(permission_id);

-- ========== 初始化数据 ==========

-- 插入默认角色
INSERT INTO sys_role (id, hotel_id, role_name, role_code, description, sort_order, status) VALUES
    (1, 1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, 'ACTIVE'),
    (2, 1, '酒店管理员', 'HOTEL_ADMIN', '酒店管理员，管理酒店日常运营', 2, 'ACTIVE'),
    (3, 1, '店长', 'MANAGER', '店长，管理前台和房务', 3, 'ACTIVE'),
    (4, 1, '前台接待', 'RECEPTIONIST', '前台接待，处理预订和入住', 4, 'ACTIVE'),
    (5, 1, '财务人员', 'FINANCE', '财务人员，管理账务和报表', 5, 'ACTIVE')
ON CONFLICT DO NOTHING;

-- 插入权限数据 - 菜单权限
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, resource_path, parent_id, sort_order, icon, is_visible) VALUES
    -- 一级菜单
    (1, '工作台', 'menu:dashboard', 'MENU', '/dashboard', NULL, 1, 'Odometer', TRUE),
    (2, '主数据', 'menu:master_data', 'MENU', NULL, NULL, 2, 'Files', TRUE),
    (3, '预订管理', 'menu:reservation', 'MENU', NULL, NULL, 3, 'Calendar', TRUE),
    (4, '前台操作', 'menu:front_desk', 'MENU', NULL, NULL, 4, 'User', TRUE),
    (5, '房务管理', 'menu:housekeeping', 'MENU', NULL, NULL, 5, 'Brush', TRUE),
    (6, '夜审管理', 'menu:night_audit', 'MENU', NULL, NULL, 6, 'Moon', TRUE),
    (7, '报表中心', 'menu:reports', 'MENU', NULL, NULL, 7, 'DataAnalysis', TRUE),
    (8, '系统管理', 'menu:system', 'MENU', NULL, NULL, 8, 'Setting', TRUE),
    
    -- 主数据子菜单
    (10, '酒店管理', 'menu:hotel', 'MENU', '/hotels', 2, 1, NULL, TRUE),
    (11, '楼层管理', 'menu:floor', 'MENU', '/floors', 2, 2, NULL, TRUE),
    (12, '房型管理', 'menu:room_type', 'MENU', '/room-types', 2, 3, NULL, TRUE),
    (13, '房间管理', 'menu:room', 'MENU', '/rooms', 2, 4, NULL, TRUE),
    
    -- 预订管理子菜单
    (20, '预订列表', 'menu:reservation_list', 'MENU', '/reservations', 3, 1, NULL, TRUE),
    (21, '今日抵店', 'menu:reservation_today', 'MENU', '/reservations/today', 3, 2, NULL, TRUE),
    
    -- 前台操作子菜单
    (30, '入住管理', 'menu:stay', 'MENU', '/stays', 4, 1, NULL, TRUE),
    (31, '房态看板', 'menu:room_board', 'MENU', '/room-board', 4, 2, NULL, TRUE),
    (32, '账务管理', 'menu:folio', 'MENU', '/folios', 4, 3, NULL, TRUE),
    
    -- 房务管理子菜单
    (40, '清洁任务', 'menu:clean', 'MENU', '/housekeeping/clean', 5, 1, NULL, TRUE),
    (41, '维修管理', 'menu:maintenance', 'MENU', '/housekeeping/maintenance', 5, 2, NULL, TRUE),
    
    -- 夜审管理子菜单
    (50, '执行夜审', 'menu:night_audit_exec', 'MENU', '/night-audit', 6, 1, NULL, TRUE),
    (51, '夜审记录', 'menu:night_audit_record', 'MENU', '/night-audit/records', 6, 2, NULL, TRUE),
    
    -- 报表中心子菜单
    (60, '营业日报', 'menu:report_daily', 'MENU', '/reports/daily', 7, 1, NULL, TRUE),
    (61, '经营指标', 'menu:report_metrics', 'MENU', '/reports/metrics', 7, 2, NULL, TRUE),
    
    -- 系统管理子菜单
    (70, '账号管理', 'menu:account', 'MENU', '/accounts', 8, 1, NULL, TRUE),
    (71, '角色管理', 'menu:role', 'MENU', '/roles', 8, 2, NULL, TRUE),
    (72, '权限管理', 'menu:permission', 'MENU', '/permissions', 8, 3, NULL, TRUE),
    (73, '交接班', 'menu:shift', 'MENU', '/shifts', 8, 4, NULL, TRUE),
    (74, '操作日志', 'menu:operation_log', 'MENU', '/operation-logs', 8, 5, NULL, TRUE)
ON CONFLICT DO NOTHING;

-- 插入权限数据 - 按钮权限
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, resource_path, parent_id, sort_order, is_visible) VALUES
    -- 酒店管理按钮
    (100, '创建酒店', 'hotel:create', 'BUTTON', NULL, 10, 1, TRUE),
    (101, '编辑酒店', 'hotel:edit', 'BUTTON', NULL, 10, 2, TRUE),
    (102, '删除酒店', 'hotel:delete', 'BUTTON', NULL, 10, 3, TRUE),
    
    -- 房间管理按钮
    (110, '创建房间', 'room:create', 'BUTTON', NULL, 13, 1, TRUE),
    (111, '编辑房间', 'room:edit', 'BUTTON', NULL, 13, 2, TRUE),
    (112, '删除房间', 'room:delete', 'BUTTON', NULL, 13, 3, TRUE),
    (113, '批量房态', 'room:batch_status', 'BUTTON', NULL, 13, 4, TRUE),
    
    -- 预订管理按钮
    (120, '创建预订', 'reservation:create', 'BUTTON', NULL, 20, 1, TRUE),
    (121, '编辑预订', 'reservation:edit', 'BUTTON', NULL, 20, 2, TRUE),
    (122, '取消预订', 'reservation:cancel', 'BUTTON', NULL, 20, 3, TRUE),
    (123, '预订入住', 'reservation:checkin', 'BUTTON', NULL, 20, 4, TRUE),
    
    -- 入住管理按钮
    (130, '散客入住', 'stay:walk_in', 'BUTTON', NULL, 30, 1, TRUE),
    (131, '换房', 'stay:change_room', 'BUTTON', NULL, 30, 2, TRUE),
    (132, '续住', 'stay:extend', 'BUTTON', NULL, 30, 3, TRUE),
    (133, '退房', 'stay:checkout', 'BUTTON', NULL, 30, 4, TRUE),
    
    -- 账务管理按钮
    (140, '收取押金', 'folio:deposit', 'BUTTON', NULL, 32, 1, TRUE),
    (141, '添加杂费', 'folio:extra', 'BUTTON', NULL, 32, 2, TRUE),
    (142, '结账', 'folio:settle', 'BUTTON', NULL, 32, 3, TRUE),
    (143, '退款', 'folio:refund', 'BUTTON', NULL, 32, 4, TRUE),
    
    -- 夜审管理按钮
    (150, '执行夜审', 'night_audit:execute', 'BUTTON', NULL, 50, 1, TRUE),
    
    -- 系统管理按钮
    (160, '创建账号', 'account:create', 'BUTTON', NULL, 70, 1, TRUE),
    (161, '编辑账号', 'account:edit', 'BUTTON', NULL, 70, 2, TRUE),
    (162, '删除账号', 'account:delete', 'BUTTON', NULL, 70, 3, TRUE),
    (163, '重置密码', 'account:reset_password', 'BUTTON', NULL, 70, 4, TRUE),
    (164, '创建角色', 'role:create', 'BUTTON', NULL, 71, 1, TRUE),
    (165, '编辑角色', 'role:edit', 'BUTTON', NULL, 71, 2, TRUE),
    (166, '删除角色', 'role:delete', 'BUTTON', NULL, 71, 3, TRUE),
    (167, '分配权限', 'role:assign_permission', 'BUTTON', NULL, 71, 4, TRUE)
ON CONFLICT DO NOTHING;

-- ========== 为默认角色分配权限 ==========

-- 超级管理员 - 所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE deleted = FALSE
ON CONFLICT DO NOTHING;

-- 酒店管理员 - 除系统管理外的所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission 
WHERE deleted = FALSE 
  AND permission_code NOT LIKE 'menu:system'
  AND permission_code NOT LIKE 'account:%'
  AND permission_code NOT LIKE 'role:%'
  AND permission_code NOT LIKE 'permission:%'
ON CONFLICT DO NOTHING;

-- 店长 - 前台操作、房务、报表权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission 
WHERE deleted = FALSE 
  AND (
    permission_code IN ('menu:dashboard', 'menu:front_desk', 'menu:housekeeping', 'menu:reports')
    OR permission_code LIKE 'menu:stay%'
    OR permission_code LIKE 'menu:room_board%'
    OR permission_code LIKE 'menu:folio%'
    OR permission_code LIKE 'menu:clean%'
    OR permission_code LIKE 'menu:maintenance%'
    OR permission_code LIKE 'menu:report%'
    OR permission_code LIKE 'stay:%'
    OR permission_code LIKE 'folio:%'
    OR permission_code LIKE 'room:batch_status'
  )
ON CONFLICT DO NOTHING;

-- 前台接待 - 预订、入住权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 4, id FROM sys_permission 
WHERE deleted = FALSE 
  AND (
    permission_code IN ('menu:dashboard', 'menu:reservation', 'menu:front_desk')
    OR permission_code LIKE 'menu:reservation%'
    OR permission_code LIKE 'menu:stay%'
    OR permission_code LIKE 'menu:room_board%'
    OR permission_code LIKE 'reservation:%'
    OR permission_code LIKE 'stay:walk_in'
    OR permission_code LIKE 'stay:checkout'
  )
ON CONFLICT DO NOTHING;

-- 财务人员 - 账务、报表权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 5, id FROM sys_permission 
WHERE deleted = FALSE 
  AND (
    permission_code IN ('menu:dashboard', 'menu:reports')
    OR permission_code LIKE 'menu:folio%'
    OR permission_code LIKE 'menu:report%'
    OR permission_code LIKE 'folio:%'
  )
ON CONFLICT DO NOTHING;

-- 为管理员账号分配超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON CONFLICT DO NOTHING;