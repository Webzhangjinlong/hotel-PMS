# -*- coding: utf-8 -*-
"""更新 rule-registry R16 为已生效（分支保护已启用）"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\.harness\rule-registry.md'
s = io.open(p, encoding='utf-8').read()
old = '| R16 | CI 门禁：合入 main 前必须通过 backend-ci + frontend-ci | GitHub 分支保护 required status checks | PR 状态检查 | ⏳ 需远端仓库启用保护 |'
new = ('| R16 | CI 门禁：合入 main 前必须通过 backend-ci + frontend-ci | GitHub 分支保护 required status checks'
       '（strict=true, enforce_admins=true, 禁 force push/删除） | PR 状态检查 | ✅ 生效（2026-09-15 API 启用，仓库已公开） |')
assert old in s, 'R16 OLD NOT FOUND'
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('R16 updated to 生效')
