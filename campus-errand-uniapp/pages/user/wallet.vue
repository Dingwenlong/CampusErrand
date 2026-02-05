<template>
  <view class="container">
    <!-- 余额卡片 -->
    <view class="balance-card">
      <view class="balance-header">
        <text class="balance-title">我的余额</text>
        <text class="balance-eye" @click="toggleEye">
          <text class="iconfont" :class="showBalance ? 'icon-eye' : 'icon-eye-close'"></text>
        </text>
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
        <view class="detail-item">
          <text class="detail-label">冻结金额</text>
          <text class="detail-value">¥{{ showBalance ? frozenAmount : '****' }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar">
      <view class="action-item" @click="goRecharge">
        <text class="iconfont icon-recharge action-icon"></text>
        <text class="action-text">充值</text>
      </view>
      <view class="action-item" @click="goWithdraw">
        <text class="iconfont icon-withdraw action-icon"></text>
        <text class="action-text">提现</text>
      </view>
      <view class="action-item" @click="goTransactions">
        <text class="iconfont icon-transaction action-icon"></text>
        <text class="action-text">明细</text>
      </view>
    </view>

    <!-- 支付密码设置 -->
    <view class="menu-list">
      <view class="menu-item" @click="goPayPassword">
        <text class="iconfont icon-password menu-icon"></text>
        <text class="menu-text">支付密码</text>
        <text class="menu-extra">{{ hasPayPassword ? '已设置' : '未设置' }}</text>
        <text class="iconfont icon-arrow-right"></text>
      </view>
    </view>

    <!-- 交易统计 -->
    <view class="stats-card">
      <view class="stats-title">交易统计</view>
      <view class="stats-content">
        <view class="stats-item">
          <text class="stats-label">累计收入</text>
          <text class="stats-value income">+¥{{ totalIncome }}</text>
        </view>
        <view class="stats-item">
          <text class="stats-label">累计支出</text>
          <text class="stats-value expense">-¥{{ totalExpense }}</text>
        </view>
      </view>
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
.container {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  color: #fff;

  .balance-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .balance-title {
      font-size: 28rpx;
      opacity: 0.9;
    }

    .balance-eye {
      .iconfont {
        font-size: 40rpx;
      }
    }
  }

  .balance-amount {
    margin-bottom: 40rpx;

    .balance-symbol {
      font-size: 40rpx;
      margin-right: 8rpx;
    }

    .balance-num {
      font-size: 72rpx;
      font-weight: bold;
    }
  }

  .balance-detail {
    display: flex;
    border-top: 1rpx solid rgba(255, 255, 255, 0.2);
    padding-top: 30rpx;

    .detail-item {
      flex: 1;
      display: flex;
      flex-direction: column;

      .detail-label {
        font-size: 24rpx;
        opacity: 0.8;
        margin-bottom: 8rpx;
      }

      .detail-value {
        font-size: 32rpx;
        font-weight: bold;
      }
    }
  }
}

.action-bar {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx 0;
  margin-bottom: 20rpx;

  .action-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;

    .action-icon {
      font-size: 48rpx;
      color: #667eea;
      margin-bottom: 12rpx;
    }

    .action-text {
      font-size: 28rpx;
      color: #333;
    }
  }
}

.menu-list {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;

  .menu-item {
    display: flex;
    align-items: center;
    padding: 30rpx;

    .menu-icon {
      font-size: 40rpx;
      color: #667eea;
      margin-right: 20rpx;
    }

    .menu-text {
      flex: 1;
      font-size: 30rpx;
      color: #333;
    }

    .menu-extra {
      font-size: 26rpx;
      color: #999;
      margin-right: 16rpx;
    }

    .icon-arrow-right {
      font-size: 28rpx;
      color: #ccc;
    }
  }
}

.stats-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;

  .stats-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
  }

  .stats-content {
    display: flex;

    .stats-item {
      flex: 1;
      display: flex;
      flex-direction: column;

      .stats-label {
        font-size: 26rpx;
        color: #999;
        margin-bottom: 12rpx;
      }

      .stats-value {
        font-size: 36rpx;
        font-weight: bold;

        &.income {
          color: #52c41a;
        }

        &.expense {
          color: #ff4d4f;
        }
      }
    }
  }
}
</style>
