<template>
  <view class="container" v-if="task">
    <!-- 状态栏 -->
    <view class="status-bar" :class="'status-' + task.status">
      <view class="status-icon">
        <text class="iconfont" :class="getStatusIcon(task.status)"></text>
      </view>
      <view class="status-content">
        <text class="status-text">{{ task.statusName }}</text>
        <text class="status-desc">{{ getStatusDesc(task.status) }}</text>
      </view>
    </view>

    <!-- 任务信息 -->
    <view class="info-card card-hover">
      <view class="info-header">
        <view class="task-type" :class="'type-' + task.taskType">{{ task.taskTypeName }}</view>
        <view class="task-price">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ task.totalAmount }}</text>
        </view>
      </view>
      <view class="task-title">{{ task.title }}</view>
      <view class="task-desc" v-if="task.description">{{ task.description }}</view>
    </view>

    <!-- 地址信息 -->
    <view class="address-card card-hover">
      <view class="address-item">
        <view class="address-icon pickup">
          <text class="iconfont icon-location"></text>
        </view>
        <view class="address-content">
          <view class="address-title">取件地址</view>
          <view class="address-detail">{{ task.pickupAddress }}</view>
          <view class="address-contact" v-if="task.pickupContact || task.pickupPhone">
            <text class="contact-name">{{ task.pickupContact }}</text>
            <text class="contact-phone" v-if="task.pickupPhone">{{ task.pickupPhone }}</text>
          </view>
        </view>
        <view class="address-action" v-if="task.pickupPhone" @click="makeCall(task.pickupPhone)">
          <text class="iconfont icon-phone"></text>
        </view>
      </view>
      <view class="address-divider">
        <view class="divider-dot"></view>
        <view class="divider-dot"></view>
        <view class="divider-dot"></view>
      </view>
      <view class="address-item">
        <view class="address-icon delivery">
          <text class="iconfont icon-location"></text>
        </view>
        <view class="address-content">
          <view class="address-title">送达地址</view>
          <view class="address-detail">{{ task.deliveryAddress }}</view>
          <view class="address-contact" v-if="task.deliveryContact || task.deliveryPhone">
            <text class="contact-name">{{ task.deliveryContact }}</text>
            <text class="contact-phone" v-if="task.deliveryPhone">{{ task.deliveryPhone }}</text>
          </view>
        </view>
        <view class="address-action" v-if="task.deliveryPhone" @click="makeCall(task.deliveryPhone)">
          <text class="iconfont icon-phone"></text>
        </view>
      </view>
    </view>

    <!-- 时间信息 -->
    <view class="time-card card-hover">
      <view class="time-item">
        <view class="time-icon">
          <text class="iconfont icon-time"></text>
        </view>
        <view class="time-content">
          <text class="time-label">发布时间</text>
          <text class="time-value">{{ task.createTime }}</text>
        </view>
      </view>
      <view class="time-item" v-if="task.expectTime">
        <view class="time-icon expect">
          <text class="iconfont icon-clock"></text>
        </view>
        <view class="time-content">
          <text class="time-label">期望送达</text>
          <text class="time-value">{{ task.expectTime }}</text>
        </view>
      </view>
      <view class="time-item" v-if="task.deadlineTime">
        <view class="time-icon deadline">
          <text class="iconfont icon-deadline"></text>
        </view>
        <view class="time-content">
          <text class="time-label">截止时间</text>
          <text class="time-value">{{ task.deadlineTime }}</text>
        </view>
      </view>
    </view>

    <!-- 发布者信息 -->
    <view class="publisher-card card-hover">
      <view class="publisher-header">
        <text class="section-title">发布者信息</text>
      </view>
      <view class="publisher-info">
        <view class="publisher-avatar" :class="'avatar-type-' + task.taskType">
          <text class="avatar-text">{{ task.publisherName ? task.publisherName.charAt(0) : '?' }}</text>
        </view>
        <view class="publisher-detail">
          <text class="publisher-name">{{ task.publisherName }}</text>
          <view class="publisher-stats">
            <view class="credit-badge">
              <text class="credit-icon">⭐</text>
              <text class="credit-value">{{ task.publisherCreditScore }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 接单者信息 -->
    <view class="runner-card card-hover" v-if="task.runnerId">
      <view class="runner-header">
        <text class="section-title">接单者信息</text>
      </view>
      <view class="runner-info">
        <view class="runner-avatar avatar-gradient">
          <text class="avatar-text">{{ task.runnerName ? task.runnerName.charAt(0) : '?' }}</text>
        </view>
        <view class="runner-detail">
          <text class="runner-name">{{ task.runnerName }}</text>
          <view class="runner-stats">
            <view class="credit-badge">
              <text class="credit-icon">⭐</text>
              <text class="credit-value">{{ task.runnerCreditScore || 100 }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 备注 -->
    <view class="remark-card card-hover" v-if="task.remark">
      <view class="remark-header">
        <text class="iconfont icon-remark"></text>
        <text class="remark-title">备注</text>
      </view>
      <view class="remark-content">{{ task.remark }}</view>
    </view>

    <!-- 底部占位 -->
    <view class="bottom-placeholder"></view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="price-info">
        <text class="price-label">任务金额</text>
        <view class="price-value-wrapper">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ task.totalAmount }}</text>
        </view>
      </view>
      <view class="action-btns">
        <button 
          class="btn-primary pressable" 
          v-if="task.status === 0 && task.userId !== currentUserId"
          @click="acceptTask"
        >
          <text class="iconfont icon-accept"></text>
          <text>立即抢单</text>
        </button>
        <button 
          class="btn-primary pressable" 
          v-if="task.status === 1 && task.runnerId === currentUserId"
          @click="updateStatus(2)"
        >
          <text class="iconfont icon-check"></text>
          <text>确认取件</text>
        </button>
        <button 
          class="btn-primary pressable" 
          v-if="task.status === 2 && task.runnerId === currentUserId"
          @click="updateStatus(3)"
        >
          <text class="iconfont icon-deliver"></text>
          <text>开始配送</text>
        </button>
        <button 
          class="btn-primary pressable" 
          v-if="task.status === 3 && task.runnerId === currentUserId"
          @click="updateStatus(4)"
        >
          <text class="iconfont icon-check-circle"></text>
          <text>确认送达</text>
        </button>
        <button 
          class="btn-primary pressable" 
          v-if="task.status === 4 && task.userId === currentUserId"
          @click="updateStatus(5)"
        >
          <text class="iconfont icon-complete"></text>
          <text>确认完成</text>
        </button>
        <view class="status-badge" v-if="task.status === 5 || task.status === 6">
          <text class="iconfont icon-finished"></text>
          <text>{{ task.status === 5 ? '已完成' : '已取消' }}</text>
        </view>
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
      // 触觉反馈
      // #ifdef MP-WEIXIN
      if (uni.vibrateShort) {
        uni.vibrateShort({ type: 'medium' })
      }
      // #endif
      
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
      
      // 触觉反馈
      // #ifdef MP-WEIXIN
      if (uni.vibrateShort) {
        uni.vibrateShort({ type: 'light' })
      }
      // #endif
      
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
    
    makeCall(phone) {
      uni.makePhoneCall({
        phoneNumber: phone
      })
    },
    
    getStatusIcon(status) {
      const iconMap = {
        0: 'icon-waiting',
        1: 'icon-accepted',
        2: 'icon-pickup',
        3: 'icon-delivering',
        4: 'icon-delivered',
        5: 'icon-completed',
        6: 'icon-cancelled'
      }
      return iconMap[status] || 'icon-waiting'
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
@use '@/static/styles/mixins.scss' as *;

.container {
  padding: var(--space-6);
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
  background-color: var(--color-bg);
  min-height: 100vh;
}

.status-bar {
  display: flex;
  align-items: center;
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-md);
  animation: slideIn var(--duration-normal) var(--ease-out) both;
  
  &.status-0 {
    background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  }
  
  &.status-1, &.status-2, &.status-3 {
    background: linear-gradient(135deg, #FFB088 0%, #7BC47F 100%);
  }
  
  &.status-4 {
    background: linear-gradient(135deg, #7BC47F 0%, #9DD9A0 100%);
  }
  
  &.status-5 {
    background: linear-gradient(135deg, #7BC47F 0%, #9DD9A0 100%);
  }
  
  &.status-6 {
    background: linear-gradient(135deg, #A89888 0%, #B8A898 100%);
  }
}

.status-bar {
  .status-icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: var(--space-5);
    
    .iconfont {
      font-size: 40rpx;
      color: var(--color-white);
    }
  }
  
  .status-content {
    flex: 1;
  }
  
  .status-text {
    display: block;
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-white);
    margin-bottom: var(--space-1);
  }
  
  .status-desc {
    font-size: var(--font-size-sm);
    color: rgba(255, 255, 255, 0.9);
  }
}

.info-card {
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-sm);
  animation: slideIn var(--duration-normal) var(--ease-out) both;
  animation-delay: 0.1s;
  
  .info-header {
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
        font-size: var(--font-size-2xl);
        font-weight: var(--font-weight-bold);
      }
    }
  }
  
  .task-title {
    font-size: var(--font-size-lg);
    color: var(--color-text-primary);
    font-weight: var(--font-weight-semibold);
    margin-bottom: var(--space-3);
    line-height: var(--line-height-snug);
  }
  
  .task-desc {
    font-size: var(--font-size-base);
    color: var(--color-text-secondary);
    line-height: var(--line-height-relaxed);
  }
}

.address-card {
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-sm);
  animation: slideIn var(--duration-normal) var(--ease-out) both;
  animation-delay: 0.2s;
  
  .address-item {
    display: flex;
    align-items: flex-start;
    
    .address-icon {
      width: 72rpx;
      height: 72rpx;
      border-radius: 50%;
      @include flex-center;
      margin-right: var(--space-4);
      flex-shrink: 0;
      
      &.pickup {
        background-color: var(--color-task-shopping-soft);
        color: var(--color-task-shopping);
      }
      
      &.delivery {
        background-color: var(--color-error-soft);
        color: var(--color-error);
      }
      
      .iconfont {
        font-size: var(--font-size-lg);
      }
    }
    
    .address-content {
      flex: 1;
      
      .address-title {
        font-size: var(--font-size-xs);
        color: var(--color-text-tertiary);
        margin-bottom: var(--space-1);
        text-transform: uppercase;
        letter-spacing: 1rpx;
      }
      
      .address-detail {
        font-size: var(--font-size-base);
        color: var(--color-text-primary);
        font-weight: var(--font-weight-medium);
        margin-bottom: var(--space-2);
        line-height: var(--line-height-snug);
      }
      
      .address-contact {
        display: flex;
        align-items: center;
        gap: var(--space-3);
        
        .contact-name {
          font-size: var(--font-size-sm);
          color: var(--color-text-secondary);
        }
        
        .contact-phone {
          font-size: var(--font-size-sm);
          color: var(--color-primary);
          background-color: var(--color-primary-soft);
          padding: var(--space-1) var(--space-3);
          border-radius: var(--radius-sm);
        }
      }
    }
    
    .address-action {
      width: 72rpx;
      height: 72rpx;
      border-radius: 50%;
      background-color: var(--color-success-soft);
      @include flex-center;
      margin-left: var(--space-4);
      transition: all var(--duration-fast) var(--ease-out);
      
      &:active {
        transform: scale(0.95);
        background-color: var(--color-success);
        
        .iconfont {
          color: var(--color-white);
        }
      }
      
      .iconfont {
        font-size: var(--font-size-lg);
        color: var(--color-success);
      }
    }
  }
  
  .address-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: var(--space-4) 0;
    margin-left: 36rpx;
    
    .divider-dot {
      width: 8rpx;
      height: 8rpx;
      border-radius: 50%;
      background-color: var(--color-border);
      margin: 0 var(--space-1);
      
      &:nth-child(2) {
        width: 6rpx;
        height: 6rpx;
      }
    }
  }
}

.time-card {
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-sm);
  animation: slideIn var(--duration-normal) var(--ease-out) both;
  animation-delay: 0.3s;
  
  .time-item {
    display: flex;
    align-items: center;
    margin-bottom: var(--space-4);
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .time-icon {
      width: 56rpx;
      height: 56rpx;
      border-radius: var(--radius-md);
      background-color: var(--color-bg);
      @include flex-center;
      margin-right: var(--space-4);
      
      &.expect {
        background-color: var(--color-primary-soft);
        color: var(--color-primary);
      }
      
      &.deadline {
        background-color: var(--color-error-soft);
        color: var(--color-error);
      }
      
      .iconfont {
        font-size: var(--font-size-base);
        color: var(--color-text-secondary);
      }
    }
    
    .time-content {
      flex: 1;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .time-label {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
      }
      
      .time-value {
        font-size: var(--font-size-sm);
        color: var(--color-text-primary);
        font-weight: var(--font-weight-medium);
      }
    }
  }
}

.publisher-card,
.runner-card {
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-sm);
  animation: slideIn var(--duration-normal) var(--ease-out) both;
  animation-delay: 0.4s;
  
  .publisher-header,
  .runner-header {
    margin-bottom: var(--space-5);
    
    .section-title {
      font-size: var(--font-size-base);
      font-weight: var(--font-weight-bold);
      color: var(--color-text-primary);
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
      margin-right: var(--space-5);
      box-shadow: var(--shadow-sm);
      @include flex-center;

      &.avatar-type-1 {
        background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
      }

      &.avatar-type-2 {
        background: linear-gradient(135deg, #7BC47F 0%, #9DD9A0 100%);
      }

      &.avatar-type-3 {
        background: linear-gradient(135deg, #FFB347 0%, #FFC970 100%);
      }

      &.avatar-type-4, &.avatar-type-5 {
        background: linear-gradient(135deg, #FFB088 0%, #FFC4A8 100%);
      }

      &.avatar-gradient {
        background: linear-gradient(135deg, #FFB088 0%, #FF8C5A 100%);
      }

      .avatar-text {
        color: var(--color-white);
        font-size: 40rpx;
        font-weight: var(--font-weight-bold);
      }
    }
    
    .publisher-detail,
    .runner-detail {
      .publisher-name,
      .runner-name {
        display: block;
        font-size: var(--font-size-lg);
        color: var(--color-text-primary);
        font-weight: var(--font-weight-semibold);
        margin-bottom: var(--space-2);
      }
      
      .publisher-stats,
      .runner-stats {
        .credit-badge {
          display: flex;
          align-items: center;
          background-color: var(--color-primary-soft);
          padding: var(--space-1) var(--space-3);
          border-radius: var(--radius-sm);
          
          .credit-icon {
            font-size: var(--font-size-xs);
            margin-right: 4rpx;
          }
          
          .credit-value {
            font-size: var(--font-size-xs);
            color: var(--color-primary-dark);
            font-weight: var(--font-weight-semibold);
          }
        }
      }
    }
  }
}

.remark-card {
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-sm);
  animation: slideIn var(--duration-normal) var(--ease-out) both;
  animation-delay: 0.5s;
  
  .remark-header {
    display: flex;
    align-items: center;
    margin-bottom: var(--space-4);
    
    .iconfont {
      font-size: var(--font-size-base);
      color: var(--color-text-tertiary);
      margin-right: var(--space-2);
    }
    
    .remark-title {
      font-size: var(--font-size-base);
      font-weight: var(--font-weight-bold);
      color: var(--color-text-primary);
    }
  }
  
  .remark-content {
    font-size: var(--font-size-base);
    color: var(--color-text-secondary);
    line-height: var(--line-height-relaxed);
    background-color: var(--color-bg);
    padding: var(--space-4);
    border-radius: var(--radius-lg);
  }
}

.bottom-placeholder {
  height: 140rpx;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--color-surface);
  padding: var(--space-4) var(--space-6);
  padding-bottom: calc(var(--space-4) + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(74, 55, 40, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
  
  .price-info {
    display: flex;
    flex-direction: column;
    
    .price-label {
      font-size: var(--font-size-xs);
      color: var(--color-text-tertiary);
      margin-bottom: var(--space-1);
    }
    
    .price-value-wrapper {
      display: flex;
      align-items: baseline;
      
      .price-symbol {
        font-size: var(--font-size-sm);
        font-weight: var(--font-weight-bold);
        color: var(--color-error);
      }
      
      .price-value {
        font-size: var(--font-size-2xl);
        font-weight: var(--font-weight-bold);
        color: var(--color-error);
      }
    }
  }
  
  .action-btns {
    .btn-primary {
      @include flex-center;
      gap: var(--space-2);
      min-width: 200rpx;
      height: 80rpx;
      padding: 0 var(--space-6);
      background: var(--color-primary-gradient);
      border-radius: var(--radius-full);
      font-size: var(--font-size-base);
      font-weight: var(--font-weight-semibold);
      color: var(--color-text-primary);
      border: none;
      transition: all var(--duration-fast) var(--ease-out);
      
      &:active {
        transform: scale(0.95);
        box-shadow: var(--shadow-primary);
      }
      
      .iconfont {
        font-size: var(--font-size-base);
      }
    }
    
    .status-badge {
      @include flex-center;
      gap: var(--space-2);
      height: 80rpx;
      padding: 0 var(--space-6);
      background-color: var(--color-bg);
      border-radius: var(--radius-full);
      font-size: var(--font-size-base);
      font-weight: var(--font-weight-medium);
      color: var(--color-text-secondary);
      
      .iconfont {
        font-size: var(--font-size-base);
      }
    }
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

@keyframes slideIn {
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

