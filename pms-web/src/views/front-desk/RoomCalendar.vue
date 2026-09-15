<template>
  <!-- 房间预订日历页面 -->
  <div class="room-calendar-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>房间预订日历</h2>
      <div class="header-actions">
        <el-button @click="loadCalendarData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-date-picker
        v-model="startDate"
        type="date"
        placeholder="选择开始日期"
        value-format="YYYY-MM-DD"
        :disabled-date="disablePastDate"
        @change="loadCalendarData"
        style="width: 160px;"
      />
      <el-select v-model="days" placeholder="选择天数" style="width: 120px;" @change="loadCalendarData">
        <el-option label="7天" :value="7" />
        <el-option label="14天" :value="14" />
        <el-option label="21天" :value="21" />
        <el-option label="30天" :value="30" />
      </el-select>
      <el-select v-model="filterFloor" placeholder="选择楼层" clearable style="width: 140px;" @change="loadCalendarData">
        <el-option v-for="floor in floorOptions" :key="floor.id" :label="floor.name" :value="floor.id" />
      </el-select>
      <el-select v-model="filterRoomType" placeholder="选择房型" clearable style="width: 160px;" @change="loadCalendarData">
        <el-option v-for="rt in roomTypeOptions" :key="rt.id" :label="rt.name" :value="rt.id" />
      </el-select>
    </div>

    <!-- 图例 -->
    <div class="legend-bar">
      <span class="legend-item"><span class="legend-dot available"></span>空闲</span>
      <span class="legend-item"><span class="legend-dot reserved"></span>已预订</span>
      <span class="legend-item"><span class="legend-dot occupied"></span>已入住</span>
      <span class="legend-item"><span class="legend-dot maintenance"></span>维修</span>
      <span class="legend-item"><span class="legend-dot dirty"></span>脏房</span>
    </div>

    <!-- 日历表格 -->
    <div class="calendar-wrapper" v-loading="loading">
      <table class="calendar-table" v-if="calendarData && calendarData.rooms && calendarData.rooms.length > 0">
        <!-- 表头：日期列 -->
        <thead>
          <tr>
            <th class="room-header">房间</th>
            <th class="room-type-header">房型</th>
            <th 
              v-for="date in calendarData.dates" 
              :key="date" 
              class="date-header"
              :class="{ 'today': isToday(date), 'weekend': isWeekend(date) }"
            >
              <div class="date-main">{{ formatShortDate(date) }}</div>
              <div class="date-weekday">{{ getWeekday(date) }}</div>
            </th>
          </tr>
        </thead>
        <!-- 表体：房间行 -->
        <tbody>
          <tr v-for="room in calendarData.rooms" :key="room.roomId" class="room-row">
            <td class="room-no-cell">
              <div class="room-no">{{ room.roomNo }}</div>
              <div class="floor-name">{{ room.floorName }}</div>
            </td>
            <td class="room-type-cell">{{ room.roomTypeName }}</td>
            <td 
              v-for="date in calendarData.dates" 
              :key="date" 
              class="calendar-cell"
              :class="getCellClass(room, date)"
              @click="handleCellClick(room, date)"
              @mouseenter="showTooltip($event, room, date)"
              @mouseleave="hideTooltip"
            >
              <div class="cell-content" v-if="room.calendar && room.calendar[date]">
                <span class="cell-guest" v-if="room.calendar[date].guestName">
                  {{ room.calendar[date].guestName }}
                </span>
                <span class="cell-status" v-else>
                  {{ getStatusLabel(room.calendar[date].status) }}
                </span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 空数据提示 -->
      <el-empty v-else-if="!loading" description="暂无房间数据" />
    </div>

    <!-- 悬浮提示 -->
    <div 
      v-if="tooltipVisible" 
      class="cell-tooltip" 
      :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }"
    >
      <div class="tooltip-title">{{ tooltip.roomNo }} - {{ tooltip.date }}</div>
      <div class="tooltip-status">状态：{{ getStatusLabel(tooltip.status) }}</div>
      <div class="tooltip-guest" v-if="tooltip.guestName">客人：{{ tooltip.guestName }}</div>
      <div class="tooltip-hint" v-if="tooltip.status === 'AVAILABLE'">点击可快速预订</div>
      <div class="tooltip-hint" v-if="tooltip.status === 'RESERVED'">点击查看详情</div>
    </div>

    <!-- 快速预订弹窗 -->
    <el-dialog v-model="quickBookingVisible" title="快速预订" width="500px" :close-on-click-modal="false">
      <el-form :model="quickBookingForm" :rules="quickBookingRules" ref="quickBookingFormRef" label-width="100px">
        <el-form-item label="房间">
          <el-tag type="info">{{ quickBookingForm.roomNo }} ({{ quickBookingForm.roomTypeName }})</el-tag>
        </el-form-item>
        <el-form-item label="入住日期">
          <el-tag>{{ quickBookingForm.checkInDate }}</el-tag>
        </el-form-item>
        <el-form-item label="离店日期" prop="checkOutDate">
          <el-date-picker 
            v-model="quickBookingForm.checkOutDate" 
            type="date" 
            placeholder="选择离店日期" 
            value-format="YYYY-MM-DD" 
            style="width: 100%"
            :disabled-date="(date) => date <= new Date(quickBookingForm.checkInDate)"
          />
        </el-form-item>
        <el-form-item label="客人姓名" prop="guestName">
          <el-input v-model="quickBookingForm.guestName" placeholder="请输入客人姓名" />
        </el-form-item>
        <el-form-item label="客人电话" prop="guestPhone">
          <el-input v-model="quickBookingForm.guestPhone" placeholder="请输入客人电话" />
        </el-form-item>
        <el-form-item label="房价码">
          <el-select v-model="quickBookingForm.pricePlanId" placeholder="选择房价码（可选）" clearable style="width: 100%" @change="handleQuickBookingPPChange">
            <el-option v-for="plan in pricePlanOptions" :key="plan.id" :label="plan.code + ' - ' + plan.name" :value="plan.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房价码价格" v-if="planDailyPrice">
          <span class="plan-price-display">¥{{ planDailyPrice }}/晚（只读）</span>
        </el-form-item>
        <el-form-item label="预定价格">
          <el-input-number v-model="quickBookingForm.dailyPrice" :min="0" :precision="2" style="width: 200px" />
          <span class="price-unit">元/晚</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quickBookingVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitQuickBooking">确认预订</el-button>
      </template>
    </el-dialog>

    <!-- 预订详情弹窗 -->
    <el-dialog v-model="reservationDetailVisible" title="预订详情" width="500px">
      <el-descriptions :column="1" border v-if="currentReservation">
        <el-descriptions-item label="预订号">{{ currentReservation.reservationNo }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ currentReservation.guestName }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ currentReservation.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="离店日期">{{ currentReservation.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getReservationStatusType(currentReservation.status)">
            {{ getReservationStatusLabel(currentReservation.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentReservation.totalAmount }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="reservationDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 房间预订日历组件
 * 展示N天内房间的预订和排房情况，支持快速预订和冲突检查
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

// ========== 状态变量 ==========

/** 加载状态 */
const loading = ref(false)

/** 提交状态 */
const submitLoading = ref(false)

/** 开始日期 */
const startDate = ref(new Date().toISOString().split('T')[0])

/** 显示天数 */
const days = ref(7)

/** 楼层筛选 */
const filterFloor = ref(null)

/** 房型筛选 */
const filterRoomType = ref(null)

/** 日历数据 */
const calendarData = ref(null)

/** 楼层选项 */
const floorOptions = ref([])

/** 房型选项 */
const roomTypeOptions = ref([])

/** 房价码选项 */
const pricePlanOptions = ref([])
/** 房价码每日价格（只读参考价） */
const planDailyPrice = ref(null)

// ========== 悬浮提示相关 ==========

/** 提示是否显示 */
const tooltipVisible = ref(false)

/** 提示信息 */
const tooltip = reactive({
  x: 0,
  y: 0,
  roomNo: '',
  date: '',
  status: '',
  guestName: ''
})

// ========== 快速预订相关 ==========

/** 快速预订弹窗显示状态 */
const quickBookingVisible = ref(false)

/** 快速预订表单引用 */
const quickBookingFormRef = ref(null)

/** 快速预订表单数据 */
const quickBookingForm = reactive({
  roomId: null,
  roomNo: '',
  roomTypeName: '',
  roomTypeId: null,
  checkInDate: '',
  checkOutDate: '',
  guestName: '',
  guestPhone: '',
  pricePlanId: null,
  dailyPrice: null,
  hotelId: 1
})

/** 快速预订表单验证规则 */
const quickBookingRules = {
  checkOutDate: [{ required: true, message: '请选择离店日期', trigger: 'change' }],
  guestName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入客人电话', trigger: 'blur' }]
}

// ========== 预订详情相关 ==========

/** 预订详情弹窗显示状态 */
const reservationDetailVisible = ref(false)

/** 当前查看的预订 */
const currentReservation = ref(null)

// ========== 方法 ==========

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
 * 判断是否是今天
 * @param {string} dateStr - 日期字符串
 * @returns {boolean} 是否是今天
 */
const isToday = (dateStr) => {
  const today = new Date().toISOString().split('T')[0]
  return dateStr === today
}

/**
 * 判断是否是周末
 * @param {string} dateStr - 日期字符串
 * @returns {boolean} 是否是周末
 */
const isWeekend = (dateStr) => {
  const date = new Date(dateStr)
  const day = date.getDay()
  return day === 0 || day === 6
}

/**
 * 格式化短日期
 * @param {string} dateStr - 日期字符串
 * @returns {string} 格式化后的日期
 */
const formatShortDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

/**
 * 获取星期几
 * @param {string} dateStr - 日期字符串
 * @returns {string} 星期几
 */
const getWeekday = (dateStr) => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const date = new Date(dateStr)
  return weekdays[date.getDay()]
}

/**
 * 获取状态标签
 * @param {string} status - 状态码
 * @returns {string} 状态标签
 */
const getStatusLabel = (status) => {
  const statusMap = {
    'AVAILABLE': '空闲',
    'RESERVED': '已预订',
    'OCCUPIED': '已入住',
    'DIRTY': '脏房',
    'MAINTENANCE': '维修',
    'OOO': '停用'
  }
  return statusMap[status] || status
}

/**
 * 获取单元格CSS类名
 * @param {Object} room - 房间对象
 * @param {string} date - 日期
 * @returns {string} CSS类名
 */
const getCellClass = (room, date) => {
  if (!room.calendar || !room.calendar[date]) return ''
  const status = room.calendar[date].status
  const classMap = {
    'AVAILABLE': 'cell-available',
    'RESERVED': 'cell-reserved',
    'OCCUPIED': 'cell-occupied',
    'DIRTY': 'cell-dirty',
    'MAINTENANCE': 'cell-maintenance',
    'OOO': 'cell-ooo'
  }
  return classMap[status] || ''
}

/**
 * 获取预订状态类型（用于el-tag）
 * @param {string} status - 预订状态
 * @returns {string} el-tag类型
 */
const getReservationStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'CANCELLED': 'info',
    'NO_SHOW': 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取预订状态标签
 * @param {string} status - 预订状态
 * @returns {string} 状态标签
 */
const getReservationStatusLabel = (status) => {
  const labelMap = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CANCELLED': '已取消',
    'NO_SHOW': '未到店'
  }
  return labelMap[status] || status
}

/**
 * 显示悬浮提示
 * @param {Event} event - 鼠标事件
 * @param {Object} room - 房间对象
 * @param {string} date - 日期
 */
const showTooltip = (event, room, date) => {
  if (!room.calendar || !room.calendar[date]) return
  const cell = room.calendar[date]
  tooltip.x = event.clientX + 10
  tooltip.y = event.clientY + 10
  tooltip.roomNo = room.roomNo
  tooltip.date = date
  tooltip.status = cell.status
  tooltip.guestName = cell.guestName || ''
  tooltipVisible.value = true
}

/**
 * 隐藏悬浮提示
 */
const hideTooltip = () => {
  tooltipVisible.value = false
}

/**
 * 快速预订房价码变化处理
 * @param {number} planId - 房价码ID
 */
const handleQuickBookingPPChange = async (planId) => {
  if (planId && quickBookingForm.roomTypeId) {
    try {
      const res = await request.get('/v1/prices/query', { 
        params: { hotelId: 1, roomTypeId: quickBookingForm.roomTypeId, date: new Date().toISOString().split('T')[0] } 
      })
      if (res.data?.price) {
        planDailyPrice.value = res.data.price
        quickBookingForm.dailyPrice = res.data.price
      }
    } catch (error) {
      console.error('获取房价失败:', error)
    }
  } else if (planId) {
    try {
      const planRes = await request.get('/v1/price-plans/' + planId)
      if (planRes.data?.details && planRes.data.details.length > 0) {
        planDailyPrice.value = planRes.data.details[0].finalPrice
        quickBookingForm.dailyPrice = planRes.data.details[0].finalPrice
      }
    } catch (error) {
      console.error('获取房价失败:', error)
    }
  } else {
    planDailyPrice.value = null
  }
}

/**
 * 处理单元格点击
 * @param {Object} room - 房间对象
 * @param {string} date - 日期
 */
const handleCellClick = (room, date) => {
  if (!room.calendar || !room.calendar[date]) return
  const cell = room.calendar[date]
  
  if (cell.status === 'AVAILABLE') {
    // 空闲状态，打开快速预订弹窗
    openQuickBooking(room, date)
  } else if (cell.status === 'RESERVED' && cell.reservationId) {
    // 已预订状态，查看预订详情
    viewReservationDetail(cell.reservationId)
  }
}

/**
 * 打开快速预订弹窗
 * @param {Object} room - 房间对象
 * @param {string} date - 入住日期
 */
const openQuickBooking = (room, date) => {
  // 重置表单
  Object.assign(quickBookingForm, {
    roomId: room.roomId,
    roomNo: room.roomNo,
    roomTypeName: room.roomTypeName,
    roomTypeId: room.roomTypeId,
    checkInDate: date,
    checkOutDate: '',
    guestName: '',
    guestPhone: '',
    pricePlanId: null,
    dailyPrice: room.basePrice,
    hotelId: 1
  })
  planDailyPrice.value = null
  quickBookingVisible.value = true
}

/**
 * 查看预订详情
 * @param {number} reservationId - 预订ID
 */
const viewReservationDetail = async (reservationId) => {
  try {
    const res = await request.get('/v1/reservations/' + reservationId)
    currentReservation.value = res.data
    reservationDetailVisible.value = true
  } catch (error) {
    console.error('获取预订详情失败:', error)
    ElMessage.error('获取预订详情失败')
  }
}

/**
 * 提交快速预订
 */
const submitQuickBooking = async () => {
  if (!quickBookingFormRef.value) return
  
  await quickBookingFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 检查日期冲突
    if (!quickBookingForm.checkOutDate) {
      ElMessage.warning('请选择离店日期')
      return
    }
    
    try {
      // 调用冲突检查接口
      const conflictRes = await request.get('/v1/rooms/conflict-check', {
        params: {
          roomId: quickBookingForm.roomId,
          checkInDate: quickBookingForm.checkInDate,
          checkOutDate: quickBookingForm.checkOutDate
        }
      })
      
      if (conflictRes.data.hasConflict) {
        ElMessage.warning('该房间在所选日期已有预订，请选择其他日期')
        return
      }
      
      // 没有冲突，提交预订
      submitLoading.value = true
      const nights = Math.ceil((new Date(quickBookingForm.checkOutDate) - new Date(quickBookingForm.checkInDate)) / 86400000)
      
      await request.post('/v1/reservations', {
        hotelId: quickBookingForm.hotelId,
        roomId: quickBookingForm.roomId,
        roomTypeId: quickBookingForm.roomTypeId,
        guestName: quickBookingForm.guestName,
        guestPhone: quickBookingForm.guestPhone,
        checkInDate: quickBookingForm.checkInDate,
        checkOutDate: quickBookingForm.checkOutDate,
        pricePlanId: quickBookingForm.pricePlanId,
        dailyPrice: quickBookingForm.dailyPrice,
        totalAmount: quickBookingForm.dailyPrice ? quickBookingForm.dailyPrice * nights : null,
        priceSource: quickBookingForm.dailyPrice ? 'MANUAL' : 'PRICE_PLAN',
        source: 'WALK_IN'
      })
      
      ElMessage.success('预订创建成功')
      quickBookingVisible.value = false
      loadCalendarData() // 刷新日历
    } catch (error) {
      console.error('创建预订失败:', error)
      ElMessage.error('创建预订失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 加载日历数据
 */
const loadCalendarData = async () => {
  loading.value = true
  try {
    const params = {
      hotelId: 1,
      startDate: startDate.value,
      days: days.value
    }
    if (filterFloor.value) params.floorId = filterFloor.value
    if (filterRoomType.value) params.roomTypeId = filterRoomType.value
    
    const res = await request.get('/v1/rooms/calendar', { params })
    calendarData.value = res.data
  } catch (error) {
    console.error('加载日历数据失败:', error)
    ElMessage.error('加载日历数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载筛选选项
 */
const loadFilterOptions = async () => {
  try {
    const [floorRes, rtRes, ppRes] = await Promise.all([
      request.get('/v1/floors/list', { params: { hotelId: 1 } }),
      request.get('/v1/room-types', { params: { hotelId: 1 } }),
      request.get('/v1/price-plans/list', { params: { hotelId: 1 } })
    ])
    floorOptions.value = floorRes.data || []
    roomTypeOptions.value = rtRes.data?.records || []
    pricePlanOptions.value = ppRes.data || []
  } catch (error) {
    console.error('加载筛选选项失败:', error)
  }
}

// ========== 生命周期 ==========

onMounted(() => {
  loadFilterOptions()
  loadCalendarData()
})
</script>

<style scoped lang="scss">
.room-calendar-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.legend-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #606266;
    
    .legend-dot {
      width: 12px;
      height: 12px;
      border-radius: 3px;
      
      &.available { background: #67C23A; }
      &.reserved { background: #409EFF; }
      &.occupied { background: #E6A23C; }
      &.maintenance { background: #F56C6C; }
      &.dirty { background: #909399; }
    }
  }
}

.calendar-wrapper {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}

.calendar-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
  
  th, td {
    border: 1px solid #ebeef5;
    padding: 8px;
    text-align: center;
  }
  
  .room-header, .room-type-header {
    background: #f5f7fa;
    font-weight: 600;
    color: #303133;
    position: sticky;
    left: 0;
    z-index: 1;
  }
  
  .room-header {
    min-width: 80px;
    left: 0;
  }
  
  .room-type-header {
    min-width: 100px;
    left: 80px;
  }
  
  .date-header {
    background: #f5f7fa;
    font-weight: 600;
    color: #303133;
    min-width: 80px;
    
    &.today {
      background: #ecf5ff;
      color: #409EFF;
    }
    
    &.weekend {
      background: #fdf6ec;
    }
    
    .date-main {
      font-size: 14px;
    }
    
    .date-weekday {
      font-size: 12px;
      color: #909399;
    }
  }
  
  .room-row {
    &:hover {
      background: #f5f7fa;
    }
    
    .room-no-cell {
      position: sticky;
      left: 0;
      background: white;
      z-index: 1;
      
      .room-no {
        font-weight: 600;
        color: #303133;
      }
      
      .floor-name {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .room-type-cell {
      position: sticky;
      left: 80px;
      background: white;
      z-index: 1;
      font-size: 12px;
      color: #606266;
    }
  }
  
  .calendar-cell {
    cursor: pointer;
    transition: all 0.2s;
    min-width: 80px;
    height: 40px;
    
    &:hover {
      background: #ecf5ff;
    }
    
    &.cell-available {
      background: #f0f9eb;
      color: #67C23A;
    }
    
    &.cell-reserved {
      background: #ecf5ff;
      color: #409EFF;
    }
    
    &.cell-occupied {
      background: #fdf6ec;
      color: #E6A23C;
    }
    
    &.cell-dirty {
      background: #f4f4f5;
      color: #909399;
    }
    
    &.cell-maintenance {
      background: #fef0f0;
      color: #F56C6C;
    }
    
    .cell-content {
      font-size: 12px;
      
      .cell-guest {
        font-weight: 500;
      }
      
      .cell-status {
        color: inherit;
      }
    }
  }
}

.cell-tooltip {
  position: fixed;
  background: #303133;
  color: white;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 13px;
  z-index: 9999;
  pointer-events: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  
  .tooltip-title {
    font-weight: 600;
    margin-bottom: 6px;
  }
  
  .tooltip-status {
    margin-bottom: 4px;
  }
  
  .tooltip-guest {
    margin-bottom: 4px;
    color: #ecf5ff;
  }
  
  .tooltip-hint {
    color: #a0cfff;
    font-size: 12px;
  }
}

.plan-price-display {
  color: #909399;
  font-size: 14px;
}

.price-unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}
</style>
