import request from '@/utils/request'

/** 获取统计数据 */
export function getDashboardStats(hotelId) {
  return request({ url: '/v1/dashboard/stats', method: 'get', params: { hotelId } })
}

/** 获取今日营收 */
export function getTodayRevenue(hotelId) {
  return request({ url: '/v1/dashboard/today-revenue', method: 'get', params: { hotelId } })
}

/** 获取营收趋势 */
export function getRevenueTrend(hotelId, days = 7) {
  return request({ url: '/v1/dashboard/revenue-trend', method: 'get', params: { hotelId, days } })
}

/** 获取今日抵店列表 */
export function getTodayCheckins(hotelId) {
  return request({ url: '/v1/dashboard/today-checkins', method: 'get', params: { hotelId } })
}

/** 获取今日离店列表 */
export function getTodayDepartures(hotelId) {
  return request({ url: '/v1/dashboard/today-departures', method: 'get', params: { hotelId } })
}
