<template>
  <view class="container">
    <!-- 顶部黄色区域 -->
    <view class="header-section">
      <!-- 用户信息 -->
      <view class="user-info">
        <view class="avatar">
          <text class="avatar-text">{{ userInfo.nickname ? userInfo.nickname.charAt(0) : '?' }}</text>
        </view>
        <view class="user-detail">
          <text class="nickname">{{ userInfo.nickname || '未登录' }}</text>
          <view class="credit-score">
            <text class="score-label">信用分</text>
            <text class="score-value">{{ userInfo.creditScore || 100 }}</text>
          </view>
        </view>
      </view>

      <!-- 钱包摘要 -->
      <view class="wallet-summary" @click="goWallet">
        <view class="wallet-item">
          <text class="wallet-label">余额</text>
          <text class="wallet-value">¥{{ wallet.balance }}</text>
        </view>
        <view class="wallet-divider"></view>
        <view class="wallet-item">
          <text class="wallet-label">冻结</text>
          <text class="wallet-value">¥{{ wallet.frozenAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="section-header">
        <view class="title-line"></view>
        <text class="section-title">我的服务</text>
      </view>
      <view class="menu-grid">
        <view class="menu-item" @click="goPublishedTasks">
          <view class="menu-icon-wrapper icon-bg-1">
            <text class="iconfont icon-publish menu-icon"></text>
          </view>
          <text class="menu-text">我发布的</text>
        </view>
        <view class="menu-item" @click="goAcceptedTasks">
          <view class="menu-icon-wrapper icon-bg-2">
            <text class="iconfont icon-accept menu-icon"></text>
          </view>
          <text class="menu-text">我接单的</text>
        </view>
        <view class="menu-item" @click="goWallet">
          <view class="menu-icon-wrapper icon-bg-3">
            <text class="iconfont icon-wallet menu-icon"></text>
          </view>
          <text class="menu-text">我的钱包</text>
        </view>
        <view class="menu-item" @click="goEvaluations">
          <view class="menu-icon-wrapper icon-bg-4">
            <text class="iconfont icon-evaluation menu-icon"></text>
          </view>
          <text class="menu-text">我的评价</text>
        </view>
      </view>
    </view>

    <!-- 设置菜单 -->
    <view class="menu-list">
      <view class="list-item" @click="goProfile">
        <view class="item-left">
          <view class="item-icon-wrapper icon-bg-5">
            <text class="iconfont icon-profile item-icon"></text>
          </view>
          <text class="item-text">个人资料</text>
        </view>
        <text class="iconfont icon-arrow-right arrow-icon"></text>
      </view>
      <view class="list-item" @click="goPayPassword">
        <view class="item-left">
          <view class="item-icon-wrapper icon-bg-6">
            <text class="iconfont icon-password item-icon"></text>
          </view>
          <text class="item-text">支付密码</text>
        </view>
        <view class="item-right">
          <text class="item-extra">{{ wallet.hasPayPassword ? '已设置' : '未设置' }}</text>
          <text class="iconfont icon-arrow-right arrow-icon"></text>
        </view>
      </view>
      <view class="list-item" @click="goSettings">
        <view class="item-left">
          <view class="item-icon-wrapper icon-bg-7">
            <text class="iconfont icon-setting item-icon"></text>
          </view>
          <text class="item-text">设置</text>
        </view>
        <text class="iconfont icon-arrow-right arrow-icon"></text>
      </view>
    </view>
  </view>
</template>

<script>
import userApi from '@/api/user.js'
import walletApi from '@/api/wallet.js'

export default {
  data() {
    return {
      userInfo: {
        nickname: '',
        avatar: '',
        creditScore: 100
      },
      wallet: {
        balance: '0.00',
        frozenAmount: '0.00',
        hasPayPassword: false
      }
    }
  },
  onShow() {
    this.loadUserInfo()
    this.loadWalletInfo()
  },
  methods: {
    async loadUserInfo() {
      try {
        const res = await userApi.getUserInfo()
        if (res.code === 200) {
          this.userInfo = res.data
        }
      } catch (e) {
        console.error('加载用户信息失败', e)
      }
    },
    async loadWalletInfo() {
      try {
        const res = await walletApi.getBalance()
        if (res.code === 200) {
          const data = res.data
          this.wallet.balance = parseFloat(data.balance).toFixed(2)
          this.wallet.frozenAmount = parseFloat(data.frozenAmount).toFixed(2)
          this.wallet.hasPayPassword = data.hasPayPassword === 1
        }
      } catch (e) {
        console.error('加载钱包信息失败', e)
      }
    },
    goPublishedTasks() {
      uni.switchTab({
        url: '/pages/order/list'
      })
    },
    goAcceptedTasks() {
      uni.switchTab({
        url: '/pages/order/list'
      })
    },
    goWallet() {
      uni.navigateTo({
        url: '/package-user/pages/user/wallet'
      })
    },
    goEvaluations() {
      uni.showToast({
        title: '评价功能开发中',
        icon: 'none'
      })
    },
    goProfile() {
      uni.navigateTo({
        url: '/package-user/pages/user/profile'
      })
    },
    goPayPassword() {
      if (this.wallet.hasPayPassword) {
        uni.showToast({
          title: '支付密码已设置',
          icon: 'none'
        })
        return
      }
      uni.navigateTo({
        url: '/package-user/pages/user/pay-password'
      })
    },
    goSettings() {
      uni.showToast({
        title: '设置功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #F5F5F5;
  padding-bottom: var(--space-6);
}

/* 顶部黄色区域 */
.header-section {
  background: linear-gradient(180deg, #FFC300 0%, #FFB300 100%);
  padding: var(--space-8) var(--space-6) var(--space-10);
  border-radius: 0 0 32rpx 32rpx;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-8);
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--space-5);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);

  .avatar-text {
    color: #333;
    font-size: 48rpx;
    font-weight: var(--font-weight-bold);
  }
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: #333;
  margin-bottom: var(--space-2);
}

.credit-score {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.3);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);

  .score-label {
    font-size: var(--font-size-xs);
    color: #333;
    margin-right: var(--space-2);
  }

  .score-value {
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-bold);
    color: #333;
  }
}

/* 钱包摘要 */
.wallet-summary {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  padding: var(--space-5);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.wallet-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;

  .wallet-label {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
    margin-bottom: var(--space-2);
  }

  .wallet-value {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: #FF4D4F;
  }
}

.wallet-divider {
  width: 2rpx;
  height: 60rpx;
  background-color: var(--color-divider);
}

/* 功能菜单区域 */
.menu-section {
  margin: -16rpx var(--space-6) var(--space-6);
  background-color: #fff;
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  position: relative;
  z-index: 1;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-5);
}

.title-line {
  width: 8rpx;
  height: 36rpx;
  background: linear-gradient(180deg, #FFC300 0%, #FFB300 100%);
  border-radius: 4rpx;
  margin-right: var(--space-3);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.menu-grid {
  display: flex;
  justify-content: space-around;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-3);

  &:active {
    opacity: 0.8;
  }
}

.menu-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-3);

  &.icon-bg-1 {
    background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  }

  &.icon-bg-2 {
    background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
  }

  &.icon-bg-3 {
    background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);
  }

  &.icon-bg-4 {
    background: linear-gradient(135deg, #FFA07A 0%, #FFB347 100%);
  }

  .menu-icon {
    font-size: 44rpx;
    color: #fff;
  }
}

.menu-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

/* 设置菜单 */
.menu-list {
  margin: 0 var(--space-6);
  background-color: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  border-bottom: 2rpx solid var(--color-divider);

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: var(--color-bg);
  }
}

.item-left {
  display: flex;
  align-items: center;
}

.item-icon-wrapper {
  width: 64rpx;
  height: 64rpx;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--space-4);

  &.icon-bg-5 {
    background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  }

  &.icon-bg-6 {
    background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
  }

  &.icon-bg-7 {
    background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);
  }

  .item-icon {
    font-size: 32rpx;
    color: #fff;
  }
}

.item-text {
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

.item-right {
  display: flex;
  align-items: center;
}

.item-extra {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  margin-right: var(--space-3);
}

.arrow-icon {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}
</style>
