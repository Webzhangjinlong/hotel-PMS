<template>
  <div class="hotel-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="酒店名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入酒店名称" 
            clearable 
            style="width: 200px;"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="queryParams.status" 
            placeholder="全部状态" 
            clearable
            style="width: 140px;"
          >
            <el-option label="全部状态" :value="null" />
            <el-option label="启用" value="ACTIVE">
              <el-tag type="success" size="small" effect="plain">启用</el-tag>
            </el-option>
            <el-option label="停用" value="INACTIVE">
              <el-tag type="danger" size="small" effect="plain">停用</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>酒店列表</span>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增酒店
          </el-button>
        </div>
      </template>

      <el-table :data="hotelList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="hotelCode" label="酒店标识码" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="primary" effect="plain">{{ row.hotelCode || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="酒店名称" min-width="150" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="timezone" label="时区" width="120" />
        <el-table-column prop="auditTime" label="夜审时间" width="100" align="center">
          <template #default="{ row }">
            {{ row.auditTime || '04:00' }}
          </template>
        </el-table-column>
        <el-table-column prop="autoAuditEnabled" label="自动夜审" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.autoAuditEnabled ? 'success' : 'info'" effect="plain">
              {{ row.autoAuditEnabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" effect="plain">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleNightAuditConfig(row)">夜审配置</el-button>
            <el-button type="primary" link size="small" @click="handleStatus(row)">
              {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="酒店标识码" prop="hotelCode" v-if="!isEdit">
          <el-input v-model="formData.hotelCode" placeholder="请输入酒店标识码（如：H001）" />
          <div class="form-tip">标识码用于登录时区分不同酒店，创建后不可修改</div>
        </el-form-item>
        <el-form-item label="酒店标识码" v-else>
          <el-input :model-value="formData.hotelCode" disabled />
        </el-form-item>
        <el-form-item label="酒店名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入酒店名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="时区" prop="timezone">
          <el-select v-model="formData.timezone" placeholder="请选择时区" style="width: 100%;">
            <el-option label="Asia/Shanghai" value="Asia/Shanghai" />
            <el-option label="Asia/Hong_Kong" value="Asia/Hong_Kong" />
            <el-option label="America/New_York" value="America/New_York" />
            <el-option label="Europe/London" value="Europe/London" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 夜审配置对话框 -->
    <el-dialog v-model="nightAuditDialogVisible" title="夜审配置" width="450px" destroy-on-close>
      <el-form ref="nightAuditFormRef" :model="nightAuditFormData" label-width="120px">
        <el-form-item label="夜审时间">
          <el-time-picker
            v-model="nightAuditFormData.auditTime"
            placeholder="选择夜审时间"
            format="HH:mm"
            value-format="HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="自动夜审">
          <el-switch
            v-model="nightAuditFormData.autoAuditEnabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-alert
          title="说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>1. 夜审时间设置后，系统将在每天该时间自动执行夜审</p>
            <p>2. 关闭自动夜审后，需手动执行夜审操作</p>
            <p>3. 夜审时间修改后，次日生效</p>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="nightAuditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="nightAuditSubmitLoading" @click="handleNightAuditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 查询相关
const loading = ref(false)
const hotelList = ref([])
const total = ref(0)
const queryParams = reactive({
  name: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({
    hotelCode: '',
  id: null,
  name: '',
  address: '',
  phone: '',
  timezone: 'Asia/Shanghai'
})
const formRules = {
    hotelCode: [
        { required: true, message: '请输入酒店标识码', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' },
        { pattern: /^[A-Za-z0-9_-]+$/, message: '只能包含字母、数字、下划线和横线', trigger: 'blur' }
    ],
  name: [
    { required: true, message: '请输入酒店名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ],
  address: [
    { max: 500, message: '长度不能超过500个字符', trigger: 'blur' }
  ],
  phone: [
    { max: 20, message: '长度不能超过20个字符', trigger: 'blur' }
  ]
}

// 夜审配置相关
const nightAuditDialogVisible = ref(false)
const nightAuditFormRef = ref(null)
const nightAuditSubmitLoading = ref(false)
const currentHotelId = ref(null)
const nightAuditFormData = reactive({
  auditTime: '04:00:00',
  autoAuditEnabled: true
})

// 查询酒店列表
async function handleQuery() {
  loading.value = true
  try {
    const res = await request.get('/v1/hotels', { params: queryParams })
    hotelList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('查询酒店列表失败', error)
  } finally {
    loading.value = false
  }
}

// 重置查询
function resetQuery() {
  queryParams.name = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  handleQuery()
}

// 新增酒店
function handleAdd() {
  dialogTitle.value = '新增酒店'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑酒店
function handleEdit(row) {
  dialogTitle.value = '编辑酒店'
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 夜审配置
function handleNightAuditConfig(row) {
  currentHotelId.value = row.id
  nightAuditFormData.auditTime = row.auditTime || '04:00:00'
  nightAuditFormData.autoAuditEnabled = row.autoAuditEnabled !== false
  nightAuditDialogVisible.value = true
}

// 提交夜审配置
async function handleNightAuditSubmit() {
  nightAuditSubmitLoading.value = true
  try {
    await request.put('/v1/hotels/' + currentHotelId.value + '/night-audit-config', nightAuditFormData)
    ElMessage.success('夜审配置更新成功')
    nightAuditDialogVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('更新夜审配置失败', error)
  } finally {
    nightAuditSubmitLoading.value = false
  }
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await request.put('/v1/hotels', formData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/v1/hotels', formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    submitLoading.value = false
  }
}

// 修改状态
async function handleStatus(row) {
  const statusText = row.status === 'ACTIVE' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm('确定要' + statusText + '该酒店吗？', '提示', { type: 'warning' })
    await request.put('/v1/hotels', {
      id: row.id,
      status: row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    })
    ElMessage.success(statusText + '成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改状态失败', error)
    }
  }
}

// 删除酒店
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该酒店吗？删除后不可恢复。', '警告', { type: 'error' })
    await request.delete('/v1/hotels/' + row.id)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

// 重置表单
function resetForm() {
  formData.id = null
    formData.hotelCode = ''
    formData.name = ''
  formData.address = ''
  formData.phone = ''
  formData.timezone = 'Asia/Shanghai'
}

// 生命周期
onMounted(() => {
  handleQuery()
})
</script>

<style lang="scss" scoped>
.hotel-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.search-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 20px 20px 0;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
  }
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
