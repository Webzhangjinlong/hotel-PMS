import request from '@/utils/request'

/**
 * 归档历史数据
 * @param {number} hotelId - 酒店ID
 * @param {string} beforeDate - 归档此日期之前的数据
 * @param {string} archivedBy - 归档人
 * @returns {Promise} 归档结果
 */
export function archiveNightAuditData(hotelId, beforeDate, archivedBy = 'system') {
  return request({
    url: '/v1/night-audit/archive/archive',
    method: 'post',
    params: { hotelId, beforeDate, archivedBy }
  })
}

/**
 * 查询归档数据列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 归档数据列表
 */
export function getArchiveList(params) {
  return request({
    url: '/v1/night-audit/archive/list',
    method: 'get',
    params
  })
}

/**
 * 查询归档统计
 * @param {number} hotelId - 酒店ID
 * @param {string} beforeDate - 统计此日期之前的数据
 * @returns {Promise} 归档统计
 */
export function getArchiveStats(hotelId, beforeDate) {
  return request({
    url: '/v1/night-audit/archive/stats',
    method: 'get',
    params: { hotelId, beforeDate }
  })
}