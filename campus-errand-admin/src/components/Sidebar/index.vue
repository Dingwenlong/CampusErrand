<template>
  <a-layout-sider v-model:collapsed="collapsed" collapsible>
    <div class="logo">
      <span v-if="!collapsed">校园跑腿后台</span>
      <span v-else>跑腿</span>
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      theme="dark"
      mode="inline"
      @click="handleMenuClick"
    >
      <a-menu-item key="/dashboard">
        <DashboardOutlined />
        <span>数据仪表盘</span>
      </a-menu-item>
      <a-menu-item key="/user">
        <UserOutlined />
        <span>用户管理</span>
      </a-menu-item>
      <a-menu-item key="/task">
        <FileTextOutlined />
        <span>任务管理</span>
      </a-menu-item>
      <a-menu-item key="/wallet">
        <WalletOutlined />
        <span>钱包管理</span>
      </a-menu-item>
      <a-menu-item key="/evaluation">
        <StarOutlined />
        <span>评价管理</span>
      </a-menu-item>
      <a-menu-item key="/setting">
        <SettingOutlined />
        <span>系统设置</span>
      </a-menu-item>
    </a-menu>
  </a-layout-sider>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DashboardOutlined,
  UserOutlined,
  FileTextOutlined,
  WalletOutlined,
  StarOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const selectedKeys = ref<string[]>([route.path])

watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
})

const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
</style>
