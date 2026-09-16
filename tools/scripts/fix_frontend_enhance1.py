# -*- coding: utf-8 -*-
"""ReservationList.vue：新增预订弹窗增强
1. 房价来源标注（协议价/房价码价/门市价/手工修改）
2. 协议单位联动（可选）：选中协议单位→按房型匹配协议价
3. 预付款/押金（可选）+ 支付方式
"""
import io

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reservation\ReservationList.vue'
s = io.open(p, encoding='utf-8').read()

# ========== 模板 ==========
# 1. 房价码价格行后加 协议单位 + 预定价格后加 房价来源
old = '''        <el-form-item label="房价码价格" v-if="planDailyPrice">
          <span class="plan-price-display">¥{{ planDailyPrice }}/晚（只读）</span>
        </el-form-item>
        <el-form-item label="预定价格">
          <el-input-number v-model="formData.dailyPrice" :min="0" :precision="2" placeholder="可修改" style="width: 200px" />
          <span class="price-unit">元/晚</span>
        </el-form-item>'''
new = '''        <el-form-item label="房价码价格" v-if="planDailyPrice && priceSource !== 'AGREEMENT'">
          <span class="plan-price-display">¥{{ planDailyPrice }}/晚（只读）</span>
        </el-form-item>
        <el-form-item label="协议单位">
          <el-select v-model="formData.creditCompanyId" placeholder="请选择协议单位（可选）" clearable style="width: 100%" @change="handleCreditCompanyChange">
            <el-option v-for="item in creditCompanyOptions" :key="item.id" :label="item.companyName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预定价格">
          <el-input-number v-model="formData.dailyPrice" :min="0" :precision="2" placeholder="可修改" style="width: 200px" @change="handleDailyPriceChange" />
          <span class="price-unit">元/晚</span>
        </el-form-item>
        <el-form-item label="房价来源">
          <el-tag size="small" :type="priceSourceTagType">{{ priceSourceLabel }}</el-tag>
        </el-form-item>'''
assert old in s, 'TEMPLATE PRICE BLOCK NOT FOUND'
s = s.replace(old, new)

# 2. 离店日期后加 预付款/押金/支付方式
old = '''        <el-form-item label="离店日期" prop="checkOutDate">
          <el-date-picker v-model="formData.checkOutDate" type="date" placeholder="选择离店日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="来源" prop="source">'''
new = '''        <el-form-item label="离店日期" prop="checkOutDate">
          <el-date-picker v-model="formData.checkOutDate" type="date" placeholder="选择离店日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预付款">
          <el-input-number v-model="formData.prepaymentAmount" :min="0" :precision="2" style="width: 180px" placeholder="可选" />
          <span class="price-unit">元</span>
        </el-form-item>
        <el-form-item label="押金">
          <el-input-number v-model="formData.depositAmount" :min="0" :precision="2" style="width: 180px" placeholder="可选" />
          <span class="price-unit">元</span>
        </el-form-item>
        <el-form-item label="支付方式" v-if="formData.prepaymentAmount || formData.depositAmount">
          <el-select v-model="formData.paymentMethod" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="POS" value="POS" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源" prop="source">'''
assert old in s, 'TEMPLATE DEPOSIT BLOCK NOT FOUND'
s = s.replace(old, new)

# ========== script ==========
# 3. refs
old = '''const planDailyPrice = ref(null)

// ========== 弹窗相关 =========='''
new = '''const planDailyPrice = ref(null)
// 协议单位选项
const creditCompanyOptions = ref([])
// 房价来源：AGREEMENT-协议价 / PRICE_PLAN-房价码价 / WALKIN-门市价 / MANUAL-手工修改
const priceSource = ref('WALKIN')
// 自动填充的参考价（用于判断是否手工修改）
const autoPrice = ref(null)

// ========== 弹窗相关 =========='''
assert old in s, 'REFS BLOCK NOT FOUND'
s = s.replace(old, new)

# 4. formData
old = '''  pricePlanId: null,
  dailyPrice: null,
  specialRequests: ''
})'''
new = '''  pricePlanId: null,
  dailyPrice: null,
  specialRequests: '',
  creditCompanyId: null,
  prepaymentAmount: null,
  depositAmount: null,
  paymentMethod: 'CASH'
})'''
assert old in s, 'FORMDATA BLOCK NOT FOUND'
s = s.replace(old, new)

# 5. 房价来源标签 computed（插到 handleAdd 前）
old = '''// ========== 新增 ==========
const handleAdd = () => {'''
new = '''// ========== 房价来源标签 ==========
const priceSourceLabel = computed(() => {
  const map = { AGREEMENT: '协议价', PRICE_PLAN: '房价码价', WALKIN: '门市价', MANUAL: '手工修改' }
  return map[priceSource.value] || '门市价'
})
const priceSourceTagType = computed(() => {
  const map = { AGREEMENT: 'success', PRICE_PLAN: 'warning', WALKIN: 'info', MANUAL: 'danger' }
  return map[priceSource.value] || 'info'
})

// ========== 新增 ==========
const handleAdd = () => {'''
assert old in s, 'HANDLEADD BLOCK NOT FOUND'
s = s.replace(old, new)

# 6. handleAdd 重置
old = '''  formData.pricePlanId = null
  formData.dailyPrice = null
  formData.specialRequests = ''
  dialogVisible.value = true
}'''
new = '''  formData.pricePlanId = null
  formData.dailyPrice = null
  formData.specialRequests = ''
  formData.creditCompanyId = null
  formData.prepaymentAmount = null
  formData.depositAmount = null
  formData.paymentMethod = 'CASH'
  planDailyPrice.value = null
  autoPrice.value = null
  priceSource.value = 'WALKIN'
  dialogVisible.value = true
}'''
assert old in s, 'HANDLEADD RESET NOT FOUND'
s = s.replace(old, new)

# 7. handleEdit 填充
old = '''  formData.pricePlanId = row.pricePlanId || null
  formData.dailyPrice = row.dailyPrice || null
  formData.specialRequests = row.specialRequests
  dialogVisible.value = true
}'''
new = '''  formData.pricePlanId = row.pricePlanId || null
  formData.dailyPrice = row.dailyPrice || null
  formData.specialRequests = row.specialRequests
  formData.creditCompanyId = row.creditCompanyId || null
  formData.prepaymentAmount = null
  formData.depositAmount = null
  formData.paymentMethod = 'CASH'
  planDailyPrice.value = null
  autoPrice.value = null
  priceSource.value = 'WALKIN'
  dialogVisible.value = true
}'''
assert old in s, 'HANDLEEDIT RESET NOT FOUND'
s = s.replace(old, new)

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('PART1 (template+refs+formdata+handlers) done')
