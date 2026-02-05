<template>
  <view class="container">
    <!-- 筛选标签 -->
    <view class="filter-bar">
      <view class="filter-item" :class="{ active: currentFilter === 0 }" @click="changeFilter(0)">
        <text>全部</text>
      </view>
      <view class="filter-item" :class="{ active: currentFilter === 1 }" @click="changeFilter(1)">
        <text>收入</text>
      </view>
      <view class="filter-item" :class="{ active: currentFilter === 2 }" @click="changeFilter(2)">
        <text>支出</text>
      </view>
    </view>

    <!-- 交易列表 -->
    <scroll-view scroll-y class="transaction-list" @scrolltolower="loadMore">
      <view v-if="transactions.length === 0" class="empty-state">
        <text class="iconfont icon-empty"></text>
        <text class="empty-text">暂无交易记录</text>
      </view>

      <view v-else class="transaction-item" v-for="(item, index) in transactions" :key="index">
        <view class="transaction-icon" :class="item.direction === 1 ? 'income' : 'expense'">
          <text class="iconfont" :class="item.direction === 1 ? 'icon-income' : 'icon-expense'"></text>
        </view>
        <view class="transaction-info">
          <view class="transaction-title">
            <text class="type-name">{{ item.transactionTypeName }}</text>
            <text class="status-badge" :class="'status-' + item.status">{{ item.statusName }}</text>
          </view>
          <text class="transaction-time">{{ item.createTime }}</text>
          <text v-if="item.remark" class="transaction-remark">{{ item.remark }}</text>
        </view>
        <view class="transaction-amount">
          <text :class="item.direction === 1 ? 'amount-income' : 'amount-expense'">
            {{ item.direction === 1 ? '+' : '-' }}¥{{ item.amount }}
          </text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="transactions.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else>上拉加载更多</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import walletApi from '@/api/wallet.js'

export default {
  data() {
    return {
      currentFilter: 0,
      transactions: [],
      current: 1,
      size: 10,
      loading: false,
      noMore: false
    }
  },
  onLoad() {
    this.loadTransactions()
  },
  methods: {
    changeFilter(filter) {
      if (this.currentFilter === filter) return
      this.currentFilter = filter
      this.current = 1
      this.transactions = []
      this.noMore = false
      this.loadTransactions()
    },
    async loadTransactions() {
      if (this.loading || this.noMore) return

      this.loading = true
      try {
        const res = await walletApi.getTransactions({
          direction: this.currentFilter,
          current: this.current,
          size: this.size
        })

        if (res.code === 200) {
          const data = res.data
          const list = data.list || []

          if (list.length < this.size) {
            this.noMore = true
          }

          this.transactions = this.current === 1 ? list : [...this.transactions, ...list]
        }
      } catch (e) {
        console.error('加载交易记录失败', e)
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
      this.loadTransactions()
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
}

.filter-bar {
  display: flex;
  background: #fff;
  padding: 20rpx 0;
  margin-bottom: 20rpx;

  .filter-item {
    flex: 1;
    text-align: center;
    font-size: 28rpx;
    color: #666;
    padding: 16rpx 0;
    position: relative;

    &.active {
      color: #667eea;
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        background: #667eea;
        border-radius: 2rpx;
      }
    }
  }
}

.transaction-list {
  height: calc(100vh - 120rpx);
  padding: 0 20rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;

  .iconfont {
    font-size: 120rpx;
    color: #ddd;
    margin-bottom: 20rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.transaction-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .transaction-icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;

    &.income {
      background: rgba(82, 196, 26, 0.1);

      .iconfont {
        color: #52c41a;
        font-size: 40rpx;
      }
    }

    &.expense {
      background: rgba(255, 77, 79, 0.1);

      .iconfont {
        color: #ff4d4f;
        font-size: 40rpx;
      }
    }
  }

  .transaction-info {
    flex: 1;
    display: flex;
    flex-direction: column;

    .transaction-title {
      display: flex;
      align-items: center;
      margin-bottom: 8rpx;

      .type-name {
        font-size: 30rpx;
        color: #333;
        font-weight: bold;
        margin-right: 12rpx;
      }

      .status-badge {
        font-size: 20rpx;
        padding: 4rpx 12rpx;
        border-radius: 8rpx;

        &.status-0 {
          background: #fff7e6;
          color: #fa8c16;
        }

        &.status-1 {
          background: #f6ffed;
          color: #52c41a;
        }

        &.status-2 {
          background: #fff1f0;
          color: #ff4d4f;
        }
      }
    }

    .transaction-time {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 4rpx;
    }

    .transaction-remark {
      font-size: 24rpx;
      color: #666;
    }
  }

  .transaction-amount {
    .amount-income {
      font-size: 36rpx;
      color: #52c41a;
      font-weight: bold;
    }

    .amount-expense {
      font-size: 36rpx;
      color: #ff4d4f;
      font-weight: bold;
    }
  }
}

.load-more {
  text-align: center;
  padding: 30rpx;
  font-size: 26rpx;
  color: #999;
}
</style>
