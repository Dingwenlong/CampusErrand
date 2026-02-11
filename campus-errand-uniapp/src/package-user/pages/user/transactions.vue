<template>
  <view class="container container-safe">
    <!-- 筛选标签 -->
    <view class="filter-bar animate-fade-in-down">
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
        <text>收入</text>
      </view>
      <view 
        class="filter-item" 
        :class="{ active: currentFilter === 2 }" 
        @click="changeFilter(2)"
      >
        <text>支出</text>
      </view>
    </view>

    <!-- 交易列表 -->
    <scroll-view 
      scroll-y 
      class="transaction-scroll" 
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view v-if="transactions.length === 0 && !loading" class="empty-state">
        <view class="empty-icon-wrapper">
          <text class="iconfont icon-empty empty-icon"></text>
        </view>
        <text class="empty-title">暂无交易记录</text>
        <text class="empty-desc">您的交易记录将显示在这里</text>
      </view>

      <!-- 交易列表 -->
      <view v-else class="transaction-list">
        <view 
          class="transaction-item" 
          v-for="(item, index) in transactions" 
          :key="index"
          :style="{ animationDelay: index * 50 + 'ms' }"
          @click="viewDetail(item)"
        >
          <view class="transaction-icon" :class="item.direction === 1 ? 'income' : 'expense'">
            <text class="iconfont" :class="getIconClass(item.transactionType)"></text>
          </view>
          <view class="transaction-info">
            <view class="transaction-header">
              <text class="transaction-type">{{ item.transactionTypeName }}</text>
              <view class="status-badge" :class="'status-' + item.status">
                {{ item.statusName }}
              </view>
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
      </view>

      <!-- 加载更多 -->
      <view v-if="transactions.length > 0" class="load-more">
        <view v-if="loading" class="loading-indicator">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        <text v-else-if="noMore" class="no-more">没有更多了</text>
        <text v-else>上拉加载更多</text>
      </view>
    </scroll-view>

    <!-- 统计摘要 -->
    <view class="summary-bar" v-if="transactions.length > 0">
      <view class="summary-item">
        <text class="summary-label">本页收入</text>
        <text class="summary-value income">+¥{{ pageIncome }}</text>
      </view>
      <view class="summary-divider"></view>
      <view class="summary-item">
        <text class="summary-label">本页支出</text>
        <text class="summary-value expense">-¥{{ pageExpense }}</text>
      </view>
    </view>
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
      noMore: false,
      refreshing: false,
      pageIncome: '0.00',
      pageExpense: '0.00'
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

          if (this.current === 1) {
            this.transactions = list
          } else {
            this.transactions = [...this.transactions, ...list]
          }
          
          this.calculateSummary()
        }
      } catch (e) {
        console.error('加载交易记录失败', e)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    loadMore() {
      if (this.noMore || this.loading) return
      this.current++
      this.loadTransactions()
    },
    onRefresh() {
      this.refreshing = true
      this.current = 1
      this.noMore = false
      this.loadTransactions()
    },
    calculateSummary() {
      let income = 0
      let expense = 0
      this.transactions.forEach(item => {
        if (item.direction === 1) {
          income += parseFloat(item.amount)
        } else {
          expense += parseFloat(item.amount)
        }
      })
      this.pageIncome = income.toFixed(2)
      this.pageExpense = expense.toFixed(2)
    },
    getIconClass(type) {
      const iconMap = {
        1: 'icon-recharge',
        2: 'icon-withdraw',
        3: 'icon-task',
        4: 'icon-refund',
        5: 'icon-transfer'
      }
      return iconMap[type] || 'icon-transaction'
    },
    viewDetail(item) {
      // 查看交易详情
      console.log('查看详情:', item)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/static/styles/mixins.scss';

.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--color-bg);
}

// 筛选标签
.filter-bar {
  @include flex-between;
  background: var(--color-surface);
  padding: var(--space-4) var(--space-6);
  margin-bottom: var(--space-4);
  box-shadow: var(--shadow-sm);

  .filter-item {
    flex: 1;
    text-align: center;
    font-size: var(--font-size-base);
    color: var(--color-text-secondary);
    padding: var(--space-3) 0;
    margin: 0 var(--space-2);
    border-radius: var(--radius-full);
    transition: all var(--duration-fast) var(--ease-out);
    position: relative;

    &:active {
      background: var(--color-bg-secondary);
    }

    &.active {
      color: var(--color-primary);
      font-weight: var(--font-weight-semibold);
      background: rgba(102, 126, 234, 0.1);
    }
  }
}

// 滚动区域
.transaction-scroll {
  flex: 1;
  padding: 0 var(--space-6);
}

// 空状态
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

// 交易列表
.transaction-list {
  padding-bottom: var(--space-4);
}

.transaction-item {
  @include flex-vcenter;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  margin-bottom: var(--space-4);
  box-shadow: var(--shadow-sm);
  transition: all var(--duration-fast) var(--ease-out);
  animation: fadeInUp var(--duration-normal) var(--ease-out) forwards;
  opacity: 0;
  transform: translateY(20rpx);

  &:active {
    box-shadow: var(--shadow-md);
    transform: translateY(-2rpx);
  }

  .transaction-icon {
    width: 88rpx;
    height: 88rpx;
    @include flex-center;
    border-radius: var(--radius-lg);
    margin-right: var(--space-5);
    flex-shrink: 0;

    &.income {
      background: rgba(82, 196, 26, 0.1);

      .iconfont {
        color: var(--color-success);
      }
    }

    &.expense {
      background: rgba(255, 77, 79, 0.1);

      .iconfont {
        color: var(--color-error);
      }
    }

    .iconfont {
      font-size: var(--font-size-xl);
    }
  }

  .transaction-info {
    flex: 1;
    @include flex-column;
    min-width: 0;

    .transaction-header {
      @include flex-vcenter;
      margin-bottom: var(--space-2);

      .transaction-type {
        font-size: var(--font-size-base);
        font-weight: var(--font-weight-medium);
        color: var(--color-text-primary);
        margin-right: var(--space-3);
      }

      .status-badge {
        font-size: var(--font-size-xs);
        padding: var(--space-1) var(--space-2);
        border-radius: var(--radius-sm);
        font-weight: var(--font-weight-medium);

        &.status-0 {
          background: rgba(250, 173, 20, 0.1);
          color: var(--color-warning);
        }

        &.status-1 {
          background: rgba(82, 196, 26, 0.1);
          color: var(--color-success);
        }

        &.status-2 {
          background: rgba(255, 77, 79, 0.1);
          color: var(--color-error);
        }
      }
    }

    .transaction-time {
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
      margin-bottom: var(--space-1);
    }

    .transaction-remark {
      font-size: var(--font-size-sm);
      color: var(--color-text-tertiary);
      @include text-ellipsis;
    }
  }

  .transaction-amount {
    margin-left: var(--space-4);
    flex-shrink: 0;

    .amount-income {
      font-size: var(--font-size-xl);
      font-weight: var(--font-weight-bold);
      color: var(--color-success);
    }

    .amount-expense {
      font-size: var(--font-size-xl);
      font-weight: var(--font-weight-bold);
      color: var(--color-error);
    }
  }
}

// 加载更多
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

// 统计摘要
.summary-bar {
  @include flex-between;
  background: var(--color-surface);
  padding: var(--space-5) var(--space-6);
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  border-top: 1rpx solid var(--color-border);

  .summary-item {
    flex: 1;
    @include flex-column;
    align-items: center;

    .summary-label {
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
      margin-bottom: var(--space-1);
    }

    .summary-value {
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-bold);

      &.income {
        color: var(--color-success);
      }

      &.expense {
        color: var(--color-error);
      }
    }
  }
  
  .summary-divider {
    width: 1rpx;
    height: 60rpx;
    background: var(--color-border);
  }
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
