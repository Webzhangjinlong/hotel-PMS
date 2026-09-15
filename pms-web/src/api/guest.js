import request from '@/utils/request'

/**
 * 查询客人列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 客人列表
 */
export function getGuestList(params) {
  return request({
    url: '/v1/guests',
    method: 'get',
    params
  })
}

/**
 * 查询客人详情
 * @param {number} id - 客人ID
 * @returns {Promise} 客人信息
 */
export function getGuestById(id) {
  return request({
    url: '/v1/guests/' + id,
    method: 'get'
  })
}

/**
 * 创建客人
 * @param {Object} data - 客人信息
 * @returns {Promise} 客人信息
 */
export function createGuest(data) {
  return request({
    url: '/v1/guests',
    method: 'post',
    data
  })
}

/**
 * 设置客人VIP状态
 * @param {number} id - 客人ID
 * @param {boolean} isVip - VIP状态
 * @returns {Promise} 更新后的客人信息
 */
export function setVipStatus(id, isVip) {
  return request({
    url: '/v1/guests/' + id + '/vip',
    method: 'put',
    params: { isVip }
  })
}

/**
 * 将客人加入黑名单
 * @param {number} id - 客人ID
 * @param {string} reason - 黑名单原因
 * @returns {Promise} 更新后的客人信息
 */
export function addToBlacklist(id, reason) {
  return request({
    url: '/v1/guests/' + id + '/blacklist/add',
    method: 'put',
    params: { reason }
  })
}

/**
 * 将客人从黑名单移出
 * @param {number} id - 客人ID
 * @returns {Promise} 更新后的客人信息
 */
export function removeFromBlacklist(id) {
  return request({
    url: '/v1/guests/' + id + '/blacklist/remove',
    method: 'put'
  })
}

/**
 * 获取客人入住历史
 * @param {number} guestId - 客人ID
 * @returns {Promise} 入住历史列表
 */
export function getStayHistory(guestId) {
  return request({
    url: '/v1/guests/' + guestId + '/history',
    method: 'get'
  })
}
