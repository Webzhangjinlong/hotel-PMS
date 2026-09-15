<template>
  <div class="report-page">
    <div class="page-header">
      <h2>商品销售明细</h2>
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
          <el-select v-model="queryParams.operator" placeholder="请选择操作人" clearable>
            <el-option v-for="user in userList" :key="user.id" :label="user.username" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 报表内容 -->
    <el-card class="report-content" v-loading="loading">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="stayNo" label="入住单号" width="150" />
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column prop="guestName" label="客人姓名" width="120" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="paymentMethod" label="支付方式" width="120" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" link size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-if="tableData.length > 0"
        class="pagination"
        :current-page="queryParams.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="queryParams.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const userList = ref([])

const queryParams = reactive({
  businessDate: new Date().toISOString().split('T')[0],
  shift: 'MORNING',
  operator: '',
  page: 1,
  pageSize: 10
})

const handleQuery = () => {
  loading.value = true
  // TODO: 调用后端API获取数据
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询成功')
  }, 500)
}

const handleReset = () => {
  queryParams.businessDate = new Date().toISOString().split('T')[0]
  queryParams.shift = 'MORNING'
  queryParams.operator = ''
  queryParams.page = 1
  handleQuery()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handleSizeChange = (size) => {
  queryParams.pageSize = size
  handleQuery()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  handleQuery()
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
