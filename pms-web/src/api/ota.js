import request from '@/utils/request'

/**
 * 查询OTA渠道列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 渠道列表
 */
export function getOtaChannelList(params) {
  return request({
    url: '/v1/ota/channels',
    method: 'get',
    params
  })
}

/**
 * 查询OTA渠道详情
 * @param {number} id - 渠道ID
 * @returns {Promise} 渠道信息
 */
export function getOtaChannelById(id) {
  return request({
    url: `/v1/ota/channels/${id}`,
    method: 'get'
  })
}

/**
 * 创建OTA渠道
 * @param {Object} data - 渠道信息
 * @returns {Promise} 渠道信息
 */
export function createOtaChannel(data) {
  return request({
    url: '/v1/ota/channels',
    method: 'post',
    data
  })
}

/**
 * 更新OTA渠道
 * @param {number} id - 渠道ID
 * @param {Object} data - 渠道信息
 * @returns {Promise} 渠道信息
 */
export function updateOtaChannel(id, data) {
  return request({
    url: `/v1/ota/channels/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除OTA渠道
 * @param {number} id - 渠道ID
 * @returns {Promise} 操作结果
 */
export function deleteOtaChannel(id) {
  return request({
    url: `/v1/ota/channels/${id}`,
    method: 'delete'
  })
}

/**
 * 获取所有启用的渠道
 * @param {number} hotelId - 酒店ID
 * @returns {Promise} 渠道列表
 */
export function getActiveChannels(hotelId) {
  return request({
    url: '/v1/ota/channels/active',
    method: 'get',
    params: { hotelId }
  })
}
