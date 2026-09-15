import request from '@/utils/request'

export function getRoleList() {
  return request({
    url: '/v1/roles',
    method: 'get'
  })
}

export function getRoleById(id) {
  return request({
    url: '/v1/roles/' + id,
    method: 'get'
  })
}

export function createRole(data) {
  return request({
    url: '/v1/roles',
    method: 'post',
    data
  })
}

export function updateRole(id, data) {
  return request({
    url: '/v1/roles/' + id,
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: '/v1/roles/' + id,
    method: 'delete'
  })
}

export function getRolePermissions(id) {
  return request({
    url: '/v1/roles/' + id + '/permissions',
    method: 'get'
  })
}

export function assignRolePermissions(id, permissionIds) {
  return request({
    url: '/v1/roles/' + id + '/permissions',
    method: 'post',
    data: permissionIds
  })
}

export function getAllPermissions() {
  return request({
    url: '/v1/permissions',
    method: 'get'
  })
}
