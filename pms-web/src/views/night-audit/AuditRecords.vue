<template>
  <div class="audit-records-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>夜审记录</span>
          <div class="header-actions">
            <el-select v-model="queryParams.hotelId" placeholder="选择酒店" style="width: 200px;" clearable>
              <el-option
                v-for="hotel in hotelList"
                :key="hotel.id"
                :label="hotel.name"
                :value="hotel.id"
              />
            </el-select>
            <el-select v-model="queryParams.status" placeholder="状态" style="width: 120px;" clearable>
              <el-option label="待执行" value="PENDING" />
              <el-option label="执行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="执行失败" value="FAILED" />
              <el-option label="有错误完成" value="COMPLETED_WITH_ERRORS" />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="handleDateChange"
            />
            <el-button type="primary" @click="fetchRecords">查询</el-button>
          </div>
        </div>
      </template>
      
      <!-- 夜审记录表格 -->
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="auditDate" label="审计日期" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalRooms" label="总房间数" width="100" />
        <el-table-column prop="occupiedRooms" label="在住房间数" width="120" />
        <el-table-column prop="occupancyRate" label="入住率" width="100">
          <template #default="{ row }">
            {{ row.occupancyRate || 0 }}%
          </template>
        </el-table-column>
        <el-table-column prop="roomRevenue" label="房费收入" width="120">
          <template #default="{ row }">
            ¥{{ (row.roomRevenue || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="adr" label="ADR" width="100">
          <template #default="{ row }">
            ¥{{ (row.adr || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="revpar" label="RevPAR" width="100">
          <template #default="{ row }">
            ¥{{ (row.revpar || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="completedAt" label="完成时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.completedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="夜审详情" width="800px">
      <div v-if="selectedAudit">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="审计日期">{{ selectedAudit.auditDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedAudit.status)">
              {{ getStatusLabel(selectedAudit.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总房间数">{{ selectedAudit.totalRooms || 0 }}</el-descriptions-item>
          <el-descriptions-item label="在住房间数">{{ selectedAudit.occupiedRooms || 0 }}</el-descriptions-item>
          <el-descriptions-item label="入住率">{{ selectedAudit.occupancyRate || 0 }}%</el-descriptions-item>
          <el-descriptions-item label="房费收入">¥{{ (selectedAudit.roomRevenue || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="ADR">¥{{ (selectedAudit.adr || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="RevPAR">¥{{ (selectedAudit.revpar || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(selectedAudit.startedAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDateTime(selectedAudit.completedAt) }}</el-descriptions-item>
        </el-descriptions>
        
        <h4 style="margin-top: 20px;">执行步骤</h4>
        <el-steps :active="detailActiveStep" finish-status="success" direction="vertical">
          <el-step 
            v-for="step in selectedAudit.steps" 
            :key="step.id"
            :title="getStepTitle(step.stepName)"
            :status="getStepStatus(step.status)"
          >
            <template #description>
              <div class="step-description">
                <span>{{ getStepDescription(step) }}</span>
                <el-button 
                  v-if="step.status === 'FAILED'" 
                  type="warning" 
                  size="small" 
                  @click="retryStep(step)"
                  :loading="step.retrying"
                  style="margin-left: 10px;"
                >
                  重试
                </el-button>
              </div>
            </template>
          </el-step>
        </el-steps>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNightAuditList, getNightAuditDetail, retryNightAuditStep } from '@/api/night-audit'
import request from '@/utils/request'

const loading = ref(false)
const records = ref([])
const dateRange = ref([])
const hotelList = ref([])
const queryParams = ref({
  hotelId: null,
  status: null,
  pageNum: 1,
  pageSize: 10
})
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const detailDialogVisible = ref(false)
const selectedAudit = ref(null)

// 计算详情对话框中激活的步骤
const detailActiveStep = computed(() => {
  if (!selectedAudit.value || !selectedAudit.value.steps) {
    return 0
  }
  return selectedAudit.value.steps.filter(s => s.status === 'COMPLETED').length
})

// 加载酒店列表
const loadHotelList = async () => {
  try {
    const result = await request.get('/v1/hotels', { params: { pageNum: 1, pageSize: 100 } })
    hotelList.value = result.data?.records || []
  } catch (error) {
    console.error('加载酒店列表失败:', error)
  }
}

// 获取夜审记录
const fetchRecords = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const result = await getNightAuditList(params)
    records.value = result.data.records || []
    pagination.value.total = result.data.total || 0
  } catch (error) {
    console.error('获取夜审记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = async (row) => {
  try {
    const result = await getNightAuditDetail(row.id)
    selectedAudit.value = result.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取夜审详情失败:', error)
  }
}

// 重试失败步骤
const retryStep = async (step) => {
  if (!selectedAudit.value) return
  
  try {
    await ElMessageBox.confirm('确定要重试步骤' + getStepTitle(step.stepName) + '吗？', '确认重试', {
      type: 'warning'
    })
    
    step.retrying = true
    await retryNightAuditStep(selectedAudit.value.id, step.id)
    ElMessage.success('步骤重试成功')
    
    // 刷新详情
    const result = await getNightAuditDetail(selectedAudit.value.id)
    selectedAudit.value = result.data
    
    // 刷新列表
    await fetchRecords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重试失败: ' + (error.message || '未知错误'))
    }
  } finally {
    step.retrying = false
  }
}

// 日期变化处理
const handleDateChange = () => {
  pagination.value.pageNum = 1
  fetchRecords()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  fetchRecords()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.value.pageNum = page
  fetchRecords()
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'FAILED': 'danger',
    'COMPLETED_WITH_ERRORS': 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    'PENDING': '待执行',
    'IN_PROGRESS': '执行中',
    'COMPLETED': '已完成',
    'FAILED': '执行失败',
    'COMPLETED_WITH_ERRORS': '有错误完成'
  }
  return statusMap[status] || status
}

// 获取步骤标题
const getStepTitle = (stepName) => {
  const titleMap = {
    'PRE_CHECK': '预检查',
    'AUTO_POST_ROOM_CHARGES': '自动过房费',
    'HANDLE_OVERTIME': '处理超时离店',
    'TEAM_FOLIO_SUMMARY': '团队账务汇总',
    'POLICE_UPLOAD_CHECK': '公安上传检查',
    'CALCULATE_STATISTICS': '计算统计指标',
    'GENERATE_DAILY_REPORT': '生成营业日报',
    'LOCK_DATA': '锁定数据'
  }
  return titleMap[stepName] || stepName
}

// 获取步骤描述
const getStepDescription = (step) => {
  if (step.status === 'COMPLETED') {
    return '已完成'
  } else if (step.status === 'FAILED') {
    return '失败: ' + (step.errorMessage || '未知错误')
  } else if (step.status === 'IN_PROGRESS') {
    return '执行中...'
  }
  return '待执行'
}

// 获取步骤状态
const getStepStatus = (status) => {
  const statusMap = {
    'PENDING': 'wait',
    'IN_PROGRESS': 'process',
    'COMPLETED': 'success',
    'FAILED': 'error',
    'SKIPPED': 'skip'
  }
  return statusMap[status] || 'wait'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(async () => {
  await loadHotelList()
  await fetchRecords()
})
</script>

<style scoped>
.audit-records-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.step-description {
  display: flex;
  align-items: center;
}
</style>
