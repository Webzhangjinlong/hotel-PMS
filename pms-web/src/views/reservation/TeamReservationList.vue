<template>
  <div class="team-reservation-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>团队预订</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增团队预订
        </el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="预订号">
          <el-input 
            v-model="queryParams.teamReservationNo" 
            placeholder="请输入团队预订号" 
            clearable 
            style="width: 160px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="团队名称">
          <el-input 
            v-model="queryParams.teamName" 
            placeholder="请输入团队名称" 
            clearable 
            style="width: 140px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input 
            v-model="queryParams.contactName" 
            placeholder="请输入联系人" 
            clearable 
            style="width: 120px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="queryParams.status" 
            placeholder="全部状态" 
            clearable 
            style="width: 120px;"
          >
            <el-option label="全部状态" :value="null" />
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已入住" value="CHECKED_IN" />
            <el-option label="已退房" value="CHECKED_OUT" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;"
            @change="handleDateChange"
          />
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
        <el-table-column prop="teamReservationNo" label="团队预订号" width="140" fixed="left" />
        <el-table-column prop="teamName" label="团队名称" width="140" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="checkInDate" label="入住日期" width="110" />
        <el-table-column prop="checkOutDate" label="离店日期" width="110" />
        <el-table-column prop="nights" label="房晚" width="60" align="center" />
        <el-table-column prop="totalRooms" label="房间数" width="80" align="center" />
        <el-table-column prop="totalAmount" label="总金额" width="100" align="right">
          <template #default="{ row }">
            <span class="amount">&yen;{{ row.totalAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="settlementType" label="结算方式" width="100" align="center">
          <template #default="{ row }">
            {{ row.settlementType === 'UNIFIED' ? '统一结算' : '分开结算' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="success" 
              link 
              @click="handleConfirm(row)"
            >确认</el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="warning" 
              link 
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button 
              v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'" 
              type="danger" 
              link 
              @click="handleCancel(row)"
            >取消</el-button>
            <el-button 
              v-if="row.status === 'CONFIRMED'" 
              type="primary" 
              link 
              @click="handleCheckIn(row)"
            >入住</el-button>
            <el-button 
              v-if="row.status === 'CHECKED_IN'" 
              type="warning" 
              link 
              @click="handleCheckOut(row)"
            >退房</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// ========== 查询参数 ==========
const queryParams = reactive({
  hotelId: 1, // 默认酒店ID
  teamReservationNo: '',
  teamName: '',
  contactName: '',
  contactPhone: '',
  status: null,
  checkInDateStart: null,
  checkInDateEnd: null,
  page: 1,
  size: 10
})

const dateRange = ref(null)

// ========== 表格数据 ==========
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// ========== 初始化 ==========
onMounted(() => {
  fetchData()
})

// ========== 获取数据 ==========
const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/v1/team-reservations', { params: queryParams })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取团队预订列表失败', error)
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
  queryParams.teamReservationNo = ''
  queryParams.teamName = ''
  queryParams.contactName = ''
  queryParams.contactPhone = ''
  queryParams.status = null
  queryParams.checkInDateStart = null
  queryParams.checkInDateEnd = null
  dateRange.value = null
  handleSearch()
}

// ========== 日期变化 ==========
const handleDateChange = (val) => {
  if (val) {
    queryParams.checkInDateStart = val[0]
    queryParams.checkInDateEnd = val[1]
  } else {
    queryParams.checkInDateStart = null
    queryParams.checkInDateEnd = null
  }
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
  router.push('/reservation/team/create')
}

// ========== 查看详情 ==========
const handleView = (row) => {
  router.push(`/reservation/team/${row.id}`)
}

// ========== 编辑 ==========
const handleEdit = (row) => {
  router.push(`/reservation/team/${row.id}?edit=true`)
}

// ========== 确认预订 ==========
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确定要确认该团队预订吗？', '提示', { type: 'warning' })
    await request.put(`/v1/team-reservations/${row.id}/confirm`)
    ElMessage.success('确认成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认失败', error)
    }
  }
}

// ========== 取消预订 ==========
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该团队预订吗？取消后不可恢复。', '警告', { type: 'error' })
    await request.delete(`/v1/team-reservations/${row.id}`)
    ElMessage.success('取消成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败', error)
    }
  }
}

// ========== 入住 ==========
const handleCheckIn = async (row) => {
  try {
    await ElMessageBox.confirm('确定要办理团队入住吗？', '提示', { type: 'warning' })
    await request.post(`/v1/team-reservations/${row.id}/check-in`, {})
    ElMessage.success('入住成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('入住失败', error)
    }
  }
}

// ========== 退房 ==========
const handleCheckOut = async (row) => {
  try {
    await ElMessageBox.confirm('确定要办理团队退房吗？', '提示', { type: 'warning' })
    await request.post(`/v1/team-reservations/${row.id}/check-out`, {})
    ElMessage.success('退房成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退房失败', error)
    }
  }
}

// ========== 状态相关 ==========
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'CHECKED_IN': 'primary',
    'CHECKED_OUT': 'info',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CHECKED_IN': '已入住',
    'CHECKED_OUT': '已退房',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}
</script>

<style scoped lang="scss">
.team-reservation-container {
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
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.search-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 20px 20px 0;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

.table-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .amount {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>