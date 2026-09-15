<template>
  <div class="member-management">
    <div class="page-header">
      <h2>会员管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showRegisterDialog">
          <el-icon><Plus /></el-icon>注册会员
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">会员总数</div>
          <div class="stat-value primary">{{ stats.totalMembers || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">普通会员</div>
          <div class="stat-value">{{ stats.normalCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">银卡会员</div>
          <div class="stat-value silver">{{ stats.silverCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">金卡会员</div>
          <div class="stat-value gold">{{ stats.goldCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">钻石卡</div>
          <div class="stat-value diamond">{{ stats.diamondCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">累计消费</div>
          <div class="stat-value">¥{{ formatMoney(stats.totalConsumption) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="queryParams.levelCode" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="普通会员" value="NORMAL" />
            <el-option label="银卡" value="SILVER" />
            <el-option label="金卡" value="GOLD" />
            <el-option label="钻石卡" value="DIAMOND" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 会员列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="memberList" v-loading="loading" stripe>
        <el-table-column prop="memberNo" label="会员编号" width="150" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.levelCode)" effect="dark" size="small">
              {{ row.levelName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="折扣" width="80" align="center">
          <template #default="{ row }">{{ row.discountRate }}%</template>
        </el-table-column>
        <el-table-column label="可用积分" width="100" align="right">
          <template #default="{ row }">
            <span class="points-value">{{ row.availablePoints }}</span>
          </template>
        </el-table-column>
        <el-table-column label="累计消费" width="120" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.totalConsumption) }}</template>
        </el-table-column>
        <el-table-column prop="totalStayCount" label="入住次数" width="90" align="center" />
        <el-table-column label="最后入住" width="170">
          <template #default="{ row }">{{ formatDateTime(row.lastStayTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetailDialog(row)">详情</el-button>
            <el-button type="success" link size="small" @click="showPointsLog(row)">积分流水</el-button>
            <el-button type="warning" link size="small" @click="showAdjustPoints(row)">调整积分</el-button>
          </template>
        </el-table-column>
      </el-table>
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

    <!-- 注册会员弹窗 -->
    <el-dialog v-model="registerDialogVisible" title="注册会员" width="500px">
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="100px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="registerForm.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="证件号">
          <el-input v-model="registerForm.idNo" placeholder="请输入证件号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="registerForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="saving">确认注册</el-button>
      </template>
    </el-dialog>

    <!-- 会员详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="会员详情" width="700px">
      <div v-if="currentMember">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="会员编号">{{ currentMember.memberNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentMember.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentMember.phone }}</el-descriptions-item>
          <el-descriptions-item label="等级">
            <el-tag :type="getLevelTagType(currentMember.levelCode)" effect="dark">
              {{ currentMember.levelName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="折扣率">{{ currentMember.discountRate }}%</el-descriptions-item>
          <el-descriptions-item label="积分倍率">{{ currentMember.pointsMultiplier }}倍</el-descriptions-item>
          <el-descriptions-item label="可用积分">
            <span class="points-value">{{ currentMember.availablePoints }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="已使用积分">{{ currentMember.usedPoints }}</el-descriptions-item>
          <el-descriptions-item label="累计消费">¥{{ formatMoney(currentMember.totalConsumption) }}</el-descriptions-item>
          <el-descriptions-item label="入住次数">{{ currentMember.totalStayCount }}次</el-descriptions-item>
          <el-descriptions-item label="注册来源">{{ currentMember.registerSourceName }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDateTime(currentMember.registerTime) }}</el-descriptions-item>
          <el-descriptions-item label="最后入住" :span="2">{{ formatDateTime(currentMember.lastStayTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 积分流水弹窗 -->
    <el-dialog v-model="pointsLogVisible" title="积分流水" width="900px">
      <div v-if="currentMember" style="margin-bottom: 15px;">
        <span>会员：{{ currentMember.name }}（{{ currentMember.phone }}）</span>
        <span style="margin-left: 20px;">可用积分：<strong class="points-value">{{ currentMember.availablePoints }}</strong></span>
      </div>
      <el-table :data="pointsLogs" v-loading="pointsLogLoading" stripe max-height="400">
        <el-table-column prop="changeTypeName" label="变动类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getChangeTypeTag(row.changeType)" size="small">{{ row.changeTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动积分" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.points > 0 ? 'text-success' : 'text-danger'">
              {{ row.points > 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforePoints" label="变动前" width="100" align="right" />
        <el-table-column prop="afterPoints" label="变动后" width="100" align="right" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 手动调整积分弹窗 -->
    <el-dialog v-model="adjustDialogVisible" title="手动调整积分" width="500px">
      <div v-if="currentMember" style="margin-bottom: 15px;">
        <span>会员：{{ currentMember.name }}，当前可用积分：<strong class="points-value">{{ currentMember.availablePoints }}</strong></span>
      </div>
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="调整积分" required>
          <el-input-number v-model="adjustForm.points" :step="100" />
          <div class="form-tip">正数增加，负数减少</div>
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="adjustForm.description" type="textarea" :rows="2" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustPoints" :loading="saving">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getMemberList, getMemberById, registerMember,
  getPointsLogs, getMemberStatistics, adjustPoints
} from '@/api/member'

const queryParams = reactive({ page: 1, size: 10, phone: '', name: '', levelCode: '' })
const memberList = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const stats = ref({})

// 注册弹窗
const registerDialogVisible = ref(false)
const registerFormRef = ref(null)
const registerForm = reactive({ phone: '', name: '', gender: '男', idNo: '', remark: '' })
const registerRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

// 详情弹窗
const detailDialogVisible = ref(false)
const currentMember = ref(null)

// 积分流水弹窗
const pointsLogVisible = ref(false)
const pointsLogLoading = ref(false)
const pointsLogs = ref([])

// 调整积分弹窗
const adjustDialogVisible = ref(false)
const adjustForm = reactive({ points: 0, description: '' })

const loadMemberList = async () => {
  loading.value = true
  try {
    const res = await getMemberList(queryParams)
    if (res.code === 200) {
      memberList.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await getMemberStatistics()
    if (res.code === 200) stats.value = res.data
  } catch (e) { console.error(e) }
}

const handleSearch = () => { queryParams.page = 1; loadMemberList() }
const handleReset = () => { queryParams.phone = ''; queryParams.name = ''; queryParams.levelCode = ''; handleSearch() }
const handleSizeChange = (size) => { queryParams.size = size; loadMemberList() }
const handleCurrentChange = (page) => { queryParams.page = page; loadMemberList() }

const showRegisterDialog = () => {
  registerForm.phone = ''; registerForm.name = ''; registerForm.gender = '男'; registerForm.idNo = ''; registerForm.remark = ''
  registerDialogVisible.value = true
}

const handleRegister = async () => {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = await registerMember(registerForm)
    if (res.code === 200) {
      ElMessage.success('注册成功')
      registerDialogVisible.value = false
      loadMemberList()
      loadStats()
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (e) {
    ElMessage.error('注册失败')
  } finally {
    saving.value = false
  }
}

const showDetailDialog = async (row) => {
  try {
    const res = await getMemberById(row.id)
    if (res.code === 200) { currentMember.value = res.data; detailDialogVisible.value = true }
  } catch (e) { console.error(e) }
}

const showPointsLog = async (row) => {
  currentMember.value = row
  pointsLogVisible.value = true
  pointsLogLoading.value = true
  try {
    const res = await getPointsLogs(row.id)
    if (res.code === 200) pointsLogs.value = res.data
  } catch (e) { console.error(e) }
  finally { pointsLogLoading.value = false }
}

const showAdjustPoints = (row) => {
  currentMember.value = row
  adjustForm.points = 0
  adjustForm.description = ''
  adjustDialogVisible.value = true
}

const handleAdjustPoints = async () => {
  if (adjustForm.points === 0) { ElMessage.warning('请输入调整积分数'); return }
  saving.value = true
  try {
    const res = await adjustPoints(currentMember.value.id, { points: adjustForm.points, description: adjustForm.description })
    if (res.code === 200) {
      ElMessage.success('调整成功')
      adjustDialogVisible.value = false
      loadMemberList()
      loadStats()
    }
  } catch (e) { ElMessage.error('调整失败') }
  finally { saving.value = false }
}

const getLevelTagType = (code) => {
  const map = { NORMAL: 'info', SILVER: '', GOLD: 'warning', DIAMOND: 'danger' }
  return map[code] || 'info'
}

const getChangeTypeTag = (type) => {
  const map = { EARN: 'success', EXCHANGE: 'primary', REFUND: 'warning', ADJUST: 'info' }
  return map[type] || ''
}

const formatMoney = (val) => val != null ? Number(val).toFixed(2) : '0.00'
const formatDateTime = (dt) => dt ? new Date(dt).toLocaleString('zh-CN') : '-'

onMounted(() => { loadMemberList(); loadStats() })
</script>

<style scoped>
.member-management { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; color: #303133; }
.filter-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
.stat-card { text-align: center; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; }
.stat-value.primary { color: #409eff; }
.stat-value.silver { color: #909399; }
.stat-value.gold { color: #e6a23c; }
.stat-value.diamond { color: #f56c6c; }
.points-value { color: #e6a23c; font-weight: bold; }
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
