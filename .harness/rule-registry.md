# 规则注册表（Rule Registry）

> 本表登记所有已落地为**机器强制**的规则（ArchUnit / CI / DB 约束 / 服务校验），
> 以及**存量违规**与**待收紧规则**（⏳ 状态）。来源：AGENTS.md 硬约束 + docs/开发约束与流程规范.md。
> 每条规则注明：载体（哪里强制）、验证方式、生效状态。
> 收敛机制：存量违规在 ArchUnit 中以 `@ArchIgnore` + 原因注释豁免，修复一项删除一项豁免，直到结构幂等。

## 已注册规则

| # | 规则 | 载体 | 验证方式 | 状态 |
|---|------|------|----------|------|
| R01 | 金额一律 `NUMERIC(12,2)`/BigDecimal，禁止 float/double | ArchUnit `business_fields_must_not_be_float_or_double`（entity+dto） | `mvn verify` | ✅ 生效 |
| R02 | 分层依赖方向 Controller→Service→Mapper，禁止反向 | ArchUnit `layered_dependencies`（com.hotel.pms..controller/service/dao.mapper） | `mvn verify` | ✅ 生效 |
| R03 | Controller 不得直调 Mapper | ArchUnit `controller_never_touches_mapper` | `mvn verify` | ⏳ 存量违规豁免（V-01） |
| R04 | Controller 不得直用 Entity（应经 Service + DTO/VO） | ArchUnit `controller_never_uses_entity` | `mvn verify` | ⏳ 存量违规豁免（V-02） |
| R05 | Controller 命名 `*Controller` | ArchUnit `controller_naming` | `mvn verify` | ✅ 生效 |
| R06 | Mapper 命名 `*Mapper` | ArchUnit `mapper_naming` | `mvn verify` | ✅ 生效 |
| R07 | Service 命名 `*Service`（顶层类，config 基础设施 `*Helper` 白名单） | ArchUnit `service_naming` | `mvn verify` | ✅ 生效 |
| R08 | Service 不得依赖 Controller | ArchUnit `service_never_touches_controller` | `mvn verify` | ✅ 生效 |
| R09 | Controller 不得依赖其他 Controller（ExceptionHandler 除外） | ArchUnit `controller_never_touches_controller` | `mvn verify` | ✅ 生效 |
| R10 | **多酒店隔离：禁止硬编码 hotel_id（如 1L）** | 代码审查 + 静态扫描（Grep 门禁待建） | CI 冒烟 + 人工复查 | ⏳ 存量违规（V-03）待修复 |
| R11 | 跨业务域禁止直调他人 Mapper（走对方 Service） | 文档约束（存量 ReportServiceImpl 违规登记） | 代码审查 | ⏳ 待收紧（V-05） |
| R12 | 结构变更只走 Flyway，禁止改已合入脚本 | Flyway checksum + `db/migration/V*.sql` | `mvn verify`（迁移校验） | ✅ 生效 |
| R13 | 接口统一返回 `Result<T>`；分页统一 `PageRequest/PageResponse` | 代码约定 + 审查 | 人工复查 | ✅ 生效（约定） |
| R14 | 关键写操作留操作日志（`@OperationLog` + AOP） | OperationLogAspect（44 个注解已接入） | 运行日志 + 审查 | ✅ 生效 |
| R15 | 密码 BCrypt；JWT 无状态；登录失败 5 次锁定 30 分钟 | AuthService + JwtUtil | 接口实测 | ✅ 生效 |
| R16 | CI 门禁：合入 main 前必须通过 backend-ci + frontend-ci | GitHub 分支保护 required status checks | PR 状态检查 | ⏳ 需远端仓库启用保护 |
| R17 | 本地全量验证 `tools/verify-local.ps1`（后端 mvn verify + 前端 build） | 脚本固化 | 退出码 0 = ALL GREEN | ✅ 生效 |

## 存量违规登记（治理 Backlog 对应项）

| 编号 | 违规描述 | 位置 | 对应规则 | 治理方式 | 状态 |
|------|----------|------|----------|----------|------|
| V-01 | Controller 直调 Mapper | `UserController`（SysAccountMapper、SysUserRoleMapper） | R03 | 抽到 Service 层，ArchUnit 豁免删除 | ⏳ 待治理 |
| V-02 | Controller 直用 Entity | `MemberController`/`RoomController`/`NightAuditArchiveController`/`OperationLogController`/`PermissionController`/`PoliceUploadController`/`ShiftController`/`UserController`（8 个类，ArchUnit 实测 7 处调用 + 7 处 import） | R04 | 经 Service 返回 DTO/VO | ⏳ 待治理 |
| V-03 | 硬编码 hotelId=1L | `ReportServiceImpl` 8 处（getShiftReport/getEntryDetail/getEntrySummary/getEntryTotal/getPaymentDetail/getPaymentSummary/getTransferReport/getChargeBackAdjust/getCheckoutActualStats/getCheckoutActualDetail） | R10 | 改用 `UserContext.getHotelId()` | ⏳ 待治理 |
| V-04 | 报表服务跨域直调 Mapper | `ReportServiceImpl` 直调 DepositMapper/MemberMapper/SysShiftMapper 等 | R11 | 收敛为走对应 Service 或显式登记例外 | ⏳ 待收紧 |
| V-05 | 源码目录混入 `.bak`/`.backup` 文件 | 全仓约 20+ 个（`*.bak`、`*.backup`、`*.backup2`~`*.backup6`） | AGENTS.md DO NOT | 确认无用后清理 | ⏳ 待治理 |
| V-06 | 前端硬编码 hotelId=1 | `pms-web/src/views/reports/EnhancedReport.vue`（queryParams.hotelId=1） | R10 | 从登录态/路由上下文取当前酒店 | ⏳ 待治理 |
| V-07 | 前端引用不存在的 API 导出 | `EnhancedReport.vue` 导入 `getFullReport` 而 `api/report.js` 未导出（存量 bug，构建失败） | CI frontend-ci | **已修复**（report.js 补 getFullReport → /api/v1/metrics/full-report） | ✅ 已修复 |

## 规则注册流程

1. 新增/变更业务规则 → 同步更新 AGENTS.md 硬约束 + 本表。
2. 规则若可落到 ArchUnit/DB/CI/脚本，必须落地；纯文档规则标注"文档约束"。
3. 存量违规修复后：删除 ArchUnit 对应 `@ArchIgnore` 豁免 + 将本表状态改为 ✅。

## 已知存量设计差异（按项目实际，不视为违规）

| 项 | 说明 |
|----|------|
| 主键策略 | `IdType.AUTO` 数据库自增（非雪花 ID），`BaseEntity` 统一 |
| 权限注解 | 自定义 `@RequiresPermission` / `@RequiresRole` + `PermissionInterceptor`（非 Spring Security @PreAuthorize） |
| 多租户隔离 | 应用层 `hotel_id` 过滤（`UserContext.getHotelId()`），未启用 DB RLS |
| 分页结构 | `PageResponse{records,total,page,size,pages}`（与 finance 一致） |
