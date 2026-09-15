<template>
  <div class="quick-checkin-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" text>
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>快速入住</h2>
      </div>
      <div class="header-right">
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          <el-icon><Check /></el-icon>
          确认入住
        </el-button>
      </div>
    </div>

    <!-- 主要内容区 -->
    <el-row :gutter="20">
      <!-- 左侧：入住信息表单 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">入住信息</span>
          </template>
          <el-form 
            ref="formRef" 
            :model="formData" 
            :rules="formRules" 
            label-width="100px"
            class="checkin-form"
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
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="身份证号">
                    <el-input 
                      v-model="formData.guestIdNo" 
                      placeholder="请输入身份证号（可选）" 
                      maxlength="18"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="性别">
                    <el-select v-model="formData.guestGender" placeholder="请选择性别" style="width: 100%">
                      <el-option label="男" value="M" />
                      <el-option label="女" value="F" />
                      <el-option label="未知" value="UNKNOWN" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <!-- 房间信息 -->
            <div class="form-section">
              <div class="section-title">房间信息</div>
              <el-row :gutter="20">
                <el-col :span="12">
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
                </el-col>
                <el-col :span="12">
                  <el-form-item label="房间号" prop="roomId">
                    <el-select 
                      v-model="formData.roomId" 
                      placeholder="请选择房间" 
                      style="width: 100%"
                      :loading="roomLoading"
                      :disabled="!formData.roomTypeId"
                    >
                      <el-option 
                        v-for="item in availableRooms" 
                        :key="item.id" 
                        :label="item.roomNo + ' (' + item.roomTypeName + ')'" 
                        :value="item.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="房价码">
                <el-select 
                  v-model="formData.pricePlanId" 
                  placeholder="请选择房价码（可选）" 
                  clearable 
                  style="width: 100%"
                >
                  <el-option 
                    v-for="item in pricePlanOptions" 
                    :key="item.id" 
                    :label="item.code + ' - ' + item.name" 
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </div>

            <!-- 入住信息 -->
            <div class="form-section">
              <div class="section-title">入住信息</div>
              <el-form-item label="预计离店" prop="expectedCheckOutDate">
                <el-date-picker 
                  v-model="formData.expectedCheckOutDate" 
                  type="date" 
                  placeholder="选择预计离店日期" 
                  value-format="YYYY-MM-DD" 
                  style="width: 100%"
                  :disabled-date="disablePastDate"
                />
              </el-form-item>
              <el-form-item label="房晚数">
                <el-input-number 
                  v-model="nights" 
                  :min="1" 
                  :max="365" 
                  disabled 
                  style="width: 120px;"
                />
                <span class="nights-hint">晚</span>
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
              <span class="fee-label">房间号</span>
              <span class="fee-value">{{ selectedRoom?.roomNo || '-' }}</span>
            </div>
            <el-divider />
            <div class="fee-item">
              <span class="fee-label">入住日期</span>
              <span class="fee-value">今天</span>
            </div>
            <div class="fee-item">
              <span class="fee-label">离店日期</span>
              <span class="fee-value">{{ formData.expectedCheckOutDate || '-' }}</span>
            </div>
            <div class="fee-item">
              <span class="fee-label">房晚数</span>
              <span class="fee-value">{{ nights }} 晚</span>
            </div>
            <el-divider />
            <div class="fee-item">
              <span class="fee-label">房价</span>
              <span class="fee-value">¥{{ roomPrice }}/晚</span>
            </div>
            <div class="fee-item">
              <span class="fee-label">房费小计</span>
              <span class="fee-value total">¥{{ roomSubtotal.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 快捷提示 -->
        <el-card shadow="never" class="tip-card">
          <template #header>
            <span class="card-title">入住须知</span>
          </template>
          <ul class="tip-list">
            <li>入住时间通常为 14:00 后</li>
            <li>离店时间通常为 12:00 前</li>
            <li>请核实客人身份证信息</li>
            <li>押金可在入住后收取</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * 快速入住页面
 * <p>
 * 提供快速入住功能，支持：
 * - 散客直接入住
 * - 选择房型和房间
 * - 费用预览
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import request from '@/utils/request'

// ========== 路由和状态 ==========
const router = useRouter()
const route = useRoute()

// ========== 表单引用 ==========
const formRef = ref(null)
const submitLoading = ref(false)

// ========== 房型和房间选项 ==========
const roomTypeOptions = ref([])
const pricePlanOptions = ref([])
const availableRooms = ref([])
const roomLoading = ref(false)

// ========== 表单数据 ==========
/**
 * 入住表单数据
 */
const formData = reactive({
  hotelId: 1,
  guestName: '',
  guestPhone: '',
  guestIdNo: '',
  guestGender: 'UNKNOWN',
  roomTypeId: null,
  roomId: null,
  expectedCheckOutDate: '',
  pricePlanId: null
})

// ========== 表单验证规则 ==========
/**
 * 表单字段验证规则
 */
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
  roomId: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ],
  expectedCheckOutDate: [
    { required: true, message: '请选择预计离店日期', trigger: 'change' }
  ]
}

// ========== 计算属性 ==========

/**
 * 计算房晚数
 * @returns {number} 房晚数
 */
const nights = computed(() => {
  if (!formData.expectedCheckOutDate) return 1
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const checkOut = new Date(formData.expectedCheckOutDate)
  const diff = Math.ceil((checkOut - today) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 1
})

/**
 * 获取选中的房型
 */
const selectedRoomType = computed(() => {
  return roomTypeOptions.value.find(item => item.id === formData.roomTypeId)
})

/**
 * 获取选中的房间
 */
const selectedRoom = computed(() => {
  return availableRooms.value.find(item => item.id === formData.roomId)
})

/**
 * 获取房价
 * @returns {number} 房价
 */
const roomPrice = computed(() => {
  if (selectedRoomType.value) {
    return selectedRoomType.value.basePrice || 0
  }
  return 0
})

/**
 * 计算房费小计
 * @returns {number} 房费小计
 */
const roomSubtotal = computed(() => {
  return roomPrice.value * nights.value
})

// ========== 方法 ==========

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

/**
 * 禁用过去的日期
 * @param {Date} date - 日期对象
 * @returns {boolean} 是否禁用
 */
const disablePastDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

/**
 * 房型变化处理
 * 当房型变化时，加载可用房间列表
 */
const handleRoomTypeChange = async () => {
  // 清空已选房间
  formData.roomId = null
  
  if (!formData.roomTypeId) {
    availableRooms.value = []
    return
  }
  
  // 加载该房型下的空闲房间
  roomLoading.value = true
  try {
    const res = await request.get('/v1/rooms', {
      params: {
        hotelId: formData.hotelId,
        roomTypeId: formData.roomTypeId,
        status: 'AVAILABLE',
        size: 100
      }
    })
    availableRooms.value = res.data?.records || []
  } catch (error) {
    console.error('获取可用房间失败', error)
    ElMessage.error('获取可用房间失败')
    availableRooms.value = []
  } finally {
    roomLoading.value = false
  }
}

/**
 * 重置表单
 */
const handleReset = () => {
  formRef.value?.resetFields()
  formData.guestName = ''
  formData.guestPhone = ''
  formData.guestIdNo = ''
  formData.guestGender = 'UNKNOWN'
  formData.roomTypeId = null
  formData.roomId = null
  formData.expectedCheckOutDate = ''
  formData.pricePlanId = null
  availableRooms.value = []
}

/**
 * 提交入住
 * <p>
 * 调用后端API完成入住操作：
 * 1. 验证表单
 * 2. 创建入住单
 * 3. 更新房间状态
 * 4. 创建账务单
 * </p>
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  // 表单验证
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      // 调用散客入住API
      const res = await request.post('/v1/stays/walk-in', formData)
      
      ElMessage.success('入住成功')
      
      // 显示入住成功信息
      const stayData = res.data
      ElMessage({
        message: `入住单号：${stayData.stayNo}，房间：${stayData.roomNo}，房费：¥${stayData.totalAmount}`,
        type: 'success',
        duration: 5000
      })
      
      // 返回列表页
      router.push('/stays')
    } catch (error) {
      console.error('入住失败', error)
      ElMessage.error('入住失败：' + (error.message || '请重试'))
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 加载房型选项
 * 获取酒店所有可用房型
 */
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

/**
 * 加载房价码选项
 * 获取酒店所有房价码
 */
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

/**
 * 设置默认离店日期
 * 默认为明天
 */
const setDefaultCheckOutDate = () => {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  formData.expectedCheckOutDate = tomorrow.toISOString().split('T')[0]
}

// ========== 生命周期 ==========
onMounted(async () => {
  // 加载基础数据
  await Promise.all([
    fetchRoomTypeOptions(),
    fetchPricePlanOptions()
  ])
  
  // 设置默认离店日期
  setDefaultCheckOutDate()
  
  // 如果从预订入住跳转过来，预填信息
  if (route.query.reservationId) {
    // TODO: 加载预订信息并填充表单
  }
})
</script>

<style lang="scss" scoped>
.quick-checkin-container {
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

.checkin-form {
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
</style>
