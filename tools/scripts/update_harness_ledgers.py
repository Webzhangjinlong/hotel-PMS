# -*- coding: utf-8 -*-
"""更新 backlog / lessons / failure-review 台账（前端 hotelId 治理 + 门禁）"""
import io

H = r'D:\codex\hotelPMS_doubao\hotel-PMS\.harness'

# ===== backlog.md =====
p = H + r'\backlog.md'
with io.open(p, encoding='utf-8') as f:
    c = f.read()
old = '| V-06 | 前端硬编码 hotelId 治理 | `EnhancedReport.vue` queryParams.hotelId=1 改为 user store 取值 | R10 | P0 | ✅ 已完成 |'
new = '| V-06 | 前端硬编码 hotelId 治理 | 全仓 60 处/20 个 Vue 文件全部改为 userStore.hotelId（审计发现原登记仅 1 处，实际规模 60 处），并补 CI grep 门禁（V-08） | R10 | P0 | ✅ 已完成 |'
if old in c:
    c = c.replace(old, new)
    with io.open(p, 'w', encoding='utf-8', newline='') as f:
        f.write(c)
    print('backlog: V-06 已更新')
else:
    print('backlog: V-06 行未匹配')

# ===== lessons.md =====
p = H + r'\lessons.md'
with io.open(p, encoding='utf-8') as f:
    c = f.read()
anchor = '| L06 |'
new_lesson = ('| L07 | 前端硬编码 hotelId=1 登记规模严重低估：rule-registry 只登记 EnhancedReport.vue 1 处，审计全仓扫描发现实际 60 处/20 个 Vue 文件 | 登记违规时只凭已知违规点，未做全仓扫描，导致治理范围与实际不符 | CI grep 门禁（backend-ci 查 `hotelId=1L`、frontend-ci 查 `hotelId: 1`）+ 治理前全仓扫描流程（fix_frontend_hotelid.py） | ✅ 已固化 |\n')
if 'L07' not in c:
    idx = c.find(anchor)
    if idx >= 0:
        c = c[:idx] + new_lesson + c[idx:]
        with io.open(p, 'w', encoding='utf-8', newline='') as f:
            f.write(c)
        print('lessons: L07 已追加')
    else:
        print('lessons: 未找到 L06 锚点')
else:
    print('lessons: L07 已存在')

# ===== failure-review.md =====
p = H + r'\failure-review.md'
with io.open(p, encoding='utf-8') as f:
    c = f.read()
new_row = ('| 2026-09-15 | 前端 hotelId 治理（60 处/20 文件） | 批量脚本修正后 `npm run build` 失败：MainLayout.vue 多行 import（`import {\\n ... } from ...`）中间被插入 `const userStore = useUserStore()`，语法错误 | 修正脚本按"行首 import 匹配"定位插入点，未处理多行 import 语句 | 按括号平衡定位 import 块末尾重插；构建通过 | fix_frontend_hotelid.py（括号跟踪）+ lessons L07 | ✅ 已修复 |\n')
marker = '\n---\n'
if '前端 hotelId 治理' not in c:
    idx = c.find(marker)
    if idx >= 0:
        c = c[:idx] + new_row + c[idx:]
        with io.open(p, 'w', encoding='utf-8', newline='') as f:
            f.write(c)
        print('failure-review: 已追加')
    else:
        print('failure-review: 未找到分隔线')
else:
    print('failure-review: 已存在')
