<template>
  <view class="container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-info">
        <image class="user-avatar" src="/static/avatar/default.png" mode="aspectFill"></image>
        <view class="user-detail">
          <text class="user-name">点击登录</text>
          <text class="user-school">未认证</text>
        </view>
        <text class="iconfont icon-settings" @click="goProfile"></text>
      </view>
      <view class="user-stats">
        <view class="stat-item">
          <text class="stat-num">0</text>
          <text class="stat-label">信用分</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">0</text>
          <text class="stat-label">完成订单</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">0</text>
          <text class="stat-label">发布任务</text>
        </view>
      </view>
    </view>

    <!-- 我的钱包 -->
    <view class="wallet-card" @click="goWallet">
      <view class="wallet-header">
        <text class="wallet-title">我的钱包</text>
        <text class="wallet-more">查看详情 ></text>
      </view>
      <view class="wallet-balance">
        <text class="balance-label">余额</text>
        <text class="balance-num">¥0.00</text>
      </view>
      <view class="wallet-actions">
        <view class="action-item">
          <text class="iconfont icon-recharge"></text>
          <text>充值</text>
        </view>
        <view class="action-item">
          <text class="iconfont icon-withdraw"></text>
          <text>提现</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-list">
      <view class="menu-group">
        <view class="menu-item" @click="navigateTo('/pages/order/list')">
          <text class="iconfont icon-order menu-icon"></text>
          <text class="menu-text">我的订单</text>
          <text class="iconfont icon-arrow-right"></text>
        </view>
        <view class="menu-item" @click="navigateTo('/pages/task/publish')">
          <text class="iconfont icon-publish menu-icon"></text>
          <text class="menu-text">发布任务</text>
          <text class="iconfont icon-arrow-right"></text>
        </view>
        <view class="menu-item" @click="navigateTo('/pages/message/list')">
          <text class="iconfont icon-message menu-icon"></text>
          <text class="menu-text">消息通知</text>
          <text class="badge" v-if="unreadCount > 0">{{ unreadCount }}</text>
          <text class="iconfont icon-arrow-right" v-else></text>
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item">
          <text class="iconfont icon-verify menu-icon"></text>
          <text class="menu-text">实名认证</text>
          <text class="menu-extra">未认证</text>
          <text class="iconfont icon-arrow-right"></text>
        </view>
        <view class="menu-item">
          <text class="iconfont icon-address menu-icon"></text>
          <text class="menu-text">地址管理</text>
          <text class="iconfont icon-arrow-right"></text>
        </view>
        <view class="menu-item">
          <text class="iconfont icon-service menu-icon"></text>
          <text class="menu-text">联系客服</text>
          <text class="iconfont icon-arrow-right"></text>
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item">
          <text class="iconfont icon-about menu-icon"></text>
          <text class="menu-text">关于我们</text>
          <text class="iconfont icon-arrow-right"></text>
        </view>
        <view class="menu-item">
          <text class="iconfont icon-settings menu-icon"></text>
          <text class="menu-text">设置</text>
          <text class="iconfont icon-arrow-right"></text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      unreadCount: 3
    }
  },
  methods: {
    navigateTo(path) {
      uni.navigateTo({
        url: path
      })
    },
    goWallet() {
      uni.navigateTo({
        url: '/pages/user/wallet'
      })
    },
    goProfile() {
      uni.navigateTo({
        url: '/pages/user/profile'
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
    margin-bottom: 40rpx;

    .user-avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      border: 4rpx solid rgba(255, 255, 255, 0.3);
      margin-right: 24rpx;
    }

    .user-detail {
      flex: 1;

      .user-name {
        display: block;
        font-size: 36rpx;
        font-weight: bold;
        margin-bottom: 8rpx;
      }

      .user-school {
        display: block;
        font-size: 26rpx;
        opacity: 0.9;
      }
    }

    .icon-settings {
      font-size: 40rpx;
      opacity: 0.9;
    }
  }

  .user-stats {
    display: flex;
    justify-content: space-around;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-num {
        font-size: 40rpx;
        font-weight: bold;
        margin-bottom: 8rpx;
      }

      .stat-label {
        font-size: 24rpx;
        opacity: 0.9;
      }
    }
  }
}

.wallet-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .wallet-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .wallet-title {
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
    }

    .wallet-more {
      font-size: 24rpx;
      color: #999;
    }
  }

  .wallet-balance {
    display: flex;
    flex-direction: column;
    margin-bottom: 30rpx;

    .balance-label {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 8rpx;
    }

    .balance-num {
      font-size: 48rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .wallet-actions {
    display: flex;
    border-top: 1rpx solid #f0f0f0;
    padding-top: 20rpx;

    .action-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20rpx 0;

      .iconfont {
        font-size: 48rpx;
        color: #667eea;
        margin-bottom: 8rpx;
      }

      text:last-child {
        font-size: 26rpx;
        color: #666;
      }
    }
  }
}

.menu-list {
  .menu-group {
    background: #fff;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
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

      .badge {
        background: #ff4d4f;
        color: #fff;
        font-size: 22rpx;
        padding: 4rpx 12rpx;
        border-radius: 20rpx;
        margin-right: 16rpx;
      }

      .icon-arrow-right {
        font-size: 28rpx;
        color: #ccc;
      }
    }
  }
}
</style>
