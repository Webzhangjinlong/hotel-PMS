<template>
  <div class="night-audit-container">
    <el-card class="audit-card">
      <template #header>
        <div class="card-header">
          <span>执行夜审</span>
          <div class="header-actions">
            <el-select v-model="selectedHotelId" placeholder="选择酒店" style="width: 200px; margin-right: 10px;">
              <el-option
                v-for="hotel in hotelList"
                :key="hotel.id"
                :label="hotel.name"
                :value="hotel.id"
              />
            </el-select>
            <el-button 
              type="primary" 
              @click="executeAudit" 
              :loading="loading" 
              :disabled="currentAudit && currentAudit.status === 'IN_PROGRESS'"
            >
              执行夜审
            </el-button>
            <el-button 
              v-if="currentAudit && currentAudit.status === 'IN_PROGRESS'" 
              type="warning" 
              @click="resetAuditStatus"
              :loading="resetLoading"
            >
              重置状态
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 夜审状态 -->
      <div class="audit-status" v-if="currentAudit">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="审计日期">{{ currentAudit.auditDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentAudit.status)">
              {{ getStatusLabel(currentAudit.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(currentAudit.startedAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDateTime(currentAudit.completedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 执行进度 -->
      <div class="audit-progress" v-if="currentAudit && currentAudit.status === 'IN_PROGRESS'">
        <h4>执行进度</h4>
        <el-progress 
          :percentage="progressPercentage" 
          :status="progressStatus"
          :stroke-width="20"
          :text-inside="true"
        />
      </div>
      
      <!-- 步骤卡片化展示 -->
      <div class="audit-steps-cards" v-if="currentAudit && currentAudit.steps">
        <h4>执行步骤</h4>
        <div class="steps-grid">
          <div 
            v-for="(step, index) in currentAudit.steps" 
            :key="step.id"
            class="step-card"
            :class="getStepCardClass(step.status)"
          >
            <div class="step-card-header">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-title">{{ getStepTitle(step.stepName) }}</div>
              <el-tag :type="getStepTagType(step.status)" size="small">
                {{ getStepStatusLabel(step.status) }}
              </el-tag>
            </div>
            
            <div class="step-card-body">
              <div class="step-icon">
                <el-icon :size="32">
                  <component :is="getStepIcon(step.stepName)" />
                </el-icon>
              </div>
              
              <div class="step-info">
                <div v-if="step.startedAt" class="step-time">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(step.startedAt) }}</span>
                </div>
                <div v-if="step.completedAt" class="step-duration">
                  <el-icon><Timer /></el-icon>
                  <span>耗时: {{ calculateDuration(step.startedAt, step.completedAt) }}</span>
                </div>
                <div v-if="step.errorMessage" class="step-error">
                  <el-icon><Warning /></el-icon>
                  <span>{{ step.errorMessage }}</span>
                </div>
              </div>
            </div>
            
            <div class="step-card-footer" v-if="step.status === 'FAILED'">
              <el-button 
                type="warning" 
                size="small" 
                @click="retryStep(step)"
                :loading="step.retrying"
              >
                <el-icon><RefreshRight /></el-icon>
                重试
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 统计信息 -->
      <div class="audit-statistics" v-if="currentAudit && (currentAudit.status === 'COMPLETED' || currentAudit.status === 'COMPLETED_WITH_ERRORS')">
        <h4>统计信息</h4>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总房间数" :value="currentAudit.totalRooms || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="在住房间数" :value="currentAudit.occupiedRooms || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="入住率" :value="currentAudit.occupancyRate || 0" suffix="%" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="房费收入" :value="currentAudit.roomRevenue || 0" prefix="¥" />
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="6">
            <el-statistic title="ADR" :value="currentAudit.adr || 0" prefix="¥" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="RevPAR" :value="currentAudit.revpar || 0" prefix="¥" />
          </el-col>
        </el-row>
      </div>
      
      <!-- 执行日志 -->
      <div class="audit-logs" v-if="currentAudit">
        <div class="logs-header">
          <h4>执行日志</h4>
          <el-button size="small" @click="refreshLogs">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="logs-container" ref="logsContainer">
          <div v-if="logs.length === 0" class="logs-empty">
            暂无日志
          </div>
          <div 
            v-for="(log, index) in logs" 
            :key="index"
            class="log-item"
            :class="getLogClass(log.level)"
          >
            <span class="log-time">{{ log.time }}</span>
            <span class="log-level">{{ log.level }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
      
      <!-- 无数据提示 -->
      <div v-if="!currentAudit" class="empty-tip">
        <el-empty description="暂无夜审记录，点击上方按钮执行夜审" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, Clock, Timer, Warning, RefreshRight,
  CircleCheck, Money, User, OfficeBuilding, 
  Warning as PoliceIcon, DataLine, Document, Lock
} from '@element-plus/icons-vue'
import { executeNightAudit, getNightAuditList, getNightAuditDetail, retryNightAuditStep, resetNightAuditStatus } from '@/api/night-audit'
import request from '@/utils/request'

const loading = ref(false)
const resetLoading = ref(false)
const currentAudit = ref(null)
const selectedHotelId = ref(1)
const hotelList = ref([])
const logs = ref([])
const logsContainer = ref(null)

// 计算当前激活的步骤
const activeStep = computed(() => {
  if (!currentAudit.value || !currentAudit.value.steps) {
    return 0
  }
  const completedSteps = currentAudit.value.steps.filter(s => s.status === 'COMPLETED').length
  return completedSteps
})

// 计算进度百分比
const progressPercentage = computed(() => {
  if (!currentAudit.value || !currentAudit.value.steps) {
    return 0
  }
  const total = currentAudit.value.steps.length
  const completed = currentAudit.value.steps.filter(s => 
    s.status === 'COMPLETED' || s.status === 'FAILED' || s.status === 'SKIPPED'
  ).length
  return Math.round((completed / total) * 100)
})

// 计算进度状态
const progressStatus = computed(() => {
  if (!currentAudit.value) return ''
  if (currentAudit.value.status === 'COMPLETED') return 'success'
  if (currentAudit.value.status === 'FAILED') return 'exception'
  if (currentAudit.value.status === 'COMPLETED_WITH_ERRORS') return 'warning'
  return ''
})

// 获取步骤图标
const getStepIcon = (stepName) => {
  const iconMap = {
    'PRE_CHECK': CircleCheck,
    'AUTO_POST_ROOM_CHARGES': Money,
    'HANDLE_OVERTIME': User,
    'TEAM_FOLIO_SUMMARY': OfficeBuilding,
    'POLICE_UPLOAD_CHECK': PoliceIcon,
    'CALCULATE_STATISTICS': DataLine,
    'GENERATE_DAILY_REPORT': Document,
    'LOCK_DATA': Lock
  }
  return iconMap[stepName] || CircleCheck
}

// 获取步骤卡片样式类
const getStepCardClass = (status) => {
  const classMap = {
    'PENDING': 'step-pending',
    'IN_PROGRESS': 'step-running',
    'COMPLETED': 'step-completed',
    'FAILED': 'step-failed',
    'SKIPPED': 'step-skipped'
  }
  return classMap[status] || 'step-pending'
}

// 获取步骤标签类型
const getStepTagType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'FAILED': 'danger',
    'SKIPPED': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取步骤状态标签
const getStepStatusLabel = (status) => {
  const labelMap = {
    'PENDING': '待执行',
    'IN_PROGRESS': '执行中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'SKIPPED': '已跳过'
  }
  return labelMap[status] || status
}

// 计算耗时
const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return '-'
  const start = new Date(startTime)
  const end = new Date(endTime)
  const duration = Math.round((end - start) / 1000)
  if (duration < 60) return duration + '秒'
  const minutes = Math.floor(duration / 60)
  const seconds = duration % 60
  return minutes + '分' + seconds + '秒'
}

// 格式化时间（只显示时分秒）
const formatTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleTimeString('zh-CN')
}

// 加载酒店列表
const loadHotelList = async () => {
  try {
    const result = await request.get('/v1/hotels', { params: { pageNum: 1, pageSize: 100 } })
    hotelList.value = result.data?.records || []
    if (hotelList.value.length > 0 && !selectedHotelId.value) {
      selectedHotelId.value = hotelList.value[0].id
    }
  } catch (error) {
    console.error('加载酒店列表失败:', error)
  }
}

// 执行夜审
const executeAudit = async () => {
  if (!selectedHotelId.value) {
    ElMessage.warning('请先选择酒店')
    return
  }
  
  loading.value = true
  logs.value = []
  addLog('INFO', '开始执行夜审...')
  
  try {
    const result = await executeNightAudit(selectedHotelId.value)
    currentAudit.value = result.data
    addLog('INFO', '夜审执行成功')
    ElMessage.success('夜审执行成功')
  } catch (error) {
    addLog('ERROR', '夜审执行失败: ' + (error.message || '未知错误'))
    ElMessage.error('夜审执行失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置夜审状态
const resetAuditStatus = async () => {
  if (!currentAudit.value) return
  
  try {
    await ElMessageBox.confirm('确定要重置夜审状态吗？这将允许重新执行夜审。', '确认重置', {
      type: 'warning'
    })
    
    resetLoading.value = true
    addLog('WARN', '正在重置夜审状态...')
    await resetNightAuditStatus(currentAudit.value.id)
    addLog('INFO', '状态已重置')
    ElMessage.success('状态已重置')
    await loadLatestAudit()
  } catch (error) {
    if (error !== 'cancel') {
      addLog('ERROR', '重置失败: ' + (error.message || '未知错误'))
      ElMessage.error('重置失败: ' + (error.message || '未知错误'))
    }
  } finally {
    resetLoading.value = false
  }
}

// 重试失败步骤
const retryStep = async (step) => {
  if (!currentAudit.value) return
  
  try {
    await ElMessageBox.confirm('确定要重试步骤' + getStepTitle(step.stepName) + '吗？', '确认重试', {
      type: 'warning'
    })
    
    step.retrying = true
    addLog('INFO', '正在重试步骤: ' + getStepTitle(step.stepName))
    await retryNightAuditStep(currentAudit.value.id, step.id)
    addLog('INFO', '步骤重试成功')
    ElMessage.success('步骤重试成功')
    await loadLatestAudit()
  } catch (error) {
    if (error !== 'cancel') {
      addLog('ERROR', '重试失败: ' + (error.message || '未知错误'))
      ElMessage.error('重试失败: ' + (error.message || '未知错误'))
    }
  } finally {
    step.retrying = false
  }
}

// 刷新日志
const refreshLogs = () => {
  if (currentAudit.value && currentAudit.value.steps) {
    logs.value = []
    currentAudit.value.steps.forEach(step => {
      if (step.status === 'COMPLETED') {
        addLog('INFO', '步骤完成: ' + getStepTitle(step.stepName))
      } else if (step.status === 'FAILED') {
        addLog('ERROR', '步骤失败: ' + getStepTitle(step.stepName) + ' - ' + (step.errorMessage || '未知错误'))
      } else if (step.status === 'IN_PROGRESS') {
        addLog('INFO', '步骤执行中: ' + getStepTitle(step.stepName))
      }
    })
  }
}

// 添加日志
const addLog = (level, message) => {
  const now = new Date()
  const time = now.toLocaleTimeString('zh-CN')
  logs.value.push({ time, level, message })
  
  // 自动滚动到底部
  nextTick(() => {
    if (logsContainer.value) {
      logsContainer.value.scrollTop = logsContainer.value.scrollHeight
    }
  })
}

// 获取日志样式
const getLogClass = (level) => {
  const classMap = {
    'INFO': 'log-info',
    'WARN': 'log-warn',
    'ERROR': 'log-error'
  }
  return classMap[level] || 'log-info'
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

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(async () => {
  await loadHotelList()
  await loadLatestAudit()
})

const loadLatestAudit = async () => {
  try {
    const listResult = await getNightAuditList({ 
      hotelId: selectedHotelId.value, 
      pageNum: 1, 
      pageSize: 1 
    })
    
    if (listResult.data?.records?.length > 0) {
      const latestId = listResult.data.records[0].id
      const result = await getNightAuditDetail(latestId)
      currentAudit.value = result.data
      refreshLogs()
    } else {
      currentAudit.value = null
      logs.value = []
    }
  } catch (error) {
    console.error('加载夜审记录失败:', error)
    currentAudit.value = null
    logs.value = []
  }
}
</script>

<style scoped>
.night-audit-container {
  padding: 20px;
}

.audit-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.audit-status {
  margin-bottom: 20px;
}

.audit-progress {
  margin: 20px 0;
}

.audit-progress h4 {
  margin-bottom: 10px;
}

/* 步骤卡片样式 */
.audit-steps-cards {
  margin: 20px 0;
}

.audit-steps-cards h4 {
  margin-bottom: 15px;
}

.steps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.step-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.step-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.step-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #dcdfe6;
}

.step-pending::before {
  background: #909399;
}

.step-running {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.step-running::before {
  background: #e6a23c;
}

.step-completed {
  border-color: #67c23a;
  background: #f0f9eb;
}

.step-completed::before {
  background: #67c23a;
}

.step-failed {
  border-color: #f56c6c;
  background: #fef0f0;
}

.step-failed::before {
  background: #f56c6c;
}

.step-skipped::before {
  background: #c0c4cc;
}

.step-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.step-number {
  width: 28px;
  height: 28px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.step-completed .step-number {
  background: #67c23a;
}

.step-failed .step-number {
  background: #f56c6c;
}

.step-running .step-number {
  background: #e6a23c;
}

.step-title {
  flex: 1;
  font-weight: 600;
  font-size: 14px;
}

.step-card-body {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.step-icon {
  color: #909399;
}

.step-completed .step-icon {
  color: #67c23a;
}

.step-running .step-icon {
  color: #e6a23c;
}

.step-failed .step-icon {
  color: #f56c6c;
}

.step-info {
  flex: 1;
}

.step-time,
.step-duration,
.step-error {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.step-time .el-icon,
.step-duration .el-icon {
  color: #909399;
}

.step-error {
  color: #f56c6c;
}

.step-error .el-icon {
  color: #f56c6c;
}

.step-card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

/* 统计信息 */
.audit-statistics {
  margin-top: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 执行日志 */
.audit-logs {
  margin-top: 20px;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.logs-header h4 {
  margin: 0;
}

.logs-container {
  max-height: 300px;
  overflow-y: auto;
  background-color: #1e1e1e;
  border-radius: 4px;
  padding: 10px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.logs-empty {
  color: #6c757d;
  text-align: center;
  padding: 20px;
}

.log-item {
  padding: 4px 0;
  display: flex;
  gap: 10px;
}

.log-time {
  color: #6c757d;
  min-width: 80px;
}

.log-level {
  min-width: 50px;
  font-weight: bold;
}

.log-message {
  flex: 1;
  word-break: break-all;
}

.log-info .log-level {
  color: #17a2b8;
}

.log-info .log-message {
  color: #e9ecef;
}

.log-warn .log-level {
  color: #ffc107;
}

.log-warn .log-message {
  color: #ffc107;
}

.log-error .log-level {
  color: #dc3545;
}

.log-error .log-message {
  color: #dc3545;
}

.empty-tip {
  padding: 40px 0;
}
</style>