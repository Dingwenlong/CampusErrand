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
@use '@/static/styles/mixins.scss' as *;

.container {
  min-height: 100vh;
  background: radial-gradient(circle at 100% 0, rgba(var(--color-primary-rgb), 0.1) 0%, transparent 45%), var(--color-bg);
}

.rating-overview {
  display: flex;
  align-items: center;
  margin: var(--space-4) var(--space-5) 0;
  padding: var(--space-6);
  color: var(--color-white);
  border-radius: var(--radius-2xl);
  background: linear-gradient(135deg, var(--color-brand-blue) 0%, var(--color-brand-indigo) 100%);
  box-shadow: var(--shadow-md);

  .rating-left {
    flex: 1;
    padding-right: var(--space-5);
    text-align: center;
    border-right: 2rpx solid rgba(255, 255, 255, 0.25);

    .average-score {
      display: block;
      font-size: var(--font-size-4xl);
      font-weight: var(--font-weight-bold);
      line-height: var(--line-height-none);
    }

    .star-display {
      margin: var(--space-2) 0;

      .star {
        margin: 0 4rpx;
        font-size: var(--font-size-base);
        color: rgba(255, 255, 255, 0.42);

        &.active {
          color: var(--color-primary-light);
        }
      }
    }

    .rating-count {
      font-size: var(--font-size-xs);
      opacity: 0.9;
    }
  }

  .rating-right {
    flex: 1.5;
    padding-left: var(--space-5);

    .rating-bar {
      display: flex;
      align-items: center;
      margin-bottom: var(--space-2);

      &:last-child {
        margin-bottom: 0;
      }

      .star-label {
        width: 56rpx;
        font-size: var(--font-size-xs);
      }

      .progress-bar {
        flex: 1;
        height: 12rpx;
        margin: 0 var(--space-3);
        border-radius: var(--radius-full);
        background: rgba(255, 255, 255, 0.3);
        overflow: hidden;

        .progress-fill {
          height: 100%;
          border-radius: var(--radius-full);
          background: linear-gradient(90deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
          transition: width var(--duration-fast) var(--ease-out);
        }
      }

      .count {
        width: 48rpx;
        text-align: right;
        font-size: var(--font-size-xs);
      }
    }
  }
}

.tab-section {
  display: flex;
  margin: var(--space-4) var(--space-5);
  padding: var(--space-1);
  border-radius: var(--radius-full);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);

  .tab-item {
    flex: 1;
    text-align: center;
    padding: var(--space-3) 0;
    border-radius: var(--radius-full);
    font-size: var(--font-size-base);
    color: var(--color-text-secondary);
    transition: all var(--duration-fast) var(--ease-out);

    &.active {
      color: var(--color-text-primary);
      font-weight: var(--font-weight-semibold);
      background: var(--color-primary-gradient);
      box-shadow: var(--shadow-primary);
    }
  }
}

.evaluation-list {
  height: calc(100vh - 360rpx);
  padding: 0 var(--space-5) var(--space-6);

  .evaluation-item {
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

    .user-info {
      display: flex;
      align-items: center;
      margin-bottom: var(--space-4);

      .avatar {
        width: 72rpx;
        height: 72rpx;
        margin-right: var(--space-4);
        border-radius: var(--radius-full);
        border: 2rpx solid var(--color-border-light);
      }

      .user-detail {
        flex: 1;

        .username {
          display: block;
          margin-bottom: var(--space-1);
          font-size: var(--font-size-base);
          font-weight: var(--font-weight-semibold);
          color: var(--color-text-primary);
        }

        .user-type {
          display: inline-block;
          padding: 4rpx 12rpx;
          border-radius: var(--radius-sm);
          font-size: var(--font-size-xs);
          color: var(--color-task-other);
          background: var(--color-task-other-soft);
        }
      }

      .time {
        font-size: var(--font-size-xs);
        color: var(--color-text-tertiary);
      }
    }

    .rating-info {
      margin-bottom: var(--space-3);

      .stars {
        .star {
          margin-right: var(--space-1);
          font-size: var(--font-size-sm);
          color: var(--color-border-dark);

          &.active {
            color: var(--color-primary);
          }
        }
      }
    }

    .content {
      margin-bottom: var(--space-3);
      font-size: var(--font-size-sm);
      line-height: var(--line-height-relaxed);
      color: var(--color-text-primary);
    }

    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: var(--space-2);
      margin-bottom: var(--space-3);

      .tag {
        padding: var(--space-1) var(--space-3);
        border-radius: var(--radius-sm);
        font-size: var(--font-size-xs);
        color: var(--color-info);
        background: var(--color-info-soft);
      }
    }

    .task-info {
      padding-top: var(--space-3);
      border-top: 2rpx solid var(--color-divider);

      .task-label {
        margin-right: var(--space-1);
        font-size: var(--font-size-xs);
        color: var(--color-text-tertiary);
      }

      .task-title {
        font-size: var(--font-size-xs);
        color: var(--color-text-secondary);
      }
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

