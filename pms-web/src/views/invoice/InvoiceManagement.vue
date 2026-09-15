<template>
  <div class="invoice-management">
    <div class="page-header">
      <h2>发票管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          登记发票
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="发票号码">
          <el-input v-model="queryParams.invoiceNo" placeholder="请输入发票号码" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="发票类型">
          <el-select v-model="queryParams.invoiceType" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="普通发票" value="NORMAL" />
            <el-option label="增值税专用发票" value="SPECIAL" />
            <el-option label="电子发票" value="ELECTRONIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="发票状态">
          <el-select v-model="queryParams.invoiceStatus" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="正常" value="NORMAL" />
            <el-option label="已作废" value="VOID" />
            <el-option label="已红冲" value="RED" />
          </el-select>
        </el-form-item>
        <el-form-item label="购买方名称">
          <el-input v-model="queryParams.buyerName" placeholder="请输入购买方名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="开票日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 发票列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="invoiceList" v-loading="loading" stripe>
        <el-table-column prop="invoiceNo" label="发票号码" width="150" />
        <el-table-column prop="invoiceTypeName" label="发票类型" width="120" />
        <el-table-column prop="invoiceStatusName" label="发票状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.invoiceStatus)">
              {{ row.invoiceStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="buyerName" label="购买方" min-width="200" show-overflow-tooltip />
        <el-table-column prop="invoiceDate" label="开票日期" width="120" />
        <el-table-column label="金额（不含税）" width="120" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="税额" width="100" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.taxAmount) }}</template>
        </el-table-column>
        <el-table-column label="价税合计" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-highlight">¥{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="guestName" label="客人姓名" width="100" />
        <el-table-column prop="stayNo" label="入住单号" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.invoiceStatus === 'NORMAL'" type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-dropdown v-if="row.invoiceStatus === 'NORMAL'" trigger="click" @command="(cmd) => handleCommand(cmd, row)">
              <el-button type="primary" link size="small">
                更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="void">作废</el-dropdown-item>
                  <el-dropdown-item command="red">红冲</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-if="row.invoiceStatus !== 'NORMAL'" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="1000px" :close-on-click-modal="false">
      <el-form :model="invoiceForm" :rules="invoiceRules" ref="invoiceFormRef" label-width="120px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="发票号码" prop="invoiceNo">
                  <el-input v-model="invoiceForm.invoiceNo" placeholder="请输入发票号码" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="发票代码">
                  <el-input v-model="invoiceForm.invoiceCode" placeholder="请输入发票代码" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="发票类型" prop="invoiceType">
                  <el-select v-model="invoiceForm.invoiceType" placeholder="请选择发票类型" style="width: 100%;">
                    <el-option label="普通发票" value="NORMAL" />
                    <el-option label="增值税专用发票" value="SPECIAL" />
                    <el-option label="电子发票" value="ELECTRONIC" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="开票日期" prop="invoiceDate">
                  <el-date-picker v-model="invoiceForm.invoiceDate" type="date" placeholder="选择开票日期" value-format="YYYY-MM-DD" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="关联入住单">
                  <el-input v-model="invoiceForm.stayNo" placeholder="请输入入住单号" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="客人姓名">
                  <el-input v-model="invoiceForm.guestName" placeholder="请输入客人姓名" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="客人电话">
                  <el-input v-model="invoiceForm.guestPhone" placeholder="请输入客人电话" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          
          <el-tab-pane label="购买方信息" name="buyer">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="购买方名称" prop="buyerName">
                  <el-input v-model="invoiceForm.buyerName" placeholder="请输入购买方名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="纳税人识别号">
                  <el-input v-model="invoiceForm.buyerTaxNo" placeholder="请输入纳税人识别号" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="地址电话">
                  <el-input v-model="invoiceForm.buyerAddress" placeholder="请输入地址" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="电话">
                  <el-input v-model="invoiceForm.buyerPhone" placeholder="请输入电话" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开户行">
                  <el-input v-model="invoiceForm.buyerBank" placeholder="请输入开户行" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="银行账号">
                  <el-input v-model="invoiceForm.buyerBankAccount" placeholder="请输入银行账号" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          
          <el-tab-pane label="销售方信息" name="seller">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="销售方名称">
                  <el-input v-model="invoiceForm.sellerName" placeholder="请输入销售方名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="纳税人识别号">
                  <el-input v-model="invoiceForm.sellerTaxNo" placeholder="请输入纳税人识别号" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="地址电话">
                  <el-input v-model="invoiceForm.sellerAddress" placeholder="请输入地址" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="电话">
                  <el-input v-model="invoiceForm.sellerPhone" placeholder="请输入电话" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开户行">
                  <el-input v-model="invoiceForm.sellerBank" placeholder="请输入开户行" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="银行账号">
                  <el-input v-model="invoiceForm.sellerBankAccount" placeholder="请输入银行账号" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          
          <el-tab-pane label="商品明细" name="items">
            <div class="items-header">
              <el-button type="primary" size="small" @click="addItem">
                <el-icon><Plus /></el-icon>
                添加明细
              </el-button>
            </div>
            <el-table :data="invoiceForm.items" border style="margin-top: 10px;">
              <el-table-column label="商品/服务名称" min-width="200">
                <template #default="{ row }">
                  <el-input v-model="row.itemName" placeholder="请输入名称" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="规格型号" width="120">
                <template #default="{ row }">
                  <el-input v-model="row.itemSpec" placeholder="规格型号" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="单位" width="80">
                <template #default="{ row }">
                  <el-input v-model="row.itemUnit" placeholder="单位" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="数量" width="100">
                <template #default="{ row }">
                  <el-input-number v-model="row.itemQuantity" :min="0" :precision="2" size="small" style="width: 100%;" />
                </template>
              </el-table-column>
              <el-table-column label="单价" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.itemPrice" :min="0" :precision="2" size="small" style="width: 100%;" />
                </template>
              </el-table-column>
              <el-table-column label="税率(%)" width="100">
                <template #default="{ row }">
                  <el-input-number v-model="row.taxRate" :min="0" :max="100" :precision="2" size="small" style="width: 100%;" />
                </template>
              </el-table-column>
              <el-table-column label="金额" width="120">
                <template #default="{ row }">
                  ¥{{ calculateAmount(row) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" @click="removeItem($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="items-total">
              <span>金额合计：¥{{ calculateTotalAmount() }}</span>
              <span>税额合计：¥{{ calculateTotalTax() }}</span>
              <span>价税合计：¥{{ calculateGrandTotal() }}</span>
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <el-form-item label="备注" style="margin-top: 20px;">
          <el-input v-model="invoiceForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="发票详情" v-model="detailVisible" width="900px">
      <div v-if="currentInvoice" class="invoice-detail">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="发票号码">{{ currentInvoice.invoiceNo }}</el-descriptions-item>
          <el-descriptions-item label="发票代码">{{ currentInvoice.invoiceCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发票类型">{{ currentInvoice.invoiceTypeName }}</el-descriptions-item>
          <el-descriptions-item label="发票状态">
            <el-tag :type="getStatusType(currentInvoice.invoiceStatus)">
              {{ currentInvoice.invoiceStatusName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开票日期">{{ currentInvoice.invoiceDate }}</el-descriptions-item>
          <el-descriptions-item label="价税合计">
            <span class="amount-highlight">¥{{ formatMoney(currentInvoice.totalAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="购买方" :span="2">{{ currentInvoice.buyerName }}</el-descriptions-item>
          <el-descriptions-item label="客人姓名">{{ currentInvoice.guestName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入住单号">{{ currentInvoice.stayNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentInvoice.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        
        <h4>发票明细</h4>
        <el-table :data="currentInvoice.items" border style="margin-bottom: 20px;">
          <el-table-column prop="itemName" label="商品/服务名称" min-width="200" />
          <el-table-column prop="itemSpec" label="规格型号" width="120" />
          <el-table-column prop="itemUnit" label="单位" width="80" />
          <el-table-column prop="itemQuantity" label="数量" width="100" />
          <el-table-column prop="itemPrice" label="单价" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.itemPrice) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column prop="taxRate" label="税率" width="100">
            <template #default="{ row }">{{ row.taxRate }}%</template>
          </el-table-column>
          <el-table-column prop="taxAmount" label="税额" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.taxAmount) }}</template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="价税合计" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.totalAmount) }}</template>
          </el-table-column>
        </el-table>
        
        <el-descriptions :column="3" border>
          <el-descriptions-item label="金额合计">¥{{ formatMoney(currentInvoice.amount) }}</el-descriptions-item>
          <el-descriptions-item label="税额合计">¥{{ formatMoney(currentInvoice.taxAmount) }}</el-descriptions-item>
          <el-descriptions-item label="价税合计">
            <span class="amount-highlight">¥{{ formatMoney(currentInvoice.totalAmount) }}</span>
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="currentInvoice.invoiceStatus !== 'NORMAL'" style="margin-top: 20px;">
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="currentInvoice.invoiceStatus === 'VOID' ? '作废原因' : '红冲原因'">
              {{ currentInvoice.voidReason || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 作废/红冲对话框 -->
    <el-dialog :title="actionDialogTitle" v-model="actionDialogVisible" width="500px">
      <el-form :model="actionForm" label-width="100px">
        <el-form-item :label="actionDialogTitle + '原因'" required>
          <el-input v-model="actionForm.reason" type="textarea" :rows="3" :placeholder="'请输入' + actionDialogTitle + '原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleActionConfirm" :loading="saving">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { 
  getInvoiceList, getInvoiceById, createInvoice, 
  updateInvoice, voidInvoice, redInvoice, deleteInvoice 
} from '@/api/invoice'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  invoiceNo: '',
  invoiceType: '',
  invoiceStatus: '',
  buyerName: '',
  invoiceDateStart: '',
  invoiceDateEnd: ''
})

const dateRange = ref([])

// 列表数据
const invoiceList = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('登记发票')
const invoiceFormRef = ref(null)
const activeTab = ref('basic')
const invoiceForm = reactive({
  id: null,
  invoiceNo: '',
  invoiceCode: '',
  invoiceType: 'NORMAL',
  invoiceDate: '',
  buyerName: '',
  buyerTaxNo: '',
  buyerAddress: '',
  buyerPhone: '',
  buyerBank: '',
  buyerBankAccount: '',
  sellerName: '',
  sellerTaxNo: '',
  sellerAddress: '',
  sellerPhone: '',
  sellerBank: '',
  sellerBankAccount: '',
  stayId: null,
  stayNo: '',
  guestName: '',
  guestPhone: '',
  remark: '',
  items: []
})
const invoiceRules = {
  invoiceNo: [{ required: true, message: '请输入发票号码', trigger: 'blur' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  invoiceDate: [{ required: true, message: '请选择开票日期', trigger: 'change' }],
  buyerName: [{ required: true, message: '请输入购买方名称', trigger: 'blur' }]
}

// 详情相关
const detailVisible = ref(false)
const currentInvoice = ref(null)

// 作废/红冲相关
const actionDialogVisible = ref(false)
const actionDialogTitle = ref('')
const actionForm = reactive({
  id: null,
  reason: ''
})

// 保存状态
const saving = ref(false)

/**
 * 加载发票列表
 */
const loadInvoiceList = async () => {
  loading.value = true
  try {
    const response = await getInvoiceList(queryParams)
    if (response.code === 200) {
      invoiceList.value = response.data.list
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询发票列表失败:', error)
    ElMessage.error('查询发票列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.invoiceDateStart = dateRange.value[0]
    queryParams.invoiceDateEnd = dateRange.value[1]
  } else {
    queryParams.invoiceDateStart = ''
    queryParams.invoiceDateEnd = ''
  }
  queryParams.page = 1
  loadInvoiceList()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  queryParams.invoiceNo = ''
  queryParams.invoiceType = ''
  queryParams.invoiceStatus = ''
  queryParams.buyerName = ''
  queryParams.invoiceDateStart = ''
  queryParams.invoiceDateEnd = ''
  dateRange.value = []
  handleSearch()
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
  queryParams.size = size
  loadInvoiceList()
}

/**
 * 当前页变化
 */
const handleCurrentChange = (page) => {
  queryParams.page = page
  loadInvoiceList()
}

/**
 * 新增发票
 */
const handleAdd = () => {
  dialogTitle.value = '登记发票'
  resetInvoiceForm()
  dialogVisible.value = true
}

/**
 * 编辑发票
 */
const handleEdit = async (row) => {
  dialogTitle.value = '编辑发票'
  try {
    const response = await getInvoiceById(row.id)
    if (response.code === 200) {
      Object.assign(invoiceForm, response.data)
      dialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取发票详情失败')
    }
  } catch (error) {
    console.error('获取发票详情失败:', error)
    ElMessage.error('获取发票详情失败')
  }
}

/**
 * 查看详情
 */
const handleView = async (row) => {
  try {
    const response = await getInvoiceById(row.id)
    if (response.code === 200) {
      currentInvoice.value = response.data
      detailVisible.value = true
    } else {
      ElMessage.error(response.message || '获取发票详情失败')
    }
  } catch (error) {
    console.error('获取发票详情失败:', error)
    ElMessage.error('获取发票详情失败')
  }
}

/**
 * 处理下拉菜单命令
 */
const handleCommand = (command, row) => {
  if (command === 'void') {
    handleVoid(row)
  } else if (command === 'red') {
    handleRed(row)
  }
}

/**
 * 作废发票
 */
const handleVoid = (row) => {
  actionDialogTitle.value = '作废'
  actionForm.id = row.id
  actionForm.reason = ''
  actionDialogVisible.value = true
}

/**
 * 红冲发票
 */
const handleRed = (row) => {
  actionDialogTitle.value = '红冲'
  actionForm.id = row.id
  actionForm.reason = ''
  actionDialogVisible.value = true
}

/**
 * 确认作废/红冲
 */
const handleActionConfirm = async () => {
  if (!actionForm.reason) {
    ElMessage.warning('请输入' + actionDialogTitle.value + '原因')
    return
  }
  
  saving.value = true
  try {
    let response
    if (actionDialogTitle.value === '作废') {
      response = await voidInvoice(actionForm.id, actionForm.reason)
    } else {
      response = await redInvoice(actionForm.id, actionForm.reason)
    }
    
    if (response.code === 200) {
      ElMessage.success(actionDialogTitle.value + '成功')
      actionDialogVisible.value = false
      loadInvoiceList()
    } else {
      ElMessage.error(response.message || actionDialogTitle.value + '失败')
    }
  } catch (error) {
    console.error(actionDialogTitle.value + '失败:', error)
    ElMessage.error(actionDialogTitle.value + '失败')
  } finally {
    saving.value = false
  }
}

/**
 * 删除发票
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该发票吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteInvoice(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadInvoiceList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除发票失败:', error)
      ElMessage.error('删除发票失败')
    }
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  const valid = await invoiceFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (invoiceForm.items.length === 0) {
    ElMessage.warning('请添加发票明细')
    activeTab.value = 'items'
    return
  }
  
  saving.value = true
  try {
    let response
    if (invoiceForm.id) {
      response = await updateInvoice(invoiceForm.id, invoiceForm)
    } else {
      response = await createInvoice(invoiceForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(invoiceForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadInvoiceList()
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
const resetInvoiceForm = () => {
  invoiceForm.id = null
  invoiceForm.invoiceNo = ''
  invoiceForm.invoiceCode = ''
  invoiceForm.invoiceType = 'NORMAL'
  invoiceForm.invoiceDate = ''
  invoiceForm.buyerName = ''
  invoiceForm.buyerTaxNo = ''
  invoiceForm.buyerAddress = ''
  invoiceForm.buyerPhone = ''
  invoiceForm.buyerBank = ''
  invoiceForm.buyerBankAccount = ''
  invoiceForm.sellerName = ''
  invoiceForm.sellerTaxNo = ''
  invoiceForm.sellerAddress = ''
  invoiceForm.sellerPhone = ''
  invoiceForm.sellerBank = ''
  invoiceForm.sellerBankAccount = ''
  invoiceForm.stayId = null
  invoiceForm.stayNo = ''
  invoiceForm.guestName = ''
  invoiceForm.guestPhone = ''
  invoiceForm.remark = ''
  invoiceForm.items = []
  activeTab.value = 'basic'
}

/**
 * 添加发票明细
 */
const addItem = () => {
  invoiceForm.items.push({
    itemName: '',
    itemSpec: '',
    itemUnit: '',
    itemQuantity: 1,
    itemPrice: 0,
    taxRate: 6,
    remark: ''
  })
}

/**
 * 删除发票明细
 */
const removeItem = (index) => {
  invoiceForm.items.splice(index, 1)
}

/**
 * 计算明细金额
 */
const calculateAmount = (row) => {
  const amount = (row.itemPrice || 0) * (row.itemQuantity || 0)
  return formatMoney(amount)
}

/**
 * 计算金额合计
 */
const calculateTotalAmount = () => {
  let total = 0
  invoiceForm.items.forEach(item => {
    total += (item.itemPrice || 0) * (item.itemQuantity || 0)
  })
  return formatMoney(total)
}

/**
 * 计算税额合计
 */
const calculateTotalTax = () => {
  let total = 0
  invoiceForm.items.forEach(item => {
    const amount = (item.itemPrice || 0) * (item.itemQuantity || 0)
    total += amount * (item.taxRate || 0) / 100
  })
  return formatMoney(total)
}

/**
 * 计算价税合计
 */
const calculateGrandTotal = () => {
  let total = 0
  invoiceForm.items.forEach(item => {
    const amount = (item.itemPrice || 0) * (item.itemQuantity || 0)
    const tax = amount * (item.taxRate || 0) / 100
    total += amount + tax
  })
  return formatMoney(total)
}

/**
 * 获取状态类型
 */
const getStatusType = (status) => {
  const map = { 'NORMAL': 'success', 'VOID': 'danger', 'RED': 'warning' }
  return map[status] || 'info'
}

/**
 * 格式化金额
 */
const formatMoney = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

// 监听日期范围变化
watch(dateRange, (val) => {
  if (val && val.length === 2) {
    queryParams.invoiceDateStart = val[0]
    queryParams.invoiceDateEnd = val[1]
  } else {
    queryParams.invoiceDateStart = ''
    queryParams.invoiceDateEnd = ''
  }
})

// 初始化加载数据
onMounted(() => {
  loadInvoiceList()
})
</script>

<style scoped lang="scss">
.invoice-management {
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

.amount-highlight {
  font-weight: bold;
  color: #f56c6c;
}

.items-header {
  margin-bottom: 10px;
}

.items-total {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-weight: bold;
}

.invoice-detail {
  h4 {
    margin: 0 0 10px 0;
    font-size: 16px;
    color: #303133;
  }
}
</style>
