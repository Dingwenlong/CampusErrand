<template>
  <view class="container">
    <!-- 评分概览 -->
    <view class="rating-overview">
      <view class="rating-left">
        <text class="average-score">{{ averageRating }}</text>
        <view class="star-display">
          <text 
            class="star" 
            v-for="(item, index) in 5" 
            :key="index"
            :class="{ active: index < Math.round(averageRating) }"
          >★</text>
        </view>
        <text class="rating-count">{{ totalCount }}条评价</text>
      </view>
      <view class="rating-right">
        <view class="rating-bar" v-for="(item, index) in ratingStats" :key="index">
          <text class="star-label">{{ 5 - index }}星</text>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: item.percent + '%' }"></view>
          </view>
          <text class="count">{{ item.count }}</text>
        </view>
      </view>
    </view>

    <!-- 标签切换 -->
    <view class="tab-section">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'received' }"
        @click="switchTab('received')"
      >
        收到的评价
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'sent' }"
        @click="switchTab('sent')"
      >
        发出的评价
      </view>
    </view>

    <!-- 评价列表 -->
    <scroll-view 
      scroll-y 
      class="evaluation-list"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="evaluation-item" v-for="(item, index) in evaluationList" :key="index">
        <view class="user-info">
          <image :src="item.fromUserAvatar || '/static/avatar/default.png'" class="avatar"></image>
          <view class="user-detail">
            <text class="username">{{ item.fromUserName }}</text>
            <text class="user-type">{{ item.userTypeName }}</text>
          </view>
          <text class="time">{{ formatTime(item.createTime) }}</text>
        </view>
        
        <view class="rating-info">
          <view class="stars">
            <text 
              class="star" 
              v-for="(star, starIndex) in 5" 
              :key="starIndex"
              :class="{ active: starIndex < item.rating }"
            >★</text>
          </view>
        </view>
        
        <view class="content">{{ item.content }}</view>
        
        <view class="tags" v-if="item.tags && item.tags.length > 0">
          <text class="tag" v-for="(tag, tagIndex) in item.tags" :key="tagIndex">{{ tag }}</text>
        </view>
        
        <view class="task-info" v-if="item.taskTitle">
          <text class="task-label">任务：</text>
          <text class="task-title">{{ item.taskTitle }}</text>
        </view>
      </view>
      
      <!-- 加载状态 -->
      <view class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else-if="evaluationList.length === 0">暂无评价</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import evaluationApi from '@/api/evaluation.js'

export default {
  data() {
    return {
      currentTab: 'received',
      evaluationList: [],
      averageRating: 5.0,
      totalCount: 0,
      ratingStats: [
        { count: 0, percent: 0 },
        { count: 0, percent: 0 },
        { count: 0, percent: 0 },
        { count: 0, percent: 0 },
        { count: 0, percent: 0 }
      ],
      current: 1,
      size: 10,
      loading: false,
      noMore: false,
      refreshing: false
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    switchTab(tab) {
      if (this.currentTab === tab) return
      this.currentTab = tab
      this.current = 1
      this.evaluationList = []
      this.noMore = false
      this.loadData()
    },
    
    async loadData() {
      if (this.loading || this.noMore) return
      
      this.loading = true
      
      try {
        // 加载评价列表
        const listRes = await (this.currentTab === 'received' 
          ? evaluationApi.getReceived({ current: this.current, size: this.size })
          : evaluationApi.getSent({ current: this.current, size: this.size }))
        
        if (listRes.code === 200) {
          const records = listRes.data.records || []
          
          if (this.current === 1) {
            this.evaluationList = records
          } else {
            this.evaluationList = this.evaluationList.concat(records)
          }
          
          if (records.length < this.size) {
            this.noMore = true
          }
        }
        
        // 加载平均评分（只在第一页时加载）
        if (this.current === 1 && this.currentTab === 'received') {
          const avgRes = await evaluationApi.getAverageRating()
          if (avgRes.code === 200) {
            this.averageRating = avgRes.data || 5.0
          }
        }
        
        // 计算评分统计
        this.calculateRatingStats()
      } catch (e) {
        console.error('加载评价失败', e)
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    
    calculateRatingStats() {
      const stats = [0, 0, 0, 0, 0] // 1-5星
      this.evaluationList.forEach(item => {
        if (item.rating >= 1 && item.rating <= 5) {
          stats[item.rating - 1]++
        }
      })
      
      this.totalCount = this.evaluationList.length
      
      this.ratingStats = stats.reverse().map((count, index) => ({
        count,
        percent: this.totalCount > 0 ? (count / this.totalCount * 100) : 0
      }))
    },
    
    loadMore() {
      if (!this.noMore && !this.loading) {
        this.current++
        this.loadData()
      }
    },
    
    onRefresh() {
      this.refreshing = true
      this.current = 1
      this.noMore = false
      this.loadData()
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

.rating-overview {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
  display: flex;
  align-items: center;
  color: #fff;
  
  .rating-left {
    flex: 1;
    text-align: center;
    border-right: 1rpx solid rgba(255, 255, 255, 0.3);
    padding-right: 30rpx;
    
    .average-score {
      font-size: 72rpx;
      font-weight: bold;
      display: block;
    }
    
    .star-display {
      margin: 16rpx 0;
      
      .star {
        font-size: 32rpx;
        color: rgba(255, 255, 255, 0.5);
        margin: 0 4rpx;
        
        &.active {
          color: #FFC300;
        }
      }
    }
    
    .rating-count {
      font-size: 26rpx;
      opacity: 0.9;
    }
  }
  
  .rating-right {
    flex: 1.5;
    padding-left: 30rpx;
    
    .rating-bar {
      display: flex;
      align-items: center;
      margin-bottom: 12rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .star-label {
        font-size: 24rpx;
        width: 50rpx;
        opacity: 0.9;
      }
      
      .progress-bar {
        flex: 1;
        height: 12rpx;
        background: rgba(255, 255, 255, 0.3);
        border-radius: 6rpx;
        margin: 0 16rpx;
        overflow: hidden;
        
        .progress-fill {
          height: 100%;
          background: #FFC300;
          border-radius: 6rpx;
          transition: width 0.3s;
        }
      }
      
      .count {
        font-size: 24rpx;
        width: 50rpx;
        text-align: right;
        opacity: 0.9;
      }
    }
  }
}

.tab-section {
  background: #fff;
  display: flex;
  border-bottom: 1rpx solid #f0f0f0;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 30rpx 0;
    font-size: 30rpx;
    color: #666;
    position: relative;
    
    &.active {
      color: #667eea;
      font-weight: 500;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background: #667eea;
        border-radius: 2rpx;
      }
    }
  }
}

.evaluation-list {
  height: calc(100vh - 300rpx - 100rpx);
  padding: 20rpx;
  
  .evaluation-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    
    .user-info {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;
      
      .avatar {
        width: 72rpx;
        height: 72rpx;
        border-radius: 50%;
        margin-right: 20rpx;
      }
      
      .user-detail {
        flex: 1;
        
        .username {
          display: block;
          font-size: 30rpx;
          color: #333;
          font-weight: 500;
          margin-bottom: 8rpx;
        }
        
        .user-type {
          font-size: 24rpx;
          color: #667eea;
          background: #f0f0ff;
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
        }
      }
      
      .time {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .rating-info {
      margin-bottom: 16rpx;
      
      .stars {
        .star {
          font-size: 28rpx;
          color: #ddd;
          margin-right: 8rpx;
          
          &.active {
            color: #FFC300;
          }
        }
      }
    }
    
    .content {
      font-size: 28rpx;
      color: #333;
      line-height: 1.6;
      margin-bottom: 16rpx;
    }
    
    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;
      margin-bottom: 16rpx;
      
      .tag {
        font-size: 24rpx;
        color: #667eea;
        background: #f0f0ff;
        padding: 8rpx 16rpx;
        border-radius: 8rpx;
      }
    }
    
    .task-info {
      padding-top: 16rpx;
      border-top: 1rpx solid #f0f0f0;
      
      .task-label {
        font-size: 26rpx;
        color: #999;
      }
      
      .task-title {
        font-size: 26rpx;
        color: #666;
      }
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
