<template>
  <!-- 桌面端侧边栏 -->
  <a-layout-sider 
    v-if="!mobile" 
    :collapsed="collapsed"
    @update:collapsed="$emit('update:collapsed', $event)"
    collapsible 
    class="sidebar"
    :width="200"
  >
    <div class="logo">
      <div class="logo-icon">🏃</div>
      <div v-if="!collapsed" class="logo-text-group">
        <span class="logo-text">校园跑腿</span>
        <span class="logo-sub">Management</span>
      </div>
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      theme="dark"
      mode="inline"
      @click="handleMenuClick"
      class="custom-menu"
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
      <a-menu-item key="/banner">
        <PictureOutlined />
        <span>轮播图管理</span>
      </a-menu-item>
      <a-menu-item key="/setting">
        <SettingOutlined />
        <span>系统设置</span>
      </a-menu-item>
    </a-menu>
  </a-layout-sider>

  <!-- 移动端侧边栏 -->
  <div v-else class="mobile-sidebar">
    <div class="logo">
      <div class="logo-icon">🏃</div>
      <div class="logo-text-group">
        <span class="logo-text">校园跑腿</span>
        <span class="logo-sub">Management</span>
      </div>
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      theme="dark"
      mode="inline"
      @click="handleMobileMenuClick"
      class="custom-menu mobile-menu"
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
      <a-menu-item key="/banner">
        <PictureOutlined />
        <span>轮播图管理</span>
      </a-menu-item>
      <a-menu-item key="/setting">
        <SettingOutlined />
        <span>系统设置</span>
      </a-menu-item>
    </a-menu>
  </div>
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
  PictureOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'

const props = defineProps<{
  mobile?: boolean
  collapsed?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:collapsed', value: boolean): void
  (e: 'close'): void
}>()

const route = useRoute()
const router = useRouter()
const selectedKeys = ref<string[]>([route.path])

watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
})

const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}

const handleMobileMenuClick = ({ key }: { key: string }) => {
  router.push(key)
  emit('close')
}
</script>

<style scoped>
.sidebar {
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%) !important;
  box-shadow: 4px 0 20px -14px rgba(15, 23, 42, 0.75);
}

.mobile-sidebar {
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  height: 100%;
}

.logo {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(15, 23, 42, 0.24);
}

.logo-icon {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 10px 20px -12px rgba(245, 158, 11, 0.75);
}

.logo-text-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #f8fafc;
}

.logo-sub {
  font-size: 10px;
  color: rgba(203, 213, 225, 0.85);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.custom-menu {
  background: transparent;
  border-right: none;
  color: #e2e8f0;
}

.mobile-menu {
  padding-top: 8px;
}

.custom-menu :deep(.ant-menu-item) {
  margin: 5px 10px;
  border-radius: 10px;
  color: #cbd5e1;
  font-weight: 600;
}

.custom-menu :deep(.ant-menu-item:hover) {
  color: #fef3c7;
  background: rgba(245, 158, 11, 0.22);
}

.custom-menu :deep(.ant-menu-item-selected) {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%) !important;
  color: #1f2937 !important;
  font-weight: 600;
  box-shadow: 0 10px 16px -12px rgba(245, 158, 11, 0.9);
}

.custom-menu :deep(.ant-menu-item-selected .anticon) {
  color: #1f2937;
}

.custom-menu :deep(.ant-menu-item .anticon) {
  font-size: 16px;
}
</style>
