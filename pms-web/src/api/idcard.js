import request from '@/utils/request'

/**
 * 查询身份证阅读器设备列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 设备列表
 */
export function getReaderList(params) {
  return request({
    url: '/v1/idcard/readers',
    method: 'get',
    params
  })
}

/**
 * 查询身份证阅读器设备详情
 * @param {number} id - 设备ID
 * @returns {Promise} 设备信息
 */
export function getReaderById(id) {
  return request({
    url: `/v1/idcard/readers/${id}`,
    method: 'get'
  })
}

/**
 * 创建身份证阅读器设备
 * @param {Object} data - 设备信息
 * @returns {Promise} 设备信息
 */
export function createReader(data) {
  return request({
    url: '/v1/idcard/readers',
    method: 'post',
    data
  })
}

/**
 * 更新身份证阅读器设备
 * @param {number} id - 设备ID
 * @param {Object} data - 设备信息
 * @returns {Promise} 设备信息
 */
export function updateReader(id, data) {
  return request({
    url: `/v1/idcard/readers/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除身份证阅读器设备
 * @param {number} id - 设备ID
 * @returns {Promise} 操作结果
 */
export function deleteReader(id) {
  return request({
    url: `/v1/idcard/readers/${id}`,
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
    url: `/v1/idcard/readers/${id}/status`,
    method: 'put',
    params: { deviceStatus }
  })
}

/**
 * 获取所有启用的设备
 * @param {number} hotelId - 酒店ID
 * @returns {Promise} 设备列表
 */
export function getActiveReaders(hotelId) {
  return request({
    url: '/v1/idcard/readers/active',
    method: 'get',
    params: { hotelId }
  })
}

/**
 * 读取身份证信息
 * @param {number} deviceId - 设备ID
 * @returns {Promise} 身份证信息
 */
export function readIdcard(deviceId) {
  return request({
    url: `/v1/idcard/read/device/${deviceId}`,
    method: 'post'
  })
}

/**
 * 模拟读取身份证信息
 * @returns {Promise} 模拟的身份证信息
 */
export function simulateRead() {
  return request({
    url: '/v1/idcard/read/simulate',
    method: 'post'
  })
}

/**
 * 检查设备状态
 * @param {number} deviceId - 设备ID
 * @returns {Promise} 设备状态
 */
export function checkDeviceStatus(deviceId) {
  return request({
    url: `/v1/idcard/read/device/${deviceId}/status`,
    method: 'get'
  })
}
