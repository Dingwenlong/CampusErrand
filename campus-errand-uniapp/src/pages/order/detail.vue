<template>
  <view class="container container-safe">
    <view v-if="order.id" class="order-detail">
      <!-- 订单状态 -->
      <view class="status-section" :class="'status-bg-' + order.status">
        <view class="status-icon" :class="'status-' + order.status">
          <text class="iconfont" :class="getStatusIcon(order.status)"></text>
        </view>
        <text class="status-text">{{ order.statusName }}</text>
        <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
        <view class="status-progress" v-if="order.status < 4">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: getProgressWidth(order.status) + '%' }"></view>
          </view>
          <view class="progress-steps">
            <view class="step" :class="{ active: order.status >= 0 }">
              <view class="step-dot"></view>
              <text class="step-text">待接单</text>
            </view>
            <view class="step" :class="{ active: order.status >= 1 }">
              <view class="step-dot"></view>
              <text class="step-text">进行中</text>
            </view>
            <view class="step" :class="{ active: order.status >= 2 }">
              <view class="step-dot"></view>
              <text class="step-text">待确认</text>
            </view>
            <view class="step" :class="{ active: order.status >= 3 }">
              <view class="step-dot"></view>
              <text class="step-text">已完成</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 任务信息 -->
      <view class="section-card card-hover">
        <view class="section-header">
          <view class="section-icon">
            <text class="iconfont icon-task"></text>
          </view>
          <text class="section-title">任务信息</text>
        </view>
        <view class="info-list">
          <view class="info-row">
            <text class="info-label">任务标题</text>
            <text class="info-value">{{ order.taskTitle }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">任务类型</text>
            <view class="info-value">
              <text class="type-tag" :class="'type-' + order.taskType">{{ order.taskTypeName }}</text>
            </view>
          </view>
          <view class="info-row" v-if="order.taskDesc">
            <text class="info-label">任务描述</text>
            <text class="info-value">{{ order.taskDesc }}</text>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="section-card card-hover">
        <view class="section-header">
          <view class="section-icon">
            <text class="iconfont icon-order"></text>
          </view>
          <text class="section-title">订单信息</text>
        </view>
        <view class="info-list">
          <view class="info-row">
            <text class="info-label">订单编号</text>
            <view class="info-value copyable" @click="copyOrderNo">
              <text>{{ order.orderNo }}</text>
              <text class="copy-icon">📋</text>
            </view>
          </view>
          <view class="info-row">
            <text class="info-label">创建时间</text>
            <text class="info-value">{{ order.createTime }}</text>
          </view>
          <view class="info-row price-row">
            <text class="info-label">订单金额</text>
            <view class="info-value">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ order.amount }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 配送信息 -->
      <view class="section-card card-hover" v-if="order.deliveryInfo">
        <view class="section-header">
          <view class="section-icon">
            <text class="iconfont icon-location"></text>
          </view>
          <text class="section-title">配送信息</text>
        </view>
        <view class="info-list">
          <view class="info-row">
            <text class="info-label">配送地址</text>
            <text class="info-value">{{ order.deliveryInfo.address }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">联系人</text>
            <text class="info-value">{{ order.deliveryInfo.contactName }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">联系电话</text>
            <view class="info-value phone-value" @click="makeCall(order.deliveryInfo.contactPhone)">
              <text>{{ order.deliveryInfo.contactPhone }}</text>
              <text class="phone-icon">📞</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 跑腿员信息 -->
      <view class="section-card card-hover" v-if="order.runnerInfo">
        <view class="section-header">
          <view class="section-icon">
            <text class="iconfont icon-runner"></text>
          </view>
          <text class="section-title">跑腿员信息</text>
        </view>
        <view class="runner-info">
          <view class="runner-avatar">
            <text class="avatar-text">{{ order.runnerInfo.name ? order.runnerInfo.name.charAt(0) : '?' }}</text>
          </view>
          <view class="runner-detail">
            <text class="runner-name">{{ order.runnerInfo.name }}</text>
            <view class="runner-stats">
              <view class="credit-badge">
                <text class="credit-icon">⭐</text>
                <text class="credit-value">{{ order.runnerInfo.creditScore || 100 }}</text>
              </view>
            </view>
          </view>
          <view class="runner-action" @click="contactRunner" v-if="order.runnerInfo.phone">
            <text class="iconfont icon-phone"></text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <view v-if="order.status === 0" class="btn btn-lg btn-primary pressable" @click="cancelOrder">
          <text class="iconfont icon-close"></text>
          <text>取消订单</text>
        </view>
        <view v-if="order.status === 2" class="btn btn-lg btn-primary pressable" @click="confirmOrder">
          <text class="iconfont icon-check"></text>
          <text>确认完成</text>
        </view>
        <view class="btn btn-lg btn-ghost pressable" @click="contactService">
          <text class="iconfont icon-service"></text>
          <text>联系客服</text>
        </view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-else-if="loading" class="loading-state">
      <view class="loading-spinner"></view>
      <text>加载中...</text>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <view class="empty-icon">📭</view>
      <text class="empty-title">订单不存在</text>
      <text class="empty-desc">该订单可能已被删除或不存在</text>
    </view>
  </view>
</template>

<script>
import orderApi from '@/api/order.js'

export default {
  data() {
    return {
      orderId: null,
      order: {},
      loading: false
    }
  },
  onLoad(options) {
    this.orderId = options.id
    this.loadOrderDetail()
  },
  methods: {
    async loadOrderDetail() {
      this.loading = true
      try {
        const res = await orderApi.getOrderDetail(this.orderId)
        if (res.code === 200) {
          this.order = res.data
        }
      } catch (e) {
        console.error('加载订单详情失败', e)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    getStatusIcon(status) {
      const icons = {
        0: 'icon-pending',
        1: 'icon-processing',
        2: 'icon-delivering',
        3: 'icon-completed',
        4: 'icon-cancelled'
      }
      return icons[status] || 'icon-order'
    },
    getStatusDesc(status) {
      const descs = {
        0: '订单已创建，等待接单',
        1: '跑腿员已接单，正在处理',
        2: '任务已完成，等待确认',
        3: '订单已完成',
        4: '订单已取消'
      }
      return descs[status] || ''
    },
    getProgressWidth(status) {
      const widths = {
        0: 12.5,
        1: 37.5,
        2: 62.5,
        3: 100
      }
      return widths[status] || 0
    },
    copyOrderNo() {
      uni.setClipboardData({
        data: this.order.orderNo,
        success: () => {
          uni.showToast({
            title: '已复制',
            icon: 'none'
          })
        }
      })
    },
    makeCall(phone) {
      uni.makePhoneCall({
        phoneNumber: phone
      })
    },
    contactRunner() {
      if (this.order.runnerInfo && this.order.runnerInfo.phone) {
        this.makeCall(this.order.runnerInfo.phone)
      }
    },
    cancelOrder() {
      // 触觉反馈
      // #ifdef MP-WEIXIN
      if (uni.vibrateShort) {
        uni.vibrateShort({ type: 'light' })
      }
      // #endif
      
      uni.showModal({
        title: '提示',
        content: '确定要取消该订单吗？',
        success: (res) => {
          if (res.confirm) {
            this.handleCancel()
          }
        }
      })
    },
    async handleCancel() {
      try {
        const res = await orderApi.cancelOrder(this.orderId)
        if (res.code === 200) {
          uni.showToast({
            title: '取消成功',
            icon: 'success'
          })
          this.loadOrderDetail()
        }
      } catch (e) {
        uni.showToast({
          title: '取消失败',
          icon: 'none'
        })
      }
    },
    confirmOrder() {
      // 触觉反馈
      // #ifdef MP-WEIXIN
      if (uni.vibrateShort) {
        uni.vibrateShort({ type: 'medium' })
      }
      // #endif
      
      uni.showModal({
        title: '提示',
        content: '确认该订单已完成？',
        success: (res) => {
          if (res.confirm) {
            this.handleConfirm()
          }
        }
      })
    },
    async handleConfirm() {
      try {
        const res = await orderApi.confirmOrder(this.orderId)
        if (res.code === 200) {
          uni.showToast({
            title: '确认成功',
            icon: 'success'
          })
          this.loadOrderDetail()
        }
      } catch (e) {
        uni.showToast({
          title: '确认失败',
          icon: 'none'
        })
      }
    },
    contactService() {
      uni.showToast({
        title: '客服功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@use '@/static/styles/mixins.scss' as *;

.container {
  min-height: 100vh;
  background-color: var(--color-bg);
  padding: var(--space-6);
  padding-bottom: calc(var(--space-6) + env(safe-area-inset-bottom));
}

.order-detail {
  @include flex-column;
  gap: var(--space-6);
}

.status-section {
  @include flex-column;
  align-items: center;
  padding: var(--space-10) var(--space-6);
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  animation: slideIn var(--duration-normal) var(--ease-out) both;
  
  &.status-bg-0 {
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  }
  
  &.status-bg-1, &.status-bg-2 {
    background: linear-gradient(135deg, var(--color-brand-blue) 0%, var(--color-brand-mint) 100%);
  }
  
  &.status-bg-3 {
    background: linear-gradient(135deg, var(--color-success) 0%, var(--color-success-light) 100%);
  }
  
  &.status-bg-4 {
    background: linear-gradient(135deg, var(--color-text-tertiary) 0%, var(--color-text-disabled) 100%);
  }

  .status-icon {
    width: 120rpx;
    height: 120rpx;
    @include flex-center;
    border-radius: var(--radius-full);
    margin-bottom: var(--space-4);
    background: rgba(255, 255, 255, 0.2);

    &.status-0 .iconfont { color: var(--color-white); }
    &.status-1 .iconfont, &.status-2 .iconfont { color: var(--color-white); }
    &.status-3 .iconfont { color: var(--color-white); }
    &.status-4 .iconfont { color: var(--color-white); }

    .iconfont {
      font-size: var(--font-size-3xl);
    }
  }

  .status-text {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-white);
    margin-bottom: var(--space-2);
  }

  .status-desc {
    font-size: var(--font-size-sm);
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: var(--space-6);
  }
  
  .status-progress {
    width: 100%;
    padding: 0 var(--space-4);
    
    .progress-bar {
      height: 8rpx;
      background: rgba(255, 255, 255, 0.3);
      border-radius: var(--radius-full);
      margin-bottom: var(--space-4);
      overflow: hidden;
      
      .progress-fill {
        height: 100%;
        background: var(--color-white);
        border-radius: var(--radius-full);
        transition: width var(--duration-slow) var(--ease-out);
      }
    }
    
    .progress-steps {
      display: flex;
      justify-content: space-between;
      
      .step {
        @include flex-column;
        align-items: center;
        
        .step-dot {
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
          background: rgba(255, 255, 255, 0.4);
          margin-bottom: var(--space-1);
          transition: all var(--duration-fast) var(--ease-out);
        }
        
        .step-text {
          font-size: var(--font-size-xs);
          color: rgba(255, 255, 255, 0.7);
          transition: all var(--duration-fast) var(--ease-out);
        }
        
        &.active {
          .step-dot {
            background: var(--color-white);
            box-shadow: 0 0 8rpx rgba(255, 255, 255, 0.5);
          }
          
          .step-text {
            color: var(--color-white);
            font-weight: var(--font-weight-medium);
          }
        }
      }
    }
  }
}

.section-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  animation: slideIn var(--duration-normal) var(--ease-out) both;
  animation-delay: 0.1s;

  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: var(--space-5);
    
    .section-icon {
      width: 48rpx;
      height: 48rpx;
      border-radius: var(--radius-md);
      background: var(--color-primary-soft);
      @include flex-center;
      margin-right: var(--space-3);
      
      .iconfont {
        font-size: var(--font-size-base);
        color: var(--color-primary);
      }
    }
  }

  .section-title {
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
  }

  .info-list {
    @include flex-column;
    gap: var(--space-4);

    .info-row {
      @include flex-between;
      align-items: flex-start;

      .info-label {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        flex-shrink: 0;
        margin-right: var(--space-4);
      }

      .info-value {
        font-size: var(--font-size-sm);
        color: var(--color-text-primary);
        text-align: right;
        word-break: break-all;
        
        &.copyable {
          @include flex-center;
          gap: var(--space-2);
          color: var(--color-primary);
          
          .copy-icon {
            font-size: var(--font-size-xs);
          }
        }
        
        &.phone-value {
          @include flex-center;
          gap: var(--space-2);
          color: var(--color-success);
          
          .phone-icon {
            font-size: var(--font-size-xs);
          }
        }
        
        .type-tag {
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
      }
      
      &.price-row {
        padding-top: var(--space-4);
        border-top: 2rpx solid var(--color-divider);
        
        .info-value {
          display: flex;
          align-items: baseline;
          
          .price-symbol {
            font-size: var(--font-size-sm);
            font-weight: var(--font-weight-bold);
            color: var(--color-error);
          }
          
          .price-value {
            font-size: var(--font-size-xl);
            font-weight: var(--font-weight-bold);
            color: var(--color-error);
          }
        }
      }
    }
  }
  
  .runner-info {
    display: flex;
    align-items: center;
    
    .runner-avatar {
      width: 88rpx;
      height: 88rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--color-brand-blue) 0%, var(--color-brand-blue-dark) 100%);
      @include flex-center;
      margin-right: var(--space-4);
      
      .avatar-text {
        color: var(--color-white);
        font-size: 36rpx;
        font-weight: var(--font-weight-bold);
      }
    }
    
    .runner-detail {
      flex: 1;
      
      .runner-name {
        font-size: var(--font-size-base);
        font-weight: var(--font-weight-semibold);
        color: var(--color-text-primary);
        margin-bottom: var(--space-1);
      }
      
      .runner-stats {
        .credit-badge {
          display: flex;
          align-items: center;
          background-color: var(--color-primary-soft);
          padding: var(--space-1) var(--space-2);
          border-radius: var(--radius-sm);
          width: fit-content;
          
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
    
    .runner-action {
      width: 72rpx;
      height: 72rpx;
      border-radius: 50%;
      background-color: var(--color-success-soft);
      @include flex-center;
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
}

.action-section {
  @include flex-column;
  gap: var(--space-4);
  margin-top: var(--space-6);

  .btn {
    width: 100%;
    @include flex-center;
    gap: var(--space-2);
    
    .iconfont {
      font-size: var(--font-size-base);
    }
  }
}

.loading-state {
  @include flex-column;
  align-items: center;
  justify-content: center;
  padding: var(--space-24) 0;

  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid var(--color-border);
    border-top-color: var(--color-primary);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-bottom: var(--space-4);
  }

  text {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
  }
}

.empty-state {
  @include flex-column;
  align-items: center;
  justify-content: center;
  padding: var(--space-24) 0;
  
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

.pressable {
  transition: transform var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: scale(0.98);
  }
}

.card-hover {
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: translateY(-2rpx);
    box-shadow: var(--shadow-md);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
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

