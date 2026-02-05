<template>
  <div class="square-container">
    <a-card :bordered="false" class="publish-card">
      <div class="publish-header">
        <a-avatar :src="userInfo?.avatar">
          <template #icon><user-outlined /></template>
        </a-avatar>
        <span class="publish-title">在消息广场发言</span>
        <a-button 
          v-if="userInfo?.role === 'admin'" 
          type="link" 
          class="system-notice-btn"
          @click="showNotificationModal = true"
        >
          <template #icon><notification-outlined /></template>
          发送系统通知
        </a-button>
      </div>
      <a-textarea
        v-model:value="messageContent"
        :placeholder="messageSquareEnabled ? '分享你的想法，或者@某位同学...' : '消息广场发言功能已关闭'"
        :rows="4"
        :maxlength="200"
        show-count
        class="message-input"
        :disabled="!messageSquareEnabled"
      />
      <div class="publish-footer">
        <a-select
          v-model:value="atUserId"
          placeholder="@提到某人"
          style="width: 200px"
          allow-clear
          show-search
          :filter-option="false"
          @search="handleUserSearch"
          @focus="handleUserSearch('')"
          :loading="userSearchLoading"
          :disabled="!messageSquareEnabled"
        >
          <a-select-option v-for="user in userOptions" :key="user.id" :value="user.id">
            {{ user.realName || user.username }} (@{{ user.username }})
          </a-select-option>
        </a-select>
        <a-button type="primary" :loading="publishing" @click="handlePublish" :disabled="!messageContent.trim() || !messageSquareEnabled">
          发布消息
        </a-button>
      </div>
    </a-card>

    <div class="message-list">
      <a-list :data-source="messages" :loading="loading">
        <template #renderItem="{ item }">
          <a-card class="message-item" :bordered="false">
            <div class="message-header">
              <a-avatar :src="item.avatar">
                <template #icon><user-outlined /></template>
              </a-avatar>
              <div class="user-info">
                <div class="user-main">
                  <span class="real-name">{{ item.username }}{{ item.realName ? `(${item.realName})` : '' }}</span>
                  <a-tag :color="getRoleColor(item.role)" size="small">{{ item.role }}</a-tag>
                  <a-badge status="processing" :text="item.status === 'active' ? '在线' : '离线'" />
                </div>
                <div class="user-sub">
                  <span class="time">{{ formatTime(item.createTime) }}</span>
                  <span v-if="item.seatNo" class="seat-info">
                    <environment-outlined /> 正在使用 <b>{{ item.seatNo }}</b> 座位
                  </span>
                </div>
              </div>
            </div>
            <div class="message-content">
              <span v-if="item.atUserId" class="at-text">@某位用户 </span>
              {{ item.content }}
            </div>
          </a-card>
        </template>
      </a-list>
      <div v-if="hasMore" class="load-more">
        <a-button @click="loadMore" :loading="loading">加载更多</a-button>
      </div>
    </div>

    <!-- 系统通知弹窗 -->
    <a-modal
      v-model:visible="showNotificationModal"
      title="发送系统通知"
      @ok="handleSendNotification"
      :confirm-loading="sendingNotification"
      destroy-on-close
    >
      <a-form layout="vertical">
        <a-form-item label="通知标题" required>
          <a-input v-model:value="notificationForm.title" placeholder="请输入通知标题" />
        </a-form-item>
        <a-form-item label="通知内容" required>
          <a-textarea v-model:value="notificationForm.content" :rows="4" placeholder="请输入通知内容" />
        </a-form-item>
        <a-form-item label="通知类型">
          <a-radio-group v-model:value="notificationForm.type">
            <a-radio value="info">普通</a-radio>
            <a-radio value="warning">警告</a-radio>
            <a-radio value="error">紧急</a-radio>
            <a-radio value="success">成功</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { 
  UserOutlined, 
  EnvironmentOutlined, 
  NotificationOutlined 
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'
import { getConfigs } from '../../api/config'
import { wsService } from '../../utils/websocket'
import { eventBus } from '../../utils/eventBus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// 消息广场功能开关
const messageSquareEnabled = ref(true)
const fetchConfig = async () => {
  try {
    const res = await getConfigs()
    const data = (res as any).data || res
    const config = data.find((c: any) => c.configKey === 'message_square_enabled')
    if (config) {
      messageSquareEnabled.value = config.configValue === 'true'
    }
  } catch (e) {
    console.error('获取配置失败', e)
  }
}

// 系统通知相关
const showNotificationModal = ref(false)
const sendingNotification = ref(false)
const notificationForm = reactive({
  title: '',
  content: '',
  type: 'info'
})

const handleSendNotification = async () => {
  if (!notificationForm.title.trim() || !notificationForm.content.trim()) {
    message.warning('请填写完整的标题和内容')
    return
  }
  sendingNotification.value = true
  try {
    await request({
      url: '/messages/system-notification',
      method: 'post',
      data: notificationForm
    })
    message.success('系统通知发送成功')
    showNotificationModal.value = false
    notificationForm.title = ''
    notificationForm.content = ''
    notificationForm.type = 'info'
    // 触发通知刷新事件，让通知铃铛组件立即刷新
    eventBus.emit('refresh_notifications')
  } catch (e) {
  } finally {
    sendingNotification.value = false
  }
}

const loading = ref(false)
const publishing = ref(false)
const messageContent = ref('')
const atUserId = ref<number | null>(null)
const messages = ref<any[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})
const hasMore = computed(() => messages.value.length < pagination.total)

// 用户搜索相关
const userSearchLoading = ref(false)
const userOptions = ref<any[]>([])
const handleUserSearch = async (val: string) => {
  userSearchLoading.value = true
  try {
    const res = await request<any>({
      url: '/users/list',
      method: 'get',
      params: { 
        page: 1, 
        size: 50, 
        realName: val || undefined,
        username: val || undefined 
      }
    })
    const data = res.data || res
    userOptions.value = data.records
  } catch (e) {
  } finally {
    userSearchLoading.value = false
  }
}

const fetchMessages = async (append = false) => {
  loading.value = true
  try {
    const res = await request<any>({
      url: '/messages/list',
      method: 'get',
      params: { page: pagination.current, size: pagination.pageSize }
    })
    const data = res.data || res
    if (append) {
      messages.value = [...messages.value, ...data.records]
    } else {
      messages.value = data.records
    }
    pagination.total = data.total
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handlePublish = async () => {
  if (!messageContent.value.trim()) return
  publishing.value = true
  try {
    await request({
      url: '/messages',
      method: 'post',
      data: {
        content: messageContent.value,
        atUserId: atUserId.value
      }
    })
    message.success('发布成功')
    messageContent.value = ''
    atUserId.value = null
    pagination.current = 1
    fetchMessages()
  } catch (e) {
  } finally {
    publishing.value = false
  }
}

const loadMore = () => {
  pagination.current++
  fetchMessages(true)
}

const getRoleColor = (role: string) => {
  switch (role) {
    case 'admin': return 'red'
    case 'librarian': return 'orange'
    default: return 'blue'
  }
}

const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// WebSocket 实时更新逻辑
const handleNewMessage = (msg: any) => {
  // 如果当前在第一页，则直接推送到列表顶部
  if (pagination.current === 1) {
    // 避免重复推送自己刚发的（handlePublish已经调用了fetchMessages）
    const exists = messages.value.some(m => m.id === msg.id)
    if (!exists) {
      messages.value = [msg, ...messages.value]
      pagination.total++
    }
  }
}

const handleStatusChange = (data: { username: string, status: string }) => {
  messages.value.forEach(m => {
    if (m.username === data.username) {
      m.status = data.status
    }
  })
}

const handleUserSeatChange = (data: { username: string, seatNo: string | null }) => {
  messages.value.forEach(m => {
    if (m.username === data.username) {
      m.seatNo = data.seatNo
    }
  })
}

onMounted(() => {
  fetchConfig()
  fetchMessages()
  wsService.on('new_message', handleNewMessage)
  wsService.on('online_status', handleStatusChange)
  wsService.on('user_seat_status', handleUserSeatChange)
})

onUnmounted(() => {
  wsService.off('new_message', handleNewMessage)
  wsService.off('online_status', handleStatusChange)
  wsService.off('user_seat_status', handleUserSeatChange)
})
</script>

<style scoped>
.square-container {
  max-width: 800px;
  margin: 0 auto;
}
.publish-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.publish-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.publish-title {
  margin-left: 12px;
  font-weight: 600;
  font-size: 16px;
}
.system-notice-btn {
  margin-left: auto;
  color: #ff4d4f;
  display: flex;
  align-items: center;
}
.system-notice-btn:hover {
  color: #ff7875;
}
.message-input {
  border-radius: 8px;
  background: #f9f9f9;
  border: 1px solid #eee;
  transition: all 0.3s;
}
.message-input:focus {
  background: #fff;
}
.publish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}
.message-list {
  /* gap对List内部无效，由message-item的margin控制 */
}
.message-item {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: transform 0.3s;
  padding: 8px 0;
  margin-bottom: 12px;
}
.message-item :deep(.ant-card-body) {
  padding: 16px 24px;
}
.message-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.message-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 12px;
  margin-bottom: 12px;
}
.user-info {
  margin-left: 12px;
  flex: 1;
}
.user-main {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}
.real-name {
  font-weight: 600;
  font-size: 15px;
  color: #1f1f1f;
}
.user-sub {
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  align-items: center;
  gap: 12px;
}
.seat-info {
  color: var(--color-primary);
  background: rgba(24, 144, 255, 0.1);
  padding: 0 8px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.message-content {
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
  color: #262626;
  padding-left: 54px; /* Align with text, not avatar */
}
.at-text {
  color: var(--color-primary);
  font-weight: 500;
}
.load-more {
  text-align: center;
  margin: 24px 0;
}
</style>
