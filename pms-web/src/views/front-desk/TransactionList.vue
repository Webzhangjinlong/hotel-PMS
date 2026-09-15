<template>
  <div class="transaction-list">
    <div class="page-header">
      <h2>交易流水查询</h2>
      <div class="header-actions">
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出Excel
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="客人姓名">
          <el-input v-model="queryParams.guestName" placeholder="请输入客人姓名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="queryParams.roomNo" placeholder="请输入房间号" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select v-model="queryParams.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="押金" value="DEPOSIT" />
            <el-option label="房费" value="ROOM_FEE" />
            <el-option label="杂费" value="EXTRA" />
            <el-option label="付款" value="PAYMENT" />
            <el-option label="退款" value="REFUND" />
            <el-option label="冲账" value="REVERSAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="queryParams.paymentMethod" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="刷卡" value="POS" />
            <el-option label="银行转账" value="BANK_TRANSFER" />
            <el-option label="挂账" value="CREDIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="transactionList" v-loading="loading" stripe>
        <el-table-column prop="transactionNo" label="交易号" width="160" />
        <el-table-column prop="typeName" label="交易类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">{{ row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.type === 'REFUND' || row.type === 'REVERSAL' ? '#f56c6c' : '#67c23a' }">
              {{ row.type === 'REFUND' || row.type === 'REVERSAL' ? '-' : '' }}¥{{ (row.amount || 0).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethodName" label="支付方式" width="100" />
        <el-table-column prop="stayNo" label="入住单号" width="140">
          <template #default="{ row }">{{ row.stayNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="guestName" label="客人姓名" width="120">
          <template #default="{ row }">{{ row.guestName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="roomNo" label="房间号" width="80">
          <template #default="{ row }">{{ row.roomNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="creditCompanyName" label="挂账公司" width="120">
          <template #default="{ row }">{{ row.creditCompanyName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="交易时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const transactionList = ref([])
const total = ref(0)
const dateRange = ref([])

const queryParams = reactive({
  page: 1,
  size: 20,
  guestName: '',
  roomNo: '',
  type: '',
  paymentMethod: '',
  startDate: '',
  endDate: ''
})

// 监听日期范围变化
watch(dateRange, (val) => {
  if (val && val.length === 2) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
})

// 查询交易列表
const fetchTransactions = async () => {
  loading.value = true
  try {
    const res = await request.get('/v1/folios/transactions/flow', { params: queryParams })
    transactionList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('查询交易记录失败:', error)
    ElMessage.error('查询交易记录失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchTransactions()
}

// 重置
const handleReset = () => {
  queryParams.guestName = ''
  queryParams.roomNo = ''
  queryParams.type = ''
  queryParams.paymentMethod = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  dateRange.value = []
  handleSearch()
}

// 导出Excel
const handleExport = async () => {
  try {
    const params = { ...queryParams }
    delete params.page
    delete params.size
    
    const response = await request.get('/v1/folios/transactions/export', {
      params,
      responseType: 'blob'
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '交易流水.csv')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 获取类型标签样式
const getTypeTagType = (type) => {
  const map = {
    'DEPOSIT': 'info',
    'ROOM_FEE': '',
    'EXTRA': 'warning',
    'PAYMENT': 'success',
    'REFUND': 'danger',
    'REVERSAL': 'danger'
  }
  return map[type] || ''
}

onMounted(() => {
  fetchTransactions()
})
</script>

<style scoped lang="scss">
.transaction-list {
  padding: 20px;
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
  }
}

.filter-card {
  margin-bottom: 16px;
}

.table-card {
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>

