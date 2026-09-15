import request from '@/utils/request'

export function getPermissionList() {
  return request({
    url: '/v1/permissions',
    method: 'get'
  })
}

export function getPermissionById(id) {
  return request({
    url: '/v1/permissions/' + id,
    method: 'get'
  })
}

export function createPermission(data) {
  return request({
    url: '/v1/permissions',
    method: 'post',
    data
  })
}

export function updatePermission(id, data) {
  return request({
    url: '/v1/permissions/' + id,
    method: 'put',
    data
  })
}

export function deletePermission(id) {
  return request({
    url: '/v1/permissions/' + id,
    method: 'delete'
  })
}
