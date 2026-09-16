<template>
  <div class="reservation-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>预订管理</h2>
      <div class="header-actions">
        <el-button :type="showTodayArrivals ? 'primary' : 'default'" @click="toggleTodayArrivals">
          <el-icon><Calendar /></el-icon>
          今日抵店 {{ todayArrivalsCount > 0 ? '(' + todayArrivalsCount + ')' : '' }}
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增预订
        </el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="预订号">
          <el-input 
            v-model="queryParams.reservationNo" 
            placeholder="请输入预订号" 
            clearable 
            style="width: 160px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="客人姓名">
          <el-input 
            v-model="queryParams.guestName" 
            placeholder="请输入客人姓名" 
            clearable 
            style="width: 140px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="客人电话">
          <el-input 
            v-model="queryParams.guestPhone" 
            placeholder="请输入客人电话" 
            clearable 
            style="width: 140px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="queryParams.status" 
            placeholder="全部状态" 
            clearable 
            style="width: 140px;"
          >
            <el-option label="全部状态" :value="null" />
            <el-option label="待确认" value="PENDING">
              <el-tag type="warning" size="small" effect="plain">待确认</el-tag>
            </el-option>
            <el-option label="已确认" value="CONFIRMED">
              <el-tag type="success" size="small" effect="plain">已确认</el-tag>
            </el-option>
            <el-option label="已入住" value="CHECKED_IN">
              <el-tag type="primary" size="small" effect="plain">已入住</el-tag>
            </el-option>
            <el-option label="已取消" value="CANCELLED">
              <el-tag type="info" size="small" effect="plain">已取消</el-tag>
            </el-option>
            <el-option label="未到店" value="NO_SHOW">
              <el-tag type="danger" size="small" effect="plain">未到店</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="单日房价">
          <el-input-number v-model="formData.dailyPrice" :min="0" :precision="2" placeholder="选择房价码后自动填充，可修改" style="width: 100%" />
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="reservationNo" label="预订号" width="140" fixed="left" />
        <el-table-column prop="guestName" label="客人姓名" width="100" />
        <el-table-column prop="guestPhone" label="客人电话" width="120" />
        <el-table-column prop="roomTypeName" label="房型" width="120" />
        <el-table-column prop="roomNo" label="房间号" width="80">
          <template #default="{ row }">
            {{ row.roomNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" width="110" />
        <el-table-column prop="checkOutDate" label="离店日期" width="110" />
        <el-table-column prop="nights" label="房晚" width="60" align="center" />
        <el-table-column prop="totalAmount" label="总金额" width="100" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="primary" link @click="handleConfirm(row)"
            >确认</el-button>
            <el-button 
              v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'" 
              type="warning" 
              link 
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button 
              v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'" 
              type="danger" 
              link 
              @click="handleCancel(row)"
            >取消</el-button>
            <el-button 
              v-if="row.status === 'CONFIRMED'" 
              type="info" 
              link 
              @click="handleNoShow(row)"
            >未到店</el-button>
            <el-button 
              v-if="row.status === 'CONFIRMED'" 
              type="primary" 
              link 
              @click="handleCheckIn(row)"
            >入住</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="客人姓名" prop="guestName">
          <el-input v-model="formData.guestName" placeholder="请输入客人姓名" />
        </el-form-item>
        <el-form-item label="客人电话" prop="guestPhone">
          <el-input v-model="formData.guestPhone" placeholder="请输入客人电话" />
        </el-form-item>
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="formData.roomTypeId" placeholder="请选择房型" style="width: 100%" @change="handleRoomTypeChange">
            <el-option v-for="item in roomTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排房">
          <div class="room-select-area">
            <el-button v-if="!selectedRoomInfo" type="primary" plain size="small" :disabled="!formData.roomTypeId" @click="showRoomDialog = true">
              选择房间
            </el-button>
            <div v-else class="selected-room-display">
              <el-tag type="success" closable @close="clearRoomSelection">
                {{ selectedRoomInfo.roomNo }}号房（{{ selectedRoomInfo.roomTypeName }}）
              </el-tag>
              <el-button text type="primary" size="small" @click="showRoomDialog = true">更换</el-button>
            </div>
            <span v-if="!selectedRoomInfo" class="room-hint">不选则自动分配</span>
          </div>
        </el-form-item>
        <el-form-item label="房价码">
          <el-select v-model="formData.pricePlanId" placeholder="请选择房价码（可选）" clearable style="width: 100%" @change="handlePricePlanChange">
            <el-option v-for="item in pricePlanOptions" :key="item.id" :label="item.code + ' - ' + item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房价码价格" v-if="planDailyPrice">
          <span class="plan-price-display">¥{{ planDailyPrice }}/晚（只读）</span>
        </el-form-item>
        <el-form-item label="预定价格">
          <el-input-number v-model="formData.dailyPrice" :min="0" :precision="2" placeholder="可修改" style="width: 200px" />
          <span class="price-unit">元/晚</span>
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker v-model="formData.checkInDate" type="date" placeholder="选择入住日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="离店日期" prop="checkOutDate">
          <el-date-picker v-model="formData.checkOutDate" type="date" placeholder="选择离店日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-select v-model="formData.source" placeholder="请选择来源" style="width: 100%">
            <el-option label="散客" value="WALK_IN" />
            <el-option label="电话" value="PHONE" />
            <el-option label="OTA" value="OTA" />
          </el-select>
        </el-form-item>
        <el-form-item label="特殊要求">
          <el-input v-model="formData.specialRequests" type="textarea" :rows="3" placeholder="请输入特殊要求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 房间选择弹窗 -->
    <RoomSelectDialog 
      v-model="showRoomDialog"
      :hotel-id="1"
      :room-type-id="formData.roomTypeId"
      :check-in-date="formData.checkInDate"
      :check-out-date="formData.checkOutDate"
      @select="handleRoomSelected"
    />
    
    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="预订详情" size="500px">
      <el-descriptions v-if="currentReservation" :column="1" border>
        <el-descriptions-item label="预订号">{{ currentReservation.reservationNo }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ currentReservation.guestName }}</el-descriptions-item>
        <el-descriptions-item label="客人电话">{{ currentReservation.guestPhone }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ currentReservation.roomTypeName }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ currentReservation.roomNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ currentReservation.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="离店日期">{{ currentReservation.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="房晚数">{{ currentReservation.nights }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentReservation.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentReservation.status)">
            {{ getStatusLabel(currentReservation.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源">{{ getSourceLabel(currentReservation.source) }}</el-descriptions-item>
        <el-descriptions-item label="特殊要求">{{ currentReservation.specialRequests || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentReservation.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>

import { useUserStore } from '@/stores/user'
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Calendar } from '@element-plus/icons-vue'
import request from '@/utils/request'
import RoomSelectDialog from './RoomSelectDialog.vue'
const userStore = useUserStore()

const router = useRouter()

// ========== 显示今日抵店 ==========
const showTodayArrivals = ref(false)

// ========== 查询相关 ==========
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref(null)
const queryParams = reactive({
  page: 1,
  size: 10,
  hotelId: userStore.hotelId,
  status: null,
  excludeStatus: 'CHECKED_IN',
  reservationNo: '',
  guestName: '',
  guestPhone: '',
  checkInDateStart: null,
  checkInDateEnd: null
})

// ========== 今日抵店 ==========
const todayArrivalsCount = ref(0)

// ========== 房型选项 ==========
const roomTypeOptions = ref([])
const pricePlanOptions = ref([])
// 排房相关
const showRoomDialog = ref(false)
const selectedRoomInfo = ref(null)
const planDailyPrice = ref(null)

// ========== 弹窗相关 ==========
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

// ========== 详情抽屉 ==========
const drawerVisible = ref(false)
const currentReservation = ref(null)

// ========== 表单数据 ==========
const formData = reactive({
  guestName: '',
  guestPhone: '',
  roomTypeId: null,
  roomId: null,
  checkInDate: '',
  checkOutDate: '',
  source: 'WALK_IN',
  pricePlanId: null,
  dailyPrice: null,
  specialRequests: ''
})

// ========== 表单验证规则 ==========
const formRules = {
  guestName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入客人电话', trigger: 'blur' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择离店日期', trigger: 'change' }]
}

// ========== 获取数据 ==========
const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (showTodayArrivals.value) {
      params.checkInDateStart = new Date().toISOString().split('T')[0]
      params.checkInDateEnd = params.checkInDateStart
    }
    const res = await request.get('/v1/reservations', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取预订列表失败', error)
  } finally {
    loading.value = false
  }
}

// ========== 获取房型选项 ==========
const fetchRoomTypeOptions = async () => {
  try {
    const res = await request.get('/v1/room-types/options', { params: { hotelId: userStore.hotelId } })
    roomTypeOptions.value = res.data
  } catch (error) {
    console.error('获取房型选项失败', error)
  }
}

// ========== 房型变化处理 ==========
const handleRoomTypeChange = () => {
  selectedRoomInfo.value = null
  formData.roomId = null
  // 【联动】已选房价码时按新房型刷新房价码金额
  if (formData.pricePlanId) {
    handlePricePlanChange(formData.pricePlanId)
  }
}

// ========== 房间选择回调 ==========
const handleRoomSelected = (room) => {
  selectedRoomInfo.value = room
  formData.roomId = room.id
}

// ========== 清除房间选择 ==========
const clearRoomSelection = () => {
  selectedRoomInfo.value = null
  formData.roomId = null
}

// ========== 房价码变化处理 ==========
/**
 * 房价码变化处理：按 房价码 + 房型 查询房价码金额（/v1/prices/query 支持 pricePlanId）
 */
const handlePricePlanChange = async (planId) => {
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
}

// ========== 获取房价码选项 ==========
const fetchPricePlanOptions = async () => {
  try {
    const res = await request.get('/v1/price-plans/list', { params: { hotelId: userStore.hotelId } })
    pricePlanOptions.value = res.data || []
  } catch (error) {
    console.error('获取房价码选项失败:', error)
  }
}

// ========== 获取今日抵店数量 ==========
const fetchTodayArrivalsCount = async () => {
  try {
    const res = await request.get('/v1/reservations', {
      params: {
        hotelId: userStore.hotelId,
        status: 'CONFIRMED',
        checkInDateStart: new Date().toISOString().split('T')[0],
        checkInDateEnd: new Date().toISOString().split('T')[0],
        size: 1
      }
    })
    todayArrivalsCount.value = res.data.total
  } catch (error) {
    console.error('获取今日抵店数量失败', error)
  }
}

// ========== 切换今日抵店 ==========
const toggleTodayArrivals = () => {
  showTodayArrivals.value = !showTodayArrivals.value
  queryParams.page = 1
  fetchData()
}

// ========== 状态筛选变化 ==========
const handleStatusChange = (val) => {
  // 选择具体状态时，不排除任何状态；清空时排除已入住
  queryParams.excludeStatus = val ? null : 'CHECKED_IN'
  queryParams.page = 1
  fetchData()
}

// ========== 搜索 ==========
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// ========== 重置 ==========
const handleReset = () => {
  queryParams.reservationNo = ''
  queryParams.guestName = ''
  queryParams.guestPhone = ''
  queryParams.status = null
  queryParams.excludeStatus = 'CHECKED_IN'
  queryParams.checkInDateStart = null
  queryParams.checkInDateEnd = null
  dateRange.value = null
  queryParams.page = 1
  fetchData()
}

// ========== 日期变化 ==========
const handleDateChange = (val) => {
  if (val) {
    queryParams.checkInDateStart = val[0]
    queryParams.checkInDateEnd = val[1]
  } else {
    queryParams.checkInDateStart = null
    queryParams.checkInDateEnd = null
  }
}

// ========== 分页 ==========
const handleSizeChange = (val) => {
  queryParams.size = val
  fetchData()
}

const handleCurrentChange = (val) => {
  queryParams.page = val
  fetchData()
}

// ========== 新增 ==========
const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增预订'
  formData.guestName = ''
  formData.guestPhone = ''
  formData.roomTypeId = null
  formData.checkInDate = ''
  formData.checkOutDate = ''
  formData.source = 'WALK_IN'
  formData.pricePlanId = null
  formData.dailyPrice = null
  formData.specialRequests = ''
  dialogVisible.value = true
}

// ========== 编辑 ==========
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑预订'
  formData.guestName = row.guestName
  formData.guestPhone = row.guestPhone
  formData.roomTypeId = row.roomTypeId
  formData.checkInDate = row.checkInDate
  formData.checkOutDate = row.checkOutDate
  formData.source = row.source
  formData.pricePlanId = row.pricePlanId || null
  formData.dailyPrice = row.dailyPrice || null
  formData.specialRequests = row.specialRequests
  dialogVisible.value = true
}

// ========== 详情 ==========
const handleView = (row) => {
  currentReservation.value = row
  drawerVisible.value = true
}

// ========== 确认预订 ==========
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确定要确认该预订吗？', '提示', { type: 'warning' })
    await request.put('/v1/reservations/' + row.id + '/confirm')
    ElMessage.success('确认成功')
    fetchData()
    fetchTodayArrivalsCount()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认预订失败', error)
    }
  }
}

// ========== 取消预订 ==========
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预订吗？取消后不可恢复。', '警告', { type: 'error' })
    await request.delete('/v1/reservations/' + row.id)
    ElMessage.success('取消成功')
    fetchData()
    fetchTodayArrivalsCount()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预订失败', error)
    }
  }
}

// ========== 标记未到店 ==========
const handleNoShow = async (row) => {
  try {
    await ElMessageBox.confirm('确定要标记该预订为未到店吗？', '提示', { type: 'warning' })
    await request.put('/v1/reservations/' + row.id + '/no-show')
    ElMessage.success('标记成功')
    fetchData()
    fetchTodayArrivalsCount()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记未到店失败', error)
    }
  }
}

// ========== 入住 ==========
const handleCheckIn = (row) => {
  // 跳转到入住管理页面，带上预订信息
  router.push({
    path: '/stays',
    query: { 
      reservationId: row.id,
      guestName: row.guestName,
      guestPhone: row.guestPhone,
      roomId: row.roomId,
      roomTypeId: row.roomTypeId,
      roomTypeName: row.roomTypeName
    }
  })
}

// ========== 提交表单 ==========
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await request.put('/v1/reservations/' + editId.value, formData)
        ElMessage.success('更新成功')
      } else {
        await request.post('/v1/reservations', { ...formData, hotelId: queryParams.hotelId, totalAmount: formData.dailyPrice ? formData.dailyPrice * Math.ceil((new Date(formData.checkOutDate) - new Date(formData.checkInDate)) / (1000 * 60 * 60 * 24)) : null })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
      fetchTodayArrivalsCount()
    } catch (error) {
      console.error('保存预订失败', error)
    } finally {
      submitLoading.value = false
    }
  })
}

// ========== 弹窗关闭 ==========
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// ========== 状态相关 ==========
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'CHECKED_IN': 'primary',
    'CANCELLED': 'info',
    'NO_SHOW': 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CHECKED_IN': '已入住',
    'CANCELLED': '已取消',
    'NO_SHOW': '未到店'
  }
  return map[status] || status
}

const getSourceLabel = (source) => {
  const map = {
    'WALK_IN': '散客',
    'PHONE': '电话',
    'OTA': 'OTA'
  }
  return map[source] || source
}

// ========== 初始化 ==========
onMounted(() => {
  fetchData()
  fetchRoomTypeOptions()
  fetchPricePlanOptions()
  fetchTodayArrivalsCount()
})
</script>

<style scoped lang="scss">
.reservation-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.search-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 20px 20px 0;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
  }
}

.table-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .amount {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
