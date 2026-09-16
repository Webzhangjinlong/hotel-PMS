# -*- coding: utf-8 -*-
"""DashboardView.vue Part2：handleReservationPP 改造 + 协议单位/改价/来源逻辑 + 提交"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\dashboard\DashboardView.vue'
s = io.open(p, encoding='utf-8').read()

# 1. handleReservationPP 完整替换（协议价优先 + 来源/autoPrice 维护）
old = '''const handleReservationPP = async (planId) => {
  if (planId && reservationForm.roomTypeId) {
    try { const r = await request.get('/v1/prices/query', { params: { hotelId, roomTypeId: reservationForm.roomTypeId, date: new Date().toISOString().split('T')[0], pricePlanId: planId } }); if (r.data?.price) { planDailyPrice.value = r.data.price; reservationForm.dailyPrice = r.data.price } } catch(e) {}
  } else if (planId) {
    try { const r = await request.get('/v1/price-plans/' + planId); if (r.data?.details?.length) planDailyPrice.value = r.data.details[0].finalPrice; reservationForm.dailyPrice = r.data.details[0].finalPrice } catch(e) {}
  } else { planDailyPrice.value = null; reservationForm.dailyPrice = null }
}'''
new = '''const handleReservationPP = async (planId) => {
  if (!planId) {
    planDailyPrice.value = null
    reservationForm.dailyPrice = null
    autoPrice.value = null
    priceSource.value = 'WALKIN'
    // 已选协议单位时优先显示协议价
    if (reservationForm.creditCompanyId) {
      handleReservationCC(reservationForm.creditCompanyId)
    }
    return
  }
  // 协议单位优先：已选协议单位时房价码不覆盖协议价
  if (reservationForm.creditCompanyId) {
    handleReservationCC(reservationForm.creditCompanyId)
    return
  }
  try {
    if (reservationForm.roomTypeId) {
      const r = await request.get('/v1/prices/query', { params: { hotelId, roomTypeId: reservationForm.roomTypeId, date: new Date().toISOString().split('T')[0], pricePlanId: planId } })
      if (r.data?.price) { planDailyPrice.value = r.data.price; reservationForm.dailyPrice = r.data.price; autoPrice.value = r.data.price; priceSource.value = 'PRICE_PLAN' }
    } else {
      const r = await request.get('/v1/price-plans/' + planId)
      if (r.data?.details?.length) { planDailyPrice.value = r.data.details[0].finalPrice; reservationForm.dailyPrice = r.data.details[0].finalPrice; autoPrice.value = r.data.details[0].finalPrice; priceSource.value = 'PRICE_PLAN' }
    }
  } catch(e) { console.error('获取房价码金额失败', e) }
}

// ========== 协议单位变化处理 ==========
/** 选中协议单位：按 协议单位 + 房型 匹配协议价，优先于房价码价 */
const handleReservationCC = async (companyId) => {
  if (!companyId) {
    planDailyPrice.value = null
    autoPrice.value = null
    if (reservationForm.pricePlanId) {
      handleReservationPP(reservationForm.pricePlanId)
    } else {
      reservationForm.dailyPrice = null
      priceSource.value = 'WALKIN'
    }
    return
  }
  if (!reservationForm.roomTypeId) return
  try {
    const res = await request.get('/v1/credit-companies/' + companyId + '/agreement-prices')
    const list = res.data || []
    const match = list.find(p => p.roomTypeId === reservationForm.roomTypeId && p.status === 'ACTIVE')
    if (match && match.price) {
      autoPrice.value = match.price
      planDailyPrice.value = match.price
      reservationForm.dailyPrice = match.price
      priceSource.value = 'AGREEMENT'
    } else {
      ElMessage.warning('该协议单位暂无当前房型的有效协议价')
      planDailyPrice.value = null
      autoPrice.value = null
      if (reservationForm.pricePlanId) {
        handleReservationPP(reservationForm.pricePlanId)
      } else {
        reservationForm.dailyPrice = null
        priceSource.value = 'WALKIN'
      }
    }
  } catch (error) {
    console.error('获取协议价失败', error)
  }
}

// ========== 手动修改价格 ==========
const handleReservationPriceChange = (val) => {
  if (val === null || val === undefined) return
  if (autoPrice.value === null || autoPrice.value === undefined || Math.abs(val - autoPrice.value) > 0.001) {
    priceSource.value = 'MANUAL'
  }
}

// ========== 房价来源标签 ==========
const reservationPriceSourceLabel = computed(() => {
  const map = { AGREEMENT: '协议价', PRICE_PLAN: '房价码价', WALKIN: '门市价', MANUAL: '手工修改' }
  return map[priceSource.value] || '门市价'
})
const reservationPriceSourceTagType = computed(() => {
  const map = { AGREEMENT: 'success', PRICE_PLAN: 'warning', WALKIN: 'info', MANUAL: 'danger' }
  return map[priceSource.value] || 'info'
})'''
assert old in s, 'RESERVATION PP NOT FOUND'
s = s.replace(old, new)

# 2. submitReservation：priceSource 动态 + 带新字段（reservationForm 已含）
old = '''      const n = Math.ceil((new Date(reservationForm.checkOutDate) - new Date(reservationForm.checkInDate)) / 86400000)
      await request.post('/v1/reservations', { ...reservationForm, roomId: reservationForm.roomId, totalAmount: reservationForm.dailyPrice ? reservationForm.dailyPrice * n : null, priceSource: reservationForm.dailyPrice ? 'MANUAL' : 'PRICE_PLAN' })'''
new = '''      const n = Math.ceil((new Date(reservationForm.checkOutDate) - new Date(reservationForm.checkInDate)) / 86400000)
      await request.post('/v1/reservations', { ...reservationForm, roomId: reservationForm.roomId, totalAmount: reservationForm.dailyPrice ? reservationForm.dailyPrice * n : null, priceSource: priceSource.value })'''
assert old in s, 'SUBMIT NOT FOUND'
s = s.replace(old, new)

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('PART2 (logic) done')
