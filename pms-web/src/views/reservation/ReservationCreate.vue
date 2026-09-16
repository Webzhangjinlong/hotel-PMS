<template>
  <div class="reservation-create-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" text>
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>{{ isEdit ? '编辑预订' : '新建预订' }}</h2>
      </div>
      <div class="header-right">
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '提交预订' }}
        </el-button>
      </div>
    </div>

    <!-- 主要内容区 -->
    <el-row :gutter="20">
      <!-- 左侧：预订信息表单 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">预订信息</span>
          
    <!-- 房间选择弹窗 -->
    <RoomSelectDialog 
      v-model="showRoomDialog"
      :hotel-id="formData.hotelId"
      :room-type-id="formData.roomTypeId"
      :check-in-date="formData.checkInDate"
      :check-out-date="formData.checkOutDate"
      @select="handleRoomSelected"
    />
</template>
          <el-form 
            ref="formRef" 
            :model="formData" 
            :rules="formRules" 
            label-width="100px"
            class="reservation-form"
          >
            <!-- 客人信息 -->
            <div class="form-section">
              <div class="section-title">客人信息</div>
              <el-form-item label="客人姓名" prop="guestName">
                <el-input 
                  v-model="formData.guestName" 
                  placeholder="请输入客人姓名" 
                  maxlength="50"
                />
              </el-form-item>
              <el-form-item label="客人电话" prop="guestPhone">
                <el-input 
                  v-model="formData.guestPhone" 
                  placeholder="请输入客人电话" 
                  maxlength="20"
                />
              </el-form-item>
              <el-form-item label="身份证号">
                <el-input 
                  v-model="formData.idCardNo" 
                  placeholder="请输入身份证号（可选）" 
                  maxlength="18"
                />
              </el-form-item>
            </div>

            <!-- 房间信息 -->
            <div class="form-section">
              <div class="section-title">房间信息</div>
              <el-form-item label="房型" prop="roomTypeId">
                <el-select 
                  v-model="formData.roomTypeId" 
                  placeholder="请选择房型" 
                  style="width: 100%"
                  @change="handleRoomTypeChange"
                >
                  <el-option 
                    v-for="item in roomTypeOptions" 
                    :key="item.id" 
                    :label="item.name + ' (¥' + item.basePrice + '/晚)'" 
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="房价码">
                <el-select 
                  v-model="formData.pricePlanId" 
                  placeholder="请选择房价码（可选）" 
                  clearable 
                  style="width: 100%"
                  @change="handlePricePlanChange"
                >
                  <el-option 
                    v-for="item in pricePlanOptions" 
                    :key="item.id" 
                    :label="item.code + ' - ' + item.name" 
                    :value="item.id"
                  />
                </el-select>
                <div v-if="formData.pricePlanId && planDailyPrice" class="rate-price-hint">
                  该房价码金额：<span class="rate-price-value">¥{{ planDailyPrice }}</span>/晚
                </div>
              </el-form-item>
              <el-form-item label="排房">
                <div class="room-select-area">
                  <el-button 
                    v-if="!selectedRoomInfo" 
                    type="primary" 
                    plain 
                    :disabled="!formData.roomTypeId"
                    @click="showRoomDialog = true"
                  >
                    <el-icon><Grid /></el-icon>
                    选择房间
                  </el-button>
                  <div v-else class="selected-room-display">
                    <el-tag type="success" closable @close="clearRoomSelection">
                      {{ selectedRoomInfo.roomNo }}号房（{{ selectedRoomInfo.roomTypeName }}）- {{ selectedRoomInfo.floorName }}楼
                    </el-tag>
                    <el-button text type="primary" @click="showRoomDialog = true">更换</el-button>
                  </div>
                </div>
              </el-form-item>
            </div>

            <!-- 日期信息 -->
            <div class="form-section">
              <div class="section-title">入住信息</div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="入住日期" prop="checkInDate">
                    <el-date-picker 
                      v-model="formData.checkInDate" 
                      type="date" 
                      placeholder="选择入住日期" 
                      value-format="YYYY-MM-DD" 
                      style="width: 100%"
                      :disabled-date="disablePastDate"
                      @change="calculateNights"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="离店日期" prop="checkOutDate">
                    <el-date-picker 
                      v-model="formData.checkOutDate" 
                      type="date" 
                      placeholder="选择离店日期" 
                      value-format="YYYY-MM-DD" 
                      style="width: 100%"
                      :disabled-date="disableCheckOutDate"
                      @change="calculateNights"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="房晚数">
                <el-input-number 
                  :model-value="nights" 
                  :min="1" 
                  :max="365" 
                  disabled 
                  style="width: 120px;"
                />
                <span class="nights-hint">晚</span>
              </el-form-item>
            </div>

            <!-- 预订来源 -->
            <div class="form-section">
              <div class="section-title">其他信息</div>
              <el-form-item label="预订来源" prop="source">
                <el-select v-model="formData.source" placeholder="请选择来源" style="width: 100%">
                  <el-option label="散客" value="WALK_IN" />
                  <el-option label="电话" value="PHONE" />
                  <el-option label="OTA" value="OTA" />
                  <el-option label="协议单位" value="AGREEMENT" />
                </el-select>
              </el-form-item>
              <el-form-item label="特殊要求">
                <el-input 
                  v-model="formData.specialRequests" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请输入特殊要求（可选）" 
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：费用预览 -->
      <el-col :span="8">
        <el-card shadow="never" class="fee-card">
          <template #header>
            <span class="card-title">费用预览</span>
          </template>
          <div class="fee-preview">
            <div class="fee-item">
              <span class="fee-label">房型</span>
              <span class="fee-value">{{ selectedRoomType?.name || '-' }}</span>
            </div>
            <div class="fee-item">
              <span class="fee-label">房价码</span>
              <span class="fee-value">{{ selectedPricePlan?.code || '无' }}</span>
            </div>
            <el-divider />
            <div class="fee-item">
              <span class="fee-label">入住日期</span>
              <span class="fee-value">{{ formData.checkInDate || '-' }}</span>
            </div>
            <div class="fee-item">
              <span class="fee-label">离店日期</span>
              <span class="fee-value">{{ formData.checkOutDate || '-' }}</span>
            </div>
            <div class="fee-item">
              <span class="fee-label">房晚数</span>
              <span class="fee-value">{{ nights }} 晚</span>
            </div>
            <el-divider />
            <div class="fee-item">
              <span class="fee-label">房价码金额</span>
              <span class="fee-value plan-price">¥{{ planDailyPrice || '-' }}/晚 <span class="price-hint">（只读）</span></span>
            </div>
            <div class="fee-item">
              <span class="fee-label">预定价格</span>
              <el-input-number 
                v-model="formData.dailyPrice" 
                :min="0" 
                :precision="2" 
                size="small" 
                style="width: 140px"
                placeholder="输入预定价格"
              />
              <span class="price-unit">元/晚</span>
            </div>
            <el-divider />
            <div class="fee-item">
              <span class="fee-label">房费小计</span>
              <span class="fee-value total">¥{{ roomSubtotal.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 快捷提示 -->
        <el-card shadow="never" class="tip-card">
          <template #header>
            <span class="card-title">预订须知</span>
          </template>
          <ul class="tip-list">
            <li>入住时间通常为 14:00 后</li>
            <li>离店时间通常为 12:00 前</li>
            <li>预订后请保持电话畅通</li>
            <li>如需变更请联系前台</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>

import { useUserStore } from '@/stores/user'
/**
 * 新建预订页面
 * 提供完整的预订创建功能
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'
import RoomSelectDialog from './RoomSelectDialog.vue'
const userStore = useUserStore()

const router = useRouter()
const route = useRoute()

// ========== 表单引用 ==========
const formRef = ref(null)
const submitLoading = ref(false)

// ========== 是否编辑模式 ==========
const isEdit = ref(false)
const editId = ref(null)

// ========== 房型和房价码选项 ==========
const roomTypeOptions = ref([])
const pricePlanOptions = ref([])
const availableRoomOptions = ref([])
// 房间选择弹窗相关
const showRoomDialog = ref(false)
const selectedRoomInfo = ref(null) // 已选房间信息
const planDailyPrice = ref(null) // 房价码每日价格（只读参考价）

// ========== 表单数据 ==========
const formData = reactive({
  guestName: '',
  guestPhone: '',
  idCardNo: '',
  roomTypeId: null,
  roomId: null,
  checkInDate: '',
  checkOutDate: '',
  source: 'WALK_IN',
  pricePlanId: null,
  dailyPrice: null,
  specialRequests: '',
  hotelId: userStore.hotelId
})

// ========== 表单验证规则 ==========
const formRules = {
  guestName: [
    { required: true, message: '请输入客人姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  guestPhone: [
    { required: true, message: '请输入客人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  roomTypeId: [
    { required: true, message: '请选择房型', trigger: 'change' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  checkOutDate: [
    { required: true, message: '请选择离店日期', trigger: 'change' }
  ],
  source: [
    { required: true, message: '请选择预订来源', trigger: 'change' }
  ]
}

// ========== 计算属性 ==========

/** 计算房晚数 */
const nights = computed(() => {
  if (!formData.checkInDate || !formData.checkOutDate) return 1
  const start = new Date(formData.checkInDate)
  const end = new Date(formData.checkOutDate)
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 1
})

/** 获取选中的房型 */
const selectedRoomType = computed(() => {
  return roomTypeOptions.value.find(item => item.id === formData.roomTypeId)
})

/** 获取选中的房价码 */
const selectedPricePlan = computed(() => {
  return pricePlanOptions.value.find(item => item.id === formData.pricePlanId)
})

/** 获取房价 */
const roomPrice = computed(() => {
  if (selectedRoomType.value) {
    return selectedRoomType.value.basePrice || 0
  }
  return 0
})

/** 计算房费小计 */
const roomSubtotal = computed(() => {
  const price = formData.dailyPrice || roomPrice.value
  return price * nights.value
})

// ========== 方法 ==========

/** 返回上一页 */
const goBack = () => {
  router.back()
}

/** 禁用过去的日期 */
const disablePastDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

/** 禁用离店日期（必须晚于入住日期） */
const disableCheckOutDate = (date) => {
  if (!formData.checkInDate) return false
  const checkIn = new Date(formData.checkInDate)
  checkIn.setHours(0, 0, 0, 0)
  return date <= checkIn
}

/** 房型变化处理 */
const handleRoomTypeChange = async () => {
  formData.roomId = null
  if (formData.roomTypeId) {
    try {
      const res = await request.get('/v1/rooms', {
        params: {
          hotelId: formData.hotelId,
          roomTypeId: formData.roomTypeId,
          status: 'AVAILABLE',
          size: 100
        }
      })
      availableRoomOptions.value = res.data?.records || []
    } catch (error) {
      console.error('获取可用房间失败', error)
      availableRoomOptions.value = []
    }
  } else {
    availableRoomOptions.value = []
  }
  // 【联动】已选房价码时，按新房型刷新房价码金额
  if (formData.pricePlanId) {
    fetchPriceByPlan()
  }
}

/** 房价手动修改 */
const onDailyPriceChange = () => {}

/** 房价码变化处理 */
/**
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
}

/** 房间选择回调 */
const handleRoomSelected = (room) => {
  selectedRoomInfo.value = room
  formData.roomId = room.id
}

/** 清除房间选择 */
const clearRoomSelection = () => {
  selectedRoomInfo.value = null
  formData.roomId = null
}

/** 计算房晚数 */
const calculateNights = () => {}

/** 重置表单 */
const handleReset = () => {
  formRef.value?.resetFields()
  formData.guestName = ''
  formData.guestPhone = ''
  formData.idCardNo = ''
  formData.roomTypeId = null
  formData.roomId = null
  formData.checkInDate = ''
  formData.checkOutDate = ''
  formData.source = 'WALK_IN'
  formData.pricePlanId = null
  formData.specialRequests = ''
}

/** 提交预订 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const submitData = {
        ...formData,
        totalAmount: roomSubtotal.value,
        dailyPrice: formData.dailyPrice,
        priceSource: formData.dailyPrice ? 'MANUAL' : 'PRICE_PLAN'
      }
      
      if (isEdit.value) {
        await request.put('/v1/reservations/' + editId.value, submitData)
        ElMessage.success('预订更新成功')
      } else {
        await request.post('/v1/reservations', submitData)
        ElMessage.success('预订创建成功')
      }
      
      router.push('/reservation/list')
    } catch (error) {
      console.error('保存预订失败', error)
      ElMessage.error('保存失败，请重试')
    } finally {
      submitLoading.value = false
    }
  })
}

/** 加载房型选项 */
const fetchRoomTypeOptions = async () => {
  try {
    const res = await request.get('/v1/room-types/options', { 
      params: { hotelId: formData.hotelId } 
    })
    roomTypeOptions.value = res.data || []
  } catch (error) {
    console.error('获取房型选项失败', error)
  }
}

/** 加载房价码选项 */
const fetchPricePlanOptions = async () => {
  try {
    const res = await request.get('/v1/price-plans/list', { 
      params: { hotelId: formData.hotelId } 
    })
    pricePlanOptions.value = res.data || []
  } catch (error) {
    console.error('获取房价码选项失败:', error)
  }
}

/** 加载预订详情（编辑模式） */
const fetchReservationDetail = async (id) => {
  try {
    const res = await request.get('/v1/reservations/' + id)
    const data = res.data
    formData.guestName = data.guestName
    formData.guestPhone = data.guestPhone
    formData.idCardNo = data.idCardNo || ''
    formData.roomTypeId = data.roomTypeId
    formData.roomId = data.roomId
    formData.checkInDate = data.checkInDate
    formData.checkOutDate = data.checkOutDate
    formData.source = data.source
    formData.pricePlanId = data.pricePlanId
    formData.specialRequests = data.specialRequests || ''
  } catch (error) {
    console.error('获取预订详情失败', error)
    ElMessage.error('获取预订详情失败')
  }
}

// ========== 生命周期 ==========
onMounted(async () => {
  await Promise.allSettled([
    fetchRoomTypeOptions(),
    fetchPricePlanOptions()
  ])
  
  if (route.params.id) {
    isEdit.value = true
    editId.value = route.params.id
    await fetchReservationDetail(route.params.id)
  }
  
  if (route.query.reservationId) {
    isEdit.value = true
    editId.value = route.query.reservationId
    await fetchReservationDetail(route.query.reservationId)
  }
})
</script>

<style lang="scss" scoped>
.reservation-create-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 16px 20px;
  border-radius: 4px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .header-right {
    display: flex;
    gap: 12px;
  }
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  
  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
  }
  
  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #606266;
    margin-bottom: 16px;
  }
}

.reservation-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

.nights-hint {
  margin-left: 8px;
  color: #909399;
}

.fee-card {
  margin-bottom: 20px;
}

.fee-preview {
  .fee-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    
    .fee-label {
      color: #606266;
    }
    
    .fee-value {
      color: #303133;
      font-weight: 500;
      
      &.total {
        font-size: 18px;
        color: #f56c6c;
        font-weight: 600;
      }
    }
  }
}

.tip-card {
  .tip-list {
    margin: 0;
    padding: 0 0 0 20px;
    list-style-type: disc;
    
    li {
      color: #909399;
      font-size: 13px;
      line-height: 1.8;
    }
  }
}

// 排房选择区域样式
.room-select-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-room-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 价格显示样式
.plan-price {
  color: #909399;
  font-size: 14px;
  
  .price-hint {
    font-size: 12px;
    color: #c0c4cc;
  }
}

.price-unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}
.rate-price-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 20px;
}

.rate-price-value {
  color: #e6a23c;
  font-weight: 600;
}

</style>
