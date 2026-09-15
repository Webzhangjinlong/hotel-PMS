<template>
  <div class="deposit-management">
    <div class="page-header">
      <h2>押金管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCollectDialog">
          <el-icon><Plus /></el-icon>
          收取押金
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="已收取" value="COLLECTED" />
            <el-option label="已退还" value="REFUNDED" />
            <el-option label="部分退还" value="PARTIAL_REFUND" />
            <el-option label="部分抵扣" value="PARTIAL_DEDUCT" />
          </el-select>
        </el-form-item>
        <el-form-item label="客人姓名">
          <el-input v-model="queryParams.guestName" placeholder="请输入客人姓名" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="queryParams.roomNo" placeholder="请输入房间号" clearable style="width: 100px" />
        </el-form-item>
        <el-form-item label="押金日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 押金列表 -->
    <el-table :data="depositList" v-loading="loading" stripe>
      <el-table-column prop="depositNo" label="押金单号" width="140" />
      <el-table-column prop="guestName" label="客人姓名" width="120" />
      <el-table-column prop="roomNo" label="房间号" width="80" />
      <el-table-column label="押金金额" width="120" align="right">
        <template #default="{ row }">
          <span class="amount-value">¥{{ row.amount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="paymentMethodName" label="支付方式" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="已退金额" width="120" align="right">
        <template #default="{ row }">
          <span v-if="row.refundedAmount > 0" class="refund-value">¥{{ row.refundedAmount }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="collectedAt" label="收取时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.collectedAt) }}</template>
      </el-table-column>
      <el-table-column prop="collectedByName" label="收取人" width="100" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'COLLECTED' || row.status === 'PARTIAL_REFUND' || row.status === 'PARTIAL_DEDUCT'" type="warning" link size="small" @click="showRefundDialog(row)">退还</el-button>
            <el-button v-if="row.status === 'COLLECTED' || row.status === 'PARTIAL_REFUND' || row.status === 'PARTIAL_DEDUCT'" type="success" link size="small" @click="showDeductDialog(row)">抵扣房费</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </div>

    <!-- 收取押金对话框 -->
    <el-dialog v-model="collectDialogVisible" title="收取押金" width="500px" :close-on-click-modal="false">
      <el-form :model="collectForm" :rules="collectRules" ref="collectFormRef" label-width="100px">
        <el-form-item label="客人" prop="guestId">
          <el-select 
            v-model="collectForm.guestId" 
            filterable 
            remote
            :remote-method="remoteGuestSearch"
            :loading="guestSearchLoading"
            placeholder="请输入客人姓名搜索" 
            style="width: 100%" 
            @change="handleGuestSelect"
          >
            <el-option v-for="guest in guestList" :key="guest.id" :label="guest.name + ' - ' + guest.phone" :value="guest.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="客人姓名" prop="guestName">
          <el-input v-model="collectForm.guestName" placeholder="客人姓名" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="collectForm.phone" placeholder="手机号" disabled />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="collectForm.roomNo" placeholder="请输入房间号（可选）" />
        </el-form-item>
        <el-form-item label="押金金额" prop="amount">
          <el-input-number v-model="collectForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="collectForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="刷卡" value="POS" />
            <el-option label="银行转账" value="BANK_TRANSFER" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="collectForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="collectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCollect" :loading="submitLoading">确认收取</el-button>
      </template>
    </el-dialog>

    <!-- 退还押金对话框 -->
    <el-dialog v-model="refundDialogVisible" title="退还押金" width="500px" :close-on-click-modal="false">
      <el-form :model="refundForm" :rules="refundRules" ref="refundFormRef" label-width="100px">
        <el-form-item label="押金单号">
          <el-input :value="currentDeposit.depositNo" disabled />
        </el-form-item>
        <el-form-item label="客人姓名">
          <el-input :value="currentDeposit.guestName" disabled />
        </el-form-item>
        <el-form-item label="押金金额">
          <el-input :value="'¥' + (currentDeposit.amount || 0)" disabled />
        </el-form-item>
        <el-form-item label="已退金额">
          <el-input :value="'¥' + (currentDeposit.refundedAmount || 0)" disabled />
        </el-form-item>
        <el-form-item label="可退金额">
          <el-input :value="'¥' + refundableAmount" disabled class="refundable-amount" />
        </el-form-item>
        <el-form-item label="退还金额" prop="amount">
          <el-input-number v-model="refundForm.amount" :min="0.01" :max="refundableAmount" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退还方式" prop="refundMethod">
          <el-select v-model="refundForm.refundMethod" placeholder="请选择退还方式" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="原路退回" value="ORIGINAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="refundForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRefund" :loading="submitLoading">确认退还</el-button>
      </template>
    </el-dialog>

      <!-- 抵扣房费对话框 -->
      <el-dialog v-model="deductDialogVisible" title="抵扣房费" width="500px" :close-on-click-modal="false">
        <el-form :model="deductForm" :rules="deductRules" ref="deductFormRef" label-width="100px">
          <el-form-item label="押金单号">
            <el-input :value="currentDeposit.depositNo" disabled />
          </el-form-item>
          <el-form-item label="客人姓名">
            <el-input :value="currentDeposit.guestName" disabled />
          </el-form-item>
          <el-form-item label="可抵扣金额">
            <el-input :value="refundableAmount.toFixed(2)" disabled />
          </el-form-item>
          <el-form-item label="抵扣金额" prop="amount">
            <el-input-number v-model="deductForm.amount" :min="0.01" :max="refundableAmount" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="入住单ID" prop="stayNo">
            <el-input v-model="deductForm.stayNo" placeholder="请输入入住单ID（数字）" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="deductForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="deductDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleDeduct" :loading="submitLoading">确认抵扣</el-button>
        </template>
      </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="押金详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="押金单号">{{ currentDeposit.depositNo }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ currentDeposit.guestName }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ currentDeposit.roomNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="押金金额">¥{{ currentDeposit.amount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentDeposit.paymentMethodName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentDeposit.status)">{{ currentDeposit.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已退金额">¥{{ currentDeposit.refundedAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="可退金额">¥{{ refundableAmount }}</el-descriptions-item>
        <el-descriptions-item label="收取时间">{{ formatDateTime(currentDeposit.collectedAt) }}</el-descriptions-item>
        <el-descriptions-item label="收取人">{{ currentDeposit.collectedByName }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentDeposit.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { collectDeposit, refundDeposit, getDepositList, getDepositById, deductDepositForRoomFee } from '@/api/deposit'
import { getGuestList } from '@/api/guest'
import { getStayList } from '@/api/stay'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  hotelId: 1, // 默认酒店ID
  status: '',
  guestName: '',
  roomNo: '',
  startDate: '',
  endDate: ''
})

const dateRange = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const depositList = ref([])
const total = ref(0)
const guestList = ref([])

// 收取押金相关
const collectDialogVisible = ref(false)
const collectFormRef = ref(null)
const collectForm = reactive({
  hotelId: 1, // 默认酒店ID
  stayId: null, // 入住单ID
  guestId: null,
  phone: '',
  guestName: '',
  roomNo: '',
  amount: 0,
  paymentMethod: 'CASH',
  remark: '',
  operatorId: 1 // 默认操作员ID
})

const collectRules = {
  guestId: [{ required: true, message: '请选择客人', trigger: 'change' }],
  guestName: [{ required: true, message: '客人姓名不能为空', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入押金金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

// 退还押金相关
const refundDialogVisible = ref(false)
const refundFormRef = ref(null)
const deductDialogVisible = ref(false)
const deductFormRef = ref(null)
const currentDeposit = ref({})
const deductForm = reactive({
  amount: 0,
  stayNo: null,
  remark: ''
  })
  const deductRules = {
    amount: [{ required: true, message: '请输入抵扣金额', trigger: 'blur' }],
    stayNo: [{ required: true, message: '请选择入住单', trigger: 'change' }]
  }
const refundForm = reactive({
  amount: 0,
  refundMethod: 'CASH',
  remark: '',
  operatorId: 1
})

const refundRules = {
  amount: [{ required: true, message: '请输入退还金额', trigger: 'blur' }],
  refundMethod: [{ required: true, message: '请选择退还方式', trigger: 'change' }]
}

// 详情对话框
const detailDialogVisible = ref(false)

// 计算可退金额
const refundableAmount = computed(() => {
  if (!currentDeposit.value.amount) return 0
  return currentDeposit.value.amount - (currentDeposit.value.refundedAmount || 0) - (currentDeposit.value.deductedAmount || 0)

})
// 获取押金列表
const fetchDepositList = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getDepositList(params)
    depositList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取押金列表失败:', error)
    ElMessage.error('获取押金列表失败')
  } finally {
    loading.value = false
  }
}

// 获取客人列表
const fetchGuestList = async (keyword = '') => {
  try {
    const res = await getGuestList({ 
      hotelId: queryParams.hotelId,
      name: keyword,  // 搜索关键词
      page: 1,
      size: 100  // 增加每页显示数量
    })
    // 后端返回的是分页数据，需要从 records 中获取数组
    guestList.value = res.data?.records || []
  } catch (error) {
    console.error('获取客人列表失败:', error)
  }
}

// 远程搜索客人
const guestSearchLoading = ref(false)
const remoteGuestSearch = async (keyword) => {
  if (keyword === '') {
    guestList.value = []
    return
  }
  guestSearchLoading.value = true
  try {
    await fetchGuestList(keyword)
  } catch (error) {
    console.error('搜索客人失败:', error)
  } finally {
    guestSearchLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchDepositList()
}

// 重置
const handleReset = () => {
  queryParams.status = ''
  queryParams.guestName = ''
  queryParams.roomNo = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  dateRange.value = []
  handleSearch()
}

// 显示收取押金对话框
const showCollectDialog = () => {
  Object.assign(collectForm, {
    hotelId: 1, // 默认酒店ID
    stayId: null, // 入住单ID
    guestId: null,
    guestName: '',
    phone: '',
    roomNo: '',
    amount: 0,
    paymentMethod: 'CASH',
    remark: '',
    operatorId: 1 // 默认操作员ID
  })
  collectDialogVisible.value = true
}

// 选择客人
const handleGuestSelect = async (guestId) => {
  const guest = guestList.value.find(g => g.id === guestId)
  if (guest) {
    collectForm.guestName = guest.name
    collectForm.phone = guest.phone || ''
    
    // 查询该客人的入住单信息
    try {
      const res = await getStayList({
        hotelId: queryParams.hotelId,
        guestId: guestId,
        status: 'CHECKED_IN', // 只查询在住状态的入住单
        page: 1,
        size: 1
      })
      const stays = res.data?.records || []
      if (stays.length > 0) {
        const stay = stays[0]
        collectForm.stayId = stay.id // 设置入住单ID
        collectForm.roomNo = stay.roomNo || ''
        // 押金金额默认为房费（totalAmount）
        if (stay.totalAmount && stay.totalAmount > 0) {
          collectForm.amount = stay.totalAmount
        }
      }
    } catch (error) {
      console.error('查询入住单失败:', error)
    }
  }
}

// 收取押金
const handleCollect = async () => {
  try {
    await collectFormRef.value.validate()
    submitLoading.value = true
    await collectDeposit(collectForm)
    ElMessage.success('押金收取成功')
    collectDialogVisible.value = false
    fetchDepositList()
  } catch (error) {
    if (error !== false) {
      console.error('收取押金失败:', error)
      ElMessage.error('收取押金失败: ' + (error.message || '未知错误'))
    }
  } finally {
    submitLoading.value = false
  }
}

// 显示退还对话框
const showRefundDialog = async (row) => {
  try {
    const res = await getDepositById(row.id)
    currentDeposit.value = res.data
    refundForm.amount = refundableAmount.value
    refundForm.refundMethod = 'CASH'
    refundForm.remark = ''
    refundDialogVisible.value = true
  } catch (error) {
    console.error('获取押金详情失败:', error)
    ElMessage.error('获取押金详情失败')
  }
}

// 退还押金
const handleRefund = async () => {
  try {
    await refundFormRef.value.validate()
    submitLoading.value = true
    await refundDeposit(currentDeposit.value.id, refundForm)
    ElMessage.success('押金退还成功')
    refundDialogVisible.value = false
    fetchDepositList()
  } catch (error) {
    if (error !== false) {
      console.error('退还押金失败:', error)
      ElMessage.error('退还押金失败: ' + (error.message || '未知错误'))
    }
  } finally {
    submitLoading.value = false
  }
}

  const showDeductDialog = async (row) => {
  currentDeposit.value = row
  deductForm.amount = 0
  deductForm.stayNo = ''
  deductForm.remark = ''
  deductDialogVisible.value = true
  }

  const handleDeduct = async () => {
  try {
    await deductFormRef.value.validate()
    submitLoading.value = true
    await deductDepositForRoomFee(currentDeposit.value.id, deductForm)
    ElMessage.success('抵扣成功')
    deductDialogVisible.value = false
    fetchDepositList()
  } catch (error) {
    console.error('抵扣失败', error)
    ElMessage.error('抵扣失败')
  } finally {
    submitLoading.value = false
  }
  }
// 查看详情
const viewDetail = async (row) => {
  try {
    const res = await getDepositById(row.id)
    currentDeposit.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取押金详情失败:', error)
    ElMessage.error('获取押金详情失败')
  }
}

// 状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'COLLECTED': return 'success'
    case 'REFUNDED': return 'info'
    case 'PARTIAL_REFUND': return 'warning'
    case 'PARTIAL_DEDUCT': return 'warning'
    default: return ''
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 监听日期范围变化
const handleDateRangeChange = (val) => {
  if (val && val.length === 2) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
}

onMounted(() => {
  fetchDepositList()
  fetchGuestList()
})
</script>

<style scoped lang="scss">
.deposit-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    color: #303133;
  }
}

.filter-card {
  margin-bottom: 20px;
}

.amount-value {
  color: #67c23a;
  font-weight: bold;
}

.refund-value {
  color: #e6a23c;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
