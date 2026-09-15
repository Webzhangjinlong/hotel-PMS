import request from '@/utils/request'

/**
 * 查询发票列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 发票列表
 */
export function getInvoiceList(params) {
  return request({
    url: '/v1/invoices',
    method: 'get',
    params
  })
}

/**
 * 查询发票详情
 * @param {number} id - 发票ID
 * @returns {Promise} 发票信息
 */
export function getInvoiceById(id) {
  return request({
    url: `/v1/invoices/${id}`,
    method: 'get'
  })
}

/**
 * 创建发票
 * @param {Object} data - 发票信息
 * @returns {Promise} 发票信息
 */
export function createInvoice(data) {
  return request({
    url: '/v1/invoices',
    method: 'post',
    data
  })
}

/**
 * 更新发票
 * @param {number} id - 发票ID
 * @param {Object} data - 发票信息
 * @returns {Promise} 发票信息
 */
export function updateInvoice(id, data) {
  return request({
    url: `/v1/invoices/${id}`,
    method: 'put',
    data
  })
}

/**
 * 作废发票
 * @param {number} id - 发票ID
 * @param {string} reason - 作废原因
 * @returns {Promise} 操作结果
 */
export function voidInvoice(id, reason) {
  return request({
    url: `/v1/invoices/${id}/void`,
    method: 'post',
    params: { reason }
  })
}

/**
 * 红冲发票
 * @param {number} id - 发票ID
 * @param {string} reason - 红冲原因
 * @returns {Promise} 操作结果
 */
export function redInvoice(id, reason) {
  return request({
    url: `/v1/invoices/${id}/red`,
    method: 'post',
    params: { reason }
  })
}

/**
 * 删除发票
 * @param {number} id - 发票ID
 * @returns {Promise} 操作结果
 */
export function deleteInvoice(id) {
  return request({
    url: `/v1/invoices/${id}`,
    method: 'delete'
  })
}
