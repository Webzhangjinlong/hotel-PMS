# AGENTS.md — PMS 酒店管理系统（Codex 自动加载）

本文件由 Codex/编码代理每次启动时自动读取并注入上下文。所有规则直接内联于此，不要求 Agent 额外翻阅文档；详细设计见文末导航。

## 项目

酒店物业管理系统（PMS）。核心功能域：认证授权、主数据（酒店/楼层/房型/房间）、房价管理、预订管理（散客/团队）、入住管理（散客/团队/预订/续住/退房/换房）、账务管理（收款/退款/冲账/挂账/预付款）、交班、夜审、经营报表（12 张收银报表）、会员、协议单位、发票、OTA、公安上传、操作日志。

## 技术栈（版本锁定，禁止擅自变更）

- 前端：Vue 3 + Vite 5 + Pinia + Vue Router + Element Plus + ECharts（`pms-web/`）
- 后端：Spring Boot 3.3.5 (JDK 17) + MyBatis-Plus 3.5.7 + PostgreSQL 17 + Flyway + Redisson(Redis) + JWT，Maven 多模块（`pms-common` / `pms-dao` / `pms-service` / `pms-api`，包根 `com.hotel.pms`）
- 存储：PostgreSQL（金额 NUMERIC(12,2)、时间 TIMESTAMP）+ Redis（分布式锁/缓存）
- 所有 Controller 返回统一 `Result<T>{code, message, data, timestamp}`，code=200 成功；分页统一 `PageResponse{records, total, page, size, pages}`

## 硬约束（违反即必须修复，禁止交付）

### 业务（酒店核心）
1. **多酒店隔离**：所有业务查询/写入必须按当前登录酒店 `hotel_id` 过滤/写入，取 `UserContext.getHotelId()`（api 层注入）。**禁止硬编码酒店 ID**（如 `1L`）、禁止跨酒店读写。
2. 金额一律 `BigDecimal` / `NUMERIC(12,2)`，**禁止 float/double**（字段、参数、局部变量同禁）。
3. 账务流水 `fin_transaction` **只增不改**，冲账通过 `REVERSAL` 流水（`refund_transaction_id` 关联原单），禁止直接修改/物理删除流水。
4. 同一房间同一时间只能有一个在住单（`stay` 部分唯一索引兜底）；预订/入住防超卖（事务内锁校验 + 唯一索引），**禁止并发超卖**。
5. 编号生成（预订号/入住单号/账务单号等）全局唯一、并发不重号。
6. 夜审必须 Redis 分布式锁保证单实例执行；失败步骤支持重试/重置；禁止并发执行夜审。
7. 业务数据统一逻辑删除 `deleted`，查询默认过滤；禁止物理删业务数据（配置/字典类除外，需在 rule-registry 登记语义）。
8. 公安上传、OTA 事件投递必须幂等（eventId/渠道单号去重），失败按退避重试，禁止重复投递。
9. 敏感数据加密存储：密码 BCrypt、身份证/渠道密钥等 AES-GCM；禁止明文落库。

### 架构与分层
10. 依赖方向 `Controller → Service → Mapper(DAO)`，禁止反向；**Controller 不得直调 Mapper、不得直用 Entity**（必须经 Service + DTO/VO 转换）。
11. 模块依赖 `pms-api → pms-service → pms-dao → pms-common`，禁止跨层依赖（service 不得依赖 api，dao 不得依赖 service，common 不得依赖其他模块）。
12. 代码落位：DTO/VO/常量/枚举/异常/工具 → `pms-common`；Entity/Mapper/Flyway → `pms-dao`；业务逻辑 → `pms-service`；Controller/拦截器/AOP/配置 → `pms-api`。
13. Service 跨业务域协作**走对方 Service**，禁止绕过 Service 直调其他业务域 Mapper（存量例外见 `.harness/rule-registry.md` 登记）。
14. 写操作必须 `@Transactional(rollbackFor = Exception.class)` + 必要幂等控制；禁止无事务多步写。
15. 接口统一返回 `Result<T>`；分页统一 `PageRequest/PageResponse`；禁止自造分页结构、禁止裸返回 Entity。

### 代码与命名
16. 类名大驼峰；分层后缀命名（`*Controller` / `*Service` / `*Mapper`）；常量 `UPPER_SNAKE`；表/字段小写下划线。
17. 禁止魔法值：状态/类型/支付方式等一律用常量类（`pms-common/constant/*Constants`）或枚举；禁止散落字符串字面量。
18. 接口路径 RESTful：`/api/v1/{模块}/*`；Controller 方法必须带权限注解（`@RequiresPermission` / `@RequiresRole`，白名单除外）。
19. 注释规范按 `doc/06-代码开发规范.md`：每个类/方法/关键调用点必须注释（`/** */` + `// 【步骤】`），禁止无注释交付。
20. 日志禁止输出密码/token/身份证/手机号等敏感信息；关键写操作（入住/退房/收款/退款/冲账/夜审/交班/用户管理）必须留操作日志（`@OperationLog`）。

### 数据库
21. 结构变更只走 Flyway 迁移（`pms-dao/src/main/resources/db/migration`，`V{n}__*.sql`），**禁止手工改库、禁止修改已合入脚本**（checksum 保护，变更用新脚本）。
22. 公共字段：`id / created_at / updated_at / deleted / version`（继承 `BaseEntity`）；主键自增；时间 `TIMESTAMP`。
23. 关键约束落 DDL：唯一约束（房间号/编号/用户名等）、部分唯一索引（在住单）、金额 `NUMERIC(12,2)`、外键/索引按查询路径设计。
24. 索引命名 `idx_表_字段`、唯一约束 `uk_表_字段`；表/字段注释必写（`COMMENT ON`）。

### 安全
25. 密码 BCrypt；JWT 无状态（有效期 24h 配置）；登录失败 5 次锁定 30 分钟；验证码防暴力破解。
26. 入参必须 `@Valid` + JSR-303 校验；SQL 一律 MyBatis-Plus 参数化（`LambdaQueryWrapper`/XML 参数绑定），禁止字符串拼接 SQL。
27. 敏感字段（身份证/密钥）AES-GCM；防止 XSS/CSRF；token 不写日志。

## 开发工作流

1. 每次改动从 `main` 拉新分支：`feat/<模块>-<简述>` 或 `fix/<简述>`；**禁止直推 main**。
2. 提交用 Conventional Commits（`feat:` / `fix:` / `chore:` / `docs:` / `refactor:` / `test:`）。
3. 完成标准：按 `.harness/checklist.md` 自查 → 本地验证通过（`tools/verify-local.ps1`）→ 提交 → push → 创建 PR → CI 全绿 → 合入。
4. 涉及新业务规则时，同步更新 `.harness/rule-registry.md`；新失败复盘到 `.harness/failure-review.md` 并登记 `.harness/lessons.md`。
5. 功能开发按 `.harness/backlog.md` 顺序执行；存量违规治理（`ArchUnit` 豁免）按 `rule-registry` 登记逐项收敛，修复后删除豁免。

## 命令清单（已实测可执行）

```bash
# 后端（工作目录 hotel-PMS/ 根）
mvn -pl pms-api -am package -DskipTests   # 编译打包（跳过测试）
mvn test                                   # 运行测试（含 ArchUnit 架构守护）

# 前端（工作目录 pms-web/）
npm run build      # 构建验证

# 工具脚本（仓库根目录）
.\tools\verify-local.ps1            # 本地全量验证（后端 mvn verify + 前端 build），ALL GREEN 才算过
```

## DO NOT

- 不要 `--no-verify` 跳过检查，不要强推/直推 `main`，不要绕过分支保护合入。
- 不要手工改数据库、不要用 float/double 存金额、不要跳过或篡改测试让构建变绿。
- 不要把 token/密码/密钥写入代码、提交、日志或文档。
- 不要硬编码 `hotel_id`（如 `1L`）；不要为实现方便破坏上述任何一条硬约束。
- 不要新增 `.bak` / `.backup` / `.bak2` 类文件混入源码目录（存量见 backlog 清理）。

## 文档导航（详细设计，按需查阅）

- `docs/开发约束与流程规范.md` — Harness 约束体系、CI 门禁、收敛机制
- `doc/01-业务设计.md` / `doc/02-功能设计.md` — 业务与功能设计
- `doc/03-技术设计.md` — 技术架构、分层、数据库设计
- `doc/06-代码开发规范.md` — 代码/注释/命名/日志规范
- `doc/08-接口文档.md` — 接口定义
- `doc/05-功能开发进度.md` — 功能开发进度
- `.harness/rule-registry.md` — 机器强制规则注册表（ArchUnit/DB/CI 载体与状态）
- `.harness/lessons.md` — 教训库（错误→根因→固化载体→状态）
- `.harness/failure-review.md` — 失败复盘记录与模板
- `.harness/checklist.md` — 每次 PR 前的完成自查清单
- `.harness/backlog.md` — 功能开发与存量治理 Backlog
