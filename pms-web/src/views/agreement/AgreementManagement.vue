<template>
  <div class="agreement-management">
    <div class="page-header">
      <h2>协议单位管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增协议单位
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="公司名称">
          <el-input v-model="queryParams.companyName" placeholder="请输入公司名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="公司类型">
          <el-select v-model="queryParams.companyType" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="企业" value="ENTERPRISE" />
            <el-option label="旅行社" value="TRAVEL_AGENCY" />
            <el-option label="政府" value="GOVERNMENT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="协议状态">
          <el-select v-model="queryParams.agreementStatus" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="有效" value="ACTIVE" />
            <el-option label="过期" value="EXPIRED" />
            <el-option label="终止" value="TERMINATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="协议编号">
          <el-input v-model="queryParams.agreementNo" placeholder="请输入协议编号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 协议单位列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="companyList" v-loading="loading" stripe>
        <el-table-column prop="agreementNo" label="协议编号" width="120" />
        <el-table-column prop="companyName" label="公司名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="companyTypeName" label="公司类型" width="100" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="协议有效期" width="200">
          <template #default="{ row }">
            {{ row.agreementStartDate }} 至 {{ row.agreementEndDate }}
          </template>
        </el-table-column>
        <el-table-column label="协议状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAgreementStatusType(row.agreementStatus)">
              {{ row.agreementStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="信用额度" width="120" align="right">
          <template #default="{ row }">
            ¥{{ formatMoney(row.creditLimit) }}
          </template>
        </el-table-column>
        <el-table-column label="当前余额" width="120" align="right">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.currentBalance > 0 }">
              ¥{{ formatMoney(row.currentBalance) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleViewPrices(row)">协议价</el-button>
            <el-button type="info" link size="small" @click="handleViewTransactions(row)">挂账明细</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
      <el-form :model="companyForm" :rules="companyRules" ref="companyFormRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公司名称" prop="companyName">
              <el-input v-model="companyForm.companyName" placeholder="请输入公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="协议编号" prop="agreementNo">
              <el-input v-model="companyForm.agreementNo" placeholder="请输入协议编号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公司类型" prop="companyType">
              <el-select v-model="companyForm.companyType" placeholder="请选择公司类型" style="width: 100%;">
                <el-option label="企业" value="ENTERPRISE" />
                <el-option label="旅行社" value="TRAVEL_AGENCY" />
                <el-option label="政府" value="GOVERNMENT" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="协议状态" prop="agreementStatus">
              <el-select v-model="companyForm.agreementStatus" placeholder="请选择协议状态" style="width: 100%;">
                <el-option label="有效" value="ACTIVE" />
                <el-option label="过期" value="EXPIRED" />
                <el-option label="终止" value="TERMINATED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="companyForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="companyForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系邮箱">
              <el-input v-model="companyForm.email" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="传真号码">
              <el-input v-model="companyForm.fax" placeholder="请输入传真号码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="协议开始日期" prop="agreementStartDate">
              <el-date-picker v-model="companyForm.agreementStartDate" type="date" placeholder="选择协议开始日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="协议结束日期" prop="agreementEndDate">
              <el-date-picker v-model="companyForm.agreementEndDate" type="date" placeholder="选择协议结束日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="信用额度">
              <el-input-number v-model="companyForm.creditLimit" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="companyForm.status" placeholder="请选择状态" style="width: 100%;">
                <el-option label="活跃" value="ACTIVE" />
                <el-option label="停用" value="INACTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="公司地址">
          <el-input v-model="companyForm.address" placeholder="请输入公司地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="companyForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">确认</el-button>
      </template>
    </el-dialog>

    <!-- 协议价对话框 -->
    <el-dialog title="协议价管理" v-model="priceDialogVisible" width="1000px">
      <div v-if="currentCompany" class="price-header">
        <span>协议单位：{{ currentCompany.companyName }}</span>
        <span>协议编号：{{ currentCompany.agreementNo }}</span>
      </div>
      
      <div class="price-actions" style="margin-bottom: 16px;">
        <el-button type="primary" @click="handleAddPrice">
          <el-icon><Plus /></el-icon>
          新增协议价
        </el-button>
      </div>
      
      <el-table :data="priceList" v-loading="priceLoading" stripe>
        <el-table-column prop="roomTypeName" label="房型" width="150" />
        <el-table-column prop="price" label="协议价格" width="120" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣比例" width="100" align="center">
          <template #default="{ row }">{{ row.discountRate }}%</template>
        </el-table-column>
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            {{ row.startDate }} 至 {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '有效' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditPrice(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeletePrice(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 协议价编辑对话框 -->
    <el-dialog :title="priceDialogTitle" v-model="priceEditDialogVisible" width="600px">
      <el-form :model="priceForm" :rules="priceRules" ref="priceFormRef" label-width="100px">
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="priceForm.roomTypeId" placeholder="请选择房型" style="width: 100%;">
            <el-option 
              v-for="roomType in roomTypeList" 
              :key="roomType.id" 
              :label="roomType.name" 
              :value="roomType.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="协议价格" prop="price">
          <el-input-number v-model="priceForm.price" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="折扣比例">
          <el-input-number v-model="priceForm.discountRate" :min="0" :max="100" :precision="2" style="width: 100%;" />
          <span class="form-tip">100表示原价，90表示9折</span>
        </el-form-item>
        <el-form-item label="有效期" prop="startDate">
          <el-date-picker v-model="priceDateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="priceForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="有效" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="priceForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="priceEditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePriceSubmit" :loading="saving">确认</el-button>
      </template>
    </el-dialog>

    <!-- 挂账明细对话框 -->
    <el-dialog title="挂账明细" v-model="transactionDialogVisible" width="900px">
      <div v-if="currentCompany" class="transaction-header">
        <span>协议单位：{{ currentCompany.companyName }}</span>
        <span>当前余额：¥{{ formatMoney(currentCompany.currentBalance) }}</span>
      </div>
      <el-table :data="transactionList" v-loading="transactionLoading" stripe>
        <el-table-column prop="transactionNo" label="交易号" width="180" />
        <el-table-column prop="type" label="交易类型" width="100">
          <template #default="{ row }">
            {{ getTypeName(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span :class="{ 'text-success': row.amount > 0, 'text-danger': row.amount < 0 }">
              ¥{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="交易时间" width="170" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getCreditCompanyList, getCreditCompanyById, createCreditCompany, 
  updateCreditCompany, deleteCreditCompany, getCreditTransactions,
  getAgreementPrices, createAgreementPrice, updateAgreementPrice, 
  deleteAgreementPrice
} from '@/api/credit'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  companyName: '',
  companyType: '',
  agreementStatus: '',
  agreementNo: ''
})

// 列表数据
const companyList = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增协议单位')
const companyFormRef = ref(null)
const companyForm = reactive({
  id: null,
  companyName: '',
  agreementNo: '',
  companyType: 'ENTERPRISE',
  agreementStatus: 'ACTIVE',
  contactName: '',
  contactPhone: '',
  email: '',
  fax: '',
  agreementStartDate: '',
  agreementEndDate: '',
  creditLimit: 0,
  status: 'ACTIVE',
  address: '',
  remark: ''
})
const companyRules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  companyType: [{ required: true, message: '请选择公司类型', trigger: 'change' }],
  agreementStatus: [{ required: true, message: '请选择协议状态', trigger: 'change' }],
  agreementStartDate: [{ required: true, message: '请选择协议开始日期', trigger: 'change' }],
  agreementEndDate: [{ required: true, message: '请选择协议结束日期', trigger: 'change' }]
}

// 协议价相关
const priceDialogVisible = ref(false)
const priceEditDialogVisible = ref(false)
const priceDialogTitle = ref('新增协议价')
const priceFormRef = ref(null)
const currentCompany = ref(null)
const priceList = ref([])
const priceLoading = ref(false)
const roomTypeList = ref([])
const priceDateRange = ref([])
const priceForm = reactive({
  id: null,
  roomTypeId: null,
  price: 0,
  discountRate: 100,
  startDate: '',
  endDate: '',
  status: 'ACTIVE',
  remark: ''
})
const priceRules = {
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  price: [{ required: true, message: '请输入协议价格', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

// 挂账明细相关
const transactionDialogVisible = ref(false)
const transactionList = ref([])
const transactionLoading = ref(false)

// 保存状态
const saving = ref(false)

/**
 * 加载协议单位列表
 */
const loadCompanyList = async () => {
  loading.value = true
  try {
    const response = await getCreditCompanyList(queryParams)
    if (response.code === 200) {
      companyList.value = response.data.list
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询协议单位列表失败:', error)
    ElMessage.error('查询协议单位列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  queryParams.page = 1
  loadCompanyList()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  queryParams.companyName = ''
  queryParams.companyType = ''
  queryParams.agreementStatus = ''
  queryParams.agreementNo = ''
  handleSearch()
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
  queryParams.size = size
  loadCompanyList()
}

/**
 * 当前页变化
 */
const handleCurrentChange = (page) => {
  queryParams.page = page
  loadCompanyList()
}

/**
 * 新增协议单位
 */
const handleAdd = () => {
  dialogTitle.value = '新增协议单位'
  resetCompanyForm()
  dialogVisible.value = true
}

/**
 * 编辑协议单位
 */
const handleEdit = (row) => {
  dialogTitle.value = '编辑协议单位'
  Object.assign(companyForm, row)
  dialogVisible.value = true
}

/**
 * 删除协议单位
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该协议单位吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteCreditCompany(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadCompanyList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除协议单位失败:', error)
      ElMessage.error('删除协议单位失败')
    }
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  const valid = await companyFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    let response
    if (companyForm.id) {
      response = await updateCreditCompany(companyForm.id, companyForm)
    } else {
      response = await createCreditCompany(companyForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(companyForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadCompanyList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

/**
 * 重置表单
 */
const resetCompanyForm = () => {
  companyForm.id = null
  companyForm.companyName = ''
  companyForm.agreementNo = ''
  companyForm.companyType = 'ENTERPRISE'
  companyForm.agreementStatus = 'ACTIVE'
  companyForm.contactName = ''
  companyForm.contactPhone = ''
  companyForm.email = ''
  companyForm.fax = ''
  companyForm.agreementStartDate = ''
  companyForm.agreementEndDate = ''
  companyForm.creditLimit = 0
  companyForm.status = 'ACTIVE'
  companyForm.address = ''
  companyForm.remark = ''
}

/**
 * 查看协议价
 */
const handleViewPrices = async (row) => {
  currentCompany.value = row
  priceDialogVisible.value = true
  await loadPriceList(row.id)
}

/**
 * 加载协议价列表
 */
const loadPriceList = async (companyId) => {
  priceLoading.value = true
  try {
    const response = await getAgreementPrices(companyId)
    if (response.code === 200) {
      priceList.value = response.data
    } else {
      ElMessage.error(response.message || '查询协议价失败')
    }
  } catch (error) {
    console.error('查询协议价失败:', error)
    ElMessage.error('查询协议价失败')
  } finally {
    priceLoading.value = false
  }
}

/**
 * 新增协议价
 */
const handleAddPrice = () => {
  priceDialogTitle.value = '新增协议价'
  resetPriceForm()
  priceEditDialogVisible.value = true
}

/**
 * 编辑协议价
 */
const handleEditPrice = (row) => {
  priceDialogTitle.value = '编辑协议价'
  Object.assign(priceForm, row)
  priceDateRange.value = [row.startDate, row.endDate]
  priceEditDialogVisible.value = true
}

/**
 * 删除协议价
 */
const handleDeletePrice = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该协议价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteAgreementPrice(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadPriceList(currentCompany.value.id)
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除协议价失败:', error)
      ElMessage.error('删除协议价失败')
    }
  }
}

/**
 * 提交协议价
 */
const handlePriceSubmit = async () => {
  const valid = await priceFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (priceDateRange.value && priceDateRange.value.length === 2) {
    priceForm.startDate = priceDateRange.value[0]
    priceForm.endDate = priceDateRange.value[1]
  }
  
  saving.value = true
  try {
    let response
    if (priceForm.id) {
      response = await updateAgreementPrice(priceForm.id, priceForm)
    } else {
      response = await createAgreementPrice(currentCompany.value.id, priceForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(priceForm.id ? '更新成功' : '创建成功')
      priceEditDialogVisible.value = false
      loadPriceList(currentCompany.value.id)
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

/**
 * 重置协议价表单
 */
const resetPriceForm = () => {
  priceForm.id = null
  priceForm.roomTypeId = null
  priceForm.price = 0
  priceForm.discountRate = 100
  priceForm.startDate = ''
  priceForm.endDate = ''
  priceForm.status = 'ACTIVE'
  priceForm.remark = ''
  priceDateRange.value = []
}

/**
 * 查看挂账明细
 */
const handleViewTransactions = async (row) => {
  currentCompany.value = row
  transactionDialogVisible.value = true
  transactionLoading.value = true
  try {
    const response = await getCreditTransactions(row.id)
    if (response.code === 200) {
      transactionList.value = response.data
    } else {
      ElMessage.error(response.message || '查询挂账明细失败')
    }
  } catch (error) {
    console.error('查询挂账明细失败:', error)
    ElMessage.error('查询挂账明细失败')
  } finally {
    transactionLoading.value = false
  }
}

/**
 * 获取协议状态类型
 */
const getAgreementStatusType = (status) => {
  const map = { 'ACTIVE': 'success', 'EXPIRED': 'warning', 'TERMINATED': 'danger' }
  return map[status] || 'info'
}

/**
 * 获取交易类型名称
 */
const getTypeName = (type) => {
  const map = {
    'ROOM_FEE': '房费',
    'DEPOSIT': '押金',
    'CONSUMPTION': '消费',
    'REFUND': '退款',
    'REVERSAL': '冲账',
    'SETTLEMENT': '结算'
  }
  return map[type] || type
}

/**
 * 格式化金额
 */
const formatMoney = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

// 初始化加载数据
onMounted(() => {
  loadCompanyList()
})
</script>

<style scoped lang="scss">
.agreement-management {
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

.price-header,
.transaction-header {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
</style>
