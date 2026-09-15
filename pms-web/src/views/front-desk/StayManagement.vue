<template>
  <div class="stay-management">
    <div class="page-header">
      <h2>入住管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showWalkInDialog">
          <el-icon><Plus /></el-icon>
          散客入住
        </el-button>
        <el-button type="primary" @click="showReservationCheckInDialog">
          <el-icon><Connection /></el-icon>
          预订入住
        </el-button>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="在住" value="CHECKED_IN" />
            <el-option label="已离店" value="CHECKED_OUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="queryParams.roomNo" placeholder="请输入房间号" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="客人姓名">
          <el-input v-model="queryParams.guestName" placeholder="请输入客人姓名" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="type-segment">
      <el-radio-group v-model="queryParams.checkInType" size="large" @change="handleSearch">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="WALK_IN">散客入住</el-radio-button>
        <el-radio-button value="TEAM">团队入住</el-radio-button>
        <el-radio-button value="RESERVATION">预订入住</el-radio-button>
      </el-radio-group>
    </div>


    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="在住列表" name="list">
        <!-- 全部类型表格 -->
        <el-table v-if="!queryParams.checkInType" :data="stayList" v-loading="loading" stripe>
          <el-table-column prop="stayNo" label="入住单" width="140" />
          <el-table-column label="入住类型" width="80">
            <template #default="{ row }">
              <el-tag :type="getCheckInTypeTag(row.checkInType)">{{ getCheckInTypeLabel(row.checkInType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="guestName" label="客人姓名" width="120" />
            <el-table-column label="同住人" width="120">
              <template #default="{ row }">
                <el-popover
                  v-if="row.coGuestCount > 0"
                  placement="left"
                  :width="300"
                  trigger="click"
                >
                  <template #reference>
                    <el-tag type="primary" size="small" style="cursor: pointer;">{{ row.coGuestCount }}人</el-tag>
                  </template>
                  <div>
                    <h4 style="margin: 0 0 10px 0;">同住人信息</h4>
                    <el-table :data="JSON.parse(row.coGuestNames || '[]')" size="small" border>
                      <el-table-column prop="name" label="姓名" />
                    </el-table>
                    <div style="margin-top: 10px; font-size: 12px; color: #909399;">
                      点击"管理同住人"可查看详细信息
                    </div>
                  </div>
                </el-popover>
                <span v-else style="color: #909399;">无</span>
              </template>
            </el-table-column>
          <el-table-column prop="guestPhone" label="电话" width="120" />
          <el-table-column prop="roomTypeName" label="房型" width="120" />
          <el-table-column prop="roomNo" label="房号" width="80" />
          <el-table-column prop="checkInTime" label="入住时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
          </el-table-column>
          <el-table-column prop="checkOutTime" label="预计离店" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkOutTime) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'CHECKED_IN' ? 'success' : 'info'" size="small">{{ row.status === 'CHECKED_IN' ? '在住' : '已离店' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getPaymentStatusType(row)" size="small" effect="light">{{ getPaymentStatusText(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="押金" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.depositAmount > 0" class="amount-value">¥{{ (row.depositAmount || 0).toFixed(2) }}</span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" link size="small" @click="viewStayDetail(row)">详情</el-button>
                <el-dropdown v-if="row.status === 'CHECKED_IN'" trigger="click" @command="(cmd) => handleAction(cmd, row)">
                  <el-button type="primary" link size="small">
                    更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="payment">收款</el-dropdown-item>
                      <el-dropdown-item command="extend">续住</el-dropdown-item>
                      <el-dropdown-item command="changeRoom">换房</el-dropdown-item>
                      <el-dropdown-item command="coGuests">管理同住人</el-dropdown-item>
                      <el-dropdown-item command="checkout" divided>退房</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 散客入住表格 -->
        <el-table v-else-if="queryParams.checkInType === 'WALK_IN'" :data="stayList" v-loading="loading" stripe>
          <el-table-column prop="stayNo" label="入住单" width="140" />
          <el-table-column prop="guestName" label="客人姓名" width="120" />
            <el-table-column label="同住人" width="120">
              <template #default="{ row }">
                <el-popover
                  v-if="row.coGuestCount > 0"
                  placement="left"
                  :width="300"
                  trigger="click"
                >
                  <template #reference>
                    <el-tag type="primary" size="small" style="cursor: pointer;">{{ row.coGuestCount }}人</el-tag>
                  </template>
                  <div>
                    <h4 style="margin: 0 0 10px 0;">同住人信息</h4>
                    <el-table :data="JSON.parse(row.coGuestNames || '[]')" size="small" border>
                      <el-table-column prop="name" label="姓名" />
                    </el-table>
                    <div style="margin-top: 10px; font-size: 12px; color: #909399;">
                      点击"管理同住人"可查看详细信息
                    </div>
                  </div>
                </el-popover>
                <span v-else style="color: #909399;">无</span>
              </template>
            </el-table-column>
          <el-table-column prop="guestPhone" label="电话" width="120" />
          <el-table-column prop="roomTypeName" label="房型" width="120" />
          <el-table-column prop="roomNo" label="房号" width="80" />
          <el-table-column label="房费" width="120" align="right">
            <template #default="{ row }">
              <span class="price-value">¥{{ row.totalAmount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="checkInTime" label="入住时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
          </el-table-column>
          <el-table-column prop="checkOutTime" label="预计离店" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkOutTime) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'CHECKED_IN' ? 'success' : 'info'" size="small">{{ row.status === 'CHECKED_IN' ? '在住' : '已离店' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getPaymentStatusType(row)" size="small" effect="light">{{ getPaymentStatusText(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="押金" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.depositAmount > 0" class="amount-value">¥{{ (row.depositAmount || 0).toFixed(2) }}</span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" link size="small" @click="viewStayDetail(row)">详情</el-button>
                <el-dropdown v-if="row.status === 'CHECKED_IN'" trigger="click" @command="(cmd) => handleAction(cmd, row)">
                  <el-button type="primary" link size="small">
                    更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="payment">收款</el-dropdown-item>
                      <el-dropdown-item command="extend">续住</el-dropdown-item>
                      <el-dropdown-item command="changeRoom">换房</el-dropdown-item>
                      <el-dropdown-item command="coGuests">管理同住人</el-dropdown-item>
                      <el-dropdown-item command="checkout" divided>退房</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 团队入住表格（带展开行） -->
        <el-table v-else-if="queryParams.checkInType === 'TEAM'" :data="teamSummaryList" v-loading="loading" stripe row-key="teamReservationId" :expand-row-keys="expandedTeamIds" @expand-change="handleTeamExpand">
          <el-table-column type="expand" width="50">
            <template #default="{ row }">
              <div class="team-detail-wrapper">
                <el-table :data="row.stays" stripe size="small" class="team-detail-table">
                  <el-table-column prop="stayNo" label="入住单" width="140" />
                  <el-table-column prop="guestName" label="客人姓名" width="120" />
                  <el-table-column prop="guestPhone" label="电话" width="120" />
                  <el-table-column prop="roomTypeName" label="房型" width="120" />
                  <el-table-column prop="roomNo" label="房号" width="80" />
                  <el-table-column label="房费" width="120" align="right">
                    <template #default="{ row: stay }">
                      <span class="price-value">¥{{ stay.totalAmount || 0 }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="checkInTime" label="入住时间" width="170">
                    <template #default="{ row: stay }">{{ formatDateTime(stay.checkInTime) }}</template>
                  </el-table-column>
                  <el-table-column prop="checkOutTime" label="预计离店" width="170">
                    <template #default="{ row: stay }">{{ formatDateTime(stay.checkOutTime) }}</template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row: stay }">
                      <el-tag :type="stay.status === 'CHECKED_IN' ? 'success' : 'info'" size="small">{{ stay.status === 'CHECKED_IN' ? '在住' : '已离店' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="支付状态" width="100" align="center">
                    <template #default="{ row: stay }">
                      <el-tag :type="getPaymentStatusType(stay)" size="small" effect="light">{{ getPaymentStatusText(stay) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row: stay }">
                      <el-button type="primary" link size="small" @click="viewStayDetail(stay)">详情</el-button>
                      <el-button v-if="stay.status === 'CHECKED_IN'" type="success" link size="small" @click="showPaymentDialog(stay)">收款</el-button>
                      <el-button v-if="stay.status === 'CHECKED_IN'" type="warning" link size="small" @click="showExtendDialog(stay)">续住</el-button>
                      <el-button v-if="stay.status === 'CHECKED_IN'" type="danger" link size="small" @click="showCheckOutDialog(stay)">退房</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="teamReservationNo" label="预定单" width="150" />
          <el-table-column prop="teamName" label="团队名称" min-width="150" />
          <el-table-column prop="totalRooms" label="房数" width="80" align="center" />
          <el-table-column label="总金额" width="120" align="right">
            <template #default="{ row }">
              <span class="price-value">¥{{ row.totalAmount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="contactName" label="联系人" width="100" />
          <el-table-column prop="contactPhone" label="联系电话" width="130" />
          <el-table-column label="预订渠道" width="100">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.source || '散客' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="结算方式" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.settlementType === 'UNIFIED' ? 'primary' : 'warning'">{{ row.settlementType === 'UNIFIED' ? '统一结算' : '分开结算' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'CHECKED_IN' ? 'success' : 'info'" size="small">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getTeamPaymentStatusType(row)" size="small" effect="light">{{ getTeamPaymentStatusText(row) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 预订入住表格 -->
        <el-table v-else-if="queryParams.checkInType === 'RESERVATION'" :data="stayList" v-loading="loading" stripe>
          <el-table-column prop="reservationNo" label="预订单" width="150">
            <template #default="{ row }">{{ row.reservationNo || '-' }}</template>
          </el-table-column>
          <el-table-column prop="stayNo" label="入住单" width="140" />
          <el-table-column prop="guestName" label="客人姓名" width="120" />
            <el-table-column label="同住人" width="120">
              <template #default="{ row }">
                <el-popover
                  v-if="row.coGuestCount > 0"
                  placement="left"
                  :width="300"
                  trigger="click"
                >
                  <template #reference>
                    <el-tag type="primary" size="small" style="cursor: pointer;">{{ row.coGuestCount }}人</el-tag>
                  </template>
                  <div>
                    <h4 style="margin: 0 0 10px 0;">同住人信息</h4>
                    <el-table :data="JSON.parse(row.coGuestNames || '[]')" size="small" border>
                      <el-table-column prop="name" label="姓名" />
                    </el-table>
                    <div style="margin-top: 10px; font-size: 12px; color: #909399;">
                      点击"管理同住人"可查看详细信息
                    </div>
                  </div>
                </el-popover>
                <span v-else style="color: #909399;">无</span>
              </template>
            </el-table-column>
          <el-table-column prop="guestPhone" label="电话" width="120" />
          <el-table-column prop="roomTypeName" label="房型" width="120" />
          <el-table-column prop="roomNo" label="房号" width="80" />
          <el-table-column label="房费" width="120" align="right">
            <template #default="{ row }">
              <span class="price-value">¥{{ row.totalAmount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="预订渠道" width="100">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.source || '散客' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="checkInTime" label="入住时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
          </el-table-column>
          <el-table-column prop="checkOutTime" label="预计离店" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkOutTime) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'CHECKED_IN' ? 'success' : 'info'" size="small">{{ row.status === 'CHECKED_IN' ? '在住' : '已离店' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getPaymentStatusType(row)" size="small" effect="light">{{ getPaymentStatusText(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="押金" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.depositAmount > 0" class="amount-value">¥{{ (row.depositAmount || 0).toFixed(2) }}</span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" link size="small" @click="viewStayDetail(row)">详情</el-button>
                <el-dropdown v-if="row.status === 'CHECKED_IN'" trigger="click" @command="(cmd) => handleAction(cmd, row)">
                  <el-button type="primary" link size="small">
                    更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="payment">收款</el-dropdown-item>
                      <el-dropdown-item command="extend">续住</el-dropdown-item>
                      <el-dropdown-item command="changeRoom">换房</el-dropdown-item>
                      <el-dropdown-item command="coGuests">管理同住人</el-dropdown-item>
                      <el-dropdown-item command="checkout" divided>退房</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
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
      </el-tab-pane>

      <el-tab-pane label="今日入住" name="today">
        <el-table :data="todayCheckIns" v-loading="todayLoading" stripe>
          <el-table-column prop="stayNo" label="入住单" width="140" />
          <el-table-column label="入住类型" width="80">
            <template #default="{ row }">
              <el-tag :type="getCheckInTypeTag(row.checkInType)">{{ getCheckInTypeLabel(row.checkInType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="guestName" label="客人姓名" width="120" />
          <el-table-column label="同住人" width="120">
            <template #default="{ row }">
              <span v-if="row.coGuestCount > 0" style="color: #409EFF; cursor: pointer;">{{ row.coGuestCount }}人</span>
              <span v-else style="color: #909399;">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="guestPhone" label="电话" width="120" />
          <el-table-column prop="roomTypeName" label="房型" width="120" />
          <el-table-column prop="roomNo" label="房号" width="80" />
          <el-table-column prop="checkInTime" label="入住时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
          </el-table-column>
          <el-table-column label="预计离店" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkOutTime) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'CHECKED_IN' ? 'success' : 'info'">{{ row.status === 'CHECKED_IN' ? '在住' : '已离店' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getPaymentStatusType(row)">{{ getPaymentStatusText(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="押金" width="120" align="right">
            <template #default="{ row }">{{ row.depositAmount ? '¥' + row.depositAmount.toFixed(2) : '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="viewStayDetail(row)">详情</el-button>
              <el-button v-if="row.status === 'CHECKED_IN'" type="warning" link size="small" @click="showExtendDialog(row)">续住</el-button>
              <el-button v-if="row.status === 'CHECKED_IN'" type="success" link size="small" @click="showPaymentDialog(row)">收款</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="今日离店" name="departures">
        <el-table :data="todayDepartures" v-loading="departuresLoading" stripe>
          <el-table-column prop="stayNo" label="入住单" width="140" />
          <el-table-column label="入住类型" width="80">
            <template #default="{ row }">
              <el-tag :type="getCheckInTypeTag(row.checkInType)">{{ getCheckInTypeLabel(row.checkInType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="guestName" label="客人姓名" width="120" />
          <el-table-column prop="guestPhone" label="电话" width="120" />
          <el-table-column prop="roomTypeName" label="房型" width="120" />
          <el-table-column prop="roomNo" label="房号" width="80" />
          <el-table-column prop="checkInTime" label="入住时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
          </el-table-column>
          <el-table-column label="预计离店" width="170">
            <template #default="{ row }">{{ formatDateTime(row.checkOutTime) }}</template>
          </el-table-column>
          <el-table-column label="支付状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getPaymentStatusType(row)">{{ getPaymentStatusText(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="viewStayDetail(row)">详情</el-button>
              <el-button type="danger" link size="small" @click="handleCheckOut(row)">退房</el-button>
              <el-button type="success" link size="small" @click="showPaymentDialog(row)">收款</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 收款对话框 -->
    <el-dialog v-model="paymentDialogVisible" title="快速收款" width="500px" :close-on-click-modal="false">
      <el-descriptions :column="2" border class="payment-info">
        <el-descriptions-item label="入住单号">{{ paymentStay.stayNo }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ paymentStay.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ paymentStay.guestName }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ paymentStay.roomTypeName }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="folioInfo" class="folio-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总金额"><span class="amount">¥{{ (folioInfo.totalAmount || 0).toFixed(2) }}</span></el-descriptions-item>
          <el-descriptions-item label="已付金额"><span class="amount paid">¥{{ (folioInfo.paidAmount || 0).toFixed(2) }}</span></el-descriptions-item>
          <el-descriptions-item label="待付金额" :span="2"><span class="amount highlight">¥{{ (folioInfo.balance || 0).toFixed(2) }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form :model="paymentForm" :rules="paymentRules" ref="paymentFormRef" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="收款类型" prop="transactionType">
          <el-select v-model="paymentForm.transactionType" style="width: 100%">
            <el-option label="押金" value="DEPOSIT" />
            <el-option label="房费" value="ROOM_FEE" />
            <el-option label="杂费" value="EXTRA" />
            <el-option label="付款" value="PAYMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款金额" prop="amount">
          <el-input-number v-model="paymentForm.amount" :min="0.01" :precision="2" style="width: 100%" placeholder="请输入收款金额" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="paymentForm.paymentMethod" style="width: 100%" @change="handlePaymentMethodChange">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="刷卡" value="POS" />
            <el-option label="银行转账" value="BANK_TRANSFER" />
            <el-option label="挂账" value="CREDIT" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="paymentForm.paymentMethod === 'CREDIT'" label="挂账公司" prop="creditCompanyId">
          <el-select v-model="paymentForm.creditCompanyId" placeholder="请选择挂账公司" style="width: 100%">
            <el-option v-for="company in creditCompanies" :key="company.id" :label="company.companyName" :value="company.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="paymentForm.remark" type="textarea" :rows="2" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paymentLoading" @click="submitPayment">确认收款</el-button>
      </template>
    </el-dialog>

    
    <!-- 加床/杂费对话框 -->
    <el-dialog v-model="extraChargeDialogVisible" title="加床/杂费" width="500px" :close-on-click-modal="false">
      <el-descriptions :column="2" border class="payment-info">
        <el-descriptions-item label="入住单号">{{ extraChargeStay.stayNo }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ extraChargeStay.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ extraChargeStay.guestName }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ extraChargeStay.roomTypeName }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="extraChargeForm" :rules="extraChargeRules" ref="extraChargeFormRef" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="费用类型" prop="chargeType">
          <el-radio-group v-model="extraChargeForm.chargeType">
            <el-radio label="BED">加床费</el-radio>
            <el-radio label="EXTRA">杂费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="费用描述" prop="description">
          <el-input v-model="extraChargeForm.description" placeholder="请输入费用描述，如：加床费、洗衣费等" />
        </el-form-item>
        <el-form-item label="费用金额" prop="amount">
          <el-input-number v-model="extraChargeForm.amount" :min="0.01" :precision="2" style="width: 100%" placeholder="请输入费用金额" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="extraChargeForm.paymentMethod" style="width: 100%" @change="handleExtraChargePaymentMethodChange">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="刷卡" value="POS" />
            <el-option label="银行转账" value="BANK_TRANSFER" />
            <el-option label="挂账" value="CREDIT" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="extraChargeForm.paymentMethod === 'CREDIT'" label="挂账公司" prop="creditCompanyId">
          <el-select v-model="extraChargeForm.creditCompanyId" placeholder="请选择挂账公司" style="width: 100%">
            <el-option v-for="company in creditCompanies" :key="company.id" :label="company.companyName" :value="company.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="extraChargeForm.remark" type="textarea" :rows="2" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="extraChargeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="extraChargeLoading" @click="submitExtraCharge">确认入账</el-button>
      </template>
    </el-dialog>
<!-- 散客入住对话框 -->
    <el-dialog v-model="walkInDialogVisible" title="散客入住" width="600px" :close-on-click-modal="false">
      <el-form :model="walkInForm" :rules="walkInRules" ref="walkInFormRef" label-width="100px">
        <el-divider content-position="left">房间信息</el-divider>
        <el-form-item label="选择房型" prop="roomTypeId">
          <el-select v-model="walkInForm.roomTypeId" placeholder="请选择房型" style="width: 100%" @change="handleRoomTypeChange">
            <el-option v-for="rt in roomTypeOptions" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择房间" prop="roomId">
          <el-select v-model="walkInForm.roomId" placeholder="请选择房间" style="width: 100%" :disabled="!walkInForm.roomTypeId">
            <el-option v-for="room in availableRooms" :key="room.id" :label="room.roomNo + ' (' + room.roomTypeName + ')'" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-divider content-position="left">客人信息</el-divider>
        <el-form-item label="客人姓名" prop="guestName"><el-input v-model="walkInForm.guestName" placeholder="请输入客人姓名" /></el-form-item>
        <el-form-item label="客人电话" prop="guestPhone"><el-input v-model="walkInForm.guestPhone" placeholder="请输入客人电话" /></el-form-item>
        <el-form-item label="证件号"><el-input v-model="walkInForm.guestIdNo" placeholder="请输入证件号" /></el-form-item>
        <el-form-item label="性别"><el-select v-model="walkInForm.guestGender" placeholder="请选择性别"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item>

        <!-- 同住人区域 -->
        <el-divider content-position="left">
          同住人
          <el-button type="primary" link size="small" @click="addCoGuestRow" style="margin-left: 10px;">
            + 添加同住人
          </el-button>
        </el-divider>
        <div v-for="(cg, idx) in walkInForm.coGuests" :key="idx" style="border: 1px solid #eee; padding: 10px; margin-bottom: 10px; border-radius: 4px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <span style="font-weight: bold;">同住人 {{ idx + 1 }}</span>
            <el-button type="danger" link size="small" @click="removeCoGuestRow(idx)">删除</el-button>
          </div>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="cg.guestName" placeholder="姓名" size="small" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="cg.phone" placeholder="手机号" size="small" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="cg.idNo" placeholder="证件号" size="small" />
            </el-col>
          </el-row>
        </div>

        <el-divider content-position="left">入住信息</el-divider>
        <el-form-item label="房价码">
          <el-select v-model="walkInForm.pricePlanId" placeholder="选择房价码（可选，不选则按房型基础价）" clearable style="width: 100%" @change="handleWalkInPricePlanChange">
            <el-option v-for="plan in pricePlanOptions" :key="plan.id" :label="plan.code + ' - ' + plan.name" :value="plan.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房价码价格"><el-input :model-value="walkInForm.planPrice !== null ? '¥' + walkInForm.planPrice.toFixed(2) : '-'" readonly style="width: 100%" /></el-form-item>
        <el-form-item label="实际单价"><el-input-number v-model="walkInForm.dailyPrice" :min="0" :precision="2" placeholder="选择房价码后自动填充，可修改" style="width: 100%" /></el-form-item>
        <el-form-item label="预计离店" prop="expectedCheckOutDate"><el-date-picker v-model="walkInForm.expectedCheckOutDate" type="date" placeholder="选择预计离店日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="walkInDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitWalkIn">确认入住</el-button>
      </template>
    </el-dialog>


    <!-- 预订入住对话框 -->
    <el-dialog v-model="reservationCheckInVisible" title="预订入住" width="600px" :close-on-click-modal="false">
      <el-form :model="reservationCheckInForm" :rules="reservationCheckInRules" ref="reservationCheckInFormRef" label-width="100px">
        <el-divider content-position="left">预订信息</el-divider>
        <el-descriptions :column="2" border style="margin-bottom: 15px;">
          <el-descriptions-item label="预订房型">{{ reservationCheckInForm.roomTypeName || '未指定' }}</el-descriptions-item>
          <el-descriptions-item label="客人姓名">{{ reservationCheckInForm.guestName }}</el-descriptions-item>
          <el-descriptions-item label="客人电话">{{ reservationCheckInForm.guestPhone }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">分配房间</el-divider>
        <el-form-item label="选择房间" prop="roomId">
          <el-select v-model="reservationCheckInForm.roomId" placeholder="请选择房间" style="width: 100%">
            <el-option v-for="room in reservationAvailableRooms" :key="room.id" :label="room.roomNo + ' (' + room.roomTypeName + ')'" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-divider content-position="left">补充信息</el-divider>
        <el-form-item label="证件号"><el-input v-model="reservationCheckInForm.guestIdNo" placeholder="请输入证件号（选填）" /></el-form-item>
        <el-form-item label="性别"><el-select v-model="reservationCheckInForm.guestGender" placeholder="请选择性别"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item>

        <!-- 同住人区域 -->
        <el-divider content-position="left">
          同住人
          <el-button type="primary" link size="small" @click="addResCoGuestRow" style="margin-left: 10px;">
            + 添加同住人
          </el-button>
        </el-divider>
        <div v-for="(cg, idx) in reservationCheckInForm.coGuests" :key="idx" style="border: 1px solid #eee; padding: 10px; margin-bottom: 10px; border-radius: 4px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <span style="font-weight: bold;">同住人 {{ idx + 1 }}</span>
            <el-button type="danger" link size="small" @click="removeResCoGuestRow(idx)">删除</el-button>
          </div>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="cg.guestName" placeholder="姓名" size="small" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="cg.phone" placeholder="手机号" size="small" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="cg.idNo" placeholder="证件号" size="small" />
            </el-col>
          </el-row>
        </div>

      </el-form>
      <template #footer>
        <el-button @click="reservationCheckInVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitReservationCheckIn">确认入住</el-button>
      </template>
    </el-dialog>

    <!-- 入住详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="入住详情" width="600px">
      <el-descriptions v-if="stayDetail" :column="2" border>
        <el-descriptions-item label="入住单号">{{ stayDetail.stayNo }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ stayDetail.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ stayDetail.roomTypeName }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ stayDetail.guestName }}</el-descriptions-item>
        <el-descriptions-item label="客人电话">{{ stayDetail.guestPhone }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="stayDetail.status === 'CHECKED_IN' ? 'success' : 'info'">{{ stayDetail.status === 'CHECKED_IN' ? '在住' : '已离店' }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatDateTime(stayDetail.checkInTime) }}</el-descriptions-item>
        <el-descriptions-item label="预计离店">{{ formatDateTime(stayDetail.checkOutTime) }}</el-descriptions-item>
        <el-descriptions-item label="关联预订" :span="2">{{ stayDetail.reservationNo || '散客' }}</el-descriptions-item>
      </el-descriptions>

        <!-- 押金信息 -->
        <el-divider content-position="left">押金信息</el-divider>
        <div v-if="stayDeposits.length > 0">
          <el-table :data="stayDeposits" border size="small" style="margin-bottom: 15px;">
            <el-table-column prop="depositNo" label="押金单号" width="140" />
            <el-table-column label="押金金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount-value">¥{{ (row.amount || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="已退金额" width="120" align="right">
              <template #default="{ row }">
                <span v-if="row.refundedAmount > 0" class="refund-value">¥{{ row.refundedAmount.toFixed(2) }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getDepositStatusType(row.status)" size="small">{{ row.statusName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethodName" label="支付方式" width="100" />
            <el-table-column prop="collectedAt" label="收取时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.collectedAt) }}</template>
            </el-table-column>
          </el-table>
          <div style="text-align: right; font-size: 14px; color: #606266;">
            <span>押金总额：<strong class="amount-value">¥{{ totalDepositAmount.toFixed(2) }}</strong></span>
            <span style="margin-left: 20px;">可退押金：<strong class="refund-value">¥{{ refundableDepositAmount.toFixed(2) }}</strong></span>
          </div>
        </div>
        <div v-else style="text-align: center; color: #909399; padding: 20px;">
          暂无押金记录
        </div>
    </el-dialog>

    <!-- 续住对话框 -->

      <!-- 同住人管理对话框 -->
    <el-dialog v-model="coGuestDialogVisible" title="同住人管理" width="700px" :close-on-click-modal="false">
      <div v-if="currentStayForCoGuest" style="margin-bottom: 15px;">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="入住单号">{{ currentStayForCoGuest.stayNo }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ currentStayForCoGuest.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="主客人">{{ currentStayForCoGuest.guestName }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 当前同住人列表 -->
      <div style="margin-bottom: 15px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
          <h4 style="margin: 0;">当前入住客人</h4>
          <el-button type="primary" size="small" @click="showAddCoGuestForm = true">
            <el-icon><Plus /></el-icon> 添加同住人
          </el-button>
        </div>
        <el-table :data="currentCoGuests" border size="small" v-loading="coGuestLoading">
          <el-table-column label="类型" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isPrimary ? 'danger' : 'info'" size="small">{{ row.isPrimary ? '主客人' : '同住人' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="guestName" label="姓名" width="100" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="idNo" label="证件号" width="180" />
          <el-table-column prop="gender" label="性别" width="60" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button v-if="!row.isPrimary" type="danger" link size="small" @click="removeCoGuest(row.id)">删除</el-button>
              <span v-else style="color: #909399; font-size: 12px;">不可删除</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 添加同住人表单 -->
      <el-card v-if="showAddCoGuestForm" shadow="never" style="margin-top: 15px;">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>添加同住人</span>
            <el-button type="info" link size="small" @click="showAddCoGuestForm = false">取消</el-button>
          </div>
        </template>
        <el-form :model="addCoGuestForm" label-width="80px">
          <el-row :gutter="15">
            <el-col :span="8">
              <el-form-item label="姓名" required>
                <el-input v-model="addCoGuestForm.guestName" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="手机号">
                <el-input v-model="addCoGuestForm.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="证件号">
                <el-input v-model="addCoGuestForm.idNo" placeholder="请输入证件号" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="15">
            <el-col :span="8">
              <el-form-item label="性别">
                <el-select v-model="addCoGuestForm.gender" placeholder="请选择" style="width: 100%">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="16" style="text-align: right;">
              <el-button type="primary" @click="submitAddCoGuest" :loading="addCoGuestLoading">确认添加</el-button>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </el-dialog>

    <!-- 换房对话框 -->
      <el-dialog v-model="changeRoomDialogVisible" title="换房" width="700px" :close-on-click-modal="false">
        <el-form :model="changeRoomForm" ref="changeRoomFormRef" :rules="changeRoomRules" label-width="100px">
          <el-descriptions :column="2" border class="change-room-info" style="margin-bottom: 20px;">
            <el-descriptions-item label="入住单号">{{ changeRoomForm.stayNo }}</el-descriptions-item>
            <el-descriptions-item label="客人姓名">{{ changeRoomForm.guestName }}</el-descriptions-item>
            <el-descriptions-item label="当前房号">{{ changeRoomForm.oldRoomNo }}</el-descriptions-item>
            <el-descriptions-item label="当前房型">{{ changeRoomForm.oldRoomTypeName }}</el-descriptions-item>
            <el-descriptions-item label="入住时间">{{ formatDateTime(changeRoomForm.checkInTime) }}</el-descriptions-item>
            <el-descriptions-item label="预计离店">{{ formatDateTime(changeRoomForm.checkOutTime) }}</el-descriptions-item>
            <el-descriptions-item label="当前总金额">¥{{ (changeRoomForm.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
          </el-descriptions>
          
          <el-form-item label="新房间" prop="newRoomId">
            <el-select v-model="changeRoomForm.newRoomId" filterable placeholder="请选择新房间" style="width: 100%" @change="onNewRoomChange">
              <el-option v-for="room in changeAvailableRooms" :key="room.id" :label="room.roomNo + ' - ' + room.roomTypeName" :value="room.id" />
            </el-select>
          </el-form-item>
          
          <el-form-item v-if="changeRoomForm.newRoomId" label="新房型">
            <el-input :value="changeRoomForm.newRoomTypeName" disabled />
          </el-form-item>
          
          <el-form-item label="换房原因">
            <el-input v-model="changeRoomForm.reason" type="textarea" :rows="3" placeholder="请输入换房原因（可选）" />
          </el-form-item>
          
          <el-alert v-if="changeRoomForm.amountAdjustment !== 0" 
            :title="changeRoomForm.amountAdjustment > 0 ? '需加收费用' : '将退还费用'" 
            :description="'¥' + Math.abs(changeRoomForm.amountAdjustment || 0).toFixed(2)"
            :type="changeRoomForm.amountAdjustment > 0 ? 'warning' : 'success'"
            show-icon style="margin-bottom: 20px;" />
        </el-form>
        <template #footer>
          <el-button @click="changeRoomDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="changeRoomLoading" @click="submitChangeRoom">确认换房</el-button>
        </template>
      </el-dialog>
    <el-dialog v-model="extendDialogVisible" title="续住" width="600px" :close-on-click-modal="false">
      <el-descriptions :column="2" border class="extend-info">
        <el-descriptions-item label="入住单号">{{ extendForm.stayNo }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ extendForm.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ extendForm.roomTypeName }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ extendForm.guestName }}</el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatDateTime(extendForm.checkInTime) }}</el-descriptions-item>
        <el-descriptions-item label="当前离店时间">{{ formatDateTime(extendForm.checkOutTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="extendForm" :rules="extendRules" ref="extendFormRef" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="新离店日期" prop="newCheckOutDate">
          <el-date-picker v-model="extendForm.newCheckOutDate" type="date" placeholder="选择新离店日期" value-format="YYYY-MM-DD" :disabled-date="disabledDate" style="width: 100%" @change="calculateExtendFee" />
        </el-form-item>
      </el-form>
      <div v-if="extendPriceDetails.length > 0" class="price-details">
        <h4>续住期间房价明细</h4>
        <el-table :data="extendPriceDetails" size="small" border>
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="price" label="房价"><template #default="{ row }">¥{{ row.price.toFixed(2) }}</template></el-table-column>
        </el-table>
      </div>
      <div class="fee-summary">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="续住天数">{{ extendSummary.extendedDays || 0 }} 天</el-descriptions-item>
          <el-descriptions-item label="新增费用"><span class="amount">¥{{ (extendSummary.additionalAmount || 0).toFixed(2) }}</span></el-descriptions-item>
          <el-descriptions-item label="原总金额">¥{{ (extendSummary.oldTotalAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="新总金额"><span class="amount highlight">¥{{ (extendSummary.newTotalAmount || 0).toFixed(2) }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="extendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="extendLoading" @click="submitExtend">确认续住</el-button>
      </template>
    </el-dialog>

    <!-- 退房确认对话框 -->
    <el-dialog v-model="checkOutVisible" title="退房确认" width="500px" :close-on-click-modal="false">
      <div v-if="checkOutStay" class="checkout-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="入住单号">{{ checkOutStay.stayNo }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ checkOutStay.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ checkOutStay.roomTypeName }}</el-descriptions-item>
          <el-descriptions-item label="客人姓名">{{ checkOutStay.guestName }}</el-descriptions-item>
          <el-descriptions-item label="入住时间">{{ formatDateTime(checkOutStay.checkInTime) }}</el-descriptions-item>
          <el-descriptions-item label="预计离店">{{ formatDateTime(checkOutStay.checkOutTime) }}</el-descriptions-item>
        </el-descriptions>
        <div class="fee-summary" style="margin-top: 20px;">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总费用"><span class="amount">{{ checkOutStay.totalAmount?.toFixed(2) }} 元</span></el-descriptions-item>
            <el-descriptions-item label="已付金额"><span class="amount">{{ (checkOutStay.paidAmount || 0).toFixed(2) }} 元</span></el-descriptions-item>
            <el-descriptions-item label="待结金额"><span class="amount highlight">{{ ((checkOutStay.totalAmount || 0) - (checkOutStay.paidAmount || 0)).toFixed(2) }} 元</span></el-descriptions-item>
          </el-descriptions>
        </div>
        <el-form-item label="退房备注" style="margin-top: 16px;">
          <el-input v-model="checkOutRemark" type="textarea" :rows="2" placeholder="请输入退房备注（可选）" />
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="checkOutVisible = false">取消</el-button>
        <el-button type="primary" :loading="checkOutLoading" @click="submitCheckOut">确认退房</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getDepositsByStayId } from '@/api/deposit'
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Connection, Calendar, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getStayGuests, getStayList, getStayById, walkInCheckIn, reservationCheckIn, getTodayCheckIns, extendStay, checkOut, getTeamStaySummary, changeRoom, addCoGuest, removeCoGuest as removeCoGuestApi } from '@/api/stay'
import { getFolioByStayId, collectPayment, getCreditCompanyList, addExtraCharge, getExtraChargesByStayId } from '@/api/folio'
import request from '@/utils/request'

const route = useRoute()

const activeTab = ref('list')
const loading = ref(false)
const todayLoading = ref(false)
const stayList = ref([])
const todayCheckIns = ref([])
const todayDepartures = ref([])
const departuresLoading = ref(false)
const total = ref(0)
const dateRange = ref(null)
const queryParams = reactive({ page: 1, size: 10, hotelId: null, status: '', roomNo: '', guestName: '', checkInType: '', checkInDateStart: null, checkInDateEnd: null })

// 团队视图相关
const teamSummaryList = ref([])
const expandedTeamIds = ref([])

const walkInDialogVisible = ref(false)
const walkInFormRef = ref(null)
const submitLoading = ref(false)
const walkInForm = reactive({ roomTypeId: '', roomId: '', guestName: '', guestIdNo: '', guestPhone: '', guestGender: '', expectedCheckOutDate: '', pricePlanId: '', planPrice: null, dailyPrice: null, coGuests: [] })
const walkInRules = {
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  guestName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入客人电话', trigger: 'blur' }],
  expectedCheckOutDate: [{ required: true, message: '请选择预计离店日期', trigger: 'change' }]
}

const reservationCheckInVisible = ref(false)
const reservationCheckInFormRef = ref(null)
const reservationCheckInForm = reactive({ reservationId: '', roomId: '', guestName: '', guestIdNo: '', guestPhone: '', guestGender: '', roomTypeId: '', roomTypeName: '', coGuests: [] })
const reservationCheckInRules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }]
}

const detailDialogVisible = ref(false)
const stayDetail = ref(null)
const stayDetailGuests = ref([])
const stayDeposits = ref([])
const totalDepositAmount = ref(0)
const refundableDepositAmount = ref(0)

// 同住人管理弹窗
const coGuestDialogVisible = ref(false)
const currentStayForCoGuest = ref(null)
const currentCoGuests = ref([])
const coGuestLoading = ref(false)
const showAddCoGuestForm = ref(false)
const addCoGuestLoading = ref(false)
const addCoGuestForm = reactive({ guestName: '', phone: '', idNo: '', gender: '' })

// 房型和房间选项
const roomTypeOptions = ref([])
const availableRooms = ref([])
const pricePlanOptions = ref([])
const reservationAvailableRooms = ref([])

// 收款相关
const paymentDialogVisible = ref(false)
const paymentLoading = ref(false)
const paymentFormRef = ref(null)
const paymentStay = ref({})
const folioInfo = ref(null)
const creditCompanies = ref([])
const paymentForm = reactive({ transactionType: 'DEPOSIT', amount: 0, paymentMethod: 'CASH', creditCompanyId: null, remark: '' })
// 获取挂账公司列表
const fetchCreditCompanies = async () => {
  try {
    const res = await getCreditCompanyList()
    if (res.code === 200) {
      creditCompanies.value = res.data || []
    }
  } catch (e) {
    console.error('获取挂账公司列表失败', e)
  }
}
const paymentRules = {
  transactionType: [{ required: true, message: '请选择收款类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

// 续住相关
const extendDialogVisible = ref(false)
const extendLoading = ref(false)
const extendFormRef = ref(null)
const extendForm = reactive({ stayId: '', stayNo: '', roomNo: '', roomTypeName: '', guestName: '', checkInTime: '', checkOutTime: '', roomId: '', hotelId: '', roomTypeId: '', newCheckOutDate: '' })
const extendRules = { newCheckOutDate: [{ required: true, message: '请选择新离店日期', trigger: 'change' }] }
const extendPriceDetails = ref([])
const extendSummary = reactive({ extendedDays: 0, additionalAmount: 0, oldTotalAmount: 0, newTotalAmount: 0 })

// 退房相关
const checkOutVisible = ref(false)
const checkOutLoading = ref(false)
const checkOutStay = ref(null)
const checkOutRemark = ref('')

const getCheckInTypeTag = (type) => {
  const map = { 'TEAM': 'warning', 'INDIVIDUAL': '', 'WALK_IN': '', 'RESERVATION': 'success' }
  return map[type] || 'info'
}
const getCheckInTypeLabel = (type) => {
  const map = { 'TEAM': '团队', 'INDIVIDUAL': '散客', 'WALK_IN': '散客', 'RESERVATION': '预订' }
  return map[type] || type || '-'
}

// 获取状态标签
const getStatusLabel = (status) => {
  const map = { 'CHECKED_IN': '在住', 'CHECKED_OUT': '已离店', 'PENDING': '待入住', 'CANCELLED': '已取消' }
  return map[status] || status || '-'
}
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const fetchStayList = async () => {
    loading.value = true
    try {
      const params = { ...queryParams }
      if (dateRange.value && dateRange.value.length === 2) {
        params.checkInDateStart = dateRange.value[0]
        params.checkInDateEnd = dateRange.value[1]
      }
      
      // 团队入住使用专门的团队汇总API
      if (queryParams.checkInType === 'TEAM') {
        const res = await getTeamStaySummary(params)
        teamSummaryList.value = res.data.records || []
        total.value = res.data.total || 0
      } else {
        const res = await getStayList(params)
        stayList.value = res.data.records
        total.value = res.data.total
      }
    } catch (error) { console.error('查询入住列表失败', error) } finally { loading.value = false }
  }
const handleSearch = () => { queryParams.page = 1; fetchStayList() }
const handleReset = () => {
  queryParams.status = ''; queryParams.roomNo = ''; queryParams.guestName = ''; queryParams.checkInType = ''
  queryParams.checkInDateStart = null; queryParams.checkInDateEnd = null; dateRange.value = null
  queryParams.page = 1; fetchStayList()
}
const fetchTodayCheckIns = async () => {
  todayLoading.value = true
  try {
    const res = await getTodayCheckIns(queryParams.hotelId || 1)
    todayCheckIns.value = res.data || []
  } catch (e) { console.error(e) } finally { todayLoading.value = false }
}
const fetchTodayDepartures = async () => {
  departuresLoading.value = true
  try {
    const today = new Date().toISOString().split('T')[0]
    const res = await request.get('/v1/stays', { params: { hotelId: queryParams.hotelId || 1, status: 'CHECKED_IN', checkInDateEnd: today, size: 100 } })
    todayDepartures.value = (res.data?.records || []).filter(s => s.checkOutTime && s.checkOutTime.startsWith(today))
  } catch (e) { console.error(e) } finally { departuresLoading.value = false }
}
const handleTabChange = (tab) => {
  if (tab === 'today') fetchTodayCheckIns()
  if (tab === 'departures') fetchTodayDepartures()
}

const showWalkInDialog = async () => {
  walkInForm.roomTypeId = ''; walkInForm.roomId = ''; walkInForm.guestName = ''
  walkInForm.guestIdNo = ''; walkInForm.guestPhone = ''; walkInForm.guestGender = ''; walkInForm.expectedCheckOutDate = ''; walkInForm.pricePlanId = ''
  walkInForm.planPrice = null; walkInForm.dailyPrice = null; walkInForm.coGuests = []
  availableRooms.value = []
  await Promise.all([fetchRoomTypes(), fetchPricePlans()])
  walkInDialogVisible.value = true
}



// 显示同住人管理弹窗
const showCoGuestDialog = async (row) => {
  currentStayForCoGuest.value = row
  coGuestDialogVisible.value = true
  showAddCoGuestForm.value = false
  addCoGuestForm.guestName = ''
  addCoGuestForm.phone = ''
  addCoGuestForm.idNo = ''
  addCoGuestForm.gender = ''
  await loadCoGuests(row.id)
}

// 加载同住人列表
const loadCoGuests = async (stayId) => {
  coGuestLoading.value = true
  try {
    const res = await getStayGuests(stayId)
    if (res.code === 200) {
      currentCoGuests.value = res.data || []
    }
  } catch (e) {
    console.error('加载同住人失败', e)
    currentCoGuests.value = []
  } finally {
    coGuestLoading.value = false
  }
}

// 提交添加同住人
const submitAddCoGuest = async () => {
  if (!addCoGuestForm.guestName) {
    ElMessage.warning('请输入同住人姓名')
    return
  }
  addCoGuestLoading.value = true
  try {
    const res = await addCoGuest(currentStayForCoGuest.value.id, currentStayForCoGuest.value.hotelId, addCoGuestForm)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      addCoGuestForm.guestName = ''
      addCoGuestForm.phone = ''
      addCoGuestForm.idNo = ''
      addCoGuestForm.gender = ''
      showAddCoGuestForm.value = false
      await loadCoGuests(currentStayForCoGuest.value.id)
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch (e) {
    ElMessage.error('添加失败')
  } finally {
    addCoGuestLoading.value = false
  }
}

// 删除同住人
const removeCoGuest = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该同住人吗？', '提示', { type: 'warning' })
    const res = await removeCoGuestApi(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadCoGuests(currentStayForCoGuest.value.id)
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 同住人操作方法
const addCoGuestRow = () => {
  walkInForm.coGuests.push({ guestName: '', phone: '', idNo: '', gender: '' })
}
const removeCoGuestRow = (idx) => {
  walkInForm.coGuests.splice(idx, 1)
}
const addResCoGuestRow = () => {
  reservationCheckInForm.coGuests.push({ guestName: '', phone: '', idNo: '', gender: '' })
}
const removeResCoGuestRow = (idx) => {
  reservationCheckInForm.coGuests.splice(idx, 1)
}

const submitWalkIn = async () => {
  const valid = await walkInFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try { await walkInCheckIn({ ...walkInForm, hotelId: queryParams.hotelId || 1, dailyPrice: walkInForm.dailyPrice || null }); ElMessage.success('入住成功'); walkInDialogVisible.value = false; fetchStayList() }
  catch (error) { console.error('散客入住失败', error) } finally { submitLoading.value = false }
}


// 获取可用房间列表

// 房型变更时获取可用房间
const handleRoomTypeChange = async (roomTypeId) => {
  walkInForm.roomId = ''
  if (!roomTypeId) {
    availableRooms.value = []
    return
  }
  try {
    const params = { hotelId: queryParams.hotelId || 1, status: 'AVAILABLE', roomTypeId }
    const res = await request.get('/v1/rooms', { params })
    availableRooms.value = res.data?.records || []
  } catch (error) { console.error('获取可用房间失败', error) }
}

const fetchAvailableRooms = async () => {
  try {
    const params = { hotelId: queryParams.hotelId || 1, status: 'AVAILABLE' }
    const res = await request.get('/v1/rooms', { params })
    availableRooms.value = res.data?.records || []
  } catch (error) { console.error('获取可用房间失败', error) }
}

// 根据房型获取可用房间列表
const fetchReservationAvailableRooms = async (roomTypeId) => {
  if (!roomTypeId) {
    reservationAvailableRooms.value = []
    return
  }
  try {
    // 查询所有房间（包含预订的房间）
    const params = { hotelId: queryParams.hotelId || 1, roomTypeId }
    const res = await request.get('/v1/rooms', { params })
    const allRooms = res.data?.records || []
    
    // 过滤出可用的房间（AVAILABLE 或 RESERVED 状态）
    reservationAvailableRooms.value = allRooms.filter(room => 
      room.status === 'AVAILABLE' || room.status === 'RESERVED'
    )
    
    // 如果预订的房间不在列表中，添加到列表开头
    if (reservationCheckInForm.roomId) {
      // 确保roomId是数字类型
      reservationCheckInForm.roomId = Number(reservationCheckInForm.roomId)
      const bookedRoom = reservationAvailableRooms.value.find(r => Number(r.id) === reservationCheckInForm.roomId)
      if (!bookedRoom) {
        // 查询预订的房间信息
        try {
          const roomRes = await request.get('/v1/rooms/' + reservationCheckInForm.roomId)
          if (roomRes.data) {
            reservationAvailableRooms.value.unshift(roomRes.data)
          }
        } catch (e) {
          console.error('获取预订房间信息失败', e)
        }
      }
    }
  } catch (error) { console.error('获取可用房间失败', error) }
}

// 获取房型列表
const fetchRoomTypes = async () => {
  try {
    const params = { hotelId: queryParams.hotelId || 1 }
    const res = await request.get('/v1/room-types', { params })
    roomTypeOptions.value = res.data?.records || []
  } catch (error) { console.error('获取房型列表失败', error) }
}

// 房价码变化处理
const handleWalkInPricePlanChange = async (planId) => {
  if (planId && walkInForm.roomTypeId) {
    try {
      const res = await request.get('/v1/prices/query', { params: { hotelId: queryParams.hotelId || 1, roomTypeId: walkInForm.roomTypeId, date: new Date().toISOString().split('T')[0] } })
      if (res.data?.price) {
        walkInForm.planPrice = res.data.price
        walkInForm.dailyPrice = res.data.price
      }
    } catch (e) { console.error('获取房价失败', e) }
  } else if (planId) {
    try {
      const planRes = await request.get('/v1/price-plans/' + planId)
      if (planRes.data?.details && planRes.data.details.length > 0) {
        walkInForm.planPrice = planRes.data.details[0].finalPrice
        walkInForm.dailyPrice = planRes.data.details[0].finalPrice
      }
    } catch (e) { console.error('获取房价失败', e) }
  } else {
    walkInForm.planPrice = null
    walkInForm.dailyPrice = null
  }
}

// 获取房价码列表
const fetchPricePlans = async () => {
  try {
    const params = { hotelId: queryParams.hotelId || 1, status: 'ACTIVE' }
    const res = await request.get('/v1/price-plans', { params })
    pricePlanOptions.value = res.data?.records || []
  } catch (error) { console.error('获取房价码列表失败', error) }
}

const showReservationCheckInDialog = async (params = {}) => {
  const reservationId = typeof params === 'object' ? params.reservationId : params
  reservationCheckInForm.reservationId = reservationId || ''
  reservationCheckInForm.roomId = (typeof params === 'object' ? params.roomId : '') || ''
  reservationCheckInForm.guestName = (typeof params === 'object' ? params.guestName : '') || ''
  reservationCheckInForm.guestIdNo = (typeof params === 'object' ? params.guestIdNo : '') || ''
  reservationCheckInForm.guestPhone = (typeof params === 'object' ? params.guestPhone : '') || ''
  reservationCheckInForm.guestGender = (typeof params === 'object' ? params.guestGender : '') || ''
  reservationCheckInForm.roomTypeId = (typeof params === 'object' ? params.roomTypeId : '') || ''
  reservationCheckInForm.roomTypeName = (typeof params === 'object' ? params.roomTypeName : '') || ''
    reservationCheckInForm.coGuests = []
  // 根据预订房型筛选可用房间
  if (reservationCheckInForm.roomTypeId) {
    await fetchReservationAvailableRooms(reservationCheckInForm.roomTypeId)
  } else {
    await fetchAvailableRooms()
    reservationAvailableRooms.value = availableRooms.value
  }
  reservationCheckInVisible.value = true
}

const submitReservationCheckIn = async () => {
  const valid = await reservationCheckInFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try { await reservationCheckIn(reservationCheckInForm); ElMessage.success('预订入住成功'); reservationCheckInVisible.value = false; fetchStayList() }
  catch (error) { console.error('预订入住失败', error) } finally { submitLoading.value = false }
}


// 计算支付状态类型
const getPaymentStatusType = (row) => {
  const total = row.totalAmount || 0
  const paid = row.paidAmount || 0
  
  if (total === 0) return 'info'
  if (paid >= total) return 'success'
  if (paid > 0) return 'warning'
  return 'danger'
}

// 计算支付状态文本
const getPaymentStatusText = (row) => {
  const total = row.totalAmount || 0
  const paid = row.paidAmount || 0
  
  if (total === 0) return '无费用'
  if (paid >= total) return '已付清'
  if (paid > 0) return `部分支付`
  return '未支付'
}


// 计算团队支付状态类型
const getTeamPaymentStatusType = (row) => {
  const total = row.totalAmount || 0
  const paid = row.paidAmount || 0
  if (total === 0) return 'info'
  if (paid >= total) return 'success'
  if (paid > 0) return 'warning'
  return 'danger'
}

// 计算团队支付状态文本
const getTeamPaymentStatusText = (row) => {
  const total = row.totalAmount || 0
  const paid = row.paidAmount || 0
  if (total === 0) return '无费用'
  if (paid >= total) return '已付清'
  if (paid > 0) return '部分支付'
  return '未支付'
}

  const viewStayDetail = async (row) => {
    try {
      const res = await getStayById(row.id)
      stayDetail.value = res.data
      await fetchStayDeposits(row.id)
      detailDialogVisible.value = true
    } catch (error) {
      console.error("获取详情失败", error)
    }
  }

  // 获取入住单的押金信息
  const fetchStayDeposits = async (stayId) => {
    try {
      const res = await getDepositsByStayId(stayId)
      stayDeposits.value = res.data || []
      totalDepositAmount.value = stayDeposits.value.reduce((sum, d) => sum + (d.amount || 0), 0)
      refundableDepositAmount.value = stayDeposits.value.reduce((sum, d) => sum + ((d.amount || 0) - (d.refundedAmount || 0) - (d.deductedAmount || 0)), 0)
    } catch (error) {
      console.error("获取押金信息失败", error)
      stayDeposits.value = []
      totalDepositAmount.value = 0
      refundableDepositAmount.value = 0
    }
  }

  // 获取押金状态类型
  const getDepositStatusType = (status) => {
    switch (status) {
      case 'COLLECTED': return 'success'
      case 'REFUNDED': return 'info'
      case 'PARTIAL_REFUND': return 'warning'
      default: return ''
    }
  }

// 显示加床/杂费对话框
const showExtraChargeDialog = async (row) => {
  extraChargeStay.value = row
  extraChargeForm.chargeType = 'BED'
  extraChargeForm.description = ''
  extraChargeForm.amount = 0
  extraChargeForm.paymentMethod = 'CASH'
  extraChargeForm.creditCompanyId = null
  extraChargeForm.remark = ''
  await fetchCreditCompanies()
  extraChargeDialogVisible.value = true
}

// 处理加床/杂费支付方式变化
const handleExtraChargePaymentMethodChange = (val) => {
  if (val !== 'CREDIT') {
    extraChargeForm.creditCompanyId = null
  }
}

// 提交加床/杂费
const submitExtraCharge = async () => {
  if (!extraChargeFormRef.value) return
  const valid = await extraChargeFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  extraChargeLoading.value = true
  try {
    const data = {
      ...extraChargeForm,
      operatorId: currentUserId.value
    }
    await addExtraCharge(extraChargeStay.value.id, data)
    ElMessage.success('加床/杂费入账成功')
    extraChargeDialogVisible.value = false
    fetchStayList()
  } catch (error) {
    console.error('加床/杂费入账失败:', error)
    ElMessage.error('入账失败: ' + (error.message || '未知错误'))
  } finally {
    extraChargeLoading.value = false
  }
}

const showPaymentDialog = async (row) => {
  paymentStay.value = row
  paymentForm.transactionType = 'DEPOSIT'; paymentForm.amount = 0; paymentForm.paymentMethod = 'CASH'
  paymentForm.creditCompanyId = null; paymentForm.remark = ''
  folioInfo.value = null
  try { const res = await getFolioByStayId(row.id); folioInfo.value = res.data }
  catch (error) { console.log('账务单不存在，将在收款时自动创建') }
  await fetchCreditCompanies()
  paymentDialogVisible.value = true
}

const handlePaymentMethodChange = (val) => { if (val !== 'CREDIT') paymentForm.creditCompanyId = null }

const submitPayment = async () => {
  const valid = await paymentFormRef.value.validate().catch(() => false)
  if (!valid) return
  paymentLoading.value = true
  try { await collectPayment(paymentStay.value.id, paymentForm); ElMessage.success('收款成功'); paymentDialogVisible.value = false; fetchStayList() }
  catch (error) { console.error('收款失败', error) } finally { paymentLoading.value = false }
}

// 续住功能
const showExtendDialog = async (row) => {
  extendForm.stayId = row.id; extendForm.stayNo = row.stayNo; extendForm.roomNo = row.roomNo
  extendForm.roomTypeName = row.roomTypeName; extendForm.guestName = row.guestName
  extendForm.checkInTime = row.checkInTime; extendForm.checkOutTime = row.checkOutTime
  extendForm.roomId = row.roomId; extendForm.hotelId = row.hotelId; extendForm.roomTypeId = row.roomTypeId
  extendForm.newCheckOutDate = ''; extendPriceDetails.value = []
  extendSummary.extendedDays = 0; extendSummary.additionalAmount = 0
  extendSummary.oldTotalAmount = row.totalAmount || 0; extendSummary.newTotalAmount = row.totalAmount || 0
  extendDialogVisible.value = true
}

const disabledDate = (date) => {
  const checkOutDate = new Date(extendForm.checkOutTime)
  return date.getTime() <= checkOutDate.getTime()
}

const calculateExtendFee = async (newDate) => {
  if (!newDate) {
    extendPriceDetails.value = []; extendSummary.extendedDays = 0
    extendSummary.additionalAmount = 0; extendSummary.newTotalAmount = extendSummary.oldTotalAmount
    return
  }
  const checkOutDate = new Date(extendForm.checkOutTime)
  const newCheckOutDate = new Date(newDate)
  const days = Math.ceil((newCheckOutDate - checkOutDate) / (1000 * 60 * 60 * 24))
  extendSummary.extendedDays = days
  try {
    const priceDetails = []; let totalAdditional = 0
    const startDate = new Date(checkOutDate)
    for (let i = 0; i < days; i++) {
      const date = new Date(startDate); date.setDate(date.getDate() + i)
      const dateStr = date.toISOString().split('T')[0]
      const res = await request({ url: '/v1/prices/query', method: 'get', params: { hotelId: extendForm.hotelId, roomTypeId: extendForm.roomTypeId, date: dateStr } })
      const price = res.data?.price || 0
      priceDetails.push({ date: dateStr, price: price })
      totalAdditional += price
    }
    extendPriceDetails.value = priceDetails; extendSummary.additionalAmount = totalAdditional
    extendSummary.newTotalAmount = extendSummary.oldTotalAmount + totalAdditional
  } catch (error) { console.error('获取房价失败', error); ElMessage.error('获取房价信息失败') }
}

const submitExtend = async () => {
  const valid = await extendFormRef.value.validate().catch(() => false)
  if (!valid) return
  extendLoading.value = true
  try { await extendStay({ stayId: extendForm.stayId, newCheckOutDate: extendForm.newCheckOutDate }); ElMessage.success('续住成功'); extendDialogVisible.value = false; fetchStayList() }
  catch (error) { console.error('续住失败', error) } finally { extendLoading.value = false }
}


  // 换房相关
  const changeRoomDialogVisible = ref(false)
  const changeRoomLoading = ref(false)
  const changeRoomFormRef = ref(null)
  const changeAvailableRooms = ref([])
  const changeRoomForm = reactive({
    stayId: '',
    stayNo: '',
    guestName: '',
    oldRoomId: '',
    oldRoomNo: '',
    oldRoomTypeName: '',
    checkInTime: '',
    checkOutTime: '',
    totalAmount: 0,
    newRoomId: '',
    newRoomNo: '',
    newRoomTypeName: '',
    reason: '',
    amountAdjustment: 0
  })
  const changeRoomRules = {
    newRoomId: [{ required: true, message: '请选择新房间', trigger: 'change' }]
  }

  // 显示换房对话框
  const showChangeRoomDialog = async (row) => {
    changeRoomForm.stayId = row.id
    changeRoomForm.stayNo = row.stayNo
    changeRoomForm.guestName = row.guestName
    changeRoomForm.oldRoomId = row.roomId
    changeRoomForm.oldRoomNo = row.roomNo
    changeRoomForm.oldRoomTypeName = row.roomTypeName
    changeRoomForm.checkInTime = row.checkInTime
    changeRoomForm.checkOutTime = row.checkOutTime
    changeRoomForm.totalAmount = row.totalAmount || 0
    changeRoomForm.newRoomId = ''
    changeRoomForm.newRoomNo = ''
    changeRoomForm.newRoomTypeName = ''
    changeRoomForm.reason = ''
    changeRoomForm.amountAdjustment = 0
    
    // 加载可用房间列表
    await loadAvailableRooms(row.hotelId, row.roomTypeId)
    changeRoomDialogVisible.value = true
  }

  // 加载可用房间
  const loadAvailableRooms = async (hotelId, roomTypeId) => {
    try {
      const res = await request.get('/v1/rooms', { 
        params: { 
          hotelId: hotelId,
          status: 'AVAILABLE'
        } 
      })
      // 过滤掉当前房间
      changeAvailableRooms.value = (res.data?.records || []).filter(room => room.id !== changeRoomForm.oldRoomId)
    } catch (error) {
      console.error('获取可用房间失败', error)
      changeAvailableRooms.value = []
    }
  }

  // 新房间选择变化
  const onNewRoomChange = (roomId) => {
    const room = changeAvailableRooms.value.find(r => r.id === roomId)
    if (room) {
      changeRoomForm.newRoomNo = room.roomNo
      changeRoomForm.newRoomTypeName = room.roomTypeName
      // 简单计算费用调整（实际应由后端计算）
      changeRoomForm.amountAdjustment = 0
    }
  }

  // 提交换房
  const submitChangeRoom = async () => {
    const valid = await changeRoomFormRef.value.validate().catch(() => false)
    if (!valid) return
    
    changeRoomLoading.value = true
    try {
      await changeRoom({
        stayId: changeRoomForm.stayId,
        newRoomId: changeRoomForm.newRoomId,
        reason: changeRoomForm.reason
      })
      ElMessage.success('换房成功')
      changeRoomDialogVisible.value = false
      fetchStayList()
    } catch (error) {
      console.error('换房失败', error)
    } finally {
      changeRoomLoading.value = false
    }
  }

  // 退房功能
const showCheckOutDialog = (row) => { checkOutStay.value = row; checkOutRemark.value = ''; checkOutVisible.value = true }

// 处理下拉菜单操作
const handleAction = (command, row) => {
  switch (command) {
    case 'payment':
      showPaymentDialog(row)
      break
    case 'extend':
      showExtendDialog(row)
      break
    case 'checkout':
        showCheckOutDialog(row)
        break
      case 'changeRoom':
        showChangeRoomDialog(row)
        break
    case 'coGuests':
      showCoGuestDialog(row)
      break
    }
}

const submitCheckOut = async () => {
  checkOutLoading.value = true
  try {
    const res = await checkOut({ stayId: checkOutStay.value.id, remark: checkOutRemark.value })
    ElMessage.success('退房成功'); checkOutVisible.value = false
    const data = res.data
    await ElMessageBox.alert(
      '<div style="line-height: 2;"><p><strong>入住单号：</strong>' + data.stayNo + '</p><p><strong>总费用：</strong>' + (data.totalAmount || 0).toFixed(2) + ' 元</p><p><strong>已付金额：</strong>' + (data.paidAmount || 0).toFixed(2) + ' 元</p><p style="color: #f56c6c; font-weight: bold;"><strong>待结金额：</strong>' + (data.outstandingAmount || 0).toFixed(2) + ' 元</p></div>',
      '退房结算单', { dangerouslyUseHTMLString: true, confirmButtonText: '确定', type: 'success' }
    )
    fetchStayList()
  } catch (error) { console.error('退房失败', error) } finally { checkOutLoading.value = false }
}

// 监听路由参数变化，自动打开预订入住弹窗
watch(() => route.query, async (newQuery) => {
  if (newQuery.stayId && newQuery.action === 'detail') {
    await nextTick()
    // 查找对应的入住记录并显示详情
    const stay = stayList.value.find(s => s.id === Number(newQuery.stayId))
    if (stay) {
      await viewStayDetail(stay)
    } else {
      // 如果不在当前列表中，重新加载列表
      await fetchStayList()
      const updatedStay = stayList.value.find(s => s.id === Number(newQuery.stayId))
      if (updatedStay) {
        await viewStayDetail(updatedStay)
      }
    }
  } else if (newQuery.reservationId) {
    await nextTick()
    await showReservationCheckInDialog({
      reservationId: newQuery.reservationId || '',
      guestName: newQuery.guestName || '',
      guestPhone: newQuery.guestPhone || '',
      roomId: newQuery.roomId || '',
      roomTypeId: newQuery.roomTypeId || '',
      roomTypeName: newQuery.roomTypeName || ''
    })
  }
}, { immediate: true })

const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  fetchStayList()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchStayList()
}
onMounted(async () => {
  // 检查是否从首页跳转过来需要切换到今日入住标签
  if (route.query.tab === 'today') {
    activeTab.value = 'today'
    fetchTodayCheckIns()
  }
  if (route.query.tab === 'departures') {
    activeTab.value = 'departures'
    fetchTodayDepartures()
  }
  await fetchStayList()
})
</script>

<style scoped lang="scss">
.stay-management { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { margin: 0; font-size: 20px; font-weight: 600; } .header-actions { display: flex; gap: 10px; } }
.filter-card { margin-bottom: 20px; }
.type-segment {
  margin-bottom: 16px;
  .el-radio-group {
    --el-radio-button-checked-bg-color: #409eff;
    --el-radio-button-checked-border-color: #409eff;
    .el-radio-button {
      .el-radio-button__inner {
        min-width: 100px;
        font-size: 14px;
        border-color: #dcdfe6;
        &:hover {
          color: #409eff;
        }
      }
      &.is-active .el-radio-button__inner {
        color: #fff;
      }
    }
  }
  }
.extend-info { margin-bottom: 20px; }
.payment-info { margin-bottom: 16px; }
.folio-info { margin-top: 16px; .amount { font-weight: bold; color: #e6a23c; &.paid { color: #67c23a; } &.highlight { color: #f56c6c; font-size: 16px; } } }
.price-details { margin: 20px 0; h4 { margin: 0 0 10px 0; font-size: 14px; color: #606266; } }
.fee-summary { margin-top: 20px; .amount { font-weight: bold; color: #e6a23c; &.highlight { color: #f56c6c; font-size: 16px; } } }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
.checkout-info { margin-bottom: 16px; }
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>















