<template>
  <view class="container" v-if="task">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text class="status-text">{{ task.statusName }}</text>
      <text class="status-desc">{{ getStatusDesc(task.status) }}</text>
    </view>

    <!-- 任务信息 -->
    <view class="info-card">
      <view class="info-header">
        <view class="task-type" :class="'type-' + task.taskType">{{ task.taskTypeName }}</view>
        <text class="task-price">¥{{ task.totalAmount }}</text>
      </view>
      <view class="task-title">{{ task.title }}</view>
      <view class="task-desc" v-if="task.description">{{ task.description }}</view>
    </view>

    <!-- 地址信息 -->
    <view class="address-card">
      <view class="address-item">
        <view class="address-icon pickup">
          <text class="iconfont icon-location"></text>
        </view>
        <view class="address-content">
          <view class="address-title">取件地址</view>
          <view class="address-detail">{{ task.pickupAddress }}</view>
          <view class="address-contact" v-if="task.pickupContact || task.pickupPhone">
            {{ task.pickupContact }} {{ task.pickupPhone }}
          </view>
        </view>
      </view>
      <view class="address-divider"></view>
      <view class="address-item">
        <view class="address-icon delivery">
          <text class="iconfont icon-location"></text>
        </view>
        <view class="address-content">
          <view class="address-title">送达地址</view>
          <view class="address-detail">{{ task.deliveryAddress }}</view>
          <view class="address-contact" v-if="task.deliveryContact || task.deliveryPhone">
            {{ task.deliveryContact }} {{ task.deliveryPhone }}
          </view>
        </view>
      </view>
    </view>

    <!-- 时间信息 -->
    <view class="time-card">
      <view class="time-item">
        <text class="time-label">发布时间</text>
        <text class="time-value">{{ task.createTime }}</text>
      </view>
      <view class="time-item" v-if="task.expectTime">
        <text class="time-label">期望送达</text>
        <text class="time-value">{{ task.expectTime }}</text>
      </view>
      <view class="time-item" v-if="task.deadlineTime">
        <text class="time-label">截止时间</text>
        <text class="time-value">{{ task.deadlineTime }}</text>
      </view>
    </view>

    <!-- 发布者信息 -->
    <view class="publisher-card">
      <view class="publisher-header">
        <text class="section-title">发布者信息</text>
      </view>
      <view class="publisher-info">
        <view class="publisher-avatar avatar-gradient">
          <text class="avatar-text">{{ task.publisherName ? task.publisherName.charAt(0) : '?' }}</text>
        </view>
        <view class="publisher-detail">
          <text class="publisher-name">{{ task.publisherName }}</text>
          <view class="publisher-stats">
            <text class="credit">信用 {{ task.publisherCreditScore }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 接单者信息 -->
    <view class="runner-card" v-if="task.runnerId">
      <view class="runner-header">
        <text class="section-title">接单者信息</text>
      </view>
      <view class="runner-info">
        <view class="runner-avatar avatar-gradient">
          <text class="avatar-text">{{ task.runnerName ? task.runnerName.charAt(0) : '?' }}</text>
        </view>
        <view class="runner-detail">
          <text class="runner-name">{{ task.runnerName }}</text>
        </view>
      </view>
    </view>

    <!-- 备注 -->
    <view class="remark-card" v-if="task.remark">
      <view class="remark-title">备注</view>
      <view class="remark-content">{{ task.remark }}</view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="price-info">
        <text class="price-label">任务金额</text>
        <text class="price-value">¥{{ task.totalAmount }}</text>
      </view>
      <view class="action-btns">
        <button 
          class="btn-primary" 
          v-if="task.status === 0 && task.userId !== currentUserId"
          @click="acceptTask"
        >
          立即抢单
        </button>
        <button 
          class="btn-primary" 
          v-if="task.status === 1 && task.runnerId === currentUserId"
          @click="updateStatus(2)"
        >
          确认取件
        </button>
        <button 
          class="btn-primary" 
          v-if="task.status === 2 && task.runnerId === currentUserId"
          @click="updateStatus(3)"
        >
          开始配送
        </button>
        <button 
          class="btn-primary" 
          v-if="task.status === 3 && task.runnerId === currentUserId"
          @click="updateStatus(4)"
        >
          确认送达
        </button>
        <button 
          class="btn-primary" 
          v-if="task.status === 4 && task.userId === currentUserId"
          @click="updateStatus(5)"
        >
          确认完成
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import taskApi from '@/api/task.js'

export default {
  data() {
    return {
      taskId: null,
      task: null,
      currentUserId: null
    }
  },
  onLoad(options) {
    this.taskId = options.id
    this.currentUserId = uni.getStorageSync('userId')
    this.loadTaskDetail()
  },
  methods: {
    async loadTaskDetail() {
      try {
        const res = await taskApi.getDetail(this.taskId)
        if (res.code === 200) {
          this.task = res.data
        }
      } catch (e) {
        console.error('加载任务详情失败', e)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },
    
    async acceptTask() {
      uni.showModal({
        title: '确认抢单',
        content: '确定要接这个任务吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await taskApi.accept(this.taskId)
              if (result.code === 200) {
                uni.showToast({
                  title: '抢单成功',
                  icon: 'success'
                })
                this.loadTaskDetail()
              }
            } catch (e) {
              uni.showToast({
                title: '抢单失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    async updateStatus(status) {
      const statusMap = {
        2: '确认取件',
        3: '开始配送',
        4: '确认送达',
        5: '确认完成'
      }
      
      uni.showModal({
        title: statusMap[status],
        content: `确定要${statusMap[status]}吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await taskApi.updateStatus(this.taskId, status)
              if (result.code === 200) {
                uni.showToast({
                  title: '操作成功',
                  icon: 'success'
                })
                this.loadTaskDetail()
              }
            } catch (e) {
              uni.showToast({
                title: '操作失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    getStatusDesc(status) {
      const descMap = {
        0: '等待接单中',
        1: '已有人接单',
        2: '等待取件',
        3: '配送进行中',
        4: '等待确认收货',
        5: '任务已完成',
        6: '任务已取消'
      }
      return descMap[status] || ''
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  padding: var(--spacing-md);
  padding-bottom: 140rpx;
  background: var(--bg-color);
  min-height: 100vh;
}

.status-bar {
  background: var(--primary-color);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-md);
  color: white;
  box-shadow: var(--shadow-md);
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20rpx);
  
  .status-text {
    display: block;
    font-size: var(--font-size-xl);
    font-weight: bold;
    margin-bottom: var(--spacing-xs);
  }
  
  .status-desc {
    font-size: var(--font-size-base);
    opacity: 0.9;
  }
}

.info-card {
  background: var(--card-bg);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20rpx);
  animation-delay: 0.1s;
  
  &:active {
    box-shadow: var(--shadow-md);
    transform: translateY(-2rpx);
  }
  
  .info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-sm);
    
    .task-type {
      padding: var(--spacing-xs) var(--spacing-sm);
      border-radius: var(--border-radius-sm);
      font-size: var(--font-size-xs);
      
      &.type-1 {
        background: rgba(24, 144, 255, 0.1);
        color: var(--info-color);
      }
      
      &.type-2 {
        background: rgba(82, 196, 26, 0.1);
        color: var(--success-color);
      }
      
      &.type-3 {
        background: rgba(250, 173, 20, 0.1);
        color: var(--warning-color);
      }
      
      &.type-4 {
        background: rgba(114, 46, 209, 0.1);
        color: var(--primary-color);
      }
    }
    
    .task-price {
      font-size: var(--font-size-xl);
      font-weight: bold;
      color: var(--error-color);
    }
  }
  
  .task-title {
    font-size: var(--font-size-lg);
    color: var(--text-primary);
    font-weight: bold;
    margin-bottom: var(--spacing-xs);
    line-height: 1.4;
  }
  
  .task-desc {
    font-size: var(--font-size-base);
    color: var(--text-secondary);
    line-height: 1.6;
  }
}

.address-card {
  background: var(--card-bg);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20rpx);
  animation-delay: 0.2s;
  
  &:active {
    box-shadow: var(--shadow-md);
    transform: translateY(-2rpx);
  }
  
  .address-item {
    display: flex;
    align-items: flex-start;
    
    .address-icon {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: var(--spacing-md);
      
      &.pickup {
        background: rgba(82, 196, 26, 0.1);
        color: var(--success-color);
      }
      
      &.delivery {
        background: rgba(255, 77, 79, 0.1);
        color: var(--error-color);
      }
      
      .iconfont {
        font-size: var(--font-size-lg);
      }
    }
    
    .address-content {
      flex: 1;
      
      .address-title {
        font-size: var(--font-size-sm);
        color: var(--text-tertiary);
        margin-bottom: var(--spacing-xs);
      }
      
      .address-detail {
        font-size: var(--font-size-base);
        color: var(--text-primary);
        font-weight: 500;
        margin-bottom: var(--spacing-xs);
        line-height: 1.3;
      }
      
      .address-contact {
        font-size: var(--font-size-sm);
        color: var(--text-secondary);
      }
    }
  }
  
  .address-divider {
    height: 2rpx;
    background: var(--border-color);
    margin: var(--spacing-md) 0;
  }
}

.time-card {
  background: var(--card-bg);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20rpx);
  animation-delay: 0.3s;
  
  &:active {
    box-shadow: var(--shadow-md);
    transform: translateY(-2rpx);
  }
  
  .time-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: var(--spacing-md);
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .time-label {
      font-size: var(--font-size-base);
      color: var(--text-secondary);
    }
    
    .time-value {
      font-size: var(--font-size-base);
      color: var(--text-primary);
      font-weight: 500;
    }
  }
}

.publisher-card,
.runner-card {
  background: var(--card-bg);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20rpx);
  animation-delay: 0.4s;
  
  &:active {
    box-shadow: var(--shadow-md);
    transform: translateY(-2rpx);
  }
  
  .publisher-header,
  .runner-header {
    margin-bottom: var(--spacing-md);
    
    .section-title {
      font-size: var(--font-size-lg);
      font-weight: bold;
      color: var(--text-primary);
    }
  }
  
  .publisher-info,
  .runner-info {
    display: flex;
    align-items: center;
    
    .publisher-avatar,
    .runner-avatar {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      margin-right: var(--spacing-lg);
      box-shadow: var(--shadow-sm);
      display: flex;
      align-items: center;
      justify-content: center;

      &.avatar-gradient {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      .avatar-text {
        color: #fff;
        font-size: 40rpx;
        font-weight: bold;
      }
    }
    
    .publisher-detail,
    .runner-detail {
      .publisher-name,
      .runner-name {
        display: block;
        font-size: var(--font-size-lg);
        color: var(--text-primary);
        font-weight: 500;
        margin-bottom: var(--spacing-xs);
      }
      
      .publisher-stats {
        .credit {
          font-size: var(--font-size-sm);
          color: var(--warning-color);
          background: rgba(250, 173, 20, 0.1);
          padding: var(--spacing-xs) var(--spacing-sm);
          border-radius: var(--border-radius-sm);
        }
      }
    }
  }
}

.remark-card {
  background: var(--card-bg);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20rpx);
  animation-delay: 0.5s;
  
  &:active {
    box-shadow: var(--shadow-md);
    transform: translateY(-2rpx);
  }
  
  .remark-title {
    font-size: var(--font-size-lg);
    font-weight: bold;
    color: var(--text-primary);
    margin-bottom: var(--spacing-sm);
  }
  
  .remark-content {
    font-size: var(--font-size-base);
    color: var(--text-secondary);
    line-height: 1.6;
  }
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--card-bg);
  padding: var(--spacing-md) var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  border-top: 2rpx solid var(--border-color);
  
  .price-info {
    .price-label {
      display: block;
      font-size: var(--font-size-xs);
      color: var(--text-tertiary);
      margin-bottom: var(--spacing-xs);
    }
    
    .price-value {
      font-size: var(--font-size-xl);
      font-weight: bold;
      color: var(--error-color);
    }
  }
  
  .action-btns {
    .btn-primary {
      background: var(--primary-color);
      color: white;
      font-size: var(--font-size-base);
      padding: var(--spacing-sm) var(--spacing-xl);
      border-radius: var(--border-radius-lg);
      line-height: 1.5;
      transition: all 0.2s ease;
      border: none;
      outline: none;
      
      &:active {
        background-color: var(--primary-active);
        transform: scale(0.98);
      }
    }
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
