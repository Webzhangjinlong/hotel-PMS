import request from '@/utils/request'

/**
 * 查询入住账务
 * @param {number} stayId - 入住单ID
 * @returns {Promise} 账务信息
 */
export function getFolioByStayId(stayId) {
  return request({
    url: `/v1/folios/stay/${stayId}`,
    method: 'get'
  })
}

/**
 * 入住收款
 * @param {number} stayId - 入住单ID
 * @param {Object} data - 收款参数
 * @returns {Promise} 收款结果
 */
export function collectPayment(stayId, data) {
  return request({
    url: `/v1/folios/stay/${stayId}/payment`,
    method: 'post',
    data
  })
}

/**
 * 退款
 * @param {number} folioId - 账务单ID
 * @param {Object} data - 退款参数
 * @returns {Promise} 退款结果
 */
export function refund(folioId, data) {
  return request({
    url: `/v1/folios/${folioId}/refund`,
    method: 'post',
    data
  })
}

/**
 * 查询账务单交易记录
 * @param {number} folioId - 账务单ID
 * @returns {Promise} 交易记录列表
 */
export function getTransactions(folioId) {
  return request({
    url: `/v1/folios/${folioId}/transactions`,
    method: 'get'
  })
}

/**
 * 冲账
 * @param {number} transactionId - 交易ID
 * @param {string} reason - 冲账原因
 * @returns {Promise} 冲账结果
 */
export function reverseTransaction(transactionId, reason) {
  return request({
    url: `/v1/folios/transactions/${transactionId}/reverse`,
    method: 'post',
    params: { reason }
  })
}

/**
 * 查询交易记录（分页）
 * @param {Object} params - 查询参数
 * @returns {Promise} 交易记录列表
 */
export function getTransactionList(params) {
  return request({
    url: '/v1/folios/transactions',
    method: 'get',
    params
  })
}

/**
 * 查询挂账公司列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 挂账公司列表
 */
export function getCreditCompanyList(params) {
  return request({
    url: '/v1/credit-companies',
    method: 'get',
    params
  })
}

/**
 * 创建挂账公司
 * @param {Object} data - 挂账公司信息
 * @returns {Promise} 挂账公司信息
 */
export function createCreditCompany(data) {
  return request({
    url: '/v1/credit-companies',
    method: 'post',
    data
  })
}

/**
 * 挂账结算
 * @param {number} id - 挂账公司ID
 * @param {Object} params - 结算参数
 * @returns {Promise} 结算结果
 */
export function settleCredit(id, params) {
  return request({
    url: `/v1/credit-companies/${id}/settle`,
    method: 'post',
    params
  })
}

/**
 * 预订预付
 * @param {Object} data - 预付参数
 * @returns {Promise} 预付结果
 */
export function prepay(data) {
  return request({
    url: '/v1/prepayments',
    method: 'post',
    data
  })
}

/**
 * 预付款转入住账务
 * @param {number} id - 预付ID
 * @param {number} stayId - 入住单ID
 * @returns {Promise} 转账结果
 */
export function transferPrepayment(id, stayId) {
  return request({
    url: `/v1/prepayments/${id}/transfer/${stayId}`,
    method: 'post'
  })
}

/**
 * 预付退款
 * @param {number} id - 预付ID
 * @param {string} refundMethod - 退款方式
 * @returns {Promise} 退款结果
 */
export function refundPrepayment(id, refundMethod) {
  return request({
    url: `/v1/prepayments/${id}/refund`,
    method: 'post',
    params: { refundMethod }
  })
}

/**
 * 加床/杂费入账
 * @param {number} stayId - 入住单ID
 * @param {Object} data - 加床/杂费参数
 * @returns {Promise} 入账结果
 */
export function addExtraCharge(stayId, data) {
  return request({
    url: `/v1/folios/stay/${stayId}/extra-charge`,
    method: 'post',
    data
  })
}

/**
 * 查询入住单的加床/杂费记录
 * @param {number} stayId - 入住单ID
 * @returns {Promise} 交易记录列表
 */
export function getExtraChargesByStayId(stayId) {
  return request({
    url: `/v1/folios/stay/${stayId}/extra-charges`,
    method: 'get'
  })
}
