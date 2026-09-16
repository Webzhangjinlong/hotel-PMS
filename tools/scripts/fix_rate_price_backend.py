# -*- coding: utf-8 -*-
"""后端：房价码金额查询支持
1. RoomPriceService 注入 RoomPricePlanDetailMapper + 新增 getPriceByDate(..., pricePlanId) 重载
2. RoomPriceController /query 增加可选 pricePlanId 参数
"""
import io

# ---------- 1. RoomPriceService ----------
p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-service\src\main\java\com\hotel\pms\service\price\RoomPriceService.java'
s = io.open(p, encoding='utf-8').read()

# A. import
old = 'import com.hotel.pms.dao.entity.RoomPrice;\nimport com.hotel.pms.dao.entity.RoomType;\nimport com.hotel.pms.dao.mapper.RoomPriceMapper;\nimport com.hotel.pms.dao.mapper.RoomTypeMapper;'
new = ('import com.hotel.pms.dao.entity.RoomPrice;\n'
       'import com.hotel.pms.dao.entity.RoomPricePlanDetail;\n'
       'import com.hotel.pms.dao.entity.RoomType;\n'
       'import com.hotel.pms.dao.mapper.RoomPriceMapper;\n'
       'import com.hotel.pms.dao.mapper.RoomPricePlanDetailMapper;\n'
       'import com.hotel.pms.dao.mapper.RoomTypeMapper;')
assert old in s, 'IMPORT BLOCK NOT FOUND'
s = s.replace(old, new)

# B. 注入 detailMapper
old = '    @Autowired\n    private RoomPricePlanService roomPricePlanService;'
new = ('    @Autowired\n    private RoomPricePlanService roomPricePlanService;\n\n'
       '    @Autowired\n'
       '    private RoomPricePlanDetailMapper detailMapper;')
assert old in s, 'INJECTION BLOCK NOT FOUND'
s = s.replace(old, new)

# C. 新增带 pricePlanId 的重载方法（插在原 getPriceByDate 注释前）
anchor = '    /**\n     * 查询指定日期房价\n     *\n     * @param hotelId 酒店ID\n     * @param roomTypeId 房型ID\n     * @param date 日期\n     * @return 房价信息\n     */\n    public RoomPriceVO getPriceByDate(Long hotelId, Long roomTypeId, LocalDate date) {'
overload = '''    /**
     * 查询指定日期房价（可指定房价码）
     *
     * @param hotelId     酒店ID
     * @param roomTypeId  房型ID
     * @param date        日期
     * @param pricePlanId 房价码ID（可选，非空时优先返回房价码金额）
     * @return 房价信息
     */
    public RoomPriceVO getPriceByDate(Long hotelId, Long roomTypeId, LocalDate date, Long pricePlanId) {
        // 【房价码金额：按 房价码 + 房型 查询房价码明细 final_price】
        if (pricePlanId != null) {
            RoomPricePlanDetail detail = detailMapper.selectOne(
                new LambdaQueryWrapper<RoomPricePlanDetail>()
                    .eq(RoomPricePlanDetail::getPlanId, pricePlanId)
                    .eq(RoomPricePlanDetail::getRoomTypeId, roomTypeId)
                    .last("LIMIT 1")
            );
            if (detail != null && detail.getFinalPrice() != null) {
                RoomType roomType = roomTypeMapper.selectById(roomTypeId);
                return RoomPriceVO.builder()
                        .hotelId(hotelId)
                        .roomTypeId(roomTypeId)
                        .roomTypeName(roomType != null ? roomType.getName() : null)
                        .priceDate(date)
                        .price(detail.getFinalPrice())
                        .status("PRICE_PLAN")
                        .build();
            }
        }
        // 【回退：门市价 / 房型基础价】
        return getPriceByDate(hotelId, roomTypeId, date);
    }

'''
assert anchor in s, 'GETPRICEBYDATE ANCHOR NOT FOUND'
s = s.replace(anchor, overload + anchor)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('RoomPriceService updated')

# ---------- 2. RoomPriceController ----------
p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-api\src\main\java\com\hotel\pms\api\controller\RoomPriceController.java'
s = io.open(p, encoding='utf-8').read()
old = '''    public Result<RoomPriceVO> getPriceByDate(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Parameter(description = "房型ID") @RequestParam Long roomTypeId,
            @Parameter(description = "日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        RoomPriceVO result = roomPriceService.getPriceByDate(hotelId, roomTypeId, date);
        return Result.success(result);'''
new = '''    public Result<RoomPriceVO> getPriceByDate(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Parameter(description = "房型ID") @RequestParam Long roomTypeId,
            @Parameter(description = "日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "房价码ID（可选）") @RequestParam(required = false) Long pricePlanId) {
        RoomPriceVO result = roomPriceService.getPriceByDate(hotelId, roomTypeId, date, pricePlanId);
        return Result.success(result);'''
assert old in s, 'CONTROLLER ANCHOR NOT FOUND'
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('RoomPriceController updated')
