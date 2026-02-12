<template>
  <view class="container">
    <!-- 消息类型筛选 -->
    <view class="filter-bar">
      <scroll-view scroll-x class="filter-scroll">
        <view 
          class="filter-item" 
          :class="{ active: currentType === 0 }"
          @click="selectType(0)"
        >
          全部
        </view>
        <view 
          class="filter-item" 
          :class="{ active: currentType === 1 }"
          @click="selectType(1)"
        >
          任务通知
        </view>
        <view 
          class="filter-item" 
          :class="{ active: currentType === 3 }"
          @click="selectType(3)"
        >
          订单通知
        </view>
        <view 
          class="filter-item" 
          :class="{ active: currentType === 6 }"
          @click="selectType(6)"
        >
          评价通知
        </view>
        <view 
          class="filter-item" 
          :class="{ active: currentType === 7 }"
          @click="selectType(7)"
        >
          资金通知
        </view>
      </scroll-view>
    </view>

    <!-- 一键已读 -->
    <view class="mark-all-read" v-if="unreadCount > 0">
      <text class="unread-text">{{ unreadCount }}条未读</text>
      <text class="mark-btn" @click="markAllRead">一键已读</text>
    </view>

    <!-- 消息列表 -->
    <scroll-view 
      scroll-y 
      class="message-list"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view 
        class="message-item" 
        v-for="(item, index) in messageList" 
        :key="index"
        :class="{ unread: item.isRead === 0 }"
        @click="goDetail(item)"
      >
        <view class="message-icon" :class="'type-' + item.type">
          <text class="iconfont">{{ getIcon(item.type) }}</text>
        </view>
        <view class="message-content">
          <view class="message-header">
            <text class="title">{{ item.title }}</text>
            <text class="time">{{ formatTime(item.createTime) }}</text>
          </view>
          <text class="desc">{{ item.content }}</text>
        </view>
        <view class="unread-dot" v-if="item.isRead === 0"></view>
      </view>
      
      <!-- 加载状态 -->
      <view class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else-if="messageList.length === 0">暂无消息</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import messageApi from '@/api/message.js'
import socket from '@/utils/socket.js'

export default {
  data() {
    return {
      currentType: 0,
      messageList: [],
      unreadCount: 0,
      current: 1,
      size: 10,
      loading: false,
      noMore: false,
      refreshing: false
    }
  },
  onLoad() {
    this.refreshList()
    this.loadUnreadCount()
    
    // 监听WebSocket消息
    socket.on('message', this.onNewMessage)
  },
  onUnload() {
    // 移除监听
    socket.off('message', this.onNewMessage)
  },
  onShow() {
    // 每次显示页面时刷新
    this.loadUnreadCount()
  },
  methods: {
    refreshList() {
      this.current = 1
      this.noMore = false
      this.loadMessageList()
    },

    selectType(type) {
      this.currentType = type
      this.messageList = []
      this.refreshList()
    },
    
    async loadMessageList() {
      if (this.loading || this.noMore) return
      
      this.loading = true
      
      try {
        const params = {
          current: this.current,
          size: this.size
        }
        
        if (this.currentType !== 0) {
          params.type = this.currentType
        }
        
        const res = await messageApi.getList(params)
        
        if (res.code === 200) {
          const records = res.data.records || []
          
          if (this.current === 1) {
            this.messageList = records
          } else {
            this.messageList = this.messageList.concat(records)
          }
          
          if (records.length < this.size) {
            this.noMore = true
          }
        }
      } catch (e) {
        console.error('加载消息失败', e)
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    
    async loadUnreadCount() {
      try {
        const res = await messageApi.getUnreadCount()
        if (res.code === 200) {
          this.unreadCount = res.data || 0
          
          // 设置tabBar红点
          if (this.unreadCount > 0) {
            uni.setTabBarBadge({
              index: 3,
              text: this.unreadCount > 99 ? '99+' : String(this.unreadCount)
            })
          } else {
            uni.removeTabBarBadge({ index: 3 })
          }
        }
      } catch (e) {
        console.error('加载未读数失败', e)
      }
    },
    
    async markAllRead() {
      try {
        const res = await messageApi.markAllRead()
        if (res.code === 200) {
          uni.showToast({
            title: '已标记为已读',
            icon: 'success'
          })
          
          // 刷新列表
          this.messageList.forEach(item => {
            item.isRead = 1
          })
          
          // 更新未读数
          this.loadUnreadCount()
        }
      } catch (e) {
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    },
    
    async goDetail(item) {
      // 标记为已读
      if (item.isRead === 0) {
        try {
          await messageApi.markAsRead(item.id)
          item.isRead = 1
          this.loadUnreadCount()
        } catch (e) {
          console.error('标记已读失败', e)
        }
      }
      
      // 根据消息类型跳转
      if (item.relatedId) {
        switch (item.type) {
          case 1: // 任务被接单
          case 2: // 任务状态变更
          case 3: // 订单取消
          case 4: // 确认送达
          case 5: // 确认收货
          case 6: // 收到评价
            uni.navigateTo({
              url: `/pages/order/detail?id=${item.relatedId}`
            })
            break
          default:
            // 其他类型不跳转
            break
        }
      }
    },
    
    onNewMessage(data) {
      // 新消息到达，刷新列表
      this.loadUnreadCount()
      
      // 如果当前在第一页，刷新列表
      if (this.current === 1) {
        this.messageList = []
        this.refreshList()
      }
      
      // 显示提示
      uni.showToast({
        title: '收到新消息',
        icon: 'none'
      })
    },
    
    loadMore() {
      if (!this.noMore && !this.loading) {
        this.current++
        this.loadMessageList()
      }
    },
    
    onRefresh() {
      this.refreshing = true
      this.messageList = []
      this.refreshList()
      this.loadUnreadCount()
    },
    
    getIcon(type) {
      const icons = {
        1: '📋', // 任务被接单
        2: '📋', // 任务状态变更
        3: '❌', // 订单取消
        4: '🚚', // 确认送达
        5: '✅', // 确认收货
        6: '⭐', // 收到评价
        7: '💰', // 资金变动
        8: '📢'  // 系统通知
      }
      return icons[type] || '📢'
    },
    
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) {
        return Math.floor(diff / 3600000) + '小时前'
      } else if (diff < 2592000000) {
        return Math.floor(diff / 86400000) + '天前'
      } else {
        return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@use '@/static/styles/mixins.scss' as *;

.container {
  min-height: 100vh;
  background: radial-gradient(circle at 0 0, rgba(var(--color-primary-rgb), 0.09) 0%, transparent 45%), var(--color-bg);
}

.filter-bar {
  margin: var(--space-4) var(--space-5) 0;

  .filter-scroll {
    white-space: nowrap;

    .filter-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: var(--space-2) var(--space-5);
      margin-right: var(--space-3);
      border-radius: var(--radius-full);
      border: 2rpx solid var(--color-border-light);
      background: var(--color-surface);
      color: var(--color-text-secondary);
      font-size: var(--font-size-sm);
      transition: all var(--duration-fast) var(--ease-out);

      &.active {
        background: var(--color-primary-gradient);
        border-color: transparent;
        color: var(--color-text-primary);
        font-weight: var(--font-weight-semibold);
        box-shadow: var(--shadow-primary);
      }
    }
  }
}

.mark-all-read {
  @include flex-between;
  margin: var(--space-4) var(--space-5);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-xs);

  .unread-text {
    font-size: var(--font-size-sm);
    color: var(--color-error);
    font-weight: var(--font-weight-medium);
  }

  .mark-btn {
    padding: var(--space-1) var(--space-3);
    border-radius: var(--radius-full);
    font-size: var(--font-size-xs);
    color: var(--color-task-other);
    background: var(--color-task-other-soft);
  }
}

.message-list {
  height: calc(100vh - 170rpx);
  padding: 0 var(--space-5) var(--space-6);

  .message-item {
    position: relative;
    display: flex;
    align-items: flex-start;
    padding: var(--space-5);
    margin-bottom: var(--space-4);
    border-radius: var(--radius-xl);
    background: var(--color-surface);
    box-shadow: var(--shadow-sm);
    transition: all var(--duration-fast) var(--ease-out);

    &:active {
      transform: translateY(-2rpx);
      box-shadow: var(--shadow-md);
    }

    &.unread {
      border: 2rpx solid rgba(var(--color-primary-rgb), 0.22);
      background: linear-gradient(180deg, rgba(var(--color-primary-rgb), 0.09) 0%, var(--color-surface) 55%);
    }

    .message-icon {
      @include flex-center;
      width: 84rpx;
      height: 84rpx;
      margin-right: var(--space-4);
      border-radius: var(--radius-lg);
      flex-shrink: 0;

      &.type-1,
      &.type-2 {
        background: var(--color-task-express-soft);
      }

      &.type-3 {
        background: var(--color-error-soft);
      }

      &.type-4,
      &.type-5 {
        background: var(--color-task-shopping-soft);
      }

      &.type-6 {
        background: var(--color-warning-soft);
      }

      &.type-7 {
        background: var(--color-task-other-soft);
      }

      &.type-8 {
        background: var(--color-bg-secondary);
      }

      .iconfont {
        font-size: var(--font-size-xl);
      }
    }

    .message-content {
      flex: 1;
      min-width: 0;

      .message-header {
        @include flex-between;
        margin-bottom: var(--space-2);

        .title {
          max-width: 360rpx;
          font-size: var(--font-size-base);
          font-weight: var(--font-weight-semibold);
          color: var(--color-text-primary);
          @include text-ellipsis;
        }

        .time {
          margin-left: var(--space-3);
          font-size: var(--font-size-xs);
          color: var(--color-text-tertiary);
        }
      }

      .desc {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        line-height: var(--line-height-relaxed);
        @include text-ellipsis-multi(2);
      }
    }

    .unread-dot {
      position: absolute;
      top: var(--space-4);
      right: var(--space-4);
      width: 14rpx;
      height: 14rpx;
      border-radius: var(--radius-full);
      background: var(--color-error);
      box-shadow: 0 0 0 4rpx rgba(var(--color-error-rgb), 0.2);
    }
  }

  .load-more {
    padding: var(--space-6) 0;
    text-align: center;
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
  }
}
</style>

