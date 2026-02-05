<template>
  <view class="container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-info">
        <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
        <view class="user-detail">
          <text class="nickname">{{ userInfo.nickname || '未登录' }}</text>
          <view class="credit-score">
            <text class="score-label">信用分</text>
            <text class="score-value">{{ userInfo.creditScore || 100 }}</text>
          </view>
        </view>
      </view>
      <view class="wallet-summary" @click="goWallet">
        <view class="wallet-item">
          <text class="wallet-label">余额</text>
          <text class="wallet-value">¥{{ wallet.balance }}</text>
        </view>
        <view class="wallet-item">
          <text class="wallet-label">冻结</text>
          <text class="wallet-value">¥{{ wallet.frozenAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-title">我的服务</view>
      <view class="menu-grid">
        <view class="menu-item" @click="goPublishedTasks">
          <text class="iconfont icon-publish menu-icon"></text>
          <text class="menu-text">我发布的</text>
        </view>
        <view class="menu-item" @click="goAcceptedTasks">
          <text class="iconfont icon-accept menu-icon"></text>
          <text class="menu-text">我接单的</text>
        </view>
        <view class="menu-item" @click="goWallet">
          <text class="iconfont icon-wallet menu-icon"></text>
          <text class="menu-text">我的钱包</text>
        </view>
        <view class="menu-item" @click="goEvaluations">
          <text class="iconfont icon-evaluation menu-icon"></text>
          <text class="menu-text">我的评价</text>
        </view>
      </view>
    </view>

    <!-- 设置菜单 -->
    <view class="menu-list">
      <view class="menu-item" @click="goProfile">
        <text class="iconfont icon-profile menu-icon"></text>
        <text class="menu-text">个人资料</text>
        <text class="iconfont icon-arrow-right"></text>
      </view>
      <view class="menu-item" @click="goPayPassword">
        <text class="iconfont icon-password menu-icon"></text>
        <text class="menu-text">支付密码</text>
        <text class="menu-extra">{{ wallet.hasPayPassword ? '已设置' : '未设置' }}</text>
        <text class="iconfont icon-arrow-right"></text>
      </view>
      <view class="menu-item" @click="goSettings">
        <text class="iconfont icon-setting menu-icon"></text>
        <text class="menu-text">设置</text>
        <text class="iconfont icon-arrow-right"></text>
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
      uni.navigateTo({
        url: '/pages/order/list?type=published'
      })
    },
    goAcceptedTasks() {
      uni.navigateTo({
        url: '/pages/order/list?type=accepted'
      })
    },
    goWallet() {
      uni.navigateTo({
        url: '/pages/user/wallet'
      })
    },
    goEvaluations() {
      uni.navigateTo({
        url: '/pages/evaluation/list'
      })
    },
    goProfile() {
      uni.navigateTo({
        url: '/pages/user/profile'
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
        url: '/pages/user/pay-password'
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
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  color: #fff;

  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;

    .avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      border: 4rpx solid rgba(255, 255, 255, 0.3);
      margin-right: 20rpx;
    }

    .user-detail {
      display: flex;
      flex-direction: column;

      .nickname {
        font-size: 36rpx;
        font-weight: bold;
        margin-bottom: 10rpx;
      }

      .credit-score {
        display: flex;
        align-items: center;
        background: rgba(255, 255, 255, 0.2);
        padding: 6rpx 16rpx;
        border-radius: 20rpx;

        .score-label {
          font-size: 24rpx;
          margin-right: 8rpx;
        }

        .score-value {
          font-size: 28rpx;
          font-weight: bold;
        }
      }
    }
  }

  .wallet-summary {
    display: flex;
    border-top: 1rpx solid rgba(255, 255, 255, 0.2);
    padding-top: 20rpx;

    .wallet-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      .wallet-label {
        font-size: 24rpx;
        opacity: 0.8;
        margin-bottom: 8rpx;
      }

      .wallet-value {
        font-size: 32rpx;
        font-weight: bold;
      }
    }
  }
}

.menu-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .menu-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
  }

  .menu-grid {
    display: flex;
    flex-wrap: wrap;

    .menu-item {
      width: 25%;
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 30rpx;

      .menu-icon {
        font-size: 48rpx;
        color: #667eea;
        margin-bottom: 12rpx;
      }

      .menu-text {
        font-size: 26rpx;
        color: #666;
      }
    }
  }
}

.menu-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;

  .menu-item {
    display: flex;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

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
</style>
