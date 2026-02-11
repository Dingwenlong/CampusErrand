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
      <a-layout-content class="content" :class="{ 'mobile': isMobile }">
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
}

.content {
  margin: 24px;
  padding: 24px;
  background: #fff;
  min-height: 280px;
  overflow-y: auto;
  border-radius: 8px;
}

.content.mobile {
  margin: 12px;
  padding: 16px;
}

/* 移动端抽屉样式 */
:deep(.mobile-drawer .ant-drawer-body) {
  padding: 0;
}

:deep(.mobile-drawer .ant-drawer-content) {
  background: #fff;
}
</style>
