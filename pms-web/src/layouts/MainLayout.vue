<template>
  <div class="layout-container">
    <aside class="layout-sidebar" :class="{ 'is-collapse': isCollapse }">
      <div class="sidebar-logo">
        <el-icon :size="28" color="#67c23a">
          <OfficeBuilding />
        </el-icon>
        <span v-show="!isCollapse" class="logo-text">PMS 酒店管理</span>
      </div>
      <el-scrollbar>
          <el-menu :default-active="activeMenu" :collapse="isCollapse" :collapse-transition="false" router class="sidebar-menu">
            <el-menu-item index="/dashboard">
              <el-icon><Odometer /></el-icon><template #title>首页</template>
            </el-menu-item>
            <!-- 预订管理 -->
            <el-sub-menu index="reservation">
              <template #title><el-icon><Calendar /></el-icon><span>预订管理</span></template>
              <el-menu-item index="/reservation/list">预订列表</el-menu-item>
              <el-menu-item index="/reservation/team">团队预订</el-menu-item>
            </el-sub-menu>
            <!-- 前台操作 -->
            <el-sub-menu index="front-desk">
              <template #title><el-icon><User /></el-icon><span>前台操作</span></template>
              <el-menu-item index="/stays">入住管理</el-menu-item>
              <el-menu-item index="/room-board">房态看板</el-menu-item>
              <el-menu-item index="/room-calendar">房间预订日历</el-menu-item>
              <el-menu-item index="/folios">账务管理</el-menu-item>
              <el-menu-item index="/deposits">押金管理</el-menu-item>
              <el-menu-item index="/guests">客人档案</el-menu-item>
              <el-menu-item index="/transactions">交易流水</el-menu-item>
            </el-sub-menu>
            <!-- 会员中心 -->
            <el-sub-menu index="member">
              <template #title><el-icon><CreditCard /></el-icon><span>会员中心</span></template>
              <el-menu-item index="/member/management">会员管理</el-menu-item>
              <el-menu-item index="/member/levels">会员等级配置</el-menu-item>
            </el-sub-menu>
            <!-- 客房管理 -->
            <el-sub-menu index="housekeeping">
              <template #title><el-icon><House /></el-icon><span>客房管理</span></template>
              <el-menu-item index="/master/room">房间管理</el-menu-item>
              <el-menu-item index="/master/floor">楼层管理</el-menu-item>
              <el-menu-item index="/master/room-type">房型管理</el-menu-item>
              <el-menu-item index="/housekeeping/clean">清洁任务</el-menu-item>
              <el-menu-item index="/housekeeping/maintenance">维修管理</el-menu-item>
            </el-sub-menu>
            <!-- 财务管理 -->
            <el-sub-menu index="finance-management">
              <template #title><el-icon><Money /></el-icon><span>财务管理</span></template>
              <el-menu-item index="/price/management">房价管理</el-menu-item>
              <el-menu-item index="/price/plan">房价码管理</el-menu-item>
              <el-menu-item index="/reports/daily">营业日报</el-menu-item>
              <el-menu-item index="/reports/metrics">经营指标</el-menu-item>
              <el-menu-item index="/agreement">协议单位管理</el-menu-item>
              <el-menu-item index="/invoice">发票管理</el-menu-item>
            </el-sub-menu>
            <!-- 渠道与设备 -->
            <el-sub-menu index="channel-device">
              <template #title><el-icon><Connection /></el-icon><span>渠道与设备</span></template>
              <el-menu-item index="/ota/channels">OTA渠道管理</el-menu-item>
              <el-menu-item index="/idcard/readers">身份证阅读器</el-menu-item>
              <el-menu-item index="/doorlock">门锁/房卡管理</el-menu-item>
            </el-sub-menu>
            <!-- 夜审管理 -->
            <el-sub-menu index="night-audit">
              <template #title><el-icon><Moon /></el-icon><span>夜审管理</span></template>
              <el-menu-item index="/night-audit">执行夜审</el-menu-item>
              <el-menu-item index="/night-audit/records">夜审记录</el-menu-item>
              <el-menu-item index="/night-audit/config">夜审配置</el-menu-item>
              <el-menu-item index="/night-audit/archive">历史数据</el-menu-item>
            </el-sub-menu>
            <!-- 系统设置 -->
            <el-sub-menu index="system">
              <template #title><el-icon><Setting /></el-icon><span>系统设置</span></template>
              <el-menu-item index="/master/hotel">酒店管理</el-menu-item>
              <el-menu-item index="/accounts">账号管理</el-menu-item>
              <el-menu-item index="/roles">角色管理</el-menu-item>
              <el-menu-item index="/permissions">权限管理</el-menu-item>
              <el-menu-item index="/shifts">交班管理</el-menu-item>
              <el-menu-item index="/operation-logs">操作日志</el-menu-item>
              <el-menu-item index="/police-upload">公安上传</el-menu-item>
              <el-menu-item index="/system/hotel-config">超时离店配置</el-menu-item>
                          </el-sub-menu>
              <!-- 收银报表 -->
              <el-sub-menu index="cashier-report">
                <template #title><el-icon><DataAnalysis /></el-icon><span>收银报表</span></template>
                <el-menu-item index="/reports/cashier/shift">收银员交接表</el-menu-item>
                <el-menu-item index="/reports/cashier/entry-detail">前台入账明细</el-menu-item>
                <el-menu-item index="/reports/cashier/entry-summary">前台入账简表</el-menu-item>
                <el-menu-item index="/reports/cashier/entry-total">前台入账汇总</el-menu-item>
                <el-menu-item index="/reports/cashier/payment-detail">前台收款明细</el-menu-item>
                <el-menu-item index="/reports/cashier/payment-summary">前台收款汇总</el-menu-item>
                <el-menu-item index="/reports/cashier/transfer">前台转账报表</el-menu-item>
                <el-menu-item index="/reports/cashier/chargeback">冲账调账报表</el-menu-item>
                <el-menu-item index="/reports/cashier/checkout-stats">结账实收统计</el-menu-item>
                <el-menu-item index="/reports/cashier/checkout-detail">结账实收明细</el-menu-item>
                <el-menu-item index="/reports/cashier/product-summary">商品销售汇总</el-menu-item>
                <el-menu-item index="/reports/cashier/product-detail">商品销售明细</el-menu-item>
              </el-sub-menu>
            </el-menu>
      </el-scrollbar>
    </aside>
    <div class="layout-main">
      <header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" :size="20" @click="isCollapse = !isCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">{{ item.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" icon="UserFilled" />
              <span class="user-name">{{ userStore.realName || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Odometer, Calendar, User, House, Money, Moon, Setting, CreditCard, Connection,
  Expand, Fold, ArrowDown, UserFilled, OfficeBuilding
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const activeMenu = computed(() => {
  return route.path
})

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人信息页面
      break
    case 'logout':
      await userStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ========== 侧边栏 ========== */
.layout-sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1a1f36 0%, #0d1117 100%);
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  z-index: 10;

  &.is-collapse {
    width: 64px;
  }

  .sidebar-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 0 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);

    .logo-text {
      color: #ffffff;
      font-size: 18px;
      font-weight: 700;
      letter-spacing: 1px;
    }
  }
}

/* ========== 菜单分隔符 ========== */
.menu-divider {
  padding: 16px 20px 8px;
  position: relative;

  span {
    font-size: 11px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 1.2px;
    color: rgba(255, 255, 255, 0.3);
  }
}

/* ========== 菜单样式 ========== */
.sidebar-menu {
  border-right: none !important;
  background: transparent !important;
  flex: 1;
  padding: 4px 0;

  /* 一级菜单项（子菜单标题） */
  :deep(.el-sub-menu__title) {
    color: rgba(255, 255, 255, 0.65) !important;
    height: 44px;
    line-height: 44px;
    margin: 2px 10px;
    border-radius: 8px;
    font-weight: 600;
    font-size: 14.5px;
    background: rgba(255, 255, 255, 0.03);
    border-left: 3px solid rgba(255, 255, 255, 0.15);
    transition: all 0.25s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.08);
      color: rgba(255, 255, 255, 0.95) !important;
    }

    .el-icon {
      font-size: 18px;
      margin-right: 10px;
    }
  }

  /* 顶部工作台菜单项（无子菜单的一级项） */
  :deep(.el-menu-item) {
    color: rgba(255, 255, 255, 0.65) !important;
    height: 44px;
    line-height: 44px;
    margin: 2px 10px;
    border-radius: 8px;
    transition: all 0.25s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.08);
      color: rgba(255, 255, 255, 0.95) !important;
    }

    .el-icon {
      font-size: 18px;
      margin-right: 10px;
    }
  }

  /* 子菜单展开容器背景透明 */
  :deep(.el-menu--inline) {
    background: transparent !important;
  }

  /* 二级菜单项 - 视觉降级，明确层级 */
  :deep(.el-menu--inline .el-menu-item) {
    color: rgba(255, 255, 255, 0.5) !important;
    padding-left: 56px !important;
    height: 40px;
    line-height: 40px;
    font-size: 13px;
    position: relative;

    /* 左侧圆点指示器 */
    &::before {
      content: '';
      position: absolute;
      left: 28px;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 4px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.25);
      transition: all 0.25s ease;
    }

    &:hover {
      color: rgba(255, 255, 255, 0.9) !important;
      background: rgba(255, 255, 255, 0.06);

      &::before {
        background: rgba(255, 255, 255, 0.5);
      }
    }
  }

  /* 二级菜单选中态 - 绿色高亮 + 发光 */
  :deep(.el-menu--inline .el-menu-item.is-active) {
    color: #67c23a !important;
    background: rgba(103, 194, 58, 0.08);
    font-weight: 500;
    box-shadow: 0 0 8px rgba(103, 194, 58, 0.1);

    &::before {
      background: #67c23a;
      box-shadow: 0 0 6px rgba(103, 194, 58, 0.5);
      width: 5px;
      height: 5px;
    }
  }

  /* 一级菜单选中态 - 发光药丸 */
  :deep(.el-menu-item.is-active) {
    background: linear-gradient(135deg, rgba(103, 194, 58, 0.25), rgba(103, 194, 58, 0.12));
    color: #67c23a !important;
    box-shadow:
      0 0 12px rgba(103, 194, 58, 0.2),
      inset 0 0 20px rgba(103, 194, 58, 0.05);
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 8px;
      bottom: 8px;
      width: 3px;
      border-radius: 0 3px 3px 0;
      background: #67c23a;
      box-shadow: 0 0 8px rgba(103, 194, 58, 0.6);
    }
  }

  /* 子菜单展开时的背景 */
  :deep(.el-sub-menu.is-opened) {
    > .el-sub-menu__title {
      color: rgba(255, 255, 255, 0.9) !important;
      background: rgba(255, 255, 255, 0.05);
      border-left-color: rgba(103, 194, 58, 0.4);
    }
  }

  /* 折叠态样式 */
  .is-collapse & {
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      margin: 2px 6px;
    }

    :deep(.el-menu-item.is-active) {
      &::before {
        display: none;
      }
    }

    :deep(.el-sub-menu__title) {
      border-left: none;
    }
  }
}
/* ========== 主内容区 ========== */
.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ========== 顶部导航 ========== */
.layout-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: relative;
  z-index: 5;

  .header-left {
    display: flex;
    align-items: center;

    .collapse-btn {
      cursor: pointer;
      color: #606266;
      padding: 4px;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        color: #67c23a;
        background: rgba(103, 194, 58, 0.08);
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
      padding: 6px 12px;
      border-radius: 8px;
      transition: background 0.2s;

      &:hover {
        background: #f5f7fa;
      }

      .user-name {
        margin: 0 8px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

/* ========== 内容区域 ========== */
.layout-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f7fa;
}

/* ========== 过渡动画 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>


