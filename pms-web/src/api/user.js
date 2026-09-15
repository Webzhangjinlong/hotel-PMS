import request from '@/utils/request'

export function getUserList() {
  return request({
    url: '/v1/users',
    method: 'get'
  })
}

export function getUserById(id) {
  return request({
    url: '/v1/users/' + id,
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/v1/users',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: '/v1/users/' + id,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: '/v1/users/' + id,
    method: 'delete'
  })
}

export function resetPassword(id, newPassword) {
  return request({
    url: '/v1/users/' + id + '/reset-password',
    method: 'post',
    params: { newPassword }
  })
}

export function assignUserRoles(data) {
  return request({
    url: '/v1/users/roles',
    method: 'post',
    data
  })
}

export function getUserRoles(userId) {
  return request({
    url: '/v1/users/' + userId + '/roles',
    method: 'get'
  })
}

export function getRoleList() {
  return request({
    url: '/v1/roles',
    method: 'get'
  })
}
