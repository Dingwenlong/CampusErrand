<template>
  <div class="notification-bell">
    <a-dropdown :trigger="['click']" placement="bottomRight">
      <div class="tool-item">
        <a-badge :count="unreadCount" size="small">
          <bell-outlined style="font-size: 18px" />
        </a-badge>
      </div>
      <template #overlay>
        <a-card class="notification-dropdown" title="系统通知" :bordered="false">
          <template #extra>
            <a-button type="link" size="small" @click="handleReadAll" v-if="unreadCount > 0">全部已读</a-button>
          </template>
          
          <a-list :data-source="notifications" :loading="loading" size="small">
            <template #renderItem="{ item }">
              <a-list-item class="notification-item" :class="{ unread: !item.isRead }" @click="handleRead(item)">
                <a-list-item-meta :title="item.title" :description="item.content">
                  <template #avatar>
                    <a-avatar :style="{ backgroundColor: getTypeColor(item.type) }">
                      <template #icon>
                        <info-circle-outlined v-if="item.type === 'info'" />
                        <check-circle-outlined v-else-if="item.type === 'success'" />
                        <warning-outlined v-else-if="item.type === 'warning'" />
                        <close-circle-outlined v-else />
                      </template>
                    </a-avatar>
                  </template>
                </a-list-item-meta>
                <div class="time">{{ formatTime(item.createTime) }}</div>
              </a-list-item>
            </template>
            <template #footer v-if="notifications.length > 0">
              <div style="text-align: center">
                <a-button type="link" size="small" @click="router.push('/system/notifications')">查看全部</a-button>
              </div>
            </template>
          </a-list>
          <div v-if="notifications.length === 0" class="empty-notifications">
            <a-empty description="暂无通知" />
          </div>
        </a-card>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  BellOutlined, 
  InfoCircleOutlined, 
  CheckCircleOutlined, 
  WarningOutlined, 
  CloseCircleOutlined 
 } from '@ant-design/icons-vue'
import { message as antMessage } from 'ant-design-vue'
import request from '../../../utils/request'
import { wsService } from '../../../utils/websocket'
import { eventBus } from '../../../utils/eventBus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const loading = ref(false)
const unreadCount = ref(0)
const notifications = ref<any[]>([])
let timer: any = null

const fetchUnreadCount = async () => {
  try {
    const res = await request<any>({ url: '/notifications/unread-count', method: 'get' })
    unreadCount.value = res.data || res
  } catch (e) {}
}

const fetchLatestNotifications = async () => {
  loading.value = true
  try {
    const res = await request<any>({ 
      url: '/notifications/list', 
      method: 'get',
      params: { page: 1, size: 5 }
    })
    const data = res.data || res
    notifications.value = data.records
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handleRead = async (item: any) => {
  if (item.isRead) return
  try {
    await request({ url: `/notifications/${item.id}/read`, method: 'put' })
    item.isRead = 1
    fetchUnreadCount()
  } catch (e) {}
}

const handleReadAll = async () => {
  try {
    await request({ url: '/notifications/read-all', method: 'put' })
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
  } catch (e) {}
}

const getTypeColor = (type: string) => {
  switch (type) {
    case 'success': return '#52c41a'
    case 'warning': return '#faad14'
    case 'error': return '#f5222d'
    default: return '#1890ff'
  }
}

const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// WebSocket 实时更新
const handleNewNotification = (notification: any) => {
  // 1. 立即更新未读数
  unreadCount.value++
  
  // 2. 立即更新列表 (推送到最前面，并保持最多5条)
  const newNotification = {
    ...notification,
    isRead: 0 // 确保标记为未读
  }
  notifications.value = [newNotification, ...notifications.value].slice(0, 5)

  // 3. 弹出提示
  antMessage.info({
    content: `新通知: ${notification.title}`,
    duration: 5
  })
  
  // 4. 延迟刷新一次，确保与后端数据完全同步 (防止并发导致的数据不一致)
  setTimeout(() => {
    fetchUnreadCount()
    fetchLatestNotifications()
  }, 1000)
}

// 处理手动刷新通知（从消息广场发送系统通知后）
const handleRefreshNotifications = () => {
  // 延迟一点时间，确保后端数据已更新
  setTimeout(() => {
    fetchUnreadCount()
    fetchLatestNotifications()
    antMessage.success('收到新通知')
  }, 500)
}

onMounted(() => {
  fetchUnreadCount()
  fetchLatestNotifications()
  wsService.on('new_notification', handleNewNotification)
  eventBus.on('refresh_notifications', handleRefreshNotifications)
  // 每30秒轮询一次
  timer = setInterval(() => {
    fetchUnreadCount()
    if (notifications.value.length < 5) fetchLatestNotifications()
  }, 30000)
})

onUnmounted(() => {
  wsService.off('new_notification', handleNewNotification)
  eventBus.off('refresh_notifications', handleRefreshNotifications)
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.notification-bell {
  display: flex;
  align-items: center;
  height: 100%;
}
.tool-item {
  padding: 0 12px;
  cursor: pointer;
  height: 100%;
  display: flex;
  align-items: center;
  transition: all 0.3s;
  color: rgba(0, 0, 0, 0.75);
}
.tool-item:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--color-primary);
}
.tool-item:hover :deep(.anticon-bell) {
  animation: bell-swing 1s ease-in-out infinite;
  transform-origin: top center;
}

@keyframes bell-swing {
  0% { transform: rotate(0); }
  20% { transform: rotate(15deg); }
  40% { transform: rotate(-10deg); }
  60% { transform: rotate(5deg); }
  80% { transform: rotate(-5deg); }
  100% { transform: rotate(0); }
}

.notification-dropdown {
  width: 350px;
  max-height: 500px;
  overflow-y: auto;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05);
}
.notification-item {
  cursor: pointer;
  transition: background 0.3s;
  padding: 12px 16px !important;
}
.notification-item:hover {
  background: #f5f5f5;
}
.notification-item.unread {
  background: #e6f7ff;
}
.notification-item.unread:hover {
  background: #bae7ff;
}
.time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.empty-notifications {
  padding: 32px 0;
}
:deep(.ant-list-item-meta-title) {
  margin-bottom: 4px;
}
:deep(.ant-list-item-meta-description) {
  font-size: 13px;
  line-height: 1.5;
}
</style>
