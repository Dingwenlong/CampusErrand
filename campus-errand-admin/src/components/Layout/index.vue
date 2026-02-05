<template>
  <a-layout class="layout">
    <Sidebar />
    <a-layout>
      <Header />
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import Sidebar from '@/components/Sidebar/index.vue'
import Header from '@/components/Header/index.vue'
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
}
</style>
