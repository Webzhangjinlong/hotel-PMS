# 教训库（Lessons）

> 错误 → 根因 → 固化载体 → 状态。每次失败复盘后登记；新规则/新检查必须指向可追溯的教训。
> 维护人：系统管理员。

| # | 教训（问题描述） | 根因 | 固化载体 | 状态 |
|---|------------------|------|----------|------|
| L01 | 报表服务硬编码 `hotelId=1L`（8 处），多酒店场景下报表数据串店 | 开发时未从 `UserContext.getHotelId()` 取当前酒店，绕过登录上下文 | AGENTS.md 硬约束 #1 + rule-registry R10 + 静态扫描门禁（待建） | ⏳ 已登记待治理（V-03） |
| L02 | 早期开发在源码目录累积 20+ 个 `.bak`/`.backup` 备份文件，污染源码树、干扰代理阅读 | 直接复制改名当备份，未用 git 版本管理 | AGENTS.md DO NOT + .gitignore + backlog 清理项（V-05） | ⏳ 待治理 |
| L03 | 项目无 git 仓库 / 无测试 / 无 CI，架构约束只能靠文档软约束，无法机器强制 | 项目早期未按 harness 工程约束开发和执行 | 本仓库（AGENTS.md/.harness/ArchUnit/CI/verify-local） | ✅ 已固化（本次集成） |
| L04 | Controller 直调 Mapper/Entity（UserController 等 8 个 Controller、14 处引用），破坏分层 | 为图省事绕过 Service 层直接访问数据层 | ArchUnit R03/R04 + 存量豁免登记 + backlog 治理（V-01/V-02） | ⏳ 已登记待治理 |
| L05 | 前端引用不存在的 API 导出（`EnhancedReport.vue` 导入 `getFullReport` 而 report.js 未导出），生产构建直接失败 | 新增页面未同步维护 api 层导出，且此前无前端 CI 门禁，缺陷未被发现 | CI frontend-ci（npm run build 门禁）+ V-07 已修复 | ✅ 已固化 |
