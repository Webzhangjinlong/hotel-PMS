import request from '@/utils/request'

/**
 * 获取夜审配置
 * @param {number} hotelId - 酒店ID
 * @returns {Promise} 夜审配置
 */
export function getNightAuditConfig(hotelId) {
  return request({
    url: '/v1/night-audit-config/' + hotelId,
    method: 'get'
  })
}

/**
 * 更新夜审配置
 * @param {number} hotelId - 酒店ID
 * @param {Object} data - 配置数据
 * @returns {Promise} 更新结果
 */
export function updateNightAuditConfig(hotelId, data) {
  return request({
    url: '/v1/night-audit-config/' + hotelId,
    method: 'put',
    data
  })
}