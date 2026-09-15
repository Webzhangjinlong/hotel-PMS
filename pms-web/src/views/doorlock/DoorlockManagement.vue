<template>
  <div class="doorlock-management">
    <div class="page-header">
      <h2>门锁/房卡管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增设备
        </el-button>
        <el-button type="success" @click="handleIssueCard">
          <el-icon><CreditCard /></el-icon>
          发卡
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="设备名称">
          <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="设备型号">
          <el-input v-model="queryParams.deviceModel" placeholder="请输入设备型号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="queryParams.deviceStatus" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="在线" value="ONLINE" />
            <el-option label="故障" value="ERROR" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 设备列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="doorlockList" v-loading="loading" stripe>
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceModel" label="设备型号" width="120" />
        <el-table-column prop="deviceIp" label="设备IP" width="150" />
        <el-table-column prop="devicePort" label="设备端口" width="100" />
        <el-table-column prop="deviceStatusName" label="设备状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getDeviceStatusType(row.deviceStatus)">
              {{ row.deviceStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="配置状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleIssueCardWithDevice(row)">发卡</el-button>
            <el-button type="info" link size="small" @click="handleReadCard(row)">读卡</el-button>
            <el-button type="warning" link size="small" @click="handleCheckStatus(row)">检查状态</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="doorlockForm" :rules="doorlockRules" ref="doorlockFormRef" label-width="100px">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="doorlockForm.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备型号">
          <el-input v-model="doorlockForm.deviceModel" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="设备IP">
          <el-input v-model="doorlockForm.deviceIp" placeholder="请输入设备IP地址" />
        </el-form-item>
        <el-form-item label="设备端口">
          <el-input v-model="doorlockForm.devicePort" placeholder="请输入设备端口" />
        </el-form-item>
        <el-form-item label="配置状态">
          <el-select v-model="doorlockForm.status" placeholder="请选择配置状态" style="width: 100%;">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="doorlockForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">确认</el-button>
      </template>
    </el-dialog>

    <!-- 发卡对话框 -->
    <el-dialog title="发卡" v-model="issueCardVisible" width="600px">
      <el-form :model="issueCardForm" :rules="issueCardRules" ref="issueCardFormRef" label-width="100px">
        <el-form-item label="选择设备" prop="deviceId">
          <el-select v-model="issueCardForm.deviceId" placeholder="请选择设备" style="width: 100%;">
            <el-option 
              v-for="device in activeDevices" 
              :key="device.id" 
              :label="device.deviceName" 
              :value="device.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" prop="roomId">
          <el-input v-model="issueCardForm.roomId" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="客人姓名" prop="guestName">
          <el-input v-model="issueCardForm.guestName" placeholder="请输入客人姓名" />
        </el-form-item>
        <el-form-item label="有效期开始" prop="validStart">
          <el-date-picker v-model="issueCardForm.validStart" type="datetime" placeholder="选择有效期开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="有效期结束" prop="validEnd">
          <el-date-picker v-model="issueCardForm.validEnd" type="datetime" placeholder="选择有效期结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueCardVisible = false">取消</el-button>
        <el-button type="primary" @click="handleIssueCardSubmit" :loading="saving">发卡</el-button>
      </template>
    </el-dialog>

    <!-- 读卡结果对话框 -->
    <el-dialog title="读卡结果" v-model="readCardVisible" width="500px">
      <div v-if="readCardResult" class="read-card-result">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="卡号">{{ readCardResult.cardNo }}</el-descriptions-item>
          <el-descriptions-item label="卡类型">{{ readCardResult.cardType }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ readCardResult.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="有效期开始">{{ readCardResult.validStart }}</el-descriptions-item>
          <el-descriptions-item label="有效期结束">{{ readCardResult.validEnd }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="readCardVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 设备状态对话框 -->
    <el-dialog title="设备状态检查" v-model="statusDialogVisible" width="400px">
      <div v-if="deviceStatus" class="device-status">
        <el-result :icon="deviceStatus.status === 'ONLINE' ? 'success' : 'warning'" :title="deviceStatus.statusName">
          <template #sub-title>
            <p>{{ deviceStatus.message }}</p>
          </template>
        </el-result>
      </div>
      <template #footer>
        <el-button @click="statusDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, CreditCard } from '@element-plus/icons-vue'
import { 
  getDoorLockList, getDoorLockById, createDoorLock, 
  updateDoorLock, deleteDoorLock, getActiveDoorLocks,
  issueCard, readCard, checkDeviceStatus 
} from '@/api/doorlock'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  deviceName: '',
  deviceModel: '',
  deviceStatus: '',
  status: ''
})

// 列表数据
const doorlockList = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')
const doorlockFormRef = ref(null)
const doorlockForm = reactive({
  id: null,
  deviceName: '',
  deviceModel: '',
  deviceIp: '',
  devicePort: '',
  status: 'ACTIVE',
  remark: ''
})
const doorlockRules = {
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

// 发卡相关
const issueCardVisible = ref(false)
const issueCardFormRef = ref(null)
const issueCardForm = reactive({
  deviceId: null,
  roomId: '',
  guestName: '',
  validStart: '',
  validEnd: ''
})
const issueCardRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  roomId: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  guestName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  validStart: [{ required: true, message: '请选择有效期开始时间', trigger: 'change' }],
  validEnd: [{ required: true, message: '请选择有效期结束时间', trigger: 'change' }]
}
const activeDevices = ref([])

// 读卡相关
const readCardVisible = ref(false)
const readCardResult = ref(null)

// 设备状态相关
const statusDialogVisible = ref(false)
const deviceStatus = ref(null)

// 保存状态
const saving = ref(false)

/**
 * 加载设备列表
 */
const loadDoorLockList = async () => {
  loading.value = true
  try {
    const response = await getDoorLockList(queryParams)
    if (response.code === 200) {
      doorlockList.value = response.data.list
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询设备列表失败:', error)
    ElMessage.error('查询设备列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载启用设备列表
 */
const loadActiveDevices = async () => {
  try {
    const response = await getActiveDoorLocks(1) // 默认酒店ID
    if (response.code === 200) {
      activeDevices.value = response.data
    }
  } catch (error) {
    console.error('获取启用设备失败:', error)
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  queryParams.page = 1
  loadDoorLockList()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  queryParams.deviceName = ''
  queryParams.deviceModel = ''
  queryParams.deviceStatus = ''
  queryParams.status = ''
  handleSearch()
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
  queryParams.size = size
  loadDoorLockList()
}

/**
 * 当前页变化
 */
const handleCurrentChange = (page) => {
  queryParams.page = page
  loadDoorLockList()
}

/**
 * 新增设备
 */
const handleAdd = () => {
  dialogTitle.value = '新增设备'
  resetDoorlockForm()
  dialogVisible.value = true
}

/**
 * 编辑设备
 */
const handleEdit = async (row) => {
  dialogTitle.value = '编辑设备'
  try {
    const response = await getDoorLockById(row.id)
    if (response.code === 200) {
      Object.assign(doorlockForm, response.data)
      dialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取设备详情失败')
    }
  } catch (error) {
    console.error('获取设备详情失败:', error)
    ElMessage.error('获取设备详情失败')
  }
}

/**
 * 发卡
 */
const handleIssueCard = () => {
  issueCardForm.deviceId = null
  issueCardForm.roomId = ''
  issueCardForm.guestName = ''
  issueCardForm.validStart = ''
  issueCardForm.validEnd = ''
  issueCardVisible.value = true
  loadActiveDevices()
}

/**
 * 使用指定设备发卡
 */
const handleIssueCardWithDevice = (row) => {
  issueCardForm.deviceId = row.id
  issueCardForm.roomId = ''
  issueCardForm.guestName = ''
  issueCardForm.validStart = ''
  issueCardForm.validEnd = ''
  issueCardVisible.value = true
  loadActiveDevices()
}

/**
 * 读卡
 */
const handleReadCard = async (row) => {
  try {
    const response = await readCard(row.id)
    if (response.code === 200) {
      readCardResult.value = response.data
      readCardVisible.value = true
    } else {
      ElMessage.error(response.message || '读卡失败')
    }
  } catch (error) {
    console.error('读卡失败:', error)
    ElMessage.error('读卡失败')
  }
}

/**
 * 检查设备状态
 */
const handleCheckStatus = async (row) => {
  try {
    const response = await checkDeviceStatus(row.id)
    if (response.code === 200) {
      deviceStatus.value = response.data
      statusDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '检查设备状态失败')
    }
  } catch (error) {
    console.error('检查设备状态失败:', error)
    ElMessage.error('检查设备状态失败')
  }
}

/**
 * 删除设备
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该设备吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteDoorLock(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadDoorLockList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除设备失败:', error)
      ElMessage.error('删除设备失败')
    }
  }
}

/**
 * 提交设备表单
 */
const handleSubmit = async () => {
  const valid = await doorlockFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    let response
    if (doorlockForm.id) {
      response = await updateDoorLock(doorlockForm.id, doorlockForm)
    } else {
      response = await createDoorLock(doorlockForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(doorlockForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadDoorLockList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

/**
 * 提交发卡表单
 */
const handleIssueCardSubmit = async () => {
  const valid = await issueCardFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    const response = await issueCard(issueCardForm.deviceId, issueCardForm)
    if (response.code === 200) {
      ElMessage.success('发卡成功')
      issueCardVisible.value = false
    } else {
      ElMessage.error(response.message || '发卡失败')
    }
  } catch (error) {
    console.error('发卡失败:', error)
    ElMessage.error('发卡失败')
  } finally {
    saving.value = false
  }
}

/**
 * 重置设备表单
 */
const resetDoorlockForm = () => {
  doorlockForm.id = null
  doorlockForm.deviceName = ''
  doorlockForm.deviceModel = ''
  doorlockForm.deviceIp = ''
  doorlockForm.devicePort = ''
  doorlockForm.status = 'ACTIVE'
  doorlockForm.remark = ''
}

/**
 * 获取设备状态类型
 */
const getDeviceStatusType = (status) => {
  const map = { 'OFFLINE': 'info', 'ONLINE': 'success', 'ERROR': 'danger' }
  return map[status] || 'info'
}

// 初始化加载数据
onMounted(() => {
  loadDoorLockList()
})
</script>

<style scoped lang="scss">
.doorlock-management {
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

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.read-card-result {
  padding: 20px;
}

.device-status {
  text-align: center;
  padding: 20px;
}
</style>
