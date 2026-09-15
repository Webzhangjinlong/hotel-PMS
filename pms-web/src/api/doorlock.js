import request from '@/utils/request'

/**
 * 查询门锁设备列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 设备列表
 */
export function getDoorLockList(params) {
  return request({
    url: '/v1/door-lock/configs',
    method: 'get',
    params
  })
}

/**
 * 查询门锁设备详情
 * @param {number} id - 设备ID
 * @returns {Promise} 设备信息
 */
export function getDoorLockById(id) {
  return request({
    url: `/v1/door-lock/configs/${id}`,
    method: 'get'
  })
}

/**
 * 创建门锁设备
 * @param {Object} data - 设备信息
 * @returns {Promise} 设备信息
 */
export function createDoorLock(data) {
  return request({
    url: '/v1/door-lock/configs',
    method: 'post',
    data
  })
}

/**
 * 更新门锁设备
 * @param {number} id - 设备ID
 * @param {Object} data - 设备信息
 * @returns {Promise} 设备信息
 */
export function updateDoorLock(id, data) {
  return request({
    url: `/v1/door-lock/configs/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除门锁设备
 * @param {number} id - 设备ID
 * @returns {Promise} 操作结果
 */
export function deleteDoorLock(id) {
  return request({
    url: `/v1/door-lock/configs/${id}`,
    method: 'delete'
  })
}

/**
 * 更新设备状态
 * @param {number} id - 设备ID
 * @param {string} deviceStatus - 设备状态
 * @returns {Promise} 操作结果
 */
export function updateDeviceStatus(id, deviceStatus) {
  return request({
    url: `/v1/door-lock/configs/${id}/status`,
    method: 'put',
    params: { deviceStatus }
  })
}

/**
 * 获取所有启用的设备
 * @param {number} hotelId - 酒店ID
 * @returns {Promise} 设备列表
 */
export function getActiveDoorLocks(hotelId) {
  return request({
    url: '/v1/door-lock/configs/active',
    method: 'get',
    params: { hotelId }
  })
}

/**
 * 发卡
 * @param {number} deviceId - 设备ID
 * @param {Object} cardInfo - 卡信息
 * @returns {Promise} 发卡结果
 */
export function issueCard(deviceId, cardInfo) {
  return request({
    url: `/v1/door-lock/cards/device/${deviceId}/issue`,
    method: 'post',
    data: cardInfo
  })
}

/**
 * 读卡
 * @param {number} deviceId - 设备ID
 * @returns {Promise} 卡信息
 */
export function readCard(deviceId) {
  return request({
    url: `/v1/door-lock/cards/device/${deviceId}/read`,
    method: 'post'
  })
}

/**
 * 注销卡
 * @param {number} deviceId - 设备ID
 * @param {string} cardNo - 卡号
 * @returns {Promise} 注销结果
 */
export function cancelCard(deviceId, cardNo) {
  return request({
    url: `/v1/door-lock/cards/device/${deviceId}/cancel`,
    method: 'post',
    params: { cardNo }
  })
}

/**
 * 检查设备状态
 * @param {number} deviceId - 设备ID
 * @returns {Promise} 设备状态
 */
export function checkDeviceStatus(deviceId) {
  return request({
    url: `/v1/door-lock/cards/device/${deviceId}/status`,
    method: 'get'
  })
}
