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
    this.loadMessageList()
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
    selectType(type) {
      this.currentType = type
      this.current = 1
      this.messageList = []
      this.noMore = false
      this.loadMessageList()
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
        this.loadMessageList()
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
      this.current = 1
      this.noMore = false
      this.loadMessageList()
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
.container {
  background: #f5f5f5;
  min-height: 100vh;
}

.filter-bar {
  background: #fff;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .filter-scroll {
    white-space: nowrap;
    
    .filter-item {
      display: inline-block;
      padding: 12rpx 32rpx;
      margin-right: 16rpx;
      font-size: 28rpx;
      color: #666;
      background: #f5f5f5;
      border-radius: 32rpx;
      
      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

.mark-all-read {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #f0f0f0;
  
  .unread-text {
    font-size: 26rpx;
    color: #ff4d4f;
  }
  
  .mark-btn {
    font-size: 26rpx;
    color: #667eea;
    padding: 8rpx 20rpx;
    background: #f0f0ff;
    border-radius: 8rpx;
  }
}

.message-list {
  height: calc(100vh - 120rpx);
  padding: 20rpx;
  
  .message-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 16rpx;
    display: flex;
    align-items: flex-start;
    position: relative;
    
    &.unread {
      background: #f8f8ff;
    }
    
    .message-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
      flex-shrink: 0;
      
      &.type-1, &.type-2 {
        background: #e6f7ff;
      }
      
      &.type-3 {
        background: #fff2f0;
      }
      
      &.type-4, &.type-5 {
        background: #f6ffed;
      }
      
      &.type-6 {
        background: #fffbe6;
      }
      
      &.type-7 {
        background: #f9f0ff;
      }
      
      &.type-8 {
        background: #f5f5f5;
      }
      
      .iconfont {
        font-size: 40rpx;
      }
    }
    
    .message-content {
      flex: 1;
      min-width: 0;
      
      .message-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12rpx;
        
        .title {
          font-size: 30rpx;
          color: #333;
          font-weight: 500;
        }
        
        .time {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .desc {
        font-size: 26rpx;
        color: #666;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }
    
    .unread-dot {
      width: 16rpx;
      height: 16rpx;
      background: #ff4d4f;
      border-radius: 50%;
      position: absolute;
      top: 24rpx;
      right: 24rpx;
    }
  }
  
  .load-more {
    text-align: center;
    padding: 30rpx;
    font-size: 26rpx;
    color: #999;
  }
}
</style>
