<template>
  <div class="night-audit-config-container">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>夜审配置</span>
          <el-button type="primary" @click="saveConfig" :loading="saving">
            保存配置
          </el-button>
        </div>
      </template>
      
      <el-form :model="configForm" label-width="150px" class="config-form">
        <!-- 基本配置 -->
        <el-divider content-position="left">基本配置</el-divider>
        
        <el-form-item label="夜审时间">
          <el-time-picker
            v-model="configForm.auditTime"
            format="HH:mm"
            value-format="HH:mm:ss"
            placeholder="选择夜审时间"
          />
          <div class="form-tip">每天自动执行夜审的时间</div>
        </el-form-item>
        
        <el-form-item label="自动夜审">
          <el-switch
            v-model="configForm.autoAuditEnabled"
            active-text="启用"
            inactive-text="禁用"
          />
          <div class="form-tip">关闭后需手动执行夜审</div>
        </el-form-item>
        
        <!-- 步骤配置 -->
        <el-divider content-position="left">步骤配置</el-divider>
        
        <el-form-item label="启用步骤">
          <div class="steps-container">
            <el-checkbox
              v-for="step in availableSteps"
              :key="step.stepName"
              v-model="step.enabled"
              :label="step.stepName"
              class="step-checkbox"
            >
              <div class="step-info">
                <span class="step-name">{{ step.displayName }}</span>
                <span class="step-desc">{{ step.description }}</span>
              </div>
            </el-checkbox>
          </div>
        </el-form-item>
        
        <!-- 通知配置 -->
        <el-divider content-position="left">通知配置</el-divider>
        
        <el-form-item label="启用通知">
          <el-switch
            v-model="configForm.notification.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        
        <template v-if="configForm.notification.enabled">
          <el-form-item label="完成时通知">
            <el-switch
              v-model="configForm.notification.notifyOnComplete"
              active-text="是"
              inactive-text="否"
            />
          </el-form-item>
          
          <el-form-item label="失败时通知">
            <el-switch
              v-model="configForm.notification.notifyOnFailure"
              active-text="是"
              inactive-text="否"
            />
          </el-form-item>
          
          <el-form-item label="通知邮箱">
            <div class="email-container">
              <div v-for="(email, index) in configForm.notification.emailRecipients" :key="index" class="email-item">
                <el-input v-model="configForm.notification.emailRecipients[index]" placeholder="输入邮箱地址" />
                <el-button type="danger" @click="removeEmail(index)" :icon="Delete" circle />
              </div>
              <el-button type="primary" @click="addEmail" plain>
                添加邮箱
              </el-button>
            </div>
          </el-form-item>
        </template>
      </el-form>
      
      <!-- 配置预览 -->
      <el-divider content-position="left">配置预览</el-divider>
      
      <el-descriptions :column="2" border class="config-preview">
        <el-descriptions-item label="夜审时间">{{ configForm.auditTime || '04:00' }}</el-descriptions-item>
        <el-descriptions-item label="自动夜审">{{ configForm.autoAuditEnabled ? '启用' : '禁用' }}</el-descriptions-item>
        <el-descriptions-item label="启用步骤">{{ enabledStepsCount }} / {{ availableSteps.length }}</el-descriptions-item>
        <el-descriptions-item label="通知状态">{{ configForm.notification.enabled ? '启用' : '禁用' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getNightAuditConfig, updateNightAuditConfig } from '@/api/night-audit-config'
import request from '@/utils/request'

const saving = ref(false)
const hotelId = ref(1) // 默认酒店ID，实际应从用户登录信息获取

const configForm = reactive({
  auditTime: '04:00:00',
  autoAuditEnabled: true,
  enabledSteps: [],
  notification: {
    enabled: false,
    notifyOnComplete: true,
    notifyOnFailure: true,
    emailRecipients: []
  }
})

const availableSteps = ref([])

// 计算启用的步骤数量
const enabledStepsCount = computed(() => {
  return availableSteps.value.filter(step => step.enabled).length
})

// 加载配置
const loadConfig = async () => {
  try {
    const result = await getNightAuditConfig(hotelId.value)
    if (result.data) {
      const config = result.data
      
      configForm.auditTime = config.auditTime || '04:00:00'
      configForm.autoAuditEnabled = config.autoAuditEnabled !== false
      configForm.enabledSteps = config.enabledSteps || []
      
      if (config.notification) {
        configForm.notification.enabled = config.notification.enabled || false
        configForm.notification.notifyOnComplete = config.notification.notifyOnComplete !== false
        configForm.notification.notifyOnFailure = config.notification.notifyOnFailure !== false
        configForm.notification.emailRecipients = config.notification.emailRecipients || []
      }
      
      // 设置可用步骤
      if (config.availableSteps) {
        availableSteps.value = config.availableSteps
      }
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败')
  }
}

// 保存配置
const saveConfig = async () => {
  saving.value = true
  try {
    const enabledSteps = availableSteps.value
      .filter(step => step.enabled)
      .map(step => step.stepName)
    
    const data = {
      auditTime: configForm.auditTime,
      autoAuditEnabled: configForm.autoAuditEnabled,
      enabledSteps: enabledSteps,
      notification: configForm.notification
    }
    
    await updateNightAuditConfig(hotelId.value, data)
    ElMessage.success('配置保存成功')
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

// 添加邮箱
const addEmail = () => {
  configForm.notification.emailRecipients.push('')
}

// 删除邮箱
const removeEmail = (index) => {
  configForm.notification.emailRecipients.splice(index, 1)
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.night-audit-config-container {
  padding: 20px;
}

.config-card {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-form {
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.steps-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.step-checkbox {
  height: auto !important;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin: 0;
}

.step-info {
  display: flex;
  flex-direction: column;
}

.step-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.step-desc {
  font-size: 12px;
  color: #909399;
}

.email-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.email-item {
  display: flex;
  gap: 10px;
  align-items: center;
}

.config-preview {
  margin-top: 20px;
}
</style>