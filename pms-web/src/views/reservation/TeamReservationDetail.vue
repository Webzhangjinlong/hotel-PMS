<template>
  <div class="team-reservation-detail">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>{{ isCreateMode ? '新建团队预订' : '团队预订详情' }}</h2>
      </div>
      <div class="header-actions" v-if="!isCreateMode">
        <el-button 
          v-if="reservation.status === 'PENDING'" 
          type="success" 
          @click="handleConfirm"
        >确认预订</el-button>
        <el-button 
          v-if="reservation.status === 'PENDING'" 
          type="warning" 
          @click="isEdit = true"
        >编辑</el-button>
        <el-button 
          v-if="reservation.status === 'PENDING' || reservation.status === 'CONFIRMED'" 
          type="danger" 
          @click="handleCancel"
        >取消预订</el-button>
        <el-button 
          v-if="reservation.status === 'CONFIRMED'" 
          type="primary" 
          @click="showCheckInDialog = true"
        >办理入住</el-button>
        <el-button 
          v-if="reservation.status === 'CHECKED_IN'" 
          type="warning" 
          @click="handleCheckOut"
        >办理退房</el-button>
        <el-button 
          v-if="reservation.status === 'CHECKED_IN'" 
          type="primary" 
          @click="showExtendDialog = true"
        >团队续住</el-button>
      </div>
    </div>

    <!-- 创建表单 -->
    <el-card v-if="isCreateMode" class="form-card" shadow="never">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="团队名称" prop="teamName">
              <el-input v-model="createForm.teamName" placeholder="请输入团队/公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结算方式" prop="settlementType">
              <el-select v-model="createForm.settlementType" placeholder="请选择结算方式" style="width: 100%">
                <el-option label="统一结算" value="UNIFIED" />
                <el-option label="分开结算" value="SEPARATE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="createForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="createForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="证件号">
              <el-input v-model="createForm.contactIdNo" placeholder="请输入证件号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="入住日期" prop="checkInDate">
              <el-date-picker v-model="createForm.checkInDate" type="date" placeholder="选择入住日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="离店日期" prop="checkOutDate">
              <el-date-picker v-model="createForm.checkOutDate" type="date" placeholder="选择离店日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="特殊要求">
              <el-input v-model="createForm.specialRequests" placeholder="请输入特殊要求" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="房价码">
              <el-select v-model="createForm.pricePlanId" placeholder="选择房价码（可选）" clearable style="width: 100%">
                <el-option v-for="plan in pricePlanOptions" :key="plan.id" :label="plan.code + ' - ' + plan.name" :value="plan.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 房间明细 -->
        <el-divider content-position="left">房间明细</el-divider>
        <el-button type="primary" size="small" @click="addRoom" style="margin-bottom: 15px;">
          <el-icon><Plus /></el-icon> 添加房间
        </el-button>
        <el-table :data="createForm.rooms" border>
          <el-table-column label="房型" min-width="150">
            <template #default="{ row }">
              <el-select v-model="row.roomTypeId" placeholder="选择房型" style="width: 100%">
                <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.name" :value="rt.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="客人姓名" width="120">
            <template #default="{ row }">
              <el-input v-model="row.guestName" placeholder="姓名" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="客人电话" width="130">
            <template #default="{ row }">
              <el-input v-model="row.guestPhone" placeholder="电话" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="证件号" width="150">
            <template #default="{ row }">
              <el-input v-model="row.guestIdNo" placeholder="证件号" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="性别" width="80">
            <template #default="{ row }">
              <el-select v-model="row.guestGender" placeholder="性别" size="small">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button type="danger" size="small" link @click="removeRoom($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 20px; text-align: right;">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="handleCreate" :loading="createLoading">创建预订</el-button>
        </div>
      </el-form>
    </el-card>

    <div v-if="!isCreateMode">
      <!-- 基本信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-tag :type="getStatusType(reservation.status)">
              {{ getStatusLabel(reservation.status) }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="团队预订号">{{ reservation.teamReservationNo }}</el-descriptions-item>
          <el-descriptions-item label="团队名称">{{ reservation.teamName }}</el-descriptions-item>
          <el-descriptions-item label="结算方式">{{ reservation.settlementType === 'UNIFIED' ? '统一结算' : '分开结算' }}</el-descriptions-item>
          <el-descriptions-item label="房价码">{{ getPricePlanName(reservation.pricePlanId) }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ reservation.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ reservation.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="证件号">{{ reservation.contactIdNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ reservation.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="离店日期">{{ reservation.checkOutDate }}</el-descriptions-item>
          <el-descriptions-item label="房晚数">{{ reservation.nights }}</el-descriptions-item>
          <el-descriptions-item label="房间数">{{ reservation.totalRooms }}</el-descriptions-item>
          <el-descriptions-item label="总金额">
            <span class="amount">&yen;{{ reservation.totalAmount?.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ reservation.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="特殊要求" :span="3">{{ reservation.specialRequests || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 房间明细 -->
      <el-card class="room-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>房间明细</span>
            <el-button 
              v-if="reservation.status === 'CONFIRMED'" 
              type="primary" 
              size="small"
              @click="openAssignDialog"
            >分配房间</el-button>
          </div>
        </template>
        <el-table :data="reservation.rooms" border stripe>
          <el-table-column prop="roomTypeName" label="房型" width="120" />
          <el-table-column prop="roomNo" label="房间号" width="100">
            <template #default="{ row }">
              {{ row.roomNo || '待分配' }}
            </template>
          </el-table-column>
          <el-table-column prop="guestName" label="客人姓名" width="100" />
          <el-table-column prop="guestPhone" label="客人电话" width="120" />
          <el-table-column prop="guestGender" label="性别" width="80" />
          <el-table-column prop="amount" label="金额" width="100" align="right">
            <template #default="{ row }">
              <span class="amount">&yen;{{ row.amount?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getRoomStatusType(row.status)" size="small">
                {{ getRoomStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="checkInTime" label="入住时间" width="160" />
          <el-table-column prop="checkOutTime" label="退房时间" width="160" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button
                v-if="canSplitRoom(row)"
                type="danger"
                link
                size="small"
                @click="handleSplitRoom(row)"
              >拆分</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    <!-- 团队账务信息（统一结算） -->
    <el-card v-if="!isCreateMode && reservation.status === 'CHECKED_IN' && reservation.settlementType === 'UNIFIED'" class="folio-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>团队账务信息</span>
          <el-button type="success" size="small" @click="showTeamPaymentDialog">
            <el-icon><Money /></el-icon>
            团队收款
          </el-button>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="账务单号">{{ teamFolio.folioNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span class="amount">¥{{ (teamFolio.totalAmount || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="已付金额">
          <span class="amount paid">¥{{ (teamFolio.paidAmount || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="待付金额">
          <span class="amount highlight">¥{{ (teamFolio.balance || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="getTeamPaymentStatusType()" size="small">
            {{ getTeamPaymentStatusText() }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="结算方式">统一结算</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 团队收款弹窗 -->
    <el-dialog v-model="teamPaymentDialogVisible" title="团队收款" width="500px" :close-on-click-modal="false">
      <el-descriptions :column="2" border class="payment-info">
        <el-descriptions-item label="团队名称">{{ reservation.teamName }}</el-descriptions-item>
        <el-descriptions-item label="账务单号">{{ teamFolio.folioNo }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ (teamFolio.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="待付金额">
          <span class="amount highlight">¥{{ (teamFolio.balance || 0).toFixed(2) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-form :model="teamPaymentForm" :rules="teamPaymentRules" ref="teamPaymentFormRef" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="收款金额" prop="amount">
          <el-input-number v-model="teamPaymentForm.amount" :min="0.01" :max="teamFolio.balance" :precision="2" style="width: 100%" placeholder="请输入收款金额" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="teamPaymentForm.paymentMethod" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银行卡" value="BANK_CARD" />
            <el-option label="挂账" value="CREDIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="teamPaymentForm.remark" type="textarea" :rows="2" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="teamPaymentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="teamPaymentLoading" @click="submitTeamPayment">确认收款</el-button>
      </template>
    </el-dialog>
    </div>

    <!-- 分配房间弹窗 -->
    <el-dialog v-model="showAssignDialog" title="分配房间" width="600px">
      <el-table :data="unassignedRooms" border>
        <el-table-column prop="guestName" label="客人姓名" />
        <el-table-column prop="roomTypeName" label="房型" />
        <el-table-column label="分配房间" width="200">
          <template #default="{ row }">
            <el-select 
              v-model="row._assignedRoomId" 
              placeholder="选择房间" 
              clearable
              style="width: 100%"
            >
              <el-option 
                v-for="room in getFilteredRooms(row.roomTypeId)" 
                :key="room.id" 
                :label="room.roomNo + ' (' + room.roomTypeName + ')'" 
                :value="room.id" 
              />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showAssignDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRooms" :loading="assignLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 入住弹窗 -->
    <el-dialog v-model="showCheckInDialog" title="办理入住" width="500px">
      <p>确定要为所有已分配房间的预订办理入住吗？</p>
      <template #footer>
        <el-button @click="showCheckInDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCheckIn" :loading="checkInLoading">确定入住</el-button>
      </template>
    </el-dialog>

    <!-- 续住弹窗 -->
    <el-dialog v-model="showExtendDialog" title="团队续住" width="500px">
      <el-form :model="extendForm" label-width="100px">
        <el-form-item label="当前离店">
          <el-input :value="reservation.checkOutDate" disabled />
        </el-form-item>
        <el-form-item label="新离店日期">
          <el-date-picker
            v-model="extendForm.newCheckOutDate"
            type="date"
            placeholder="选择新离店日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledExtendDate"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showExtendDialog = false">取消</el-button>
        <el-button type="primary" @click="handleExtend" :loading="extendLoading">确定续住</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Money } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

// ========== 模式判断 ==========
const isCreateMode = computed(() => route.path === '/reservation/team/create')
const isEdit = ref(false)

// ========== 创建表单 ==========
const createFormRef = ref(null)
const createLoading = ref(false)
const createForm = reactive({
  hotelId: 1,
  teamName: '',
  contactName: '',
  contactPhone: '',
  contactIdNo: '',
  checkInDate: '',
  checkOutDate: '',
  settlementType: 'UNIFIED',
  specialRequests: '',
  pricePlanId: null,
  rooms: []
})

const createRules = {
  teamName: [{ required: true, message: '请输入团队名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择离店日期', trigger: 'change' }],
  settlementType: [{ required: true, message: '请选择结算方式', trigger: 'change' }]
}

const roomTypes = ref([])
const pricePlanOptions = ref([])

// ========== 详情数据 ==========
const reservation = ref({
  rooms: []
})

// ========== 弹窗控制 ==========
const showAssignDialog = ref(false)
const showCheckInDialog = ref(false)
const showExtendDialog = ref(false)
const assignLoading = ref(false)
const checkInLoading = ref(false)
const extendLoading = ref(false)
// 团队账务相关
const teamFolio = ref({})
const teamPaymentDialogVisible = ref(false)
const teamPaymentLoading = ref(false)
const teamPaymentFormRef = ref(null)
const teamPaymentForm = reactive({ amount: 0, paymentMethod: 'CASH', remark: '' })
const teamPaymentRules = {
  amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}
const extendForm = reactive({
  newCheckOutDate: ''
})

// ========== 计算属性 ==========
const unassignedRooms = computed(() => {
  return reservation.value.rooms?.filter(r => !r.roomId) || []
})

const availableRooms = ref([])

// 根据房型获取可用房间
const getFilteredRooms = (roomTypeId) => {
  if (!roomTypeId) return availableRooms.value
  return availableRooms.value.filter(room => room.roomTypeId === roomTypeId)
}

// ========== 获取可用房间列表 ==========
const fetchAvailableRooms = async () => {
  try {
    const res = await request.get('/v1/rooms', { 
      params: { 
        hotelId: 1, 
        status: 'AVAILABLE',
        size: 1000 
      } 
    })
    availableRooms.value = res.data.records || []
  } catch (error) {
    console.error('获取可用房间失败', error)
  }
}

// ========== 初始化 ==========
onMounted(() => {
  fetchRoomTypes()
  fetchPricePlans()
  if (!isCreateMode.value) {
    fetchData()
  } else {
    // 添加默认房间行
    addRoom()
  }
})

// 监听路由变化
watch(() => route.path, () => {
  if (!isCreateMode.value && route.params.id) {
    fetchData()
  }
})

// ========== 获取房型列表 ==========
const fetchRoomTypes = async () => {
  try {
    const res = await request.get('/v1/room-types', { params: { hotelId: 1, size: 100 } })
    roomTypes.value = res.data.records
  } catch (error) {
    console.error('获取房型列表失败', error)
  }
}

// ========== 获取房价码列表 ==========
const fetchPricePlans = async () => {
  try {
    const res = await request.get('/v1/price-plans/list', { params: { hotelId: 1 } })
    if (res.code === 200) {
      pricePlanOptions.value = res.data || []
    }
  } catch (error) {
    console.error('获取房价码列表失败', error)
  }
}


// ========== 获取房价码名称 ==========
const getPricePlanName = (planId) => {
  if (!planId) return '默认价格'
  const plan = pricePlanOptions.value.find(p => p.id === planId)
  return plan ? plan.code + ' - ' + plan.name : '未知'
}

// ========== 获取详情 ==========
const fetchData = async () => {
  if (!route.params.id) return
  try {
    const res = await request.get(`/v1/team-reservations/${route.params.id}`)
    reservation.value = res.data
    fetchTeamFolio()
  } catch (error) {
    console.error('获取团队预订详情失败', error)
    ElMessage.error('获取详情失败')
  }
}

// ========== 添加房间行 ==========
const addRoom = () => {
  createForm.rooms.push({
    roomTypeId: null,
    guestName: '',
    guestPhone: '',
    guestIdNo: '',
    guestGender: '',
    amount: null
  })
}

// ========== 删除房间行 ==========
const removeRoom = (index) => {
  createForm.rooms.splice(index, 1)
}

// ========== 创建预订 ==========
const handleCreate = async () => {
  if (!createFormRef.value) return
  
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (createForm.rooms.length === 0) {
      ElMessage.warning('请至少添加一个房间')
      return
    }
    
    // 验证房间明细
    for (let i = 0; i < createForm.rooms.length; i++) {
      const room = createForm.rooms[i]
      if (!room.roomTypeId) {
        ElMessage.warning(`请选择第${i + 1}个房间的房型`)
        return
      }
      if (!room.guestName) {
        ElMessage.warning(`请输入第${i + 1}个房间的客人姓名`)
        return
      }
      if (!room.guestPhone) {
        ElMessage.warning(`请输入第${i + 1}个房间的客人电话`)
        return
      }
    }
    
    // 处理房间金额：如果金额为0或undefined，设置为null让后端自动计算
    for (let i = 0; i < createForm.rooms.length; i++) {
      if (createForm.rooms[i].amount === 0 || createForm.rooms[i].amount === undefined) {
        createForm.rooms[i].amount = null
      }
    }
    
    createLoading.value = true
    try {
      await request.post('/v1/team-reservations', createForm)
      ElMessage.success('创建成功')
      router.push('/reservation/team')
    } catch (error) {
      console.error('创建失败', error)
    } finally {
      createLoading.value = false
    }
  })
}

// ========== 返回 ==========

// 获取团队账务信息
const fetchTeamFolio = async () => {
  if (!reservation.value || reservation.value.settlementType !== 'UNIFIED') return
  try {
    const res = await request.get('/v1/team-folios', {
      params: {
        teamReservationId: reservation.value.id,
        status: 'OPEN'
      }
    })
    if (res.data?.records?.length > 0) {
      teamFolio.value = res.data.records[0]
    }
  } catch (error) {
    console.error('获取团队账务信息失败', error)
  }
}

// 显示团队收款弹窗
const showTeamPaymentDialog = () => {
  teamPaymentForm.amount = teamFolio.value.balance || 0
  teamPaymentForm.paymentMethod = 'CASH'
  teamPaymentForm.remark = ''
  teamPaymentDialogVisible.value = true
}

// 提交团队收款
const submitTeamPayment = async () => {
  const valid = await teamPaymentFormRef.value.validate().catch(() => false)
  if (!valid) return
  teamPaymentLoading.value = true
  try {
    await request.post(`/v1/team-folios/${teamFolio.value.id}/payments`, {
      amount: teamPaymentForm.amount,
      paymentMethod: teamPaymentForm.paymentMethod,
      remark: teamPaymentForm.remark
    })
    ElMessage.success('收款成功')
    teamPaymentDialogVisible.value = false
    fetchData()
    fetchTeamFolio()
  } catch (error) {
    console.error('收款失败', error)
  } finally {
    teamPaymentLoading.value = false
  }
}

// 团队支付状态
const getTeamPaymentStatusType = () => {
  if (!teamFolio.value) return 'info'
  const balance = teamFolio.value.balance || 0
  if (balance <= 0) return 'success'
  const paid = teamFolio.value.paidAmount || 0
  if (paid > 0) return 'warning'
  return 'danger'
}

const getTeamPaymentStatusText = () => {
  if (!teamFolio.value) return '-'
  const balance = teamFolio.value.balance || 0
  if (balance <= 0) return '已付清'
  const paid = teamFolio.value.paidAmount || 0
  if (paid > 0) return '部分支付'
  return '未支付'
}
const goBack = () => {
  router.push('/reservation/team')
}

// ========== 确认预订 ==========
const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确定要确认该团队预订吗？', '提示', { type: 'warning' })
    await request.put(`/v1/team-reservations/${reservation.value.id}/confirm`)
    ElMessage.success('确认成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认失败', error)
    }
  }
}

// ========== 取消预订 ==========
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该团队预订吗？取消后不可恢复。', '警告', { type: 'error' })
    await request.delete(`/v1/team-reservations/${reservation.value.id}`)
    ElMessage.success('取消成功')
    router.push('/reservation/team')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败', error)
    }
  }
}

// ========== 打开分配房间弹窗 ==========
const openAssignDialog = async () => {
  await fetchAvailableRooms()
  showAssignDialog.value = true
}

// ========== 分配房间 ==========
const handleAssignRooms = async () => {
  const assignments = unassignedRooms.value
    .filter(r => r._assignedRoomId)
    .map(r => ({
      roomDetailId: r.id,
      roomId: r._assignedRoomId
    }))
  
  if (assignments.length === 0) {
    ElMessage.warning('请至少分配一个房间')
    return
  }
  
  assignLoading.value = true
  try {
    await request.post(`/v1/team-reservations/${reservation.value.id}/assign-rooms`, assignments)
    ElMessage.success('分配成功')
    showAssignDialog.value = false
    fetchData()
  } catch (error) {
    console.error('分配房间失败', error)
  } finally {
    assignLoading.value = false
  }
}

// ========== 办理入住 ==========
const handleCheckIn = async () => {
  checkInLoading.value = true
  try {
    await request.post(`/v1/team-reservations/${reservation.value.id}/check-in`, {})
    ElMessage.success('入住成功')
    showCheckInDialog.value = false
    fetchData()
  } catch (error) {
    console.error('入住失败', error)
  } finally {
    checkInLoading.value = false
  }
}

// ========== 办理退房 ==========
const handleCheckOut = async () => {
  try {
    await ElMessageBox.confirm('确定要办理团队退房吗？', '提示', { type: 'warning' })
    await request.post(`/v1/team-reservations/${reservation.value.id}/check-out`, {})
    ElMessage.success('退房成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退房失败', error)
    }
  }
}

// ========== 状态相关 ==========
// ========== 拆分房间 ==========
const canSplitRoom = (row) => {
  const status = reservation.value?.status
  const teamValid = ['PENDING', 'CONFIRMED', 'CHECKED_IN'].includes(status)
  const roomValid = ['PENDING', 'CHECKED_IN'].includes(row.status)
  const hasEnoughRooms = (reservation.value?.rooms?.length || 0) >= 2
  return teamValid && roomValid && hasEnoughRooms
}

const handleSplitRoom = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要将 ${row.guestName} 的 ${row.roomTypeName || '房间'} 从团队中拆分吗？拆分后该房间将转为散客，此操作不可撤销。`,
      '确认拆分',
      { type: 'warning', confirmButtonText: '确认拆分', cancelButtonText: '取消' }
    )
    await request.post(`/v1/team-reservations/${reservation.value.id}/rooms/${row.id}/split`)
    ElMessage.success('拆分成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拆分失败', error)
    }
  }
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'CHECKED_IN': 'primary',
    'CHECKED_OUT': 'info',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CHECKED_IN': '已入住',
    'CHECKED_OUT': '已退房',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const getRoomStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'CHECKED_IN': 'success',
    'CHECKED_OUT': 'info'
  }
  return map[status] || 'info'
}

const getRoomStatusLabel = (status) => {
  const map = {
    'PENDING': '待入住',
    'CHECKED_IN': '已入住',
    'CHECKED_OUT': '已退房'
  }
  return map[status] || status
}
</script>

<style scoped lang="scss">
.team-reservation-detail {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.form-card, .info-card, .room-card {
  margin-bottom: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .amount {
    color: #f56c6c;
    font-weight: 600;
  }
}
</style>