# -*- coding: utf-8 -*-
"""修正 ArchitectureTest 域映射：stay 瘦身，新增 folio/payment 域"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-api\src\test\java\com\hotel\pms\arch\ArchitectureTest.java'
s = io.open(p, encoding='utf-8').read()

old1 = 'Map.entry("stay", Set.of("StayMapper", "StayGuestMapper", "FolioMapper", "TeamFolioMapper", "TeamFolioPaymentMapper")),'
new1 = 'Map.entry("stay", Set.of("StayMapper", "StayGuestMapper")),'
assert old1 in s, 'OLD1 NOT FOUND'
s = s.replace(old1, new1)

old2 = 'Map.entry("finance", Set.of("FinTransactionMapper", "DepositMapper", "CardIssueLogMapper")),'
new2 = 'Map.entry("folio", Set.of("FolioMapper", "TeamFolioMapper", "TeamFolioPaymentMapper")),\n            Map.entry("payment", Set.of("FinTransactionMapper", "DepositMapper")),'
assert old2 in s, 'OLD2 NOT FOUND'
s = s.replace(old2, new2)

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('domain map fixed: stay slimmed, folio/payment added')
