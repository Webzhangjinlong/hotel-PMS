# 开发完成检查清单（Checklist）

> 来源：docs/开发约束与流程规范.md + AGENTS.md 工作流。
> **每次 PR 提交前逐项自查**；任一项不满足禁止合入。

## A. 任务定义

- [ ] 任务拆解 ≤ 1 天，有明确验收标准（对应哪条硬约束 / 哪个接口 / 哪个功能点）
- [ ] 需求与 .harness/backlog.md / doc/02-功能设计.md 对应功能点一致

## B. 上下文与约束

- [ ] 已读 AGENTS.md（Codex 自动加载）与相关 docs/ 章节；**没有凭记忆写代码**
- [ ] 涉及表结构变更：已写 Flyway 迁移（`V{n}__*.sql`），未改已合入脚本（checksum 保护）
- [ ] 涉及新业务规则：已同步 .harness/rule-registry.md

## C. 硬约束自查（违反即禁止交付）

- [ ] 金额一律 BigDecimal / NUMERIC(12,2)，**无 float/double**
- [ ] 业务查询/写入带 hotel_id 且取自 `UserContext.getHotelId()`，**无硬编码酒店 ID**
- [ ] 依赖方向 Controller→Service→Mapper；**Controller 无直调 Mapper、无直用 Entity**
- [ ] 模块依赖 api→service→dao→common，无跨层依赖；Service 跨域走对方 Service
- [ ] 账务流水只增不改（冲账用 REVERSAL）；写操作 @Transactional；分页统一 PageResponse
- [ ] 命名规范（类大驼峰/分层后缀/常量 UPPER_SNAKE/表小写下划线）；无魔法值（常量/枚举）
- [ ] Controller 方法带权限注解（@RequiresPermission/@RequiresRole，白名单除外）
- [ ] 关键写操作带 @OperationLog；日志无敏感信息；入参 @Valid；SQL 参数化
- [ ] 密码 BCrypt；敏感字段（身份证等）AES-GCM；不新增 `.bak`/`.backup` 文件

## D. 自跑验证（本机）

- [ ] 后端：`tools/verify-local.ps1`（mvn verify 全绿：测试 + ArchUnit 架构守护）
- [ ] 前端：`npm run build` 成功
- [ ] 业务核心用例通过（多酒店隔离/防超卖/编号唯一/账务冲账/夜审锁）
- [ ] 新增代码有对应测试（业务硬约束必须有测试用例，结构性规则由 ArchUnit 守护）

## E. 提交与 CI

- [ ] 分支名 `feat/<模块>-<简述>` 或 `fix/<简述>`；**未直推 main**
- [ ] commit message 用 Conventional Commits（feat:/fix:/chore:/docs:/refactor:/test:）
- [ ] PR 描述关联需求与验收标准
- [ ] CI 全绿（backend-ci + frontend-ci）才合入；未用 `--no-verify`/绕过

## F. 收敛（每次交付后）

- [ ] 新失败已复盘（failure-review.md）并登记 lessons.md
- [ ] 新规则已固化（AGENTS.md / ArchUnit / 工具 / DB / CI）并更新 rule-registry.md
- [ ] 存量违规（V-01~V-05）治理进度已更新，ArchUnit 豁免数与登记一致
