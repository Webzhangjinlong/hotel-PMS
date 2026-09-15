<template>
  <div class="shift-report">
    <div class="page-header">
      <h2>收银员交接表</h2>
    </div>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="营业日期">
          <el-date-picker v-model="queryParams.businessDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="班次">
          <el-select v-model="queryParams.shift" placeholder="请选择班次">
            <el-option label="早班" value="MORNING" />
            <el-option label="中班" value="MIDDLE" />
            <el-option label="晚班" value="NIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-select v-model="queryParams.operatorId" placeholder="请选择操作人" clearable>
            <el-option v-for="user in userList" :key="user.id" :label="user.username" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handlePrint">打印</el-button>
          <el-button type="warning" @click="handleShift">交班</el-button>
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 报表内容 -->
    <el-card class="report-content" v-loading="loading">
      <div class="report-header">
        <h3>营业日 {{ queryParams.businessDate }} {{ getShiftName(queryParams.shift) }}</h3>
        <div class="report-info">
          <span>打印人：{{ currentUser }}</span>
          <span>打印时间：{{ printTime }}</span>
          <span>交班模式：备用金</span>
        </div>
      </div>
      
      <!-- 上缴汇总 -->
      <div class="section">
        <h4>上缴汇总</h4>
        <el-table :data="[reportData.depositSummary]" border size="small">
          <el-table-column prop="cash" label="现金" width="150">
            <template #default>¥{{ reportData.depositSummary?.cash || '0.00' }}</template>
          </el-table-column>
          <el-table-column prop="wechat" label="微信" width="150">
            <template #default>¥{{ reportData.depositSummary?.wechat || '0.00' }}</template>
          </el-table-column>
          <el-table-column prop="alipay" label="支付宝" width="150">
            <template #default>¥{{ reportData.depositSummary?.alipay || '0.00' }}</template>
          </el-table-column>
          <el-table-column prop="preAuth" label="预授权完成" width="150">
            <template #default>{{ reportData.depositSummary?.preAuth || 0 }}</template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 当班明细 -->
      <div class="section">
        <h4>当班明细↓</h4>
        
        <!-- 收入总计 -->
        <div class="sub-section">
          <h5>收入总计：¥{{ reportData.incomeSummary?.total || '0.00' }}</h5>
          <el-table :data="[reportData.incomeSummary]" border size="small">
            <el-table-column label="房费" width="150">
              <template #default>¥{{ reportData.incomeSummary?.roomFee || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="商品" width="150">
              <template #default>¥{{ reportData.incomeSummary?.goods || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="餐费" width="150">
              <template #default>¥{{ reportData.incomeSummary?.meal || '0.00' }}</template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 总收款 -->
        <div class="sub-section">
          <h5>总收款：¥{{ reportData.paymentSummary?.total || '0.00' }}</h5>
          <el-table :data="[reportData.paymentSummary]" border size="small">
            <el-table-column label="现金" width="100">
              <template #default>¥{{ reportData.paymentSummary?.cash || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="银行卡" width="100">
              <template #default>¥{{ reportData.paymentSummary?.bankCard || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="微信" width="100">
              <template #default>¥{{ reportData.paymentSummary?.wechat || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="支付宝" width="100">
              <template #default>¥{{ reportData.paymentSummary?.alipay || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="会员支付" width="100">
              <template #default>¥{{ reportData.paymentSummary?.memberPay || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="AR账" width="100">
              <template #default>¥{{ reportData.paymentSummary?.arAccount || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="代金券" width="100">
              <template #default>¥{{ reportData.paymentSummary?.coupon || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="内部招待" width="100">
              <template #default>¥{{ reportData.paymentSummary?.internal || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="第三方支付" width="100">
              <template #default>¥{{ reportData.paymentSummary?.thirdParty || '0.00' }}</template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 预授权 -->
        <div class="sub-section">
          <h5>预授权</h5>
          <el-table :data="[reportData.preAuth]" border size="small">
            <el-table-column label="当班新增" width="150">
              <template #default>{{ reportData.preAuth?.newCount || 0 }}</template>
            </el-table-column>
            <el-table-column label="当班完成" width="150">
              <template #default>{{ reportData.preAuth?.completedCount || 0 }}</template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 会员冻结 -->
        <div class="sub-section">
          <h5>会员冻结</h5>
          <el-table :data="[reportData.memberFreeze]" border size="small">
            <el-table-column label="当班新增" width="150">
              <template #default>¥{{ reportData.memberFreeze?.newAmount || '0.00' }}</template>
            </el-table-column>
            <el-table-column label="当班完成" width="150">
              <template #default>¥{{ reportData.memberFreeze?.completedAmount || '0.00' }}</template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 会员售卡 -->
        <div class="sub-section">
          <h5>会员售卡：新增会员 {{ reportData.memberCard?.newCount || 0 }} 名，售卡金额 ¥{{ reportData.memberCard?.totalAmount || '0' }}</h5>
          <el-table :data="[reportData.memberCard?.payment]" border size="small">
            <el-table-column label="现金" width="120">
              <template #default>¥{{ reportData.memberCard?.payment?.cash || '0' }}</template>
            </el-table-column>
            <el-table-column label="微信" width="120">
              <template #default>¥{{ reportData.memberCard?.payment?.wechat || '0' }}</template>
            </el-table-column>
            <el-table-column label="支付宝" width="120">
              <template #default>¥{{ reportData.memberCard?.payment?.alipay || '0' }}</template>
            </el-table-column>
            <el-table-column label="银行卡" width="120">
              <template #default>¥{{ reportData.memberCard?.payment?.bankCard || '0' }}</template>
            </el-table-column>
            <el-table-column label="其他" width="120">
              <template #default>¥{{ reportData.memberCard?.payment?.other || '0' }}</template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 会员充值 -->
        <div class="sub-section">
          <h5>会员充值：¥{{ reportData.memberRecharge?.totalAmount || '0' }}</h5>
          <el-table :data="[reportData.memberRecharge?.payment]" border size="small">
            <el-table-column label="现金" width="120">
              <template #default>¥{{ reportData.memberRecharge?.payment?.cash || '0' }}</template>
            </el-table-column>
            <el-table-column label="微信" width="120">
              <template #default>¥{{ reportData.memberRecharge?.payment?.wechat || '0' }}</template>
            </el-table-column>
            <el-table-column label="支付宝" width="120">
              <template #default>¥{{ reportData.memberRecharge?.payment?.alipay || '0' }}</template>
            </el-table-column>
            <el-table-column label="银行卡" width="120">
              <template #default>¥{{ reportData.memberRecharge?.payment?.bankCard || '0' }}</template>
            </el-table-column>
            <el-table-column label="其他" width="120">
              <template #default>¥{{ reportData.memberRecharge?.payment?.other || '0' }}</template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 协议回款 -->
        <div class="sub-section">
          <h5>协议回款：¥{{ reportData.agreementPayment?.totalAmount || '0' }}</h5>
          <el-table :data="[reportData.agreementPayment?.payment]" border size="small">
            <el-table-column label="现金" width="120">
              <template #default>¥{{ reportData.agreementPayment?.payment?.cash || '0' }}</template>
            </el-table-column>
            <el-table-column label="微信" width="120">
              <template #default>¥{{ reportData.agreementPayment?.payment?.wechat || '0' }}</template>
            </el-table-column>
            <el-table-column label="支付宝" width="120">
              <template #default>¥{{ reportData.agreementPayment?.payment?.alipay || '0' }}</template>
            </el-table-column>
            <el-table-column label="银行卡" width="120">
              <template #default>¥{{ reportData.agreementPayment?.payment?.bankCard || '0' }}</template>
            </el-table-column>
            <el-table-column label="其他" width="120">
              <template #default>¥{{ reportData.agreementPayment?.payment?.other || '0' }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getShiftReport } from '@/api/report'

const loading = ref(false)
const currentUser = ref('admin')
const printTime = ref(new Date().toLocaleString())

const queryParams = reactive({
  businessDate: new Date().toISOString().split('T')[0],
  shift: 'MORNING',
  operatorId: null
})

const userList = ref([])

const reportData = reactive({
  depositSummary: {},
  incomeSummary: {},
  paymentSummary: {},
  preAuth: {},
  memberFreeze: {},
  memberCard: { payment: {} },
  memberRecharge: { payment: {} },
  agreementPayment: { payment: {} }
})

const getShiftName = (shift) => {
  const map = { 'MORNING': '早班', 'MIDDLE': '中班', 'NIGHT': '晚班' }
  return map[shift] || ''
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await getShiftReport(queryParams)
    if (res.code === 200) {
      Object.assign(reportData, res.data)
      ElMessage.success('查询成功')
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.businessDate = new Date().toISOString().split('T')[0]
  queryParams.shift = 'MORNING'
  queryParams.operatorId = null
  handleQuery()
}

const handlePrint = () => {
  window.print()
}

const handleShift = () => {
  ElMessageBox.confirm('确认进行交班操作？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('交班成功')
  }).catch(() => {})
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.shift-report {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.report-content {
  min-height: 600px;
}

.report-header {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #303133;
}

.report-header h3 {
  margin: 0 0 10px 0;
}

.report-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.section {
  margin-bottom: 20px;
}

.section h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.sub-section {
  margin-bottom: 15px;
  margin-left: 20px;
}

.sub-section h5 {
  margin: 0 0 10px 0;
  color: #606266;
  font-weight: normal;
}
</style>
