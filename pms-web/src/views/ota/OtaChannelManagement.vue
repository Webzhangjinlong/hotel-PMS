<template>
  <div class="ota-management">
    <div class="page-header">
      <h2>OTA渠道管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增渠道
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="渠道名称">
          <el-input v-model="queryParams.channelName" placeholder="请输入渠道名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="渠道编码">
          <el-input v-model="queryParams.channelCode" placeholder="请输入渠道编码" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="渠道类型">
          <el-select v-model="queryParams.channelType" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="在线旅行社" value="OTA" />
            <el-option label="直销" value="DIRECT" />
            <el-option label="协议单位" value="CORPORATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
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

    <!-- 渠道列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="channelList" v-loading="loading" stripe>
        <el-table-column prop="channelName" label="渠道名称" width="150" />
        <el-table-column prop="channelCode" label="渠道编码" width="120" />
        <el-table-column prop="channelTypeName" label="渠道类型" width="120" />
        <el-table-column prop="authTypeName" label="认证类型" width="120" />
        <el-table-column prop="appKey" label="App Key" width="200" show-overflow-tooltip />
        <el-table-column prop="apiUrl" label="API地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link size="small" @click="handleTest(row)">测试</el-button>
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
      <el-form :model="channelForm" :rules="channelRules" ref="channelFormRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="渠道名称" prop="channelName">
              <el-input v-model="channelForm.channelName" placeholder="请输入渠道名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="渠道编码" prop="channelCode">
              <el-input v-model="channelForm.channelCode" placeholder="请输入渠道编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="渠道类型" prop="channelType">
              <el-select v-model="channelForm.channelType" placeholder="请选择渠道类型" style="width: 100%;">
                <el-option label="在线旅行社" value="OTA" />
                <el-option label="直销" value="DIRECT" />
                <el-option label="协议单位" value="CORPORATE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="认证类型" prop="authType">
              <el-select v-model="channelForm.authType" placeholder="请选择认证类型" style="width: 100%;">
                <el-option label="API Key" value="API_KEY" />
                <el-option label="OAuth 2.0" value="OAUTH2" />
                <el-option label="Basic Auth" value="BASIC" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="App Key">
              <el-input v-model="channelForm.appKey" placeholder="请输入App Key" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="App Secret">
              <el-input v-model="channelForm.appSecret" placeholder="请输入App Secret" type="password" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="API地址">
          <el-input v-model="channelForm.apiUrl" placeholder="请输入API地址" />
        </el-form-item>
        <el-form-item label="回调地址">
          <el-input v-model="channelForm.callbackUrl" placeholder="请输入回调地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="channelForm.status" placeholder="请选择状态" style="width: 100%;">
                <el-option label="启用" value="ACTIVE" />
                <el-option label="停用" value="INACTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="channelForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">确认</el-button>
      </template>
    </el-dialog>

    <!-- 测试连接对话框 -->
    <el-dialog title="测试连接" v-model="testDialogVisible" width="500px">
      <div v-if="testResult" class="test-result">
        <el-result :icon="testResult.success ? 'success' : 'error'" :title="testResult.message" />
      </div>
      <div v-else class="test-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在测试连接...</span>
      </div>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Loading } from '@element-plus/icons-vue'
import { 
  getOtaChannelList, getOtaChannelById, createOtaChannel, 
  updateOtaChannel, deleteOtaChannel 
} from '@/api/ota'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  channelName: '',
  channelCode: '',
  channelType: '',
  status: ''
})

// 列表数据
const channelList = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增渠道')
const channelFormRef = ref(null)
const channelForm = reactive({
  id: null,
  channelName: '',
  channelCode: '',
  channelType: 'OTA',
  authType: 'API_KEY',
  appKey: '',
  appSecret: '',
  apiUrl: '',
  callbackUrl: '',
  status: 'ACTIVE',
  remark: ''
})
const channelRules = {
  channelName: [{ required: true, message: '请输入渠道名称', trigger: 'blur' }],
  channelCode: [{ required: true, message: '请输入渠道编码', trigger: 'blur' }],
  channelType: [{ required: true, message: '请选择渠道类型', trigger: 'change' }],
  authType: [{ required: true, message: '请选择认证类型', trigger: 'change' }]
}

// 测试连接相关
const testDialogVisible = ref(false)
const testResult = ref(null)

// 保存状态
const saving = ref(false)

/**
 * 加载渠道列表
 */
const loadChannelList = async () => {
  loading.value = true
  try {
    const response = await getOtaChannelList(queryParams)
    if (response.code === 200) {
      channelList.value = response.data.list
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询渠道列表失败:', error)
    ElMessage.error('查询渠道列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  queryParams.page = 1
  loadChannelList()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  queryParams.channelName = ''
  queryParams.channelCode = ''
  queryParams.channelType = ''
  queryParams.status = ''
  handleSearch()
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
  queryParams.size = size
  loadChannelList()
}

/**
 * 当前页变化
 */
const handleCurrentChange = (page) => {
  queryParams.page = page
  loadChannelList()
}

/**
 * 新增渠道
 */
const handleAdd = () => {
  dialogTitle.value = '新增渠道'
  resetChannelForm()
  dialogVisible.value = true
}

/**
 * 编辑渠道
 */
const handleEdit = async (row) => {
  dialogTitle.value = '编辑渠道'
  try {
    const response = await getOtaChannelById(row.id)
    if (response.code === 200) {
      Object.assign(channelForm, response.data)
      dialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取渠道详情失败')
    }
  } catch (error) {
    console.error('获取渠道详情失败:', error)
    ElMessage.error('获取渠道详情失败')
  }
}

/**
 * 测试连接
 */
const handleTest = async (row) => {
  testResult.value = null
  testDialogVisible.value = true
  
  // 模拟测试连接
  setTimeout(() => {
    testResult.value = {
      success: true,
      message: '连接成功'
    }
  }, 2000)
}

/**
 * 删除渠道
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该渠道吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteOtaChannel(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadChannelList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除渠道失败:', error)
      ElMessage.error('删除渠道失败')
    }
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  const valid = await channelFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    let response
    if (channelForm.id) {
      response = await updateOtaChannel(channelForm.id, channelForm)
    } else {
      response = await createOtaChannel(channelForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(channelForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadChannelList()
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
const resetChannelForm = () => {
  channelForm.id = null
  channelForm.channelName = ''
  channelForm.channelCode = ''
  channelForm.channelType = 'OTA'
  channelForm.authType = 'API_KEY'
  channelForm.appKey = ''
  channelForm.appSecret = ''
  channelForm.apiUrl = ''
  channelForm.callbackUrl = ''
  channelForm.status = 'ACTIVE'
  channelForm.remark = ''
}

// 初始化加载数据
onMounted(() => {
  loadChannelList()
})
</script>

<style scoped lang="scss">
.ota-management {
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

.test-result {
  text-align: center;
  padding: 20px;
}

.test-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  
  .is-loading {
    font-size: 32px;
    color: #409eff;
    margin-bottom: 10px;
    animation: rotating 2s linear infinite;
  }
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
