<template>
  <div class="price-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><PriceTag /></el-icon>
          房价管理
        </h1>
        <p class="page-desc">管理酒店房型价格，支持日历视图和批量调价</p>
      </div>
      <div class="header-right">
        <el-button type="warning" @click="handleBatchAdjust">
          <el-icon><Edit /></el-icon>
          批量调价
        </el-button>
        <el-button @click="handleInitPrices">
          <el-icon><Refresh /></el-icon>
          初始化房价
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filterParams" inline>
        <el-form-item label="房型">
          <el-select 
            v-model="filterParams.roomTypeId" 
            placeholder="请选择房型" 
            clearable 
            style="width: 200px"
            @change="handleFilterChange"
          >
            <el-option 
              v-for="item in roomTypeOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="月份">
          <el-date-picker
            v-model="filterParams.month"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            style="width: 200px"
            @change="handleFilterChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 视图切换卡片 -->
    <el-card class="view-card" shadow="never">
      <template #header>
        <div class="view-header">
          <div class="view-tabs">
            <el-radio-group v-model="viewMode" @change="handleViewChange">
              <el-radio-button value="calendar">
                <el-icon><Calendar /></el-icon>
                月历视图
              </el-radio-button>
              <el-radio-button value="table">
                <el-icon><List /></el-icon>
                表格视图
              </el-radio-button>
            </el-radio-group>
          </div>
          <div class="view-info" v-if="currentRoomType">
            <el-tag type="primary" effect="dark" size="large">
              {{ currentRoomType.name }}
            </el-tag>
            <el-tag type="success" effect="plain" size="large">
              基础价：¥{{ currentRoomType.basePrice }}
            </el-tag>
          </div>
        </div>
      </template>

      <!-- 月历视图 -->
      <div v-if="viewMode === 'calendar'" class="calendar-view">
        <div class="calendar-nav">
          <el-button circle @click="handlePrevMonth">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <h2 class="calendar-title">{{ calendarYear }}年{{ calendarMonth }}月</h2>
          <el-button circle @click="handleNextMonth">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div class="calendar-grid">
          <!-- 星期头部 -->
          <div class="weekday-header">
            <div v-for="day in weekdays" :key="day" class="weekday-cell">
              {{ day }}
            </div>
          </div>

          <!-- 日期网格 -->
          <div class="days-grid">
            <div
              v-for="(day, index) in calendarDays"
              :key="index"
              class="day-cell"
              :class="{
                'is-empty': !day.date,
                'is-weekend': day.dayOfWeek === 6 || day.dayOfWeek === 7,
                'is-today': day.isToday,
                'has-special-price': day.hasSpecialPrice
              }"
              @click="day.date && handleDayClick(day)"
            >
              <template v-if="day.date">
                <div class="day-header">
                  <span class="day-number">{{ day.day }}</span>
                  <el-tag v-if="day.isToday" type="danger" size="small" effect="dark">今</el-tag>
                </div>
                <div class="day-price" :class="{ 'is-special': day.hasSpecialPrice }">
                  ¥{{ day.price || calendarData.basePrice }}
                </div>
                <div class="day-status" v-if="day.hasSpecialPrice">
                  <el-icon><Star /></el-icon>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- 图例 -->
        <div class="calendar-legend">
          <div class="legend-item">
            <span class="legend-color normal"></span>
            <span>基础价格</span>
          </div>
          <div class="legend-item">
            <span class="legend-color special"></span>
            <span>特殊价格</span>
          </div>
          <div class="legend-item">
            <span class="legend-color weekend"></span>
            <span>周末</span>
          </div>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-else class="table-view">
        <el-table 
          :data="tableData" 
          v-loading="tableLoading" 
          border 
          stripe
          style="width: 100%"
        >
          <el-table-column prop="priceDate" label="日期" width="120" />
          <el-table-column prop="roomTypeName" label="房型" width="150" />
          <el-table-column prop="price" label="价格" width="120">
            <template #default="{ row }">
              <span class="price-value">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
                {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEditPrice(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="tableParams.page"
            v-model:page-size="tableParams.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="tableTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 编辑价格对话框 -->
    <el-dialog 
      v-model="priceDialogVisible" 
      title="编辑房价" 
      width="500px"
      @close="handlePriceDialogClose"
    >
      <el-form 
        ref="priceFormRef" 
        :model="priceFormData" 
        :rules="priceFormRules" 
        label-width="100px"
      >
        <el-form-item label="房型">
          <el-input :value="priceFormData.roomTypeName" disabled />
        </el-form-item>
        <el-form-item label="日期">
          <el-input :value="priceFormData.priceDate" disabled />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number 
            v-model="priceFormData.price" 
            :min="0" 
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="priceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="priceSubmitLoading" @click="handlePriceSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量调价对话框 -->
    <el-dialog 
      v-model="batchDialogVisible" 
      title="批量调价" 
      width="600px"
      @close="handleBatchDialogClose"
    >
      <el-form 
        ref="batchFormRef" 
        :model="batchFormData" 
        :rules="batchFormRules" 
        label-width="100px"
      >
        <el-form-item label="房型" prop="roomTypeIds">
          <el-select 
            v-model="batchFormData.roomTypeIds" 
            multiple 
            placeholder="请选择房型"
            style="width: 100%"
          >
            <el-option 
              v-for="item in roomTypeOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchFormData.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="调价方式" prop="adjustType">
          <el-radio-group v-model="batchFormData.adjustType">
            <el-radio value="FIXED">固定价格</el-radio>
            <el-radio value="PERCENT">百分比调整</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整值" prop="adjustValue">
          <el-input-number 
            v-model="batchFormData.adjustValue" 
            :min="0" 
            :precision="2"
            style="width: 100%"
          />
          <span class="form-tip" v-if="batchFormData.adjustType === 'PERCENT'">
            输入百分比，如 10 表示上浮 10%
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSubmitLoading" @click="handleBatchSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 初始化房价对话框 -->
    <el-dialog 
      v-model="initDialogVisible" 
      title="初始化房价" 
      width="500px"
      @close="handleInitDialogClose"
    >
      <el-form 
        ref="initFormRef" 
        :model="initFormData" 
        :rules="initFormRules" 
        label-width="100px"
      >
        <el-form-item label="房型" prop="roomTypeId">
          <el-select 
            v-model="initFormData.roomTypeId" 
            placeholder="请选择房型"
            style="width: 100%"
          >
            <el-option 
              v-for="item in roomTypeOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="initFormData.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="initDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="initSubmitLoading" @click="handleInitSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  PriceTag, Edit, Refresh, Search, Calendar, List, 
  ArrowLeft, ArrowRight, Star 
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// ========== 筛选参数 ==========
const filterParams = reactive({
  roomTypeId: null,
  month: new Date().toISOString().slice(0, 7)
})

const roomTypeOptions = ref([])

// ========== 视图模式 ==========
const viewMode = ref('calendar')

// ========== 日历相关 ==========
const calendarYear = ref(new Date().getFullYear())
const calendarMonth = ref(new Date().getMonth() + 1)
const calendarData = reactive({
  basePrice: 0,
  dayPrices: []
})
const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const currentRoomType = computed(() => {
  return roomTypeOptions.value.find(item => item.id === filterParams.roomTypeId)
})

const calendarDays = computed(() => {
  const days = []
  const firstDay = new Date(calendarYear.value, calendarMonth.value - 1, 1)
  const lastDay = new Date(calendarYear.value, calendarMonth.value, 0)
  const today = new Date()
  
  // 填充月初空白
  for (let i = 0; i < firstDay.getDay(); i++) {
    days.push({ date: null })
  }
  
  // 填充日期
  for (let d = 1; d <= lastDay.getDate(); d++) {
    const date = new Date(calendarYear.value, calendarMonth.value - 1, d)
    const dateStr = date.toISOString().slice(0, 10)
    const priceInfo = calendarData.dayPrices.find(p => p.date === dateStr)
    
    days.push({
      date: dateStr,
      day: d,
      dayOfWeek: date.getDay(),
      isToday: date.toDateString() === today.toDateString(),
      price: priceInfo?.price,
      hasSpecialPrice: !!priceInfo
    })
  }
  
  return days
})

// ========== 表格相关 ==========
const tableData = ref([])
const tableLoading = ref(false)
const tableTotal = ref(0)
const tableParams = reactive({
  page: 1,
  size: 20
})

// ========== 对话框相关 ==========
const priceDialogVisible = ref(false)
const priceSubmitLoading = ref(false)
const priceFormRef = ref(null)
const priceFormData = reactive({
  hotelId: null,
  roomTypeId: null,
  roomTypeName: '',
  priceDate: '',
  price: 0
})
const priceFormRules = {
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const batchDialogVisible = ref(false)
const batchSubmitLoading = ref(false)
const batchFormRef = ref(null)
const batchFormData = reactive({
  hotelId: 1,
  roomTypeIds: [],
  dateRange: [],
  adjustType: 'FIXED',
  adjustValue: 0
})
const batchFormRules = {
  roomTypeIds: [{ required: true, message: '请选择房型', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  adjustType: [{ required: true, message: '请选择调价方式', trigger: 'change' }],
  adjustValue: [{ required: true, message: '请输入调整值', trigger: 'blur' }]
}

const initDialogVisible = ref(false)
const initSubmitLoading = ref(false)
const initFormRef = ref(null)
const initFormData = reactive({
  hotelId: 1,
  roomTypeId: null,
  dateRange: []
})
const initFormRules = {
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }]
}

// ========== 方法 ==========
const fetchRoomTypes = async () => {
  try {
    const res = await request.get('/v1/room-types/list', { params: { hotelId: 1 } })
    roomTypeOptions.value = res.data
  } catch (error) {
    console.error('获取房型列表失败', error)
  }
}

const fetchCalendarData = async () => {
  if (!filterParams.roomTypeId) return
  try {
    const res = await request.get('/v1/prices/calendar', {
      params: {
        hotelId: 1,
        roomTypeId: filterParams.roomTypeId,
        year: calendarYear.value,
        month: calendarMonth.value
      }
    })
    Object.assign(calendarData, res.data)
    // 确保dayPrices数组存在
    if (!calendarData.dayPrices) {
      calendarData.dayPrices = []
    }
  } catch (error) {
    console.error('获取日历数据失败', error)
  }
}

const fetchTableData = async () => {
  tableLoading.value = true
  try {
    const res = await request.get('/v1/prices/list', {
      params: {
        hotelId: 1,
        roomTypeId: filterParams.roomTypeId,
        month: filterParams.month,
        page: tableParams.page,
        size: tableParams.size
      }
    })
    tableData.value = res.data.records
    tableTotal.value = res.data.total
  } catch (error) {
    console.error('获取表格数据失败', error)
  } finally {
    tableLoading.value = false
  }
}

const handleFilterChange = () => {
  handleSearch()
}

const handleSearch = () => {
  if (viewMode.value === 'calendar') {
    fetchCalendarData()
  } else {
    fetchTableData()
  }
}

const handleReset = () => {
  filterParams.roomTypeId = null
  filterParams.month = new Date().toISOString().slice(0, 7)
  handleSearch()
}

const handleViewChange = () => {
  handleSearch()
}

const handlePrevMonth = () => {
  if (calendarMonth.value === 1) {
    calendarMonth.value = 12
    calendarYear.value--
  } else {
    calendarMonth.value--
  }
  fetchCalendarData()
}

const handleNextMonth = () => {
  if (calendarMonth.value === 12) {
    calendarMonth.value = 1
    calendarYear.value++
  } else {
    calendarMonth.value++
  }
  fetchCalendarData()
}

const handleDayClick = (day) => {
  priceFormData.hotelId = 1
  priceFormData.roomTypeId = filterParams.roomTypeId
  priceFormData.roomTypeName = currentRoomType.value?.name || ''
  priceFormData.priceDate = day.date
  priceFormData.price = day.price || calendarData.basePrice
  priceDialogVisible.value = true
}

const handleEditPrice = (row) => {
  priceFormData.hotelId = row.hotelId
  priceFormData.roomTypeId = row.roomTypeId
  priceFormData.roomTypeName = row.roomTypeName
  priceFormData.priceDate = row.priceDate
  priceFormData.price = row.price
  priceDialogVisible.value = true
}

const handlePriceSubmit = async () => {
  if (!priceFormRef.value) return
  await priceFormRef.value.validate(async (valid) => {
    if (!valid) return
    priceSubmitLoading.value = true
    try {
      await request.put('/v1/prices', {
        hotelId: priceFormData.hotelId,
        roomTypeId: priceFormData.roomTypeId,
        priceDate: priceFormData.priceDate,
        price: priceFormData.price
      })
      ElMessage.success('更新成功')
      priceDialogVisible.value = false
      handleSearch()
    } catch (error) {
      console.error('更新价格失败', error)
    } finally {
      priceSubmitLoading.value = false
    }
  })
}

const handlePriceDialogClose = () => {
  priceFormRef.value?.resetFields()
}

const handleBatchAdjust = () => {
  batchFormData.hotelId = 1
  batchFormData.roomTypeIds = filterParams.roomTypeId ? [filterParams.roomTypeId] : []
  batchFormData.dateRange = []
  batchFormData.adjustType = 'FIXED'
  batchFormData.adjustValue = 0
  batchDialogVisible.value = true
}

const handleBatchSubmit = async () => {
  if (!batchFormRef.value) return
  await batchFormRef.value.validate(async (valid) => {
    if (!valid) return
    batchSubmitLoading.value = true
    try {
      const res = await request.post('/v1/prices/batch', {
        hotelId: batchFormData.hotelId,
        roomTypeIds: batchFormData.roomTypeIds,
        startDate: batchFormData.dateRange[0],
        endDate: batchFormData.dateRange[1],
        adjustType: batchFormData.adjustType,
        adjustValue: batchFormData.adjustValue
      })
      ElMessage.success(`批量调价成功，影响 ${res.data} 条记录`)
      batchDialogVisible.value = false
      handleSearch()
    } catch (error) {
      console.error('批量调价失败', error)
    } finally {
      batchSubmitLoading.value = false
    }
  })
}

const handleBatchDialogClose = () => {
  batchFormRef.value?.resetFields()
}

const handleInitPrices = () => {
  initFormData.hotelId = 1
  initFormData.roomTypeId = filterParams.roomTypeId
  initFormData.dateRange = []
  initDialogVisible.value = true
}

const handleInitSubmit = async () => {
  if (!initFormRef.value) return
  await initFormRef.value.validate(async (valid) => {
    if (!valid) return
    initSubmitLoading.value = true
    try {
      const res = await request.post('/v1/prices/init', null, {
        params: {
          hotelId: initFormData.hotelId,
          roomTypeId: initFormData.roomTypeId,
          startDate: initFormData.dateRange[0],
          endDate: initFormData.dateRange[1]
        }
      })
      ElMessage.success(`初始化成功，影响 ${res.data} 条记录`)
      initDialogVisible.value = false
      handleSearch()
    } catch (error) {
      console.error('初始化房价失败', error)
    } finally {
      initSubmitLoading.value = false
    }
  })
}

const handleInitDialogClose = () => {
  initFormRef.value?.resetFields()
}

const handleSizeChange = (val) => {
  tableParams.size = val
  fetchTableData()
}

const handleCurrentChange = (val) => {
  tableParams.page = val
  fetchTableData()
}

// ========== 初始化 ==========
onMounted(() => {
  fetchRoomTypes()
})
</script>

<style scoped lang="scss">
.price-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  
  .header-left {
    .page-title {
      display: flex;
      align-items: center;
      gap: 12px;
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      
      .el-icon {
        font-size: 28px;
        color: #409eff;
      }
    }
    
    .page-desc {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }
  
  .header-right {
    display: flex;
    gap: 12px;
  }
}

// 筛选卡片
.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  
  :deep(.el-card__body) {
    padding: 20px 24px;
  }
}

// 视图卡片
.view-card {
  border-radius: 12px;
  
  .view-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .view-tabs {
      :deep(.el-radio-group) {
        border-radius: 8px;
        overflow: hidden;
      }
    }
    
    .view-info {
      display: flex;
      gap: 12px;
    }
  }
}

// 日历视图
.calendar-view {
  .calendar-nav {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 24px;
    margin-bottom: 24px;
    
    .calendar-title {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      min-width: 140px;
      text-align: center;
    }
  }
  
  .calendar-grid {
    .weekday-header {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 8px;
      margin-bottom: 8px;
      
      .weekday-cell {
        text-align: center;
        padding: 12px;
        font-weight: 600;
        color: #606266;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        border-radius: 8px;
      }
    }
    
    .days-grid {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 8px;
      
      .day-cell {
        min-height: 100px;
        padding: 12px;
        background: white;
        border: 2px solid #e4e7ed;
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s ease;
        position: relative;
        
        &:hover:not(.is-empty) {
          border-color: #409eff;
          transform: translateY(-4px);
          box-shadow: 0 8px 25px rgba(64, 158, 255, 0.2);
        }
        
        &.is-empty {
          background: #fafafa;
          cursor: default;
          border-style: dashed;
        }
        
        &.is-weekend {
          background: #fef0f0;
          
          .day-number {
            color: #f56c6c;
          }
        }
        
        &.is-today {
          border-color: #409eff;
          background: #ecf5ff;
        }
        
        &.has-special-price {
          background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
          border-color: #faad14;
        }
        
        .day-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
          
          .day-number {
            font-size: 18px;
            font-weight: 700;
            color: #303133;
          }
        }
        
        .day-price {
          font-size: 16px;
          font-weight: 600;
          color: #409eff;
          
          &.is-special {
            color: #faad14;
            font-size: 18px;
          }
        }
        
        .day-status {
          position: absolute;
          top: 8px;
          right: 8px;
          color: #faad14;
          font-size: 16px;
        }
      }
    }
  }
  
  .calendar-legend {
    display: flex;
    justify-content: center;
    gap: 32px;
    margin-top: 24px;
    padding: 16px;
    background: white;
    border-radius: 8px;
    
    .legend-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: #606266;
      
      .legend-color {
        width: 24px;
        height: 24px;
        border-radius: 6px;
        border: 2px solid;
        
        &.normal {
          background: white;
          border-color: #e4e7ed;
        }
        
        &.special {
          background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
          border-color: #faad14;
        }
        
        &.weekend {
          background: #fef0f0;
          border-color: #f56c6c;
        }
      }
    }
  }
}

// 表格视图
.table-view {
  .price-value {
    font-weight: 700;
    color: #409eff;
    font-size: 16px;
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 24px;
    padding: 16px 0;
  }
}

// 表单提示
.form-tip {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>