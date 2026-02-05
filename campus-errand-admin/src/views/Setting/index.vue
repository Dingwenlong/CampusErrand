<template>
  <div class="setting">
    <a-card title="系统设置">
      <a-result title="系统设置功能开发中" status="info">
        <template #icon>
          <SettingOutlined />
        </template>
      </a-result>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { SettingOutlined } from '@ant-design/icons-vue'
</script>
