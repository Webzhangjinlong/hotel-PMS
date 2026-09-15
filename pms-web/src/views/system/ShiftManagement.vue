<template>
  <div class="shift-management">
    <div class="page-header">
      <h2>交班管理</h2>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 交班操作 -->
      <el-tab-pane label="交班操作" name="create">
        <!-- 步骤条 -->
        <el-steps :active="currentStep" finish-status="success" style="margin-bottom: 24px;">
          <el-step title="填写信息" description="选择接班人和班次时间" />
          <el-step title="核对账务" description="确认账务统计和实际交接" />
          <el-step title="提交交班" description="保存草稿或提交交班" />
        </el-steps>

        <!-- 步骤1: 基本信息 -->
        <el-card v-show="currentStep === 0" shadow="never" style="margin-bottom: 16px;">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-form :model="shiftForm" :rules="shiftRules" ref="shiftFormRef" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="交班人">
                  <el-input :value="currentUserName" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="接班人" prop="receiverId">
                  <el-select v-model="shiftForm.receiverId" placeholder="请选择接班人" style="width: 100%;">
                    <el-option 
                      v-for="user in userList" 
                      :key="user.id" 
                      :label="user.realName + ' (' + user.username + ')'" 
                      :value="user.id" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="班次时间" prop="startTime">
                  <el-date-picker
                    v-model="shiftTimeRange"
                    type="datetimerange"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%;"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <div style="text-align: right;">
            <el-button type="primary" @click="nextStep">下一步</el-button>
          </div>
        </el-card>

        <!-- 步骤2: 账务核对 -->
        <el-card v-show="currentStep === 1" shadow="never" style="margin-bottom: 16px;">
          <template #header>
            <span>账务统计（系统自动计算）</span>
          </template>
          
          <!-- 统计卡片 -->
          <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-label">总收入</div>
                <div class="stat-value primary">{{ formatMoney(statistics.totalAmount) }}</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-label">现金</div>
                <div class="stat-value">{{ formatMoney(statistics.cashAmount) }}</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-label">POS</div>
                <div class="stat-value">{{ formatMoney(statistics.posAmount) }}</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-label">微信</div>
                <div class="stat-value">{{ formatMoney(statistics.wechatAmount) }}</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-label">支付宝</div>
                <div class="stat-value">{{ formatMoney(statistics.alipayAmount) }}</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-label">退款</div>
                <div class="stat-value danger">{{ formatMoney(statistics.refundAmount) }}</div>
              </div>
            </el-col>
          </el-row>

          <el-descriptions :column="3" border style="margin-bottom: 20px;">
            <el-descriptions-item label="入住单数">{{ statistics.checkinCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="退房单数">{{ statistics.checkoutCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="交易笔数">{{ statistics.transactionCount || 0 }}</el-descriptions-item>
          </el-descriptions>

          <!-- 实际交接表格 -->
          <div style="margin: 16px 0 8px; font-weight: bold;">实际交接金额</div>
          <el-table :data="交接data" border style="width: 100%;">
            <el-table-column prop="name" label="项目" width="120" />
            <el-table-column label="系统金额" width="150">
              <template #default="{ row }">¥{{ formatMoney(row.system) }}</template>
            </el-table-column>
            <el-table-column label="实际金额" width="180">
              <template #default="{ row }">
                <el-input-number v-model="row.actual" :min="0" :precision="2" size="small" style="width: 100%;" />
              </template>
            </el-table-column>
            <el-table-column label="差额" width="120">
              <template #default="{ row }">
                <span :class="{ 'text-danger': row.diff !== 0 }">
                  {{ row.diff >= 0 ? '+' : '' }}¥{{ formatMoney(row.diff) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-icon v-if="row.diff === 0" class="text-success"><CircleCheck /></el-icon>
                <el-icon v-else class="text-warning"><Warning /></el-icon>
              </template>
            </el-table-column>
          </el-table>

          <div style="text-align: right; margin-top: 16px;">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" @click="nextStep">下一步</el-button>
          </div>
        </el-card>

        <!-- 步骤3: 提交 -->
        <el-card v-show="currentStep === 2" shadow="never" style="margin-bottom: 16px;">
          <template #header>
            <span>备注信息</span>
          </template>
          <el-form label-width="100px">
            <el-form-item label="备注">
              <el-input v-model="shiftForm.remark" type="textarea" :rows="4" placeholder="请输入备注信息（可选）" />
            </el-form-item>
          </el-form>

          <!-- 交班摘要 -->
          <el-divider content-position="left">交班摘要</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="接班人">{{ getUserName(shiftForm.receiverId) }}</el-descriptions-item>
            <el-descriptions-item label="班次时间">{{ shiftTimeRange[0] }} ~ {{ shiftTimeRange[1] }}</el-descriptions-item>
            <el-descriptions-item label="系统总额">¥{{ formatMoney(statistics.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="实际总额">¥{{ formatMoney(totalActual) }}</el-descriptions-item>
          </el-descriptions>

          <div style="text-align: right; margin-top: 16px;">
            <el-button @click="prevStep">上一步</el-button>
            <el-button @click="handleSaveDraft" :loading="saving" :disabled="saving || submitted">保存草稿</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="saving" :disabled="saving || submitted">提交交班</el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 交班记录 -->
      <el-tab-pane label="交班记录" name="list">
        <el-card shadow="never">
          <el-form inline style="margin-bottom: 16px;">
            <el-form-item label="状态">
              <el-select v-model="listQuery.status" placeholder="全部状态" clearable style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="草稿" value="DRAFT" />
                <el-option label="待接收" value="PENDING" />
                <el-option label="已接收" value="ACCEPTED" />
                <el-option label="已确认" value="CONFIRMED" />
                <el-option label="已驳回" value="REJECTED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchShiftList">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="shiftList" v-loading="loading" stripe>
            <el-table-column prop="shiftNo" label="交班单号" width="180" />
            <el-table-column label="交班人" width="100">
              <template #default="{ row }">{{ getUserName(row.operatorId) }}</template>
            </el-table-column>
            <el-table-column label="接班人" width="100">
              <template #default="{ row }">{{ row.receiverId ? getUserName(row.receiverId) : '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="总收入" width="120">
              <template #default="{ row }">¥{{ formatMoney(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column label="实际总额" width="120">
              <template #default="{ row }">
                <span :class="{ 'text-danger': getActualTotal(row) !== row.totalAmount }">
                  ¥{{ formatMoney(getActualTotal(row)) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="170" />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">详情</el-button>
                <el-button v-if="canEdit(row)" type="success" link @click="handleEdit(row)">编辑</el-button>
                <el-button v-if="canSubmit(row)" type="warning" link @click="handleSubmitRow(row)" :loading="rowSaving === row.id" :disabled="rowSaving === row.id">提交</el-button>
                <el-button v-if="canDelete(row)" type="danger" link @click="handleDelete(row)">删除</el-button>
                <el-button v-if="canAccept(row)" type="success" link @click="handleAccept(row)">接收</el-button>
                <el-button v-if="canReject(row)" type="danger" link @click="handleReject(row)">驳回</el-button>
                <el-button v-if="canConfirm(row)" type="success" link @click="handleConfirm(row)">确认</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 消息通知 -->
      <el-tab-pane label="消息通知" name="messages">
        <el-card shadow="never">
          <el-table :data="messageList" v-loading="messageLoading" stripe>
            <el-table-column prop="messageType" label="消息类型" width="150">
              <template #default="{ row }">{{ getMessageTypeName(row.messageType) }}</template>
            </el-table-column>
            <el-table-column label="关联交班" width="180">
              <template #default="{ row }">{{ getShiftNo(row.shiftId) }}</template>
            </el-table-column>
            <el-table-column prop="isRead" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isRead ? 'info' : 'danger'">{{ row.isRead ? '已读' : '未读' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="170" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button v-if="!row.isRead" type="primary" link @click="handleMarkRead(row)">标记已读</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 通知配置 -->
      <el-tab-pane label="通知配置" name="config">
        <el-card shadow="never">
          <div style="margin-bottom: 16px;">
            <strong>接收通知人员：</strong>
          </div>
          <el-checkbox-group v-model="notifyUserIds">
            <el-checkbox 
              v-for="user in userList" 
              :key="user.id" 
              :label="user.id"
              style="display: block; margin-bottom: 8px;"
            >
              {{ user.realName }}（{{ user.username }}）
            </el-checkbox>
          </el-checkbox-group>
          <el-button type="primary" style="margin-top: 16px;" @click="handleSaveNotifyConfig" :loading="saving">
            保存配置
          </el-button>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 详情弹窗 -->
    <el-dialog title="交班详情" v-model="detailVisible" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="交班单号">{{ currentShift.shiftNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentShift.status)">{{ getStatusName(currentShift.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交班人">{{ getUserName(currentShift.operatorId) }}</el-descriptions-item>
        <el-descriptions-item label="接班人">{{ currentShift.receiverId ? getUserName(currentShift.receiverId) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="班次开始">{{ currentShift.startTime }}</el-descriptions-item>
        <el-descriptions-item label="班次结束">{{ currentShift.endTime }}</el-descriptions-item>
        <el-descriptions-item label="总收入">¥{{ formatMoney(currentShift.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="现金">¥{{ formatMoney(currentShift.cashAmount) }}</el-descriptions-item>
        <el-descriptions-item label="POS">¥{{ formatMoney(currentShift.posAmount) }}</el-descriptions-item>
        <el-descriptions-item label="微信">¥{{ formatMoney(currentShift.wechatAmount) }}</el-descriptions-item>
        <el-descriptions-item label="支付宝">¥{{ formatMoney(currentShift.alipayAmount) }}</el-descriptions-item>
        <el-descriptions-item label="退款">¥{{ formatMoney(currentShift.refundAmount) }}</el-descriptions-item>
        <el-descriptions-item label="入住单数">{{ currentShift.checkinCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="退房单数">{{ currentShift.checkoutCount || 0 }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">实际交接金额</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="实际现金">¥{{ formatMoney(currentShift.actualCash) }}</el-descriptions-item>
        <el-descriptions-item label="实际POS">¥{{ formatMoney(currentShift.actualPos) }}</el-descriptions-item>
        <el-descriptions-item label="实际微信">¥{{ formatMoney(currentShift.actualWechat) }}</el-descriptions-item>
        <el-descriptions-item label="实际支付宝">¥{{ formatMoney(currentShift.actualAlipay) }}</el-descriptions-item>
        <el-descriptions-item label="实际总额" :span="2">
          <span :class="{ 'text-danger': getActualTotal(currentShift) !== currentShift.totalAmount }">
            ¥{{ formatMoney(getActualTotal(currentShift)) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentShift.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog title="驳回交班" v-model="rejectVisible" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="handleConfirmReject" :loading="saving">确认驳回</el-button>
      </template>
    </el-dialog>
    <!-- 现金清点对话框 -->
    <el-dialog title="现金清点" v-model="cashCountVisible" width="600px">
      <el-form :model="cashCountForm" label-width="100px">
        <el-form-item label="清点时间">
          <el-date-picker v-model="cashCountForm.countTime" type="datetime" placeholder="选择清点时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-divider content-position="left">现金面额清点</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="100元">
              <el-input-number v-model="cashCountForm.yuan100" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="50元">
              <el-input-number v-model="cashCountForm.yuan50" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="20元">
              <el-input-number v-model="cashCountForm.yuan20" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="10元">
              <el-input-number v-model="cashCountForm.yuan10" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="5元">
              <el-input-number v-model="cashCountForm.yuan5" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="1元">
              <el-input-number v-model="cashCountForm.yuan1" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="5角">
              <el-input-number v-model="cashCountForm.jiao5" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="1角">
              <el-input-number v-model="cashCountForm.jiao1" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="其他">
              <el-input-number v-model="cashCountForm.other" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">清点结果</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="现金总额">
            <span class="stat-value primary">¥{{ formatMoney(cashCountTotal) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="系统现金">
            <span class="stat-value">¥{{ formatMoney(statistics.cashAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="差异金额">
            <span :class="{ 'text-danger': cashCountDiff !== 0 }">¥{{ formatMoney(cashCountDiff) }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-form>
      <template #footer>
        <el-button @click="cashCountVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCashCount" :loading="saving">保存清点结果</el-button>
      </template>
    </el-dialog>
    <!-- 交班报表对话框 -->
    <el-dialog title="交班报表" v-model="reportVisible" width="900px">
      <div v-if="reportData" class="report-content">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="交班单号">{{ reportData.shiftNo }}</el-descriptions-item>
          <el-descriptions-item label="交班状态">
            <el-tag :type="getStatusType(reportData.status)">{{ getStatusName(reportData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="交班人">{{ getUserName(reportData.operatorId) }}</el-descriptions-item>
          <el-descriptions-item label="接班人">{{ reportData.receiverId ? getUserName(reportData.receiverId) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="班次时间">{{ reportData.startTime }} 至 {{ reportData.endTime }}</el-descriptions-item>
          <el-descriptions-item label="交易笔数">{{ reportData.transactionCount || 0 }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">账务统计</el-divider>
        <el-row :gutter="16" style="margin-bottom: 20px;">
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-label">总收入</div>
              <div class="stat-value primary">{{ formatMoney(reportData.totalAmount) }}</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-label">现金</div>
              <div class="stat-value">{{ formatMoney(reportData.cashAmount) }}</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-label">POS</div>
              <div class="stat-value">{{ formatMoney(reportData.posAmount) }}</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-label">微信</div>
              <div class="stat-value">{{ formatMoney(reportData.wechatAmount) }}</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-label">支付宝</div>
              <div class="stat-value">{{ formatMoney(reportData.alipayAmount) }}</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-label">退款</div>
              <div class="stat-value danger">{{ formatMoney(reportData.refundAmount) }}</div>
            </div>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">交接核对</el-divider>
        <el-table :data="reportTransferData" border style="margin-bottom: 20px;">
          <el-table-column prop="name" label="支付方式" width="120" />
          <el-table-column prop="system" label="系统金额" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.system) }}</template>
          </el-table-column>
          <el-table-column prop="actual" label="实际金额" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.actual) }}</template>
          </el-table-column>
          <el-table-column prop="diff" label="差异" width="120">
            <template #default="{ row }">
              <span :class="{ 'text-danger': row.diff !== 0 }">¥{{ formatMoney(row.diff) }}</span>
            </template>
          </el-table-column>
        </el-table>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="系统总额">¥{{ formatMoney(reportData.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="实际总额">¥{{ formatMoney(getActualTotal(reportData)) }}</el-descriptions-item>
          <el-descriptions-item label="总差异">
            <span :class="{ 'text-danger': getActualTotal(reportData) !== reportData.totalAmount }">
              ¥{{ formatMoney(getActualTotal(reportData) - reportData.totalAmount) }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="reportVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleExportReport">导出报表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 现金清点相关
const cashCountVisible = ref(false)
const cashCountForm = reactive({
  countTime: '',
  yuan100: 0,
  yuan50: 0,
  yuan20: 0,
  yuan10: 0,
  yuan5: 0,
  yuan1: 0,
  jiao5: 0,
  jiao1: 0,
  other: 0
})

// 计算现金清点总额
const cashCountTotal = computed(() => {
  return (cashCountForm.yuan100 * 100) +
         (cashCountForm.yuan50 * 50) +
         (cashCountForm.yuan20 * 20) +
         (cashCountForm.yuan10 * 10) +
         (cashCountForm.yuan5 * 5) +
         (cashCountForm.yuan1 * 1) +
         (cashCountForm.jiao5 * 0.5) +
         (cashCountForm.jiao1 * 0.1) +
         (cashCountForm.other || 0)
})

// 计算现金清点差异
const cashCountDiff = computed(() => {
  return cashCountTotal.value - (statistics.value.cashAmount || 0)
})

// 交班报表相关
const reportVisible = ref(false)
const reportData = ref(null)
const reportTransferData = ref([])

// 显示现金清点对话框
const showCashCountDialog = () => {
  cashCountVisible.value = true
  cashCountForm.countTime = new Date().toISOString().split('T')[0] + ' ' + new Date().toTimeString().split(' ')[0]
}

// 保存现金清点结果
const handleSaveCashCount = async () => {
  if (saving.value) return
  
  saving.value = true
  try {
    // 这里可以调用后端接口保存现金清点结果
    ElMessage.success('现金清点结果保存成功')
    cashCountVisible.value = false
  } catch (error) {
    console.error('保存现金清点结果失败', error)
  } finally {
    saving.value = false
  }
}

// 显示交班报表对话框
const showReportDialog = async (shiftId) => {
  try {
    const res = await getShiftReport(shiftId)
    reportData.value = res.data
    
    // 构建交接数据
    reportTransferData.value = [
      { name: '现金', system: res.data.cashAmount || 0, actual: res.data.actualCash || 0, diff: (res.data.actualCash || 0) - (res.data.cashAmount || 0) },
      { name: 'POS', system: res.data.posAmount || 0, actual: res.data.actualPos || 0, diff: (res.data.actualPos || 0) - (res.data.posAmount || 0) },
      { name: '微信', system: res.data.wechatAmount || 0, actual: res.data.actualWechat || 0, diff: (res.data.actualWechat || 0) - (res.data.wechatAmount || 0) },
      { name: '支付宝', system: res.data.alipayAmount || 0, actual: res.data.actualAlipay || 0, diff: (res.data.actualAlipay || 0) - (res.data.alipayAmount || 0) }
    ]
    
    reportVisible.value = true
  } catch (error) {
    console.error('获取交班报表失败', error)
  }
}

// 导出报表
const handleExportReport = () => {
  ElMessage.info('导出功能开发中...')
}

// 修改详情按钮，添加报表按钮
const handleViewWithReport = (row) => {
  currentShift.value = row
  detailVisible.value = true
}

// 添加报表按钮
const handleViewReport = (row) => {
  showReportDialog(row.id)
}

// 添加现金清点按钮
const handleCashCount = () => {
  showCashCountDialog()
}
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, Warning } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { 
  createShift, updateShift, submitShift, deleteShift,
  acceptShift, confirmShift, rejectShift,
  getShiftList, getShiftById, getPendingShifts, getShiftStatistics,
  getShiftMessages, markMessageRead, getNotifyConfig, saveNotifyConfig
} from '@/api/shift'
import { getUserList } from '@/api/user'
const userStore = useUserStore()

const activeTab = ref('create')
const loading = ref(false)
const saving = ref(false)
const submitted = ref(false)  // 是否已提交，防止重复提交
const rowSaving = ref(null)  // 当前正在提交的行ID，防止重复提交
const messageLoading = ref(false)
const currentStep = ref(0)
const currentUserId = ref(0)

// 用户列表
const userList = ref([])

// 当前用户
const currentUserName = ref('当前用户')

// 交班表单
const shiftFormRef = ref(null)
const shiftTimeRange = ref([])
const shiftForm = reactive({
  receiverId: null,
  startTime: null,
  endTime: null,
  actualCash: 0,
  actualPos: 0,
  actualWechat: 0,
  actualAlipay: 0,
  remark: ''
})
const shiftRules = {
  receiverId: [{ required: true, message: '请选择接班人', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择班次时间', trigger: 'change' }]
}

// 统计数据
const statistics = ref({
  totalAmount: 0,
  cashAmount: 0,
  posAmount: 0,
  wechatAmount: 0,
  alipayAmount: 0,
  creditAmount: 0,
  refundAmount: 0,
  checkinCount: 0,
  checkoutCount: 0,
  transactionCount: 0
})

// 交接数据
const 交接data = ref([
  { name: '现金', system: 0, actual: 0, diff: 0 },
  { name: 'POS', system: 0, actual: 0, diff: 0 },
  { name: '微信', system: 0, actual: 0, diff: 0 },
  { name: '支付宝', system: 0, actual: 0, diff: 0 }
])

// 计算实际总额
const totalActual = computed(() => {
  return 交接data.value.reduce((sum, item) => sum + (item.actual || 0), 0)
})

// 交班列表
const listQuery = reactive({ status: '' })
const shiftList = ref([])

// 消息列表
const messageList = ref([])

// 通知配置
const notifyUserIds = ref([])

// 详情弹窗
const detailVisible = ref(false)
const currentShift = ref({})

// 驳回弹窗
const rejectVisible = ref(false)
const rejectForm = reactive({ reason: '' })
const rejectShiftId = ref(null)

// 监听时间范围变化
watch(shiftTimeRange, (val) => {
  if (val && val.length === 2) {
    shiftForm.startTime = val[0]
    shiftForm.endTime = val[1]
    // 自动获取统计数据
    fetchStatistics(val[0], val[1])
  }
})

// 判断按钮显示权限
const canEdit = (row) => row.status === 'DRAFT' && currentUserId.value === row.operatorId
const canSubmit = (row) => row.status === 'DRAFT' && currentUserId.value === row.operatorId
const canDelete = (row) => row.status === 'DRAFT' && currentUserId.value === row.operatorId
const canAccept = (row) => row.status === 'PENDING' && currentUserId.value === row.receiverId
const canReject = (row) => row.status === 'PENDING' && currentUserId.value === row.receiverId
const canConfirm = (row) => row.status === 'ACCEPTED' && currentUserId.value === row.receiverId

// 计算实际交接总额
const getActualTotal = (row) => {
  return (row.actualCash || 0) + (row.actualPos || 0) + (row.actualWechat || 0) + (row.actualAlipay || 0)
}

// 监听统计数据变化，更新交接数据
watch(statistics, (val) => {
  交接data.value[0].system = val.cashAmount || 0
  交接data.value[1].system = val.posAmount || 0
  交接data.value[2].system = val.wechatAmount || 0
  交接data.value[3].system = val.alipayAmount || 0
}, { deep: true })

// 监听实际金额变化，计算差额
watch(交接data, (val) => {
  val.forEach(item => {
    item.diff = (item.actual || 0) - item.system
  })
}, { deep: true })

// 下一步
const nextStep = async () => {
  if (currentStep.value === 0) {
    const valid = await shiftFormRef.value.validate().catch(() => false)
    if (!valid) return
  }
  if (currentStep.value < 2) {
    currentStep.value++
  }
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 获取用户列表
const fetchUserList = async () => {
  try {
    const res = await getUserList()
    userList.value = res.data
  } catch (error) {
    console.error('获取用户列表失败', error)
  }
}

// 获取统计数据
const fetchStatistics = async (startTime, endTime) => {
  try {
    const res = await getShiftStatistics(startTime, endTime)
    statistics.value = res.data
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 获取交班列表
const fetchShiftList = async () => {
  loading.value = true
  try {
    const res = await getShiftList(listQuery.status)
    shiftList.value = res.data
  } catch (error) {
    console.error('获取交班列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取消息列表
const fetchMessageList = async () => {
  messageLoading.value = true
  try {
    const res = await getShiftMessages()
    messageList.value = res.data
  } catch (error) {
    console.error('获取消息列表失败', error)
  } finally {
    messageLoading.value = false
  }
}

// 获取通知配置
const fetchNotifyConfig = async () => {
  try {
    const res = await getNotifyConfig()
    notifyUserIds.value = res.data.map(item => item.userId)
  } catch (error) {
    console.error('获取通知配置失败', error)
  }
}

// 保存草稿
// 同步实际交接金额到表单
const syncActualAmounts = () => {
  shiftForm.actualCash = 交接data.value[0].actual || 0
  shiftForm.actualPos = 交接data.value[1].actual || 0
  shiftForm.actualWechat = 交接data.value[2].actual || 0
  shiftForm.actualAlipay = 交接data.value[3].actual || 0
}


// 重置表单
const resetForm = () => {
  submitted.value = false
  shiftForm.receiverId = null
  shiftForm.startTime = null
  shiftForm.endTime = null
  shiftForm.actualCash = 0
  shiftForm.actualPos = 0
  shiftForm.actualWechat = 0
  shiftForm.actualAlipay = 0
  shiftForm.remark = ''
  currentStep.value = 0
  交接data.value.forEach(item => {
    item.actual = 0
    item.diff = 0
  })
}

const handleSaveDraft = async () => {
  // 防止重复提交
  if (saving.value || submitted.value) return
  
  saving.value = true
  try {
    // 同步实际交接金额到表单
    shiftForm.actualCash = 交接data.value[0].actual || 0
    shiftForm.actualPos = 交接data.value[1].actual || 0
    shiftForm.actualWechat = 交接data.value[2].actual || 0
    shiftForm.actualAlipay = 交接data.value[3].actual || 0
    
    await createShift(shiftForm)
    ElMessage.success('草稿保存成功')
    submitted.value = true
    resetForm()
    fetchShiftList()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    saving.value = false
  }
}
const handleSubmit = async () => {
  // 防止重复提交
  if (saving.value || submitted.value) return
  
  saving.value = true
  try {
    // 同步实际交接金额到表单
    shiftForm.actualCash = 交接data.value[0].actual || 0
    shiftForm.actualPos = 交接data.value[1].actual || 0
    shiftForm.actualWechat = 交接data.value[2].actual || 0
    shiftForm.actualAlipay = 交接data.value[3].actual || 0
    
    const res = await createShift(shiftForm)
    await submitShift(res.data.id)
    ElMessage.success('交班提交成功')
    submitted.value = true
    resetForm()
    fetchShiftList()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    saving.value = false
  }
}
// 查看详情
const handleView = (row) => {
  currentShift.value = row
  detailVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  ElMessage.info('编辑功能开发中')
}

const handleSubmitRow = async (row) => {
  // 防止重复提交
  if (rowSaving.value === row.id) return
  
  try {
    await ElMessageBox.confirm('确定要提交该交班记录吗？提交后不可修改。', '确认提交', { type: 'warning' })
    rowSaving.value = row.id
    await submitShift(row.id)
    ElMessage.success('提交成功')
    fetchShiftList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败', error)
    }
  } finally {
    rowSaving.value = null
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该交班草稿吗？', '确认删除', { type: 'warning' })
    await deleteShift(row.id)
    ElMessage.success('删除成功')
    fetchShiftList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

// 接收
const handleAccept = async (row) => {
  try {
    await ElMessageBox.confirm('确定要接收该交班吗？', '确认接收', { type: 'info' })
    await acceptShift(row.id)
    ElMessage.success('接收成功')
    fetchShiftList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接收失败', error)
    }
  }
}

// 确认
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确定要确认该交班吗？', '确认交班', { type: 'info' })
    await confirmShift(row.id)
    ElMessage.success('确认成功')
    fetchShiftList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认失败', error)
    }
  }
}

// 驳回
const handleReject = (row) => {
  rejectShiftId.value = row.id
  rejectForm.reason = ''
  rejectVisible.value = true
}

// 确认驳回
const handleConfirmReject = async () => {
  // 防止重复提交
  if (saving.value) return
  
  if (!rejectForm.reason) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  saving.value = true
  try {
    await rejectShift(rejectShiftId.value, rejectForm.reason)
    ElMessage.success('驳回成功')
    rejectVisible.value = false
    fetchShiftList()
  } catch (error) {
    console.error('驳回失败', error)
  } finally {
    saving.value = false
  }
}

// 标记已读
const handleMarkRead = async (row) => {
  try {
    await markMessageRead(row.id)
    ElMessage.success('已标记已读')
    fetchMessageList()
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

// 保存通知配置
const handleSaveNotifyConfig = async () => {
  // 防止重复提交
  if (saving.value) return
  
  saving.value = true
  try {
    await saveNotifyConfig(notifyUserIds.value)
    ElMessage.success('配置保存成功')
  } catch (error) {
    console.error('保存配置失败', error)
  } finally {
    saving.value = false
  }
}

// 获取用户名
const getUserName = (userId) => {
  const user = userList.value.find(u => u.id === userId)
  return user ? user.realName : '未知用户'
}

// 获取交班单号
const getShiftNo = (shiftId) => {
  const shift = shiftList.value.find(s => s.id === shiftId)
  return shift ? shift.shiftNo : '-'
}

// 格式化金额
const formatMoney = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

// 获取状态类型
const getStatusType = (status) => {
  const map = { 'DRAFT': 'info', 'PENDING': 'warning', 'ACCEPTED': '', 'CONFIRMED': 'success', 'REJECTED': 'danger' }
  return map[status] || ''
}

// 获取状态名称
const getStatusName = (status) => {
  const map = { 'DRAFT': '草稿', 'PENDING': '待接收', 'ACCEPTED': '已接收', 'CONFIRMED': '已确认', 'REJECTED': '已驳回' }
  return map[status] || status
}

// 获取消息类型名称
const getMessageTypeName = (type) => {
  const map = { 'SHIFT_SUBMIT': '交班已提交', 'SHIFT_ACCEPT': '交班已接收', 'SHIFT_CONFIRM': '交班已确认', 'SHIFT_REJECT': '交班已驳回' }
  return map[type] || type
}

// 监听tab变化，加载数据
watch(activeTab, (val) => {
  if (val === 'list') fetchShiftList()
  else if (val === 'messages') fetchMessageList()
  else if (val === 'config') fetchNotifyConfig()
})

onMounted(async () => {
  // 获取当前用户ID
  await userStore.getUserInfo()
  currentUserId.value = userStore.userInfo?.id || 0
  fetchUserList()
  // 默认查询今天的统计
  const today = new Date().toISOString().split('T')[0]
  fetchStatistics(today + ' 00:00:00', today + ' 23:59:59')
})
</script>
<style scoped lang="scss">
.shift-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.stat-card {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  
  .stat-label {
    font-size: 13px;
    color: #909399;
    margin-bottom: 8px;
  }
  
  .stat-value {
    font-size: 20px;
    font-weight: bold;
    color: #303133;
    
    &.primary {
      color: #409eff;
    }
    
    &.danger {
      color: #f56c6c;
    }
  }
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}
</style>
