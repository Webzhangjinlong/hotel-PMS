// 收银报表路由
export const cashierReportRoutes = [
  {
    path: 'reports/cashier/shift',
    name: 'ShiftReport',
    component: () => import('@/views/reports/cashier/ShiftReport.vue'),
    meta: { title: '收银员交接表', requiresAuth: true }
  },
  {
    path: 'reports/cashier/entry-detail',
    name: 'FrontEndEntryDetail',
    component: () => import('@/views/reports/cashier/FrontEndEntryDetail.vue'),
    meta: { title: '前台入账明细', requiresAuth: true }
  },
  {
    path: 'reports/cashier/entry-summary',
    name: 'FrontEndEntrySummary',
    component: () => import('@/views/reports/cashier/FrontEndEntrySummary.vue'),
    meta: { title: '前台入账简表', requiresAuth: true }
  },
  {
    path: 'reports/cashier/entry-total',
    name: 'FrontEndEntryTotal',
    component: () => import('@/views/reports/cashier/FrontEndEntryTotal.vue'),
    meta: { title: '前台入账汇总', requiresAuth: true }
  },
  {
    path: 'reports/cashier/payment-detail',
    name: 'FrontEndPaymentDetail',
    component: () => import('@/views/reports/cashier/FrontEndPaymentDetail.vue'),
    meta: { title: '前台收款明细', requiresAuth: true }
  },
  {
    path: 'reports/cashier/payment-summary',
    name: 'FrontEndPaymentSummary',
    component: () => import('@/views/reports/cashier/FrontEndPaymentSummary.vue'),
    meta: { title: '前台收款汇总', requiresAuth: true }
  },
  {
    path: 'reports/cashier/transfer',
    name: 'FrontEndTransfer',
    component: () => import('@/views/reports/cashier/FrontEndTransfer.vue'),
    meta: { title: '前台转账报表', requiresAuth: true }
  },
  {
    path: 'reports/cashier/chargeback',
    name: 'ChargeBackAdjust',
    component: () => import('@/views/reports/cashier/ChargeBackAdjust.vue'),
    meta: { title: '冲账调账报表', requiresAuth: true }
  },
  {
    path: 'reports/cashier/checkout-stats',
    name: 'CheckoutActualStats',
    component: () => import('@/views/reports/cashier/CheckoutActualStats.vue'),
    meta: { title: '结账实收统计', requiresAuth: true }
  },
  {
    path: 'reports/cashier/checkout-detail',
    name: 'CheckoutActualDetail',
    component: () => import('@/views/reports/cashier/CheckoutActualDetail.vue'),
    meta: { title: '结账实收明细', requiresAuth: true }
  },
  {
    path: 'reports/cashier/product-summary',
    name: 'ProductSalesSummary',
    component: () => import('@/views/reports/cashier/ProductSalesSummary.vue'),
    meta: { title: '商品销售汇总', requiresAuth: true }
  },
  {
    path: 'reports/cashier/product-detail',
    name: 'ProductSalesDetail',
    component: () => import('@/views/reports/cashier/ProductSalesDetail.vue'),
    meta: { title: '商品销售明细', requiresAuth: true }
  }
]
