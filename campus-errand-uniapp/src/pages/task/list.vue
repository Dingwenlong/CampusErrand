<template>
  <view class="container">
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view 
        class="filter-item pressable" 
        :class="{ active: currentType === 0 }"
        @click="selectType(0)"
      >
        全部
      </view>
      <view 
        class="filter-item pressable" 
        :class="{ active: currentType === 1 }"
        @click="selectType(1)"
      >
        取快递
      </view>
      <view 
        class="filter-item pressable" 
        :class="{ active: currentType === 2 }"
        @click="selectType(2)"
      >
        代买
      </view>
      <view 
        class="filter-item pressable" 
        :class="{ active: currentType === 3 }"
        @click="selectType(3)"
      >
        送件
      </view>
      <view 
        class="filter-item pressable" 
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
      :enhanced="true"
      :bounces="true"
    >
      <!-- 骨架屏 -->
      <skeleton v-if="loading && taskList.length === 0" type="task-list" :rows="3" />
      
      <!-- 任务列表 -->
      <view class="task-list" v-else>
        <view 
          class="task-card card-hover" 
          v-for="(task, index) in taskList" 
          :key="task.id"
          @click="goDetail(task)"
          :style="{ animationDelay: index * 0.05 + 's' }"
        >
          <view class="task-header">
            <view class="task-type" :class="'type-' + task.taskType">
              {{ task.taskTypeName }}
            </view>
            <view class="task-price">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ task.totalAmount }}</text>
            </view>
          </view>
          
          <view class="task-title">{{ task.title }}</view>
          
          <view class="task-address">
            <view class="address-item">
              <view class="dot pickup"></view>
              <text class="address-text">{{ task.pickupAddress }}</text>
            </view>
            <view class="address-item">
              <view class="dot delivery"></view>
              <text class="address-text">{{ task.deliveryAddress }}</text>
            </view>
          </view>
          
          <view class="task-footer">
            <view class="publisher">
              <view class="avatar" :class="'avatar-type-' + task.taskType">
                <text class="avatar-text">{{ task.publisherName ? task.publisherName.charAt(0) : '?' }}</text>
              </view>
              <text class="name">{{ task.publisherName }}</text>
              <view class="credit-badge">
                <text class="credit-icon">⭐</text>
                <text class="credit-text">{{ task.publisherCreditScore }}</text>
              </view>
            </view>
            <text class="time">{{ formatTime(task.createTime) }}</text>
          </view>
        </view>
      </view>
      
      <!-- 加载状态 -->
      <view class="load-more">
        <view v-if="loading && taskList.length > 0" class="loading-state">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        <view v-else-if="noMore && taskList.length > 0" class="no-more">
          <view class="divider-line"></view>
          <text>没有更多了</text>
          <view class="divider-line"></view>
        </view>
        <view v-else-if="taskList.length === 0 && !loading" class="empty-state">
          <view class="empty-icon">📭</view>
          <text class="empty-title">暂无任务</text>
          <text class="empty-desc">暂时没有符合条件的任务</text>
        </view>
      </view>
    </scroll-view>

    <!-- 发布按钮 -->
    <view class="publish-btn pressable" @click="goPublish">
      <view class="btn-icon-wrapper">
        <text class="iconfont icon-plus"></text>
      </view>
      <text class="btn-text">发布</text>
    </view>
  </view>
</template>

<script>
import taskApi from '@/api/task.js'
import Skeleton from '@/components/skeleton/index.vue'

export default {
  components: {
    Skeleton
  },
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
      if (this.currentType === type) return
      
      // 触觉反馈
      // #ifdef MP-WEIXIN
      if (uni.vibrateShort) {
        uni.vibrateShort({ type: 'light' })
      }
      // #endif
      
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
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
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
      // 触觉反馈
      // #ifdef MP-WEIXIN
      if (uni.vibrateShort) {
        uni.vibrateShort({ type: 'medium' })
      }
      // #endif
      
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
@use '@/static/styles/mixins.scss' as *;

.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg);
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  background-color: var(--color-bg);
  padding: var(--space-4) var(--space-6);
  gap: var(--space-3);

  .filter-item {
    flex: 1;
    text-align: center;
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
    padding: var(--space-3) 0;
    border-radius: var(--radius-full);
    transition: all var(--duration-fast) var(--ease-out);
    background-color: var(--color-surface);
    box-shadow: var(--shadow-xs);

    &:active {
      transform: scale(0.95);
    }

    &.active {
      color: var(--color-text-primary);
      font-weight: var(--font-weight-semibold);
      background: var(--color-primary-gradient);
      box-shadow: var(--shadow-primary);
    }
  }
}

.task-scroll {
  flex: 1;
  padding: 0 var(--space-6);
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.task-card {
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  animation: slideUp var(--duration-normal) var(--ease-out) both;
  transition: all var(--duration-fast) var(--ease-out);
  
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
      padding: var(--space-1) var(--space-3);
      border-radius: var(--radius-sm);
      font-size: var(--font-size-xs);
      font-weight: var(--font-weight-medium);
      
      &.type-1 {
        background-color: var(--color-task-express-soft);
        color: var(--color-task-express);
      }
      
      &.type-2 {
        background-color: var(--color-task-shopping-soft);
        color: var(--color-task-shopping);
      }
      
      &.type-3 {
        background-color: var(--color-task-delivery-soft);
        color: var(--color-task-delivery);
      }
      
      &.type-4, &.type-5 {
        background-color: var(--color-task-other-soft);
        color: var(--color-task-other);
      }
    }
    
    .task-price {
      display: flex;
      align-items: baseline;
      color: var(--color-error);
      
      .price-symbol {
        font-size: var(--font-size-sm);
        font-weight: var(--font-weight-bold);
      }
      
      .price-value {
        font-size: var(--font-size-xl);
        font-weight: var(--font-weight-bold);
      }
    }
  }
  
  .task-title {
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
    margin-bottom: var(--space-4);
    line-height: var(--line-height-snug);
  }
  
  .task-address {
    margin-bottom: var(--space-4);
    
    .address-item {
      display: flex;
      align-items: center;
      margin-bottom: var(--space-2);
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .dot {
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;
        margin-right: var(--space-3);
        flex-shrink: 0;
        
        &.pickup {
          background: var(--color-task-shopping);
        }
        
        &.delivery {
          background: var(--color-error);
        }
      }
      
      .address-text {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        line-height: var(--line-height-normal);
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
        width: 56rpx;
        height: 56rpx;
        border-radius: 50%;
        margin-right: var(--space-3);
        @include flex-center;

        &.avatar-type-1 {
          background: linear-gradient(135deg, var(--color-brand-coral) 0%, var(--color-brand-coral-light) 100%);
        }

        &.avatar-type-2 {
          background: linear-gradient(135deg, var(--color-brand-mint) 0%, var(--color-brand-mint-light) 100%);
        }

        &.avatar-type-3 {
          background: linear-gradient(135deg, var(--color-brand-blue) 0%, var(--color-brand-blue-dark) 100%);
        }

        &.avatar-type-4, &.avatar-type-5 {
          background: linear-gradient(135deg, var(--color-brand-coral) 0%, var(--color-brand-coral-light) 100%);
        }

        .avatar-text {
          color: var(--color-white);
          font-size: var(--font-size-sm);
          font-weight: var(--font-weight-bold);
        }
      }
      
      .name {
        font-size: var(--font-size-sm);
        color: var(--color-text-primary);
        margin-right: var(--space-3);
        font-weight: var(--font-weight-medium);
      }
      
      .credit-badge {
        display: flex;
        align-items: center;
        background-color: var(--color-primary-soft);
        padding: var(--space-1) var(--space-2);
        border-radius: var(--radius-sm);
        
        .credit-icon {
          font-size: var(--font-size-xs);
          margin-right: 4rpx;
        }
        
        .credit-text {
          font-size: var(--font-size-xs);
          color: var(--color-primary-dark);
          font-weight: var(--font-weight-semibold);
        }
      }
    }
    
    .time {
      font-size: var(--font-size-xs);
      color: var(--color-text-tertiary);
    }
  }
}

.load-more {
  padding: var(--space-8) 0;
  
  .loading-state {
    @include flex-center;
    gap: var(--space-3);
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
  }
  
  .no-more {
    @include flex-center;
    gap: var(--space-4);
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
    
    .divider-line {
      flex: 1;
      height: 2rpx;
      background-color: var(--color-divider);
      max-width: 100rpx;
    }
  }
  
  .empty-state {
    @include flex-center;
    flex-direction: column;
    padding: var(--space-16) var(--space-6);
    
    .empty-icon {
      font-size: 120rpx;
      margin-bottom: var(--space-4);
      opacity: 0.6;
    }
    
    .empty-title {
      font-size: var(--font-size-lg);
      color: var(--color-text-secondary);
      font-weight: var(--font-weight-medium);
      margin-bottom: var(--space-2);
    }
    
    .empty-desc {
      font-size: var(--font-size-sm);
      color: var(--color-text-tertiary);
    }
  }
}

.loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 4rpx solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.publish-btn {
  position: fixed;
  right: var(--space-6);
  bottom: calc(150rpx + env(safe-area-inset-bottom));
  @include flex-center;
  flex-direction: column;
  width: 120rpx;
  height: 120rpx;
  background: var(--color-primary-gradient);
  border-radius: 50%;
  color: var(--color-text-primary);
  box-shadow: var(--shadow-primary);
  transition: all var(--duration-fast) var(--ease-out);
  z-index: 100;
  
  &:active {
    transform: scale(0.95);
    box-shadow: 0 2rpx 8rpx rgba(255, 195, 0, 0.4);
  }
  
  .btn-icon-wrapper {
    @include flex-center;
    margin-bottom: 4rpx;
  }
  
  .iconfont {
    font-size: 40rpx;
  }
  
  .btn-text {
    font-size: 22rpx;
    font-weight: var(--font-weight-medium);
  }
}

.pressable {
  transition: transform var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: scale(0.95);
  }
}

.card-hover {
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: translateY(-2rpx);
    box-shadow: var(--shadow-md);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

