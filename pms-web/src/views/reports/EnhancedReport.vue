<template>
  <div class="enhanced-report">
    <div class="page-header">
      <h2>报表增强</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 300px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 核心指标卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-title">总收入</div>
          <div class="metric-value primary">¥{{ formatMoney(reportData.totalRevenue) }}</div>
          <div class="metric-desc">统计周期内总收入</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-title">总订单数</div>
          <div class="metric-value">{{ reportData.totalOrders || 0 }}</div>
          <div class="metric-desc">统计周期内总间夜数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-title">平均入住率</div>
          <div class="metric-value">{{ formatPercentage(reportData.avgOccupancyRate) }}%</div>
          <div class="metric-desc">统计周期内平均入住率</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-title">平均房价</div>
          <div class="metric-value">¥{{ formatMoney(reportData.avgAdr) }}</div>
          <div class="metric-desc">统计周期内平均房价</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <!-- 渠道收入占比 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>渠道收入占比</span>
          </template>
          <div class="chart-container">
            <div ref="channelChart" style="width: 100%; height: 400px;"></div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 收入趋势 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>收入趋势</span>
          </template>
          <div class="chart-container">
            <div ref="revenueChart" style="width: 100%; height: 400px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <!-- 入住率趋势 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>入住率趋势</span>
          </template>
          <div class="chart-container">
            <div ref="occupancyChart" style="width: 100%; height: 400px;"></div>
          </div>
        </el-card>
      </el-col>
      
      <!-- ADR趋势 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>ADR趋势</span>
          </template>
          <div class="chart-container">
            <div ref="adrChart" style="width: 100%; height: 400px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-card shadow="never" style="margin-bottom: 20px;">
      <template #header>
        <span>渠道收入明细</span>
      </template>
      <el-table :data="reportData.channelRevenues" stripe>
        <el-table-column prop="channelName" label="渠道名称" width="150" />
        <el-table-column prop="orderCount" label="订单数量" width="120" align="center" />
        <el-table-column label="收入金额" width="150" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.revenue) }}</template>
        </el-table-column>
        <el-table-column label="占比" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.percentage)" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column label="平均房价" width="150" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.avgRate) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 趋势数据表格 -->
    <el-card shadow="never">
      <template #header>
        <span>趋势数据明细</span>
      </template>
      <el-table :data="reportData.trendData" stripe>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column label="收入" width="150" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.revenue) }}</template>
        </el-table-column>
        <el-table-column label="入住率" width="120" align="center">
          <template #default="{ row }">{{ formatPercentage(row.occupancyRate) }}%</template>
        </el-table-column>
        <el-table-column label="ADR" width="120" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.adr) }}</template>
        </el-table-column>
        <el-table-column label="RevPAR" width="120" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.revpar) }}</template>
        </el-table-column>
        <el-table-column prop="roomNights" label="间夜数" width="100" align="center" />
        <el-table-column prop="availableRooms" label="可售房间" width="100" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import { getFullReport } from '@/api/report'
const userStore = useUserStore()

// 用户状态（取当前登录酒店 ID）

// 查询参数
const queryParams = reactive({
  hotelId: null, // 当前登录酒店（onMounted 从登录态获取）
  startDate: '',
  endDate: ''
})

const dateRange = ref([])

// 报表数据
const reportData = ref({
  metrics: {},
  channelRevenues: [],
  trendData: [],
  totalRevenue: 0,
  totalOrders: 0,
  avgOccupancyRate: 0,
  avgAdr: 0
})

// 图表引用
const channelChart = ref(null)
const revenueChart = ref(null)
const occupancyChart = ref(null)
const adrChart = ref(null)

// 图表实例
let channelChartInstance = null
let revenueChartInstance = null
let occupancyChartInstance = null
let adrChartInstance = null

// 加载状态
const loading = ref(false)

/**
 * 加载报表数据
 */
const loadReportData = async () => {
  if (!queryParams.startDate || !queryParams.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }
  
  loading.value = true
  try {
    const response = await getFullReport(queryParams)
    if (response.code === 200) {
      reportData.value = response.data
      await nextTick()
      renderCharts()
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询报表数据失败:', error)
    ElMessage.error('查询报表数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startDate = dateRange.value[0]
    queryParams.endDate = dateRange.value[1]
    loadReportData()
  } else {
    ElMessage.warning('请选择日期范围')
  }
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  dateRange.value = []
  queryParams.startDate = ''
  queryParams.endDate = ''
}

/**
 * 导出报表
 */
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

/**
 * 渲染图表
 */
const renderCharts = () => {
  renderChannelChart()
  renderRevenueChart()
  renderOccupancyChart()
  renderAdrChart()
}

/**
 * 渲染渠道收入占比图表
 */
const renderChannelChart = () => {
  if (!channelChart.value) return
  
  if (channelChartInstance) {
    channelChartInstance.dispose()
  }
  
  channelChartInstance = echarts.init(channelChart.value)
  
  const data = reportData.value.channelRevenues.map(item => ({
    name: item.channelName,
    value: Number(item.revenue)
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '渠道收入',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '30',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  channelChartInstance.setOption(option)
}

/**
 * 渲染收入趋势图表
 */
const renderRevenueChart = () => {
  if (!revenueChart.value) return
  
  if (revenueChartInstance) {
    revenueChartInstance.dispose()
  }
  
  revenueChartInstance = echarts.init(revenueChart.value)
  
  const dates = reportData.value.trendData.map(item => item.date)
  const revenues = reportData.value.trendData.map(item => Number(item.revenue))
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>收入: ¥{c}'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        data: revenues,
        smooth: true,
        areaStyle: {
          opacity: 0.3
        },
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  
  revenueChartInstance.setOption(option)
}

/**
 * 渲染入住率趋势图表
 */
const renderOccupancyChart = () => {
  if (!occupancyChart.value) return
  
  if (occupancyChartInstance) {
    occupancyChartInstance.dispose()
  }
  
  occupancyChartInstance = echarts.init(occupancyChart.value)
  
  const dates = reportData.value.trendData.map(item => item.date)
  const occupancyRates = reportData.value.trendData.map(item => Number(item.occupancyRate))
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>入住率: {c}%'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '入住率',
        type: 'line',
        data: occupancyRates,
        smooth: true,
        areaStyle: {
          opacity: 0.3
        },
        itemStyle: {
          color: '#67c23a'
        }
      }
    ]
  }
  
  occupancyChartInstance.setOption(option)
}

/**
 * 渲染ADR趋势图表
 */
const renderAdrChart = () => {
  if (!adrChart.value) return
  
  if (adrChartInstance) {
    adrChartInstance.dispose()
  }
  
  adrChartInstance = echarts.init(adrChart.value)
  
  const dates = reportData.value.trendData.map(item => item.date)
  const adrs = reportData.value.trendData.map(item => Number(item.adr))
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>ADR: ¥{c}'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: 'ADR',
        type: 'line',
        data: adrs,
        smooth: true,
        areaStyle: {
          opacity: 0.3
        },
        itemStyle: {
          color: '#e6a23c'
        }
      }
    ]
  }
  
  adrChartInstance.setOption(option)
}

/**
 * 格式化金额
 */
const formatMoney = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

/**
 * 格式化百分比
 */
const formatPercentage = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

// 监听窗口大小变化，重新渲染图表
window.addEventListener('resize', () => {
  if (channelChartInstance) channelChartInstance.resize()
  if (revenueChartInstance) revenueChartInstance.resize()
  if (occupancyChartInstance) occupancyChartInstance.resize()
  if (adrChartInstance) adrChartInstance.resize()
})

// 初始化加载数据
onMounted(() => {
  // 从登录态获取当前酒店 ID
  queryParams.hotelId = userStore.hotelId || null

  // 设置默认日期范围（最近7天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 7)
  
  dateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  queryParams.startDate = dateRange.value[0]
  queryParams.endDate = dateRange.value[1]
  
  loadReportData()
})
</script>

<style scoped lang="scss">
.enhanced-report {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.metric-card {
  text-align: center;
  padding: 20px;
}

.metric-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
  
  &.primary {
    color: #409eff;
  }
}

.metric-desc {
  font-size: 12px;
  color: #c0c4cc;
}

.chart-container {
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
