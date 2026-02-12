<template>
  <view class="custom-tabbar">
    <view class="tabbar-container">
      <view 
        class="tabbar-item" 
        v-for="(item, index) in tabList" 
        :key="index"
        :class="{ active: currentIndex === index }"
        @click="switchTab(index)"
      >
        <view class="tabbar-item-inner">
          <text class="tabbar-icon">{{ item.icon }}</text>
          <text class="tabbar-text">{{ item.text }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'CustomTabBar',
  props: {
    current: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      currentIndex: 0,
      tabList: [
        { 
          pagePath: '/pages/index/index',
          text: '首页',
          icon: '🏠'
        },
        { 
          pagePath: '/pages/task/list',
          text: '任务',
          icon: '📋'
        },
        { 
          pagePath: '/pages/order/list',
          text: '订单',
          icon: '📦'
        },
        { 
          pagePath: '/pages/user/index',
          text: '我的',
          icon: '👤'
        }
      ]
    }
  },
  created() {
    this.currentIndex = this.current
    uni.$on('tabBarChange', this.onTabBarChange)
  },
  destroyed() {
    uni.$off('tabBarChange', this.onTabBarChange)
  },
  methods: {
    onTabBarChange(index) {
      this.currentIndex = index
    },
    switchTab(index) {
      if (this.currentIndex === index) return
      
      this.currentIndex = index
      const path = this.tabList[index].pagePath
      
      uni.$emit('tabBarChange', index)
      
      uni.switchTab({
        url: path,
        fail: (err) => {
          console.error('switchTab failed:', err)
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  padding: 16rpx 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: transparent;
}

.tabbar-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 64rpx;
  padding: 12rpx 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(74, 55, 40, 0.12);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
}

.tabbar-item {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12rpx 16rpx;
  border-radius: 48rpx;
  transition: all 0.3s ease;
  
  &.active {
    .tabbar-item-inner {
      background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
      box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.35);
      
      .tabbar-icon,
      .tabbar-text {
        color: #fff;
      }
    }
  }
}

.tabbar-item-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rpx 24rpx;
  border-radius: 48rpx;
  transition: all 0.3s ease;
}

.tabbar-icon {
  font-size: 40rpx;
  line-height: 1;
  margin-bottom: 4rpx;
  transition: all 0.3s ease;
}

.tabbar-text {
  font-size: 22rpx;
  font-weight: 500;
  color: #7A6B5E;
  transition: all 0.3s ease;
}
</style>
