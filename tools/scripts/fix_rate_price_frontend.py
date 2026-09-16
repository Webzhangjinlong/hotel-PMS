# -*- coding: utf-8 -*-
"""前端 ReservationCreate.vue：
1. fetchPriceByPlan 方法（/v1/prices/query 传 pricePlanId 查房价码金额）
2. handlePricePlanChange 改为调用 fetchPriceByPlan
3. handleRoomTypeChange 联动刷新房价码金额
4. 房价码下拉下方显示"该房价码金额"
5. 费用预览标签"房价码价格"→"房价码金额"
"""
import io
import re

p = r'D:\codex\hotelPMS_doubao\hotel-PMS\pms-web\src\views\reservation\ReservationCreate.vue'
s = io.open(p, encoding='utf-8').read()

# ---------- 1+2. 替换 handlePricePlanChange 为 fetchPriceByPlan + 调用 ----------
old = r'''const handlePricePlanChange = async () => {
  if (formData.pricePlanId && formData.roomTypeId) {
    try {
      const res = await request.get('/v1/prices/query', { params: { hotelId: userStore.hotelId, roomTypeId: formData.roomTypeId, date: new Date().toISOString().split('T')[0] } })
      if (res.data?.price) {
        planDailyPrice.value = res.data.price // 房价码价格（只读参考价）
        formData.dailyPrice = res.data.price // 预定价格（初始值=房价码价格）
      }
    } catch (error) {
      console.error('获取房价失败', error)
    }
  } else if (formData.pricePlanId) {
    try {
      const planRes = await request.get('/v1/price-plans/' + formData.pricePlanId)
      if (planRes.data?.details && planRes.data.details.length > 0) {
        planDailyPrice.value = planRes.data.details[0].finalPrice
        formData.dailyPrice = planRes.data.details[0].finalPrice
      }
    } catch (error) {
      console.error('获取房价失败', error)
    }
  } else {
    planDailyPrice.value = null
    formData.dailyPrice = null
  }
}'''
new = r'''/**
 * 按 房价码 + 房型 查询房价码金额（/v1/prices/query 支持 pricePlanId）
 * 有房价码无房型时等待房型联动（handleRoomTypeChange）
 */
const fetchPriceByPlan = async () => {
  if (!formData.pricePlanId) {
    planDailyPrice.value = null
    formData.dailyPrice = null
    return
  }
  if (!formData.roomTypeId) return
  try {
    const res = await request.get('/v1/prices/query', {
      params: {
        hotelId: userStore.hotelId,
        roomTypeId: formData.roomTypeId,
        date: new Date().toISOString().split('T')[0],
        pricePlanId: formData.pricePlanId
      }
    })
    if (res.data?.price) {
      planDailyPrice.value = res.data.price // 房价码金额（只读参考价）
      formData.dailyPrice = res.data.price // 预定价格（初始值=房价码金额）
    }
  } catch (error) {
    console.error('获取房价码金额失败', error)
  }
}

/** 房价码变化处理 */
const handlePricePlanChange = () => {
  fetchPriceByPlan()
}'''
assert old in s, 'HANDLEPRICEPLANCHANGE NOT FOUND'
s = s.replace(old, new)
print('1+2. fetchPriceByPlan done')

# ---------- 3. handleRoomTypeChange 联动刷新 ----------
old = '''  } else {
    availableRoomOptions.value = []
  }
}

/** 房价手动修改 */'''
new = '''  } else {
    availableRoomOptions.value = []
  }
  // 【联动】已选房价码时，按新房型刷新房价码金额
  if (formData.pricePlanId) {
    fetchPriceByPlan()
  }
}

/** 房价手动修改 */'''
assert old in s, 'ROOMTYPECHANGE TAIL NOT FOUND'
s = s.replace(old, new)
print('3. room type linkage done')

# ---------- 4. 房价码下拉下方显示金额提示 ----------
old = '''                  <el-option 
                    v-for="item in pricePlanOptions" 
                    :key="item.id" 
                    :label="item.code + ' - ' + item.name" 
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>'''
new = '''                  <el-option 
                    v-for="item in pricePlanOptions" 
                    :key="item.id" 
                    :label="item.code + ' - ' + item.name" 
                    :value="item.id"
                  />
                </el-select>
                <div v-if="formData.pricePlanId && planDailyPrice" class="rate-price-hint">
                  该房价码金额：<span class="rate-price-value">¥{{ planDailyPrice }}</span>/晚
                </div>
              </el-form-item>'''
assert old in s, 'PRICEPLAN FORM-ITEM NOT FOUND'
s = s.replace(old, new)
print('4. rate price hint done')

# ---------- 5. 费用预览标签 ----------
old = '''              <span class="fee-label">房价码价格</span>'''
new = '''              <span class="fee-label">房价码金额</span>'''
assert old in s, 'FEE LABEL NOT FOUND'
s = s.replace(old, new)
print('5. fee label done')

# ---------- 6. 样式 ----------
if '.rate-price-hint' not in s:
    old = '''</style>'''
    new = '''.rate-price-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 20px;
}

.rate-price-value {
  color: #e6a23c;
  font-weight: 600;
}

</style>'''
    assert old in s, 'STYLE TAIL NOT FOUND'
    s = s.replace(old, new, 1)
    print('6. styles done')

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('ReservationCreate.vue updated')
