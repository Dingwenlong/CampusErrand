<template>
  <view class="container container-safe">
    <view v-if="order.id" class="order-detail">
      <!-- 订单状态 -->
      <view class="status-section">
        <view class="status-icon" :class="'status-' + order.status">
          <text class="iconfont" :class="getStatusIcon(order.status)"></text>
        </view>
        <text class="status-text">{{ order.statusName }}</text>
        <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
      </view>

      <!-- 任务信息 -->
      <view class="section-card">
        <view class="section-title">任务信息</view>
        <view class="info-list">
          <view class="info-row">
            <text class="info-label">任务标题</text>
            <text class="info-value">{{ order.taskTitle }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">任务类型</text>
            <text class="info-value">{{ order.taskTypeName }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">任务描述</text>
            <text class="info-value">{{ order.taskDesc }}</text>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="section-card">
        <view class="section-title">订单信息</view>
        <view class="info-list">
          <view class="info-row">
            <text class="info-label">订单编号</text>
            <text class="info-value">{{ order.orderNo }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">创建时间</text>
            <text class="info-value">{{ order.createTime }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">订单金额</text>
            <text class="info-value price">¥{{ order.amount }}</text>
          </view>
        </view>
      </view>

      <!-- 配送信息 -->
      <view class="section-card" v-if="order.deliveryInfo">
        <view class="section-title">配送信息</view>
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
            <text class="info-value">{{ order.deliveryInfo.contactPhone }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <view v-if="order.status === 0" class="btn btn-lg btn-primary" @click="cancelOrder">取消订单</view>
        <view v-if="order.status === 2" class="btn btn-lg btn-primary" @click="confirmOrder">确认完成</view>
        <view class="btn btn-lg btn-ghost" @click="contactService">联系客服</view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-else-if="loading" class="loading-state">
      <view class="loading-spinner"></view>
      <text>加载中...</text>
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
    cancelOrder() {
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
  background: var(--color-bg);
  padding: var(--space-6);
}

.order-detail {
  @include flex-column;
  gap: var(--space-6);
}

.status-section {
  @include flex-column;
  align-items: center;
  padding: var(--space-12) var(--space-6);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);

  .status-icon {
    width: 120rpx;
    height: 120rpx;
    @include flex-center;
    border-radius: var(--radius-full);
    margin-bottom: var(--space-4);

    &.status-0 {
      background: rgba(250, 173, 20, 0.1);
      .iconfont { color: var(--color-warning); }
    }

    &.status-1, &.status-2 {
      background: rgba(24, 144, 255, 0.1);
      .iconfont { color: var(--color-info); }
    }

    &.status-3 {
      background: rgba(82, 196, 26, 0.1);
      .iconfont { color: var(--color-success); }
    }

    &.status-4 {
      background: rgba(255, 77, 79, 0.1);
      .iconfont { color: var(--color-error); }
    }

    .iconfont {
      font-size: var(--font-size-3xl);
    }
  }

  .status-text {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
    margin-bottom: var(--space-2);
  }

  .status-desc {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
  }
}

.section-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);

  .section-title {
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
    margin-bottom: var(--space-5);
    padding-bottom: var(--space-4);
    border-bottom: 1rpx solid var(--color-border);
  }

  .info-list {
    @include flex-column;
    gap: var(--space-4);

    .info-row {
      @include flex-between;

      .info-label {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
      }

      .info-value {
        font-size: var(--font-size-sm);
        color: var(--color-text-primary);
        max-width: 60%;
        text-align: right;

        &.price {
          color: var(--color-primary);
          font-weight: var(--font-weight-bold);
          font-size: var(--font-size-lg);
        }
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

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
