<template>
  <div class="dashboard-container">
    <!-- 核心数据看板 -->
    <div class="stat-cards">
      <div class="stat-card" style="border-left: 4px solid #409EFF">
        <div class="stat-icon" style="background: #ecf5ff; color: #409EFF"><el-icon :size="28"><Calendar /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayCheckIns || 0 }}</div>
          <div class="stat-label">今日入住</div>
        </div>
      </div>
      <div class="stat-card" style="border-left: 4px solid #67C23A">
        <div class="stat-icon" style="background: #f0f9eb; color: #67C23A"><el-icon :size="28"><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayDepartures || 0 }}</div>
          <div class="stat-label">今日离店</div>
        </div>
      </div>
      <div class="stat-card" style="border-left: 4px solid #E6A23C">
        <div class="stat-icon" style="background: #fdf6ec; color: #E6A23C"><el-icon :size="28"><House /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.occupiedRooms || 0 }}/{{ stats.totalRooms || 0 }}</div>
          <div class="stat-label">在住房间</div>
        </div>
      </div>
      <div class="stat-card" style="border-left: 4px solid #F56C6C">
        <div class="stat-icon" style="background: #fef0f0; color: #F56C6C"><el-icon :size="28"><Money /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">¥{{ (todayRevenue || 0).toFixed(0) }}</div>
          <div class="stat-label">今日营收</div>
        </div>
      </div>
      <div class="stat-card" style="border-left: 4px solid #909399">
        <div class="stat-icon" style="background: #f4f4f5; color: #909399"><el-icon :size="28"><Clock /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingReservations || 0 }}</div>
          <div class="stat-label">待确认预订</div>
        </div>
      </div>
      <div class="stat-card" style="border-left: 4px solid #9B59B6">
        <div class="stat-icon" style="background: #f5f0ff; color: #9B59B6"><el-icon :size="28"><Brush /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.dirtyRooms || 0 }}</div>
          <div class="stat-label">待清洁房间</div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 + 房态概览 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">快捷操作</span></template>
          <div class="action-buttons">
            <el-button type="primary" icon="Plus" @click="showWalkInDialog">散客入住</el-button>
            <el-button type="success" icon="Calendar" @click="showReservationDialog">新增预订</el-button>
            <el-button type="warning" icon="View" @click="router.push('/room-board')">房态看板</el-button>
            <el-button type="info" icon="Money" @click="router.push('/stays')">入住管理</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">房态概览</span></template>
          <div class="room-overview">
            <div class="overview-bar">
              <div v-for="item in roomStatusBars" :key="item.key" class="bar-segment" :style="{ flex: item.count, background: item.color }" :title="item.label + ': ' + item.count">
                <span v-if="item.count > 0">{{ item.count }}</span>
              </div>
            </div>
            <div class="overview-legend">
              <div v-for="item in roomStatusBars" :key="item.key" class="legend-item">
                <span class="legend-dot" :style="{ background: item.color }"></span>
                {{ item.label }} {{ item.count }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日动态 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">今日抵店</span></template>
          <el-table :data="todayCheckins" stripe size="small" max-height="300">
            <el-table-column prop="guestName" label="客人" width="100" />
            <el-table-column prop="roomNo" label="房号" width="70" />
            <el-table-column prop="roomTypeName" label="房型" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'CHECKED_IN' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'CHECKED_IN' ? '已入住' : '待入住' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="todayCheckins.length === 0" class="empty-tip">今日暂无抵店</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">今日离店</span></template>
          <el-table :data="todayDepartures" stripe size="small" max-height="300">
            <el-table-column prop="guestName" label="客人" width="100" />
            <el-table-column prop="roomNo" label="房号" width="70" />
            <el-table-column prop="roomTypeName" label="房型" />
            <el-table-column label="付款状态" width="90">
              <template #default="{ row }">
                <el-tag :type="getPayTagType(row)" size="small">{{ getPayText(row) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="todayDepartures.length === 0" class="empty-tip">今日暂无离店</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 营收趋势 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header><span style="font-weight: 600">近7日营收趋势</span></template>
      <div class="revenue-chart">
        <div v-for="item in revenueTrend" :key="item.date" class="chart-bar-wrapper">
          <div class="chart-bar" :style="{ height: getBarHeight(item.revenue) + 'px' }">
            <span class="bar-value">¥{{ (item.revenue || 0).toFixed(0) }}</span>
          </div>
          <div class="bar-date">{{ item.date.slice(5) }}</div>
        </div>
      </div>
    </el-card>

    <!-- 新建预订弹窗 -->
    <el-dialog v-model="reservationDialogVisible" title="新增预订" width="600px" :close-on-click-modal="false">
      <el-form :model="reservationForm" :rules="reservationRules" ref="reservationFormRef" label-width="100px">
        <el-form-item label="客人姓名" prop="guestName"><el-input v-model="reservationForm.guestName" placeholder="请输入客人姓名" /></el-form-item>
        <el-form-item label="客人电话" prop="guestPhone"><el-input v-model="reservationForm.guestPhone" placeholder="请输入客人电话" /></el-form-item>
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="reservationForm.roomTypeId" placeholder="请选择房型" style="width: 100%" @change="handleReservationRoomTypeChange">
            <el-option v-for="item in roomTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排房">
          <div class="room-select-area">
            <el-button v-if="!selectedRoomInfo" type="primary" plain size="small" :disabled="!reservationForm.roomTypeId" @click="showRoomDialog = true">选择房间</el-button>
            <div v-else class="selected-room-display">
              <el-tag type="success" closable @close="clearRoomSelection">{{ selectedRoomInfo.roomNo }}号房（{{ selectedRoomInfo.roomTypeName }}）</el-tag>
              <el-button text type="primary" size="small" @click="showRoomDialog = true">更换</el-button>
            </div>
            <span v-if="!selectedRoomInfo" class="room-hint">不选则自动分配</span>
          </div>
        </el-form-item>
        <el-form-item label="房价码"><el-select v-model="reservationForm.pricePlanId" placeholder="请选择房价码（可选）" clearable style="width: 100%" @change="handleReservationPP"><el-option v-for="item in pricePlanOptions" :key="item.id" :label="item.code + ' - ' + item.name" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="房价码价格" v-if="planDailyPrice"><span class="plan-price-display">¥{{ planDailyPrice }}/晚（只读）</span></el-form-item>
        <el-form-item label="预定价格"><el-input-number v-model="reservationForm.dailyPrice" :min="0" :precision="2" style="width: 200px" /><span class="price-unit">元/晚</span></el-form-item>
        <el-form-item label="入住日期" prop="checkInDate"><el-date-picker v-model="reservationForm.checkInDate" type="date" placeholder="选择入住日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="离店日期" prop="checkOutDate"><el-date-picker v-model="reservationForm.checkOutDate" type="date" placeholder="选择离店日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="来源"><el-select v-model="reservationForm.source" style="width: 100%"><el-option label="散客" value="WALK_IN" /><el-option label="电话" value="PHONE" /><el-option label="OTA" value="OTA" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reservationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitReservation">确定</el-button>
      </template>
    </el-dialog>

    <!-- 房间选择弹窗 -->
    <RoomSelectDialog v-model="showRoomDialog" :hotel-id="1" :room-type-id="reservationForm.roomTypeId" :check-in-date="reservationForm.checkInDate" :check-out-date="reservationForm.checkOutDate" @select="handleReservationRoomSelected" />

    <!-- 快速入住弹窗 -->
    <el-dialog v-model="walkInDialogVisible" title="散客入住" width="600px" :close-on-click-modal="false">
      <el-form :model="walkInForm" :rules="walkInRules" ref="walkInFormRef" label-width="100px">
        <el-divider content-position="left">房间信息</el-divider>
        <el-form-item label="选择房型" prop="roomTypeId"><el-select v-model="walkInForm.roomTypeId" placeholder="请选择房型" style="width: 100%" @change="handleRoomTypeChange"><el-option v-for="rt in roomTypeOptions" :key="rt.id" :label="rt.name" :value="rt.id" /></el-select></el-form-item>
        <el-form-item label="选择房间" prop="roomId"><el-select v-model="walkInForm.roomId" placeholder="请选择房间" style="width: 100%" :disabled="!walkInForm.roomTypeId"><el-option v-for="room in availableRooms" :key="room.id" :label="room.roomNo + ' (' + room.roomTypeName + ')'" :value="room.id" /></el-select></el-form-item>
        <el-divider content-position="left">客人信息</el-divider>
        <el-form-item label="客人姓名" prop="guestName"><el-input v-model="walkInForm.guestName" placeholder="请输入客人姓名" /></el-form-item>
        <el-form-item label="客人电话" prop="guestPhone"><el-input v-model="walkInForm.guestPhone" placeholder="请输入客人电话" /></el-form-item>
        <el-form-item label="证件号"><el-input v-model="walkInForm.guestIdNo" placeholder="请输入证件号" /></el-form-item>
        <el-form-item label="性别"><el-select v-model="walkInForm.guestGender" placeholder="请选择性别"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item>
        <el-divider content-position="left">入住信息</el-divider>
        <el-form-item label="房价码"><el-select v-model="walkInForm.pricePlanId" placeholder="选择房价码（可选）" clearable style="width: 100%" @change="handleWalkInPP"><el-option v-for="plan in pricePlanOptions" :key="plan.id" :label="plan.code + ' - ' + plan.name" :value="plan.id" /></el-select></el-form-item>
        <el-form-item label="单日房价"><el-input-number v-model="walkInForm.dailyPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="预计离店" prop="expectedCheckOutDate"><el-date-picker v-model="walkInForm.expectedCheckOutDate" type="date" placeholder="选择预计离店日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="walkInDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitWalkIn">确认入住</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, CircleCheck, House, Money, Clock, Brush } from '@element-plus/icons-vue'
import request from '@/utils/request'
import RoomSelectDialog from '../reservation/RoomSelectDialog.vue'
import { getDashboardStats, getTodayRevenue, getRevenueTrend, getTodayCheckins, getTodayDepartures } from '@/api/dashboard'

const router = useRouter()
const hotelId = 1

// ========== 数据 ==========
const stats = ref({})
const todayRevenue = ref(0)
const revenueTrend = ref([])
const todayCheckins = ref([])
const todayDepartures = ref([])

const roomStatusBars = computed(() => [
  { key: 'available', label: '空闲', count: stats.value.availableRooms || 0, color: '#67C23A' },
  { key: 'occupied', label: '在住', count: stats.value.occupiedRooms || 0, color: '#409EFF' },
  { key: 'dirty', label: '脏房', count: stats.value.dirtyRooms || 0, color: '#E6A23C' },
  { key: 'maintenance', label: '维修', count: stats.value.maintenanceRooms || 0, color: '#F56C6C' },
  { key: 'reserved', label: '预留', count: stats.value.pendingReservations || 0, color: '#9B59B6' }
])

const maxRevenue = computed(() => Math.max(...revenueTrend.value.map(i => i.revenue || 0), 1))

function getBarHeight(val) { return Math.max(((val || 0) / maxRevenue.value) * 120, 4) }

function getPayTagType(row) {
  if (!row.paidAmount || row.paidAmount === 0) return 'danger'
  if (row.paidAmount >= row.totalAmount) return 'success'
  return 'warning'
}
function getPayText(row) {
  if (!row.paidAmount || row.paidAmount === 0) return '未付款'
  if (row.paidAmount >= row.totalAmount) return '已付清'
  return '部分付款'
}

// ========== 弹窗 ==========
const submitLoading = ref(false)
const roomTypeOptions = ref([])
const pricePlanOptions = ref([])
const availableRooms = ref([])
// 排房相关
const showRoomDialog = ref(false)
const selectedRoomInfo = ref(null)
const planDailyPrice = ref(null)

// 预订弹窗
const reservationDialogVisible = ref(false)
const reservationFormRef = ref(null)
const reservationForm = reactive({ guestName: '', guestPhone: '', roomTypeId: null, roomId: null, pricePlanId: null, dailyPrice: null, checkInDate: '', checkOutDate: '', source: 'WALK_IN', hotelId: 1 })
const reservationRules = {
  guestName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入客人电话', trigger: 'blur' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择离店日期', trigger: 'change' }]
}

// 入住弹窗
const walkInDialogVisible = ref(false)
const walkInFormRef = ref(null)
const walkInForm = reactive({ roomTypeId: null, roomId: null, guestName: '', guestPhone: '', guestIdNo: '', guestGender: '', pricePlanId: null, dailyPrice: null, expectedCheckOutDate: '', hotelId: 1 })
const walkInRules = {
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  guestName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入客人电话', trigger: 'blur' }],
  expectedCheckOutDate: [{ required: true, message: '请选择预计离店日期', trigger: 'change' }]
}

// ========== 方法 ==========
async function loadOptions() {
  try {
    const [rt, pp] = await Promise.all([
      request.get('/v1/room-types', { params: { hotelId } }),
      request.get('/v1/price-plans/list', { params: { hotelId } })
    ])
    roomTypeOptions.value = rt.data?.records || []
    pricePlanOptions.value = pp.data || []
  } catch (e) { console.error(e) }
}

function showReservationDialog() {
  Object.assign(reservationForm, { guestName: '', guestPhone: '', roomTypeId: null, roomId: null, pricePlanId: null, dailyPrice: null, checkInDate: '', checkOutDate: '', source: 'WALK_IN' }); selectedRoomInfo.value = null; planDailyPrice.value = null
  reservationDialogVisible.value = true
  loadOptions()
}

function showWalkInDialog() {
  Object.assign(walkInForm, { roomTypeId: null, roomId: null, guestName: '', guestPhone: '', guestIdNo: '', guestGender: '', pricePlanId: null, dailyPrice: null, expectedCheckOutDate: '' })
  availableRooms.value = []
  walkInDialogVisible.value = true
  loadOptions()
}

async function handleRoomTypeChange(roomTypeId) {
  walkInForm.roomId = null
  if (!roomTypeId) { availableRooms.value = []; return }
  try {
    const res = await request.get('/v1/rooms', { params: { hotelId, status: 'AVAILABLE', roomTypeId } })
    availableRooms.value = res.data?.records || []
  } catch (e) { console.error(e) }
}

// ========== 预订房型变化处理 ==========
function handleReservationRoomTypeChange() {
  selectedRoomInfo.value = null
  reservationForm.roomId = null
}

// ========== 预订房间选择回调 ==========
function handleReservationRoomSelected(room) {
  selectedRoomInfo.value = room
  reservationForm.roomId = room.id
}

// ========== 清除房间选择 ==========
function clearRoomSelection() {
  selectedRoomInfo.value = null
  reservationForm.roomId = null
}

const handleReservationPP = async (planId) => {
  if (planId && reservationForm.roomTypeId) {
    try { const r = await request.get('/v1/prices/query', { params: { hotelId, roomTypeId: reservationForm.roomTypeId, date: new Date().toISOString().split('T')[0] } }); if (r.data?.price) { planDailyPrice.value = r.data.price; reservationForm.dailyPrice = r.data.price } } catch(e) {}
  } else if (planId) {
    try { const r = await request.get('/v1/price-plans/' + planId); if (r.data?.details?.length) planDailyPrice.value = r.data.details[0].finalPrice; reservationForm.dailyPrice = r.data.details[0].finalPrice } catch(e) {}
  } else { planDailyPrice.value = null; reservationForm.dailyPrice = null }
}

const handleWalkInPP = async (planId) => {
  if (planId && walkInForm.roomTypeId) {
    try { const r = await request.get('/v1/prices/query', { params: { hotelId, roomTypeId: walkInForm.roomTypeId, date: new Date().toISOString().split('T')[0] } }); if (r.data?.price) walkInForm.dailyPrice = r.data.price } catch(e) {}
  } else if (planId) {
    try { const r = await request.get('/v1/price-plans/' + planId); if (r.data?.details?.length) walkInForm.dailyPrice = r.data.details[0].finalPrice } catch(e) {}
  } else { walkInForm.dailyPrice = null }
}

async function submitReservation() {
  if (!reservationFormRef.value) return
  await reservationFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const n = Math.ceil((new Date(reservationForm.checkOutDate) - new Date(reservationForm.checkInDate)) / 86400000)
      await request.post('/v1/reservations', { ...reservationForm, roomId: reservationForm.roomId, totalAmount: reservationForm.dailyPrice ? reservationForm.dailyPrice * n : null, priceSource: reservationForm.dailyPrice ? 'MANUAL' : 'PRICE_PLAN' })
      ElMessage.success('预订创建成功')
      reservationDialogVisible.value = false
      loadData()
    } catch (e) { ElMessage.error('创建失败') } finally { submitLoading.value = false }
  })
}

async function submitWalkIn() {
  if (!walkInFormRef.value) return
  await walkInFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await request.post('/v1/stays/walk-in', walkInForm)
      ElMessage.success('入住成功')
      walkInDialogVisible.value = false
      loadData()
    } catch (e) { ElMessage.error('入住失败') } finally { submitLoading.value = false }
  })
}

async function loadData() {
  try {
    const [s, rev, trend, ci, cd] = await Promise.all([
      getDashboardStats(hotelId),
      getTodayRevenue(hotelId),
      getRevenueTrend(hotelId, 7),
      getTodayCheckins(hotelId),
      getTodayDepartures(hotelId)
    ])
    stats.value = s.data || {}
    todayRevenue.value = rev.data || 0
    revenueTrend.value = trend.data || []
    todayCheckins.value = ci.data || []
    todayDepartures.value = cd.data || []
  } catch (e) { console.error('加载数据失败', e) }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.dashboard-container { padding: 20px; background: #f5f7fa; min-height: 100vh; }
.stat-cards { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; }
.stat-card { background: #fff; border-radius: 8px; padding: 16px; display: flex; align-items: center; gap: 12px; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
.stat-icon { width: 48px; height: 48px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.stat-value { font-size: 24px; font-weight: 600; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
.action-buttons { display: flex; flex-wrap: wrap; gap: 12px; }
.room-overview { .overview-bar { display: flex; height: 32px; border-radius: 6px; overflow: hidden; margin-bottom: 12px; .bar-segment { display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; min-width: 20px; transition: flex .3s; } } .overview-legend { display: flex; gap: 16px; flex-wrap: wrap; .legend-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; .legend-dot { width: 10px; height: 10px; border-radius: 50%; } } } }
.room-select-area { display: flex; align-items: center; gap: 12px; }
.selected-room-display { display: flex; align-items: center; gap: 8px; }
.room-hint { color: #909399; font-size: 12px; }
.plan-price-display { color: #909399; font-size: 14px; }
.price-unit { margin-left: 8px; color: #909399; font-size: 14px; }
.empty-tip { text-align: center; color: #909399; padding: 20px; font-size: 14px; }
.revenue-chart { display: flex; align-items: flex-end; gap: 12px; height: 160px; padding: 10px 0; .chart-bar-wrapper { flex: 1; display: flex; flex-direction: column; align-items: center; } .chart-bar { width: 100%; max-width: 60px; background: linear-gradient(180deg, #409EFF, #66b1ff); border-radius: 4px 4px 0 0; display: flex; align-items: flex-start; justify-content: center; padding-top: 4px; transition: height .3s; .bar-value { font-size: 11px; color: #fff; font-weight: 500; } } .bar-date { font-size: 12px; color: #909399; margin-top: 6px; } }
</style>
