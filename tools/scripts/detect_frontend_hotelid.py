# -*- coding: utf-8 -*-
"""检测 20 个 Vue 文件的 script 结构，确定 hotelId 治理替换策略"""
import io, os, re

FILES = [
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\dashboard\DashboardView.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\finance\FinanceManagement.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\front-desk\DepositManagement.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\front-desk\PoliceUpload.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\front-desk\RoomBoard.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\front-desk\RoomCalendar.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\master\floor\FloorList.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\master\room\RoomList.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\master\room-type\RoomTypeList.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\night-audit\NightAuditArchive.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\price\PriceManagement.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\price\PricePlan.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reports\DailyReport.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reports\Metrics.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reservation\QuickCheckIn.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reservation\ReservationCreate.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reservation\ReservationList.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reservation\TeamReservationDetail.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reservation\TeamReservationList.vue',
    r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\system\HotelConfig.vue',
]

for p in FILES:
    c = io.open(p, encoding='utf-8').read()
    setup = '<script setup>' in c
    has_store_import = "stores/user" in c
    has_use = 'useUserStore()' in c
    m = re.search(r'<script[^>]*>', c)
    tag = m.group(0) if m else 'NO_SCRIPT'
    n = len(re.findall(r'hotelId\s*[:=]\s*1\b', c))
    print('{:<26} | setup={} | store_import={} | use={} | {} | hits={}'.format(
        os.path.basename(p), setup, has_store_import, has_use, tag, n))
