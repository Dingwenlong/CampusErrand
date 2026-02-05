<template>
  <a-layout-header class="header">
    <div class="header-right">
      <a-dropdown>
        <a class="user-info" @click.prevent>
          <UserOutlined />
          <span class="username">管理员</span>
          <DownOutlined />
        </a>
        <template #overlay>
          <a-menu @click="handleMenuClick">
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
import { UserOutlined, DownOutlined, LogoutOutlined } from '@ant-design/icons-vue'
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
  justify-content: flex-end;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(0, 0, 0, 0.85);
}

.username {
  margin: 0 4px;
}
</style>
