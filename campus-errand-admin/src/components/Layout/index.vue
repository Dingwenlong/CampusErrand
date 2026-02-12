<template>
  <a-layout class="layout">
    <!-- 桌面端侧边栏 -->
    <Sidebar v-if="!isMobile" :collapsed="collapsed" @update:collapsed="collapsed = $event" />
    
    <!-- 移动端抽屉侧边栏 -->
    <a-drawer
      v-model:open="mobileMenuOpen"
      placement="left"
      :closable="false"
      :width="200"
      class="mobile-drawer"
    >
      <Sidebar mobile @close="mobileMenuOpen = false" />
    </a-drawer>
    
    <a-layout>
      <Header :is-mobile="isMobile" @toggle-menu="mobileMenuOpen = !mobileMenuOpen" />
      <a-layout-content class="content-shell" :class="{ 'mobile': isMobile }">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from '@/components/Sidebar/index.vue'
import Header from '@/components/Header/index.vue'
import { useResponsive } from '@/composables/useResponsive'

const { isMobile } = useResponsive()
const collapsed = ref(false)
const mobileMenuOpen = ref(false)
</script>

<style scoped>
.layout {
  min-height: 100vh;
  position: relative;
}

.content-shell {
  margin: 18px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(15, 23, 42, 0.1);
  min-height: 280px;
  overflow-y: auto;
  border-radius: 18px;
  box-shadow: 0 20px 40px -28px rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  animation: shell-in 0.32s ease;
}

.content-shell.mobile {
  margin: 10px;
  padding: 14px;
}

/* 移动端抽屉样式 */
:deep(.mobile-drawer .ant-drawer-body) {
  padding: 0;
}

:deep(.mobile-drawer .ant-drawer-content) {
  background: transparent;
  backdrop-filter: blur(8px);
}

@keyframes shell-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
