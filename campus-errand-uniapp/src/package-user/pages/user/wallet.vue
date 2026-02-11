<template>
  <view class="container container-safe">
    <!-- 余额卡片 -->
    <view class="balance-card animate-fade-in-up">
      <view class="balance-header">
        <text class="balance-title">我的余额</text>
        <view class="balance-eye" @click="toggleEye">
          <text class="iconfont" :class="showBalance ? 'icon-eye' : 'icon-eye-close'"></text>
        </view>
      </view>
      <view class="balance-amount">
        <text class="balance-symbol">¥</text>
        <text class="balance-num">{{ showBalance ? balance : '****' }}</text>
      </view>
      <view class="balance-detail">
        <view class="detail-item">
          <text class="detail-label">可用余额</text>
          <text class="detail-value">¥{{ showBalance ? availableBalance : '****' }}</text>
        </view>
        <view class="detail-divider"></view>
        <view class="detail-item">
          <text class="detail-label">冻结金额</text>
          <text class="detail-value">¥{{ showBalance ? frozenAmount : '****' }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar animate-fade-in-up delay-100">
      <view class="action-item" @click="goRecharge">
        <view class="action-icon-wrapper">
          <text class="iconfont icon-recharge action-icon"></text>
        </view>
        <text class="action-text">充值</text>
      </view>
      <view class="action-item" @click="goWithdraw">
        <view class="action-icon-wrapper">
          <text class="iconfont icon-withdraw action-icon"></text>
        </view>
        <text class="action-text">提现</text>
      </view>
      <view class="action-item" @click="goTransactions">
        <view class="action-icon-wrapper">
          <text class="iconfont icon-transaction action-icon"></text>
        </view>
        <text class="action-text">明细</text>
      </view>
    </view>

    <!-- 支付密码设置 -->
    <view class="menu-list animate-fade-in-up delay-200">
      <view class="menu-item" @click="goPayPassword">
        <view class="menu-icon-wrapper">
          <text class="iconfont icon-password menu-icon"></text>
        </view>
        <text class="menu-text">支付密码</text>
        <text class="menu-extra" :class="{ 'is-set': hasPayPassword }">
          {{ hasPayPassword ? '已设置' : '未设置' }}
        </text>
        <text class="iconfont icon-arrow-right menu-arrow"></text>
      </view>
    </view>

    <!-- 交易统计 -->
    <view class="stats-card animate-fade-in-up delay-300">
      <view class="stats-header">
        <text class="stats-title">交易统计</text>
        <text class="stats-period">本月</text>
      </view>
      <view class="stats-content">
        <view class="stats-item">
          <view class="stats-icon-wrapper income">
            <text class="iconfont icon-income stats-icon"></text>
          </view>
          <view class="stats-info">
            <text class="stats-label">累计收入</text>
            <text class="stats-value income">+¥{{ totalIncome }}</text>
          </view>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <view class="stats-icon-wrapper expense">
            <text class="iconfont icon-expense stats-icon"></text>
          </view>
          <view class="stats-info">
            <text class="stats-label">累计支出</text>
            <text class="stats-value expense">-¥{{ totalExpense }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 提示信息 -->
    <view class="tips animate-fade-in-up delay-400">
      <text class="iconfont icon-info tips-icon"></text>
      <text class="tips-text">冻结金额将在任务完成后自动解冻</text>
    </view>
  </view>
</template>

<script>
import walletApi from '@/api/wallet.js'

export default {
  data() {
    return {
      showBalance: true,
      balance: '0.00',
      availableBalance: '0.00',
      frozenAmount: '0.00',
      totalIncome: '0.00',
      totalExpense: '0.00',
      hasPayPassword: false
    }
  },
  onShow() {
    this.loadWalletData()
  },
  methods: {
    toggleEye() {
      this.showBalance = !this.showBalance
    },
    async loadWalletData() {
      try {
        const res = await walletApi.getBalance()
        if (res.code === 200) {
          const data = res.data
          this.balance = this.formatMoney(data.balance)
          this.availableBalance = this.formatMoney(data.availableBalance)
          this.frozenAmount = this.formatMoney(data.frozenAmount)
          this.totalIncome = this.formatMoney(data.totalIncome)
          this.totalExpense = this.formatMoney(data.totalExpense)
          this.hasPayPassword = data.hasPayPassword === 1
        }
      } catch (e) {
        console.error('加载钱包数据失败', e)
      }
    },
    formatMoney(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2)
    },
    goRecharge() {
      uni.showToast({
        title: '充值功能开发中',
        icon: 'none'
      })
    },
    goWithdraw() {
      uni.showToast({
        title: '提现功能开发中',
        icon: 'none'
      })
    },
    goTransactions() {
      uni.navigateTo({
        url: '/pages/user/transactions'
      })
    },
    goPayPassword() {
      uni.navigateTo({
        url: '/pages/user/pay-password'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/static/styles/mixins.scss';

.container {
  padding: var(--space-6);
  background: var(--color-bg);
  min-height: 100vh;
}

// 余额卡片
.balance-card {
  @include gradient-primary;
  border-radius: var(--radius-xl);
  padding: var(--space-8);
  margin-bottom: var(--space-6);
  color: var(--color-text-inverse);
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;
  
  // 装饰性背景
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 300rpx;
    height: 300rpx;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: -10%;
    width: 200rpx;
    height: 200rpx;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 50%;
  }

  .balance-header {
    @include flex-between;
    margin-bottom: var(--space-6);
    position: relative;
    z-index: 1;

    .balance-title {
      font-size: var(--font-size-base);
      opacity: 0.9;
      font-weight: var(--font-weight-medium);
    }

    .balance-eye {
      width: 64rpx;
      height: 64rpx;
      @include flex-center;
      background: rgba(255, 255, 255, 0.2);
      border-radius: var(--radius-full);
      transition: all var(--duration-fast) var(--ease-out);
      
      &:active {
        background: rgba(255, 255, 255, 0.3);
        transform: scale(0.95);
      }

      .iconfont {
        font-size: var(--font-size-lg);
        color: var(--color-text-inverse);
      }
    }
  }

  .balance-amount {
    margin-bottom: var(--space-8);
    position: relative;
    z-index: 1;

    .balance-symbol {
      font-size: var(--font-size-xl);
      margin-right: var(--space-2);
      font-weight: var(--font-weight-medium);
    }

    .balance-num {
      font-size: var(--font-size-4xl);
      font-weight: var(--font-weight-bold);
      letter-spacing: -0.02em;
    }
  }

  .balance-detail {
    @include flex-between;
    border-top: 1rpx solid rgba(255, 255, 255, 0.2);
    padding-top: var(--space-6);
    position: relative;
    z-index: 1;

    .detail-item {
      flex: 1;
      @include flex-column;
      
      &:first-child {
        padding-right: var(--space-4);
      }
      
      &:last-child {
        padding-left: var(--space-4);
      }

      .detail-label {
        font-size: var(--font-size-sm);
        opacity: 0.8;
        margin-bottom: var(--space-2);
      }

      .detail-value {
        font-size: var(--font-size-lg);
        font-weight: var(--font-weight-semibold);
      }
    }
    
    .detail-divider {
      width: 1rpx;
      height: 60rpx;
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

// 操作按钮
.action-bar {
  @include card-base;
  @include flex-between;
  padding: var(--space-6) var(--space-4);
  margin-bottom: var(--space-6);

  .action-item {
    flex: 1;
    @include flex-column;
    align-items: center;
    padding: var(--space-4);
    border-radius: var(--radius-lg);
    transition: all var(--duration-fast) var(--ease-out);
    
    &:active {
      background: var(--color-bg-secondary);
      transform: scale(0.98);
    }

    .action-icon-wrapper {
      width: 96rpx;
      height: 96rpx;
      @include flex-center;
      background: rgba(102, 126, 234, 0.1);
      border-radius: var(--radius-lg);
      margin-bottom: var(--space-3);
      transition: all var(--duration-fast) var(--ease-out);
      
      &:active {
        transform: scale(0.95);
      }
    }

    .action-icon {
      font-size: var(--font-size-xl);
      color: var(--color-primary);
    }

    .action-text {
      font-size: var(--font-size-sm);
      color: var(--color-text-primary);
      font-weight: var(--font-weight-medium);
    }
  }
}

// 菜单列表
.menu-list {
  @include card-base;
  margin-bottom: var(--space-6);
  overflow: hidden;

  .menu-item {
    @include flex-vcenter;
    padding: var(--space-5) var(--space-6);
    transition: all var(--duration-fast) var(--ease-out);
    
    &:active {
      background: var(--color-bg-secondary);
    }

    .menu-icon-wrapper {
      width: 72rpx;
      height: 72rpx;
      @include flex-center;
      background: rgba(102, 126, 234, 0.1);
      border-radius: var(--radius-md);
      margin-right: var(--space-4);
    }

    .menu-icon {
      font-size: var(--font-size-lg);
      color: var(--color-primary);
    }

    .menu-text {
      flex: 1;
      font-size: var(--font-size-base);
      color: var(--color-text-primary);
      font-weight: var(--font-weight-medium);
    }

    .menu-extra {
      font-size: var(--font-size-sm);
      color: var(--color-text-tertiary);
      margin-right: var(--space-3);
      
      &.is-set {
        color: var(--color-success);
      }
    }

    .menu-arrow {
      font-size: var(--font-size-base);
      color: var(--color-text-tertiary);
    }
  }
}

// 统计卡片
.stats-card {
  @include card-base;
  padding: var(--space-6);
  margin-bottom: var(--space-6);

  .stats-header {
    @include flex-between;
    margin-bottom: var(--space-6);

    .stats-title {
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-bold);
      color: var(--color-text-primary);
    }
    
    .stats-period {
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
      background: var(--color-bg-secondary);
      padding: var(--space-1) var(--space-3);
      border-radius: var(--radius-full);
    }
  }

  .stats-content {
    @include flex-between;
    gap: var(--space-4);

    .stats-item {
      flex: 1;
      @include flex-vcenter;
      padding: var(--space-5);
      background: var(--color-bg-secondary);
      border-radius: var(--radius-lg);
      
      .stats-icon-wrapper {
        width: 80rpx;
        height: 80rpx;
        @include flex-center;
        border-radius: var(--radius-md);
        margin-right: var(--space-4);
        
        &.income {
          background: rgba(82, 196, 26, 0.1);
          
          .stats-icon {
            color: var(--color-success);
          }
        }
        
        &.expense {
          background: rgba(255, 77, 79, 0.1);
          
          .stats-icon {
            color: var(--color-error);
          }
        }
      }
      
      .stats-icon {
        font-size: var(--font-size-xl);
      }
      
      .stats-info {
        @include flex-column;
        
        .stats-label {
          font-size: var(--font-size-sm);
          color: var(--color-text-secondary);
          margin-bottom: var(--space-1);
        }

        .stats-value {
          font-size: var(--font-size-xl);
          font-weight: var(--font-weight-bold);

          &.income {
            color: var(--color-success);
          }

          &.expense {
            color: var(--color-error);
          }
        }
      }
    }
  }
}

// 提示信息
.tips {
  @include flex-vcenter;
  justify-content: center;
  padding: var(--space-4);
  
  .tips-icon {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
    margin-right: var(--space-2);
  }
  
  .tips-text {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
  }
}
</style>
