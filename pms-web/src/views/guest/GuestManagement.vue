<template>
  <div class="guest-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>客人档案管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="客人姓名">
          <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="VIP状态">
          <el-select v-model="queryParams.isVip" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="VIP" :value="true" />
            <el-option label="非VIP" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="黑名单">
          <el-select v-model="queryParams.isBlacklisted" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 客人列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="guestList" v-loading="loading" stripe>
        <el-table-column prop="name" label="客人姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="idTypeName" label="证件类型" width="100" />
        <el-table-column prop="idNo" label="证件号码" width="180" />
        <el-table-column label="VIP" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isVip ? 'warning' : 'info'" size="small">
              {{ row.isVip ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="黑名单" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isBlacklisted ? 'danger' : 'success'" size="small">
              {{ row.isBlacklisted ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stayCount" label="入住次数" width="100" align="center" />
        <el-table-column prop="lastStayTime" label="最后入住时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.lastStayTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link size="small" @click="handleViewHistory(row)">
                入住历史
              </el-button>
              <el-button 
                :type="row.isVip ? 'warning' : 'success'" 
                link 
                size="small" 
                @click="handleToggleVip(row)"
              >
                {{ row.isVip ? '取消VIP' : '设为VIP' }}
              </el-button>
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, row)">
                <el-button type="primary" link size="small">
                  更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="!row.isBlacklisted" command="addToBlacklist">
                      加入黑名单
                    </el-dropdown-item>
                    <el-dropdown-item v-else command="removeFromBlacklist">
                      移出黑名单
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
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

    <!-- 入住历史对话框 -->
    <el-dialog v-model="historyDialogVisible" title="入住历史" width="900px">
      <div v-if="currentGuest" class="history-header">
        <span>客人姓名：{{ currentGuest.name }}</span>
        <span>手机号：{{ currentGuest.phone }}</span>
        <span>入住次数：{{ currentGuest.stayCount }}</span>
      </div>
      <el-table :data="stayHistory" v-loading="historyLoading" stripe>
        <el-table-column prop="stayNo" label="入住单号" width="140" />
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column prop="roomTypeName" label="房型" width="120" />
        <el-table-column prop="checkInTime" label="入住时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
        </el-table-column>
        <el-table-column prop="checkOutTime" label="离店时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.checkOutTime) }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.totalAmount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CHECKED_IN' ? 'success' : 'info'">
              {{ row.status === 'CHECKED_IN' ? '在住' : '已离店' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="historyDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 加入黑名单对话框 -->
    <el-dialog v-model="blacklistDialogVisible" title="加入黑名单" width="500px">
      <el-form :model="blacklistForm" label-width="100px">
        <el-form-item label="客人姓名">
          <el-input :value="blacklistForm.name" disabled />
        </el-form-item>
        <el-form-item label="黑名单原因" required>
          <el-input v-model="blacklistForm.reason" type="textarea" :rows="3" placeholder="请输入加入黑名单的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="blacklistDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmBlacklist" :loading="blacklistLoading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, ArrowDown } from '@element-plus/icons-vue'
import { 
  getGuestList, 
  setVipStatus, 
  addToBlacklist as addToBlacklistApi, 
  removeFromBlacklist, 
  getStayHistory 
} from '@/api/guest'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  phone: '',
  isVip: '',
  isBlacklisted: ''
})

// 列表数据
const guestList = ref([])
const total = ref(0)
const loading = ref(false)

// 入住历史对话框
const historyDialogVisible = ref(false)
const historyLoading = ref(false)
const currentGuest = ref(null)
const stayHistory = ref([])

// 黑名单对话框
const blacklistDialogVisible = ref(false)
const blacklistLoading = ref(false)
const blacklistForm = reactive({
  id: null,
  name: '',
  reason: ''
})

/**
 * 加载客人列表
 */
const loadGuestList = async () => {
  loading.value = true
  try {
    const response = await getGuestList(queryParams)
    if (response.code === 200) {
        guestList.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询客人列表失败:', error)
    ElMessage.error('查询客人列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  queryParams.page = 1
  loadGuestList()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  queryParams.name = ''
  queryParams.phone = ''
  queryParams.isVip = ''
  queryParams.isBlacklisted = ''
  handleSearch()
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
  queryParams.size = size
  loadGuestList()
}

/**
 * 当前页变化
 */
const handleCurrentChange = (page) => {
  queryParams.page = page
  loadGuestList()
}

/**
 * 查看入住历史
 */
const handleViewHistory = async (row) => {
  currentGuest.value = row
  historyDialogVisible.value = true
  historyLoading.value = true
  try {
    const response = await getStayHistory(row.id)
    if (response.code === 200) {
      stayHistory.value = response.data
    } else {
      ElMessage.error(response.message || '查询入住历史失败')
    }
  } catch (error) {
    console.error('查询入住历史失败:', error)
    ElMessage.error('查询入住历史失败')
  } finally {
    historyLoading.value = false
  }
}

/**
 * 切换VIP状态
 */
const handleToggleVip = async (row) => {
  const action = row.isVip ? '取消VIP' : '设为VIP'
  try {
    await ElMessageBox.confirm(`确定要${action}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await setVipStatus(row.id, !row.isVip)
    if (response.code === 200) {
      ElMessage.success(`${action}成功`)
      loadGuestList()
    } else {
      ElMessage.error(response.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

/**
 * 处理下拉菜单命令
 */
const handleCommand = (command, row) => {
  if (command === 'addToBlacklist') {
    handleAddToBlacklist(row)
  } else if (command === 'removeFromBlacklist') {
    handleRemoveFromBlacklist(row)
  }
}

/**
 * 打开加入黑名单对话框
 */
const handleAddToBlacklist = (row) => {
  blacklistForm.id = row.id
  blacklistForm.name = row.name
  blacklistForm.reason = ''
  blacklistDialogVisible.value = true
}

/**
 * 确认加入黑名单
 */
const handleConfirmBlacklist = async () => {
  if (!blacklistForm.reason) {
    ElMessage.warning('请输入黑名单原因')
    return
  }
  
  blacklistLoading.value = true
  try {
    const response = await addToBlacklistApi(blacklistForm.id, blacklistForm.reason)
    if (response.code === 200) {
      ElMessage.success('加入黑名单成功')
      blacklistDialogVisible.value = false
      loadGuestList()
    } else {
      ElMessage.error(response.message || '加入黑名单失败')
    }
  } catch (error) {
    console.error('加入黑名单失败:', error)
    ElMessage.error('加入黑名单失败')
  } finally {
    blacklistLoading.value = false
  }
}

/**
 * 移出黑名单
 */
const handleRemoveFromBlacklist = async (row) => {
  try {
    await ElMessageBox.confirm('确定要将该客人移出黑名单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await removeFromBlacklist(row.id)
    if (response.code === 200) {
      ElMessage.success('移出黑名单成功')
      loadGuestList()
    } else {
      ElMessage.error(response.message || '移出黑名单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移出黑名单失败:', error)
      ElMessage.error('移出黑名单失败')
    }
  }
}

/**
 * 导出客人数据
 */
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 初始化加载数据
onMounted(() => {
  loadGuestList()
})
</script>

<style scoped>
.guest-management {
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

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.history-header {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>

