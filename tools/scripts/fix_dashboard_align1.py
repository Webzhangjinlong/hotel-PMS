# -*- coding: utf-8 -*-
"""DashboardView.vue：首页新增预订弹窗对齐预订列表弹窗
补：协议单位联动、房价来源标注、预付款/押金/支付方式、特殊要求、房价码金额联动
"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\dashboard\DashboardView.vue'
s = io.open(p, encoding='utf-8').read()

# ========== 模板 ==========
# 1. 房价码金额行后加协议单位；预定价格加 @change；预定价格后加房价来源
old = '''        <el-form-item label="房价码金额" v-if="planDailyPrice"><span class="plan-price-display">¥{{ planDailyPrice }}/晚（只读）</span></el-form-item>
        <el-form-item label="预定价格"><el-input-number v-model="reservationForm.dailyPrice" :min="0" :precision="2" style="width: 200px" /><span class="price-unit">元/晚</span></el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">'''
new = '''        <el-form-item label="房价码金额" v-if="planDailyPrice && priceSource !== 'AGREEMENT'"><span class="plan-price-display">¥{{ planDailyPrice }}/晚（只读）</span></el-form-item>
        <el-form-item label="协议单位"><el-select v-model="reservationForm.creditCompanyId" placeholder="请选择协议单位（可选）" clearable style="width: 100%" @change="handleReservationCC"><el-option v-for="item in creditCompanyOptions" :key="item.id" :label="item.companyName" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="预定价格"><el-input-number v-model="reservationForm.dailyPrice" :min="0" :precision="2" style="width: 200px" @change="handleReservationPriceChange" /><span class="price-unit">元/晚</span></el-form-item>
        <el-form-item label="房价来源"><el-tag size="small" :type="reservationPriceSourceTagType">{{ reservationPriceSourceLabel }}</el-tag></el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">'''
assert old in s, 'TEMPLATE PRICE BLOCK NOT FOUND'
s = s.replace(old, new)

# 2. 离店日期后加预付款/押金/支付方式；来源后加特殊要求
old = '''        <el-form-item label="离店日期" prop="checkOutDate"><el-date-picker v-model="reservationForm.checkOutDate" type="date" placeholder="选择离店日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="来源"><el-select v-model="reservationForm.source" style="width: 100%"><el-option label="散客" value="WALK_IN" /><el-option label="电话" value="PHONE" /><el-option label="OTA" value="OTA" /></el-select></el-form-item>'''
new = '''        <el-form-item label="离店日期" prop="checkOutDate"><el-date-picker v-model="reservationForm.checkOutDate" type="date" placeholder="选择离店日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="预付款"><el-input-number v-model="reservationForm.prepaymentAmount" :min="0" :precision="2" style="width: 180px" placeholder="可选" /><span class="price-unit">元</span></el-form-item>
        <el-form-item label="押金"><el-input-number v-model="reservationForm.depositAmount" :min="0" :precision="2" style="width: 180px" placeholder="可选" /><span class="price-unit">元</span></el-form-item>
        <el-form-item label="支付方式" v-if="reservationForm.prepaymentAmount || reservationForm.depositAmount"><el-select v-model="reservationForm.paymentMethod" style="width: 100%"><el-option label="现金" value="CASH" /><el-option label="微信" value="WECHAT" /><el-option label="支付宝" value="ALIPAY" /><el-option label="POS" value="POS" /></el-select></el-form-item>
        <el-form-item label="来源"><el-select v-model="reservationForm.source" style="width: 100%"><el-option label="散客" value="WALK_IN" /><el-option label="电话" value="PHONE" /><el-option label="OTA" value="OTA" /></el-select></el-form-item>
        <el-form-item label="特殊要求"><el-input v-model="reservationForm.specialRequests" type="textarea" :rows="3" placeholder="请输入特殊要求" /></el-form-item>'''
assert old in s, 'TEMPLATE DEPOSIT BLOCK NOT FOUND'
s = s.replace(old, new)

# ========== script ==========
# 3. refs：协议单位选项 + 房价来源
old = '''const roomTypeOptions = ref([])
const pricePlanOptions = ref([])'''
new = '''const roomTypeOptions = ref([])
const pricePlanOptions = ref([])
// 协议单位选项
const creditCompanyOptions = ref([])
// 房价来源：AGREEMENT-协议价 / PRICE_PLAN-房价码价 / WALKIN-门市价 / MANUAL-手工修改
const priceSource = ref('WALKIN')
// 自动填充参考价（判断是否手工修改）
const autoPrice = ref(null)'''
assert old in s, 'REFS BLOCK NOT FOUND'
s = s.replace(old, new)

# 4. reservationForm 加字段
old = "const reservationForm = reactive({ guestName: '', guestPhone: '', roomTypeId: null, roomId: null, pricePlanId: null, dailyPrice: null, checkInDate: '', checkOutDate: '', source: 'WALK_IN', hotelId: userStore.hotelId })"
new = "const reservationForm = reactive({ guestName: '', guestPhone: '', roomTypeId: null, roomId: null, pricePlanId: null, dailyPrice: null, checkInDate: '', checkOutDate: '', source: 'WALK_IN', creditCompanyId: null, prepaymentAmount: null, depositAmount: null, paymentMethod: 'CASH', specialRequests: '', hotelId: userStore.hotelId })"
assert old in s, 'RESERVATION FORM NOT FOUND'
s = s.replace(old, new)

# 5. loadOptions 加协议单位
old = '''async function loadOptions() {
  try {
    const [rt, pp] = await Promise.all([
      request.get('/v1/room-types', { params: { hotelId } }),
      request.get('/v1/price-plans/list', { params: { hotelId } })
    ])
    roomTypeOptions.value = rt.data?.records || []
    pricePlanOptions.value = pp.data || []
  } catch (e) { console.error(e) }
}'''
new = '''async function loadOptions() {
  try {
    const [rt, pp, cc] = await Promise.all([
      request.get('/v1/room-types', { params: { hotelId } }),
      request.get('/v1/price-plans/list', { params: { hotelId } }),
      request.get('/v1/credit-companies', { params: { page: 1, size: 200 } })
    ])
    roomTypeOptions.value = rt.data?.records || []
    pricePlanOptions.value = pp.data || []
    creditCompanyOptions.value = cc.data?.records || []
  } catch (e) { console.error(e) }
}'''
assert old in s, 'LOADOPTIONS NOT FOUND'
s = s.replace(old, new)

# 6. showReservationDialog 重置新字段
old = "  Object.assign(reservationForm, { guestName: '', guestPhone: '', roomTypeId: null, roomId: null, pricePlanId: null, dailyPrice: null, checkInDate: '', checkOutDate: '', source: 'WALK_IN' }); selectedRoomInfo.value = null; planDailyPrice.value = null"
new = "  Object.assign(reservationForm, { guestName: '', guestPhone: '', roomTypeId: null, roomId: null, pricePlanId: null, dailyPrice: null, checkInDate: '', checkOutDate: '', source: 'WALK_IN', creditCompanyId: null, prepaymentAmount: null, depositAmount: null, paymentMethod: 'CASH', specialRequests: '' }); selectedRoomInfo.value = null; planDailyPrice.value = null; autoPrice.value = null; priceSource.value = 'WALKIN'"
assert old in s, 'SHOWRESERVATION NOT FOUND'
s = s.replace(old, new)

# 7. handleReservationRoomTypeChange 联动改造
old = '''function handleReservationRoomTypeChange() {
  selectedRoomInfo.value = null
  reservationForm.roomId = null
  // 【联动】已选房价码时按新房型刷新房价码金额
  if (reservationForm.pricePlanId) {
    handleReservationPP(reservationForm.pricePlanId)
  }
}'''
new = '''function handleReservationRoomTypeChange() {
  selectedRoomInfo.value = null
  reservationForm.roomId = null
  // 【联动】协议单位优先：已选协议单位时按新房型查协议价
  if (reservationForm.creditCompanyId) {
    handleReservationCC(reservationForm.creditCompanyId)
    return
  }
  // 【联动】已选房价码时按新房型刷新房价码金额
  if (reservationForm.pricePlanId) {
    handleReservationPP(reservationForm.pricePlanId)
    return
  }
  autoPrice.value = null
  planDailyPrice.value = null
  priceSource.value = 'WALKIN'
}'''
assert old in s, 'RESERVATION ROOMTYPE NOT FOUND'
s = s.replace(old, new)

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('PART1 (template + basics) done')
