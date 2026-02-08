<template>
  <view class="container">
    <!-- 筛选栏 -->
    <view class="filter-bar">
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
        取快递
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentType === 2 }"
        @click="selectType(2)"
      >
        代买
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentType === 3 }"
        @click="selectType(3)"
      >
        送件
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentType === 4 }"
        @click="selectType(4)"
      >
        其他
      </view>
    </view>

    <!-- 任务列表 -->
    <scroll-view 
      scroll-y 
      class="task-scroll"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="task-list">
        <view 
          class="task-card" 
          v-for="(task, index) in taskList" 
          :key="task.id"
          @click="goDetail(task)"
        >
          <view class="task-header">
            <view class="task-type" :class="'type-' + task.taskType">
              {{ task.taskTypeName }}
            </view>
            <text class="task-price">¥{{ task.totalAmount }}</text>
          </view>
          
          <view class="task-title">{{ task.title }}</view>
          
          <view class="task-address">
            <view class="address-item">
              <text class="dot pickup"></text>
              <text class="address-text">{{ task.pickupAddress }}</text>
            </view>
            <view class="address-item">
              <text class="dot delivery"></text>
              <text class="address-text">{{ task.deliveryAddress }}</text>
            </view>
          </view>
          
          <view class="task-footer">
            <view class="publisher">
              <view class="avatar" :class="'avatar-type-' + task.type">
                <text class="avatar-text">{{ task.publisherName ? task.publisherName.charAt(0) : '?' }}</text>
              </view>
              <text class="name">{{ task.publisherName }}</text>
              <text class="credit">信用{{ task.publisherCreditScore }}</text>
            </view>
            <text class="time">{{ formatTime(task.createTime) }}</text>
          </view>
        </view>
      </view>
      
      <!-- 加载状态 -->
      <view class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else-if="taskList.length === 0">暂无任务</text>
      </view>
    </scroll-view>

    <!-- 发布按钮 -->
    <view class="publish-btn" @click="goPublish">
      <text class="iconfont icon-plus"></text>
      <text>发布</text>
    </view>
  </view>
</template>

<script>
import taskApi from '@/api/task.js'

export default {
  data() {
    return {
      currentType: 0,
      taskList: [],
      current: 1,
      size: 10,
      loading: false,
      noMore: false,
      refreshing: false
    }
  },
  onLoad() {
    this.loadTaskList()
  },
  methods: {
    selectType(type) {
      this.currentType = type
      this.current = 1
      this.taskList = []
      this.noMore = false
      this.loadTaskList()
    },
    
    async loadTaskList() {
      if (this.loading || this.noMore) return
      
      this.loading = true
      
      try {
        const params = {
          current: this.current,
          size: this.size
        }
        
        if (this.currentType !== 0) {
          params.taskType = this.currentType
        }
        
        const res = await taskApi.getList(params)
        
        if (res.code === 200) {
          const records = res.data.records || []
          
          if (this.current === 1) {
            this.taskList = records
          } else {
            this.taskList = this.taskList.concat(records)
          }
          
          if (records.length < this.size) {
            this.noMore = true
          }
        }
      } catch (e) {
        console.error('加载任务列表失败', e)
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    
    loadMore() {
      if (!this.noMore && !this.loading) {
        this.current++
        this.loadTaskList()
      }
    },
    
    onRefresh() {
      this.refreshing = true
      this.current = 1
      this.noMore = false
      this.loadTaskList()
    },
    
    goDetail(task) {
      uni.navigateTo({
        url: `/pages/task/detail?id=${task.id}`
      })
    },
    
    goPublish() {
      uni.navigateTo({
        url: '/package-task/pages/task/publish'
      })
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
      } else {
        return Math.floor(diff / 86400000) + '天前'
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F5F5F5;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  background: #F5F5F5;
  padding: var(--space-4) var(--space-6);
  margin-bottom: var(--space-4);

  .filter-item {
    flex: 1;
    text-align: center;
    font-size: var(--font-size-base);
    color: var(--color-text-secondary);
    padding: var(--space-3) 0;
    margin: 0 var(--space-2);
    border-radius: var(--radius-lg);
    transition: all var(--duration-fast) var(--ease-out);
    background: #fff;
    box-shadow: var(--shadow-sm);

    &:active {
      background: var(--color-bg-secondary);
    }

    &.active {
      color: #333;
      font-weight: var(--font-weight-semibold);
      background: #FFC300;
    }
  }
}

.task-scroll {
  flex: 1;
  padding: 0 var(--space-6);
}

.task-list {
  .task-card {
    background: #fff;
    border-radius: var(--radius-lg);
    padding: var(--space-5);
    margin-bottom: var(--space-4);
    box-shadow: var(--shadow-sm);
    transition: all 0.3s ease;
    
    &:active {
      box-shadow: var(--shadow-md);
      transform: translateY(-2rpx);
    }
    
    .task-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--space-4);
      
      .task-type {
        padding: 6rpx 16rpx;
        border-radius: var(--radius-sm);
        font-size: var(--font-size-xs);
        font-weight: var(--font-weight-medium);
        
        &.type-1 {
          background: rgba(255, 107, 107, 0.1);
          color: #FF6B6B;
        }
        
        &.type-2 {
          background: rgba(78, 205, 196, 0.1);
          color: #4ECDC4;
        }
        
        &.type-3 {
          background: rgba(102, 126, 234, 0.1);
          color: #667eea;
        }
        
        &.type-4 {
          background: rgba(255, 160, 122, 0.1);
          color: #FFA07A;
        }
      }
      
      .task-price {
        font-size: 40rpx;
        font-weight: var(--font-weight-bold);
        color: #FF4D4F;
      }
    }
    
    .task-title {
      font-size: var(--font-size-base);
      font-weight: var(--font-weight-medium);
      color: var(--color-text-primary);
      margin-bottom: var(--space-4);
      line-height: 1.5;
    }
    
    .task-address {
      margin-bottom: var(--space-4);
      
      .address-item {
        display: flex;
        align-items: center;
        margin-bottom: var(--space-2);
        
        .dot {
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
          margin-right: var(--space-3);
          
          &.pickup {
            background: #4ECDC4;
          }
          
          &.delivery {
            background: #FF6B6B;
          }
        }
        
        .address-text {
          font-size: var(--font-size-sm);
          color: var(--color-text-secondary);
          line-height: 1.4;
        }
      }
    }
    
    .task-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: var(--space-4);
      border-top: 2rpx solid var(--color-divider);
      
      .publisher {
        display: flex;
        align-items: center;
        
        .avatar {
          width: 48rpx;
          height: 48rpx;
          border-radius: 50%;
          margin-right: var(--space-3);
          display: flex;
          align-items: center;
          justify-content: center;

          &.avatar-type-1 {
            background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
          }

          &.avatar-type-2 {
            background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
          }

          &.avatar-type-3 {
            background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);
          }

          &.avatar-type-4 {
            background: linear-gradient(135deg, #FFA07A 0%, #FFB347 100%);
          }

          .avatar-text {
            color: #fff;
            font-size: 24rpx;
            font-weight: var(--font-weight-bold);
          }
        }
        
        .name {
          font-size: var(--font-size-sm);
          color: var(--color-text-primary);
          margin-right: var(--space-3);
        }
        
        .credit {
          font-size: var(--font-size-xs);
          color: #FFC300;
          background: rgba(255, 195, 0, 0.1);
          padding: 2rpx var(--space-3);
          border-radius: var(--radius-sm);
        }
      }
      
      .time {
        font-size: var(--font-size-xs);
        color: var(--color-text-tertiary);
      }
    }
  }
}

.load-more {
  text-align: center;
  padding: var(--space-8) 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}

.publish-btn {
  position: fixed;
  right: var(--space-6);
  bottom: 150rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #FFC300 0%, #FFB300 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #333;
  box-shadow: 0 4rpx 16rpx rgba(255, 195, 0, 0.3);
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
  }
  
  .iconfont {
    font-size: 40rpx;
    margin-bottom: 4rpx;
  }
  
  text:last-child {
    font-size: 22rpx;
    font-weight: var(--font-weight-medium);
  }
}
</style>
