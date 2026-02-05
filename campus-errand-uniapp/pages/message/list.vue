<template>
  <view class="container">
    <!-- 消息列表 -->
    <scroll-view scroll-y class="message-list" @scrolltolower="loadMore">
      <view v-if="messages.length === 0" class="empty-state">
        <text class="iconfont icon-empty"></text>
        <text class="empty-text">暂无消息</text>
      </view>

      <view v-else class="message-item" v-for="(item, index) in messages" :key="index" @click="readMessage(item)">
        <view class="message-icon" :class="'type-' + item.type">
          <text class="iconfont" :class="getIconClass(item.type)"></text>
        </view>
        <view class="message-content">
          <view class="message-header">
            <text class="message-title">{{ item.title }}</text>
            <text v-if="item.isRead === 0" class="unread-dot"></text>
          </view>
          <text class="message-text">{{ item.content }}</text>
          <text class="message-time">{{ item.createTime }}</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="messages.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else>上拉加载更多</text>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="messages.length > 0">
      <view class="action-btn" @click="markAllAsRead">
        <text class="iconfont icon-read"></text>
        <text>全部已读</text>
      </view>
    </view>
  </view>
</template>

<script>
import messageApi from '@/api/message.js'
import socket from '@/utils/socket.js'

export default {
  data() {
    return {
      messages: [],
      current: 1,
      size: 10,
      loading: false,
      noMore: false
    }
  },
  onLoad() {
    this.loadMessages()
    this.initSocket()
  },
  onUnload() {
    socket.off('message', this.onNewMessage)
  },
  methods: {
    initSocket() {
      // 连接WebSocket
      socket.connect()

      // 监听新消息
      this.onNewMessage = (data) => {
        console.log('收到新消息:', data)
        // 新消息插入到列表顶部
        this.messages.unshift(data)
        // 更新未读数
        this.updateUnreadCount()
      }
      socket.on('message', this.onNewMessage)
    },
    async loadMessages() {
      if (this.loading || this.noMore) return

      this.loading = true
      try {
        const res = await messageApi.getList({
          current: this.current,
          size: this.size
        })

        if (res.code === 200) {
          const data = res.data
          const list = data.records || []

          if (list.length < this.size) {
            this.noMore = true
          }

          this.messages = this.current === 1 ? list : [...this.messages, ...list]
        }
      } catch (e) {
        console.error('加载消息失败', e)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      if (this.noMore || this.loading) return
      this.current++
      this.loadMessages()
    },
    async readMessage(item) {
      // 如果已读，直接返回
      if (item.isRead === 1) return

      try {
        const res = await messageApi.markAsRead(item.id)
        if (res.code === 200) {
          // 更新本地状态
          item.isRead = 1
          // 更新未读数
          this.updateUnreadCount()
        }
      } catch (e) {
        console.error('标记已读失败', e)
      }
    },
    async markAllAsRead() {
      try {
        const res = await messageApi.markAllAsRead()
        if (res.code === 200) {
          // 更新所有消息为已读
          this.messages.forEach(item => {
            item.isRead = 1
          })
          // 更新未读数
          this.updateUnreadCount()
          uni.showToast({
            title: `已标记${res.data}条消息为已读`,
            icon: 'none'
          })
        }
      } catch (e) {
        console.error('标记全部已读失败', e)
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    },
    updateUnreadCount() {
      // 计算未读数
      const unreadCount = this.messages.filter(item => item.isRead === 0).length
      // 更新TabBar角标
      if (unreadCount > 0) {
        uni.setTabBarBadge({
          index: 3, // 消息Tab的索引
          text: unreadCount.toString()
        })
      } else {
        uni.removeTabBarBadge({
          index: 3
        })
      }
    },
    getIconClass(type) {
      const iconMap = {
        1: 'icon-task', // 任务被接单
        2: 'icon-status', // 任务状态变更
        3: 'icon-cancel', // 订单取消
        4: 'icon-deliver', // 确认送达
        5: 'icon-complete', // 确认收货
        6: 'icon-evaluation', // 收到评价
        7: 'icon-wallet', // 资金变动
        8: 'icon-system' // 系统通知
      }
      return iconMap[type] || 'icon-message'
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 100rpx;
}

.message-list {
  height: calc(100vh - 100rpx);
  padding: 20rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 0;

  .iconfont {
    font-size: 120rpx;
    color: #ddd;
    margin-bottom: 20rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.message-item {
  display: flex;
  align-items: flex-start;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .message-icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    flex-shrink: 0;

    &.type-1 { background: rgba(102, 126, 234, 0.1); .iconfont { color: #667eea; } }
    &.type-2 { background: rgba(250, 140, 22, 0.1); .iconfont { color: #fa8c16; } }
    &.type-3 { background: rgba(255, 77, 79, 0.1); .iconfont { color: #ff4d4f; } }
    &.type-4 { background: rgba(82, 196, 26, 0.1); .iconfont { color: #52c41a; } }
    &.type-5 { background: rgba(24, 144, 255, 0.1); .iconfont { color: #1890ff; } }
    &.type-6 { background: rgba(250, 173, 20, 0.1); .iconfont { color: #faad14; } }
    &.type-7 { background: rgba(114, 46, 209, 0.1); .iconfont { color: #722ed1; } }
    &.type-8 { background: rgba(140, 140, 140, 0.1); .iconfont { color: #8c8c8c; } }

    .iconfont {
      font-size: 40rpx;
    }
  }

  .message-content {
    flex: 1;
    display: flex;
    flex-direction: column;

    .message-header {
      display: flex;
      align-items: center;
      margin-bottom: 10rpx;

      .message-title {
        font-size: 30rpx;
        color: #333;
        font-weight: bold;
        margin-right: 12rpx;
      }

      .unread-dot {
        width: 16rpx;
        height: 16rpx;
        background: #ff4d4f;
        border-radius: 50%;
      }
    }

    .message-text {
      font-size: 28rpx;
      color: #666;
      margin-bottom: 10rpx;
      line-height: 1.5;
    }

    .message-time {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.load-more {
  text-align: center;
  padding: 30rpx;
  font-size: 26rpx;
  color: #999;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1rpx solid #eee;

  .action-btn {
    display: flex;
    align-items: center;
    padding: 16rpx 40rpx;
    background: #f5f5f5;
    border-radius: 30rpx;

    .iconfont {
      font-size: 32rpx;
      color: #667eea;
      margin-right: 8rpx;
    }

    text {
      font-size: 28rpx;
      color: #666;
    }
  }
}
</style>
