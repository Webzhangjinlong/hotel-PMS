# -*- coding: utf-8 -*-
"""后端：房价码金额查询支持（分文件分步，每步立即写盘）"""
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
if old in s and 'RoomPricePlanDetailMapper' not in s:
    s = s.replace(old, new)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('A. import done')

# B. 注入 detailMapper
s = io.open(p, encoding='utf-8').read()
old = '    @Autowired\n    private RoomPricePlanService roomPricePlanService;'
new = ('    @Autowired\n    private RoomPricePlanService roomPricePlanService;\n\n'
       '    @Autowired\n'
       '    private RoomPricePlanDetailMapper detailMapper;')
if old in s and 'detailMapper' not in s:
    s = s.replace(old, new)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('B. injection done')

# C. 重载方法
s = io.open(p, encoding='utf-8').read()
anchor = '    public RoomPriceVO getPriceByDate(Long hotelId, Long roomTypeId, LocalDate date) {'
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
if anchor in s and 'pricePlanId' not in s:
    s = s.replace(anchor, overload + anchor)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('C. overload done')

# 验证
s = io.open(p, encoding='utf-8').read()
assert 'RoomPricePlanDetailMapper detailMapper' in s, 'SERVICE injection MISSING'
assert 'getPriceByDate(Long hotelId, Long roomTypeId, LocalDate date, Long pricePlanId)' in s, 'SERVICE overload MISSING'
print('RoomPriceService OK')

# ---------- 2. RoomPriceController ----------
p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-api\src\main\java\com\hotel\pms\api\controller\RoomPriceController.java'
s = io.open(p, encoding='utf-8').read()
old = '''            @Parameter(description = "日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        RoomPriceVO result = roomPriceService.getPriceByDate(hotelId, roomTypeId, date);
        return Result.success(result);'''
new = '''            @Parameter(description = "日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "房价码ID（可选）") @RequestParam(required = false) Long pricePlanId) {
        RoomPriceVO result = roomPriceService.getPriceByDate(hotelId, roomTypeId, date, pricePlanId);
        return Result.success(result);'''
assert old in s, 'CONTROLLER ANCHOR NOT FOUND'
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('RoomPriceController OK')
