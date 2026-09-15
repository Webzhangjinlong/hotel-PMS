<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移除</p>
      <el-button type="primary" size="large" @click="goHome">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled } from '@element-plus/icons-vue'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.not-found-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.not-found-content {
  text-align: center;
  
  .error-code {
    font-size: 120px;
    font-weight: 700;
    color: #67c23a;
    margin: 0;
    line-height: 1;
    text-shadow: 4px 4px 0 rgba(103, 194, 58, 0.2);
  }
  
  .error-title {
    font-size: 24px;
    color: #303133;
    margin: 20px 0 10px;
  }
  
  .error-desc {
    font-size: 16px;
    color: #909399;
    margin: 0 0 30px;
  }
}
</style>