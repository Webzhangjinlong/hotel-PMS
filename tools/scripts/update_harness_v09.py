# -*- coding: utf-8 -*-
"""更新 .harness 台账：rule-registry V-09 完整清单、backlog 治理项、lessons 教训"""
import io

ROOT = r'D:\codex\hotelPMS_doubao\hotel-PMS'

# 1. rule-registry: V-09 行更新为完整 18 类清单
p = ROOT + r'\.harness\rule-registry.md'
s = io.open(p, encoding='utf-8').read()
old = '| V-09 | 服务层跨业务域直调 Mapper（审计发现） | 10 个 Service 类：AuthService→HotelMapper，NightAuditConfigService→HotelMapper，GuestService→StayMapper，RoomService→Guest/Reservation/RoomPrice/StayMapper，MemberService→GuestMapper，NightAuditService→GuestMapper，RoomPricePlanService→RoomTypeMapper，RoomPriceService→RoomTypeMapper，ReservationService→Hotel/Room/RoomTypeMapper，StayGuestService→GuestMapper | R11 | ArchUnit 新规则已豁免登记（KNOWN_CROSS_DOMAIN_VIOLATIONS），按 backlog 逐项改走对方 Service 后删除豁免 | ⏳ 豁免中（待收敛） |'
new = ('| V-09 | 服务层跨业务域直调 Mapper（审计发现） | **18 个 Service 类**（10 个 import 扫描 + 8 个通配符 import 漏网，机器规则复查确认）：'
       'AuthService→HotelMapper；NightAuditConfigService→HotelMapper；GuestService→StayMapper；'
       'RoomService→Guest/Reservation/RoomPrice/StayMapper；MemberService→GuestMapper；NightAuditService→GuestMapper；'
       'RoomPricePlanService→RoomTypeMapper；RoomPriceService→RoomTypeMapper；ReservationService→Hotel/Room/RoomTypeMapper；'
       'StayGuestService→GuestMapper；CreditService→AgreementPrice/FinTransactionMapper；'
       'DepositService→Folio/Guest/StayMapper；FolioService→CreditCompany/Deposit/FinTransaction/Guest/Hotel/Room/StayMapper；'
       'PrepaymentService→Folio/Reservation/ReservationPrepayment/Stay/TeamReservationMapper；'
       'ShiftService→FinTransaction/SysAccountMapper；StayService→Deposit/FinTransaction/Folio/Guest/Hotel/Reservation/Room/RoomType/TeamFolio/TeamReservation/TeamReservationRoomMapper；'
       'TeamFolioService→Hotel/Stay/TeamReservation/TeamReservationRoomMapper；'
       'TeamReservationService→Folio/Guest/Hotel/Room/RoomType/Stay/TeamFolioMapper | R11 | '
       'ArchUnit 规则 service_never_touches_other_domain_mapper 已豁免登记（KNOWN_CROSS_DOMAIN_VIOLATIONS），按 backlog 逐项改走对方 Service 后删除豁免 | ⏳ 豁免中（待收敛） |')
assert old in s, 'V-09 OLD NOT FOUND'
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('rule-registry V-09 updated')

# 2. backlog: 加 V-09 治理项（追加到文件尾部）
p = ROOT + r'\.harness\backlog.md'
s = io.open(p, encoding='utf-8').read()
if 'V-09' not in s:
    s = s.rstrip() + '\n\n## V-09 服务层跨域 Mapper 治理（R11 机器强制新增后登记）\n\n' \
        '- [ ] AuthService → HotelMapper（登录/酒店状态校验改走 HotelService）\n' \
        '- [ ] NightAuditConfigService → HotelMapper（改走 HotelService）\n' \
        '- [ ] GuestService → StayMapper（在住查询改走 StayService）\n' \
        '- [ ] RoomService → Guest/Reservation/RoomPrice/StayMapper（改走对应 Service）\n' \
        '- [ ] MemberService → GuestMapper（改走 GuestService）\n' \
        '- [ ] NightAuditService → GuestMapper（改走 GuestService）\n' \
        '- [ ] RoomPricePlanService / RoomPriceService → RoomTypeMapper（改走 RoomTypeService）\n' \
        '- [ ] ReservationService → Hotel/Room/RoomTypeMapper（改走 HotelService/RoomService）\n' \
        '- [ ] StayGuestService → GuestMapper（改走 GuestService）\n' \
        '- [ ] CreditService → AgreementPrice/FinTransactionMapper（协议价/账务改走对应 Service）\n' \
        '- [ ] DepositService → Folio/Guest/StayMapper（改走 FolioService/GuestService/StayService）\n' \
        '- [ ] FolioService → 跨 6 个域 Mapper（改走对应 Service）\n' \
        '- [ ] PrepaymentService → Folio/Reservation/Stay/TeamReservationMapper（改走对应 Service）\n' \
        '- [ ] ShiftService → FinTransaction/SysAccountMapper（改走账务/账户 Service）\n' \
        '- [ ] StayService → 跨 10 个域 Mapper（改走对应 Service）\n' \
        '- [ ] TeamFolioService → Hotel/Stay/TeamReservationMapper（改走对应 Service）\n' \
        '- [ ] TeamReservationService → Folio/Guest/Hotel/Room/RoomType/Stay/TeamFolioMapper（改走对应 Service）\n' \
        '- [ ] 每收敛一项，从 ArchitectureTest KNOWN_CROSS_DOMAIN_VIOLATIONS 删除对应豁免并跑 mvn test\n'
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('backlog V-09 added')
else:
    print('backlog already has V-09')

# 3. lessons: 追加本轮 3 条教训
p = ROOT + r'\.harness\lessons.md'
s = io.open(p, encoding='utf-8').read()
if 'L09' not in s and 'Map.of' not in s:
    s = s.rstrip() + '\n\n## L09 教训（2026-09-15，R11 机器强制落地）\n\n' \
        '1. **Java `Map.of()` 最多 10 对键值**：18 个域的映射表编译失败（无法推断类型变量 K,V / 实际参数列表长度不同）→ 改用 `Map.ofEntries(...)`，无数量上限。\n' \
        '2. **`noClasses().should(ArchCondition)` 语义反转**：ArchCondition 的 violated 事件在 noClasses 语境下被反转为“满足条件”，导致规则形同虚设（测试全绿）。必须用 `classes().should(ArchCondition)` 正向语义：violated=违规=测试失败。\n' \
        '3. **ArchUnit 不导入“未使用”的 private 字段**：只声明不调用的 Mapper 字段（如 RoomService.guestMapper/stayMapper）不出现在 `getMembers()` 与依赖集合中，仅靠依赖通道会漏检；同时源码 `import ...mapper.*;` 通配符让 import 正则扫描失明（漏 8 个类）。机器规则（字节码级）比源码 import 扫描准确——扫描脚本仅作辅助。\n' \
        '4. **V-05 备份清理漏了 `.fullbak` 变体**（StayService.java.fullbak 混入源码目录被跟踪），本轮删除并 git rm；清理类任务应覆盖所有备份后缀变体。\n'
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('lessons L09 added')
else:
    print('lessons already has L09')
