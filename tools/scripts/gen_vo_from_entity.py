# -*- coding: utf-8 -*-
"""从 Entity 生成 VO（提取字段与注释，补 id/createdAt/updatedAt）"""
import io
import os
import re

ENTITY_DIR = r"D:\codex\hotelPMS_doubao\hotel-PMS\pms-dao\src\main\java\com\hotel\pms\dao\entity"
DTO_DIR = r"D:\codex\hotelPMS_doubao\hotel-PMS\pms-common\src\main\java\com\hotel\pms\common\dto"

# 需要生成的 VO：Entity -> VO
TARGETS = {
    "OperationLogEntity": "OperationLogVO",
    "SysPermission": "SysPermissionVO",
    "PoliceUploadRecord": "PoliceUploadRecordVO",
    "NightAuditArchive": "NightAuditArchiveVO",
    "SysShift": "SysShiftVO",
}

def extract_fields(entity_name):
    path = os.path.join(ENTITY_DIR, entity_name + ".java")
    with io.open(path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    fields = []  # (type, name, comment)
    i = 0
    while i < len(lines):
        line = lines[i]
        m = re.search(r"private\s+([\w<>,.\s]+?)\s+(\w+)\s*;", line)
        if m and not re.search(r"@TableField|@TableId|@TableLogic|@Version", line):
            ftype = m.group(1).strip()
            fname = m.group(2)
            # 向上找注释
            comment = ""
            j = i - 1
            while j >= 0 and lines[j].strip().startswith("/"):
                cm = re.search(r"\*\s*(.+)", lines[j])
                if cm:
                    comment = cm.group(1).strip()
                    break
                j -= 1
            fields.append((ftype, fname, comment))
        i += 1
    return fields

def gen_vo(entity_name, vo_name):
    fields = extract_fields(entity_name)
    # 补公共字段
    common = [("Long", "id", "主键ID"), ("java.time.LocalDateTime", "createdAt", "创建时间"), ("java.time.LocalDateTime", "updatedAt", "更新时间")]
    all_fields = common + [f for f in fields if f[1] not in ("id", "createdAt", "updatedAt")]
    lines = []
    lines.append("package com.hotel.pms.common.dto;\n")
    lines.append("import lombok.Data;\n")
    lines.append("import java.time.LocalDateTime;\n")
    lines.append("/**\n * {0} 视图对象\n */".format(vo_name))
    lines.append("@Data")
    lines.append("public class {0} {{\n".format(vo_name))
    for ftype, fname, comment in all_fields:
        if comment:
            lines.append("    /** {0} */".format(comment))
        lines.append("    private {0} {1};\n".format(ftype, fname))
    lines.append("}")
    content = "\n".join(lines)
    out = os.path.join(DTO_DIR, vo_name + ".java")
    with io.open(out, 'w', encoding='utf-8', newline='') as f:
        f.write(content)
    print("生成 {0} <- {1} ({2} 字段)".format(vo_name, entity_name, len(all_fields)))

for e, v in TARGETS.items():
    gen_vo(e, v)
print("DONE")
