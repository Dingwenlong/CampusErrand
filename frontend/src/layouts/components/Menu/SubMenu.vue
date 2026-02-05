<template>
  <template v-for="item in menuInfo" :key="item.id">
    <template v-if="!item.children || item.children.length === 0">
      <a-menu-item :key="item.path">
        <template #icon v-if="item.meta?.icon">
          <component :is="iconMap[item.meta.icon]" />
        </template>
        <span>{{ item.meta?.title }}</span>
      </a-menu-item>
    </template>
    <template v-else>
      <a-sub-menu :key="item.path">
        <template #icon v-if="item.meta?.icon">
          <component :is="iconMap[item.meta.icon]" />
        </template>
        <template #title>{{ item.meta?.title }}</template>
        <sub-menu :menu-info="item.children" />
      </a-sub-menu>
    </template>
  </template>
</template>

<script setup lang="ts">
import {
  DashboardOutlined,
  DesktopOutlined,
  UserOutlined,
  BarChartOutlined,
  SettingOutlined,
  FileTextOutlined,
  EnvironmentOutlined,
  CommentOutlined,
  ProjectOutlined
} from '@ant-design/icons-vue'
import type { MenuItem } from '../../../api/menu'

defineProps<{
  menuInfo: MenuItem[]
}>()

const iconMap: Record<string, any> = {
  DashboardOutlined,
  DesktopOutlined,
  UserOutlined,
  BarChartOutlined,
  SettingOutlined,
  FileTextOutlined,
  EnvironmentOutlined,
  CommentOutlined,
  ProjectOutlined
}
</script>

<script lang="ts">
export default {
  name: 'SubMenu'
}
</script>
