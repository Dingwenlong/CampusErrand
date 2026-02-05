<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input">
        <text class="iconfont icon-search"></text>
        <input type="text" placeholder="搜索任务" disabled />
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="feature-grid">
      <view class="feature-item" v-for="(item, index) in features" :key="index" @click="navigateTo(item.path)">
        <image :src="item.icon" mode="aspectFit" class="feature-icon"></image>
        <text class="feature-text">{{ item.name }}</text>
      </view>
    </view>

    <!-- 轮播图 -->
    <swiper class="banner-swiper" indicator-dots autoplay circular>
      <swiper-item v-for="(item, index) in banners" :key="index">
        <image :src="item" mode="aspectFill" class="banner-image"></image>
      </swiper-item>
    </swiper>

    <!-- 任务列表标题 -->
    <view class="section-header">
      <text class="section-title">推荐任务</text>
      <text class="section-more" @click="navigateTo('/pages/task/list')">更多 ></text>
    </view>

    <!-- 任务列表 -->
    <view class="task-list">
      <view class="task-card" v-for="(task, index) in taskList" :key="index" @click="goTaskDetail(task)">
        <view class="task-header">
          <view class="task-type" :class="'type-' + task.type">{{ task.typeName }}</view>
          <text class="task-price">¥{{ task.price }}</text>
        </view>
        <view class="task-content">
          <view class="task-title">{{ task.title }}</view>
          <view class="task-address">
            <text class="iconfont icon-location"></text>
            <text class="address-text">{{ task.from }} → {{ task.to }}</text>
          </view>
          <view class="task-time">
            <text class="iconfont icon-time"></text>
            <text>{{ task.time }}</text>
          </view>
        </view>
        <view class="task-footer">
          <view class="user-info">
            <image :src="task.userAvatar" class="user-avatar"></image>
            <text class="user-name">{{ task.userName }}</text>
          </view>
          <button class="grab-btn" @click.stop="grabTask(task)">抢单</button>
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
        { name: '取快递', icon: '/static/icons/express.png', path: '/pages/task/list?type=1' },
        { name: '代买', icon: '/static/icons/shopping.png', path: '/pages/task/list?type=2' },
        { name: '送件', icon: '/static/icons/delivery.png', path: '/pages/task/list?type=3' },
        { name: '其他', icon: '/static/icons/other.png', path: '/pages/task/list?type=4' },
      ],
      banners: [
        '/static/banner/banner1.jpg',
        '/static/banner/banner2.jpg',
        '/static/banner/banner3.jpg'
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
          userAvatar: '/static/avatar/default.png',
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
          userAvatar: '/static/avatar/default.png',
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
          userAvatar: '/static/avatar/default.png',
          userName: '王同学'
        }
      ]
    }
  },
  methods: {
    navigateTo(path) {
      uni.navigateTo({
        url: path
      })
    },
    goTaskDetail(task) {
      uni.navigateTo({
        url: `/pages/task/detail?id=${task.id}`
      })
    },
    grabTask(task) {
      uni.showModal({
        title: '确认抢单',
        content: `确定要接这个${task.typeName}任务吗？`,
        success: (res) => {
          if (res.confirm) {
            uni.showToast({
              title: '抢单成功',
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
  padding: 20rpx;
}

.search-bar {
  margin-bottom: 20rpx;

  .search-input {
    display: flex;
    align-items: center;
    background: #fff;
    border-radius: 40rpx;
    padding: 16rpx 30rpx;

    .icon-search {
      color: #999;
      margin-right: 16rpx;
    }

    input {
      flex: 1;
      font-size: 28rpx;
    }
  }
}

.feature-grid {
  display: flex;
  justify-content: space-around;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx 20rpx;
  margin-bottom: 20rpx;

  .feature-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .feature-icon {
      width: 80rpx;
      height: 80rpx;
      margin-bottom: 12rpx;
    }

    .feature-text {
      font-size: 26rpx;
      color: #333;
    }
  }
}

.banner-swiper {
  height: 300rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;

  .banner-image {
    width: 100%;
    height: 100%;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }

  .section-more {
    font-size: 26rpx;
    color: #999;
  }
}

.task-list {
  .task-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;

    .task-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .task-type {
        padding: 6rpx 16rpx;
        border-radius: 8rpx;
        font-size: 24rpx;

        &.type-1 {
          background: #e6f7ff;
          color: #1890ff;
        }

        &.type-2 {
          background: #f6ffed;
          color: #52c41a;
        }

        &.type-3 {
          background: #fff7e6;
          color: #fa8c16;
        }

        &.type-4 {
          background: #f9f0ff;
          color: #722ed1;
        }
      }

      .task-price {
        font-size: 36rpx;
        font-weight: bold;
        color: #ff4d4f;
      }
    }

    .task-content {
      margin-bottom: 20rpx;

      .task-title {
        font-size: 30rpx;
        color: #333;
        margin-bottom: 12rpx;
      }

      .task-address,
      .task-time {
        display: flex;
        align-items: center;
        font-size: 26rpx;
        color: #666;
        margin-bottom: 8rpx;

        .iconfont {
          margin-right: 8rpx;
        }
      }
    }

    .task-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;

      .user-info {
        display: flex;
        align-items: center;

        .user-avatar {
          width: 60rpx;
          height: 60rpx;
          border-radius: 50%;
          margin-right: 12rpx;
        }

        .user-name {
          font-size: 26rpx;
          color: #666;
        }
      }

      .grab-btn {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        font-size: 26rpx;
        padding: 12rpx 32rpx;
        border-radius: 30rpx;
        line-height: 1.5;
      }
    }
  }
}
</style>
