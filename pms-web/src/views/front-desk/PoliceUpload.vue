<template>
  <div class="police-upload">
    <div class="page-header">
      <h2>公安上传</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showManualUploadDialog">
          <el-icon><Plus /></el-icon>
          人工补传
        </el-button>
        <el-button @click="handleBatchRetry" :disabled="selectedIds.length === 0">
          <el-icon><Refresh /></el-icon>
          批量重试
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">今日上传</div>
          <div class="stat-value primary">{{ stats.todayCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">上传成功</div>
          <div class="stat-value success">{{ stats.successCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">上传失败</div>
          <div class="stat-value danger">{{ stats.failCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">待上传</div>
          <div class="stat-value warning">{{ stats.pendingCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="待上传" value="PENDING" />
            <el-option label="上传中" value="UPLOADING" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="客人姓名">
          <el-input v-model="queryParams.guestName" placeholder="请输入客人姓名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="上传日期">
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
      <el-table :data="uploadList" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="stayNo" label="入住单号" width="140" />
        <el-table-column prop="guestName" label="客人姓名" width="120" />
        <el-table-column prop="guestIdNo" label="证件号码" width="180" />
        <el-table-column prop="guestPhone" label="手机号" width="130" />
        <el-table-column prop="checkInTime" label="入住时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.uploadTime) }}</template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="失败原因" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.status === 'FAILED'" class="error-message">{{ row.errorMessage }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'FAILED'" type="warning" link size="small" @click="handleRetry(row)">重试</el-button>
          </template>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="上传记录详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="入住单号">{{ currentRecord.stayNo }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ currentRecord.guestName }}</el-descriptions-item>
        <el-descriptions-item label="证件号码">{{ currentRecord.guestIdNo }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentRecord.guestPhone }}</el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatDateTime(currentRecord.checkInTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord.status)">{{ getStatusLabel(currentRecord.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ formatDateTime(currentRecord.uploadTime) }}</el-descriptions-item>
        <el-descriptions-item label="失败原因" :span="2">
          <span v-if="currentRecord.status === 'FAILED'" class="error-message">{{ currentRecord.errorMessage || '-' }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(currentRecord.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentRecord.status === 'FAILED'" type="warning" @click="handleRetry(currentRecord)">重试上传</el-button>
      </template>
    </el-dialog>

    <!-- 人工补传弹窗 -->
    <el-dialog v-model="manualUploadVisible" title="人工补传" width="600px">
      <el-form :model="manualForm" :rules="manualRules" ref="manualFormRef" label-width="100px">
        <el-form-item label="入住单号" prop="stayNo">
          <el-input v-model="manualForm.stayNo" placeholder="请输入入住单号" @blur="fetchStayInfo" />
        </el-form-item>
        <el-form-item label="客人姓名" prop="guestName">
          <el-input v-model="manualForm.guestName" placeholder="请输入客人姓名" />
        </el-form-item>
        <el-form-item label="证件号码" prop="guestIdNo">
          <el-input v-model="manualForm.guestIdNo" placeholder="请输入证件号码" />
        </el-form-item>
        <el-form-item label="手机号" prop="guestPhone">
          <el-input v-model="manualForm.guestPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="入住时间" prop="checkInTime">
          <el-date-picker v-model="manualForm.checkInTime" type="datetime" placeholder="选择入住时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="manualForm.remark" type="textarea" :rows="3" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualUploadVisible = false">取消</el-button>
        <el-button type="primary" @click="handleManualUpload" :loading="uploading">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { getPoliceUploadList, retryPoliceUpload, batchRetryPoliceUpload, manualPoliceUpload, getPoliceUploadStats } from '@/api/police'
import { getStayList } from '@/api/stay'

const loading = ref(false)
const uploading = ref(false)
const uploadList = ref([])
const total = ref(0)
const selectedIds = ref([])
const dateRange = ref([])

const queryParams = reactive({
  hotelId: 1, // 默认酒店ID
  status: '',
  guestName: '',
  startDate: '',
  endDate: '',
  page: 1,
  size: 20
})

const stats = ref({
  todayCount: 0,
  successCount: 0,
  failCount: 0,
  pendingCount: 0
})

const detailVisible = ref(false)
const currentRecord = ref({})

const manualUploadVisible = ref(false)
const manualFormRef = ref(null)
const manualForm = reactive({
  stayNo: '',
  guestName: '',
  guestIdNo: '',
  guestPhone: '',
  checkInTime: '',
  remark: ''
})

const manualRules = {
  stayNo: [{ required: true, message: '请输入入住单号', trigger: 'blur' }],
  guestName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  guestIdNo: [{ required: true, message: '请输入证件号码', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  checkInTime: [{ required: true, message: '请选择入住时间', trigger: 'change' }]
}

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

// 获取上传列表
const fetchUploadList = async () => {
  loading.value = true
  try {
    const res = await getPoliceUploadList(queryParams)
    uploadList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取上传列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await getPoliceUploadStats({ hotelId: queryParams.hotelId })
    stats.value = res.data || {}
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchUploadList()
}

// 重置
const handleReset = () => {
  queryParams.status = ''
  queryParams.guestName = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  dateRange.value = []
  handleSearch()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 查看详情
const handleView = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

// 单条重试
const handleRetry = async (row) => {
  try {
    await ElMessageBox.confirm('确定要重试上传该记录吗？', '确认重试', { type: 'warning' })
    await retryPoliceUpload(row.id)
    ElMessage.success('重试成功')
    fetchUploadList()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重试失败:', error)
      ElMessage.error('重试失败')
    }
  }
}

// 批量重试
const handleBatchRetry = async () => {
  try {
    await ElMessageBox.confirm(`确定要重试上传选中的 ${selectedIds.value.length} 条记录吗？`, '确认批量重试', { type: 'warning' })
    await batchRetryPoliceUpload(selectedIds.value)
    ElMessage.success('批量重试成功')
    selectedIds.value = []
    fetchUploadList()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量重试失败:', error)
      ElMessage.error('批量重试失败')
    }
  }
}

// 显示人工补传弹窗
const showManualUploadDialog = () => {
  manualForm.stayNo = ''
  manualForm.guestName = ''
  manualForm.guestIdNo = ''
  manualForm.guestPhone = ''
  manualForm.checkInTime = ''
  manualForm.remark = ''
  manualUploadVisible.value = true
}

// 根据入住单号获取客人信息
const fetchStayInfo = async () => {
  if (!manualForm.stayNo) return
  try {
    const res = await getStayList({ stayNo: manualForm.stayNo, hotelId: queryParams.hotelId })
    if (res.data?.records?.length > 0) {
      const stay = res.data.records[0]
      manualForm.guestName = stay.guestName || ''
      manualForm.guestIdNo = stay.guestIdNo || ''
      manualForm.guestPhone = stay.guestPhone || ''
      manualForm.checkInTime = stay.checkInTime || ''
    }
  } catch (error) {
    console.error('获取入住信息失败:', error)
  }
}

// 人工补传
const handleManualUpload = async () => {
  const valid = await manualFormRef.value.validate().catch(() => false)
  if (!valid) return

  uploading.value = true
  try {
    await manualPoliceUpload({
      hotelId: queryParams.hotelId,
      ...manualForm
    })
    ElMessage.success('人工补传成功')
    manualUploadVisible.value = false
    fetchUploadList()
    fetchStats()
  } catch (error) {
    console.error('人工补传失败:', error)
    ElMessage.error('人工补传失败')
  } finally {
    uploading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态标签类型
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'UPLOADING': 'info',
    'SUCCESS': 'success',
    'FAILED': 'danger'
  }
  return map[status] || ''
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  const map = {
    'PENDING': '待上传',
    'UPLOADING': '上传中',
    'SUCCESS': '成功',
    'FAILED': '失败'
  }
  return map[status] || status
}

onMounted(() => {
  fetchUploadList()
  fetchStats()
})
</script>

<style scoped lang="scss">
.police-upload {
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

  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 16px;

  .stat-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 24px;
    font-weight: bold;

    &.primary {
      color: #409eff;
    }

    &.success {
      color: #67c23a;
    }

    &.danger {
      color: #f56c6c;
    }

    &.warning {
      color: #e6a23c;
    }
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

.error-message {
  color: #f56c6c;
  font-size: 12px;
}
</style>
