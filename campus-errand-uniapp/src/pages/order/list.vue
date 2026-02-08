<template>
  <view class="container container-safe">
    <!-- 筛选标签 -->
    <view class="filter-bar">
      <view 
        class="filter-item" 
        :class="{ active: currentFilter === 0 }" 
        @click="changeFilter(0)"
      >
        <text>全部</text>
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentFilter === 1 }" 
        @click="changeFilter(1)"
      >
        <text>进行中</text>
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentFilter === 2 }" 
        @click="changeFilter(2)"
      >
        <text>已完成</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view scroll-y class="order-scroll" @scrolltolower="loadMore">
      <!-- 空状态 -->
      <view v-if="orders.length === 0 && !loading" class="empty-state">
        <view class="empty-icon-wrapper">
          <text class="iconfont icon-empty empty-icon"></text>
        </view>
        <text class="empty-title">暂无订单</text>
        <text class="empty-desc">您还没有相关订单</text>
      </view>

      <!-- 订单列表 -->
      <view v-else class="order-list">
        <view 
          class="order-card" 
          v-for="(item, index) in orders" 
          :key="index"
          @click="viewDetail(item)"
        >
          <view class="order-header">
            <text class="order-no">订单号: {{ item.orderNo }}</text>
            <view class="status-badge" :class="'status-' + item.status">
              {{ item.statusName }}
            </view>
          </view>
          <view class="order-content">
            <view class="task-info">
              <text class="task-title">{{ item.taskTitle }}</text>
              <text class="task-type">{{ item.taskTypeName }}</text>
            </view>
            <view class="order-info">
              <view class="info-item">
                <text class="info-label">创建时间:</text>
                <text class="info-value">{{ item.createTime }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">金额:</text>
                <text class="info-value price">¥{{ item.amount }}</text>
              </view>
            </view>
          </view>
          <view class="order-actions">
            <view class="btn btn-sm btn-ghost" @click.stop="contactService">联系客服</view>
            <view v-if="item.status === 0" class="btn btn-sm btn-primary" @click.stop="cancelOrder(item)">取消订单</view>
            <view v-if="item.status === 2" class="btn btn-sm btn-primary" @click.stop="confirmOrder(item)">确认完成</view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="orders.length > 0" class="load-more">
        <view v-if="loading" class="loading-indicator">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        <text v-else-if="noMore" class="no-more">没有更多了</text>
        <text v-else>上拉加载更多</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import orderApi from '@/api/order.js'

export default {
  data() {
    return {
      currentFilter: 0,
      orders: [],
      current: 1,
      size: 10,
      loading: false,
      noMore: false
    }
  },
  onShow() {
    this.loadOrders()
  },
  methods: {
    changeFilter(filter) {
      if (this.currentFilter === filter) return
      this.currentFilter = filter
      this.current = 1
      this.orders = []
      this.noMore = false
      this.loadOrders()
    },
    async loadOrders() {
      if (this.loading || this.noMore) return

      this.loading = true
      try {
        const res = await orderApi.getOrderList({
          status: this.currentFilter,
          current: this.current,
          size: this.size
        })

        if (res.code === 200) {
          const data = res.data
          const list = data.list || []

          if (list.length < this.size) {
            this.noMore = true
          }

          if (this.current === 1) {
            this.orders = list
          } else {
            this.orders = [...this.orders, ...list]
          }
        }
      } catch (e) {
        console.error('加载订单失败', e)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      if (this.noMore || this.loading) return
      this.current++
      this.loadOrders()
    },
    viewDetail(item) {
      uni.navigateTo({
        url: `/pages/order/detail?id=${item.id}`
      })
    },
    contactService() {
      uni.showToast({
        title: '客服功能开发中',
        icon: 'none'
      })
    },
    cancelOrder(item) {
      uni.showModal({
        title: '提示',
        content: '确定要取消该订单吗？',
        success: (res) => {
          if (res.confirm) {
            this.handleCancel(item.id)
          }
        }
      })
    },
    async handleCancel(orderId) {
      try {
        const res = await orderApi.cancelOrder(orderId)
        if (res.code === 200) {
          uni.showToast({
            title: '取消成功',
            icon: 'success'
          })
          this.current = 1
          this.loadOrders()
        }
      } catch (e) {
        uni.showToast({
          title: '取消失败',
          icon: 'none'
        })
      }
    },
    confirmOrder(item) {
      uni.showModal({
        title: '提示',
        content: '确认该订单已完成？',
        success: (res) => {
          if (res.confirm) {
            this.handleConfirm(item.id)
          }
        }
      })
    },
    async handleConfirm(orderId) {
      try {
        const res = await orderApi.confirmOrder(orderId)
        if (res.code === 200) {
          uni.showToast({
            title: '确认成功',
            icon: 'success'
          })
          this.current = 1
          this.loadOrders()
        }
      } catch (e) {
        uni.showToast({
          title: '确认失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@use '@/static/styles/mixins.scss' as *;

.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--color-bg);
}

.filter-bar {
  @include flex-between;
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

.order-scroll {
  flex: 1;
  padding: 0 var(--space-6);
}

.empty-state {
  @include flex-column;
  align-items: center;
  padding: var(--space-24) var(--space-8);
  
  .empty-icon-wrapper {
    width: 160rpx;
    height: 160rpx;
    @include flex-center;
    background: var(--color-bg-secondary);
    border-radius: var(--radius-full);
    margin-bottom: var(--space-6);
  }

  .empty-icon {
    font-size: 80rpx;
    color: var(--color-text-tertiary);
  }

  .empty-title {
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-medium);
    color: var(--color-text-primary);
    margin-bottom: var(--space-2);
  }
  
  .empty-desc {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
  }
}

.order-list {
  padding-bottom: var(--space-4);
}

.order-card {
  @include card-base;
  margin-bottom: var(--space-4);
  overflow: hidden;

  &:active {
    box-shadow: var(--shadow-md);
    transform: translateY(-2rpx);
  }

  .order-header {
    @include flex-between;
    padding: var(--space-4) var(--space-6);
    background: var(--color-bg-secondary);
    border-bottom: 1rpx solid var(--color-border);

    .order-no {
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
    }

    .status-badge {
      font-size: var(--font-size-xs);
      padding: var(--space-1) var(--space-3);
      border-radius: var(--radius-sm);
      font-weight: var(--font-weight-medium);

      &.status-0 {
        background: rgba(250, 173, 20, 0.1);
        color: var(--color-warning);
      }

      &.status-1 {
        background: rgba(24, 144, 255, 0.1);
        color: var(--color-info);
      }

      &.status-2 {
        background: rgba(82, 196, 26, 0.1);
        color: var(--color-success);
      }

      &.status-3 {
        background: rgba(255, 77, 79, 0.1);
        color: var(--color-error);
      }
    }
  }

  .order-content {
    padding: var(--space-6);

    .task-info {
      margin-bottom: var(--space-4);

      .task-title {
        font-size: var(--font-size-lg);
        font-weight: var(--font-weight-semibold);
        color: var(--color-text-primary);
        display: block;
        margin-bottom: var(--space-2);
      }

      .task-type {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        background: var(--color-bg-secondary);
        padding: var(--space-1) var(--space-3);
        border-radius: var(--radius-sm);
      }
    }

    .order-info {
      @include flex-column;
      gap: var(--space-2);

      .info-item {
        @include flex-vcenter;

        .info-label {
          font-size: var(--font-size-sm);
          color: var(--color-text-secondary);
          margin-right: var(--space-2);
        }

        .info-value {
          font-size: var(--font-size-sm);
          color: var(--color-text-primary);

          &.price {
            color: var(--color-primary);
            font-weight: var(--font-weight-semibold);
          }
        }
      }
    }
  }

  .order-actions {
    @include flex-vcenter;
    justify-content: flex-end;
    gap: var(--space-3);
    padding: var(--space-4) var(--space-6);
    border-top: 1rpx solid var(--color-border);
  }
}

.load-more {
  text-align: center;
  padding: var(--space-8) 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  
  .loading-indicator {
    @include flex-vcenter;
    justify-content: center;
    
    .loading-spinner {
      width: 32rpx;
      height: 32rpx;
      border: 3rpx solid var(--color-border);
      border-top-color: var(--color-primary);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin-right: var(--space-3);
    }
  }
  
  .no-more {
    color: var(--color-text-tertiary);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
