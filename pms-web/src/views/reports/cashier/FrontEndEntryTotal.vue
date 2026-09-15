<template>
  <div class="report-page">
    <div class="page-header">
      <h2>前台入账汇总</h2>
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
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 报表内容 -->
    <el-card class="report-content" v-loading="loading">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="transactionNo" label="交易号" width="150" />
        <el-table-column prop="type" label="交易类型" width="120">
          <template #default="scope">
            {{ getTypeName(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="scope">
            ¥{{ scope.row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="scope">
            {{ getPaymentMethodName(scope.row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" />
        <el-table-column prop="createdAt" label="操作时间" width="180" />
      </el-table>
      
      <div class="summary" v-if="summaryData">
        <el-divider />
        <p><strong>汇总：</strong></p>
        <el-descriptions :column="4" border>
          <el-descriptions-item label="总笔数">{{ summaryData.total || 0 }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ summaryData.totalAmount || '0.00' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEntryTotal } from '@/api/report'

const loading = ref(false)
const tableData = ref([])
const summaryData = ref(null)
const userList = ref([])

const queryParams = reactive({
  businessDate: new Date().toISOString().split('T')[0],
  shift: 'MORNING',
  operatorId: null
})

const getTypeName = (type) => {
  const map = {
    'DEPOSIT': '入账',
    'EXTRA': '杂费',
    'PAYMENT': '收款',
    'REFUND': '退款',
    'REVERSAL': '冲账',
    'TRANSFER': '转账'
  }
  return map[type] || type
}

const getPaymentMethodName = (method) => {
  const map = {
    'CASH': '现金',
    'WECHAT': '微信',
    'ALIPAY': '支付宝',
    'POS': '银行卡',
    'BANK_TRANSFER': '银行转账',
    'MEMBER': '会员支付',
    'CREDIT': '挂账',
    'COUPON': '代金券',
    'INTERNAL': '内部招待'
  }
  return map[method] || method
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await getEntryTotal(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      summaryData.value = res.data
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

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.report-page {
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
  min-height: 400px;
}

.summary {
  margin-top: 20px;
}
</style>
