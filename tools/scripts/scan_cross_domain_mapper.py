# -*- coding: utf-8 -*-
"""精确计算 Service 层的跨域 Mapper 依赖（R11）"""
import io
import os
import re

SVC = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-service\src\main\java\com\hotel\pms\service'

# 业务域 -> 允许的本域 Mapper 简单名
DOMAIN_MAPPERS = {
    'auth': {'SysAccountMapper', 'SysRoleMapper', 'SysRolePermissionMapper', 'SysUserRoleMapper', 'SysPermissionMapper'},
    'master': {'HotelMapper', 'HotelFloorMapper', 'RoomMapper', 'RoomTypeMapper', 'RoomCardMapper'},
    'price': {'RoomPriceMapper', 'RoomPricePlanMapper', 'RoomPricePlanDetailMapper', 'AgreementPriceMapper'},
    'reservation': {'ReservationMapper', 'ReservationPrepaymentMapper', 'TeamReservationMapper', 'TeamReservationRoomMapper'},
    'stay': {'StayMapper', 'StayGuestMapper', 'FolioMapper', 'TeamFolioMapper', 'TeamFolioPaymentMapper'},
    'finance': {'FinTransactionMapper', 'DepositMapper', 'CardIssueLogMapper'},
    'member': {'MemberMapper', 'MemberLevelMapper', 'MemberPointsLogMapper'},
    'guest': {'GuestMapper'},
    'nightaudit': {'NightAuditMapper', 'NightAuditStepMapper', 'NightAuditArchiveMapper'},
    'system': {'OperationLogMapper'},
    'config': {'HotelConfigMapper'},
    'doorlock': {'DoorLockConfigMapper'},
    'idcard': {'IdcardReaderConfigMapper', 'IdcardReaderLogMapper', 'IdcardReadRecordMapper'},
    'ota': {'OtaChannelMapper', 'OtaEventLogMapper', 'OtaOrderMapper'},
    'police': {'PoliceUploadRecordMapper'},
    'shift': {'SysShiftMapper', 'SysShiftMessageMapper', 'SysShiftNotifyConfigMapper'},
    'invoice': {'InvoiceMapper', 'InvoiceItemMapper'},
    'credit': {'CreditCompanyMapper'},
}
# 聚合/报表域：允许访问任意 Mapper（设计使然，登记白名单）
AGGREGATE = {'dashboard', 'report', 'metrics'}

# 全部 Mapper
ALL_MAPPERS = set()
for v in DOMAIN_MAPPERS.values():
    ALL_MAPPERS |= v

rows = []
for dp, dn, fn in os.walk(SVC):
    for f in sorted(fn):
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        c = io.open(p, encoding='utf-8').read()
        m = re.search(r'package com\.hotel\.pms\.service\.(\w+)', c)
        domain = m.group(1) if m else '?'
        mappers = set(re.findall(r'import com\.hotel\.pms\.dao\.mapper\.(\w+);', c))
        if domain in AGGREGATE:
            rows.append((domain, f, sorted(mappers), 'AGGREGATE-OK'))
            continue
        allowed = DOMAIN_MAPPERS.get(domain, set())
        cross = sorted(mappers - allowed)
        if cross:
            rows.append((domain, f, sorted(mappers), 'CROSS: ' + ', '.join(cross)))
        else:
            rows.append((domain, f, sorted(mappers), 'OK'))

print('=== 跨域 Service 类（R11 违规） ===')
cross_files = []
for domain, f, mps, status in rows:
    if status.startswith('CROSS'):
        cross_files.append(f)
        print('[{:11}] {:<40} {}'.format(domain, f, status))
print()
print('跨域类数:', len(cross_files))
print()
print('=== 全部 Service 域分布 ===')
from collections import Counter
print(Counter(r[0] for r in rows))
