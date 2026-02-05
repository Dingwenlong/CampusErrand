<template>
  <a-layout class="layout-container">
    <!-- Desktop Sider -->
    <a-layout-sider
      v-if="!isMobile"
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="220"
      :collapsed-width="64"
      class="custom-sider"
      theme="light"
    >
      <div class="logo-container" :class="{ collapsed }">
        <div class="logo-box">
          <img src="/vite.svg" alt="logo" />
        </div>
        <h1 v-show="!collapsed" class="logo-text">座位预约与占座监督</h1>
      </div>
      
      <div class="menu-container">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          v-model:openKeys="openKeys"
          mode="inline"
          class="custom-menu"
          @click="handleMenuClick"
        >
          <sub-menu :menu-info="filteredMenus" />
        </a-menu>
      </div>
    </a-layout-sider>

    <!-- Mobile Drawer Sider -->
    <a-drawer
      v-else
      :open="!collapsed"
      placement="left"
      :closable="false"
      @close="collapsed = true"
      class="mobile-drawer-sider"
      :bodyStyle="{ padding: 0 }"
      width="220"
    >
      <div class="logo-container">
        <div class="logo-box">
          <img src="/vite.svg" alt="logo" />
        </div>
        <h1 class="logo-text">座位预约与占座监督</h1>
      </div>
      <div class="menu-container">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          v-model:openKeys="openKeys"
          mode="inline"
          class="custom-menu"
          @click="handleMobileMenuClick"
        >
          <sub-menu :menu-info="filteredMenus" />
        </a-menu>
      </div>
    </a-drawer>

    <a-layout class="site-layout">
      <a-layout-header class="custom-header">
        <div class="header-left">
          <div class="trigger-btn" @click="toggleSidebar">
            <menu-unfold-outlined v-if="collapsed" />
            <menu-fold-outlined v-else />
          </div>
          <div class="breadcrumb-wrapper" v-if="!isMobile">
            <a-breadcrumb>
              <a-breadcrumb-item>首页</a-breadcrumb-item>
              <a-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="index">
                {{ item.meta.title }}
              </a-breadcrumb-item>
            </a-breadcrumb>
          </div>
          <span v-else class="mobile-title">{{ currentRouteName }}</span>
        </div>
        
        <div class="header-right">
          <div class="header-actions">
            <!-- 全局倒计时 -->
            <reservation-timer />

            <div class="action-icons">
              <a-tooltip title="全屏" v-if="!isMobile">
                <div class="tool-item" @click="toggleFullscreen">
                  <fullscreen-outlined v-if="!isFullscreen" />
                  <fullscreen-exit-outlined v-else />
                </div>
              </a-tooltip>
              
              <a-tooltip title="刷新">
                <div class="tool-item" @click="handleRefresh">
                  <reload-outlined :spin="isRefreshing" />
                </div>
              </a-tooltip>

              <notification-bell />
            </div>
          </div>

          <a-dropdown>
            <div class="user-container">
              <a-avatar size="small" :src="userInfo?.avatar" style="background-color: var(--color-primary)">
                <template #icon><user-outlined /></template>
              </a-avatar>
              <span class="username" v-if="!isMobile">
                {{ userInfo?.username || '用户' }}
                <a-tag color="blue" style="margin-left: 5px; font-size: 10px;">{{ userInfo?.role }}</a-tag>
              </span>
            </div>
            <template #overlay>
              <a-menu class="user-dropdown-menu">
                <template v-if="userInfo?.role !== 'guest'">
                  <a-menu-item key="profile" @click="router.push('/profile/info')">
                    <user-outlined /> 个人中心
                  </a-menu-item>
                  <a-menu-item v-if="!userStore.isNewUser" key="history" @click="router.push('/profile/history')">
                    <history-outlined /> 预约/签到记录
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout" @click="handleLogout">
                    <logout-outlined /> 退出登录
                  </a-menu-item>
                </template>
                <template v-else>
                  <a-menu-item key="logout" @click="handleLogout">
                    <logout-outlined /> 退出访客模式
                  </a-menu-item>
                </template>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="custom-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="componentKey" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  UserOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
  ReloadOutlined,
  LogoutOutlined,
  HistoryOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '../stores/user'
import { message, notification } from 'ant-design-vue'
import SubMenu from './components/Menu/SubMenu.vue'
import NotificationBell from './components/Notification/NotificationBell.vue'
import ReservationTimer from '../components/Global/ReservationTimer.vue'
import { wsService } from '../utils/websocket'

const collapsed = ref(false)
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const isMobile = ref(false)

const userInfo = computed(() => userStore.userInfo)
const filteredMenus = computed(() => {
  const role = userInfo.value?.role || ''
  
  // 递归过滤函数
  const filterByRole = (menus: any[]): any[] => {
    return menus
      .filter(menu => {
        // 1. 检查当前菜单项的权限
        const allowedRoles = menu.meta?.roles || []
        if (allowedRoles.length > 0 && !allowedRoles.includes(role)) {
          return false
        }
        return true
      })
      .map(menu => {
        // 2. 递归处理子菜单
        if (menu.children && menu.children.length > 0) {
          return { ...menu, children: filterByRole(menu.children), _hadChildren: true }
        }
        return menu
      })
      .filter(menu => {
        // 3. 如果原本有子菜单但过滤后没有了，则隐藏父级目录
        if (menu._hadChildren && (!menu.children || menu.children.length === 0)) {
          return false
        }
        return true
      })
  }

  const menus = filterByRole(userStore.menus)

  if (userStore.isNewUser) {
    // 强制绑定信息时，只显示个人中心
    return menus.filter(m => m.path === '/profile')
  }
  return menus
})
const currentRouteName = computed(() => route.meta.title || route.name)
const breadcrumbList = computed(() => route.matched.filter(item => item.meta && item.meta.title && item.meta.title !== '首页'))

const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])

watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
  // 简单的自动展开逻辑: 假设只有一级嵌套 /parent/child
  const parts = newPath.split('/').filter(Boolean)
  if (parts.length > 1) {
    const parentPath = '/' + parts[0]
    if (!openKeys.value.includes(parentPath)) {
      openKeys.value.push(parentPath)
    }
  }
}, { immediate: true })

// Mobile detection
const checkMobile = () => {
  const wasMobile = isMobile.value
  isMobile.value = window.innerWidth <= 768
  if (isMobile.value && !wasMobile) {
    collapsed.value = true
  } else if (!isMobile.value && wasMobile) {
    collapsed.value = false
  }
}

const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}

// Navigation handling
const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}

const handleMobileMenuClick = (e: any) => {
  handleMenuClick(e)
  collapsed.value = true // Close drawer on selection
}

// Refresh Logic
const isRefreshing = ref(false)
const componentKey = ref(0)
const handleRefresh = () => {
  isRefreshing.value = true
  componentKey.value++
  setTimeout(() => {
    isRefreshing.value = false
  }, 1000)
}

// Fullscreen Logic
const isFullscreen = ref(false)
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

const handleLogout = async () => {
  try {
    await userStore.handleLogout()
    wsService.disconnect(true) // Force disconnect on logout
    message.success('已成功退出')
  } catch (error) {
    console.error('Logout error:', error)
  } finally {
    router.push('/login')
  }
}

// WebSocket Alerts
const handleAlert = (data: { message: string, type?: 'info' | 'warning' | 'error' }) => {
  notification[data.type || 'info']({
    message: '系统通知',
    description: data.message,
    placement: 'bottomRight'
  })
}

// WebSocket Reservation Updates
const handleReservationUpdate = (data: { event: string, reason: string }) => {
  if (data.event === 'reservation_ended') {
    userStore.clearReservation()
    let reasonText = '预约已结束'
    if (data.reason === 'admin_force_release') reasonText = '管理员已强制释放您的座位'
    if (data.reason === 'violation') reasonText = '因超时未签到/返回，预约已取消'
    if (data.reason === 'expired') reasonText = '预约已到期'
    
    notification.info({
      message: '预约状态更新',
      description: reasonText,
      placement: 'bottomRight'
    })
  }
}

onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  
  // 确保菜单已加载
  if (userStore.menus.length === 0) {
    await userStore.fetchMenus()
  }

  // 监听告警
  wsService.connect()
  wsService.on('alert', handleAlert)
  wsService.on('reservation_update', handleReservationUpdate)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  wsService.off('alert', handleAlert)
  wsService.off('reservation_update', handleReservationUpdate)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* Sider Styles */
.custom-sider {
  background: var(--g-main-sidebar-bg);
  box-shadow: 2px 0 8px 0 rgba(29, 35, 41, 0.05);
  z-index: 10;
  border-right: 1px solid var(--g-sidebar-border-color);
}

.logo-container {
  height: var(--g-header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: var(--g-sidebar-logo-bg);
  border-bottom: 1px solid var(--g-sidebar-border-color);
  overflow: hidden;
  transition: all 0.3s;
}

.logo-box {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-box img {
  width: 100%;
  height: 100%;
}

.logo-text {
  margin: 0 0 0 12px;
  color: var(--color-primary);
  font-weight: 600;
  font-size: 18px;
  white-space: nowrap;
  opacity: 1;
  transition: opacity 0.3s;
}

.collapsed .logo-text {
  opacity: 0;
  width: 0;
  margin: 0;
}

/* Menu Customization */
.menu-container {
  padding: 16px 8px;
}

.custom-menu {
  border-right: none;
  background: transparent;
}

:deep(.ant-menu-item), :deep(.ant-menu-submenu-title) {
  border-radius: 4px;
  margin-bottom: 8px;
  color: var(--g-main-sidebar-menu-color);
}

:deep(.ant-menu-item:hover), :deep(.ant-menu-submenu-title:hover) {
  color: var(--g-main-sidebar-menu-hover-color);
  background-color: var(--g-main-sidebar-menu-hover-bg);
}

:deep(.ant-menu-item-selected) {
  color: var(--g-main-sidebar-menu-active-color);
  background-color: var(--g-main-sidebar-menu-active-bg);
}

:deep(.ant-menu-item-selected::after) {
  border-right: 3px solid var(--color-primary);
}

/* Header Styles */
.custom-header {
  background: var(--g-header-bg);
  backdrop-filter: blur(8px);
  padding: 0;
  height: var(--g-header-height);
  line-height: var(--g-header-height);
  box-shadow: var(--g-header-shadow);
  position: sticky;
  top: 0;
  z-index: 9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  height: 100%;
  overflow: hidden;
}

.trigger-btn {
  padding: 0 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 18px;
  height: 100%;
  display: flex;
  align-items: center;
}

.trigger-btn:hover {
  background: rgba(0, 0, 0, 0.025);
  color: var(--color-primary);
}

.breadcrumb-wrapper {
  padding-left: 8px;
}

.mobile-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--g-header-color);
  margin-left: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  padding-right: 20px;
  height: 100%;
}

.header-actions {
  display: flex;
  align-items: center;
  height: 100%;
  margin-right: 8px;
}

.action-icons {
  display: flex;
  align-items: center;
  height: 100%;
}

.tool-item {
  padding: 0 12px;
  cursor: pointer;
  height: 100%;
  display: flex;
  align-items: center;
  transition: all 0.3s;
  color: rgba(0, 0, 0, 0.75);
  font-size: 16px;
}

.tool-item:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--color-primary);
}

.user-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 12px;
  height: 100%;
  transition: all 0.3s;
}

.user-container:hover {
  background: rgba(0, 0, 0, 0.025);
}

.username {
  margin-left: 8px;
  color: var(--g-header-color);
  font-weight: 500;
  font-size: 14px;
}

/* Content Styles */
.custom-content {
  margin: 16px;
  padding: 0;
  background: transparent;
  overflow-y: auto;
  overflow-x: hidden;
}

/* Mobile Adjustments */
@media screen and (max-width: 768px) {
  .custom-content {
    margin: 12px 8px;
  }
  
  .trigger-btn {
    padding: 0 12px;
  }
  
  .header-right {
    padding-right: 12px;
  }
  
  .tool-item {
    padding: 0 8px;
  }
  
  .user-container {
    padding: 0 8px;
  }
}
</style>
