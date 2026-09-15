# -*- coding: utf-8 -*-
"""负向验证辅助：从豁免名单移除指定类（验证 R11 规则可拦截）"""
import io
import sys

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-api\src\test\java\com\hotel\pms\arch\ArchitectureTest.java'
remove = sys.argv[1] if len(sys.argv) > 1 else 'RoomService'
s = io.open(p, encoding='utf-8').read()
name = '"' + remove + '",'
if name in s:
    s = s.replace(name, '', 1)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('removed', remove)
else:
    print('NOT FOUND:', remove)
