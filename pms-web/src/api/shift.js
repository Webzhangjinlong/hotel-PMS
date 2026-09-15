import request from '@/utils/request'

export function createShift(data) {
  return request({ url: '/v1/shifts', method: 'post', data })
}

export function updateShift(id, data) {
  return request({ url: '/v1/shifts/' + id, method: 'put', data })
}

export function submitShift(id) {
  return request({ url: '/v1/shifts/' + id + '/submit', method: 'post' })
}

export function deleteShift(id) {
  return request({ url: '/v1/shifts/' + id, method: 'delete' })
}

export function acceptShift(id) {
  return request({ url: '/v1/shifts/' + id + '/accept', method: 'post' })
}

export function confirmShift(id) {
  return request({ url: '/v1/shifts/' + id + '/confirm', method: 'post' })
}

export function rejectShift(id, reason) {
  return request({ url: '/v1/shifts/' + id + '/reject', method: 'post', params: { reason } })
}

export function getShiftList(status) {
  return request({ url: '/v1/shifts', method: 'get', params: { status } })
}

export function getShiftById(id) {
  return request({ url: '/v1/shifts/' + id, method: 'get' })
}

export function getPendingShifts() {
  return request({ url: '/v1/shifts/pending', method: 'get' })
}

export function getShiftStatistics(startTime, endTime) {
  return request({ url: '/v1/shifts/statistics', method: 'get', params: { startTime, endTime } })
}

export function getShiftMessages() {
  return request({ url: '/v1/shifts/messages', method: 'get' })
}

export function markMessageRead(id) {
  return request({ url: '/v1/shifts/messages/' + id + '/read', method: 'post' })
}

export function getNotifyConfig() {
  return request({ url: '/v1/shifts/notify-config', method: 'get' })
}

export function saveNotifyConfig(userIds) {
  return request({ url: '/v1/shifts/notify-config', method: 'post', data: userIds })
}

/**
 * 交班核对
 * @param {number} shiftId - 交班记录ID
 * @param {Object} params - 实际交接金额
 * @returns {Promise} 核对结果
 */
export function verifyShift(shiftId, params) {
  return request({ 
    url: '/v1/shifts/' + shiftId + '/verify', 
    method: 'post', 
    params: params 
  })
}

/**
 * 保存实际交接金额
 * @param {number} shiftId - 交班记录ID
 * @param {Object} params - 实际交接金额
 * @returns {Promise} 更新后的交班记录
 */
export function saveActualAmounts(shiftId, params) {
  return request({ 
    url: '/v1/shifts/' + shiftId + '/actual-amounts', 
    method: 'post', 
    params: params 
  })
}

/**
 * 获取交班报表
 * @param {number} shiftId - 交班记录ID
 * @returns {Promise} 交班报表数据
 */
export function getShiftReport(shiftId) {
  return request({ 
    url: '/v1/shifts/' + shiftId + '/report', 
    method: 'get' 
  })
}
