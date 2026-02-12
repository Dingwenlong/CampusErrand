<template>
  <view class="container">
    <!-- 角色切换标签 -->
    <view class="role-tabs">
      <view 
        class="role-tab" 
        :class="{ active: currentRole === 1 }"
        @click="switchRole(1)"
      >
        <text class="tab-text">我发布的</text>
        <view class="tab-line" v-if="currentRole === 1"></view>
      </view>
      <view 
        class="role-tab" 
        :class="{ active: currentRole === 2 }"
        @click="switchRole(2)"
      >
        <text class="tab-text">我接单的</text>
        <view class="tab-line" v-if="currentRole === 2"></view>
      </view>
    </view>

    <!-- 状态筛选 -->
    <view class="status-filter">
      <scroll-view scroll-x class="filter-scroll">
        <view 
          class="filter-item" 
          :class="{ active: currentStatus === null }"
          @click="selectStatus(null)"
        >
          全部
        </view>
        <view 
          class="filter-item" 
          :class="{ active: currentStatus === 0 }"
          @click="selectStatus(0)"
        >
          待接单
        </view>
        <view 
          class="filter-item" 
          :class="{ active: currentStatus === 1 }"
          @click="selectStatus(1)"
        >
          进行中
        </view>
        <view 
          class="filter-item" 
          :class="{ active: currentStatus === 5 }"
          @click="selectStatus(5)"
        >
          已完成
        </view>
        <view 
          class="filter-item" 
          :class="{ active: currentStatus === 6 }"
          @click="selectStatus(6)"
        >
          已取消
        </view>
      </scroll-view>
    </view>

    <!-- 订单列表 -->
    <scroll-view 
      scroll-y 
      class="order-list"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="order-item" v-for="(item, index) in orderList" :key="index" @click="goDetail(item)">
        <view class="order-header">
          <view class="task-type" :class="'type-' + item.taskType">{{ item.taskTypeName }}</view>
          <text class="order-status" :class="'status-' + item.status">{{ item.statusName }}</text>
        </view>
        
        <view class="order-title">{{ item.title }}</view>
        
        <view class="order-address">
          <view class="address-item">
            <text class="dot pickup"></text>
            <text class="address-text">{{ item.pickupAddress }}</text>
          </view>
          <view class="address-item">
            <text class="dot delivery"></text>
            <text class="address-text">{{ item.deliveryAddress }}</text>
          </view>
        </view>
        
        <view class="order-footer">
          <view class="user-info">
            <image 
              :src="currentRole === 1 ? (item.runnerAvatar || '/static/avatar/default.png') : (item.publisherAvatar || '/static/avatar/default.png')" 
              class="avatar"
            ></image>
            <text class="username">{{ currentRole === 1 ? item.runnerName : item.publisherName }}</text>
          </view>
          <view class="order-amount">
            <text class="amount-label">金额</text>
            <text class="amount-value">¥{{ item.totalAmount }}</text>
          </view>
        </view>
        
        <!-- 操作按钮 -->
        <view class="order-actions">
          <!-- 发单者操作 -->
          <template v-if="currentRole === 1">
            <button 
              class="btn btn-cancel" 
              v-if="item.status === 0 || item.status === 1 || item.status === 2"
              @click.stop="cancelOrder(item)"
            >
              取消订单
            </button>
            <button 
              class="btn btn-primary" 
              v-if="item.status === 4"
              @click.stop="confirmReceive(item)"
            >
              确认收货
            </button>
            <button 
              class="btn btn-primary" 
              v-if="item.status === 5 && !item.evaluated"
              @click.stop="goEvaluate(item)"
            >
              评价
            </button>
          </template>
          
          <!-- 接单者操作 -->
          <template v-if="currentRole === 2">
            <button 
              class="btn btn-cancel" 
              v-if="item.status === 1 || item.status === 2"
              @click.stop="cancelOrder(item)"
            >
              取消接单
            </button>
            <button 
              class="btn btn-primary" 
              v-if="item.status === 3"
              @click.stop="confirmDeliver(item)"
            >
              确认送达
            </button>
            <button 
              class="btn btn-primary" 
              v-if="item.status === 5 && !item.evaluated"
              @click.stop="goEvaluate(item)"
            >
              评价
            </button>
          </template>
        </view>
      </view>
      
      <!-- 加载状态 -->
      <view class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else-if="orderList.length === 0">暂无订单</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import taskApi from '@/api/task.js'

export default {
  data() {
    return {
      currentRole: 1, // 1-发单者 2-接单者
      currentStatus: null,
      orderList: [],
      current: 1,
      size: 10,
      loading: false,
      noMore: false,
      refreshing: false
    }
  },
  onLoad() {
    this.refreshList()
  },
  onShow() {
    this.refreshList()
  },
  methods: {
    refreshList() {
      this.current = 1
      this.noMore = false
      this.loadOrderList()
    },

    switchRole(role) {
      if (this.currentRole === role) return
      this.currentRole = role
      this.orderList = []
      this.refreshList()
    },
    
    selectStatus(status) {
      this.currentStatus = status
      this.orderList = []
      this.refreshList()
    },
    
    async loadOrderList() {
      if (this.loading || this.noMore) return
      
      this.loading = true
      
      try {
        const params = {
          role: this.currentRole,
          current: this.current,
          size: this.size
        }
        
        if (this.currentStatus !== null) {
          params.status = this.currentStatus
        }
        
        const res = await taskApi.getMyTasks(params)
        
        if (res.code === 200) {
          const records = res.data.records || []
          
          if (this.current === 1) {
            this.orderList = records
          } else {
            this.orderList = this.orderList.concat(records)
          }
          
          if (records.length < this.size) {
            this.noMore = true
          }
        }
      } catch (e) {
        console.error('加载订单失败', e)
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    
    loadMore() {
      if (!this.noMore && !this.loading) {
        this.current++
        this.loadOrderList()
      }
    },
    
    onRefresh() {
      this.refreshing = true
      this.orderList = []
      this.refreshList()
    },
    
    goDetail(item) {
      uni.navigateTo({
        url: `/pages/order/detail?id=${item.id}`
      })
    },
    
    async cancelOrder(item) {
      const cancelType = this.currentRole === 1 ? 1 : 2
      const title = this.currentRole === 1 ? '取消订单' : '取消接单'
      const content = this.currentRole === 1 ? '确定要取消这个订单吗？' : '确定要取消这个接单吗？'
      
      uni.showModal({
        title,
        content,
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({ title: '取消中...' })
              
              const result = await taskApi.cancel(item.id, {
                cancelType,
                reason: '用户主动取消'
              })
              
              uni.hideLoading()
              
              if (result.code === 200) {
                uni.showToast({
                  title: '取消成功',
                  icon: 'success'
                })
                this.orderList = []
                this.refreshList()
              } else {
                uni.showToast({
                  title: result.message || '取消失败',
                  icon: 'none'
                })
              }
            } catch (e) {
              uni.hideLoading()
              uni.showToast({
                title: '取消失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    async confirmReceive(item) {
      uni.showModal({
        title: '确认收货',
        content: '确认已经收到物品吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({ title: '确认中...' })
              
              const result = await taskApi.receive(item.id)
              
              uni.hideLoading()
              
              if (result.code === 200) {
                uni.showToast({
                  title: '确认成功',
                  icon: 'success'
                })
                this.orderList = []
                this.refreshList()
              } else {
                uni.showToast({
                  title: result.message || '确认失败',
                  icon: 'none'
                })
              }
            } catch (e) {
              uni.hideLoading()
              uni.showToast({
                title: '确认失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    async confirmDeliver(item) {
      uni.showModal({
        title: '确认送达',
        content: '确认已经送达目的地吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({ title: '确认中...' })
              
              const result = await taskApi.deliver(item.id)
              
              uni.hideLoading()
              
              if (result.code === 200) {
                uni.showToast({
                  title: '确认成功',
                  icon: 'success'
                })
                this.orderList = []
                this.refreshList()
              } else {
                uni.showToast({
                  title: result.message || '确认失败',
                  icon: 'none'
                })
              }
            } catch (e) {
              uni.hideLoading()
              uni.showToast({
                title: '确认失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    goEvaluate(item) {
      const toUserId = this.currentRole === 1 ? item.runnerId : item.userId
      uni.navigateTo({
        url: `/package-evaluation/pages/evaluation/submit?taskId=${item.id}&toUserId=${toUserId}&taskTitle=${item.title}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@use '@/static/styles/mixins.scss' as *;

.container {
  min-height: 100vh;
  background: radial-gradient(circle at top right, rgba(var(--color-primary-rgb), 0.1) 0%, transparent 45%), var(--color-bg);
}

.role-tabs {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  margin: var(--space-4) var(--space-5) 0;
  padding: var(--space-1);
  border-radius: var(--radius-full);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);

  .role-tab {
    position: relative;
    flex: 1;
    text-align: center;
    padding: var(--space-3) 0;
    border-radius: var(--radius-full);
    transition: all var(--duration-fast) var(--ease-out);

    .tab-text {
      font-size: var(--font-size-base);
      color: var(--color-text-secondary);
      transition: all var(--duration-fast) var(--ease-out);
    }

    &:active {
      transform: scale(0.97);
    }

    &.active {
      background: var(--color-primary-gradient);
      box-shadow: var(--shadow-primary);

      .tab-text {
        color: var(--color-text-primary);
        font-weight: var(--font-weight-semibold);
      }
    }

    .tab-line {
      display: none;
    }
  }
}

.status-filter {
  margin: var(--space-4) var(--space-5) var(--space-3);

  .filter-scroll {
    white-space: nowrap;

    .filter-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: var(--space-2) var(--space-5);
      margin-right: var(--space-3);
      border-radius: var(--radius-full);
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
      background: var(--color-surface);
      border: 2rpx solid var(--color-border-light);
      transition: all var(--duration-fast) var(--ease-out);

      &.active {
        background: var(--color-primary-gradient);
        border-color: transparent;
        color: var(--color-text-primary);
        font-weight: var(--font-weight-semibold);
        box-shadow: var(--shadow-primary);
      }
    }
  }
}

.order-list {
  height: calc(100vh - 236rpx);
  padding: 0 var(--space-5) var(--space-6);

  .order-item {
    background: var(--color-surface);
    border-radius: var(--radius-xl);
    padding: var(--space-5);
    margin-bottom: var(--space-4);
    box-shadow: var(--shadow-sm);
    border: 2rpx solid transparent;
    transition: all var(--duration-fast) var(--ease-out);

    &:active {
      transform: translateY(-2rpx);
      box-shadow: var(--shadow-md);
    }

    .order-header {
      @include flex-between;
      margin-bottom: var(--space-4);

      .task-type {
        padding: var(--space-1) var(--space-3);
        border-radius: var(--radius-sm);
        font-size: var(--font-size-xs);
        font-weight: var(--font-weight-medium);

        &.type-1 {
          background: var(--color-task-express-soft);
          color: var(--color-task-express);
        }

        &.type-2 {
          background: var(--color-task-shopping-soft);
          color: var(--color-task-shopping);
        }

        &.type-3 {
          background: var(--color-task-delivery-soft);
          color: var(--color-task-delivery);
        }

        &.type-4,
        &.type-5 {
          background: var(--color-task-other-soft);
          color: var(--color-task-other);
        }
      }

      .order-status {
        font-size: var(--font-size-sm);
        font-weight: var(--font-weight-semibold);

        &.status-0 {
          color: var(--color-warning);
        }

        &.status-1,
        &.status-2,
        &.status-3,
        &.status-4 {
          color: var(--color-info);
        }

        &.status-5 {
          color: var(--color-success);
        }

        &.status-6 {
          color: var(--color-text-tertiary);
        }
      }
    }

    .order-title {
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-semibold);
      color: var(--color-text-primary);
      margin-bottom: var(--space-4);
      line-height: var(--line-height-snug);
    }

    .order-address {
      margin-bottom: var(--space-4);

      .address-item {
        display: flex;
        align-items: center;
        margin-bottom: var(--space-2);

        &:last-child {
          margin-bottom: 0;
        }

        .dot {
          width: 14rpx;
          height: 14rpx;
          margin-right: var(--space-3);
          border-radius: var(--radius-full);

          &.pickup {
            background: var(--color-success);
          }

          &.delivery {
            background: var(--color-error);
          }
        }

        .address-text {
          font-size: var(--font-size-sm);
          color: var(--color-text-secondary);
        }
      }
    }

    .order-footer {
      @include flex-between;
      margin-bottom: var(--space-4);
      padding-top: var(--space-4);
      border-top: 2rpx solid var(--color-divider);

      .user-info {
        display: flex;
        align-items: center;

        .avatar {
          width: 52rpx;
          height: 52rpx;
          margin-right: var(--space-3);
          border-radius: var(--radius-full);
          border: 2rpx solid var(--color-border-light);
        }

        .username {
          font-size: var(--font-size-sm);
          color: var(--color-text-primary);
          font-weight: var(--font-weight-medium);
        }
      }

      .order-amount {
        display: flex;
        align-items: baseline;

        .amount-label {
          margin-right: var(--space-1);
          font-size: var(--font-size-xs);
          color: var(--color-text-tertiary);
        }

        .amount-value {
          font-size: var(--font-size-lg);
          font-weight: var(--font-weight-bold);
          color: var(--color-error);
        }
      }
    }

    .order-actions {
      display: flex;
      justify-content: flex-end;
      gap: var(--space-3);

      .btn {
        padding: var(--space-2) var(--space-4);
        border-radius: var(--radius-full);
        font-size: var(--font-size-sm);
        line-height: var(--line-height-normal);
        border: none;
        transition: all var(--duration-fast) var(--ease-out);

        &::after {
          border: none;
        }

        &:active {
          transform: scale(0.96);
        }

        &.btn-cancel {
          background: var(--color-bg-secondary);
          color: var(--color-text-secondary);
        }

        &.btn-primary {
          background: var(--color-primary-gradient);
          color: var(--color-text-primary);
          box-shadow: var(--shadow-primary);
        }
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

