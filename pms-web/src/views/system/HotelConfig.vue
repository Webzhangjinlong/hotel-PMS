<template>
  <div class="hotel-config-container">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>超时离店配置</span>
          <el-button type="primary" @click="saveConfigs" :loading="saving">
            保存配置
          </el-button>
        </div>
      </template>
      
      <el-form :model="configForm" label-width="150px" class="config-form">
        <el-form-item label="标准离店时间">
          <el-time-picker
            v-model="configForm.checkoutTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择标准离店时间"
          />
          <div class="form-tip">客人在此时间前离店不收取额外费用</div>
        </el-form-item>
        
        <el-form-item label="半日租截止时间">
          <el-time-picker
            v-model="configForm.halfDayDeadline"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择半日租截止时间"
          />
          <div class="form-tip">超过标准离店时间但在截止时间前离店，收取半日租</div>
        </el-form-item>
        
        <el-form-item label="半日租费率">
          <el-input-number
            v-model="configForm.halfDayRate"
            :min="0"
            :max="1"
            :step="0.1"
            :precision="2"
          />
          <span class="rate-unit">倍</span>
          <div class="form-tip">半日租 = 当日房价 × 此费率（默认 0.5 即 50%）</div>
        </el-form-item>
        
        <el-form-item label="全日租费率">
          <el-input-number
            v-model="configForm.fullDayRate"
            :min="0"
            :max="2"
            :step="0.1"
            :precision="2"
          />
          <span class="rate-unit">倍</span>
          <div class="form-tip">全日租 = 当日房价 × 此费率（默认 1.0 即 100%）</div>
        </el-form-item>
      </el-form>
      
      <el-divider />
      
      <div class="config-preview">
        <h4>收费规则预览</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="免费离店">
            {{ configForm.checkoutTime || '12:00' }} 之前离店
          </el-descriptions-item>
          <el-descriptions-item label="半日租收取">
            {{ configForm.checkoutTime || '12:00' }} - {{ configForm.halfDayDeadline || '18:00' }} 离店，收取当日房价 × {{ configForm.halfDayRate || 0.5 }}
          </el-descriptions-item>
          <el-descriptions-item label="全日租收取">
            {{ configForm.halfDayDeadline || '18:00' }} 之后离店，收取当日房价 × {{ configForm.fullDayRate || 1.0 }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>

import { useUserStore } from '@/stores/user'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotelConfigs, updateHotelConfigs } from '@/api/hotel-config'
const userStore = useUserStore()

const saving = ref(false)
const hotelId = userStore.hotelId // 默认酒店ID，实际应从用户登录信息获取

const configForm = reactive({
  checkoutTime: '12:00',
  halfDayDeadline: '18:00',
  halfDayRate: 0.5,
  fullDayRate: 1.0
})

// 加载配置
const loadConfigs = async () => {
  try {
    const result = await getHotelConfigs(hotelId)
    if (result.data) {
      const configs = result.data
      configs.forEach(config => {
        switch (config.configKey) {
          case 'CHECKOUT_TIME':
            configForm.checkoutTime = config.configValue
            break
          case 'LATE_CHECKOUT_HALF_DAY_TIME':
            configForm.halfDayDeadline = config.configValue
            break
          case 'LATE_CHECKOUT_HALF_DAY_RATE':
            configForm.halfDayRate = parseFloat(config.configValue)
            break
          case 'LATE_CHECKOUT_FULL_DAY_RATE':
            configForm.fullDayRate = parseFloat(config.configValue)
            break
        }
      })
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败')
  }
}

// 保存配置
const saveConfigs = async () => {
  saving.value = true
  try {
    const configs = {
      CHECKOUT_TIME: configForm.checkoutTime,
      LATE_CHECKOUT_HALF_DAY_TIME: configForm.halfDayDeadline,
      LATE_CHECKOUT_HALF_DAY_RATE: configForm.halfDayRate.toString(),
      LATE_CHECKOUT_FULL_DAY_RATE: configForm.fullDayRate.toString()
    }
    
    await updateHotelConfigs(hotelId, { configs })
    ElMessage.success('配置保存成功')
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.hotel-config-container {
  padding: 20px;
}

.config-card {
  max-width: 800px;
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

.rate-unit {
  margin-left: 10px;
  color: #606266;
}

.config-preview {
  margin-top: 20px;
}

.config-preview h4 {
  margin-bottom: 15px;
  color: #303133;
}
</style>
