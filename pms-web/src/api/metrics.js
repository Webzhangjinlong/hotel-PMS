import request from '@/utils/request'

export function getMetrics(params) {
  return request({
    url: '/v1/metrics',
    method: 'get',
    params
  })
}
