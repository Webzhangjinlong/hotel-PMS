# -*- coding: utf-8 -*-
"""扫描全部 Flyway 脚本：各表的 hotel_id / 租户字段覆盖情况"""
import io
import os
import re

D = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-dao\src\main\resources\db\migration'
files = sorted(f for f in os.listdir(D) if f.endswith('.sql'))

# 收集所有 CREATE TABLE 定义块（含列）
tables = {}  # name -> text
for f in files:
    c = io.open(os.path.join(D, f), encoding='utf-8').read()
    # 匹配 CREATE TABLE [IF NOT EXISTS] name ( ... );
    for m in re.finditer(r'CREATE TABLE\s+(?:IF NOT EXISTS\s+)?([a-z_]+)\s*\((.*?)\);', c, re.S):
        tables[m.group(1)] = m.group(2)

print('=== 表清单与 hotel_id 覆盖 ===')
yes, no = [], []
for name in sorted(tables):
    body = tables[name]
    has_hotel = bool(re.search(r'\bhotel_id\b', body))
    has_hotel_code = bool(re.search(r'\bhotel_code\b', body))
    tag = 'hotel_id ✓' if has_hotel else ('hotel_code ✓' if has_hotel_code else '无租户字段')
    (yes if (has_hotel or has_hotel_code) else no).append(name)
    print('  [{:10}] {}'.format(tag, name))

print()
print('含 hotel_id/ hotel_code 的表数:', len(yes), '/', len(tables))
print('无租户字段的表:', ', '.join(no) if no else '无')
