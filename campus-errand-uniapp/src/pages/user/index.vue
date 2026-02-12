<template>
  <view class="container">
    <!-- 顶部黄色区域 -->
    <view class="header-section">
      <!-- 用户信息 -->
      <view class="user-info">
        <view class="avatar-wrapper" @click="goProfile">
          <view class="avatar">
            <text class="avatar-text">{{ userInfo.nickname ? userInfo.nickname.charAt(0) : '?' }}</text>
          </view>
          <view class="avatar-edit">
            <text class="edit-icon">📷</text>
          </view>
        </view>
        <view class="user-detail">
          <text class="nickname">{{ userInfo.nickname || '未登录' }}</text>
          <view class="credit-score">
            <text class="score-icon">⭐</text>
            <text class="score-label">信用分</text>
            <text class="score-value">{{ userInfo.creditScore || 100 }}</text>
          </view>
        </view>
      </view>

      <!-- 钱包摘要 -->
      <view class="wallet-summary card-hover" @click="goWallet">
        <view class="wallet-item">
          <text class="wallet-label">余额</text>
          <view class="wallet-value-wrapper">
            <text class="wallet-symbol">¥</text>
            <text class="wallet-value">{{ wallet.balance }}</text>
          </view>
        </view>
        <view class="wallet-divider"></view>
        <view class="wallet-item">
          <text class="wallet-label">冻结</text>
          <view class="wallet-value-wrapper frozen">
            <text class="wallet-symbol">¥</text>
            <text class="wallet-value">{{ wallet.frozenAmount }}</text>
          </view>
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
        <view class="menu-item pressable" v-for="(item, index) in menuItems" :key="index" @click="item.action">
          <view class="menu-icon-wrapper" :class="'icon-bg-' + (index + 1)">
            <text class="menu-icon">{{ item.icon }}</text>
          </view>
          <text class="menu-text">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <!-- 设置菜单 -->
    <view class="menu-list">
      <view 
        class="list-item pressable" 
        v-for="(item, index) in settingItems" 
        :key="index"
        @click="item.action"
      >
        <view class="item-left">
          <view class="item-icon-wrapper" :class="'icon-bg-' + (index + 5)">
            <text class="item-icon">{{ item.icon }}</text>
          </view>
          <text class="item-text">{{ item.name }}</text>
        </view>
        <view class="item-right">
          <text v-if="item.extra" class="item-extra">{{ item.extra }}</text>
          <text class="iconfont icon-arrow-right arrow-icon"></text>
        </view>
      </view>
    </view>

    <!-- 版本信息 -->
    <view class="version-info">
      <text>校园跑腿 v1.0.0</text>
    </view>

    <!-- 自定义底部导航 -->
    <custom-tabbar :current="3" />
  </view>
</template>

<script>
import userApi from '@/api/user.js'
import walletApi from '@/api/wallet.js'
import CustomTabbar from '@/components/custom-tabbar/index.vue'

export default {
  components: {
    CustomTabbar
  },
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
      },
      menuItems: [
        { name: '我发布的', icon: '📋', action: this.goPublishedTasks },
        { name: '我接单的', icon: '📝', action: this.goAcceptedTasks },
        { name: '我的钱包', icon: '💰', action: this.goWallet },
        { name: '我的评价', icon: '⭐', action: this.goEvaluations }
      ],
      settingItems: [
        { name: '个人资料', icon: '👤', action: this.goProfile },
        { name: '支付密码', icon: '🔐', action: this.goPayPassword, extra: '' },
        { name: '设置', icon: '⚙️', action: this.goSettings }
      ]
    }
  },
  onShow() {
    uni.$emit('tabBarChange', 3)
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
          this.settingItems[1].extra = this.wallet.hasPayPassword ? '已设置' : '未设置'
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
@use '@/static/styles/mixins.scss' as *;

.container {
  min-height: 100vh;
  background-color: var(--color-bg);
  padding-bottom: calc(var(--space-6) + 140rpx + env(safe-area-inset-bottom));
}

/* 顶部黄色区域 */
.header-section {
  background: var(--color-primary-gradient);
  padding: var(--space-8) var(--space-6) var(--space-12);
  border-radius: 0 0 var(--radius-3xl) var(--radius-3xl);
  box-shadow: var(--shadow-md);
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-8);
}

.avatar-wrapper {
  position: relative;
  margin-right: var(--space-5);
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  @include flex-center;
  box-shadow: var(--shadow-md);
  border: 4rpx solid rgba(255, 255, 255, 0.5);

  .avatar-text {
    color: var(--color-text-primary);
    font-size: 48rpx;
    font-weight: var(--font-weight-bold);
  }
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  background-color: var(--color-surface);
  border-radius: 50%;
  @include flex-center;
  box-shadow: var(--shadow-sm);
  
  .edit-icon {
    font-size: 20rpx;
  }
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-3);
}

.credit-score {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.3);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  backdrop-filter: blur(10rpx);

  .score-icon {
    font-size: var(--font-size-xs);
    margin-right: var(--space-1);
  }

  .score-label {
    font-size: var(--font-size-xs);
    color: var(--color-text-primary);
    margin-right: var(--space-2);
  }

  .score-value {
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
  }
}

/* 钱包摘要 */
.wallet-summary {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.98);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  box-shadow: var(--shadow-md);
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: translateY(-2rpx);
    box-shadow: var(--shadow-lg);
  }
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

  .wallet-value-wrapper {
    display: flex;
    align-items: baseline;
    
    &.frozen {
      color: var(--color-text-tertiary);
    }
  }
  
  .wallet-symbol {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-bold);
    margin-right: 2rpx;
  }

  .wallet-value {
    font-size: var(--font-size-2xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-error);
  }
}

.wallet-divider {
  width: 2rpx;
  height: 64rpx;
  background-color: var(--color-divider);
}

/* 功能菜单区域 */
.menu-section {
  margin: calc(-1 * var(--space-6)) var(--space-6) var(--space-6);
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
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
  background: var(--color-primary-gradient);
  border-radius: var(--radius-full);
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
  transition: all var(--duration-fast) var(--ease-out);

  &:active {
    transform: scale(0.95);
    opacity: 0.8;
  }
}

.menu-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: var(--radius-xl);
  @include flex-center;
  margin-bottom: var(--space-3);
  transition: all var(--duration-fast) var(--ease-out);

  &.icon-bg-1 {
    background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
    box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.35);
  }

  &.icon-bg-2 {
    background: linear-gradient(135deg, #7BC47F 0%, #9DD9A0 100%);
    box-shadow: 0 8rpx 24rpx rgba(123, 196, 127, 0.35);
  }

  &.icon-bg-3 {
    background: linear-gradient(135deg, #FFB347 0%, #FFC970 100%);
    box-shadow: 0 8rpx 24rpx rgba(255, 179, 71, 0.35);
  }

  &.icon-bg-4 {
    background: linear-gradient(135deg, #FFB088 0%, #FFC4A8 100%);
    box-shadow: 0 8rpx 24rpx rgba(255, 176, 136, 0.35);
  }

  .menu-icon {
    font-size: 40rpx;
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
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  border-bottom: 2rpx solid var(--color-divider);
  transition: all var(--duration-fast) var(--ease-out);

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: var(--color-surface-pressed);
  }
}

.item-left {
  display: flex;
  align-items: center;
}

.item-icon-wrapper {
  width: 64rpx;
  height: 64rpx;
  border-radius: var(--radius-lg);
  @include flex-center;
  margin-right: var(--space-4);

  &.icon-bg-5 {
    background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  }

  &.icon-bg-6 {
    background: linear-gradient(135deg, #7BC47F 0%, #9DD9A0 100%);
  }

  &.icon-bg-7 {
    background: linear-gradient(135deg, #FFB347 0%, #FFC970 100%);
  }

  .item-icon {
    font-size: 28rpx;
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

/* 版本信息 */
.version-info {
  text-align: center;
  padding: var(--space-8) 0;
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.pressable {
  transition: transform var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: scale(0.95);
  }
}

.card-hover {
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: translateY(-2rpx);
    box-shadow: var(--shadow-lg);
  }
}
</style>

