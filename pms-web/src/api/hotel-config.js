import request from '@/utils/request'

/**
 * 获取酒店配置
 * @param {number} hotelId - 酒店ID
 * @returns {Promise} 配置列表
 */
export function getHotelConfigs(hotelId) {
  return request({
    url: '/v1/hotel-config/' + hotelId,
    method: 'get'
  })
}

/**
 * 更新酒店配置
 * @param {number} hotelId - 酒店ID
 * @param {Object} data - 配置数据
 * @returns {Promise} 更新结果
 */
export function updateHotelConfigs(hotelId, data) {
  return request({
    url: '/v1/hotel-config/' + hotelId,
    method: 'put',
    data
  })
}
