<template>
  <div class="room-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>房间管理</h2>
      <el-button type="success" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增房间
      </el-button>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-card-icon icon-purple">
            <el-icon :size="24"><House /></el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ summary.totalRooms || 0 }}</div>
            <div class="stat-card-label">房间总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-card-icon icon-green">
            <el-icon :size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ summary.availableRooms || 0 }}</div>
            <div class="stat-card-label">空闲</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-card-icon icon-red">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ summary.occupiedRooms || 0 }}</div>
            <div class="stat-card-label">在住</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-card-icon icon-orange">
            <el-icon :size="24"><Brush /></el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ summary.dirtyRooms || 0 }}</div>
            <div class="stat-card-label">脏房</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-card-icon icon-blue">
            <el-icon :size="24"><SetUp /></el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ summary.maintenanceRooms || 0 }}</div>
            <div class="stat-card-label">维修</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-card-icon icon-gray">
            <el-icon :size="24"><WarningFilled /></el-icon>
          </div>
          <div class="stat-card-content">
            <div class="stat-card-value">{{ summary.oooRooms || 0 }}</div>
            <div class="stat-card-label">停用</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="房间号">
          <el-input 
            v-model="queryParams.roomNo" 
            placeholder="请输入房间号" 
            clearable 
            style="width: 180px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="房型">
          <el-select 
            v-model="queryParams.roomTypeId" 
            placeholder="全部房型" 
            clearable 
            style="width: 160px;"
          >
            <el-option label="全部房型" :value="null" />
            <el-option v-for="item in roomTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-select 
            v-model="queryParams.floorId" 
            placeholder="全部楼层" 
            clearable 
            style="width: 140px;"
          >
            <el-option label="全部楼层" :value="null" />
            <el-option v-for="item in floorOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="queryParams.status" 
            placeholder="全部状态" 
            clearable 
            style="width: 140px;"
          >
            <el-option label="全部状态" :value="null" />
            <el-option label="空闲" value="AVAILABLE">
              <el-tag type="success" size="small" effect="plain">空闲</el-tag>
            </el-option>
            <el-option label="在住" value="OCCUPIED">
              <el-tag type="danger" size="small" effect="plain">在住</el-tag>
            </el-option>
            <el-option label="脏房" value="DIRTY">
              <el-tag type="warning" size="small" effect="plain">脏房</el-tag>
            </el-option>
            <el-option label="维修" value="MAINTENANCE">
              <el-tag type="info" size="small" effect="plain">维修</el-tag>
            </el-option>
            <el-option label="停用" value="OOO">
              <el-tag size="small" effect="plain">停用</el-tag>
            </el-option>
            <el-option label="预留" value="RESERVED">
              <el-tag type="primary" size="small" effect="plain">预留</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column prop="roomTypeName" label="房型" width="120" />
        <el-table-column prop="floorName" label="楼层" width="100" />
        <el-table-column prop="basePrice" label="基础价格" width="100" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600;">¥{{ row.basePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="warning" link size="small" @click="handleStatus(row)">
              <el-icon><Switch /></el-icon>
              变更状态
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="房间号" prop="roomNo">
          <el-input v-model="formData.roomNo" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="formData.roomTypeId" placeholder="请选择房型" style="width: 100%">
            <el-option v-for="item in roomTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floorId">
          <el-select v-model="formData.floorId" placeholder="请选择楼层" style="width: 100%">
            <el-option v-for="item in floorOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 状态变更弹窗 -->
    <el-dialog 
      v-model="statusDialogVisible" 
      title="变更房间状态" 
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(currentRoom?.status)">{{ getStatusLabel(currentRoom?.status) }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="newStatus" placeholder="请选择新状态" style="width: 100%">
            <el-option label="空闲" value="AVAILABLE" />
            <el-option label="在住" value="OCCUPIED" />
            <el-option label="脏房" value="DIRTY" />
            <el-option label="维修" value="MAINTENANCE" />
            <el-option label="停用" value="OOO" />
            <el-option label="预留" value="RESERVED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusSubmitLoading" @click="handleStatusSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Switch, House, CircleCheck, User, Brush, SetUp, WarningFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

// ========== 查询参数 ==========
const queryParams = reactive({
  page: 1,
  size: 10,
  hotelId: 1,
  roomNo: '',
  roomTypeId: null,
  floorId: null,
  status: null
})

// ========== 表格数据 ==========
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// ========== 统计数据 ==========
const summary = reactive({
  totalRooms: 0,
  availableRooms: 0,
  occupiedRooms: 0,
  dirtyRooms: 0,
  maintenanceRooms: 0,
  oooRooms: 0
})

// ========== 下拉选项 ==========
const roomTypeOptions = ref([])
const floorOptions = ref([])

// ========== 弹窗相关 ==========
const dialogVisible = ref(false)
const dialogTitle = ref('新增房间')
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)

const formData = reactive({
  roomNo: '',
  roomTypeId: '',
  floorId: '',
  description: ''
})

const formRules = {
  roomNo: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }]
}

// ========== 状态变更相关 ==========
const statusDialogVisible = ref(false)
const currentRoom = ref(null)
const newStatus = ref('')
const statusSubmitLoading = ref(false)


// ========== 初始化 ==========
onMounted(() => {
  fetchData()
  fetchSummary()
  fetchRoomTypeOptions()
  fetchFloorOptions()
})

// ========== 获取数据 ==========
const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/v1/rooms', { params: queryParams })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取房间列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchSummary = async () => {
  try {
    const res = await request.get('/v1/rooms/summary', { params: { hotelId: 1 } })
    Object.assign(summary, res.data)
  } catch (error) {
    console.error('获取房间统计数据失败', error)
  }
}

const fetchRoomTypeOptions = async () => {
  try {
    const res = await request.get('/v1/room-types', { params: { hotelId: 1, pageNum: 1, pageSize: 100 } })
    roomTypeOptions.value = res.data.records || []
  } catch (error) {
    console.error('获取房型选项失败', error)
  }
}

const fetchFloorOptions = async () => {
  try {
    const res = await request.get('/v1/floors', { params: { hotelId: 1, pageNum: 1, pageSize: 100 } })
    floorOptions.value = res.data.records || []
  } catch (error) {
    console.error('获取楼层选项失败', error)
  }
}

// ========== 搜索和重置 ==========
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

const handleReset = () => {
  queryParams.roomNo = ''
  queryParams.roomTypeId = null
  queryParams.floorId = null
  queryParams.status = null
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
  dialogTitle.value = '新增房间'
  formData.roomNo = ''
  formData.roomTypeId = ''
  formData.floorId = ''
  formData.description = ''
  dialogVisible.value = true
}

// ========== 编辑 ==========
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑房间'
  formData.roomNo = row.roomNo
  formData.roomTypeId = row.roomTypeId
  formData.floorId = row.floorId
  formData.description = row.description
  dialogVisible.value = true
}

// ========== 删除 ==========
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该房间吗？', '提示', {
      type: 'warning'
    })
    
    await request.delete('/v1/rooms/' + row.id)
    ElMessage.success('删除成功')
    fetchData()
    fetchSummary()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除房间失败', error)
    }
  }
}

// ========== 状态变更 ==========
const handleStatus = (row) => {
  currentRoom.value = row
  newStatus.value = row.status
  statusDialogVisible.value = true
}

const handleStatusSubmit = async () => {
  if (!currentRoom.value || !newStatus.value) return
  
  statusSubmitLoading.value = true
  try {
    await request.put('/v1/rooms/' + currentRoom.value.id + '/status', null, {
      params: { status: newStatus.value }
    })
    ElMessage.success('状态变更成功')
    statusDialogVisible.value = false
    fetchData()
    fetchSummary()
  } catch (error) {
    console.error('变更状态失败', error)
  } finally {
    statusSubmitLoading.value = false
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
        await request.put('/v1/rooms/' + editId.value, formData)
        ElMessage.success('更新成功')
      } else {
        await request.post('/v1/rooms', { ...formData, hotelId: 1 })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
      fetchSummary()
    } catch (error) {
      console.error('保存房间失败', error)
    } finally {
      submitLoading.value = false
    }
  })
}

// ========== 弹窗关闭 ==========
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// ========== 状态相关 ==========
const getStatusType = (status) => {
  const map = {
    'AVAILABLE': 'success',
    'OCCUPIED': 'danger',
    'DIRTY': 'warning',
    'MAINTENANCE': 'info',
    'OOO': 'info',
    'RESERVED': 'primary'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    'AVAILABLE': '空闲',
    'OCCUPIED': '在住',
    'DIRTY': '脏房',
    'MAINTENANCE': '维修',
    'OOO': '停用',
    'RESERVED': '预留'
  }
  return map[status] || status
}

</script>
<style scoped lang="scss">
.room-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
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
    color: #303133;
  }
}

// 统计卡片样式
.stat-cards {
  margin-bottom: 20px;
  
  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px 0 rgba(0, 0, 0, 0.12);
    }
    
    .stat-card-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
      
      &.icon-purple { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
      &.icon-green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
      &.icon-red { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
      &.icon-orange { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); }
      &.icon-blue { background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%); }
      &.icon-gray { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }
    }
    
    .stat-card-content {
      flex: 1;
      
      .stat-card-value {
        font-size: 28px;
        font-weight: 700;
        color: #303133;
        line-height: 1.2;
      }
      
      .stat-card-label {
        font-size: 13px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

// 搜索卡片样式
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

// 表格卡片样式
.table-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
