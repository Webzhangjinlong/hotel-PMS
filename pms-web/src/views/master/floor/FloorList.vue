<template>
  <div class="floor-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>楼层管理</h2>
      <el-button type="success" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增楼层
      </el-button>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="楼层名称">
          <el-input v-model="queryParams.name" placeholder="请输入楼层名称" clearable />
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
        <el-table-column prop="floorNo" label="楼层号" width="100" />
        <el-table-column prop="name" label="楼层名称" />
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
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="楼层号" prop="floorNo">
          <el-input-number v-model="formData.floorNo" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="楼层名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入楼层名称" />
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

import { useUserStore } from '@/stores/user'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
const userStore = useUserStore()

// ========== 查询参数 ==========
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
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
  floorNo: 1,
  name: ''
})

// ========== 表单验证规则 ==========
const formRules = {
  floorNo: [
    { required: true, message: '请输入楼层号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入楼层名称', trigger: 'blur' }
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
    const res = await request.get('/v1/floors', { params: queryParams })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取楼层列表失败', error)
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
  dialogTitle.value = '新增楼层'
  formData.floorNo = 1
  formData.name = ''
  dialogVisible.value = true
}

// ========== 编辑 ==========
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑楼层'
  formData.floorNo = row.floorNo
  formData.name = row.name
  dialogVisible.value = true
}

// ========== 删除 ==========
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该楼层吗？', '提示', {
      type: 'warning'
    })
    
    await request.delete('/v1/floors/' + row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除楼层失败', error)
    }
  }
}

// ========== 状态变更 ==========
const handleStatus = async (row) => {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const statusText = newStatus === 'ACTIVE' ? '启用' : '停用'
  
  try {
    await ElMessageBox.confirm('确定要' + statusText + '该楼层吗？', '提示', {
      type: 'warning'
    })
    
    await request.put('/v1/floors/' + row.id + '/status', null, {
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
        await request.put('/v1/floors/' + editId.value, formData)
        ElMessage.success('更新成功')
      } else {
        // 新增时需要传入酒店ID（这里暂时使用1）
        await request.post('/v1/floors', { ...formData, hotelId: userStore.hotelId })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('保存楼层失败', error)
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
.floor-container {
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
