import request from '@/utils/request'

/**
 * 收取押金
 * @param {Object} data - 押金收取参数
 * @returns {Promise} 押金信息
 */
export function collectDeposit(data) {
  return request({
    url: '/v1/deposits/collect',
    method: 'post',
    data
  })
}

/**
 * 退还押金
 * @param {number} id - 押金ID
 * @param {Object} data - 退还参数
 * @returns {Promise} 更新后的押金信息
 */
export function refundDeposit(id, data) {
  return request({
    url: `/v1/deposits/${id}/refund`,
    method: 'post',
    data
  })
}

/**
 * 查询押金列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 押金列表
 */
export function getDepositList(params) {
  return request({
    url: '/v1/deposits',
    method: 'get',
    params
  })
}

/**
 * 查询押金详情
 * @param {number} id - 押金ID
 * @returns {Promise} 押金信息
 */
export function getDepositById(id) {
  return request({
    url: `/v1/deposits/${id}`,
    method: 'get'
  })
}

/**
 * 根据入住单ID查询押金
 * @param {number} stayId - 入住单ID
 * @returns {Promise} 押金列表
 */
export function getDepositsByStayId(stayId) {
  return request({
    url: `/v1/deposits/stay/${stayId}`,
    method: 'get'
  })
}

/**
 * 押金抵扣房费
 * @param {number} id - 押金ID
 * @param {Object} data - 抵扣参数
 * @returns {Promise} 更新后的押金信息
 */
export function deductDepositForRoomFee(id, data) {
  return request({
    url: `/v1/deposits/${id}/deduct`,
    method: 'post',
    data
  })
}
