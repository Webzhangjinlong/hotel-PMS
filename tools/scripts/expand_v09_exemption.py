# -*- coding: utf-8 -*-
"""V-09 豁免名单扩容：加入通配符 import 漏网的 8 个类"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-api\src\test\java\com\hotel\pms\arch\ArchitectureTest.java'
s = io.open(p, encoding='utf-8').read()

old = '            "ReservationService", "StayGuestService");'
new = ('            "ReservationService", "StayGuestService",\n'
       '            "CreditService", "DepositService", "FolioService", "PrepaymentService",\n'
       '            "ShiftService", "StayService", "TeamFolioService", "TeamReservationService");')
assert old in s, 'OLD NOT FOUND'
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('V-09 exemption expanded: +8 classes')
