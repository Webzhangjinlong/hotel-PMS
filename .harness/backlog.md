# 功能开发与存量治理 Backlog

> 来源：doc/05-功能开发进度.md + rule-registry 存量违规登记。
> 功能开发按优先级执行；存量违规治理（V-01~V-07）已全部完成（2026-09-15），约束体系已收敛。

## 存量治理（P0，约束体系收敛）

| 编号 | 任务 | 说明 | 对应规则 | 优先级 | 状态 |
|------|------|------|----------|--------|------|
| V-01 | Controller 直调 Mapper 治理 | `UserController` 中 SysAccountMapper/SysUserRoleMapper 调用抽到 Service（新建 SysUserService/SysUserServiceImpl） | R03 | P0 | ✅ 已完成 |
| V-02 | Controller 直用 Entity 治理 | 8 个 Controller（Member/Room/NightAuditArchive/OperationLog/Permission/PoliceUpload/Shift/User）的 Entity 引用改为 Service + DTO/VO（新增 9 个 VO/DTO） | R04 | P0 | ✅ 已完成 |
| V-03 | 报表服务 hotel_id 硬编码治理 | `ReportServiceImpl` 8 处 `1L` 改为 hotelId 参数（12 个报表方法），Controller 从 UserContext 传入 | R10 | P0 | ✅ 已完成 |
| V-04 | 报表服务跨域 Mapper 调用收敛 | DepositMapper/MemberMapper/SysShiftMapper 等核验为死代码，已删除 | R11 | P1 | ✅ 已完成（实为死代码） |
| V-05 | 清理源码目录 `.bak`/`.backup` 文件 | 确认无用后删除（实际 70 个），.gitignore 已防护 | DO NOT | P1 | ✅ 已完成 |
| V-06 | 前端硬编码 hotelId 治理 | 全仓 60 处/20 个 Vue 文件全部改为 userStore.hotelId（审计发现原登记仅 1 处，实际规模 60 处），并补 CI grep 门禁（V-08） | R10 | P0 | ✅ 已完成 |
| V-07 | 前端缺失 API 导出修复 | report.js 补 `getFullReport`（→ /api/v1/metrics/full-report） | CI | 已完成 | ✅ 已修复 |

## 功能开发（参考 doc/05-功能开发进度.md 短期计划）

| 任务 | 优先级 | 预计工时 | 说明 |
|------|--------|----------|------|
| 换房功能 | P0 | 3天 | 客人换房操作、自动调整账务、房间状态更新 |
| 客人档案管理 | P1 | 2天 | 常住客人管理、历史入住记录查询 |
| 交班管理完善 | P1 | 2天 | 交班核对、现金清点、交班报表联动 |
| 维修管理 | P2 | 2天 | 维修工单、状态跟踪、完成确认 |
| 清洁任务 | P2 | 2天 | 任务分配、完成确认、清洁状态跟踪 |
| 协议单位 | P2 | 3天 | 协议客户管理、协议价管理 |
| 发票管理 | P2 | 3天 | 电子发票开具、发票记录查询 |

## 12 张收银报表（进行中，前后端框架已建）

| 报表 | 后端 | 前端 | 导出/打印 | 状态 |
|------|------|------|-----------|------|
| 1 收银员交接表 | 已实现（部分 TODO） | 基本完成 | 占位 | ⏳ 完善中 |
| 2 前台入账明细 | 已实现 | 占位模板 | 占位 | ⏳ |
| 3 前台入账简表 | 已实现 | 占位模板 | 占位 | ⏳ |
| 4 前台入账汇总 | 逻辑复用简表（错误） | 占位模板 | 占位 | ⏳ |
| 5 前台收款明细 | 已实现 | 占位模板 | 占位 | ⏳ |
| 6 前台收款汇总 | 已实现 | 占位模板 | 占位 | ⏳ |
| 7 前台转账报表 | 已实现 | 占位模板 | 占位 | ⏳ |
| 8 冲账调账报表 | 已实现 | 占位模板 | 占位 | ⏳ |
| 9 结账实收统计 | 已实现 | 占位模板 | 占位 | ⏳ |
| 10 结账实收明细 | 逻辑复用收款明细（错误） | 占位模板 | 占位 | ⏳ |
| 11 商品销售汇总 | TODO 空实现 | 占位模板 | 占位 | ⏳ |
| 12 商品销售明细 | TODO 空实现 | 占位模板 | 占位 | ⏳ |

报表已知问题：预授权/会员冻结/会员售卡/会员充值/协议回款为 TODO；收入分类靠描述文本（应改结构化分类）；班次时间硬编码（应与交班模块对齐）。

## 技术债务

| 任务 | 优先级 | 预计工时 | 说明 |
|------|--------|----------|------|
| 单元测试补充 | P1 | 5天 | Service 层测试覆盖核心业务约束 |
| 接口文档完善 | P1 | 3天 | 补充所有接口文档（doc/08） |
| 代码重构 | P2 | 5天 | 优化代码结构、提高可维护性 |
| 性能优化 | P2 | 3天 | 数据库索引、缓存优化 |

## V-09 服务层跨域 Mapper 治理（R11 机器强制新增后登记）

- [ ] AuthService → HotelMapper（登录/酒店状态校验改走 HotelService）
- [ ] NightAuditConfigService → HotelMapper（改走 HotelService）
- [ ] GuestService → StayMapper（在住查询改走 StayService）
- [ ] RoomService → Guest/Reservation/RoomPrice/StayMapper（改走对应 Service）
- [ ] MemberService → GuestMapper（改走 GuestService）
- [ ] NightAuditService → GuestMapper（改走 GuestService）
- [ ] RoomPricePlanService / RoomPriceService → RoomTypeMapper（改走 RoomTypeService）
- [ ] ReservationService → Hotel/Room/RoomTypeMapper（改走 HotelService/RoomService）
- [ ] StayGuestService → GuestMapper（改走 GuestService）
- [ ] CreditService → AgreementPrice/FinTransactionMapper（协议价/账务改走对应 Service）
- [ ] DepositService → Folio/Guest/StayMapper（改走 FolioService/GuestService/StayService）
- [ ] FolioService → 跨 6 个域 Mapper（改走对应 Service）
- [ ] PrepaymentService → Folio/Reservation/Stay/TeamReservationMapper（改走对应 Service）
- [ ] ShiftService → FinTransaction/SysAccountMapper（改走账务/账户 Service）
- [ ] StayService → 跨 10 个域 Mapper（改走对应 Service）
- [ ] TeamFolioService → Hotel/Stay/TeamReservationMapper（改走对应 Service）
- [ ] TeamReservationService → Folio/Guest/Hotel/Room/RoomType/Stay/TeamFolioMapper（改走对应 Service）
- [ ] 每收敛一项，从 ArchitectureTest KNOWN_CROSS_DOMAIN_VIOLATIONS 删除对应豁免并跑 mvn test
