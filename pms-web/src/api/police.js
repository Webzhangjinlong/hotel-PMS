import request from '@/utils/request'

/**
 * 查询公安上传记录
 * @param {Object} params - 查询参数 { hotelId, status, guestName, startDate, endDate, page, size }
 * @returns {Promise} 上传记录列表
 */
export function getPoliceUploadList(params) {
  return request({
    url: '/v1/police/uploads',
    method: 'get',
    params
  })
}

/**
 * 查询上传记录详情
 * @param {number} id - 记录ID
 * @returns {Promise} 记录详情
 */
export function getPoliceUploadById(id) {
  return request({
    url: '/v1/police/uploads/' + id,
    method: 'get'
  })
}

/**
 * 重试上传失败的记录
 * @param {number} id - 记录ID
 * @returns {Promise} 重试结果
 */
export function retryPoliceUpload(id) {
  return request({
    url: '/v1/police/uploads/' + id + '/retry',
    method: 'post'
  })
}

/**
 * 批量重试上传失败的记录
 * @param {Array} ids - 记录ID数组
 * @returns {Promise} 重试结果
 */
export function batchRetryPoliceUpload(ids) {
  return request({
    url: '/v1/police/uploads/batch-retry',
    method: 'post',
    data: { ids }
  })
}

/**
 * 人工补传
 * @param {Object} data - 补传数据 { stayId, guestName, guestIdNo, guestPhone, checkInTime, remark }
 * @returns {Promise} 补传结果
 */
export function manualPoliceUpload(data) {
  return request({
    url: '/v1/police/uploads/manual',
    method: 'post',
    data
  })
}

/**
 * 查询上传统计
 * @param {Object} params - 查询参数 { hotelId, startDate, endDate }
 * @returns {Promise} 统计数据
 */
export function getPoliceUploadStats(params) {
  return request({
    url: '/v1/police/uploads/stats',
    method: 'get',
    params
  })
}
