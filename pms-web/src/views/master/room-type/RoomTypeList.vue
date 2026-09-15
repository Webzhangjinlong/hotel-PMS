<template>
  <div class="room-type-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>房型管理</h2>
      <el-button type="success" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增房型
      </el-button>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="房型名称">
          <el-input v-model="queryParams.name" placeholder="请输入房型名称" clearable />
        </el-form-item>
        <el-form-item label="房型编码">
          <el-input v-model="queryParams.code" placeholder="请输入房型编码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
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
    
    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="房型编码" width="120" />
        <el-table-column prop="name" label="房型名称" />
        <el-table-column prop="bedType" label="床型" width="100" />
        <el-table-column prop="maxGuests" label="最大入住人数" width="120" />
        <el-table-column prop="basePrice" label="基础价格" width="100">
          <template #default="{ row }">
            ¥{{ row.basePrice }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleStatus(row)">
              {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
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
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="房型编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入房型编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="房型名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入房型名称" />
        </el-form-item>
        <el-form-item label="床型" prop="bedType">
          <el-select v-model="formData.bedType" placeholder="请选择床型">
            <el-option label="单人床" value="单人床" />
            <el-option label="双人床" value="双人床" />
            <el-option label="大床" value="大床" />
            <el-option label="双床" value="双床" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大入住人数" prop="maxGuests">
          <el-input-number v-model="formData.maxGuests" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="基础价格" prop="basePrice">
          <el-input-number v-model="formData.basePrice" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

// ========== 查询参数 ==========
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  code: '',
  status: ''
})

// ========== 表格数据 ==========
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// ========== 弹窗相关 ==========
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

// ========== 表单数据 ==========
const formData = reactive({
  code: '',
  name: '',
  bedType: '',
  maxGuests: 2,
  basePrice: 0,
  description: ''
})

// ========== 表单验证规则 ==========
const formRules = {
  code: [
    { required: true, message: '请输入房型编码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入房型名称', trigger: 'blur' }
  ],
  bedType: [
    { required: true, message: '请选择床型', trigger: 'change' }
  ],
  maxGuests: [
    { required: true, message: '请输入最大入住人数', trigger: 'blur' }
  ],
  basePrice: [
    { required: true, message: '请输入基础价格', trigger: 'blur' }
  ]
}

// ========== 初始化 ==========
onMounted(() => {
  fetchData()
})

// ========== 获取数据 ==========
const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/v1/room-types', { params: queryParams })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取房型列表失败', error)
  } finally {
    loading.value = false
  }
}

// ========== 搜索 ==========
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// ========== 重置 ==========
const handleReset = () => {
  queryParams.name = ''
  queryParams.code = ''
  queryParams.status = ''
  handleSearch()
}

// ========== 分页 ==========
const handleSizeChange = (val) => {
  queryParams.size = val
  fetchData()
}

const handleCurrentChange = (val) => {
  queryParams.page = val
  fetchData()
}

// ========== 新增 ==========
const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增房型'
  formData.code = ''
  formData.name = ''
  formData.bedType = ''
  formData.maxGuests = 2
  formData.basePrice = 0
  formData.description = ''
  dialogVisible.value = true
}

// ========== 编辑 ==========
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑房型'
  formData.code = row.code
  formData.name = row.name
  formData.bedType = row.bedType
  formData.maxGuests = row.maxGuests
  formData.basePrice = row.basePrice
  formData.description = row.description
  dialogVisible.value = true
}

// ========== 删除 ==========
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该房型吗？', '提示', {
      type: 'warning'
    })
    
    await request.delete('/v1/room-types/' + row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除房型失败', error)
    }
  }
}

// ========== 状态变更 ==========
const handleStatus = async (row) => {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const statusText = newStatus === 'ACTIVE' ? '启用' : '停用'
  
  try {
    await ElMessageBox.confirm('确定要' + statusText + '该房型吗？', '提示', {
      type: 'warning'
    })
    
    await request.put('/v1/room-types/' + row.id + '/status', null, {
      params: { status: newStatus }
    })
    ElMessage.success(statusText + '成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新状态失败', error)
    }
  }
}

// ========== 提交表单 ==========
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await request.put('/v1/room-types/' + editId.value, formData)
        ElMessage.success('更新成功')
      } else {
        // 新增时需要传入酒店ID（这里暂时使用1）
        await request.post('/v1/room-types', { ...formData, hotelId: 1 })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('保存房型失败', error)
    } finally {
      submitLoading.value = false
    }
  })
}

// ========== 弹窗关闭 ==========
const handleDialogClose = () => {
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.room-type-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
