# -*- coding: utf-8 -*-
"""ReservationList.vue：预订列表页新增预订弹窗 房价码金额修复
1. /v1/prices/query 传 pricePlanId（修复显示门市价）
2. 无房型时取房价码明细也设置 planDailyPrice（金额显示出现）
3. 清空房价码时同时清空 planDailyPrice（修复残留）
4. 房型变化时联动刷新房价码金额
"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reservation\ReservationList.vue'
s = io.open(p, encoding='utf-8').read()
orig = s

# 1. handlePricePlanChange 整体替换
old = r'''const handlePricePlanChange = async (planId) => {
  if (planId) {
    try {
      if (formData.roomTypeId) {
        const res = await request.get('/v1/prices/query', { params: { hotelId: queryParams.hotelId, roomTypeId: formData.roomTypeId, date: new Date().toISOString().split('T')[0] } })
        if (res.data?.price) { planDailyPrice.value = res.data.price;
          formData.dailyPrice = res.data.price
        }
      } else {
        const planRes = await request.get('/v1/price-plans/' + planId)
        if (planRes.data?.details && planRes.data.details.length > 0) {
          formData.dailyPrice = planRes.data.details[0].finalPrice
        }
      }
    } catch (error) {
      console.error('获取房价失败', error)
    }
  } else {
    formData.dailyPrice = null
  }
}'''
new = r'''/**
 * 房价码变化处理：按 房价码 + 房型 查询房价码金额（/v1/prices/query 支持 pricePlanId）
 */
const handlePricePlanChange = async (planId) => {
  if (planId) {
    try {
      if (formData.roomTypeId) {
        const res = await request.get('/v1/prices/query', { params: { hotelId: queryParams.hotelId, roomTypeId: formData.roomTypeId, date: new Date().toISOString().split('T')[0], pricePlanId: planId } })
        if (res.data?.price) {
          planDailyPrice.value = res.data.price
          formData.dailyPrice = res.data.price
        }
      } else {
        const planRes = await request.get('/v1/price-plans/' + planId)
        if (planRes.data?.details && planRes.data.details.length > 0) {
          planDailyPrice.value = planRes.data.details[0].finalPrice
          formData.dailyPrice = planRes.data.details[0].finalPrice
        }
      }
    } catch (error) {
      console.error('获取房价码金额失败', error)
    }
  } else {
    planDailyPrice.value = null
    formData.dailyPrice = null
  }
}'''
assert old in s, 'PRICEPLAN CHANGE NOT FOUND'
s = s.replace(old, new)

# 2. 房型变化联动
old = r'''const handleRoomTypeChange = () => {
  selectedRoomInfo.value = null
  formData.roomId = null
}'''
new = r'''const handleRoomTypeChange = () => {
  selectedRoomInfo.value = null
  formData.roomId = null
  // 【联动】已选房价码时按新房型刷新房价码金额
  if (formData.pricePlanId) {
    handlePricePlanChange(formData.pricePlanId)
  }
}'''
assert old in s, 'ROOMTYPE CHANGE NOT FOUND'
s = s.replace(old, new)

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('ReservationList.vue updated')
