<template>
  <a-layout-header class="header">
    <div class="header-left">
      <span class="page-title">后台管理系统</span>
    </div>
    <div class="header-right">
      <a-dropdown>
        <a class="user-info" @click.prevent>
          <div class="avatar">
            <span class="avatar-text">管</span>
          </div>
          <span class="username">管理员</span>
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
import { DownOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/store/user'

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
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(0, 0, 0, 0.85);
  padding: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.user-info:hover {
  background: rgba(255, 195, 0, 0.1);
}

.avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #FFC300 0%, #FFB300 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  color: #333;
  font-size: 14px;
  font-weight: 600;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.dropdown-menu :deep(.ant-dropdown-menu-item:hover) {
  color: #FFC300;
  background: rgba(255, 195, 0, 0.1);
}
</style>
