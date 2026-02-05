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
        <image :src="task.publisherAvatar || '/static/avatar/default.png'" class="publisher-avatar"></image>
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
        <image :src="task.runnerAvatar || '/static/avatar/default.png'" class="runner-avatar"></image>
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
  padding: 20rpx;
  padding-bottom: 140rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.status-bar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  padding: 40rpx;
  margin-bottom: 20rpx;
  color: #fff;
  
  .status-text {
    display: block;
    font-size: 40rpx;
    font-weight: bold;
    margin-bottom: 12rpx;
  }
  
  .status-desc {
    font-size: 28rpx;
    opacity: 0.9;
  }
}

.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  
  .info-header {
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
      font-size: 40rpx;
      font-weight: bold;
      color: #ff4d4f;
    }
  }
  
  .task-title {
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 12rpx;
  }
  
  .task-desc {
    font-size: 28rpx;
    color: #666;
    line-height: 1.6;
  }
}

.address-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  
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
      margin-right: 20rpx;
      
      &.pickup {
        background: #f6ffed;
        color: #52c41a;
      }
      
      &.delivery {
        background: #fff2f0;
        color: #ff4d4f;
      }
      
      .iconfont {
        font-size: 32rpx;
      }
    }
    
    .address-content {
      flex: 1;
      
      .address-title {
        font-size: 26rpx;
        color: #999;
        margin-bottom: 8rpx;
      }
      
      .address-detail {
        font-size: 30rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 8rpx;
      }
      
      .address-contact {
        font-size: 26rpx;
        color: #666;
      }
    }
  }
  
  .address-divider {
    height: 1rpx;
    background: #f0f0f0;
    margin: 24rpx 0;
  }
}

.time-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  
  .time-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .time-label {
      font-size: 28rpx;
      color: #666;
    }
    
    .time-value {
      font-size: 28rpx;
      color: #333;
    }
  }
}

.publisher-card,
.runner-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  
  .publisher-header,
  .runner-header {
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
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
      margin-right: 24rpx;
    }
    
    .publisher-detail,
    .runner-detail {
      .publisher-name,
      .runner-name {
        display: block;
        font-size: 32rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 12rpx;
      }
      
      .publisher-stats {
        .credit {
          font-size: 26rpx;
          color: #faad14;
          background: #fffbe6;
          padding: 6rpx 16rpx;
          border-radius: 8rpx;
        }
      }
    }
  }
}

.remark-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  
  .remark-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 16rpx;
  }
  
  .remark-content {
    font-size: 28rpx;
    color: #666;
    line-height: 1.6;
  }
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .price-info {
    .price-label {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-bottom: 8rpx;
    }
    
    .price-value {
      font-size: 40rpx;
      font-weight: bold;
      color: #ff4d4f;
    }
  }
  
  .action-btns {
    .btn-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      font-size: 30rpx;
      padding: 20rpx 60rpx;
      border-radius: 40rpx;
      line-height: 1.5;
    }
  }
}
</style>
