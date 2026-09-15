import request from '@/utils/request'

/**
 * 分页查询操作日志
 */
export function getOperationLogList(params) {
  return request({
    url: '/v1/operation-logs',
    method: 'get',
    params
  })
}

/**
 * 查询操作日志详情
 */
export function getOperationLogById(id) {
  return request({
    url: `/v1/operation-logs/${id}`,
    method: 'get'
  })
}
