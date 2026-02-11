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
      <span v-if="!collapsed" class="logo-text">校园跑腿</span>
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      theme="light"
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
      <span class="logo-text">校园跑腿</span>
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      theme="light"
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
import { ref, watch, defineProps, defineEmits } from 'vue'
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
  background: #fff !important;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.mobile-sidebar {
  background: #fff;
  height: 100%;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #FFC300 0%, #FFB300 100%);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.custom-menu {
  background: #fff;
  border-right: none;
}

.mobile-menu {
  padding-top: 8px;
}

.custom-menu :deep(.ant-menu-item) {
  margin: 4px 12px;
  border-radius: 8px;
  color: #666;
}

.custom-menu :deep(.ant-menu-item:hover) {
  color: #FFC300;
  background: rgba(255, 195, 0, 0.1);
}

.custom-menu :deep(.ant-menu-item-selected) {
  background: linear-gradient(135deg, #FFC300 0%, #FFB300 100%) !important;
  color: #333 !important;
  font-weight: 600;
}

.custom-menu :deep(.ant-menu-item-selected .anticon) {
  color: #333;
}
</style>
