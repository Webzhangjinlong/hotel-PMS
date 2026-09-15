<template>
  <div class="folio-management">
    <div class="page-header">
      <h2>账务管理</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card stats-total">
          <div class="stats-icon"><el-icon><Money /></el-icon></div>
          <div class="stats-info">
            <div class="stats-label">总金额</div>
            <div class="stats-value">¥{{ (statsData.totalAmount || 0).toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card stats-paid">
          <div class="stats-icon"><el-icon><CircleCheckFilled /></el-icon></div>
          <div class="stats-info">
            <div class="stats-label">已收金额</div>
            <div class="stats-value">¥{{ (statsData.paidAmount || 0).toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card stats-balance">
          <div class="stats-icon"><el-icon><Clock /></el-icon></div>
          <div class="stats-info">
            <div class="stats-label">待收金额</div>
            <div class="stats-value">¥{{ (statsData.balance || 0).toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card stats-count">
          <div class="stats-icon"><el-icon><Document /></el-icon></div>
          <div class="stats-info">
            <div class="stats-label">账务笔数</div>
            <div class="stats-value">{{ statsData.count || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 散客/团队 切换标题 -->
    <div class="type-tabs">
      <div
        class="type-tab"
        :class="{ active: activeType === 'STAY' }"
        @click="switchType('STAY')"
      >散客</div>
      <div
        class="type-tab"
        :class="{ active: activeType === 'TEAM' }"
        @click="switchType('TEAM')"
      >团队</div>
    </div>

    <!-- 散客筛选条件 -->
    <el-card v-if="activeType === 'STAY'" class="filter-card" shadow="never">
      <el-form :model="stayQuery" label-width="auto">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="客人姓名">
              <el-input v-model="stayQuery.guestName" placeholder="请输入姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="付款状态">
              <el-select v-model="stayQuery.payStatus" placeholder="全部" clearable style="width: 100%">
                <el-option label="全部" value="" />
                <el-option label="已付清" value="PAID" />
                <el-option label="未付清" value="UNPAID" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="付款日期">
              <el-date-picker v-model="stayQuery.payDateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 团队筛选条件 -->
    <el-card v-if="activeType === 'TEAM'" class="filter-card" shadow="never">
      <el-form :model="teamQuery" label-width="auto">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="团队名称">
              <el-input v-model="teamQuery.teamName" placeholder="请输入团队名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="结算方式">
              <el-select v-model="teamQuery.settlementType" placeholder="全部" clearable style="width: 100%">
                <el-option label="全部" value="" />
                <el-option label="统一结算" value="UNIFIED" />
                <el-option label="分开结算" value="SEPARATE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="付款状态">
              <el-select v-model="teamQuery.payStatus" placeholder="全部" clearable style="width: 100%">
                <el-option label="全部" value="" />
                <el-option label="已付清" value="PAID" />
                <el-option label="未付清" value="UNPAID" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="付款日期">
              <el-date-picker v-model="teamQuery.payDateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item style="text-align: right;">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table :data="folioList" v-loading="loading" stripe>
        <el-table-column prop="folioNo" label="账务单号" width="150" />
        <el-table-column v-if="activeType === 'STAY'" label="入住单号" width="150">
          <template #default="{ row }">{{ row.stayNo || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="activeType === 'STAY'" label="客人姓名" width="120">
          <template #default="{ row }">{{ row.guestName || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="activeType === 'STAY'" label="联系电话" width="130">
          <template #default="{ row }">{{ row.guestPhone || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="activeType === 'TEAM'" label="团队预定号" width="150">
          <template #default="{ row }">{{ row.teamReservationNo || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="activeType === 'TEAM'" label="团队名称" width="150">
          <template #default="{ row }">{{ row.teamName || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="activeType === 'TEAM'" label="结算方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.settlementType === 'UNIFIED' ? 'primary' : 'warning'" size="small">
              {{ row.settlementType === 'UNIFIED' ? '统一结算' : '分开结算' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">¥{{ (row.totalAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="已收金额" width="120">
          <template #default="{ row }">¥{{ (row.paidAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.balance > 0 ? '#f56c6c' : '#67c23a' }">¥{{ (row.balance || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="付款状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.balance <= 0 ? 'success' : 'danger'" size="small">
              {{ row.balance <= 0 ? '已付清' : '未付清' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'OPEN' && row.balance > 0" type="success" link size="small" @click="showPaymentDialog(row)">收款</el-button>
            <el-button v-if="row.status === 'OPEN' && row.balance <= 0" type="warning" link size="small" @click="handleClose(row)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 收款弹窗 -->
    <el-dialog v-model="showPayment" title="收款" width="500px">
      <el-descriptions :column="2" border class="payment-info">
        <el-descriptions-item label="账务单号">{{ currentFolio.folioNo }}</el-descriptions-item>
        <el-descriptions-item :label="activeType === 'TEAM' ? '团队名称' : '客人姓名'">
          {{ activeType === 'TEAM' ? currentFolio.teamName : currentFolio.guestName }}
        </el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ (currentFolio.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="已收金额">¥{{ (currentFolio.paidAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="待收金额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ (currentFolio.balance || 0).toFixed(2) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-form :model="paymentForm" :rules="paymentRules" ref="paymentFormRef" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="收款金额" prop="amount">
          <el-input-number v-model="paymentForm.amount" :min="0.01" :max="currentFolio.balance" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="paymentForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银行卡" value="BANK_CARD" />
            <el-option label="挂账" value="CREDIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="paymentForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPayment = false">取消</el-button>
        <el-button type="primary" @click="handlePayment" :loading="paymentLoading">确认收款</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" title="账务详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="账务单号">{{ detailData.folioNo }}</el-descriptions-item>
        <el-descriptions-item label="账务类型">
          <el-tag :type="detailData.folioType === 'TEAM' ? 'primary' : 'warning'" size="small">
            {{ detailData.folioType === 'TEAM' ? '团队' : '散客' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.folioType === 'TEAM'" label="团队预定号">{{ detailData.teamReservationNo }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.folioType === 'TEAM'" label="团队名称">{{ detailData.teamName }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.folioType === 'STAY'" label="入住单号">{{ detailData.stayNo }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.folioType === 'STAY'" label="客人姓名">{{ detailData.guestName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 'OPEN' ? 'success' : 'info'">
            {{ detailData.status === 'OPEN' ? '开放' : '已关闭' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ (detailData.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="已收金额">¥{{ (detailData.paidAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="余额">
          <span :style="{ color: detailData.balance > 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
            ¥{{ (detailData.balance || 0).toFixed(2) }}
          </span>
        </el-descriptions-item>
      </el-descriptions>
      <h4 style="margin: 20px 0 10px;">收款记录</h4>
      <el-table :data="paymentList" stripe size="small">
        <el-table-column :prop="detailData.folioType === 'TEAM' ? 'paymentNo' : 'transactionNo'" :label="detailData.folioType === 'TEAM' ? '收款单号' : '交易号'" width="150" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ (row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">{{ getPaymentMethodLabel(row.paymentMethod) }}</template>
        </el-table-column>
        <el-table-column :prop="detailData.folioType === 'TEAM' ? 'paymentTime' : 'transactionTime'" :label="detailData.folioType === 'TEAM' ? '收款时间' : '交易时间'" width="170">
          <template #default="{ row }">{{ formatDateTime(detailData.folioType === 'TEAM' ? row.paymentTime : row.transactionTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="100" v-if="detailData.folioType !== 'TEAM'">
          <template #default="{ row }">
            <el-button 
              v-if="row.type !== 'REVERSAL' && row.type !== 'REFUND' && !row.isReversed" 
              type="danger" 
              link 
              size="small" 
              @click="showReverseDialog(row.id)"
            >冲账</el-button>
            <el-tag v-if="row.isReversed" type="info" size="small">已冲账</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 冲账弹窗 -->
    <el-dialog v-model="showReverse" title="冲账" width="400px" :close-on-click-modal="false">
      <el-form :model="reverseForm" :rules="reverseRules" ref="reverseFormRef" label-width="80px">
        <el-form-item label="冲账原因" prop="reason">
          <el-input v-model="reverseForm.reason" type="textarea" :rows="3" placeholder="请输入冲账原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReverse = false">取消</el-button>
        <el-button type="danger" :loading="reverseLoading" @click="handleReverse">确认冲账</el-button>
      </template>
    </el-dialog>
  </div>
</template>


<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Money, CircleCheckFilled, Clock, Document } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { reverseTransaction } from '@/api/folio'

const loading = ref(false)
const folioList = ref([])
const total = ref(0)
const activeType = ref('STAY')

// 通用分页参数
const route = useRoute()
const queryParams = reactive({
  page: 1,
  size: 10
})

// 散客查询参数
const stayQuery = reactive({
  guestName: '',
  payStatus: '',
  payDateRange: null
})

// 团队查询参数
const teamQuery = reactive({
  teamName: '',
  settlementType: '',
  payStatus: '',
  payDateRange: null
})

// 统计数据
const statsData = ref({
  totalAmount: 0,
  paidAmount: 0,
  balance: 0,
  count: 0
})

// 收款相关
const showPayment = ref(false)
const paymentLoading = ref(false)
const paymentFormRef = ref(null)
const currentFolio = ref({})
const paymentForm = reactive({ amount: 0, paymentMethod: '', remark: '' })
const paymentRules = {
  amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

// 冲账相关
const showReverse = ref(false)
const reverseLoading = ref(false)
const reverseFormRef = ref(null)
const reverseTransactionId = ref(null)
const reverseForm = reactive({ reason: '' })
const reverseRules = {
  reason: [{ required: true, message: '请输入冲账原因', trigger: 'blur' }]
}

// 详情相关
const showDetail = ref(false)
const detailData = ref({})
const paymentList = ref([])

const switchType = (type) => {
  activeType.value = type
  queryParams.page = 1
  fetchList()
  fetchStats()
}

const fetchList = async () => {
  loading.value = true
  try {
    let url = ''
    let params = { ...queryParams }
    
    if (activeType.value === 'TEAM') {
      url = '/v1/team-folios'
      if (teamQuery.teamName) params.teamName = teamQuery.teamName
      if (teamQuery.settlementType) params.settlementType = teamQuery.settlementType
      if (teamQuery.payStatus) params.payStatus = teamQuery.payStatus
      if (teamQuery.payDateRange && teamQuery.payDateRange.length === 2) {
        params.payDateStart = teamQuery.payDateRange[0]
        params.payDateEnd = teamQuery.payDateRange[1]
      }
    } else {
      url = '/v1/folios/list'
      if (stayQuery.guestName) params.guestName = stayQuery.guestName
      if (stayQuery.payStatus) params.payStatus = stayQuery.payStatus
      if (stayQuery.payDateRange && stayQuery.payDateRange.length === 2) {
        params.payDateStart = stayQuery.payDateRange[0]
        params.payDateEnd = stayQuery.payDateRange[1]
      }
    }
    
    const res = await request.get(url, { params })
    folioList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('查询失败', error)
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    let url = activeType.value === 'TEAM' ? '/v1/team-folios/stats' : '/v1/folios/stats'
    let params = {}
    
    if (activeType.value === 'TEAM') {
      if (teamQuery.teamName) params.teamName = teamQuery.teamName
      if (teamQuery.settlementType) params.settlementType = teamQuery.settlementType
      if (teamQuery.payStatus) params.payStatus = teamQuery.payStatus
    } else {
      if (stayQuery.guestName) params.guestName = stayQuery.guestName
      if (stayQuery.payStatus) params.payStatus = stayQuery.payStatus
    }
    
    const res = await request.get(url, { params })
    statsData.value = res.data
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchList()
  fetchStats()
}

const handleReset = () => {
  if (activeType.value === 'STAY') {
    stayQuery.guestName = ''
    stayQuery.payStatus = ''
    stayQuery.payDateRange = null
  } else {
    teamQuery.teamName = ''
    teamQuery.settlementType = ''
    teamQuery.payStatus = ''
    teamQuery.payDateRange = null
  }
  queryParams.page = 1
  fetchList()
  fetchStats()
}

const showPaymentDialog = (row) => {
  currentFolio.value = row
  paymentForm.amount = row.balance
  paymentForm.paymentMethod = ''
  paymentForm.remark = ''
  showPayment.value = true
}

const handlePayment = async () => {
  const valid = await paymentFormRef.value.validate().catch(() => false)
  if (!valid) return
  paymentLoading.value = true
  try {
    if (currentFolio.value.folioType === 'TEAM') {
      await request.post(`/v1/team-folios/${currentFolio.value.id}/payments`, paymentForm)
    } else {
      await request.post(`/v1/folios/stay/${currentFolio.value.stayId}/payment`, paymentForm)
    }
    ElMessage.success('收款成功')
    showPayment.value = false
    fetchList()
    fetchStats()
  } catch (error) {
    console.error('收款失败', error)
  } finally {
    paymentLoading.value = false
  }
}

const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm('确定要关闭此账务单吗？', '提示', { type: 'warning' })
    await request.put(`/v1/team-folios/${row.id}/close`)
    ElMessage.success('关闭成功')
    fetchList()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') console.error('关闭失败', error)
  }
}

const showReverseDialog = (transactionId) => {
  reverseTransactionId.value = transactionId
  reverseForm.reason = ''
  showReverse.value = true
}

const handleReverse = async () => {
  const valid = await reverseFormRef.value.validate().catch(() => false)
  if (!valid) return
  reverseLoading.value = true
  try {
    await reverseTransaction(reverseTransactionId.value, reverseForm.reason)
    ElMessage.success('冲账成功')
    showReverse.value = false
    // 刷新详情
    if (detailData.value.id) {
      viewDetail(detailData.value)
    }
    fetchList()
    fetchStats()
  } catch (error) {
    console.error('冲账失败', error)
  } finally {
    reverseLoading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    if (row.folioType === 'TEAM') {
      const res = await request.get(`/v1/team-folios/${row.id}`)
      detailData.value = { ...res.data, folioType: 'TEAM' }
      const paymentsRes = await request.get(`/v1/team-folios/${row.id}/payments`)
      paymentList.value = paymentsRes.data
    } else {
      const res = await request.get(`/v1/folios/stay/${row.stayId}`)
      detailData.value = { ...res.data, folioType: 'STAY' }
      const paymentsRes = await request.get(`/v1/folios/${row.id}/transactions`)
      paymentList.value = paymentsRes.data || []
    }
    showDetail.value = true
  } catch (error) {
    console.error('查询详情失败', error)
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const getPaymentMethodLabel = (method) => {
  const map = { 'CASH': '现金', 'WECHAT': '微信', 'ALIPAY': '支付宝', 'BANK_CARD': '银行卡', 'CREDIT': '挂账', 'POINTS': '积分抵扣' }
  return map[method] || method
}

onMounted(() => {
  // 检查是否从首页跳转过来需要设置今日日期
  if (route.query.date === 'today') {
    const today = new Date()
    const todayStr = today.getFullYear() + '-' + String(today.getMonth()+1).padStart(2,'0') + '-' + String(today.getDate()).padStart(2,'0')
    stayQuery.payDateRange = [todayStr, todayStr]
  }
  fetchList()
  fetchStats()
})
</script>
<style scoped lang="scss">
.folio-management { padding: 20px; }
.page-header { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; font-weight: 600; } }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
.payment-info { margin-bottom: 10px; }

/* 统计卡片 */
.stats-row { margin-bottom: 20px; }
.stats-card {
  display: flex; align-items: center; padding: 20px; cursor: default; transition: all 0.3s;
  &:hover { transform: translateY(-4px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); }
}
.stats-icon {
  width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-right: 16px; font-size: 28px; color: #fff;
}
.stats-total .stats-icon { background: linear-gradient(135deg, #409EFF, #66B1FF); }
.stats-paid .stats-icon { background: linear-gradient(135deg, #67C23A, #85CE61); }
.stats-balance .stats-icon { background: linear-gradient(135deg, #E6A23C, #EBB563); }
.stats-count .stats-icon { background: linear-gradient(135deg, #9B59B6, #BB77D0); }
.stats-info { flex: 1; }
.stats-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stats-value { font-size: 24px; font-weight: 700; color: #303133; }

/* 散客/团队 切换标签 */
.type-tabs {
  display: flex; gap: 0; margin-bottom: 16px;
  .type-tab {
    padding: 10px 32px; font-size: 15px; font-weight: 500; cursor: pointer;
    background: #f5f7fa; color: #606266; border: 1px solid #dcdfe6; transition: all 0.2s;
    &:first-child { border-radius: 6px 0 0 6px; }
    &:last-child { border-radius: 0 6px 6px 0; border-left: none; }
    &.active {
      background: #409eff; color: #fff; border-color: #409eff;
    }
    &:hover:not(.active) { background: #ecf5ff; color: #409eff; }
  }
}

.filter-card { margin-bottom: 16px; }
</style>



