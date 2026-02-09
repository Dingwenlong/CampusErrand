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
    this.loadOrderList()
  },
  onShow() {
    // 每次显示页面时刷新
    this.loadOrderList()
  },
  methods: {
    switchRole(role) {
      if (this.currentRole === role) return
      this.currentRole = role
      this.current = 1
      this.orderList = []
      this.noMore = false
      this.loadOrderList()
    },
    
    selectStatus(status) {
      this.currentStatus = status
      this.current = 1
      this.orderList = []
      this.noMore = false
      this.loadOrderList()
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
      this.current = 1
      this.noMore = false
      this.loadOrderList()
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
                this.loadOrderList()
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
                this.loadOrderList()
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
                this.loadOrderList()
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
.container {
  background: #f5f5f5;
  min-height: 100vh;
}

.role-tabs {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  
  .role-tab {
    flex: 1;
    text-align: center;
    padding: 30rpx 0;
    position: relative;
    
    .tab-text {
      font-size: 30rpx;
      color: #666;
    }
    
    &.active {
      .tab-text {
        color: #667eea;
        font-weight: 500;
      }
    }
    
    .tab-line {
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

.status-filter {
  background: #fff;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
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

.order-list {
  height: calc(100vh - 200rpx);
  padding: 20rpx;
  
  .order-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    
    .order-header {
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
      
      .order-status {
        font-size: 26rpx;
        font-weight: 500;
        
        &.status-0 {
          color: #999;
        }
        
        &.status-1, &.status-2, &.status-3 {
          color: #667eea;
        }
        
        &.status-4 {
          color: #fa8c16;
        }
        
        &.status-5 {
          color: #52c41a;
        }
        
        &.status-6 {
          color: #999;
        }
      }
    }
    
    .order-title {
      font-size: 30rpx;
      color: #333;
      font-weight: 500;
      margin-bottom: 16rpx;
    }
    
    .order-address {
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
    
    .order-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 16rpx;
      border-top: 1rpx solid #f0f0f0;
      margin-bottom: 16rpx;
      
      .user-info {
        display: flex;
        align-items: center;
        
        .avatar {
          width: 48rpx;
          height: 48rpx;
          border-radius: 50%;
          margin-right: 12rpx;
        }
        
        .username {
          font-size: 26rpx;
          color: #333;
        }
      }
      
      .order-amount {
        display: flex;
        align-items: center;
        
        .amount-label {
          font-size: 24rpx;
          color: #999;
          margin-right: 8rpx;
        }
        
        .amount-value {
          font-size: 32rpx;
          font-weight: bold;
          color: #ff4d4f;
        }
      }
    }
    
    .order-actions {
      display: flex;
      justify-content: flex-end;
      gap: 16rpx;
      
      .btn {
        padding: 12rpx 24rpx;
        border-radius: 8rpx;
        font-size: 26rpx;
        line-height: 1.5;
        
        &::after {
          border: none;
        }
        
        &.btn-cancel {
          background: #f5f5f5;
          color: #666;
        }
        
        &.btn-primary {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
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
}
</style>
