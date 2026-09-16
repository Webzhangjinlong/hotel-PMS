# -*- coding: utf-8 -*-
"""ReservationList.vue Part2：script 逻辑（房价来源/协议联动/手动改价）"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reservation\ReservationList.vue'
s = io.open(p, encoding='utf-8').read()

# 1. handleRoomTypeChange 改造：协议单位优先
old = '''const handleRoomTypeChange = () => {
  selectedRoomInfo.value = null
  formData.roomId = null
  // 【联动】已选房价码时按新房型刷新房价码金额
  if (formData.pricePlanId) {
    handlePricePlanChange(formData.pricePlanId)
  }
}'''
new = '''const handleRoomTypeChange = () => {
  selectedRoomInfo.value = null
  formData.roomId = null
  // 【联动】协议单位优先：已选协议单位时按新房型查协议价
  if (formData.creditCompanyId) {
    handleCreditCompanyChange(formData.creditCompanyId)
    return
  }
  // 【联动】已选房价码时按新房型刷新房价码金额
  if (formData.pricePlanId) {
    handlePricePlanChange(formData.pricePlanId)
    return
  }
  autoPrice.value = null
  planDailyPrice.value = null
  priceSource.value = 'WALKIN'
}'''
assert old in s, 'ROOMTYPE HANDLER NOT FOUND'
s = s.replace(old, new)

# 2. handlePricePlanChange 改造
old = '''const handlePricePlanChange = async (planId) => {
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
new = '''const handlePricePlanChange = async (planId) => {
  if (!planId) {
    planDailyPrice.value = null
    formData.dailyPrice = null
    autoPrice.value = null
    priceSource.value = 'WALKIN'
    // 若已选协议单位，优先显示协议价
    if (formData.creditCompanyId) {
      handleCreditCompanyChange(formData.creditCompanyId)
    }
    return
  }
  // 协议单位优先：已选协议单位时房价码不覆盖协议价
  if (formData.creditCompanyId) {
    handleCreditCompanyChange(formData.creditCompanyId)
    return
  }
  try {
    if (formData.roomTypeId) {
      const res = await request.get('/v1/prices/query', { params: { hotelId: queryParams.hotelId, roomTypeId: formData.roomTypeId, date: new Date().toISOString().split('T')[0], pricePlanId: planId } })
      if (res.data?.price) {
        planDailyPrice.value = res.data.price
        formData.dailyPrice = res.data.price
        autoPrice.value = res.data.price
        priceSource.value = 'PRICE_PLAN'
      }
    } else {
      const planRes = await request.get('/v1/price-plans/' + planId)
      if (planRes.data?.details && planRes.data.details.length > 0) {
        planDailyPrice.value = planRes.data.details[0].finalPrice
        formData.dailyPrice = planRes.data.details[0].finalPrice
        autoPrice.value = planRes.data.details[0].finalPrice
        priceSource.value = 'PRICE_PLAN'
      }
    }
  } catch (error) {
    console.error('获取房价码金额失败', error)
  }
}

// ========== 协议单位变化处理 ==========
/**
 * 选中协议单位：按 协议单位 + 房型 匹配协议价（agreement_price），匹配则房价=协议价
 */
const handleCreditCompanyChange = async (companyId) => {
  if (!companyId) {
    // 清除协议单位：恢复房价码/门市价
    planDailyPrice.value = null
    autoPrice.value = null
    if (formData.pricePlanId) {
      handlePricePlanChange(formData.pricePlanId)
    } else {
      formData.dailyPrice = null
      priceSource.value = 'WALKIN'
    }
    return
  }
  if (!formData.roomTypeId) return
  try {
    const res = await request.get('/v1/credit-companies/' + companyId + '/agreement-prices')
    const list = res.data || []
    const match = list.find(p => p.roomTypeId === formData.roomTypeId && p.status === 'ACTIVE')
    if (match && match.price) {
      autoPrice.value = match.price
      planDailyPrice.value = match.price
      formData.dailyPrice = match.price
      priceSource.value = 'AGREEMENT'
    } else {
      ElMessage.warning('该协议单位暂无当前房型的有效协议价')
      planDailyPrice.value = null
      autoPrice.value = null
      if (formData.pricePlanId) {
        handlePricePlanChange(formData.pricePlanId)
      } else {
        formData.dailyPrice = null
        priceSource.value = 'WALKIN'
      }
    }
  } catch (error) {
    console.error('获取协议价失败', error)
  }
}

// ========== 手动修改价格 ==========
const handleDailyPriceChange = (val) => {
  if (val === null || val === undefined) return
  if (autoPrice.value === null || autoPrice.value === undefined || Math.abs(val - autoPrice.value) > 0.001) {
    priceSource.value = 'MANUAL'
  }
}'''
assert old in s, 'PRICEPLAN HANDLER NOT FOUND'
s = s.replace(old, new)

# 3. fetchCreditCompanies（插在 fetchPricePlanOptions 前）
old = '''// ========== 获取房价码选项 ==========
const fetchPricePlanOptions = async () => {'''
new = '''// ========== 获取协议单位选项 ==========
const fetchCreditCompanies = async () => {
  try {
    const res = await request.get('/v1/credit-companies', { params: { page: 1, size: 200 } })
    creditCompanyOptions.value = res.data?.records || []
  } catch (error) {
    console.error('获取协议单位失败:', error)
  }
}

// ========== 获取房价码选项 ==========
const fetchPricePlanOptions = async () => {'''
assert old in s, 'FETCH PRICEPLAN NOT FOUND'
s = s.replace(old, new)

# 4. onMounted 加载协议单位
old = '''onMounted(() => {
  fetchData()
  fetchRoomTypeOptions()
  fetchPricePlanOptions()
  fetchTodayArrivalsCount()
})'''
new = '''onMounted(() => {
  fetchData()
  fetchRoomTypeOptions()
  fetchPricePlanOptions()
  fetchCreditCompanies()
  fetchTodayArrivalsCount()
})'''
assert old in s, 'ONMOUNTED NOT FOUND'
s = s.replace(old, new)

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('PART2 (script logic) done')
