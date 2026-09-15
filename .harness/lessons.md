# 教训库（Lessons）

> 错误 → 根因 → 固化载体 → 状态。每次失败复盘后登记；新规则/新检查必须指向可追溯的教训。
> 维护人：系统管理员。

| # | 教训（问题描述） | 根因 | 固化载体 | 状态 |
|---|------------------|------|----------|------|
| L01 | 报表服务硬编码 `hotelId=1L`（8 处），多酒店场景下报表数据串店 | 开发时未从 `UserContext.getHotelId()` 取当前酒店，绕过登录上下文 | AGENTS.md 硬约束 #1 + rule-registry R10 + Report 模块已改为 hotelId 参数化（V-03） | ✅ 已治理 |
| L02 | 早期开发在源码目录累积 70 个 `.bak`/`.backup` 备份文件，污染源码树、干扰代理阅读 | 直接复制改名当备份，未用 git 版本管理 | AGENTS.md DO NOT + .gitignore + 全部删除（V-05）；**教训：仅加 .gitignore 不会让已跟踪文件被忽略，需 `git rm -r --cached`** | ✅ 已治理 |
| L03 | 项目无 git 仓库 / 无测试 / 无 CI，架构约束只能靠文档软约束，无法机器强制 | 项目早期未按 harness 工程约束开发和执行 | 本仓库（AGENTS.md/.harness/ArchUnit/CI/verify-local） | ✅ 已固化（本次集成） |
| L04 | Controller 直调 Mapper/Entity（UserController 等 8 个 Controller、14 处引用），破坏分层 | 为图省事绕过 Service 层直接访问数据层 | ArchUnit R03/R04（豁免已全部清零）+ 全部 Controller 改经 Service + DTO/VO（V-01/V-02） | ✅ 已治理 |
| L05 | 前端引用不存在的 API 导出（`EnhancedReport.vue` 导入 `getFullReport` 而 report.js 未导出），生产构建直接失败 | 新增页面未同步维护 api 层导出，且此前无前端 CI 门禁，缺陷未被发现 | CI frontend-ci（npm run build 门禁）+ V-07 已修复 | ✅ 已固化 |
| L07 | 前端硬编码 hotelId=1 登记规模严重低估：rule-registry 只登记 EnhancedReport.vue 1 处，审计全仓扫描发现实际 60 处/20 个 Vue 文件 | 登记违规时只凭已知违规点，未做全仓扫描，导致治理范围与实际不符 | CI grep 门禁（backend-ci 查 `hotelId=1L`、frontend-ci 查 `hotelId: 1`）+ 治理前全仓扫描流程（fix_frontend_hotelid.py） | ✅ 已固化 |
| L06 | VO 生成脚本把多行 javadoc 提取成 `/** * xxx */ */` 损坏注释，导致 pms-common 编译失败 | 生成脚本按行提取注释时未合并多行并清理 `*` 前缀，闭合标记重复 | gen_vo_from_entity.py 修复（正则合并多行注释）；生成后立即编译验证 | ✅ 已固化 |

## L09 教训（2026-09-15，R11 机器强制落地）

1. **Java `Map.of()` 最多 10 对键值**：18 个域的映射表编译失败（无法推断类型变量 K,V / 实际参数列表长度不同）→ 改用 `Map.ofEntries(...)`，无数量上限。
2. **`noClasses().should(ArchCondition)` 语义反转**：ArchCondition 的 violated 事件在 noClasses 语境下被反转为“满足条件”，导致规则形同虚设（测试全绿）。必须用 `classes().should(ArchCondition)` 正向语义：violated=违规=测试失败。
3. **ArchUnit 不导入“未使用”的 private 字段**：只声明不调用的 Mapper 字段（如 RoomService.guestMapper/stayMapper）不出现在 `getMembers()` 与依赖集合中，仅靠依赖通道会漏检；同时源码 `import ...mapper.*;` 通配符让 import 正则扫描失明（漏 8 个类）。机器规则（字节码级）比源码 import 扫描准确——扫描脚本仅作辅助。
4. **V-05 备份清理漏了 `.fullbak` 变体**（StayService.java.fullbak 混入源码目录被跟踪），本轮删除并 git rm；清理类任务应覆盖所有备份后缀变体。
