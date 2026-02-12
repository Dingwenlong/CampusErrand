<template>
  <a-layout-header class="header">
    <div class="header-left">
      <!-- 移动端菜单按钮 -->
      <a-button
        v-if="isMobile"
        type="text"
        class="menu-btn"
        @click="$emit('toggle-menu')"
      >
        <MenuOutlined />
      </a-button>
      <a-button
        v-else
        type="text"
        class="menu-btn"
        @click="$emit('toggle-collapse')"
      >
        <MenuUnfoldOutlined v-if="collapsed" />
        <MenuFoldOutlined v-else />
      </a-button>
      <div class="title-group">
        <span class="page-title">后台管理系统</span>
        <span v-if="!isMobile" class="page-subtitle">Campus Errand Operations</span>
      </div>
    </div>
    <div class="header-right">
      <a-dropdown>
        <a class="user-info" @click.prevent>
          <span class="status-dot"></span>
          <div class="avatar">
            <span class="avatar-text">管</span>
          </div>
          <span v-if="!isMobile" class="username">管理员</span>
          <DownOutlined />
        </a>
        <template #overlay>
          <a-menu @click="handleMenuClick" class="dropdown-menu">
            <a-menu-item key="logout">
              <LogoutOutlined />
              <span>退出登录</span>
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { DownOutlined, LogoutOutlined, MenuOutlined, MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/store/user'

defineProps<{
  isMobile?: boolean
  collapsed?: boolean
}>()

defineEmits<{
  (e: 'toggle-menu'): void
  (e: 'toggle-collapse'): void
}>()

const router = useRouter()
const userStore = useUserStore()

const handleMenuClick = ({ key }: { key: string }) => {
  if (key === 'logout') {
    userStore.clearToken()
    message.success('退出成功')
    router.push('/login')
  }
}
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.7);
  padding: 0 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 24px !important;
}

.menu-btn {
  font-size: 18px;
  color: #334155;
  padding: 0 8px;
}

.menu-btn:hover {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.14);
}

.page-title {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.page-subtitle {
  font-size: 11px;
  letter-spacing: 0.08em;
  color: #64748b;
  text-transform: uppercase;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1e293b;
  padding: 6px 12px;
  border-radius: 12px;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.user-info:hover {
  background: rgba(245, 158, 11, 0.08);
  border-color: rgba(245, 158, 11, 0.22);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #10b981;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.18);
}

.avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  color: #1f2937;
  font-size: 14px;
  font-weight: 700;
}

.username {
  font-size: 14px;
  font-weight: 600;
}

.dropdown-menu :deep(.ant-dropdown-menu-item:hover) {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.12);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }
  
  .page-title {
    font-size: 16px;
  }
  
  .user-info {
    padding: 4px 8px;
  }
}
</style>
