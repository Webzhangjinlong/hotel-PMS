// 报表菜单配置
export const reportMenuConfig = {
  title: '收银报表',
  icon: 'DataAnalysis',
  children: [
    {
      title: '收银员交接表',
      path: '/reports/cashier/shift',
      icon: 'Document'
    },
    {
      title: '前台入账明细',
      path: '/reports/cashier/entry-detail',
      icon: 'List'
    },
    {
      title: '前台入账简表',
      path: '/reports/cashier/entry-summary',
      icon: 'Document'
    },
    {
      title: '前台入账汇总',
      path: '/reports/cashier/entry-total',
      icon: 'DataBoard'
    },
    {
      title: '前台收款明细',
      path: '/reports/cashier/payment-detail',
      icon: 'List'
    },
    {
      title: '前台收款汇总',
      path: '/reports/cashier/payment-summary',
      icon: 'DataBoard'
    },
    {
      title: '前台转账报表',
      path: '/reports/cashier/transfer',
      icon: 'Transaction'
    },
    {
      title: '冲账调账报表',
      path: '/reports/cashier/chargeback',
      icon: 'RefreshRight'
    },
    {
      title: '结账实收统计',
      path: '/reports/cashier/checkout-stats',
      icon: 'PieChart'
    },
    {
      title: '结账实收明细',
      path: '/reports/cashier/checkout-detail',
      icon: 'List'
    },
    {
      title: '商品销售汇总',
      path: '/reports/cashier/product-summary',
      icon: 'ShoppingCart'
    },
    {
      title: '商品销售明细',
      path: '/reports/cashier/product-detail',
      icon: 'List'
    }
  ]
}
