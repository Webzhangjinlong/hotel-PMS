# -*- coding: utf-8 -*-
"""V-03/V-04 治理脚本：Report 模块 hotelId 参数化 + 删除死 Mapper 字段"""
import io
import re
import sys

def patch(path, transforms, description):
    with io.open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    print(f"===== {description} =====")
    for pattern, repl, flags in transforms:
        content, n = re.subn(pattern, repl, content, flags=flags)
        print(f"  [{n}] {pattern[:60]}")
    with io.open(path, 'w', encoding='utf-8', newline='') as f:
        f.write(content)

ROOT = r"D:\codex\hotelPMS_doubao\hotel-PMS"

# 1. ReportService.java：接口签名加 hotelId + javadoc
patch(
    ROOT + r"\pms-service\src\main\java\com\hotel\pms\service\report\ReportService.java",
    [
        (r"\(LocalDate businessDate, String shift, Long operatorId\);",
         r"(Long hotelId, LocalDate businessDate, String shift, Long operatorId);", 0),
        (r"(\s*\*\s*)@param businessDate 营业日期",
         r"\1@param hotelId 酒店ID\n     * @param businessDate 营业日期", 0),
    ],
    "ReportService.java 接口加 hotelId 参数",
)

# 2. ReportServiceImpl.java：实现签名加 hotelId + 1L 替换 + 删除 5 个死 Mapper 字段
impl = ROOT + r"\pms-service\src\main\java\com\hotel\pms\service\report\ReportServiceImpl.java"
patch(
    impl,
    [
        (r"\(LocalDate businessDate, String shift, Long operatorId\) \{",
         r"(Long hotelId, LocalDate businessDate, String shift, Long operatorId) {", 0),
        (r"FinTransaction::getHotelId, 1L",
         r"FinTransaction::getHotelId, hotelId", 0),
        (r"\n    @Autowired\n    private FolioMapper folioMapper;", "", 0),
        (r"\n    @Autowired\n    private StayMapper stayMapper;", "", 0),
        (r"\n    @Autowired\n    private DepositMapper depositMapper;", "", 0),
        (r"\n    @Autowired\n    private MemberMapper memberMapper;", "", 0),
        (r"\n    @Autowired\n    private SysShiftMapper shiftMapper;", "", 0),
    ],
    "ReportServiceImpl.java hotelId 参数化 + 删除死 Mapper 字段",
)

# 清理多余空行（字段删除后）
with io.open(impl, 'r', encoding='utf-8') as f:
    c = f.read()
c = re.sub(r"\n{3,}", "\n\n", c)
with io.open(impl, 'w', encoding='utf-8', newline='') as f:
    f.write(c)
print("  [clean] 压缩多余空行")

# 3. ReportController.java：import UserContext + 方法体取 hotelId + 调用传参
ctrl = ROOT + r"\pms-api\src\main\java\com\hotel\pms\controller\report\ReportController.java"
patch(
    ctrl,
    [
        (r"(import com\.hotel\.pms\.service\.report\.ReportService;)",
         r"import com.hotel.pms.api.config.UserContext;\n\1", 0),
        (r"(operatorId\) \{\n)(        return reportService\.)",
         r"\1        Long hotelId = UserContext.getHotelId();\n\2", 0),
        (r"reportService\.(\w+)\(businessDate, shift, operatorId\)",
         r"reportService.\1(hotelId, businessDate, shift, operatorId)", 0),
    ],
    "ReportController.java 从 UserContext 取 hotelId",
)

print("DONE")
