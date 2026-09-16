# -*- coding: utf-8 -*-
"""后端：房价码金额查询支持（第三步修复：用方法签名作锚点）"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-service\src\main\java\com\hotel\pms\service\price\RoomPriceService.java'
s = io.open(p, encoding='utf-8').read()

# 检查前两步是否已生效
assert 'RoomPricePlanDetailMapper detailMapper' in s, 'STEP1/2 NOT APPLIED'
assert 'getPriceByDate(Long hotelId, Long roomTypeId, LocalDate date, Long pricePlanId)' not in s, 'OVERLOAD EXISTS'

anchor = '    public RoomPriceVO getPriceByDate(Long hotelId, Long roomTypeId, LocalDate date) {'
assert anchor in s, 'SIGNATURE ANCHOR NOT FOUND'

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
s = s.replace(anchor, overload + anchor)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('RoomPriceService overload added')
