<template>
  <div class="archive-container">
    <el-card class="archive-card">
      <template #header>
        <div class="card-header">
          <span>夜审历史数据管理</span>
        </div>
      </template>
      
      <!-- 归档操作区 -->
      <div class="archive-section">
        <h4>数据归档</h4>
        <el-alert
          title="归档说明"
          type="info"
          :closable="false"
          show-icon
          class="archive-alert"
        >
          <template #default>
            <p>将指定日期之前的已完成夜审数据归档到历史表，释放数据库空间。</p>
            <p>归档后的数据仍可查询，但不会影响日常夜审执行。</p>
          </template>
        </el-alert>
        
        <el-form :inline="true" class="archive-form">
          <el-form-item label="归档截止日期">
            <el-date-picker
              v-model="archiveDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledDate"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadStats" :loading="statsLoading">
              查询统计
            </el-button>
            <el-button type="warning" @click="confirmArchive" :loading="archiveLoading" >
              执行归档
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 归档统计 -->
        <div v-if="archiveStats" class="stats-container">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-statistic title="待归档记录" :value="archiveStats.pendingCount || 0" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="已归档记录" :value="archiveStats.archivedCount || 0" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="最早记录日期" :value="archiveStats.earliestDate || '-'" />
            </el-col>
          </el-row>
        </div>
      </div>
      
      <el-divider />
      
      <!-- 归档记录查询 -->
      <div class="archive-section">
        <h4>归档记录</h4>
        <el-form :inline="true" class="query-form">
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="loadArchiveList"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadArchiveList">查询</el-button>
          </el-form-item>
        </el-form>
        
        <el-table :data="archiveList" v-loading="listLoading" stripe>
          <el-table-column prop="auditDate" label="审计日期" width="120" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalRooms" label="总房间" width="80" />
          <el-table-column prop="occupiedRooms" label="在住" width="80" />
          <el-table-column prop="occupancyRate" label="入住率" width="100">
            <template #default="{ row }">{{ row.occupancyRate || 0 }}%</template>
          </el-table-column>
          <el-table-column prop="roomRevenue" label="房费收入" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.roomRevenue) }}</template>
          </el-table-column>
          <el-table-column prop="totalRevenue" label="总收入" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.totalRevenue) }}</template>
          </el-table-column>
          <el-table-column prop="archivedAt" label="归档时间" width="180" />
          <el-table-column prop="archivedBy" label="归档人" width="100" />
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next"
            @size-change="loadArchiveList"
            @current-change="loadArchiveList"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>

import { useUserStore } from '@/stores/user'
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { archiveNightAuditData, getArchiveList, getArchiveStats } from '@/api/night-audit-archive'
const userStore = useUserStore()

const archiveDate = ref('')
const archiveStats = ref({})
const archiveList = ref([])
const dateRange = ref([])
const statsLoading = ref(false)
const archiveLoading = ref(false)
const listLoading = ref(false)
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 禁用未来日期
const disabledDate = (date) => {
  return date.getTime() > Date.now() - 86400000 // 禁用昨天之后的日期
}

// 加载统计数据
const loadStats = async () => {
  if (!archiveDate.value) {
    ElMessage.warning('请选择归档截止日期')
    return
  }
  
  statsLoading.value = true
  try {
    const result = await getArchiveStats(1, archiveDate.value) // 默认酒店ID为1
    archiveStats.value = result.data || {}
  } catch (error) {
    console.error('加载统计失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

// 确认归档
const confirmArchive = async () => {
  if (!archiveDate.value) {
    ElMessage.warning('请选择归档截止日期')
    return
  }
  
  // 如果还没查询过统计数据，先查询
  if (!archiveStats.value || archiveStats.value.pendingCount === undefined) {
    await loadStats()
    if (!archiveStats.value || archiveStats.value.pendingCount === 0) {
      ElMessage.info('没有需要归档的数据')
      return
    }
  }
  
  if (archiveStats.value.pendingCount === 0) {
    ElMessage.info('没有需要归档的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要归档 ${archiveDate.value} 之前的夜审数据吗？此操作不可恢复。`,
      '确认归档',
      { type: 'warning' }
    )
    
    archiveLoading.value = true
    const result = await archiveNightAuditData(1, archiveDate.value)
    ElMessage.success(`成功归档 ${result.data} 条记录`)
    await loadStats()
    await loadArchiveList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('归档失败:', error)
      ElMessage.error('归档失败')
    }
  } finally {
    archiveLoading.value = false
  }
}

// 加载归档列表
const loadArchiveList = async () => {
  listLoading.value = true
  try {
    const params = {
      hotelId: userStore.hotelId,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const result = await getArchiveList(params)
    archiveList.value = result.data?.records || []
    pagination.value.total = result.data?.total || 0
  } catch (error) {
    console.error('加载归档列表失败:', error)
  } finally {
    listLoading.value = false
  }
}

// 格式化金额
const formatMoney = (value) => {
  if (value == null) return '0.00'
  return Number(value).toFixed(2)
}

// 获取状态类型
const getStatusType = (status) => {
  const map = { 'COMPLETED': 'success', 'COMPLETED_WITH_ERRORS': 'warning' }
  return map[status] || 'info'
}

// 获取状态标签
const getStatusLabel = (status) => {
  const map = { 'COMPLETED': '已完成', 'COMPLETED_WITH_ERRORS': '有错误完成' }
  return map[status] || status
}

onMounted(() => {
  // 设置默认归档日期为30天前
  const date = new Date()
  date.setDate(date.getDate() - 30)
  archiveDate.value = date.toISOString().split('T')[0]
  
  loadArchiveList()
})
</script>

<style scoped>
.archive-container {
  padding: 20px;
}

.archive-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.archive-section {
  margin-bottom: 20px;
}

.archive-section h4 {
  margin-bottom: 15px;
  color: #303133;
}

.archive-alert {
  margin-bottom: 20px;
}

.archive-form {
  margin-bottom: 20px;
}

.stats-container {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-top: 20px;
}

.query-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>