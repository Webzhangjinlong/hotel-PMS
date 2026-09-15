<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="login-bg-circle"></div>
      <div class="login-bg-circle"></div>
      <div class="login-bg-circle"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo 和标题 -->
      <div class="login-header">
        <div class="login-logo">
          <el-icon :size="48" color="#67c23a">
            <OfficeBuilding />
          </el-icon>
        </div>
        <h1 class="login-title">PMS 酒店管理系统</h1>
        <p class="login-subtitle">Hotel Property Management System</p>
      </div>
      
      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <!-- 酒店标识码 -->
        <el-form-item prop="hotelCode">
          <el-input
            v-model="loginForm.hotelCode"
            placeholder="请输入酒店标识码"
            size="large"
            :prefix-icon="OfficeBuilding"
          />
        </el-form-item>
        
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        
        <!-- 验证码 -->
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              size="large"
              :prefix-icon="Key"
              class="captcha-input"
            />
            <div class="captcha-img" @click="refreshCaptcha">
              <img v-if="captchaImg" :src="captchaImg" alt="验证码" />
              <div v-else class="captcha-placeholder">
                <el-icon :size="20"><Loading /></el-icon>
              </div>
            </div>
          </div>
        </el-form-item>
        
        <!-- 记住密码 -->
        <el-form-item>
          <div class="login-options">
            <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
          </div>
        </el-form-item>
        
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 底部信息 -->
      <div class="login-footer">
        <p>© 2026 PMS 酒店管理系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, Loading, OfficeBuilding } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCaptcha } from '@/api/auth'

// ========== 路由和状态 ==========
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ========== 表单相关 ==========
const loginFormRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')
const captchaKey = ref('')

// 登录表单数据
const loginForm = reactive({
    hotelCode: '',
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
  remember: false
})

// 表单验证规则
const loginRules = {
    hotelCode: [
        { required: true, message: '请输入酒店标识码', trigger: 'blur' },
        { min: 2, max: 50, message: '酒店标识码长度在 2 到 50 个字符', trigger: 'blur' }
    ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为 4 位', trigger: 'blur' }
  ]
}

// ========== 方法 ==========

/**
 * 刷新验证码
 */
async function refreshCaptcha() {
  try {
    const res = await getCaptcha()
    captchaImg.value = `data:image/png;base64,${res.data.image}`
    captchaKey.value = res.data.key
    loginForm.captchaKey = res.data.key
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

/**
 * 处理登录
 */
async function handleLogin() {
  // 表单验证
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  
  try {
    // 【调用登录】
    await userStore.loginAction({
            hotelCode: loginForm.hotelCode,
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      captchaKey: loginForm.captchaKey
    })
    
    // 【记住密码】
    if (loginForm.remember) {
      localStorage.setItem('pms_hotelCode', loginForm.hotelCode)
            localStorage.setItem('pms_username', loginForm.username)
      localStorage.setItem('pms_password', btoa(loginForm.password)) // 简单加密
    } else {
      localStorage.removeItem('pms_username')
      localStorage.removeItem('pms_password')
    }
    
    // 【登录成功提示】
    ElMessage.success('登录成功')
    
    // 【跳转页面】
    const redirect = route.query.redirect || '/'
    router.push(redirect)
    
  } catch (error) {
    // 【登录失败】刷新验证码
    refreshCaptcha()
    loginForm.captcha = ''
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  // 【初始化验证码】
  refreshCaptcha()
  
  // 【恢复记住的密码】
  const savedUsername = localStorage.getItem('pms_username')
  const savedPassword = localStorage.getItem('pms_password')
  
  if (savedUsername && savedPassword) {
    if (savedHotelCode) {
            loginForm.hotelCode = savedHotelCode
        }
        loginForm.username = savedUsername
    loginForm.password = atob(savedPassword)
    loginForm.remember = true
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

// 背景装饰
.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  
  .login-bg-circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    
    &:nth-child(1) {
      width: 600px;
      height: 600px;
      top: -200px;
      right: -100px;
      animation: float 6s ease-in-out infinite;
    }
    
    &:nth-child(2) {
      width: 400px;
      height: 400px;
      bottom: -150px;
      left: -100px;
      animation: float 8s ease-in-out infinite reverse;
    }
    
    &:nth-child(3) {
      width: 300px;
      height: 300px;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      animation: float 10s ease-in-out infinite;
    }
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

// 登录卡片
.login-card {
  position: relative;
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

// 头部
.login-header {
  text-align: center;
  margin-bottom: 40px;
  
  .login-logo {
    margin-bottom: 16px;
  }
  
  .login-title {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }
  
  .login-subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

// 表单
.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
  
  .captcha-row {
    display: flex;
    gap: 12px;
    width: 100%;
    
    .captcha-input {
      flex: 1;
    }
    
    .captcha-img {
      width: 120px;
      height: 40px;
      cursor: pointer;
      border-radius: 4px;
      overflow: hidden;
      background: #f5f7fa;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .captcha-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
      }
    }
  }
  
  .login-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
  
  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    border-radius: 8px;
  }
}

// 底部
.login-footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  
  p {
    font-size: 12px;
    color: #909399;
    margin: 0;
  }
}
</style>
