import request from '@/utils/request'

/**
 * 执行夜审
 * @param {number} hotelId - 酒店ID
 * @returns {Promise} 夜审结果
 */
export function executeNightAudit(hotelId) {
  return request({
    url: '/v1/night-audit/execute',
    method: 'post',
    params: { hotelId }
  })
}

/**
 * 查询夜审记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 夜审记录列表
 */
export function getNightAuditList(params) {
  return request({
    url: '/v1/night-audit/list',
    method: 'get',
    params
  })
}

/**
 * 查询夜审详情
 * @param {number} id - 夜审记录ID
 * @returns {Promise} 夜审详情
 */
export function getNightAuditDetail(id) {
  return request({
    url: '/v1/night-audit/' + id,
    method: 'get'
  })
}

/**
 * 重试失败的夜审步骤
 * @param {number} auditId - 夜审记录ID
 * @param {number} stepId - 步骤ID
 * @returns {Promise} 重试结果
 */
export function retryNightAuditStep(auditId, stepId) {
  return request({
    url: '/v1/night-audit/' + auditId + '/steps/' + stepId + '/retry',
    method: 'post'
  })
}

/**
 * 重置夜审状态（用于卡在IN_PROGRESS的情况）
 * @param {number} auditId - 夜审记录ID
 * @returns {Promise} 重置结果
 */
export function resetNightAuditStatus(auditId) {
  return request({
    url: '/v1/night-audit/' + auditId + '/reset',
    method: 'post'
  })
}
