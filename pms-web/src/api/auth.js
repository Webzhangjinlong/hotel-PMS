import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data - 登录参数
 * @param {string} data.hotelCode - 酒店标识码
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.captcha - 验证码
 * @param {string} data.captchaKey - 验证码Key
 * @returns {Promise} 登录结果
 */
export function login(data) {
  return request({
    url: '/v1/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} 用户信息
 */
export function getUserInfo() {
  return request({
    url: '/v1/auth/current',
    method: 'get'
  })
}

/**
 * 退出登录
 * @returns {Promise} 退出结果
 */
export function logout() {
  return request({
    url: '/v1/auth/logout',
    method: 'post'
  })
}

/**
 * 获取验证码
 * @returns {Promise} 验证码信息
 */
export function getCaptcha() {
  return request({
    url: '/v1/auth/captcha',
    method: 'get'
  })
}