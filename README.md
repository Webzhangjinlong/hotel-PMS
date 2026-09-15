# PMS 酒店管理系统

## 📋 项目简介

PMS（Property Management System）是一套完整的酒店物业管理系统，涵盖预订管理、入住管理、房态管理、账务管理、夜审管理等核心业务模块，帮助酒店实现数字化、智能化管理。

---

## 🛠️ 技术栈

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.5 | 基础框架 |
| MyBatis-Plus | 3.5.7 | ORM框架 |
| PostgreSQL | 42.7.4 | 数据库 |
| Spring Security | - | 安全框架 |
| JWT | - | 身份认证 |
| Swagger/OpenAPI | - | API文档 |
| Lombok | - | 代码简化 |
| Flyway | - | 数据库迁移 |

### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | 3.5.12 | 前端框架 |
| Element Plus | 2.8.7 | UI组件库 |
| Vite | 5.4.10 | 构建工具 |
| Vue Router | 4.4.5 | 路由管理 |
| Pinia | - | 状态管理 |
| Axios | 1.7.7 | HTTP请求 |

---

## 📁 项目结构

```
hotel-PMS/
├── pms-api/                    # API接口层
│   └── src/main/java/
│       └── com/hotel/pms/api/
│           ├── controller/     # 控制器
│           ├── config/         # 配置类
│           ├── interceptor/    # 拦截器
│           └── aspect/         # AOP切面
│
├── pms-common/                 # 公共模块
│   └── src/main/java/
│       └── com/hotel/pms/common/
│           ├── dto/            # 数据传输对象
│           ├── annotation/     # 自定义注解
│           ├── constant/       # 常量定义
│           ├── exception/      # 异常处理
│           └── result/         # 返回结果
│
├── pms-dao/                    # 数据访问层
│   └── src/main/java/
│       └── com/hotel/pms/dao/
│           ├── entity/         # 实体类
│           ├── mapper/         # MyBatis Mapper
│           └── resources/db/migration/  # 数据库迁移脚本
│
├── pms-service/                # 业务逻辑层
│   └── src/main/java/
│       └── com/hotel/pms/service/
│           ├── auth/           # 认证授权
│           ├── reservation/    # 预订服务
│           ├── stay/           # 入住服务
│           ├── folio/          # 账务服务
│           ├── room/           # 房间服务
│           ├── price/          # 价格服务
│           ├── nightaudit/     # 夜审服务
│           ├── shift/          # 交班服务
│           └── system/         # 系统服务
│
├── pms-web/                    # 前端项目
│   └── src/
│       ├── api/                # API接口定义
│       ├── views/              # 页面组件
│       ├── router/             # 路由配置
│       ├── stores/             # 状态管理
│       ├── layouts/            # 布局组件
│       └── utils/              # 工具函数
│
└── sql/                        # SQL脚本
```

---

## 🗄️ 数据库设计

### 核心数据表

| 模块 | 表名 | 说明 |
|------|------|------|
| **基础数据** | hotel | 酒店信息 |
| | hotel_floor | 楼层信息 |
| | room_type | 房型信息 |
| | room | 房间信息 |
| | room_price | 房价信息 |
| | room_price_plan | 价格方案 |
| **预订管理** | reservation | 散客预订 |
| | team_reservation | 团队预订 |
| | team_reservation_room | 团队预订房间 |
| **入住管理** | guest | 客人信息 |
| | stay | 入住单 |
| **账务管理** | folio | 散客账单 |
| | team_folio | 团队账单 |
| | fin_transaction | 财务交易 |
| | credit_company | 挂账公司 |
| | reservation_prepayment | 预付款 |
| **系统管理** | sys_account | 系统账号 |
| | sys_role | 角色 |
| | sys_permission | 权限 |
| | sys_user_role | 用户角色 |
| | sys_role_permission | 角色权限 |
| | operation_log | 操作日志 |
| | sys_shift | 交班记录 |
| **夜审管理** | night_audit | 夜审记录 |
| | night_audit_step | 夜审步骤 |
| | night_audit_archive | 夜审归档 |

---

## 🎯 功能模块

### 1. 登录认证
- 用户名密码登录
- JWT Token认证
- 验证码校验
- 登录状态管理

### 2. 预订管理
- **散客预订**：创建、修改、取消、确认预订
- **团队预订**：团队预订管理、批量操作
- **预订来源**：直接预订、OTA渠道等
- **预订状态**：待确认、已确认、已取消、未到店

### 3. 入住管理
- **散客入住**：预订入住、直接入住
- **团队入住**：批量入住
- **退房操作**：散客退房、团队退房
- **换房操作**：房间调换

### 4. 房态管理
- **房态看板**：可视化展示房间状态
- **房间管理**：房间增删改查
- **房型管理**：房型配置
- **楼层管理**：楼层配置

### 5. 价格管理
- **房价管理**：按日期设置房价
- **价格方案**：创建价格方案和规则
- **批量调价**：按房型/日期范围批量调整
- **价格日历**：月历视图查看价格

### 6. 账务管理
- **散客账务**：收款、退款、冲账
- **团队账务**：团队统一结算、分账结算
- **交易记录**：查看交易明细
- **挂账管理**：挂账公司管理、挂账结算
- **预付款**：预订预付、预付转入住、预付退款

### 7. 交班管理
- **交班流程**：创建交班 → 提交 → 接收 → 确认
- **交班统计**：当班收入统计
- **消息通知**：交班消息推送

### 8. 夜审管理
- **夜审执行**：自动/手动执行夜审
- **夜审步骤**：房费计算、数据统计等
- **夜审归档**：历史数据归档
- **夜审配置**：夜审参数配置

### 9. 经营报表
- **营业日报**：每日营业数据汇总
- **经营指标**：入住率、ADR、RevPAR等

### 10. 系统设置
- **用户管理**：员工账号管理
- **角色管理**：角色创建和权限分配
- **权限管理**：菜单和按钮权限配置
- **操作日志**：系统操作记录查询
- **酒店配置**：酒店基础信息配置

---

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### 后端启动

```bash
# 1. 创建数据库
createdb hotel_pms

# 2. 执行数据库迁移
cd pms-dao
flyway migrate

# 3. 启动后端服务
cd pms-api
mvn spring-boot:run
```

### 前端启动

```bash
# 1. 安装依赖
cd pms-web
npm install

# 2. 启动开发服务器
npm run dev
```

### 访问地址
- 前端：http://localhost:5173
- 后端API：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html

---

## 📡 API接口

### 认证相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/auth/login` | POST | 用户登录 |
| `/api/v1/auth/logout` | POST | 退出登录 |
| `/api/v1/auth/current` | GET | 获取当前用户信息 |

### 预订管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/reservations` | GET | 查询预订列表 |
| `/api/v1/reservations` | POST | 创建预订 |
| `/api/v1/reservations/{id}` | PUT | 更新预订 |
| `/api/v1/reservations/{id}` | DELETE | 取消预订 |
| `/api/v1/team-reservations` | GET | 查询团队预订 |

### 入住管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/stays/walk-in` | POST | 散客直接入住 |
| `/api/v1/stays/check-in` | POST | 预订入住 |
| `/api/v1/stays/check-out` | POST | 退房 |

### 账务管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/folios/stay/{stayId}/payment` | POST | 收款 |
| `/api/v1/folios/{folioId}/refund` | POST | 退款 |
| `/api/v1/prepayments` | POST | 预订预付 |

### 系统管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/users` | GET | 查询用户列表 |
| `/api/v1/roles` | GET | 查询角色列表 |
| `/api/v1/operation-logs` | GET | 查询操作日志 |

---

## 🔐 权限管理

### 角色类型
| 角色 | 说明 | 权限范围 |
|------|------|----------|
| ADMIN | 系统管理员 | 所有权限 |
| FRONT_DESK | 前台 | 预订、入住、账务 |
| HOUSEKEEPING | 客房 | 房态管理 |
| FINANCE | 财务 | 账务、报表 |

### 权限控制
- **菜单权限**：控制左侧菜单显示
- **按钮权限**：控制页面按钮显示/隐藏
- **接口权限**：控制API访问

---

## 📝 操作日志

系统自动记录关键业务操作，包括：
- 登录登出
- 预订创建/修改/取消
- 入住/退房
- 收款/退款
- 交班操作
- 夜审执行
- 用户管理

查看路径：系统设置 → 操作日志

---

## 🔄 夜审流程

夜审是酒店每日必须执行的操作，主要步骤：
1. **数据校验**：检查当日数据完整性
2. **房费计算**：计算当日房费并入账
3. **数据统计**：统计入住率、收入等指标
4. **数据归档**：将历史数据归档

---

## 📊 经营指标

系统支持以下经营指标统计：
- **入住率（OCC）**：实际入住房间数 / 可售房间数
- **平均房价（ADR）**：客房收入 / 已售房间数
- **平均客房收益（RevPAR）**：客房收入 / 可售房间数
- **总收入**：客房收入 + 其他收入

---

## 🛡️ 数据安全

- **密码加密**：BCrypt加密存储
- **JWT认证**：Token过期自动失效
- **操作日志**：关键操作全程记录
- **数据备份**：支持夜审归档

---

## 📦 部署说明

### 生产环境配置
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hotel_pms
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
```

### Docker部署
```dockerfile
FROM eclipse-temurin:17-jre
COPY target/pms-api.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 👥 开发团队

- **项目名称**：PMS酒店管理系统
- **版本**：1.0.0-SNAPSHOT
- **最后更新**：2026-08-20

---

## 📄 许可证

本项目为内部使用系统，版权所有。

---

## 📞 技术支持

如有问题，请联系开发团队。
