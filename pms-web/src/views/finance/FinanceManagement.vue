<template>
  <div class="finance-management">
    <div class="page-header">
      <h2>财务管理</h2>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab">
      <!-- 交易记录 -->
      <el-tab-pane label="交易记录" name="transactions">
        <el-card shadow="never">
          <el-form :model="transactionQuery" inline>
            <el-form-item label="交易类型">
              <el-select v-model="transactionQuery.type" placeholder="全部类型" clearable style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="押金" value="DEPOSIT" />
                <el-option label="房费" value="ROOM_FEE" />
                <el-option label="杂费" value="EXTRA" />
                <el-option label="付款" value="PAYMENT" />
                <el-option label="退款" value="REFUND" />
                <el-option label="冲账" value="REVERSAL" />
              </el-select>
            </el-form-item>
            <el-form-item label="支付方式">
              <el-select v-model="transactionQuery.paymentMethod" placeholder="全部方式" clearable style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="现金" value="CASH" />
                <el-option label="微信" value="WECHAT" />
                <el-option label="支付宝" value="ALIPAY" />
                <el-option label="刷卡" value="POS" />
                <el-option label="银行转账" value="BANK_TRANSFER" />
                <el-option label="挂账" value="CREDIT" />
              </el-select>
            </el-form-item>
            <el-form-item label="交易号">
              <el-input v-model="transactionQuery.transactionNo" placeholder="请输入交易号" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="transactionDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchTransactions">查询</el-button>
              <el-button @click="resetTransactionQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="transactions" v-loading="transactionLoading" stripe>
            <el-table-column prop="transactionNo" label="交易号" width="180" />
            <el-table-column prop="stayNo" label="入住单号" width="150">
              <template #default="{ row }">
                {{ row.stayNo || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="typeName" label="交易类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getTransactionTypeTag(row.type)">
                  {{ row.typeName }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">
                <span :style="{ color: row.type === 'REFUND' || row.type === 'REVERSAL' ? '#f56c6c' : '#67c23a' }">
                  {{ row.type === 'REFUND' || row.type === 'REVERSAL' ? '-' : '' }}¥{{ (row.amount || 0).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethodName" label="支付方式" width="100" />
            <el-table-column prop="creditCompanyName" label="挂账公司" width="120">
              <template #default="{ row }">
                {{ row.creditCompanyName || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="150" />
            <el-table-column prop="createdAt" label="交易时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="transactionQuery.page"
              v-model:page-size="transactionQuery.size"
              :page-sizes="[10, 20, 50]"
              :total="transactionTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchTransactions"
              @current-change="fetchTransactions"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 挂账管理 -->
      <el-tab-pane label="挂账管理" name="credit">
        <el-card shadow="never">
          <div style="margin-bottom: 16px;">
            <el-button type="success" @click="showCreditCompanyDialog()">新增挂账公司</el-button>
          </div>

          <el-table :data="creditCompanies" v-loading="creditLoading" stripe>
            <el-table-column prop="companyName" label="公司名称" width="200" />
            <el-table-column prop="contactName" label="联系人" width="120" />
            <el-table-column prop="contactPhone" label="联系电话" width="150" />
            <el-table-column prop="creditLimit" label="信用额度" width="120">
              <template #default="{ row }">
                ¥{{ (row.creditLimit || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="currentBalance" label="挂账余额" width="120">
              <template #default="{ row }">
                <span :style="{ color: row.currentBalance > 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
                  ¥{{ (row.currentBalance || 0).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="statusName" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
                  {{ row.statusName }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button type="warning" link size="small" @click="showCreditCompanyDialog(row)">编辑</el-button>
                <el-button type="info" link size="small" @click="viewCreditTransactions(row)">明细</el-button>
                <el-button 
                  v-if="row.currentBalance > 0" 
                  type="success" 
                  link 
                  size="small" 
                  @click="showSettleDialog(row)"
                >结算</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="creditQuery.page"
              v-model:page-size="creditQuery.size"
              :page-sizes="[10, 20, 50]"
              :total="creditTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchCreditCompanies"
              @current-change="fetchCreditCompanies"
            />
          </div>
        </el-card>
      </el-tab-pane>

            <!-- 预付款管理 -->
      <el-tab-pane label="预付款管理" name="prepayment">
        <el-card shadow="never">
          <!-- 查询条件 -->
          <el-form :model="prepaymentQuery" inline style="margin-bottom: 16px;">
            <el-form-item label="预订ID">
              <el-input v-model="prepaymentQuery.reservationId" placeholder="散客预订ID" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="团队预订ID">
              <el-input v-model="prepaymentQuery.teamReservationId" placeholder="团队预订ID" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="prepaymentQuery.status" placeholder="全部状态" clearable style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="已付" value="PAID" />
                <el-option label="已转入" value="TRANSFERRED" />
                <el-option label="已退款" value="REFUNDED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchPrepayments">查询</el-button>
              <el-button @click="resetPrepaymentQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 预付列表 -->
          <el-table :data="prepayments" v-loading="prepaymentLoading" stripe>
            <el-table-column prop="id" label="预付ID" width="80" />
            <el-table-column prop="reservationId" label="散客预订ID" width="120">
              <template #default="{ row }">
                {{ row.reservationId || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="teamReservationId" label="团队预订ID" width="120">
              <template #default="{ row }">
                {{ row.teamReservationId || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="prepaymentTypeName" label="预付类型" width="100" />
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">
                ¥{{ (row.amount || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethodName" label="支付方式" width="100" />
            <el-table-column prop="statusName" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getPrepaymentStatusType(row.status)">
                  {{ row.statusName }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="paymentTime" label="支付时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.paymentTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button 
                  v-if="row.status === 'PAID'"
                  type="primary" 
                  size="small" 
                  @click="showTransferDialog(row)"
                >
                  转入住
                </el-button>
                <el-button 
                  v-if="row.status === 'PAID'"
                  type="danger" 
                  size="small" 
                  @click="showRefundDialog(row)"
                >
                  退款
                </el-button>
                <el-button 
                  type="info" 
                  size="small" 
                  @click="viewPrepaymentDetail(row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="prepaymentQuery.page"
              v-model:page-size="prepaymentQuery.size"
              :page-sizes="[10, 20, 50]"
              :total="prepaymentTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchPrepayments"
              @current-change="fetchPrepayments"
            />
          </div>
        </el-card>
      </el-tab-pane><!-- 统计报表 -->
      <el-tab-pane label="统计报表" name="statistics">
          <el-card shadow="never">
            <!-- 筛选条件 -->
            <el-form :model="statisticsQuery" inline style="margin-bottom: 16px;">
              <el-form-item label="日期范围">
                <el-date-picker
                  v-model="statisticsDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  style="width: 240px"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="fetchStatistics">查询</el-button>
                <el-button @click="resetStatisticsQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 核心指标卡片 -->
            <el-row :gutter="16" style="margin-bottom: 20px;">
              <el-col :span="4">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-title">预付总额</div>
                  <div class="stat-value">¥{{ formatMoney(statisticsData?.totalAmount) }}</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-title">预付笔数</div>
                  <div class="stat-value">{{ statisticsData?.totalCount || 0 }}</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-title">已转入金额</div>
                  <div class="stat-value success">¥{{ formatMoney(statisticsData?.transferredAmount) }}</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-title">已退款金额</div>
                  <div class="stat-value danger">¥{{ formatMoney(statisticsData?.refundedAmount) }}</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-title">待处理金额</div>
                  <div class="stat-value warning">¥{{ formatMoney(statisticsData?.pendingAmount) }}</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-title">平均预付金额</div>
                  <div class="stat-value">¥{{ formatMoney(statisticsData?.averageAmount) }}</div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 按日汇总明细表 -->
            <el-card shadow="never" style="margin-bottom: 20px;">
              <template #header>
                <span>按日汇总</span>
              </template>
              <el-table :data="statisticsData?.dailySummaries || []" stripe max-height="300">
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="count" label="笔数" width="80" />
                <el-table-column label="预付金额" width="120">
                  <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
                </el-table-column>
                <el-table-column label="转入金额" width="120">
                  <template #default="{ row }">
                    <span class="success">¥{{ formatMoney(row.transferredAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="退款金额" width="120">
                  <template #default="{ row }">
                    <span class="danger">¥{{ formatMoney(row.refundedAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="待处理金额" width="120">
                  <template #default="{ row }">
                    <span class="warning">¥{{ formatMoney(row.pendingAmount) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>

            <el-row :gutter="20">
              <!-- 按预付类型汇总 -->
              <el-col :span="12">
                <el-card shadow="never">
                  <template #header>
                    <span>按预付类型汇总</span>
                  </template>
                  <el-table :data="statisticsData?.typeSummaries || []" stripe>
                    <el-table-column prop="prepaymentTypeName" label="预付类型" />
                    <el-table-column prop="count" label="笔数" width="80" />
                    <el-table-column label="金额" width="120">
                      <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
                    </el-table-column>
                    <el-table-column label="占比" width="80">
                      <template #default="{ row }">{{ row.percentage }}%</template>
                    </el-table-column>
                  </el-table>
                </el-card>
              </el-col>

              <!-- 按支付方式汇总 -->
              <el-col :span="12">
                <el-card shadow="never">
                  <template #header>
                    <span>按支付方式汇总</span>
                  </template>
                  <el-table :data="statisticsData?.paymentMethodSummaries || []" stripe>
                    <el-table-column prop="paymentMethodName" label="支付方式" />
                    <el-table-column prop="count" label="笔数" width="80" />
                    <el-table-column label="金额" width="120">
                      <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
                    </el-table-column>
                    <el-table-column label="占比" width="80">
                      <template #default="{ row }">{{ row.percentage }}%</template>
                    </el-table-column>
                  </el-table>
                </el-card>
              </el-col>
            </el-row>
          </el-card>
        </el-tab-pane>
    
    <!-- 预付转入住弹窗 -->
    <el-dialog title="预付款转入住" v-model="transferDialogVisible" width="500px">
      <el-form :model="transferForm" :rules="transferRules" ref="transferFormRef" label-width="100px">
        <el-form-item label="预付ID">
          <el-input :value="currentPrepayment.id" disabled />
        </el-form-item>
        <el-form-item label="预付金额">
          <el-input :value="'¥' + (currentPrepayment.amount || 0).toFixed(2)" disabled />
        </el-form-item>
        <el-form-item label="入住单ID" prop="stayId">
          <el-input v-model="transferForm.stayId" placeholder="请输入入住单ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTransfer" :loading="transferLoading">确认转入</el-button>
      </template>
    </el-dialog>

    <!-- 预付退款弹窗 -->
    <el-dialog title="预付款退款" v-model="refundDialogVisible" width="500px">
      <el-form :model="refundForm" :rules="refundRules" ref="refundFormRef" label-width="100px">
        <el-form-item label="预付ID">
          <el-input :value="currentRefundPrepayment.id" disabled />
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input :value="'¥' + (currentRefundPrepayment.amount || 0).toFixed(2)" disabled />
        </el-form-item>
        <el-form-item label="退款方式" prop="refundMethod">
          <el-select v-model="refundForm.refundMethod" placeholder="请选择退款方式">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="原路退回" value="ORIGINAL" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRefund" :loading="refundLoading">确认退款</el-button>
      </template>
    </el-dialog>

    <!-- 预付详情弹窗 -->
    <el-dialog title="预付款详情" v-model="prepaymentDetailVisible" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预付ID">{{ prepaymentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="酒店ID">{{ prepaymentDetail.hotelId }}</el-descriptions-item>
        <el-descriptions-item label="散客预订ID">{{ prepaymentDetail.reservationId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="团队预订ID">{{ prepaymentDetail.teamReservationId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预付类型">{{ prepaymentDetail.prepaymentTypeName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ (prepaymentDetail.amount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ prepaymentDetail.paymentMethodName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ prepaymentDetail.statusName }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatDateTime(prepaymentDetail.paymentTime) }}</el-descriptions-item>
        <el-descriptions-item label="交易ID">{{ prepaymentDetail.transactionId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ prepaymentDetail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="prepaymentDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
</el-tabs>

    <!-- 挂账公司编辑对话框 -->
    <el-dialog
      v-model="creditCompanyDialogVisible"
      :title="creditCompanyForm.id ? '编辑挂账公司' : '新增挂账公司'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="creditCompanyForm" :rules="creditCompanyRules" ref="creditCompanyFormRef" label-width="100px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="creditCompanyForm.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="creditCompanyForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="creditCompanyForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="信用额度" prop="creditLimit">
          <el-input-number v-model="creditCompanyForm.creditLimit" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="creditCompanyForm.status" style="width: 100%">
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="creditCompanyForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="creditCompanyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creditCompanySaving" @click="saveCreditCompany">保存</el-button>
      </template>
    </el-dialog>

    <!-- 挂账结算对话框 -->
    <el-dialog v-model="settleDialogVisible" title="挂账结算" width="400px" :close-on-click-modal="false">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="公司名称">{{ settleCompany.companyName }}</el-descriptions-item>
        <el-descriptions-item label="挂账余额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ (settleCompany.currentBalance || 0).toFixed(2) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-form :model="settleForm" :rules="settleRules" ref="settleFormRef" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="结算金额" prop="amount">
          <el-input-number v-model="settleForm.amount" :min="0.01" :max="settleCompany.currentBalance" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="settleForm.paymentMethod" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="刷卡" value="POS" />
            <el-option label="银行转账" value="BANK_TRANSFER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="settleLoading" @click="handleSettle">确认结算</el-button>
      </template>
    </el-dialog>

    <!-- 挂账明细对话框 -->
    <el-dialog v-model="creditTransactionDialogVisible" title="挂账明细" width="800px">
      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="公司名称">{{ creditTransactionCompany.companyName }}</el-descriptions-item>
        <el-descriptions-item label="挂账余额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ (creditTransactionCompany.currentBalance || 0).toFixed(2) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-table :data="creditTransactions" stripe size="small">
        <el-table-column prop="transactionNo" label="交易号" width="180" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            {{ getTransactionTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ (row.amount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>

import { useUserStore } from '@/stores/user'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { prepay, transferPrepayment, refundPrepayment as refundPrepaymentApi } from '@/api/folio'
const userStore = useUserStore()

// ========== 标签页 ==========
const activeTab = ref('transactions')

// ========== 交易记录相关 ==========
const transactionLoading = ref(false)
const transactions = ref([])
const transactionTotal = ref(0)
const transactionDateRange = ref(null)
const transactionQuery = reactive({
  page: 1,
  size: 10,
  type: '',
  paymentMethod: '',
  transactionNo: '',
  startDate: null,
  endDate: null
})

const fetchTransactions = async () => {
  transactionLoading.value = true
  try {
    const params = { ...transactionQuery }
    if (transactionDateRange.value && transactionDateRange.value.length === 2) {
      params.startDate = transactionDateRange.value[0]
      params.endDate = transactionDateRange.value[1]
    }
    const res = await request.get('/v1/folios/transactions', { params })
    transactions.value = res.data.records
    transactionTotal.value = res.data.total
  } catch (error) {
    console.error('查询交易记录失败', error)
  } finally {
    transactionLoading.value = false
  }
}

const resetTransactionQuery = () => {
  transactionQuery.type = ''
  transactionQuery.paymentMethod = ''
  transactionQuery.transactionNo = ''
  transactionQuery.startDate = null
  transactionQuery.endDate = null
  transactionDateRange.value = null
  transactionQuery.page = 1
  fetchTransactions()
}

const getTransactionTypeTag = (type) => {
  const map = {
    'DEPOSIT': '',
    'ROOM_FEE': 'success',
    'EXTRA': 'warning',
    'PAYMENT': 'success',
    'REFUND': 'danger',
    'REVERSAL': 'info'
  }
  return map[type] || ''
}

const getTransactionTypeLabel = (type) => {
  const map = {
    'DEPOSIT': '押金',
    'ROOM_FEE': '房费',
    'EXTRA': '杂费',
    'PAYMENT': '付款',
    'REFUND': '退款',
    'REVERSAL': '冲账'
  }
  return map[type] || type
}

// ========== 挂账管理相关 ==========
const creditLoading = ref(false)
const creditCompanies = ref([])
const creditTotal = ref(0)
const creditQuery = reactive({
  page: 1,
  size: 10,
  companyName: '',
  status: ''
})

const creditCompanyDialogVisible = ref(false)
const creditCompanySaving = ref(false)
const creditCompanyFormRef = ref(null)
const creditCompanyForm = reactive({
  id: null,
  hotelId: userStore.hotelId,
  companyName: '',
  contactName: '',
  contactPhone: '',
  creditLimit: 0,
  status: 'ACTIVE',
  remark: ''
})
const creditCompanyRules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
}

const settleDialogVisible = ref(false)
const settleLoading = ref(false)
const settleFormRef = ref(null)
const settleCompany = ref({})
const settleForm = reactive({
  amount: 0,
  paymentMethod: 'CASH'
})
const settleRules = {
  amount: [{ required: true, message: '请输入结算金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const creditTransactionDialogVisible = ref(false)
const creditTransactionCompany = ref({})
const creditTransactions = ref([])

const fetchCreditCompanies = async () => {
  creditLoading.value = true
  try {
    const res = await request.get('/v1/credit-companies', { params: creditQuery })
    creditCompanies.value = res.data.records
    creditTotal.value = res.data.total
  } catch (error) {
    console.error('查询挂账公司失败', error)
  } finally {
    creditLoading.value = false
  }
}

const showCreditCompanyDialog = (row = null) => {
  if (row) {
    Object.assign(creditCompanyForm, {
      id: row.id,
      hotelId: row.hotelId,
      companyName: row.companyName,
      contactName: row.contactName,
      contactPhone: row.contactPhone,
      creditLimit: row.creditLimit,
      status: row.status,
      remark: row.remark
    })
  } else {
    Object.assign(creditCompanyForm, {
      id: null,
      hotelId: userStore.hotelId,
      companyName: '',
      contactName: '',
      contactPhone: '',
      creditLimit: 0,
      status: 'ACTIVE',
      remark: ''
    })
  }
  creditCompanyDialogVisible.value = true
}

const saveCreditCompany = async () => {
  const valid = await creditCompanyFormRef.value.validate().catch(() => false)
  if (!valid) return

  creditCompanySaving.value = true
  try {
    if (creditCompanyForm.id) {
      await request.put(`/v1/credit-companies/${creditCompanyForm.id}`, creditCompanyForm)
    } else {
      await request.post('/v1/credit-companies', creditCompanyForm)
    }
    ElMessage.success('保存成功')
    creditCompanyDialogVisible.value = false
    fetchCreditCompanies()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    creditCompanySaving.value = false
  }
}

const showSettleDialog = (row) => {
  settleCompany.value = row
  settleForm.amount = row.currentBalance
  settleForm.paymentMethod = 'CASH'
  settleDialogVisible.value = true
}

const handleSettle = async () => {
  const valid = await settleFormRef.value.validate().catch(() => false)
  if (!valid) return

  settleLoading.value = true
  try {
    await request.post(`/v1/credit-companies/${settleCompany.value.id}/settle`, null, {
      params: {
        amount: settleForm.amount,
        paymentMethod: settleForm.paymentMethod
      }
    })
    ElMessage.success('结算成功')
    settleDialogVisible.value = false
    fetchCreditCompanies()
  } catch (error) {
    console.error('结算失败', error)
  } finally {
    settleLoading.value = false
  }
}

const viewCreditTransactions = async (row) => {
  creditTransactionCompany.value = row
  try {
    const res = await request.get(`/v1/credit-companies/${row.id}/transactions`)
    creditTransactions.value = res.data
    creditTransactionDialogVisible.value = true
  } catch (error) {
    console.error('查询挂账明细失败', error)
  }
}

// ========== 通用方法 ==========
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// ========== 初始化 ==========

// ========== 预付款管理相关 ==========
const prepaymentLoading = ref(false)
const prepayments = ref([])
const prepaymentTotal = ref(0)
const prepaymentQuery = reactive({
  page: 1,
  size: 10,
  reservationId: '',
  teamReservationId: '',
  status: ''
})

// 预付转入住弹窗
const transferDialogVisible = ref(false)
const transferLoading = ref(false)
const transferFormRef = ref(null)
const currentPrepayment = ref({})
const transferForm = reactive({
  stayId: null
})
const transferRules = {
  stayId: [{ required: true, message: '请输入入住单ID', trigger: 'blur' }]
}

// 预付退款弹窗
const refundDialogVisible = ref(false)
const refundLoading = ref(false)
const refundFormRef = ref(null)
const currentRefundPrepayment = ref({})
const refundForm = reactive({
  refundMethod: 'CASH'
})
const refundRules = {
  refundMethod: [{ required: true, message: '请选择退款方式', trigger: 'change' }]
}

// 预付详情弹窗
// ========== 统计报表相关 ==========
const statisticsLoading = ref(false)
const statisticsData = ref({})
const statisticsDateRange = ref([])

const fetchStatistics = async () => {
  statisticsLoading.value = true
  try {
    let params = {}
    if (statisticsDateRange.value && statisticsDateRange.value.length === 2) {
      params.startDate = statisticsDateRange.value[0]
      params.endDate = statisticsDateRange.value[1]
    }
    const res = await request.get('/v1/prepayments/statistics', { params })
    statisticsData.value = res.data
  } catch (error) {
    console.error('查询统计数据失败', error)
  } finally {
    statisticsLoading.value = false
  }
}

const resetStatisticsQuery = () => {
  statisticsDateRange.value = []
  fetchStatistics()
}

const formatMoney = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

const prepaymentDetailVisible = ref(false)
const prepaymentDetail = ref({})

const fetchPrepayments = async () => {
  prepaymentLoading.value = true
  try {
    let url = '/v1/prepayments'
    let params = { ...prepaymentQuery }
    
    // 根据预订ID查询
    if (prepaymentQuery.reservationId) {
      url = `/v1/prepayments/reservation/${prepaymentQuery.reservationId}`
      params = {}
    } else if (prepaymentQuery.teamReservationId) {
      url = `/v1/prepayments/team-reservation/${prepaymentQuery.teamReservationId}`
      params = {}
    }
    
    const res = await request.get(url, { params })
      if (res.data.records) {
        prepayments.value = res.data.records
        prepaymentTotal.value = res.data.total
      } else {
        prepayments.value = res.data
        prepaymentTotal.value = res.data.length
      }
  } catch (error) {
    console.error('查询预付款失败', error)
  } finally {
    prepaymentLoading.value = false
  }
}

const resetPrepaymentQuery = () => {
  prepaymentQuery.reservationId = ''
  prepaymentQuery.teamReservationId = ''
  prepaymentQuery.status = ''
  prepaymentQuery.page = 1
  fetchPrepayments()
}

const getPrepaymentStatusType = (status) => {
  const map = {
    'PAID': 'success',
    'TRANSFERRED': 'info',
    'REFUNDED': 'danger'
  }
  return map[status] || ''
}

const showTransferDialog = (row) => {
  currentPrepayment.value = row
  transferForm.stayId = null
  transferDialogVisible.value = true
}

const handleTransfer = async () => {
  const valid = await transferFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  transferLoading.value = true
  try {
    await transferPrepayment(currentPrepayment.value.id, transferForm.stayId)
    ElMessage.success('预付款转入住成功')
    transferDialogVisible.value = false
    fetchPrepayments()
    fetchStatistics()
  } catch (error) {
    console.error('预付款转入住失败', error)
  } finally {
    transferLoading.value = false
  }
}

const showRefundDialog = (row) => {
  currentRefundPrepayment.value = row
  refundForm.refundMethod = 'CASH'
  refundDialogVisible.value = true
}

const handleRefund = async () => {
  const valid = await refundFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  refundLoading.value = true
  try {
    await refundPrepaymentApi(currentRefundPrepayment.value.id, refundForm.refundMethod)
    ElMessage.success('预付款退款成功')
    refundDialogVisible.value = false
    fetchPrepayments()
    fetchStatistics()
  } catch (error) {
    console.error('预付款退款失败', error)
  } finally {
    refundLoading.value = false
  }
}

const viewPrepaymentDetail = (row) => {
  prepaymentDetail.value = row
  prepaymentDetailVisible.value = true
}
onMounted(() => {
    fetchTransactions()
    fetchCreditCompanies()
    fetchPrepayments()
    fetchStatistics()
  })
</script>

<style scoped lang="scss">


.stat-card {
  text-align: center;
  
  .stat-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    
    &.success {
      color: #67c23a;
    }
    
    &.danger {
      color: #f56c6c;
    }
    
    &.warning {
      color: #e6a23c;
    }
  }
}


.stat-card {
  text-align: center;
  
  .stat-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    
    &.success {
      color: #67c23a;
    }
    
    &.danger {
      color: #f56c6c;
    }
    
    &.warning {
      color: #e6a23c;
    }
  }
}

.page-header {
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>












