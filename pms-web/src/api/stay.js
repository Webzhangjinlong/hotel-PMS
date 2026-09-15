import request from '@/utils/request'

/**
 * 鏁ｅ鍏ヤ綇
 * @param {Object} data - 鍏ヤ綇鍙傛暟
 * @returns {Promise} 鍏ヤ綇缁撴灉
 */
export function walkInCheckIn(data) {
  return request({
    url: '/v1/stays/walk-in',
    method: 'post',
    data
  })
}

/**
 * 棰勮鍏ヤ綇
 * @param {Object} data - 鍏ヤ綇鍙傛暟
 * @returns {Promise} 鍏ヤ綇缁撴灉
 */
export function reservationCheckIn(data) {
  return request({
    url: '/v1/stays/check-in',
    method: 'post',
    data
  })
}

/**
 * 鏌ヨ鍏ヤ綇鍒楄〃
 * @param {Object} params - 鏌ヨ鍙傛暟
 * @returns {Promise} 鍏ヤ綇鍒楄〃
 */
export function getStayList(params) {
  return request({
    url: '/v1/stays',
    method: 'get',
    params
  })
}

/**
 * 鏌ヨ鍏ヤ綇璇︽儏
 * @param {number} id - 鍏ヤ綇鍗旾D
 * @returns {Promise} 鍏ヤ綇璇︽儏
 */
export function getStayById(id) {
  return request({
    url: `/v1/stays/${id}`,
    method: 'get'
  })
}

/**
 * 鏌ヨ浠婃棩鍏ヤ綇鍒楄〃
 * @param {number} hotelId - 閰掑簵ID
 * @returns {Promise} 浠婃棩鍏ヤ綇鍒楄〃
 */
export function getTodayCheckIns(hotelId) {
  return request({
    url: '/v1/stays/today-checkins',
    method: 'get',
    params: { hotelId }
  })
}
/**
 * 缁綇
 * @param {Object} data - 缁綇鍙傛暟 { stayId, newCheckOutDate }
 * @returns {Promise} 缁綇缁撴灉
 */
export function extendStay(data) {
  return request({
    url: '/v1/stays/extend',
    method: 'put',
    data
  })
}
/**
 * 退房
 * @param {Object} data - 退房参数 { stayId, remark }
 * @returns {Promise} 退房结果
 */
export function checkOut(data) {
  return request({
    url: '/v1/stays/check-out',
    method: 'put',
    data
  })
}

/**
 * 散客入住单转入团队预订
 * @param {number} stayId - 入住单ID
 * @param {Object} data - { teamReservationId }
 * @returns {Promise} 转入结果
 */
export function transferToTeam(stayId, data) {
  return request({
    url: `/v1/stays/${stayId}/transfer-to-team`,
    method: 'post',
    data
  })
}



/**
 * 分页查询团队入住汇总
 * @param {Object} params - 查询参数
 * @returns {Promise} 团队汇总分页结果
 */
export function getTeamStaySummary(params) {
  return request({
    url: '/v1/stays/team-summary',
    method: 'get',
    params
  })
}

/**
 * 换房
 * @param {Object} data - 换房参数 { stayId, newRoomId, reason }
 * @returns {Promise} 换房结果
 */
export function changeRoom(data) {
  return request({
    url: '/v1/stays/change-room',
    method: 'put',
    data
  })
}

// ========== 同住人管理 ==========

/**
 * 查询入住单的同住人列表
 * @param {number} stayId - 入住单ID
 * @returns {Promise} 同住人列表
 */
export function getStayGuests(stayId) {
  return request({ url: '/v1/stay-guests/stay/' + stayId, method: 'get' })
}

/**
 * 添加同住人
 * @param {number} stayId - 入住单ID
 * @param {number} hotelId - 酒店ID
 * @param {Object} data - 同住人信息
 * @returns {Promise} 添加的同住人
 */
export function addCoGuest(stayId, hotelId, data) {
  return request({ url: '/v1/stay-guests/stay/' + stayId + '?hotelId=' + hotelId, method: 'post', data })
}

/**
 * 删除同住人
 * @param {number} id - 同住人ID
 * @returns {Promise} 操作结果
 */
export function removeCoGuest(id) {
  return request({ url: '/v1/stay-guests/' + id, method: 'delete' })
}

/**
 * 查询入住单的客人姓名列表
 * @param {number} stayId - 入住单ID
 * @returns {Promise} 姓名列表
 */
export function getStayGuestNames(stayId) {
  return request({ url: '/v1/stay-guests/stay/' + stayId + '/names', method: 'get' })
}
