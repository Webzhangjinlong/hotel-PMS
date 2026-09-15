import request from '@/utils/request'

/**
 * 查询会员列表
 */
export function getMemberList(params) {
  return request({ url: '/v1/members', method: 'get', params })
}

/**
 * 查询会员详情
 */
export function getMemberById(id) {
  return request({ url: '/v1/members/' + id, method: 'get' })
}

/**
 * 根据手机号查询会员
 */
export function getMemberByPhone(phone) {
  return request({ url: '/v1/members/phone/' + phone, method: 'get' })
}

/**
 * 注册会员
 */
export function registerMember(data) {
  return request({ url: '/v1/members', method: 'post', data })
}

/**
 * 更新会员信息
 */
export function updateMember(id, data) {
  return request({ url: '/v1/members/' + id, method: 'put', data })
}

/**
 * 积分抵扣
 */
export function exchangePoints(id, data) {
  return request({ url: '/v1/members/' + id + '/exchange-points', method: 'post', data })
}

/**
 * 查询积分流水
 */
export function getPointsLogs(id) {
  return request({ url: '/v1/members/' + id + '/points-logs', method: 'get' })
}

/**
 * 会员统计
 */
export function getMemberStatistics() {
  return request({ url: '/v1/members/statistics', method: 'get' })
}

/**
 * 手动调整积分
 */
export function adjustPoints(id, params) {
  return request({ url: '/v1/members/' + id + '/adjust-points', method: 'post', params })
}

// ========== 等级管理 ==========

/**
 * 查询等级列表
 */
export function getLevelList() {
  return request({ url: '/v1/member-levels', method: 'get' })
}

/**
 * 更新等级配置
 */
export function updateLevel(id, data) {
  return request({ url: '/v1/member-levels/' + id, method: 'put', data })
}

/**
 * 初始化默认等级
 */
export function initLevels() {
  return request({ url: '/v1/member-levels/init', method: 'post' })
}
