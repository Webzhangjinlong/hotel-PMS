# -*- coding: utf-8 -*-
"""历史脱敏脚本（供 git filter-branch --tree-filter 调用）：
将 application-prod.yml 中的生产库连接信息替换为环境变量占位符。"""
import io
import os

p = 'pms-api/src/main/resources/application-prod.yml'
if not os.path.isfile(p):
    raise SystemExit(0)  # 该提交无此文件，跳过

s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('jdbc:postgresql://111.229.231.12:5432/hotel_pms',
              'jdbc:postgresql://${PMS_DB_HOST}:${PMS_DB_PORT}/hotel_pms')
s = s.replace('username: pms_user', 'username: ${PMS_DB_USERNAME}')
s = s.replace('password: postgres', 'password: ${PMS_DB_PASSWORD}')
if s != orig:
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
