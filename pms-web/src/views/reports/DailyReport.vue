<template>
  <div class="daily-report-container">
    <el-card class="report-card">
      <template #header>
        <div class="card-header">
          <span>营业日报</span>
          <div class="header-actions">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadReport"
            />
            <el-button type="primary" @click="loadReport" :loading="loading">
              查询
            </el-button>
            <el-button @click="exportExcel" :disabled="!reportData">
              <el-icon><Download /></el-icon>
              导出Excel
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 加载中 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      
      <!-- 无数据提示 -->
      <div v-else-if="!reportData" class="empty-container">
        <el-empty description="暂无营业日报数据">
          <template #description>
            <p>暂无 {{ selectedDate }} 的营业日报数据</p>
            <p class="empty-tip">请先执行夜审生成日报</p>
          </template>
        </el-empty>
      </div>
      
      <!-- 日报内容 -->
      <div v-else class="report-content">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border class="report-section">
          <el-descriptions-item label="酒店名称">{{ reportData.hotelName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审计日期">{{ reportData.auditDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(reportData.status)">
              {{ getStatusLabel(reportData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDateTime(reportData.completedAt) }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 房间统计 -->
        <el-descriptions title="房间统计" :column="3" border class="report-section">
          <el-descriptions-item label="总房间数">
            <span class="stat-value">{{ reportData.totalRooms || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="在住房间数">
            <span class="stat-value primary">{{ reportData.occupiedRooms || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="可用房间数">
            <span class="stat-value success">{{ reportData.availableRooms || 0 }}</span>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 经营指标 -->
        <el-descriptions title="经营指标" :column="3" border class="report-section">
          <el-descriptions-item label="入住率">
            <span class="stat-value warning">{{ reportData.occupancyRate || 0 }}%</span>
          </el-descriptions-item>
          <el-descriptions-item label="ADR（平均每日房价）">
            <span class="stat-value">¥{{ formatMoney(reportData.adr) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="RevPAR（每间可售房收入）">
            <span class="stat-value">¥{{ formatMoney(reportData.revpar) }}</span>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 收入统计 -->
        <el-descriptions title="收入统计" :column="3" border class="report-section">
          <el-descriptions-item label="房费收入">
            <span class="stat-value">¥{{ formatMoney(reportData.roomRevenue) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="其他收入">
            <span class="stat-value">¥{{ formatMoney(reportData.extraRevenue) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="总收入">
            <span class="stat-value highlight">¥{{ formatMoney(reportData.totalRevenue) }}</span>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 历史对比 -->
        <div class="comparison-section">
          <h4>历史对比</h4>
          <el-table :data="comparisonData" border size="small">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="occupancyRate" label="入住率" width="100">
              <template #default="{ row }">
                <span :class="getComparisonClass(row.occupancyRate, reportData.occupancyRate)">
                  {{ row.occupancyRate || 0 }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="adr" label="ADR" width="120">
              <template #default="{ row }">
                <span :class="getComparisonClass(row.adr, reportData.adr)">
                  ¥{{ formatMoney(row.adr) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="totalRevenue" label="总收入" width="120">
              <template #default="{ row }">
                <span :class="getComparisonClass(row.totalRevenue, reportData.totalRevenue)">
                  ¥{{ formatMoney(row.totalRevenue) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="change" label="变化" width="120">
              <template #default="{ row }">
                <span :class="getChangeClass(row.totalRevenue, reportData.totalRevenue)">
                  {{ getChangeText(row.totalRevenue, reportData.totalRevenue) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 图表展示 -->
        <div class="chart-section">
          <h4>近7日趋势</h4>
          <div class="chart-container">
            <div class="chart-bars">
              <div 
                v-for="(item, index) in trendData" 
                :key="index"
                class="chart-bar-item"
              >
                <div 
                  class="chart-bar"
                  :style="{ height: getBarHeight(item.occupancyRate) + '%' }"
                >
                  <span class="bar-label">{{ item.occupancyRate || 0 }}%</span>
                </div>
                <span class="bar-date">{{ formatDate(item.auditDate) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>

import { useUserStore } from '@/stores/user'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getNightAuditDetail } from '@/api/night-audit'
import request from '@/utils/request'
const userStore = useUserStore()

const loading = ref(false)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const reportData = ref(null)
const trendData = ref([])

// 计算对比数据
const comparisonData = computed(() => {
  if (!reportData.value) return []
  
  const yesterday = new Date(selectedDate.value)
  yesterday.setDate(yesterday.getDate() - 1)
  
  const lastWeek = new Date(selectedDate.value)
  lastWeek.setDate(lastWeek.getDate() - 7)
  
  return [
    {
      date: '昨日',
      occupancyRate: reportData.value.yesterdayOccupancy || 0,
      adr: reportData.value.yesterdayAdr || 0,
      totalRevenue: reportData.value.yesterdayRevenue || 0
    },
    {
      date: '上周同期',
      occupancyRate: reportData.value.lastWeekOccupancy || 0,
      adr: reportData.value.lastWeekAdr || 0,
      totalRevenue: reportData.value.lastWeekRevenue || 0
    }
  ]
})

// 加载日报数据
const loadReport = async () => {
  loading.value = true
  try {
    // 查询指定日期的夜审记录
    const result = await request.get('/v1/night-audit/list', {
      params: {
        hotelId: userStore.hotelId,
        startDate: selectedDate.value,
        endDate: selectedDate.value,
        pageNum: 1,
        pageSize: 1
      }
    })
    
    if (result.data && result.data.records && result.data.records.length > 0) {
      const auditId = result.data.records[0].id
      // 获取详细数据
      const detailResult = await getNightAuditDetail(auditId)
      reportData.value = detailResult.data
      
      // 加载趋势数据
      await loadTrendData()
    } else {
      reportData.value = null
      trendData.value = []
    }
  } catch (error) {
    console.error('加载日报失败:', error)
    ElMessage.error('加载日报数据失败')
    reportData.value = null
  } finally {
    loading.value = false
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const endDate = new Date(selectedDate.value)
    const startDate = new Date(selectedDate.value)
    startDate.setDate(startDate.getDate() - 6)
    
    const result = await request.get('/v1/night-audit/list', {
      params: {
        hotelId: userStore.hotelId,
        startDate: startDate.toISOString().split('T')[0],
        endDate: endDate.toISOString().split('T')[0],
        pageNum: 1,
        pageSize: 7
      }
    })
    
    if (result.data && result.data.records) {
      trendData.value = result.data.records.reverse()
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 导出Excel
const exportExcel = () => {
  if (!reportData.value) return
  
  // 创建CSV内容
  const headers = ['项目', '数值']
  const rows = [
    ['酒店名称', reportData.value.hotelName || '-'],
    ['审计日期', reportData.value.auditDate],
    ['总房间数', reportData.value.totalRooms || 0],
    ['在住房间数', reportData.value.occupiedRooms || 0],
    ['可用房间数', reportData.value.availableRooms || 0],
    ['入住率', (reportData.value.occupancyRate || 0) + '%'],
    ['ADR', reportData.value.adr || 0],
    ['RevPAR', reportData.value.revpar || 0],
    ['房费收入', reportData.value.roomRevenue || 0],
    ['其他收入', reportData.value.extraRevenue || 0],
    ['总收入', reportData.value.totalRevenue || 0]
  ]
  
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')
  
  // 添加BOM头解决中文乱码
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 营业日报_.csv
  link.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('导出成功')
}

// 格式化金额
const formatMoney = (value) => {
  if (value == null) return '0.00'
  return Number(value).toFixed(2)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return (d.getMonth() + 1) + '/' + d.getDate()
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

// 获取柱状图高度
const getBarHeight = (value) => {
  if (!value) return 0
  return Math.min(Math.max(value, 10), 100)
}

// 获取对比样式
const getComparisonClass = (current, target) => {
  if (current > target) return 'text-success'
  if (current < target) return 'text-danger'
  return ''
}

// 获取变化样式
const getChangeClass = (current, target) => {
  const change = ((current - target) / target) * 100
  if (change > 0) return 'text-success'
  if (change < 0) return 'text-danger'
  return ''
}

// 获取变化文本
const getChangeText = (current, target) => {
  if (!target) return '-'
  const change = ((current - target) / target) * 100
  if (change > 0) return '+' + change.toFixed(1) + '%'
  if (change < 0) return change.toFixed(1) + '%'
  return '0%'
}

onMounted(() => {
  loadReport()
})
</script>

<style scoped>
.daily-report-container {
  padding: 20px;
}

.report-card {
  max-width: 1200px;
  margin: 0 auto;
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

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px 0;
}

.empty-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.report-content {
  padding: 10px 0;
}

.report-section {
  margin-bottom: 20px;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.highlight {
  color: #f56c6c;
  font-size: 18px;
}

.comparison-section {
  margin-top: 20px;
}

.comparison-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.text-success {
  color: #67c23a;
  font-weight: bold;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.chart-section {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.chart-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.chart-container {
  padding: 10px 0;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200px;
  padding: 0 20px;
}

.chart-bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  max-width: 80px;
}

.chart-bar {
  width: 40px;
  background: linear-gradient(180deg, #409eff, #67c23a);
  border-radius: 4px 4px 0 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  min-height: 20px;
  transition: height 0.5s ease;
}

.bar-label {
  color: white;
  font-size: 10px;
  font-weight: bold;
  padding-top: 4px;
}

.bar-date {
  margin-top: 8px;
  font-size: 12px;
  color: #606266;
}
</style>