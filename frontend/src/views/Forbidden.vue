<template>
  <div class="forbidden-container">
    <div class="content">
      <h1>403</h1>
      <p>抱歉，您没有权限访问此页面。</p>
      <a-space>
        <a-button type="primary" @click="handleGoHome">返回首页</a-button>
        <a-button @click="handleReLogin">切换账号</a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleGoHome = () => {
  router.push('/')
}

const handleReLogin = async () => {
  await userStore.handleLogout()
  router.push('/login')
}
</script>

<style scoped>
.forbidden-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f2f5;
}
.content {
  text-align: center;
}
h1 {
  font-size: 72px;
  color: #ff4d4f;
  margin-bottom: 24px;
}
p {
  margin-bottom: 32px;
  color: #666;
  font-size: 16px;
}
</style>
