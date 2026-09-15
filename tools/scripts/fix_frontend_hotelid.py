# -*- coding: utf-8 -*-
"""治理前端 hotelId 硬编码：hotelId: 1 / hotelId = 1 → userStore.hotelId
- 所有文件均为 <script setup>，统一注入 useUserStore import + 实例化
- 替换值来源不变（键名/变量名保留），只改值
"""
import io
import os
import re

ROOT = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src'
IMPORT_LINE = "import { useUserStore } from '@/stores/user'"

FILES = [os.path.join(dp, f) for dp, dn, fn in os.walk(ROOT) for f in fn if f.endswith('.vue')]

def process(path):
    with io.open(path, encoding='utf-8') as f:
        c = f.read()
    orig = c

    # 1. 替换硬编码值（键名保留）
    c = re.sub(r'hotelId\s*:\s*1\b', 'hotelId: userStore.hotelId', c)
    c = re.sub(r'hotelId\s*=\s*1\b', 'hotelId = userStore.hotelId', c)

    if c != orig:
        # 2. 注入 import + 实例化（在第一个 import 前插入）
        if 'stores/user' not in c:
            m = re.search(r'(<script setup>\s*)', c)
            if m:
                # 放在 script 块开头，import 之前
                anchor = m.group(0)
                c = c.replace(anchor, anchor + '\n' + IMPORT_LINE + '\n', 1)
            else:
                raise RuntimeError('no <script setup> in ' + path)
        if 'useUserStore()' not in c:
            # 实例化放在 import 区之后第一个空行/语句前：直接放在 IMPORT_LINE 后面
            c = c.replace(IMPORT_LINE + '\n', IMPORT_LINE + '\nconst userStore = useUserStore()\n', 1)

    with io.open(path, 'w', encoding='utf-8', newline='') as f:
        f.write(c)
    n_old = len(re.findall(r'hotelId\s*[:=]\s*1\b', orig))
    n_new = len(re.findall(r'hotelId\s*[:=]\s*1\b', c))
    return n_old, n_new

total_old = 0
for p in sorted(FILES):
    with io.open(p, encoding='utf-8') as f:
        if re.search(r'hotelId\s*[:=]\s*1\b', f.read()):
            old, new = process(p)
            total_old += old
            print('{:<48} 替换 {} 处 -> 残留 {}'.format(os.path.relpath(p, ROOT), old, new))
print('DONE, 共替换 {} 处'.format(total_old))
