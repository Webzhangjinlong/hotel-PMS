# 失败复盘（Failure Review）

> 记录每次失败的背景、根因、修复与固化动作，形成收敛反馈环。
> 模板见文末；新增条目追加在表格上方。

| 日期 | 场景/任务 | 失败现象 | 根因 | 修复动作 | 固化载体 | 状态 |
|------|-----------|----------|------|----------|----------|------|
| 2026-09-15 | Harness 约束集成（首次） | 审计发现存量违规：Controller 直调 Mapper/Entity 14 处、报表服务硬编码 hotelId=1L 8 处、源码目录混入 20+ 备份文件 | 项目此前未按分层/隔离约束开发 | 制定约束标准 + ArchUnit 守护 + 存量违规登记 rule-registry（V-01~V-06） | AGENTS.md / .harness/* / ArchitectureTest / CI | ✅ 已登记，按 backlog 治理 |
| 2026-09-15 | 前端门禁验证 | `npm run build` 失败：EnhancedReport.vue 导入不存在的 `getFullReport` | 页面新增时未维护 api 层导出，且无前端 CI 门禁 | report.js 补齐 `getFullReport`（→ /api/v1/metrics/full-report），构建通过；登记 V-07 + lessons L05 | CI frontend-ci + report.js | ✅ 已修复 |

---

## 复盘模板（新失败使用）

```markdown
| 日期 | 场景/任务 | 失败现象 | 根因 | 修复动作 | 固化载体 | 状态 |
|------|-----------|----------|------|----------|----------|------|
| YYYY-MM-DD | <任务> | <现象> | <根因> | <修复动作> | <AGENTS.md 条目 / ArchUnit 规则 / 测试 / 工具> | ⏳/✅ |
```

复盘四问：
1. 约束是否已定义？若未定义 → 补 AGENTS.md 硬约束 + rule-registry。
2. 约束是否可机器强制？若只能靠人 → 落到 ArchUnit / DB / CI / 脚本。
3. 失败是否有测试覆盖？若无 → 补测试。
4. 同类风险是否已收敛？若否 → 登记 backlog 持续跟踪。
