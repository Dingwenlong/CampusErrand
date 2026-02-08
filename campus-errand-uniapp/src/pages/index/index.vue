<template>
  <view class="container">
    <!-- 顶部黄色区域 -->
    <view class="header-section">
      <!-- 定位栏 -->
      <view class="location-bar">
        <view class="location-left">
          <text class="iconfont icon-location-fill location-icon"></text>
          <text class="location-text">山东现代学院</text>
          <text class="iconfont icon-arrow-down location-arrow"></text>
        </view>
        <view class="location-right">
          <text class="iconfont icon-bell message-icon"></text>
          <text class="iconfont icon-comment message-icon"></text>
        </view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-bar" @click="navigateTo('/pages/task/list')">
        <view class="search-input">
          <text class="iconfont icon-search search-icon"></text>
          <text class="search-placeholder">搜索任务、跑腿服务</text>
        </view>
      </view>
    </view>

    <!-- 功能入口网格 -->
    <view class="feature-section">
      <view class="feature-grid">
        <view class="feature-item" v-for="(item, index) in features" :key="index" @click="navigateTo(item.path)">
          <view class="feature-icon-wrapper" :class="'icon-bg-' + item.type">
            <text class="feature-icon">{{ item.iconText }}</text>
          </view>
          <text class="feature-text">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <!-- 轮播图 -->
    <swiper class="banner-swiper" indicator-dots autoplay circular indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#FFC300">
      <swiper-item v-for="(item, index) in banners" :key="index">
        <view class="banner-item" :class="'banner-' + (index + 1)">
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
        <text class="section-more" @click="navigateTo('/pages/task/list')">
          更多 <text class="iconfont icon-arrow-right more-arrow"></text>
        </text>
      </view>

      <!-- 任务列表 -->
      <view class="task-list">
        <view class="task-card" v-for="(task, index) in taskList" :key="index" @click="goTaskDetail(task)">
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
                <text class="iconfont icon-location info-icon"></text>
                <text class="info-text">{{ task.from }} → {{ task.to }}</text>
              </view>
              <view class="info-item">
                <text class="iconfont icon-time info-icon"></text>
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
            <button class="grab-btn" @click.stop="grabTask(task)">立即接单</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
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
      uni.showModal({
        title: '确认接单',
        content: `确定要接这个${task.typeName}任务吗？`,
        success: (res) => {
          if (res.confirm) {
            uni.showToast({
              title: '接单成功',
              icon: 'success'
            })
          }
        }
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
  padding: var(--space-4) var(--space-6) var(--space-8);
  border-radius: 0 0 32rpx 32rpx;
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
}

.location-icon {
  font-size: 32rpx;
  color: #333;
  margin-right: var(--space-2);
}

.location-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: #333;
}

.location-arrow {
  font-size: 24rpx;
  color: #333;
  margin-left: var(--space-1);
}

.location-right {
  display: flex;
  align-items: center;
  gap: var(--space-5);
}

.message-icon {
  font-size: 40rpx;
  color: #333;
}

/* 搜索栏 */
.search-bar {
  background-color: #fff;
  border-radius: var(--radius-full);
  padding: var(--space-3) var(--space-5);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.search-input {
  display: flex;
  align-items: center;
}

.search-icon {
  font-size: 32rpx;
  color: #999;
  margin-right: var(--space-3);
}

.search-placeholder {
  font-size: var(--font-size-base);
  color: #999;
}

/* 功能入口区域 */
.feature-section {
  margin: var(--space-4) var(--space-6) var(--space-4);
  position: relative;
  z-index: 1;
}

.feature-grid {
  display: flex;
  justify-content: space-around;
  background-color: #fff;
  border-radius: var(--radius-lg);
  padding: var(--space-6) var(--space-4);
  box-shadow: var(--shadow-sm);
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-2);
}

.feature-icon-wrapper {
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

  &.icon-bg-5 {
    background: linear-gradient(135deg, #45B7D1 0%, #74C7E3 100%);
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
  border-radius: var(--radius-lg);
  overflow: hidden;

  .banner-item {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;

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
      font-size: 44rpx;
      font-weight: bold;
      margin-bottom: var(--space-2);
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
  height: 36rpx;
  background: linear-gradient(180deg, #FFC300 0%, #FFB300 100%);
  border-radius: 4rpx;
  margin-right: var(--space-3);
}

.section-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.section-more {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
}

.more-arrow {
  font-size: 24rpx;
  margin-left: var(--space-1);
}

/* 任务卡片 */
.task-list {
  .task-card {
    background-color: #fff;
    border-radius: var(--radius-lg);
    margin-bottom: var(--space-5);
    box-shadow: var(--shadow-sm);
    overflow: hidden;

    &:active {
      box-shadow: var(--shadow-md);
    }

    .task-main {
      padding: var(--space-5);
    }

    .task-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--space-4);
    }

    .task-type-tag {
      padding: 6rpx 16rpx;
      border-radius: var(--radius-sm);
      font-size: var(--font-size-xs);
      font-weight: var(--font-weight-medium);

      &.type-1 {
        background-color: rgba(255, 107, 107, 0.1);
        color: #FF6B6B;
      }

      &.type-2 {
        background-color: rgba(78, 205, 196, 0.1);
        color: #4ECDC4;
      }

      &.type-3 {
        background-color: rgba(102, 126, 234, 0.1);
        color: #667eea;
      }
    }

    .task-price {
      display: flex;
      align-items: baseline;

      .price-symbol {
        font-size: var(--font-size-sm);
        color: #FF4D4F;
        font-weight: var(--font-weight-bold);
      }

      .price-value {
        font-size: 40rpx;
        color: #FF4D4F;
        font-weight: var(--font-weight-bold);
      }
    }

    .task-title {
      font-size: var(--font-size-base);
      color: var(--color-text-primary);
      font-weight: var(--font-weight-medium);
      margin-bottom: var(--space-4);
      line-height: 1.5;
    }

    .task-info {
      .info-item {
        display: flex;
        align-items: center;
        margin-bottom: var(--space-2);

        &:last-child {
          margin-bottom: 0;
        }

        .info-icon {
          font-size: 24rpx;
          color: var(--color-text-tertiary);
          margin-right: var(--space-2);
        }

        .info-text {
          font-size: var(--font-size-sm);
          color: var(--color-text-secondary);
        }
      }
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

      .user-info {
        display: flex;
        align-items: center;

        .user-avatar {
          width: 56rpx;
          height: 56rpx;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
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

          .avatar-text {
            color: #fff;
            font-size: 24rpx;
            font-weight: var(--font-weight-bold);
          }
        }

        .user-name {
          font-size: var(--font-size-sm);
          color: var(--color-text-secondary);
        }
      }

      .grab-btn {
        background: linear-gradient(135deg, #FFC300 0%, #FFB300 100%);
        color: #333;
        font-size: var(--font-size-sm);
        font-weight: var(--font-weight-semibold);
        padding: 16rpx 32rpx;
        border-radius: var(--radius-full);
        border: none;
        line-height: 1;

        &:active {
          opacity: 0.9;
          transform: scale(0.98);
        }

        &::after {
          border: none;
        }
      }
    }
  }
}
</style>
