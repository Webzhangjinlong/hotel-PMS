<template>
  <div class="level-config">
    <div class="page-header">
      <h2>会员等级配置</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleInitLevels" :loading="initLoading">
          初始化默认等级
        </el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="levelList" v-loading="loading" stripe>
        <el-table-column prop="levelName" label="等级名称" width="150">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.levelCode)" effect="dark">{{ row.levelName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discountRate" label="房价折扣" width="120" align="center">
          <template #default="{ row }">{{ row.discountRate }}%</template>
        </el-table-column>
        <el-table-column prop="pointsMultiplier" label="积分倍率" width="120" align="center">
          <template #default="{ row }">{{ row.pointsMultiplier }}倍</template>
        </el-table-column>
        <el-table-column prop="minTotalConsumption" label="升级条件(元)" width="150" align="right">
          <template #default="{ row }">≥{{ formatMoney(row.minTotalConsumption) }}</template>
        </el-table-column>
        <el-table-column prop="benefitsDesc" label="权益描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑等级配置" width="600px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="120px">
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="editForm.levelName" />
        </el-form-item>
        <el-form-item label="房价折扣(%)" prop="discountRate">
          <el-input-number v-model="editForm.discountRate" :min="1" :max="100" :precision="0" />
          <span class="form-tip">100为原价，95为95折</span>
        </el-form-item>
        <el-form-item label="积分倍率" prop="pointsMultiplier">
          <el-input-number v-model="editForm.pointsMultiplier" :min="0.1" :max="10" :step="0.5" :precision="1" />
          <span class="form-tip">每消费1元获得的积分数</span>
        </el-form-item>
        <el-form-item label="升级条件(元)" prop="minTotalConsumption">
          <el-input-number v-model="editForm.minTotalConsumption" :min="0" :step="1000" />
          <span class="form-tip">累计消费达到此金额自动升级</span>
        </el-form-item>
        <el-form-item label="权益描述">
          <el-input v-model="editForm.benefitsDesc" type="textarea" :rows="3" placeholder="请输入权益描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sortOrder" :min="0" :max="99" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio value="ACTIVE">启用</el-radio>
            <el-radio value="INACTIVE">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLevelList, updateLevel, initLevels } from '@/api/member'

const levelList = ref([])
const loading = ref(false)
const saving = ref(false)
const initLoading = ref(false)

const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: null, levelCode: '', levelName: '', discountRate: 100,
  pointsMultiplier: 1.0, minTotalConsumption: 0, benefitsDesc: '', sortOrder: 0, status: 'ACTIVE'
})
const editRules = {
  levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  discountRate: [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
  pointsMultiplier: [{ required: true, message: '请输入积分倍率', trigger: 'blur' }]
}

const loadLevelList = async () => {
  loading.value = true
  try {
    const res = await getLevelList()
    if (res.code === 200) levelList.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const showEditDialog = (row) => {
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

const handleSave = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = await updateLevel(editForm.id, editForm)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      loadLevelList()
    }
  } catch (e) { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

const handleInitLevels = async () => {
  await ElMessageBox.confirm('确定要初始化默认等级配置吗？已有配置不会被覆盖。', '提示', { type: 'warning' })
  initLoading.value = true
  try {
    const res = await initLevels()
    if (res.code === 200) {
      ElMessage.success('初始化成功')
      loadLevelList()
    }
  } catch (e) { ElMessage.error('初始化失败') }
  finally { initLoading.value = false }
}

const getLevelTagType = (code) => {
  const map = { NORMAL: 'info', SILVER: '', GOLD: 'warning', DIAMOND: 'danger' }
  return map[code] || 'info'
}

const formatMoney = (val) => val != null ? Number(val).toFixed(2) : '0.00'

onMounted(() => { loadLevelList() })
</script>

<style scoped>
.level-config { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; color: #303133; }
.form-tip { font-size: 12px; color: #909399; margin-left: 10px; }
</style>
