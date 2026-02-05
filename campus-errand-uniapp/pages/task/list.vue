<template>
  <view class="container">
    <!-- 筛选栏 -->
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
      </scroll-view>
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
              <image :src="task.publisherAvatar || '/static/avatar/default.png'" class="avatar"></image>
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
        url: '/pages/task/publish'
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
  background: #f5f5f5;
}

.filter-bar {
  background: #fff;
  padding: 20rpx;
  
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

.task-scroll {
  flex: 1;
  padding: 20rpx;
}

.task-list {
  .task-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    
    .task-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;
      
      .task-type {
        padding: 6rpx 16rpx;
        border-radius: 8rpx;
        font-size: 24rpx;
        
        &.type-1 {
          background: #e6f7ff;
          color: #1890ff;
        }
        
        &.type-2 {
          background: #f6ffed;
          color: #52c41a;
        }
        
        &.type-3 {
          background: #fff7e6;
          color: #fa8c16;
        }
        
        &.type-4 {
          background: #f9f0ff;
          color: #722ed1;
        }
      }
      
      .task-price {
        font-size: 36rpx;
        font-weight: bold;
        color: #ff4d4f;
      }
    }
    
    .task-title {
      font-size: 30rpx;
      color: #333;
      margin-bottom: 16rpx;
      font-weight: 500;
    }
    
    .task-address {
      margin-bottom: 16rpx;
      
      .address-item {
        display: flex;
        align-items: center;
        margin-bottom: 8rpx;
        
        .dot {
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
          margin-right: 12rpx;
          
          &.pickup {
            background: #52c41a;
          }
          
          &.delivery {
            background: #ff4d4f;
          }
        }
        
        .address-text {
          font-size: 26rpx;
          color: #666;
        }
      }
    }
    
    .task-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 16rpx;
      border-top: 1rpx solid #f0f0f0;
      
      .publisher {
        display: flex;
        align-items: center;
        
        .avatar {
          width: 48rpx;
          height: 48rpx;
          border-radius: 50%;
          margin-right: 12rpx;
        }
        
        .name {
          font-size: 26rpx;
          color: #333;
          margin-right: 12rpx;
        }
        
        .credit {
          font-size: 22rpx;
          color: #faad14;
          background: #fffbe6;
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
        }
      }
      
      .time {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.load-more {
  text-align: center;
  padding: 30rpx;
  font-size: 26rpx;
  color: #999;
}

.publish-btn {
  position: fixed;
  right: 30rpx;
  bottom: 150rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.4);
  
  .iconfont {
    font-size: 40rpx;
    margin-bottom: 4rpx;
  }
  
  text:last-child {
    font-size: 22rpx;
  }
}
</style>
