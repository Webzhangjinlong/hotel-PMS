# -*- coding: utf-8 -*-
"""DashboardView.vue：快捷新增预订弹窗 + 散客入住弹窗 接上房价码金额
1. handleReservationPP / handleWalkInPP 查询 /v1/prices/query 时传 pricePlanId（修复显示门市价的问题）
2. 房型变化时联动刷新房价码金额（两个弹窗）
3. 新增预订弹窗只读金额标签 "房价码价格" -> "房价码金额"
"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\dashboard\DashboardView.vue'
s = io.open(p, encoding='utf-8').read()
orig = s

# 1a. handleReservationPP：传 pricePlanId
old = "try { const r = await request.get('/v1/prices/query', { params: { hotelId, roomTypeId: reservationForm.roomTypeId, date: new Date().toISOString().split('T')[0] } }); if (r.data?.price) { planDailyPrice.value = r.data.price; reservationForm.dailyPrice = r.data.price } } catch(e) {}"
new = "try { const r = await request.get('/v1/prices/query', { params: { hotelId, roomTypeId: reservationForm.roomTypeId, date: new Date().toISOString().split('T')[0], pricePlanId: planId } }); if (r.data?.price) { planDailyPrice.value = r.data.price; reservationForm.dailyPrice = r.data.price } } catch(e) {}"
assert old in s, 'RESERVATION PP QUERY NOT FOUND'
s = s.replace(old, new)

# 1b. handleWalkInPP：传 pricePlanId
old = "try { const r = await request.get('/v1/prices/query', { params: { hotelId, roomTypeId: walkInForm.roomTypeId, date: new Date().toISOString().split('T')[0] } }); if (r.data?.price) walkInForm.dailyPrice = r.data.price } catch(e) {}"
new = "try { const r = await request.get('/v1/prices/query', { params: { hotelId, roomTypeId: walkInForm.roomTypeId, date: new Date().toISOString().split('T')[0], pricePlanId: planId } }); if (r.data?.price) walkInForm.dailyPrice = r.data.price } catch(e) {}"
assert old in s, 'WALKIN PP QUERY NOT FOUND'
s = s.replace(old, new)

# 2a. handleRoomTypeChange（walk-in 房型变化）：已选房价码时刷新金额
old = '''async function handleRoomTypeChange(roomTypeId) {
  walkInForm.roomId = null
  if (!roomTypeId) { availableRooms.value = []; return }
  try {
    const res = await request.get('/v1/rooms', { params: { hotelId, status: 'AVAILABLE', roomTypeId } })
    availableRooms.value = res.data?.records || []
  } catch (e) { console.error(e) }
}'''
new = '''async function handleRoomTypeChange(roomTypeId) {
  walkInForm.roomId = null
  if (!roomTypeId) { availableRooms.value = []; return }
  try {
    const res = await request.get('/v1/rooms', { params: { hotelId, status: 'AVAILABLE', roomTypeId } })
    availableRooms.value = res.data?.records || []
  } catch (e) { console.error(e) }
  // 【联动】已选房价码时按新房型刷新房价码金额
  if (walkInForm.pricePlanId) {
    handleWalkInPP(walkInForm.pricePlanId)
  }
}'''
assert old in s, 'ROOMTYPE CHANGE (walkin) NOT FOUND'
s = s.replace(old, new)

# 2b. handleReservationRoomTypeChange（新增预订房型变化）：已选房价码时刷新金额
old = '''function handleReservationRoomTypeChange() {
  selectedRoomInfo.value = null
  reservationForm.roomId = null
}'''
new = '''function handleReservationRoomTypeChange() {
  selectedRoomInfo.value = null
  reservationForm.roomId = null
  // 【联动】已选房价码时按新房型刷新房价码金额
  if (reservationForm.pricePlanId) {
    handleReservationPP(reservationForm.pricePlanId)
  }
}'''
assert old in s, 'ROOMTYPE CHANGE (reservation) NOT FOUND'
s = s.replace(old, new)

# 3. 标签统一
old = '<el-form-item label="房价码价格" v-if="planDailyPrice">'
new = '<el-form-item label="房价码金额" v-if="planDailyPrice">'
assert old in s, 'LABEL NOT FOUND'
s = s.replace(old, new)

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('DashboardView.vue updated')
