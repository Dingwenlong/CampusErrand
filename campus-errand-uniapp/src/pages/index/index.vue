<template>
  <view class="container">
    <!-- 顶部黄色区域 -->
    <view class="header-section">
      <!-- 定位栏 -->
      <view class="location-bar">
        <view class="location-left" @click="showLocationPicker">
          <view class="location-icon-wrapper">
            <text class="iconfont icon-location-fill location-icon"></text>
          </view>
          <text class="location-text">山东现代学院</text>
          <text class="iconfont icon-arrow-down location-arrow"></text>
        </view>
        <view class="location-right">
          <view class="icon-btn" @click="navigateTo('/pages/message/list')">
            <text class="iconfont icon-bell message-icon"></text>
            <view v-if="unreadCount > 0" class="badge">{{ unreadCount }}</view>
          </view>
        </view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-bar pressable" @click="navigateTo('/pages/task/list')">
        <view class="search-input">
          <text class="iconfont icon-search search-icon"></text>
          <text class="search-placeholder">搜索任务、跑腿服务</text>
        </view>
      </view>
    </view>

    <!-- 功能入口网格 -->
    <view class="feature-section">
      <view class="feature-grid">
        <view 
          class="feature-item pressable" 
          v-for="(item, index) in features" 
          :key="index" 
          @click="navigateTo(item.path)"
          :style="{ animationDelay: index * 0.05 + 's' }"
        >
          <view class="feature-icon-wrapper scale-in" :class="'icon-bg-' + item.type">
            <text class="feature-icon">{{ item.iconText }}</text>
          </view>
          <text class="feature-text">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <!-- 轮播图 -->
    <swiper 
      class="banner-swiper" 
      indicator-dots 
      autoplay 
      circular 
      indicator-color="rgba(255,255,255,0.5)" 
      indicator-active-color="#FFC300"
      @change="onBannerChange"
    >
      <swiper-item v-for="(item, index) in banners" :key="index">
        <view class="banner-item scale-in" :class="'banner-' + (index + 1)">
          <text class="banner-title">{{ item.title }}</text>
          <text class="banner-desc">{{ item.desc }}</text>
        </view>
      </swiper-item>
    </swiper>

    <!-- 推荐任务 -->
    <view class="task-section">
      <view class="section-header">
        <view class="section-title-wrapper">
          <view class="title-line"></view>
          <text class="section-title">推荐任务</text>
        </view>
        <view class="section-more pressable" @click="navigateTo('/pages/task/list')">
          <text>更多</text>
          <text class="iconfont icon-arrow-right more-arrow"></text>
        </view>
      </view>

      <!-- 任务列表 -->
      <view class="task-list">
        <view 
          class="task-card card-hover" 
          v-for="(task, index) in taskList" 
          :key="index" 
          @click="goTaskDetail(task)"
          :style="{ animationDelay: index * 0.1 + 's' }"
        >
          <view class="task-main">
            <view class="task-header">
              <view class="task-type-tag" :class="'type-' + task.type">{{ task.typeName }}</view>
              <view class="task-price">
                <text class="price-symbol">¥</text>
                <text class="price-value">{{ task.price }}</text>
              </view>
            </view>
            <view class="task-title">{{ task.title }}</view>
            <view class="task-info">
              <view class="info-item">
                <view class="info-icon-wrapper">
                  <text class="iconfont icon-location info-icon"></text>
                </view>
                <text class="info-text">{{ task.from }} → {{ task.to }}</text>
              </view>
              <view class="info-item">
                <view class="info-icon-wrapper time">
                  <text class="iconfont icon-time info-icon"></text>
                </view>
                <text class="info-text">{{ task.time }}</text>
              </view>
            </view>
          </view>
          <view class="task-divider"></view>
          <view class="task-footer">
            <view class="user-info">
              <view class="user-avatar" :class="'avatar-bg-' + task.type">
                <text class="avatar-text">{{ task.userName.charAt(0) }}</text>
              </view>
              <text class="user-name">{{ task.userName }}</text>
            </view>
            <button class="grab-btn pressable" @click.stop="grabTask(task)">立即接单</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="loading">
      <view class="loading-spinner"></view>
      <text>加载中...</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      unreadCount: 2,
      currentBanner: 0,
      loading: false,
      features: [
        { name: '取快递', type: 1, iconText: '📦', path: '/pages/task/list?type=1' },
        { name: '代买', type: 2, iconText: '🛒', path: '/pages/task/list?type=2' },
        { name: '送件', type: 3, iconText: '📄', path: '/pages/task/list?type=3' },
        { name: '外卖', type: 4, iconText: '🍱', path: '/pages/task/list?type=4' },
        { name: '其他', type: 5, iconText: '✨', path: '/pages/task/list?type=5' },
      ],
      banners: [
        { title: '校园跑腿', desc: '便捷生活，从这里开始' },
        { title: '安全可靠', desc: '实名认证，信用保障' },
        { title: '快速响应', desc: '附近跑腿员，即时接单' }
      ],
      taskList: [
        {
          id: 1,
          type: 1,
          typeName: '取快递',
          title: '帮忙取个快递，送到3号楼',
          price: '5.00',
          from: '菜鸟驿站',
          to: '3号楼301',
          time: '今天 18:00前',
          userName: '张同学'
        },
        {
          id: 2,
          type: 2,
          typeName: '代买',
          title: '帮忙买一份午餐',
          price: '8.00',
          from: '二食堂',
          to: '图书馆',
          time: '今天 12:00前',
          userName: '李同学'
        },
        {
          id: 3,
          type: 3,
          typeName: '送件',
          title: '送一份文件到教务处',
          price: '10.00',
          from: '行政楼',
          to: '教务处',
          time: '今天 17:00前',
          userName: '王同学'
        }
      ]
    }
  },
  methods: {
    navigateTo(path) {
      // 判断是否是 tabBar 页面
      const tabBarPages = ['/pages/index/index', '/pages/task/list', '/pages/order/list', '/pages/user/index']
      const isTabBar = tabBarPages.some(tabPath => path === tabPath || path.startsWith(tabPath + '?'))

      if (isTabBar) {
        uni.switchTab({
          url: path.split('?')[0]
        })
      } else {
        uni.navigateTo({
          url: path
        })
      }
    },
    goTaskDetail(task) {
      uni.navigateTo({
        url: `/pages/task/detail?id=${task.id}`
      })
    },
    grabTask(task) {
      // 添加触觉反馈
      // #ifdef MP-WEIXIN
      if (uni.vibrateShort) {
        uni.vibrateShort({ type: 'light' })
      }
      // #endif
      
      uni.showModal({
        title: '确认接单',
        content: `确定要接这个${task.typeName}任务吗？`,
        confirmColor: '#FFC300',
        success: (res) => {
          if (res.confirm) {
            uni.showToast({
              title: '接单成功',
              icon: 'success',
              duration: 1500
            })
          }
        }
      })
    },
    onBannerChange(e) {
      this.currentBanner = e.detail.current
    },
    showLocationPicker() {
      uni.showToast({
        title: '位置选择功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
// 引入 mixins
@import '@/static/styles/mixins.scss';

.container {
  min-height: 100vh;
  background-color: var(--color-bg);
  padding-bottom: var(--space-8);
}

/* 顶部黄色区域 */
.header-section {
  background: var(--color-primary-gradient);
  padding: var(--space-4) var(--space-6) var(--space-8);
  border-radius: 0 0 var(--radius-3xl) var(--radius-3xl);
  box-shadow: var(--shadow-md);
}

/* 定位栏 */
.location-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.location-left {
  display: flex;
  align-items: center;
  padding: var(--space-2) var(--space-3);
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-full);
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    background-color: rgba(255, 255, 255, 0.3);
    transform: scale(0.98);
  }
}

.location-icon-wrapper {
  @include flex-center;
  width: 48rpx;
  height: 48rpx;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-full);
  margin-right: var(--space-2);
}

.location-icon {
  font-size: 28rpx;
  color: var(--color-text-primary);
}

.location-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.location-arrow {
  font-size: 20rpx;
  color: var(--color-text-primary);
  margin-left: var(--space-1);
}

.location-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.icon-btn {
  position: relative;
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-full);
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    background-color: rgba(255, 255, 255, 0.3);
    transform: scale(0.95);
  }
}

.message-icon {
  font-size: 36rpx;
  color: var(--color-text-primary);
}

.badge {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background-color: var(--color-error);
  color: var(--color-white);
  font-size: var(--font-size-2xs);
  font-weight: var(--font-weight-bold);
  border-radius: var(--radius-full);
  @include flex-center;
}

/* 搜索栏 */
.search-bar {
  background-color: var(--color-white);
  border-radius: var(--radius-full);
  padding: var(--space-3) var(--space-5);
  box-shadow: var(--shadow-sm);
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    box-shadow: var(--shadow-md);
  }
}

.search-input {
  display: flex;
  align-items: center;
}

.search-icon {
  font-size: 32rpx;
  color: var(--color-text-tertiary);
  margin-right: var(--space-3);
}

.search-placeholder {
  font-size: var(--font-size-base);
  color: var(--color-text-placeholder);
}

/* 功能入口区域 */
.feature-section {
  margin: var(--space-4) var(--space-6);
  position: relative;
  z-index: 1;
}

.feature-grid {
  display: flex;
  justify-content: space-around;
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-6) var(--space-4);
  box-shadow: var(--shadow-sm);
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-2);
  animation: slideUp var(--duration-normal) var(--ease-out) both;
}

.feature-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: var(--radius-xl);
  @include flex-center;
  margin-bottom: var(--space-3);
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: scale(0.95);
  }

  &.icon-bg-1 {
    background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
    box-shadow: 0 8rpx 24rpx rgba(255, 107, 107, 0.3);
  }

  &.icon-bg-2 {
    background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
    box-shadow: 0 8rpx 24rpx rgba(78, 205, 196, 0.3);
  }

  &.icon-bg-3 {
    background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
  }

  &.icon-bg-4 {
    background: linear-gradient(135deg, #FFA07A 0%, #FFB347 100%);
    box-shadow: 0 8rpx 24rpx rgba(255, 160, 122, 0.3);
  }

  &.icon-bg-5 {
    background: linear-gradient(135deg, #45B7D1 0%, #74C7E3 100%);
    box-shadow: 0 8rpx 24rpx rgba(69, 183, 209, 0.3);
  }
}

.feature-icon {
  font-size: 48rpx;
}

.feature-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

/* 轮播图 */
.banner-swiper {
  height: 280rpx;
  margin: 0 var(--space-6) var(--space-6);
  border-radius: var(--radius-xl);
  overflow: hidden;

  .banner-item {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: var(--color-white);
    animation: scaleIn var(--duration-normal) var(--ease-spring);

    &.banner-1 {
      background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
    }

    &.banner-2 {
      background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
    }

    &.banner-3 {
      background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);
    }

    .banner-title {
      font-size: var(--font-size-3xl);
      font-weight: var(--font-weight-bold);
      margin-bottom: var(--space-2);
      text-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.1);
    }

    .banner-desc {
      font-size: var(--font-size-base);
      opacity: 0.9;
    }
  }
}

/* 任务区域 */
.task-section {
  padding: 0 var(--space-6);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
}

.section-title-wrapper {
  display: flex;
  align-items: center;
}

.title-line {
  width: 8rpx;
  height: 40rpx;
  background: var(--color-primary-gradient);
  border-radius: var(--radius-full);
  margin-right: var(--space-3);
}

.section-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.section-more {
  display: flex;
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    color: var(--color-primary-dark);
  }
}

.more-arrow {
  font-size: 24rpx;
  margin-left: var(--space-1);
}

/* 任务列表 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.task-card {
  background-color: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  animation: slideUp var(--duration-normal) var(--ease-out) both;
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: translateY(-2rpx);
    box-shadow: var(--shadow-md);
  }
}

.task-main {
  padding: var(--space-5);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
}

.task-type-tag {
  padding: var(--space-1) var(--space-3);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  border-radius: var(--radius-sm);
  
  &.type-1 {
    background-color: var(--color-task-express-soft);
    color: var(--color-task-express);
  }
  
  &.type-2 {
    background-color: var(--color-task-shopping-soft);
    color: var(--color-task-shopping);
  }
  
  &.type-3 {
    background-color: var(--color-task-delivery-soft);
    color: var(--color-task-delivery);
  }
  
  &.type-4, &.type-5 {
    background-color: var(--color-task-other-soft);
    color: var(--color-task-other);
  }
}

.task-price {
  display: flex;
  align-items: baseline;
  color: var(--color-error);
  
  .price-symbol {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-bold);
  }
  
  .price-value {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
  }
}

.task-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-4);
  line-height: var(--line-height-snug);
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.info-item {
  display: flex;
  align-items: center;
}

.info-icon-wrapper {
  @include flex-center;
  width: 48rpx;
  height: 48rpx;
  background-color: var(--color-primary-soft);
  border-radius: var(--radius-md);
  margin-right: var(--space-2);
  
  &.time {
    background-color: var(--color-info-soft);
  }
}

.info-icon {
  font-size: 24rpx;
  color: var(--color-primary-dark);
  
  .time & {
    color: var(--color-info);
  }
}

.info-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.task-divider {
  height: 2rpx;
  background-color: var(--color-divider);
  margin: 0 var(--space-5);
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: var(--radius-full);
  @include flex-center;
  margin-right: var(--space-3);
  
  &.avatar-bg-1 {
    background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  }
  
  &.avatar-bg-2 {
    background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);
  }
  
  &.avatar-bg-3 {
    background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);
  }
  
  &.avatar-bg-4, &.avatar-bg-5 {
    background: linear-gradient(135deg, #45B7D1 0%, #74C7E3 100%);
  }
}

.avatar-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
}

.user-name {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.grab-btn {
  padding: var(--space-2) var(--space-5);
  background: var(--color-primary-gradient);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-primary);
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    background: var(--color-primary-dark);
    transform: scale(0.95);
  }
}

/* 加载更多 */
.load-more {
  @include flex-center;
  padding: var(--space-6);
  gap: var(--space-3);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
}

.loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 4rpx solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 动画 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.scale-in {
  animation: scaleIn var(--duration-normal) var(--ease-spring);
}

.pressable {
  transition: transform var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: scale(0.96);
  }
}

.card-hover {
  transition: all var(--duration-fast) var(--ease-out);
  
  &:active {
    transform: translateY(-2rpx);
    box-shadow: var(--shadow-md);
  }
}
</style>
