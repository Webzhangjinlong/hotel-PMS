# -*- coding: utf-8 -*-
"""更新 rule-registry.md 的 V-06 行并追加 V-08 行"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\.harness\rule-registry.md'
with io.open(p, encoding='utf-8') as f:
    c = f.read()

old6 = '| V-06 | 前端硬编码 hotelId=1 | `pms-web/src/views/reports/EnhancedReport.vue`（queryParams.hotelId=1） | R10 | 改为从 user store 取 `userStore.hotelId` | ✅ 已治理 |'
new6 = ('| V-06 | 前端硬编码 hotelId=1 | 全仓 **60 处 / 20 个 Vue 文件**（RoomBoard 9、PriceManagement 8、RoomCalendar 7、'
        'RoomList 5、TeamReservationDetail 4、ReservationList 4、DashboardView 3、DepositManagement 3、PricePlan 3、'
        'DailyReport 2、FinanceManagement 2、ReservationCreate 2、其余 8 个各 1） | R10 | 全部改为 `userStore.hotelId`'
        '（注入 useUserStore）；CI frontend-ci 新增 grep 门禁拦截 `hotelId: 1` | ✅ 已治理 |')
if old6 in c:
    c = c.replace(old6, new6)
    print('V-06 已替换')
else:
    print('V-06 未找到，尝试按行查找...')
    for line in c.split('\n'):
        if line.startswith('| V-06 |'):
            print('当前 V-06 行：', line)

old7 = '| V-07 | 前端引用不存在的 API 导出 | `EnhancedReport.vue` 导入 `getFullReport` 而 `api/report.js` 未导出（存量 bug，构建失败） | CI frontend-ci | 已修复（report.js 补 getFullReport → /api/v1/metrics/full-report） | ✅ 已修复 |'
new8 = old7 + '\n' + ('| V-08 | 前端约束缺机器门禁（审计发现） | R10 前端只有构建门禁，`hotelId: 1` 硬编码可通过 CI（本次治理 60 处） | R10 '
        '| backend-ci 加 `hotelId=1L` grep、frontend-ci 加 `hotelId: 1` grep，出现即失败 | ✅ 已治理 |')
if old7 in c:
    c = c.replace(old7, new8)
    print('V-08 已追加')
else:
    print('V-07 未找到')

with io.open(p, 'w', encoding='utf-8', newline='') as f:
    f.write(c)
