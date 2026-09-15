import request from '@/utils/request'

/**
 * 报表API
 */

// 获取收银员交接表
export function getShiftReport(params) {
  return request({
    url: '/api/report/shift',
    method: 'get',
    params
  })
}

// 获取前台入账明细
export function getEntryDetail(params) {
  return request({
    url: '/api/report/entry/detail',
    method: 'get',
    params
  })
}

// 获取前台入账简表
export function getEntrySummary(params) {
  return request({
    url: '/api/report/entry/summary',
    method: 'get',
    params
  })
}

// 获取前台入账汇总
export function getEntryTotal(params) {
  return request({
    url: '/api/report/entry/total',
    method: 'get',
    params
  })
}

// 获取前台收款明细
export function getPaymentDetail(params) {
  return request({
    url: '/api/report/payment/detail',
    method: 'get',
    params
  })
}

// 获取前台收款汇总
export function getPaymentSummary(params) {
  return request({
    url: '/api/report/payment/summary',
    method: 'get',
    params
  })
}

// 获取前台转账报表
export function getTransferReport(params) {
  return request({
    url: '/api/report/transfer',
    method: 'get',
    params
  })
}

// 获取冲账调账报表
export function getChargeBackAdjust(params) {
  return request({
    url: '/api/report/chargeback',
    method: 'get',
    params
  })
}

// 获取结账实收统计
export function getCheckoutActualStats(params) {
  return request({
    url: '/api/report/checkout/stats',
    method: 'get',
    params
  })
}

// 获取结账实收明细
export function getCheckoutActualDetail(params) {
  return request({
    url: '/api/report/checkout/detail',
    method: 'get',
    params
  })
}

// 获取商品销售汇总
export function getProductSalesSummary(params) {
  return request({
    url: '/api/report/product/summary',
    method: 'get',
    params
  })
}

// 获取商品销售明细
export function getProductSalesDetail(params) {
  return request({
    url: '/api/report/product/detail',
    method: 'get',
    params
  })
}

// 获取完整报表（报表增强页：基础指标 + 渠道收入占比 + 趋势数据）
export function getFullReport(params) {
  return request({
    url: '/api/v1/metrics/full-report',
    method: 'get',
    params
  })
}
