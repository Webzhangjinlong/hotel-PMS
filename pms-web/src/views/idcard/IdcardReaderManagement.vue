<template>
  <div class="idcard-reader-management">
    <div class="page-header">
      <h2>身份证阅读器管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增设备
        </el-button>
        <el-button type="success" @click="handleSimulateRead">
          <el-icon><Reading /></el-icon>
          模拟读取
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
      <el-table :data="readerList" v-loading="loading" stripe>
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceModel" label="设备型号" width="120" />
        <el-table-column prop="devicePort" label="设备端口" width="120" />
        <el-table-column prop="deviceIp" label="设备IP" width="150" />
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
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleRead(row)">读取</el-button>
            <el-button type="info" link size="small" @click="handleCheckStatus(row)">检查状态</el-button>
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
      <el-form :model="readerForm" :rules="readerRules" ref="readerFormRef" label-width="100px">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="readerForm.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备型号">
          <el-input v-model="readerForm.deviceModel" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="设备端口">
          <el-input v-model="readerForm.devicePort" placeholder="请输入设备端口" />
        </el-form-item>
        <el-form-item label="设备IP">
          <el-input v-model="readerForm.deviceIp" placeholder="请输入设备IP地址" />
        </el-form-item>
        <el-form-item label="配置状态">
          <el-select v-model="readerForm.status" placeholder="请选择配置状态" style="width: 100%;">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="readerForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">确认</el-button>
      </template>
    </el-dialog>

    <!-- 读取结果对话框 -->
    <el-dialog title="身份证读取结果" v-model="readResultVisible" width="600px">
      <div v-if="readResult" class="read-result">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ readResult.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ readResult.gender }}</el-descriptions-item>
          <el-descriptions-item label="民族">{{ readResult.nation }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ readResult.birthDate }}</el-descriptions-item>
          <el-descriptions-item label="证件号码" :span="2">{{ readResult.cardNo }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ readResult.address }}</el-descriptions-item>
          <el-descriptions-item label="签发机关" :span="2">{{ readResult.issueOrg }}</el-descriptions-item>
          <el-descriptions-item label="有效期开始">{{ readResult.validStart }}</el-descriptions-item>
          <el-descriptions-item label="有效期结束">{{ readResult.validEnd }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="readResultVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleUseIdcard">使用此信息</el-button>
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
import { Plus, Reading } from '@element-plus/icons-vue'
import { 
  getReaderList, getReaderById, createReader, 
  updateReader, deleteReader, readIdcard, 
  simulateRead, checkDeviceStatus 
} from '@/api/idcard'

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
const readerList = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')
const readerFormRef = ref(null)
const readerForm = reactive({
  id: null,
  deviceName: '',
  deviceModel: '',
  devicePort: '',
  deviceIp: '',
  status: 'ACTIVE',
  remark: ''
})
const readerRules = {
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

// 读取结果相关
const readResultVisible = ref(false)
const readResult = ref(null)

// 设备状态相关
const statusDialogVisible = ref(false)
const deviceStatus = ref(null)

// 保存状态
const saving = ref(false)

/**
 * 加载设备列表
 */
const loadReaderList = async () => {
  loading.value = true
  try {
    const response = await getReaderList(queryParams)
    if (response.code === 200) {
      readerList.value = response.data.list
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
 * 搜索
 */
const handleSearch = () => {
  queryParams.page = 1
  loadReaderList()
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
  loadReaderList()
}

/**
 * 当前页变化
 */
const handleCurrentChange = (page) => {
  queryParams.page = page
  loadReaderList()
}

/**
 * 新增设备
 */
const handleAdd = () => {
  dialogTitle.value = '新增设备'
  resetReaderForm()
  dialogVisible.value = true
}

/**
 * 编辑设备
 */
const handleEdit = async (row) => {
  dialogTitle.value = '编辑设备'
  try {
    const response = await getReaderById(row.id)
    if (response.code === 200) {
      Object.assign(readerForm, response.data)
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
 * 读取身份证
 */
const handleRead = async (row) => {
  try {
    const response = await readIdcard(row.id)
    if (response.code === 200) {
      readResult.value = response.data.data
      readResultVisible.value = true
    } else {
      ElMessage.error(response.message || '读取失败')
    }
  } catch (error) {
    console.error('读取身份证失败:', error)
    ElMessage.error('读取身份证失败')
  }
}

/**
 * 模拟读取
 */
const handleSimulateRead = async () => {
  try {
    const response = await simulateRead()
    if (response.code === 200) {
      readResult.value = response.data.data
      readResultVisible.value = true
    } else {
      ElMessage.error(response.message || '模拟读取失败')
    }
  } catch (error) {
    console.error('模拟读取失败:', error)
    ElMessage.error('模拟读取失败')
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
    
    const response = await deleteReader(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadReaderList()
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
 * 使用身份证信息
 */
const handleUseIdcard = () => {
  ElMessage.success('身份证信息已复制')
  readResultVisible.value = false
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  const valid = await readerFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    let response
    if (readerForm.id) {
      response = await updateReader(readerForm.id, readerForm)
    } else {
      response = await createReader(readerForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(readerForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadReaderList()
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
 * 重置表单
 */
const resetReaderForm = () => {
  readerForm.id = null
  readerForm.deviceName = ''
  readerForm.deviceModel = ''
  readerForm.devicePort = ''
  readerForm.deviceIp = ''
  readerForm.status = 'ACTIVE'
  readerForm.remark = ''
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
  loadReaderList()
})
</script>

<style scoped lang="scss">
.idcard-reader-management {
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

.read-result {
  padding: 20px;
}

.device-status {
  text-align: center;
  padding: 20px;
}
</style>
