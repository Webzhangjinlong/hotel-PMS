import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/stores/user'
import { cashierReportRoutes } from './reportRoutes'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardView.vue'), meta: { title: '首页', requiresAuth: true } },
      { path: 'master/hotel', name: 'HotelList', component: () => import('@/views/master/hotel/HotelList.vue'), meta: { title: '酒店管理', requiresAuth: true } },
      { path: 'master/floor', name: 'FloorList', component: () => import('@/views/master/floor/FloorList.vue'), meta: { title: '楼层管理', requiresAuth: true } },
      { path: 'master/room-type', name: 'RoomTypeList', component: () => import('@/views/master/room-type/RoomTypeList.vue'), meta: { title: '房型管理', requiresAuth: true } },
      { path: 'master/room', name: 'RoomList', component: () => import('@/views/master/room/RoomList.vue'), meta: { title: '房间管理', requiresAuth: true } },
      { path: 'reservation/list', name: 'ReservationList', component: () => import('@/views/reservation/ReservationList.vue'), meta: { title: '预订列表', requiresAuth: true } },
      { path: 'reservation/create', name: 'ReservationCreate', component: () => import('@/views/reservation/ReservationCreate.vue'), meta: { title: '新建预订', requiresAuth: true } },
      { path: 'reservation/edit/:id', name: 'ReservationEdit', component: () => import('@/views/reservation/ReservationCreate.vue'), meta: { title: '编辑预订', requiresAuth: true } },
      { path: 'reservation/checkin', name: 'QuickCheckIn', component: () => import('@/views/reservation/QuickCheckIn.vue'), meta: { title: '快速入住', requiresAuth: true } },
      { path: 'reservation/team', name: 'TeamReservationList', component: () => import('@/views/reservation/TeamReservationList.vue'), meta: { title: '团队预订', requiresAuth: true } },
      { path: 'reservation/team/create', name: 'TeamReservationCreate', component: () => import('@/views/reservation/TeamReservationDetail.vue'), meta: { title: '新建团队预订', requiresAuth: true } },
      { path: 'reservation/team/:id', name: 'TeamReservationDetail', component: () => import('@/views/reservation/TeamReservationDetail.vue'), meta: { title: '团队预订详情', requiresAuth: true } },
      { path: 'price/management', name: 'PriceManagement', component: () => import('@/views/price/PriceManagement.vue'), meta: { title: '房价管理', requiresAuth: true } },
      { path: 'price/plan', name: 'PricePlan', component: () => import('@/views/price/PricePlan.vue'), meta: { title: '房价码管理', requiresAuth: true } },
      { path: 'stays', name: 'StayManagement', component: () => import('@/views/front-desk/StayManagement.vue'), meta: { title: '入住管理', requiresAuth: true } },
      { path: 'room-board', name: 'RoomBoard', component: () => import('@/views/front-desk/RoomBoard.vue'), meta: { title: '房态看板', requiresAuth: true } },
      { path: 'room-calendar', name: 'RoomCalendar', component: () => import('@/views/front-desk/RoomCalendar.vue'), meta: { title: '房间预订日历', requiresAuth: true } },
      { path: 'folios', name: 'FolioManagement', component: () => import('@/views/front-desk/FolioManagement.vue'), meta: { title: '账务管理', requiresAuth: true } },
        { path: 'transactions', name: 'TransactionList', component: () => import('@/views/front-desk/TransactionList.vue'), meta: { title: '交易流水', requiresAuth: true } },
      { path: 'deposits', name: 'DepositManagement', component: () => import('@/views/front-desk/DepositManagement.vue'), meta: { title: '押金管理', requiresAuth: true } },
      { path: 'guests', name: 'GuestManagement', component: () => import('@/views/guest/GuestManagement.vue'), meta: { title: '客人档案', requiresAuth: true } },
      { path: 'member/management', name: 'MemberManagement', component: () => import('@/views/member/MemberManagement.vue'), meta: { title: '会员管理', requiresAuth: true } },
      { path: 'member/levels', name: 'MemberLevelConfig', component: () => import('@/views/member/MemberLevelConfig.vue'), meta: { title: '会员等级配置', requiresAuth: true } },
      { path: 'police-upload', name: 'PoliceUpload', component: () => import('@/views/front-desk/PoliceUpload.vue'), meta: { title: '公安上传', requiresAuth: true } },
      { path: 'finance', name: 'FinanceManagement', component: () => import('@/views/finance/FinanceManagement.vue'), meta: { title: '财务管理', requiresAuth: true } },
      { path: 'agreement', name: 'AgreementManagement', component: () => import('@/views/agreement/AgreementManagement.vue'), meta: { title: '协议单位管理', requiresAuth: true } },
      { path: 'invoice', name: 'InvoiceManagement', component: () => import('@/views/invoice/InvoiceManagement.vue'), meta: { title: '发票管理', requiresAuth: true } },
      { path: 'reports/enhanced', name: 'EnhancedReport', component: () => import('@/views/reports/EnhancedReport.vue'), meta: { title: '报表增强', requiresAuth: true } },
      { path: 'ota/channels', name: 'OtaChannelManagement', component: () => import('@/views/ota/OtaChannelManagement.vue'), meta: { title: 'OTA渠道管理', requiresAuth: true } },
      { path: 'idcard/readers', name: 'IdcardReaderManagement', component: () => import('@/views/idcard/IdcardReaderManagement.vue'), meta: { title: '身份证阅读器管理', requiresAuth: true } },
      { path: 'doorlock', name: 'DoorlockManagement', component: () => import('@/views/doorlock/DoorlockManagement.vue'), meta: { title: '门锁/房卡管理', requiresAuth: true } },
      { path: 'housekeeping/clean', name: 'CleanTask', component: () => import('@/views/housekeeping/CleanTask.vue'), meta: { title: '清洁任务', requiresAuth: true } },
      { path: 'housekeeping/maintenance', name: 'Maintenance', component: () => import('@/views/housekeeping/Maintenance.vue'), meta: { title: '维修管理', requiresAuth: true } },
      { path: 'night-audit', name: 'NightAudit', component: () => import('@/views/night-audit/NightAudit.vue'), meta: { title: '执行夜审', requiresAuth: true } },
      { path: 'night-audit/records', name: 'AuditRecords', component: () => import('@/views/night-audit/AuditRecords.vue'), meta: { title: '夜审记录', requiresAuth: true } },
      { path: 'night-audit/config', name: 'NightAuditConfig', component: () => import('@/views/night-audit/NightAuditConfig.vue'), meta: { title: '夜审配置', requiresAuth: true } },
      { path: 'night-audit/archive', name: 'NightAuditArchive', component: () => import('@/views/night-audit/NightAuditArchive.vue'), meta: { title: '历史数据', requiresAuth: true } },
      { path: 'reports/daily', name: 'DailyReport', component: () => import('@/views/reports/DailyReport.vue'), meta: { title: '营业日报', requiresAuth: true } },
            { path: 'reports/cashier/shift', name: 'ShiftReport', component: () => import('@/views/reports/cashier/ShiftReport.vue'), meta: { title: '收银员交接表', requiresAuth: true } },
      { path: 'reports/cashier/entry-detail', name: 'FrontEndEntryDetail', component: () => import('@/views/reports/cashier/FrontEndEntryDetail.vue'), meta: { title: '前台入账明细', requiresAuth: true } },
      { path: 'reports/cashier/entry-summary', name: 'FrontEndEntrySummary', component: () => import('@/views/reports/cashier/FrontEndEntrySummary.vue'), meta: { title: '前台入账简表', requiresAuth: true } },
      { path: 'reports/cashier/entry-total', name: 'FrontEndEntryTotal', component: () => import('@/views/reports/cashier/FrontEndEntryTotal.vue'), meta: { title: '前台入账汇总', requiresAuth: true } },
      { path: 'reports/cashier/payment-detail', name: 'FrontEndPaymentDetail', component: () => import('@/views/reports/cashier/FrontEndPaymentDetail.vue'), meta: { title: '前台收款明细', requiresAuth: true } },
      { path: 'reports/cashier/payment-summary', name: 'FrontEndPaymentSummary', component: () => import('@/views/reports/cashier/FrontEndPaymentSummary.vue'), meta: { title: '前台收款汇总', requiresAuth: true } },
      { path: 'reports/cashier/transfer', name: 'FrontEndTransfer', component: () => import('@/views/reports/cashier/FrontEndTransfer.vue'), meta: { title: '前台转账报表', requiresAuth: true } },
      { path: 'reports/cashier/chargeback', name: 'ChargeBackAdjust', component: () => import('@/views/reports/cashier/ChargeBackAdjust.vue'), meta: { title: '冲账调账报表', requiresAuth: true } },
      { path: 'reports/cashier/checkout-stats', name: 'CheckoutActualStats', component: () => import('@/views/reports/cashier/CheckoutActualStats.vue'), meta: { title: '结账实收统计', requiresAuth: true } },
      { path: 'reports/cashier/checkout-detail', name: 'CheckoutActualDetail', component: () => import('@/views/reports/cashier/CheckoutActualDetail.vue'), meta: { title: '结账实收明细', requiresAuth: true } },
      { path: 'reports/cashier/product-summary', name: 'ProductSalesSummary', component: () => import('@/views/reports/cashier/ProductSalesSummary.vue'), meta: { title: '商品销售汇总', requiresAuth: true } },
      { path: 'reports/cashier/product-detail', name: 'ProductSalesDetail', component: () => import('@/views/reports/cashier/ProductSalesDetail.vue'), meta: { title: '商品销售明细', requiresAuth: true } },
      { path: 'reports/metrics', name: 'Metrics', component: () => import('@/views/reports/Metrics.vue'), meta: { title: '经营指标', requiresAuth: true } },
      { path: 'accounts', name: 'AccountManagement', component: () => import('@/views/system/AccountManagement.vue'), meta: { title: '账号管理', requiresAuth: true } },
      { path: 'roles', name: 'RoleManagement', component: () => import('@/views/system/RoleManagement.vue'), meta: { title: '角色管理', requiresAuth: true } },
      { path: 'permissions', name: 'PermissionManagement', component: () => import('@/views/system/PermissionManagement.vue'), meta: { title: '权限管理', requiresAuth: true } },
      { path: 'shifts', name: 'ShiftManagement', component: () => import('@/views/system/ShiftManagement.vue'), meta: { title: '交班', requiresAuth: true } },
      { path: 'operation-logs', name: 'OperationLogs', component: () => import('@/views/system/OperationLogs.vue'), meta: { title: '操作日志', requiresAuth: true } },
      { path: 'system/hotel-config', name: 'HotelConfig', component: () => import('@/views/system/HotelConfig.vue'), meta: { title: '超时离店配置', requiresAuth: true } },
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/auth/NotFound.vue'), meta: { title: '页面不存在' } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = to.meta.title ? to.meta.title + ' - PMS酒店管理系统' : 'PMS酒店管理系统'
  if (to.meta.requiresAuth !== false) {
    const userStore = useUserStore()
    if (userStore.token) {
      if (!userStore.userInfo) {
        try { await userStore.getUserInfo(); next() }
        catch (error) { userStore.logout(); next('/login') }
      } else { next() }
    } else { next('/login?redirect=' + to.path) }
  } else {
    const userStore = useUserStore()
    if (to.path === '/login' && userStore.token) { next('/') }
    else { next() }
  }
})

router.afterEach(() => { NProgress.done() })

export default router














