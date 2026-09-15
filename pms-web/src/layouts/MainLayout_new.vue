<style lang="scss" scoped>
.layout-container {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

// 侧边栏
.layout-sidebar {
  width: 240px;
  height: 100%;
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
  
  &.is-collapse {
    width: 64px;
  }
  
  .sidebar-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    background: #263445;
    
    .logo-text {
      margin-left: 12px;
      font-size: 18px;
      font-weight: 600;
      color: #ffffff;
      white-space: nowrap;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    background: transparent;
    
    // 一级菜单项样式
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      color: #bfcbd9;
      
      &:hover {
        background: #263445;
        color: #ffffff;
      }
    }
    
    // 一级菜单展开状态
    :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
      color: #ffffff;
      background: #263445;
    }
    
    // 二级菜单容器背景
    :deep(.el-sub-menu .el-menu) {
      background: #f0f2f5;
    }
    
    // 二级菜单项样式
    :deep(.el-sub-menu .el-menu-item) {
      color: #303133;
      background: #f0f2f5;
      border-left: 3px solid transparent;
      padding-left: 47px;
      
      &:hover {
        background: #ffffff;
        color: #67c23a;
      }
      
      &.is-active {
        background: #ffffff;
        color: #67c23a;
        border-left-color: #67c23a;
        padding-left: 47px;
      }
    }
    
    // 一级菜单选中状态（保持深色风格）
    :deep(.el-menu-item.is-active) {
      background: #67c23a;
      color: #ffffff;
    }
  }
}

// 主内容区
.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 顶部导航栏
.layout-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      cursor: pointer;
      color: #606266;
      
      &:hover {
        color: #67c23a;
      }
    }
    
    .el-breadcrumb {
      margin-left: 16px;
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 0 8px;
      
      .user-name {
        margin: 0 8px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

// 页面内容
.layout-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f7fa;
}

// 路由过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
