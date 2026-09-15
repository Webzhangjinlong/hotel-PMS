import request from '@/utils/request'

/**
 * 查询酒店列表
 */
export function getHotelList(params) {
  return request({
    url: '/v1/hotels',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询酒店
 */
export function getHotelById(id) {
  return request({
    url: '/v1/hotels/' + id,
    method: 'get'
  })
}

/**
 * 创建酒店
 */
export function createHotel(data) {
  return request({
    url: '/v1/hotels',
    method: 'post',
    data
  })
}

/**
 * 更新酒店
 */
export function updateHotel(data) {
  return request({
    url: '/v1/hotels',
    method: 'put',
    data
  })
}

/**
 * 删除酒店
 */
export function deleteHotel(id) {
  return request({
    url: '/v1/hotels/' + id,
    method: 'delete'
  })
}

/**
 * 更新夜审配置
 */
export function updateNightAuditConfig(id, data) {
  return request({
    url: '/v1/hotels/' + id + '/night-audit-config',
    method: 'put',
    data
  })
}
