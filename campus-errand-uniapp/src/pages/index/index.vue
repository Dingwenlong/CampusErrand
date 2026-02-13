<template>
  <view class="container">
    <!-- 顶部黄色区域 -->
    <view class="header-section">
      <!-- 定位栏 -->
      <view class="location-bar">
        <view class="location-left" @click="showLocationPicker">
          <text class="iconfont icon-location-fill location-icon"></text>
          <text class="location-text">{{ currentSchool }}</text>
          <text class="iconfont icon-arrow-down location-arrow"></text>
        </view>
        <view class="location-right">
          <view class="icon-btn message-btn" @click="navigateTo('/pages/message/list')">
            <text class="iconfont icon-message message-icon-text"></text>
            <view v-if="unreadCount > 0" class="badge-full">{{ unreadCount }}</view>
          </view>
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
          @click="goToTaskList(item.type)"
          :style="{ animationDelay: index * 0.05 + 's' }"
        >
          <view class="feature-icon-wrapper scale-in" :class="'icon-bg-' + item.type">
            <text class="feature-icon">{{ item.icon }}</text>
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
      indicator-active-color="var(--color-primary)"
      @change="onBannerChange"
    >
      <swiper-item v-for="(item, index) in banners" :key="index">
        <view class="banner-item scale-in" :style="{ background: item.bgColor || getDefaultBannerColor(index) }">
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
      <view class="task-list" v-if="taskList.length > 0">
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
      
      <!-- 空状态 -->
      <view class="empty-state" v-else-if="!loading">
        <text class="iconfont icon-empty empty-icon"></text>
        <text class="empty-text">暂无推荐任务</text>
        <text class="empty-subtext">去发布一个任务吧</text>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="loading">
      <view class="loading-spinner"></view>
      <text>加载中...</text>
    </view>

    <!-- 自定义底部导航 -->
    <custom-tabbar :current="0" />
  </view>
</template>

<script>
import { http } from '@/utils/request.js'
import CustomTabbar from '@/components/custom-tabbar/index.vue'

const taskTypeMap = {
  1: '取快递',
  2: '代买',
  3: '送件',
  4: '外卖',
  5: '其他'
}

const bannerColors = [
  'linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%)',
  'linear-gradient(135deg, #7BC47F 0%, #9DD9A0 100%)',
  'linear-gradient(135deg, #FFB347 0%, #FFC970 100%)'
]

export default {
  components: {
    CustomTabbar
  },
  data() {
    return {
      unreadCount: 2,
      currentBanner: 0,
      loading: false,
      currentSchool: '山东现代学院',
      jinanSchools: [
        '山东现代学院',
        '山东大学',
        '山东师范大学',
        '济南大学',
        '山东财经大学',
        '山东建筑大学',
        '山东中医药大学',
        '齐鲁工业大学',
        '山东第一医科大学',
        '山东交通学院',
        '山东青年政治学院',
        '山东管理学院',
        '山东农业工程学院',
        '山东女子学院',
        '山东工艺美术学院',
        '山东艺术学院'
      ],
      features: [
        { name: '取快递', type: 1, icon: '📦', path: '/pages/task/list?type=1' },
        { name: '代买', type: 2, icon: '🛒', path: '/pages/task/list?type=2' },
        { name: '送件', type: 3, icon: '📨', path: '/pages/task/list?type=3' },
        { name: '外卖', type: 4, icon: '🍔', path: '/pages/task/list?type=4' },
        { name: '其他', type: 5, icon: '✨', path: '/pages/task/list?type=5' },
      ],
      banners: [
        { title: '校园跑腿', desc: '便捷生活，从这里开始', bgColor: '' },
        { title: '安全可靠', desc: '实名认证，信用保障', bgColor: '' },
        { title: '快速响应', desc: '附近跑腿员，即时接单', bgColor: '' }
      ],
      taskList: []
    }
  },
  onLoad() {
    const savedSchool = uni.getStorageSync('currentSchool')
    if (savedSchool) {
      this.currentSchool = savedSchool
    }
    this.fetchRecommendTasks()
    this.fetchBanners()
  },
  onShow() {
    uni.$emit('tabBarChange', 0)
  },
  onPullDownRefresh() {
    this.fetchRecommendTasks()
    this.fetchBanners()
    uni.stopPullDownRefresh()
  },
  methods: {
    async fetchRecommendTasks() {
      this.loading = true
      try {
        const res = await http.get('/task/list', {
          status: 0,
          current: 1,
          size: 5
        })
        if (res.code === 200) {
          this.taskList = res.data.records.map(task => ({
            id: task.id,
            type: task.taskType,
            typeName: task.taskTypeName || taskTypeMap[task.taskType] || '其他',
            title: task.title,
            price: task.totalAmount || task.reward,
            from: task.pickupAddress,
            to: task.deliveryAddress,
            time: this.formatTime(task.deadlineTime),
            userName: task.publisherName || '匿名用户'
          }))
        }
      } catch (error) {
        console.error('获取推荐任务失败:', error)
      } finally {
        this.loading = false
      }
    },
    async fetchBanners() {
      try {
        const res = await http.get('/banner/list')
        if (res.code === 200 && res.data && res.data.length > 0) {
          this.banners = res.data.map((item, index) => ({
            title: item.title,
            desc: item.content || item.description,
            bgColor: item.bgColor || bannerColors[index % bannerColors.length]
          }))
        }
      } catch (error) {
        console.error('获取轮播图失败:', error)
      }
    },
    formatTime(timeStr) {
      if (!timeStr) return '暂无截止时间'
      const date = new Date(timeStr)
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
      
      const diffDays = Math.floor((targetDate - today) / (1000 * 60 * 60 * 24))
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      
      if (diffDays === 0) {
        return `今天 ${hours}:${minutes}前`
      } else if (diffDays === 1) {
        return `明天 ${hours}:${minutes}前`
      } else if (diffDays > 1) {
        return `${diffDays}天后 ${hours}:${minutes}前`
      } else {
        return `${date.getMonth() + 1}月${date.getDate()}日 ${hours}:${minutes}前`
      }
    },
    getDefaultBannerColor(index) {
      return bannerColors[index % bannerColors.length]
    },
    navigateTo(path) {
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
    goToTaskList(type) {
      uni.setStorageSync('taskListType', type)
      uni.switchTab({
        url: '/pages/task/list'
      })
    },
    goTaskDetail(task) {
      uni.navigateTo({
        url: `/pages/task/detail?id=${task.id}`
      })
    },
    grabTask(task) {
      // #ifdef MP-WEIXIN
      if (uni.vibrateShort) {
        uni.vibrateShort({ type: 'light' })
      }
      // #endif
      
      uni.showModal({
        title: '确认接单',
        content: `确定要接这个${task.typeName}任务吗？`,
        confirmColor: '#f59e0b',
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
      uni.showActionSheet({
        itemList: this.jinanSchools,
        success: (res) => {
          this.currentSchool = this.jinanSchools[res.tapIndex]
          uni.setStorageSync('currentSchool', this.currentSchool)
          uni.showToast({
            title: `已切换到${this.currentSchool}`,
            icon: 'none'
          })
        }
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
  padding-bottom: calc(var(--space-8) + 140rpx + env(safe-area-inset-bottom));
}

/* 顶部黄色区域 */
.header-section {
  background: var(--color-primary-gradient);
  padding: var(--space-4) var(--space-6) var(--space-6);
  border-radius: 0 0 var(--radius-3xl) var(--radius-3xl);
  box-shadow: var(--shadow-md);
}

/* 定位栏 */
.location-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.location-icon {
  font-size: 28rpx;
  color: var(--color-text-primary);
  margin-right: var(--space-2);
}

.location-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  max-width: 280rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  width: 48rpx;
  height: 48rpx;
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

.message-btn {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.25) 0%, rgba(255, 255, 255, 0.15) 100%);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.message-icon-text {
  font-size: 24rpx;
}

.badge-full {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: var(--color-error);
  color: var(--color-white);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  border-radius: var(--radius-full);
  @include flex-center;
  opacity: 0.95;
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

  &.icon-bg-5 {
    background: linear-gradient(135deg, #FF8C5A 0%, #FFB088 100%);
    box-shadow: 0 8rpx 24rpx rgba(255, 140, 90, 0.35);
  }
}

.feature-icon {
  font-size: 44rpx;
}

.feature-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

/* 轮播图 */
.banner-swiper {
  height: 280rpx;
  margin: var(--space-6);
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
  padding: var(--space-6);
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
    background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  }
  
  &.avatar-bg-2 {
    background: linear-gradient(135deg, #7BC47F 0%, #9DD9A0 100%);
  }
  
  &.avatar-bg-3 {
    background: linear-gradient(135deg, #FFB347 0%, #FFC970 100%);
  }
  
  &.avatar-bg-4, &.avatar-bg-5 {
    background: linear-gradient(135deg, #FFB088 0%, #FFC4A8 100%);
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

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-12) var(--space-6);
  
  .empty-icon {
    font-size: 80rpx;
    margin-bottom: var(--space-4);
  }
  
  .empty-text {
    font-size: var(--font-size-lg);
    color: var(--color-text-secondary);
    margin-bottom: var(--space-2);
  }
  
  .empty-subtext {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
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
