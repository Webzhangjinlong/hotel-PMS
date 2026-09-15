import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'
import { login, getUserInfo, logout } from '@/api/auth'

// Token 存储的 Cookie 名称
const TOKEN_KEY = 'pms_token'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // ========== 状态 ==========
  
  /** Token */
  const token = ref(Cookies.get(TOKEN_KEY) || '')
  
  /** 用户信息 */
  const userInfo = ref(null)
  
  /** 权限列表 */
  const permissions = ref([])
  
  // ========== 计算属性 ==========
  
  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)
  
  /** 用户名 */
  const username = computed(() => userInfo.value?.username || '')
  
  /** 真实姓名 */
  const realName = computed(() => userInfo.value?.realName || '')
  
  /** 角色 */
  const role = computed(() => userInfo.value?.role || '')
  
  /** 酒店ID */
  const hotelId = computed(() => userInfo.value?.hotelId || null)
  
  /** 酒店名称 */
  const hotelName = computed(() => userInfo.value?.hotelName || '')
  
  // ========== 方法 ==========
  
  /**
   * 设置 Token
   * @param {string} newToken - 新的 Token
   */
  function setToken(newToken) {
    token.value = newToken
    Cookies.set(TOKEN_KEY, newToken, { expires: 1 }) // 1天过期
  }
  
  /**
   * 清除 Token
   */
  function clearToken() {
    token.value = ''
    Cookies.remove(TOKEN_KEY)
  }
  
  /**
   * 登录
   * @param {Object} loginData - 登录数据
   * @param {string} loginData.username - 用户名
   * @param {string} loginData.password - 密码
   * @param {string} loginData.captcha - 验证码
   * @param {string} loginData.captchaKey - 验证码Key
   * @returns {Promise} 登录结果
   */
  async function loginAction(loginData) {
    try {
      // 【调用登录接口】
      const res = await login(loginData)
      
      // 【保存 Token】
      setToken(res.data.token)
      
      // 【保存用户信息】
      userInfo.value = res.data.userInfo
      
      return res
    } catch (error) {
      throw error
    }
  }
  
  /**
   * 获取用户信息
   * @returns {Promise} 用户信息
   */
  async function getUserInfoAction() {
    try {
      // 【调用获取用户信息接口】
      const res = await getUserInfo()
      
      // 【保存用户信息】
      userInfo.value = res.data
      
      return res
    } catch (error) {
      throw error
    }
  }
  
  /**
   * 退出登录
   */
  async function logoutAction() {
    try {
      // 【调用退出登录接口】
      await logout()
    } catch (error) {
      // 即使接口调用失败，也清除本地状态
      console.error('退出登录接口调用失败:', error)
    } finally {
      // 【清除本地状态】
      clearToken()
      userInfo.value = null
      permissions.value = []
    }
  }
  
  /**
   * 重置状态
   */
  function resetState() {
    clearToken()
    userInfo.value = null
    permissions.value = []
  }
  
  return {
    // 状态
    token,
    userInfo,
    permissions,
    
    // 计算属性
    isLoggedIn,
    username,
    realName,
    role,
    hotelId,
    hotelName,
    
    // 方法
    setToken,
    clearToken,
    loginAction,
    getUserInfo: getUserInfoAction,
    logout: logoutAction,
    resetState
  }
})