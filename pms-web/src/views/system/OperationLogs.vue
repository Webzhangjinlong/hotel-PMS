<template>
  <div class="operation-logs">
    <div class="page-header">
      <h2>操作日志</h2>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="操作模块">
          <el-select v-model="queryParams.module" placeholder="全部模块" clearable style="width: 130px;">
            <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryParams.action" placeholder="全部类型" clearable style="width: 130px;">
            <el-option v-for="item in actionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-select v-model="queryParams.operatorName" placeholder="全部操作人" clearable filterable style="width: 150px;">
            <el-option v-for="user in userList" :key="user.id" :label="user.realName" :value="user.realName" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="logList" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="createdAt" label="操作时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="module" label="操作模块" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="操作内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="targetType" label="目标类型" width="100" />
        <el-table-column prop="targetId" label="目标ID" width="80" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDateTime(currentLog.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ currentLog.operatorId }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.action }}</el-descriptions-item>
        <el-descriptions-item label="目标类型">{{ currentLog.targetType }}</el-descriptions-item>
        <el-descriptions-item label="目标ID">{{ currentLog.targetId }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="酒店ID">{{ currentLog.hotelId }}</el-descriptions-item>
        <el-descriptions-item label="操作内容" :span="2">
          <div style="white-space: pre-wrap;">{{ currentLog.content || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { getOperationLogList } from '@/api/operationLog'
import { getUserList } from '@/api/user'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const dateRange = ref([])

const queryParams = reactive({
  page: 1,
  size: 20,
  module: '',
  action: '',
  operatorName: '',
  startDate: '',
  endDate: ''
})

const userList = ref([])
const detailVisible = ref(false)
const currentLog = ref({})

// 模块选项
const moduleOptions = [
  { label: '预订管理', value: '预订管理' },
  { label: '入住管理', value: '入住管理' },
  { label: '账务管理', value: '账务管理' },
  { label: '交班管理', value: '交班管理' },
  { label: '夜审管理', value: '夜审管理' },
  { label: '房态管理', value: '房态管理' },
  { label: '价格管理', value: '价格管理' },
  { label: '系统设置', value: '系统设置' },
  { label: '登录登出', value: '登录登出' }
]

// 操作类型选项
const actionOptions = [
  { label: '创建', value: '创建' },
  { label: '修改', value: '修改' },
  { label: '删除', value: '删除' },
  { label: '查询', value: '查询' },
  { label: '登录', value: '登录' },
  { label: '登出', value: '登出' },
  { label: '入住', value: '入住' },
  { label: '退房', value: '退房' },
  { label: '收款', value: '收款' },
  { label: '退款', value: '退款' }
]

// 监听日期范围变化
watch(dateRange, (val) => {
  if (val && val.length === 2) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
})

// 获取用户列表
const fetchUserList = async () => {
  try {
    const res = await getUserList()
    userList.value = res.data || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 查询
const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getOperationLogList(queryParams)
    logList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('查询操作日志失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置
const handleReset = () => {
  queryParams.page = 1
  queryParams.module = ''
  queryParams.action = ''
  queryParams.operatorName = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  dateRange.value = []
  handleSearch()
}

// 查看详情
const handleViewDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 获取操作类型标签类型
const getActionType = (action) => {
  const map = {
    '创建': 'success',
    '修改': 'warning',
    '删除': 'danger',
    '登录': '',
    '登出': 'info'
  }
  return map[action] || ''
}

onMounted(() => {
  fetchUserList()
  handleSearch()
})
</script>

<style scoped lang="scss">
.operation-logs {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
  
  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.filter-card {
  margin-bottom: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>


