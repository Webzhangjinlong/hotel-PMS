<template>
  <div class="price-plan-container">
    <div class="page-header">
      <h2>房价码管理</h2>
      <el-button type="success" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增房价码
      </el-button>
    </div>
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="code" label="房价码" width="120" />
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <span v-if="row.validFrom && row.validTo">{{ row.validFrom }} ~ {{ row.validTo }}</span>
            <span v-else-if="row.validFrom">{{ row.validFrom }} 起</span>
            <span v-else>永久</span>
          </template>
        </el-table-column>
        <el-table-column label="房型数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.details ? row.details.length : 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="房型价格" min-width="250">
          <template #default="{ row }">
            <div v-if="row.details && row.details.length > 0" class="detail-tags">
              <el-tag v-for="d in row.details" :key="d.id" size="small" class="detail-tag">
                {{ d.roomTypeName }}: ¥{{ d.finalPrice || d.basePrice }}
              </el-tag>
            </div>
            <span v-else class="no-detail">未配置房型</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
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
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房价码编码" prop="code">
              <el-input v-model="formData.code" placeholder="如：PEAK_SEASON" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房价码名称" prop="name">
              <el-input v-model="formData.name" placeholder="如：旺季价" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="有效期开始">
              <el-date-picker v-model="formData.validFrom" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期结束">
              <el-date-picker v-model="formData.validTo" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>

        <el-divider content-position="left">房型价格配置</el-divider>
        
        <div v-for="(detail, index) in formData.details" :key="index" class="detail-row">
          <el-row :gutter="12" align="middle">
            <el-col :span="6">
              <el-select v-model="detail.roomTypeId" placeholder="选择房型" style="width: 100%">
                <el-option v-for="rt in roomTypeOptions" :key="rt.id" :label="rt.name" :value="rt.id" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-input-number v-model="detail.basePrice" :min="0" :precision="2" placeholder="基础价" style="width: 100%" />
            </el-col>
            <el-col :span="4">
              <el-select v-model="detail.discountType" style="width: 100%">
                <el-option label="无折扣" value="NONE" />
                <el-option label="百分比" value="PERCENT" />
                <el-option label="固定价" value="FIXED" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-input-number v-model="detail.discountValue" :min="0" :precision="2" :disabled="detail.discountType === 'NONE'" style="width: 100%" />
            </el-col>
            <el-col :span="4">
              <span class="final-price">¥{{ calculateFinalPrice(detail) }}</span>
            </el-col>
            <el-col :span="2">
              <el-button type="danger" :icon="Delete" circle size="small" @click="removeDetail(index)" />
            </el-col>
          </el-row>
        </div>
        
        <el-button type="primary" link @click="addDetail" style="margin-top: 8px">
          <el-icon><Plus /></el-icon> 添加房型
        </el-button>
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
import { Plus, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const roomTypeOptions = ref([])
const queryParams = reactive({ page: 1, size: 10 })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

const formData = reactive({
  code: '',
  name: '',
  validFrom: '',
  validTo: '',
  description: '',
  details: []
})

const formRules = {
  code: [{ required: true, message: '请输入房价码编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入房价码名称', trigger: 'blur' }]
}

onMounted(() => { fetchRoomTypeOptions(); fetchData() })

const fetchRoomTypeOptions = async () => {
  try {
    const res = await request.get('/v1/room-types/options', { params: { hotelId: 1 } })
    roomTypeOptions.value = res.data
  } catch (e) { console.error(e) }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/v1/price-plans', {
      params: { hotelId: 1, page: queryParams.page, size: queryParams.size }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const calculateFinalPrice = (detail) => {
  if (!detail.basePrice) return '0.00'
  if (detail.discountType === 'NONE' || !detail.discountValue) return detail.basePrice.toFixed(2)
  if (detail.discountType === 'PERCENT') {
    return (detail.basePrice * (1 - detail.discountValue / 100)).toFixed(2)
  }
  if (detail.discountType === 'FIXED') return detail.discountValue.toFixed(2)
  return detail.basePrice.toFixed(2)
}

const addDetail = () => {
  formData.details.push({ roomTypeId: null, basePrice: 0, discountType: 'NONE', discountValue: 0 })
}

const removeDetail = (index) => {
  formData.details.splice(index, 1)
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增房价码'
  Object.assign(formData, {
    code: '', name: '', validFrom: '', validTo: '', description: '',
    details: [{ roomTypeId: null, basePrice: 0, discountType: 'NONE', discountValue: 0 }]
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑房价码'
  Object.assign(formData, {
    code: row.code,
    name: row.name,
    validFrom: row.validFrom || '',
    validTo: row.validTo || '',
    description: row.description || '',
    details: row.details ? row.details.map(d => ({
      id: d.id,
      roomTypeId: d.roomTypeId,
      basePrice: d.basePrice || 0,
      discountType: d.discountType || 'NONE',
      discountValue: d.discountValue || 0
    })) : []
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (formData.details.length === 0) {
      ElMessage.warning('请至少添加一个房型配置')
      return
    }
    for (const d of formData.details) {
      if (!d.roomTypeId) {
        ElMessage.warning('请选择房型')
        return
      }
    }
    submitLoading.value = true
    try {
      const payload = { ...formData, hotelId: 1 }
      if (isEdit.value) {
        await request.put('/v1/price-plans/' + editId.value, payload)
      } else {
        await request.post('/v1/price-plans', payload)
      }
      ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
      dialogVisible.value = false
      fetchData()
    } catch (e) { console.error(e) } finally { submitLoading.value = false }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该房价码吗？', '提示', { type: 'warning' })
    await request.delete('/v1/price-plans/' + row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleStatus = async (row) => {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    await ElMessageBox.confirm(`确定要${newStatus === 'ACTIVE' ? '启用' : '停用'}该房价码吗？`, '提示', { type: 'warning' })
    await request.put('/v1/price-plans/' + row.id + '/status', null, { params: { status: newStatus } })
    ElMessage.success('操作成功')
    fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleDialogClose = () => { formRef.value?.resetFields() }
const handleSizeChange = (val) => { queryParams.size = val; fetchData() }
const handleCurrentChange = (val) => { queryParams.page = val; fetchData() }
</script>

<style scoped lang="scss">
.price-plan-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  h2 { margin: 0; font-size: 20px; font-weight: 600; }
}
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.detail-tag {
  margin: 0;
}
.no-detail {
  color: #909399;
  font-size: 13px;
}
.detail-row {
  margin-bottom: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
.final-price {
  font-weight: 600;
  color: #e6a23c;
}
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>