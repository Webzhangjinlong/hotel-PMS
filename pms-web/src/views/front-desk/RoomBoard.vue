<template>
  <div class="room-board-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>房态看板</h2>
      <div class="header-actions">
        <el-button @click="handleRefresh" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>
    
    <!-- 统计面板 -->
    <div class="stat-panel">
      <div 
        v-for="item in statCards" 
        :key="item.status"
        class="stat-card"
        :class="{ active: selectedStatus === item.status }"
        @click="handleStatusFilter(item.status)"
      >
        <div class="stat-icon" :style="{ backgroundColor: item.color }">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.count }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </div>
    
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select 
        v-model="selectedFloor" 
        placeholder="选择楼层" 
        clearable 
        @change="handleFloorChange"
      >
        <el-option 
          v-for="floor in floorOptions" 
          :key="floor.id" 
          :label="floor.name" 
          :value="floor.id" 
        />
      </el-select>
      <el-tag 
        v-if="selectedStatus" 
        closable 
        @close="handleClearStatusFilter"
      >
        当前筛选: {{ getStatusName(selectedStatus) }}
      </el-tag>
    </div>
    
    <!-- 房态分组视图 -->
    <div class="room-groups" v-loading="loading">
      <div 
        v-for="group in displayGroups" 
        :key="group.statusCode"
        class="room-group"
      >
        <div class="group-header" :class="{ 'floor-header': group.isFloorGroup }">
          <span class="group-title">
            <template v-if="group.isFloorGroup">
              <el-icon class="floor-icon"><House /></el-icon>
            </template>
            <template v-else>
              <span class="status-dot" :style="{ backgroundColor: getStatusColor(group.statusCode) }"></span>
            </template>
            {{ group.isFloorGroup ? group.statusName + '楼' : group.statusName }}
          </span>
          <span class="group-count">{{ group.roomCount }}间</span>
        </div>
        
        <div class="room-grid">
          <div 
            v-for="room in group.rooms" 
            :key="room.roomId"
            class="room-card"
            :class="{ 'is-occupied': room.status === 'OCCUPIED' }"
            :style="{ 
              borderLeft: '4px solid ' + getStatusColor(room.status),
              backgroundColor: getStatusBgColor(room.status)
            }"
            @click="handleRoomClick(room)"
            @contextmenu.prevent="handleContextMenu($event, room)"
          >
            <div class="room-header">
              <span class="room-no">{{ room.roomNo }}</span>
              <span class="room-type">{{ room.roomTypeName }}</span>
            </div>
            
            <div class="room-body">
              <!-- 空闲房间显示房价 -->
              <template v-if="room.status === 'AVAILABLE'">
                <div class="room-price">
                    <span class="price-label">今日</span>
                    <span class="price-value">¥{{ room.todayPrice || room.basePrice }}</span>
                </div>
              </template>
              
              <!-- 在住房间显示客人信息 -->
              <template v-if="room.status === 'OCCUPIED' && room.currentStay">
                <div class="guest-info">
                  <div class="guest-name">{{ room.currentStay.guestName }}</div>
                  <div class="stay-info">
                    {{ formatDate(room.currentStay.checkInTime) }} - {{ formatDate(room.currentStay.checkOutTime) }}
                  </div>
                  <div class="payment-status" :class="getPaymentStatusClass(room.currentStay.paymentStatus)">
                    {{ getPaymentStatusText(room.currentStay.paymentStatus) }}
                  </div>
                </div>
              </template>
              
              <!-- 预留房间显示预订信息 -->
              <template v-if="room.status === 'RESERVED' && room.futureReservations && room.futureReservations.length > 0">
                <div class="reservation-info">
                  <div class="guest-name">{{ room.futureReservations[0].guestName }}</div>
                  <div class="stay-info">
                    {{ room.futureReservations[0].checkInDate }} 入住
                  </div>
                </div>
              </template>
            </div>
            
            <div class="room-footer">
              <span class="floor-name">{{ room.floorName }}</span>
              <div class="quick-actions">
                <el-dropdown trigger="click" @command="(cmd) => handleQuickAction(cmd, room)">
                  <el-icon class="action-icon"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item 
                        v-for="action in getQuickActions(room.status)" 
                        :key="action.command"
                        :command="action.command"
                      >
                        {{ action.label }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="group.rooms.length === 0" class="empty-group">
            <span>暂无{{ group.statusName }}房间</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 房间详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="currentRoom ? `房间 ${currentRoom.roomNo} 详情` : '房间详情'"
      size="400px"
    >
      <div v-if="currentRoom" class="room-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="房间号">{{ currentRoom.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ currentRoom.floorName }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ currentRoom.roomTypeName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :color="getStatusColor(currentRoom.status)" effect="dark">
              {{ currentRoom.statusName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="基础价格">¥{{ currentRoom.basePrice }}</el-descriptions-item>
          <el-descriptions-item label="今日房价">¥{{ currentRoom.todayPrice }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 当前入住信息 -->
        <div v-if="currentRoom.currentStay" class="detail-section">
          <h4>当前入住</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="客人姓名">{{ currentRoom.currentStay.guestName }}</el-descriptions-item>
            <el-descriptions-item label="入住时间">{{ currentRoom.currentStay.checkInTime }}</el-descriptions-item>
            <el-descriptions-item label="离店时间">{{ currentRoom.currentStay.checkOutTime }}</el-descriptions-item>
            <el-descriptions-item label="已住天数">{{ currentRoom.currentStay.stayDays }}天</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 未来预订 -->
        <div v-if="currentRoom.futureReservations && currentRoom.futureReservations.length > 0" class="detail-section">
          <h4>未来预订</h4>
          <div 
            v-for="reservation in currentRoom.futureReservations" 
            :key="reservation.reservationId"
            class="reservation-item"
          >
            <div class="reservation-guest">{{ reservation.guestName }}</div>
            <div class="reservation-date">
              {{ reservation.checkInDate }} - {{ reservation.checkOutDate }}
            </div>
            <el-tag size="small">{{ getStatusName(reservation.status) }}</el-tag>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="detail-actions">
          <el-button 
            v-for="action in getQuickActions(currentRoom.status)" 
            :key="action.command"
            type="primary"
            @click="handleQuickAction(action.command, currentRoom)"
          >
            {{ action.label }}
          </el-button>
        </div>
      </div>
    </el-drawer>
    
    <!-- 右键菜单 -->
    <div 
      v-show="contextMenu.visible" 
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
    >
      <div 
        v-for="action in contextMenu.actions" 
        :key="action.command"
        class="context-menu-item"
        @click="handleQuickAction(action.command, contextMenu.room)"
      >
        {{ action.label }}
      </div>
    </div>

    <!-- 散客入住对话框 -->
    <el-dialog v-model="walkInDialogVisible" title="散客入住" width="600px" :close-on-click-modal="false">
      <el-form :model="walkInForm" :rules="walkInRules" ref="walkInFormRef" label-width="100px">
        <!-- 房间信息 -->
        <el-divider content-position="left">房间信息</el-divider>
        <el-form-item label="选择房型" prop="roomTypeId">
          <el-select v-model="walkInForm.roomTypeId" placeholder="请选择房型" style="width: 100%" @change="handleRoomTypeChange">
            <el-option v-for="rt in roomTypeOptions" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择房间" prop="roomId">
          <el-select v-model="walkInForm.roomId" placeholder="请选择房间" style="width: 100%" :disabled="roomDisabled || !walkInForm.roomTypeId">
            <el-option v-for="room in availableRooms" :key="room.id" :label="room.roomNo + ' (' + room.roomTypeName + ')'" :value="room.id" />
          </el-select>
        </el-form-item>
        
        <!-- 客人信息 -->
        <el-divider content-position="left">客人信息</el-divider>
        <el-form-item label="客人姓名" prop="guestName">
          <el-input v-model="walkInForm.guestName" placeholder="请输入客人姓名" />
        </el-form-item>
        <el-form-item label="客人电话" prop="guestPhone">
          <el-input v-model="walkInForm.guestPhone" placeholder="请输入客人电话" />
        </el-form-item>
        <el-form-item label="证件号">
          <el-input v-model="walkInForm.guestIdNo" placeholder="请输入证件号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="walkInForm.guestGender" placeholder="请选择性别">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        
        <!-- 入住信息 -->
        <el-divider content-position="left">入住信息</el-divider>
        <el-form-item label="房价码">
          <el-select v-model="walkInForm.pricePlanId" placeholder="选择房价码（可选）" clearable style="width: 100%" @change="handlePricePlanChange">
            <el-option v-for="plan in pricePlanOptions" :key="plan.id" :label="plan.code + ' - ' + plan.name" :value="plan.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单日房价">
          <el-input-number v-model="walkInForm.dailyPrice" :min="0" :precision="2" placeholder="选择房价码后自动填充，可修改" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预计离店" prop="expectedCheckOutDate">
          <el-date-picker v-model="walkInForm.expectedCheckOutDate" type="date" placeholder="选择预计离店日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="walkInDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitWalkIn">确认入住</el-button>
      </template>
    </el-dialog>

    <!-- 续住对话框 -->
    <el-dialog v-model="extendDialogVisible" title="续住" width="600px" :close-on-click-modal="false">
      <el-descriptions :column="2" border class="extend-info">
        <el-descriptions-item label="入住单号">{{ extendForm.stayNo }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ extendForm.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ extendForm.roomTypeName }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ extendForm.guestName }}</el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatDateTime(extendForm.checkInTime) }}</el-descriptions-item>
        <el-descriptions-item label="当前离店时间">{{ formatDateTime(extendForm.checkOutTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="extendForm" :rules="extendRules" ref="extendFormRef" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="新离店日期" prop="newCheckOutDate">
          <el-date-picker v-model="extendForm.newCheckOutDate" type="date" placeholder="选择新离店日期" value-format="YYYY-MM-DD" style="width: 100%" @change="calculateExtendFee" />
        </el-form-item>
      </el-form>
      <div v-if="extendPriceDetails.length > 0" class="price-details">
        <h4>续住期间房价明细</h4>
        <el-table :data="extendPriceDetails" size="small" border>
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="price" label="房价"><template #default="{ row }">¥{{ row.price.toFixed(2) }}</template></el-table-column>
        </el-table>
      </div>
      <div class="fee-summary">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="续住天数">{{ extendSummary.extendedDays || 0 }} 天</el-descriptions-item>
          <el-descriptions-item label="新增费用"><span class="amount">¥{{ (extendSummary.additionalAmount || 0).toFixed(2) }}</span></el-descriptions-item>
          <el-descriptions-item label="原总金额">¥{{ (extendSummary.oldTotalAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="新总金额"><span class="amount highlight">¥{{ (extendSummary.newTotalAmount || 0).toFixed(2) }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="extendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="extendLoading" @click="submitExtend">确认续住</el-button>
      </template>
    </el-dialog>

    <!-- 换房对话框 -->
    <el-dialog v-model="changeRoomDialogVisible" title="换房" width="700px" :close-on-click-modal="false">
      <el-form :model="changeRoomForm" ref="changeRoomFormRef" :rules="changeRoomRules" label-width="100px">
        <el-descriptions :column="2" border class="change-room-info" style="margin-bottom: 20px;">
          <el-descriptions-item label="入住单号">{{ changeRoomForm.stayNo }}</el-descriptions-item>
          <el-descriptions-item label="客人姓名">{{ changeRoomForm.guestName }}</el-descriptions-item>
          <el-descriptions-item label="当前房号">{{ changeRoomForm.oldRoomNo }}</el-descriptions-item>
          <el-descriptions-item label="当前房型">{{ changeRoomForm.oldRoomTypeName }}</el-descriptions-item>
          <el-descriptions-item label="入住时间">{{ formatDate(changeRoomForm.checkInTime) }}</el-descriptions-item>
          <el-descriptions-item label="预计离店">{{ formatDate(changeRoomForm.checkOutTime) }}</el-descriptions-item>
          <el-descriptions-item label="当前总金额">¥{{ (changeRoomForm.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        </el-descriptions>
        
        <el-form-item label="新房间" prop="newRoomId">
          <el-select v-model="changeRoomForm.newRoomId" filterable placeholder="请选择新房间" style="width: 100%" @change="onNewRoomChange">
            <el-option v-for="room in changeAvailableRooms" :key="room.id" :label="room.roomNo + ' - ' + room.roomTypeName" :value="room.id" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="changeRoomForm.newRoomId" label="新房型">
          <el-input :value="changeRoomForm.newRoomTypeName" disabled />
        </el-form-item>
        
        <el-form-item label="换房原因">
          <el-input v-model="changeRoomForm.reason" type="textarea" :rows="3" placeholder="请输入换房原因（可选）" />
        </el-form-item>
        
        <el-alert v-if="changeRoomForm.amountAdjustment !== 0" 
          :title="changeRoomForm.amountAdjustment > 0 ? '需加收费用' : '将退还费用'" 
          :description="'¥' + Math.abs(changeRoomForm.amountAdjustment || 0).toFixed(2)"
          :type="changeRoomForm.amountAdjustment > 0 ? 'warning' : 'success'"
          show-icon style="margin-bottom: 20px;" />
      </el-form>
      <template #footer>
        <el-button @click="changeRoomDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="changeRoomLoading" @click="submitChangeRoom">确认换房</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import { useUserStore } from '@/stores/user'
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, MoreFilled, House, User, Brush, SetUp, WarningFilled, CircleCheck, Calendar 
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getStayList, checkOut } from '@/api/stay'
const userStore = useUserStore()

const route = useRoute()
  
const loading = ref(false)
const dashboardData = ref({
  totalRooms: 0,
  statusCounts: {},
  statusGroups: []
})
const selectedFloor = ref(null)
const selectedStatus = ref(null)
const viewMode = ref('floor') // floor or status
const floorOptions = ref([])
const drawerVisible = ref(false)
  // 散客入住相关
  const walkInDialogVisible = ref(false)
  const roomDisabled = ref(false)
  const walkInFormRef = ref(null)
  const submitLoading = ref(false)
  const walkInForm = reactive({
    roomTypeId: '',
    roomId: '',
    guestName: '',
    guestIdNo: '',
    guestPhone: '',
    guestGender: '',
    expectedCheckOutDate: '',
    pricePlanId: '',
    dailyPrice: null,
    coGuests: []
  })
  const walkInRules = {
    roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
    roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
    guestName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
    guestPhone: [{ required: true, message: '请输入客人电话', trigger: 'blur' }],
    expectedCheckOutDate: [{ required: true, message: '请选择预计离店日期', trigger: 'change' }]
  }
  const roomTypeOptions = ref([])
  const availableRooms = ref([])

  // 续住相关
  const extendDialogVisible = ref(false)
  const extendLoading = ref(false)
  const extendFormRef = ref(null)
  const extendForm = reactive({ 
    stayId: '', stayNo: '', roomNo: '', roomTypeName: '', 
    guestName: '', checkInTime: '', checkOutTime: '', 
    roomId: '', hotelId: '', roomTypeId: '', newCheckOutDate: '' 
  })
  const extendRules = { 
    newCheckOutDate: [{ required: true, message: '请选择新离店日期', trigger: 'change' }] 
  }
  const extendPriceDetails = ref([])
  const extendSummary = reactive({ extendedDays: 0, additionalAmount: 0, oldTotalAmount: 0, newTotalAmount: 0 })
  
  // 换房相关
  const changeRoomDialogVisible = ref(false)
  const changeRoomLoading = ref(false)
  const changeRoomFormRef = ref(null)
  const changeAvailableRooms = ref([])
  const changeRoomForm = reactive({
    stayId: '',
    stayNo: '',
    guestName: '',
    oldRoomId: '',
    oldRoomNo: '',
    oldRoomTypeName: '',
    checkInTime: '',
    checkOutTime: '',
    totalAmount: 0,
    newRoomId: '',
    newRoomNo: '',
    newRoomTypeName: '',
    reason: '',
    amountAdjustment: 0
  })
  const changeRoomRules = {
    newRoomId: [{ required: true, message: '请选择新房间', trigger: 'change' }]
  }
  const pricePlanOptions = ref([])
const currentRoom = ref(null)

// 右键菜单
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  room: null,
  actions: []
})

// 统计卡片配置
const statCards = computed(() => [
  { 
    status: null, 
    label: '全部', 
    count: dashboardData.value.totalRooms, 
    color: '#909399',
    icon: 'House'
  },
  { 
    status: 'AVAILABLE', 
    label: '空闲', 
    count: dashboardData.value.statusCounts?.AVAILABLE || 0, 
    color: '#67C23A',
    icon: 'CircleCheck'
  },
  { 
    status: 'OCCUPIED', 
    label: '在住', 
    count: dashboardData.value.statusCounts?.OCCUPIED || 0, 
    color: '#409EFF',
    icon: 'User'
  },
  { 
    status: 'DIRTY', 
    label: '脏房', 
    count: dashboardData.value.statusCounts?.DIRTY || 0, 
    color: '#E6A23C',
    icon: 'Brush'
  },
  { 
    status: 'MAINTENANCE', 
    label: '维修', 
    count: dashboardData.value.statusCounts?.MAINTENANCE || 0, 
    color: '#F56C6C',
    icon: 'SetUp'
  },
  { 
    status: 'OOO', 
    label: '停用', 
    count: dashboardData.value.statusCounts?.OOO || 0, 
    color: '#909399',
    icon: 'WarningFilled'
  },
  { 
    status: 'RESERVED', 
    label: '预留', 
    count: dashboardData.value.statusCounts?.RESERVED || 0, 
    color: '#9B59B6',
    icon: 'Calendar'
  }
])


// 按楼层分组的房间数据（用于默认视图）
const floorGroups = computed(() => {
  const allRooms = []
  // 从 statusGroups 中收集所有房间
  if (dashboardData.value.statusGroups) {
    dashboardData.value.statusGroups.forEach(group => {
      if (group.rooms) {
        allRooms.push(...group.rooms)
      }
    })
  }
  
  // 按楼层分组
  const floorMap = {}
  allRooms.forEach(room => {
    const floorId = room.floorId || 'unknown'
    const floorName = room.floorName || '未知楼层'
    if (!floorMap[floorId]) {
      floorMap[floorId] = {
        floorId,
        floorName,
        rooms: [],
        roomCount: 0
      }
    }
    floorMap[floorId].rooms.push(room)
    floorMap[floorId].roomCount++
  })
  
  // 按楼层号排序
  return Object.values(floorMap).sort((a, b) => {
    const numA = parseInt(a.floorName) || 0
    const numB = parseInt(b.floorName) || 0
    return numA - numB
  })
})

// 当前显示的分组数据（根据 viewMode 切换）
const displayGroups = computed(() => {
  if (viewMode.value === 'floor') {
    // 楼层视图：返回楼层分组格式（兼容模板）
    return floorGroups.value.map(g => ({
      statusCode: 'FLOOR_' + g.floorId,
      statusName: g.floorName,
      roomCount: g.roomCount,
      rooms: g.rooms,
      isFloorGroup: true
    }))
  } else {
    // 状态视图：返回原始状态分组
    return dashboardData.value.statusGroups || []
  }
})

// 状态名称映射
const statusNameMap = {
  'AVAILABLE': '空闲',
  'OCCUPIED': '在住',
  'DIRTY': '脏房',
  'MAINTENANCE': '维修',
  'OOO': '停用',
  'RESERVED': '预留',
  'PENDING': '待确认',
  'CONFIRMED': '已确认',
  'CHECKED_IN': '已入住',
  'CHECKED_OUT': '已离店',
  'CANCELLED': '已取消',
  'NO_SHOW': '未到店'
}

// 状态颜色映射
const statusColorMap = {
  'AVAILABLE': '#67C23A',
  'OCCUPIED': '#409EFF',
  'DIRTY': '#E6A23C',
  'MAINTENANCE': '#F56C6C',
  'OOO': '#909399',
  'RESERVED': '#9B59B6'
}

// 状态对应的浅色背景
const statusBgColorMap = {
  'AVAILABLE': '#f0f9eb',
  'OCCUPIED': '#ecf5ff',
  'DIRTY': '#fdf6ec',
  'MAINTENANCE': '#fef0f0',
  'OOO': '#f4f4f5',
  'RESERVED': '#f4ecf7'
}

// 获取状态名称
const getStatusName = (status) => statusNameMap[status] || status

// 获取状态颜色
const getStatusColor = (status) => statusColorMap[status] || '#909399'
const getStatusBgColor = (status) => statusBgColorMap[status] || '#fafafa'

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return d.getFullYear() + '/' + pad(d.getMonth()+1) + '/' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
}


  // 获取付款状态样式
  const getPaymentStatusClass = (status) => {
    switch (status) {
      case 'PAID': return 'paid'
      case 'PARTIAL': return 'partial'
      case 'UNPAID':
      default: return 'unpaid'
    }
  }

  // 获取付款状态文本
  const getPaymentStatusText = (status) => {
    switch (status) {
      case 'PAID': return '已付清'
      case 'PARTIAL': return '部分付款'
      case 'UNPAID':
      default: return '未付款'
    }
  }
  // 获取快速操作列表
const getQuickActions = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return [
        { command: 'checkin', label: '办理入住' },
        { command: 'reserve', label: '设置预留' },
        { command: 'maintenance', label: '设置维修' },
        { command: 'ooo', label: '设置停用' }
      ]
    case 'OCCUPIED':
      return [
        { command: 'checkout', label: '退房' },
        { command: 'extend', label: '续住' },
        { command: 'changeRoom', label: '换房' },
        { command: 'dirty', label: '置脏房' }
      ]
    case 'DIRTY':
      return [
        { command: 'clean', label: '清洁完成' },
        { command: 'available', label: '设置空闲' }
      ]
    case 'MAINTENANCE':
      return [
        { command: 'available', label: '维修完成' }
      ]
    case 'OOO':
      return [
        { command: 'available', label: '启用' }
      ]
    case 'RESERVED':
      return [
        { command: 'checkin', label: '办理入住' },
        { command: 'cancel', label: '取消预留' },
        { command: 'available', label: '设置空闲' }
      ]
    default:
      return []
  }
}

// 加载房态数据
const loadDashboard = async () => {
  loading.value = true
  try {
    const params = { hotelId: userStore.hotelId }
    if (selectedFloor.value) {
      params.floorId = selectedFloor.value
    }
    if (selectedStatus.value) {
      params.status = selectedStatus.value
    }
    
    const res = await request.get('/v1/rooms/dashboard', { params })
    dashboardData.value = res.data
  } catch (error) {
    console.error('加载房态数据失败:', error)
    ElMessage.error('加载房态数据失败')
  } finally {
    loading.value = false
  }
}

// 加载楼层选项
const loadFloorOptions = async () => {
  try {
    const res = await request.get('/v1/floors/list', { params: { hotelId: userStore.hotelId } })
    floorOptions.value = res.data
  } catch (error) {
    console.error('加载楼层列表失败:', error)
  }
}

// 刷新
const handleRefresh = () => {
  loadDashboard()
}

// 楼层筛选
const handleFloorChange = () => {
  loadDashboard()
}

// 状态筛选
const handleStatusFilter = (status) => {
  if (selectedStatus.value === status) {
    // 取消筛选，回到楼层视图
    selectedStatus.value = null
    viewMode.value = 'floor'
  } else if (status === null) {
    // 点击"全部"卡片，回到楼层视图
    selectedStatus.value = null
    viewMode.value = 'floor'
  } else {
    // 点击某个状态卡片，切换到状态视图
    selectedStatus.value = status
    viewMode.value = 'status'
  }
  loadDashboard()
}

// 清除状态筛选
const handleClearStatusFilter = () => {
  selectedStatus.value = null
  viewMode.value = 'floor'
  loadDashboard()
}

// 点击房间卡片
const handleRoomClick = (room) => {
  currentRoom.value = room
  drawerVisible.value = true
}

// 右键菜单
const handleContextMenu = (event, room) => {
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    room: room,
    actions: getQuickActions(room.status)
  }
}

// 快速操作
  // 设置房间状态
  const handleSetStatus = async (room, newStatus, confirmMessage) => {
    try {
      await ElMessageBox.confirm(confirmMessage, '确认操作', { type: 'warning' })
      const res = await request.put(`/v1/rooms/${room.roomId}/status`, null, { params: { status: newStatus } })
      ElMessage.success('操作成功')
      drawerVisible.value = false
      loadDashboard() // 刷新房态
    } catch (error) {
      if (error !== 'cancel') {
        console.error('操作失败:', error)
        ElMessage.error('操作失败: ' + (error.message || '未知错误'))
      }
    }
  }

const handleQuickAction = async (command, room) => {
  contextMenu.value.visible = false
  
  switch (command) {
    case 'checkin':
      await handleCheckIn(room)
      break
    case 'checkout':
      await handleCheckOut(room)
      break
    case 'dirty':
      await handleSetStatus(room, 'DIRTY', '确认将房间设置为脏房？')
      break
    case 'clean':
    case 'available':
      await handleSetStatus(room, 'AVAILABLE', '确认将房间设置为空闲？')
      break
    case 'maintenance':
      await handleSetStatus(room, 'MAINTENANCE', '确认将房间设置为维修状态？')
      break
    case 'ooo':
      await handleSetStatus(room, 'OOO', '确认将房间设置为停用状态？')
      break
    case 'reserve':
      await handleSetStatus(room, 'RESERVED', '确认将房间设置为预留状态？')
      break
    case 'cancel':
      await handleSetStatus(room, 'AVAILABLE', '确认取消预留？')
      break
    case 'extend':
      await showExtendDialog(room)
      break
    case 'changeRoom':
      await showChangeRoomDialog(room)
      break
    default:
      break
  }
}

// 办理入住
  // 办理入住 - 弹出入住表单
  const handleCheckIn = async (room) => {
    await showWalkInDialog(room)
  }
  // 退房
  const handleCheckOut = async (room) => {
    try {
      const res = await getStayList({ hotelId: room.hotelId || 1, status: 'CHECKED_IN', size: 100 })
      const stays = res.data?.records || []
      const stay = stays.find(s => s.roomId === room.roomId)
      if (!stay) { ElMessage.error('未找到对应的入住单'); return }
      await ElMessageBox.confirm(
        '确认为房间 ' + room.roomNo + ' 的客人 ' + (stay.guestName || '') + ' 办理退房？',
        '退房确认',
        { confirmButtonText: '确认退房', cancelButtonText: '取消', type: 'warning' }
      )
      const result = await checkOut({ stayId: stay.id, remark: '' })
      ElMessage.success('退房成功')
      loadDashboard()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('退房失败', error)
        ElMessage.error('退房失败: ' + (error.message || '未知错误'))
      }
    }
  }

  // 获取房型列表
  const fetchRoomTypes = async () => {
    try {
      const params = { hotelId: userStore.hotelId }
      const res = await request.get('/v1/room-types', { params })
      roomTypeOptions.value = res.data?.records || []
    } catch (error) {
      console.error('获取房型列表失败:', error)
    }
  }

  // 房价码变化处理
  const handlePricePlanChange = async (planId) => {
    if (planId) {
      try {
        // 如果已选择房间，通过房间获取房型ID查询房价
        if (walkInForm.roomId) {
          const roomRes = await request.get('/v1/rooms/' + walkInForm.roomId)
          if (roomRes.data?.roomTypeId) {
            const res = await request.get('/v1/prices/query', { params: { hotelId: userStore.hotelId, roomTypeId: roomRes.data.roomTypeId, date: new Date().toISOString().split('T')[0] } })
            if (res.data?.price) {
              walkInForm.dailyPrice = res.data.price
            }
          }
        } else {
          // 未选择房间时，通过房价码获取默认价格
          const planRes = await request.get('/v1/price-plans/' + planId)
          if (planRes.data?.details && planRes.data.details.length > 0) {
            walkInForm.dailyPrice = planRes.data.details[0].finalPrice
          }
        }
      } catch (error) {
        console.error('获取房价失败', error)
      }
    } else {
      walkInForm.dailyPrice = null
    }
  }

  // 获取房价码列表
  const fetchPricePlans = async () => {
    try {
      const params = { hotelId: userStore.hotelId, status: 'ACTIVE' }
      const res = await request.get('/v1/price-plans', { params })
      pricePlanOptions.value = res.data?.records || []
    } catch (error) {
      console.error('获取房价码列表失败:', error)
    }
  }

  // 房型变更时获取可用房间
  const handleRoomTypeChange = async (roomTypeId, skipClear = false, currentRoomId = null) => {
      if (!skipClear) walkInForm.roomId = ''
      if (!roomTypeId) {
        availableRooms.value = []
        return
      }
      try {
        const params = { hotelId: userStore.hotelId, status: 'AVAILABLE', roomTypeId }
        const res = await request.get('/v1/rooms', { params })
        availableRooms.value = res.data?.records || []
        // 如果传入了当前房间ID，且不在列表中，添加到列表
        if (currentRoomId && !availableRooms.value.find(r => r.id === currentRoomId)) {
          // 获取当前房间信息并添加
          const roomRes = await request.get(`/v1/rooms/${currentRoomId}`)
          if (roomRes.data) {
            availableRooms.value.unshift(roomRes.data)
          }
        }
      } catch (error) {
        console.error('获取可用房间失败:', error)
      }
    }

  // 显示入住对话框
  const showWalkInDialog = async (room) => {
    // 如果传入了房间，禁用房间选择
    roomDisabled.value = !!room?.roomId
    // 重置表单
    Object.assign(walkInForm, {
      roomTypeId: '',
      roomId: room.roomId || '',
      guestName: '',
      guestIdNo: '',
      guestPhone: '',
      guestGender: '',
      expectedCheckOutDate: '',
      pricePlanId: '',
      coGuests: []
    })
    // 加载房型和房价码
    await Promise.all([fetchRoomTypes(), fetchPricePlans()])
    // 如果传入了房间，自动选择对应的房型
    if (room.roomTypeId) {
      walkInForm.roomTypeId = room.roomTypeId
      await handleRoomTypeChange(room.roomTypeId, true, room.roomId)
      walkInForm.roomId = room.roomId
    }
    walkInDialogVisible.value = true
  }

  // 提交入住
  const submitWalkIn = async () => {
    const valid = await walkInFormRef.value.validate().catch(() => false)
    if (!valid) return
    submitLoading.value = true
    try {
      const res = await request.post('/v1/stays/walk-in', { ...walkInForm, hotelId: userStore.hotelId })
      ElMessage.success('入住成功')
      walkInDialogVisible.value = false
      drawerVisible.value = false // 关闭房间详情弹框
      loadDashboard() // 刷新房态
    } catch (error) {
      console.error('散客入住失败:', error)
      ElMessage.error('入住失败: ' + (error.message || '未知错误'))
    } finally {
      submitLoading.value = false
    }
  }

  // 显示续住对话框
  const showExtendDialog = async (room) => {
    try {
      const res = await getStayList({ hotelId: room.hotelId || 1, status: 'CHECKED_IN', size: 100 })
      const stays = res.data?.records || []
      const stay = stays.find(s => s.roomId === room.roomId)
      if (!stay) { ElMessage.error('未找到对应的入住单'); return }
      extendForm.stayId = stay.id
      extendForm.stayNo = stay.stayNo || room.currentStay?.stayNo || ''
      extendForm.roomNo = room.roomNo
      extendForm.roomTypeName = room.roomTypeName
      extendForm.guestName = stay.guestName || room.currentStay?.guestName || ''
      extendForm.checkInTime = stay.checkInTime
      extendForm.checkOutTime = stay.checkOutTime || ''
      extendForm.roomId = room.roomId
      extendForm.hotelId = userStore.hotelId
      extendForm.roomTypeId = room.roomTypeId
      extendForm.newCheckOutDate = ''
      extendPriceDetails.value = []
      extendSummary.extendedDays = 0
      extendSummary.additionalAmount = 0
      extendSummary.oldTotalAmount = room.todayPrice || 0
      extendSummary.newTotalAmount = room.todayPrice || 0
      extendDialogVisible.value = true
    } catch (error) {
      console.error('查询入住单失败', error)
    }
  }

  // 计算续住费用
  const calculateExtendFee = async (newDate) => {
    if (!newDate) {
      extendPriceDetails.value = []
      extendSummary.extendedDays = 0
      extendSummary.additionalAmount = 0
      extendSummary.newTotalAmount = extendSummary.oldTotalAmount
      return
    }
    const checkOutDate = new Date(extendForm.checkOutTime)
    const newCheckOutDate = new Date(newDate)
    const days = Math.ceil((newCheckOutDate - checkOutDate) / (1000 * 60 * 60 * 24))
    extendSummary.extendedDays = days
    
    try {
      const priceDetails = []
      let totalAdditional = 0
      for (let i = 0; i < days; i++) {
        const date = new Date(checkOutDate)
        date.setDate(date.getDate() + i + 1)
        const dateStr = date.toISOString().split('T')[0]
        const res = await request({ url: '/v1/prices/query', method: 'get', params: { hotelId: extendForm.hotelId, roomTypeId: extendForm.roomTypeId, date: dateStr } })
        const price = res.data?.price || 0
        priceDetails.push({ date: dateStr, price: price })
        totalAdditional += price
      }
      extendPriceDetails.value = priceDetails
      extendSummary.additionalAmount = totalAdditional
      extendSummary.newTotalAmount = extendSummary.oldTotalAmount + totalAdditional
    } catch (error) {
      console.error('计算续住费用失败:', error)
    }
  }

  // 提交续住
  const submitExtend = async () => {
    const valid = await extendFormRef.value.validate().catch(() => false)
    if (!valid) return
    extendLoading.value = true
    try {
      await request.put('/v1/stays/extend', { stayId: extendForm.stayId, newCheckOutDate: extendForm.newCheckOutDate })
      ElMessage.success('续住成功')
      extendDialogVisible.value = false
      drawerVisible.value = false
      loadDashboard()
    } catch (error) {
      console.error('续住失败', error)
      ElMessage.error('续住失败: ' + (error.message || '未知错误'))
    } finally {
      extendLoading.value = false
    }
  }

  // 显示换房对话框
  const showChangeRoomDialog = async (room) => {
    try {
      const res = await getStayList({ hotelId: room.hotelId || 1, status: 'CHECKED_IN', size: 100 })
      const stays = res.data?.records || []
      const stay = stays.find(s => s.roomId === room.roomId)
      if (!stay) { ElMessage.error('未找到对应的入住单'); return }
      changeRoomForm.stayId = stay.id
      changeRoomForm.stayNo = stay.stayNo || room.currentStay?.stayNo || ''
      changeRoomForm.guestName = stay.guestName || room.currentStay?.guestName || ''
      changeRoomForm.oldRoomId = room.roomId
      changeRoomForm.oldRoomNo = room.roomNo
      changeRoomForm.oldRoomTypeName = room.roomTypeName
      changeRoomForm.checkInTime = stay.checkInTime
      changeRoomForm.checkOutTime = stay.checkOutTime || ''
      changeRoomForm.totalAmount = room.todayPrice || 0
      changeRoomForm.newRoomId = ''
      changeRoomForm.newRoomNo = ''
      changeRoomForm.newRoomTypeName = ''
      changeRoomForm.reason = ''
      changeRoomForm.amountAdjustment = 0
      
      // 加载可用房间列表
      const roomRes = await request.get('/v1/rooms', { params: { hotelId: userStore.hotelId, status: 'AVAILABLE', roomTypeId: room.roomTypeId } })
      changeAvailableRooms.value = (roomRes.data?.records || []).filter(r => r.id !== room.roomId)
      changeRoomDialogVisible.value = true
    } catch (error) {
      console.error('查询入住单或可用房间失败', error)
    }
  }

  // 新房间选择变化
  const onNewRoomChange = (roomId) => {
    const room = changeAvailableRooms.value.find(r => r.id === roomId)
    if (room) {
      changeRoomForm.newRoomNo = room.roomNo
      changeRoomForm.newRoomTypeName = room.roomTypeName
      changeRoomForm.amountAdjustment = 0
    }
  }

  // 提交换房
  const submitChangeRoom = async () => {
    const valid = await changeRoomFormRef.value.validate().catch(() => false)
    if (!valid) return
    
    changeRoomLoading.value = true
    try {
      await request.put('/v1/stays/change-room', {
        stayId: changeRoomForm.stayId,
        newRoomId: changeRoomForm.newRoomId,
        reason: changeRoomForm.reason
      })
      ElMessage.success('换房成功')
      changeRoomDialogVisible.value = false
      drawerVisible.value = false
      loadDashboard()
    } catch (error) {
      console.error('换房失败', error)
      ElMessage.error('换房失败: ' + (error.message || '未知错误'))
    } finally {
      changeRoomLoading.value = false
    }
  }


// 页面加载时获取数据
// 监听路由参数变化
watch(() => route.query.status, (newStatus) => {
  selectedStatus.value = newStatus || null
  loadDashboard()
}, { immediate: true })

onMounted(() => {
    // 处理路由参数
    if (route.query.status) {
      selectedStatus.value = route.query.status
    }
    loadDashboard()
    loadFloorOptions()
  })

</script>

<style scoped lang="scss">
.room-board-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

// 页面头部
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

// 统计面板
.stat-panel {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 16px;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  @media (max-width: 1400px) {
    grid-template-columns: repeat(4, 1fr);
  }
  
  @media (max-width: 900px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &.active {
    border: 2px solid var(--el-color-primary);
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    
    .el-icon {
      font-size: 24px;
      color: white;
    }
  }
  
  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
      line-height: 1;
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

// 筛选栏
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .el-select {
    width: 200px;
  }
}

// 房态分组
.room-groups {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.room-group {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .group-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;
    
    .group-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      
      .status-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        margin-right: 8px;
      }
    }
    
    .group-count {
      font-size: 14px;
      color: #909399;
      background: #f5f7fa;
      padding: 4px 12px;
      border-radius: 12px;
    }
  }
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.room-card {
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 4px solid #909399;
  
  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
  
  &.is-occupied {
    border-color: #b3d8ff;
  }
  
  .room-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .room-no {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
    
    .room-type {
      font-size: 12px;
      color: #909399;
      background: #f0f2f5;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }
  
  .room-body {
    min-height: 40px;
    margin-bottom: 8px;
    
    .room-price {
      display: flex;
      align-items: baseline;
      gap: 4px;
      
      .price-label {
        font-size: 12px;
        color: #909399;
      }
      
      .price-value {
        font-size: 20px;
        font-weight: 600;
        color: #f56c6c;
      }
    }
    
    .guest-info {
      .guest-name {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
      }
      
      
  .payment-status {
    font-size: 11px;
    padding: 2px 6px;
    border-radius: 3px;
    margin-top: 4px;
    display: inline-block;
    
    &.paid {
      background-color: #f0f9eb;
      color: #67c23a;
    }
    
    &.partial {
      background-color: #fdf6ec;
      color: #e6a23c;
    }
    
    &.unpaid {
      background-color: #fef0f0;
      color: #f56c6c;
    }
  }
  .stay-info {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .reservation-info {
      .guest-name {
        font-size: 14px;
        font-weight: 500;
        color: #9B59B6;
        margin-bottom: 4px;
      }
      
      .stay-info {
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .room-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 8px;
    border-top: 1px solid #ebeef5;
    
    .floor-name {
      font-size: 12px;
      color: #909399;
    }
    
    .quick-actions {
      .action-icon {
        cursor: pointer;
        color: #909399;
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }
}

.empty-group {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
  color: #909399;
}

// 房间详情
.room-detail {
  padding: 20px;
  
  .detail-section {
    margin-top: 24px;
    
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
    }
  }
  
  .reservation-item {
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 8px;
    
    .reservation-guest {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .reservation-date {
      font-size: 12px;
      color: #909399;
      margin-bottom: 8px;
    }
  }
  
  .detail-actions {
    margin-top: 24px;
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
}

// 右键菜单
.context-menu {
  position: fixed;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  padding: 8px 0;
  
  .context-menu-item {
    padding: 10px 20px;
    cursor: pointer;
    font-size: 14px;
    color: #606266;
    transition: background-color 0.2s;
    
    &:hover {
      background-color: #ecf5ff;
      color: var(--el-color-primary);
    }
  }
}

// 楼层分组样式
.floor-header {
  .floor-icon {
    width: 12px;
    height: 12px;
    margin-right: 8px;
    color: #409EFF;
    font-size: 16px;
  }
}
</style>






