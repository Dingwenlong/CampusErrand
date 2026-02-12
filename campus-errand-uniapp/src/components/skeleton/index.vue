<template>
  <view class="skeleton-container" :class="{ animate: animated }">
    <!-- 顶部骨架 -->
    <view v-if="type === 'header' || type === 'full'" class="skeleton-header">
      <view class="skeleton-avatar"></view>
      <view class="skeleton-info">
        <view class="skeleton-line short"></view>
        <view class="skeleton-line"></view>
      </view>
    </view>

    <!-- 卡片骨架 -->
    <view v-if="type === 'card' || type === 'full'" class="skeleton-card">
      <view class="skeleton-line title"></view>
      <view class="skeleton-content">
        <view v-for="i in rows" :key="i" class="skeleton-line" :style="{ width: getRandomWidth() }"></view>
      </view>
    </view>

    <!-- 列表骨架 -->
    <view v-if="type === 'list' || type === 'full'" class="skeleton-list">
      <view v-for="i in rows" :key="i" class="skeleton-item">
        <view class="skeleton-icon"></view>
        <view class="skeleton-text">
          <view class="skeleton-line"></view>
          <view class="skeleton-line short"></view>
        </view>
      </view>
    </view>

    <!-- 任务列表骨架 -->
    <view v-if="type === 'task-list'" class="skeleton-task-list">
      <view v-for="i in rows" :key="i" class="skeleton-task-item">
        <view class="task-header">
          <view class="skeleton-tag"></view>
          <view class="skeleton-price"></view>
        </view>
        <view class="task-body">
          <view class="skeleton-line title"></view>
          <view class="skeleton-address">
            <view class="skeleton-dot"></view>
            <view class="skeleton-line"></view>
          </view>
          <view class="skeleton-address">
            <view class="skeleton-dot red"></view>
            <view class="skeleton-line"></view>
          </view>
        </view>
        <view class="task-footer">
          <view class="skeleton-avatar small"></view>
          <view class="skeleton-line short"></view>
        </view>
      </view>
    </view>

    <!-- 自定义骨架 -->
    <slot></slot>
  </view>
</template>

<script>
export default {
  name: 'Skeleton',
  props: {
    // 骨架屏类型：header-头部, card-卡片, list-列表, task-list-任务列表, full-完整
    type: {
      type: String,
      default: 'full'
    },
    // 行数
    rows: {
      type: Number,
      default: 3
    },
    // 是否开启动画
    animated: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    getRandomWidth() {
      const widths = ['60%', '80%', '100%', '75%', '90%']
      return widths[Math.floor(Math.random() * widths.length)]
    }
  }
}
</script>

<style lang="scss" scoped>
.skeleton-container {
  padding: var(--space-6);
}

/* 骨架屏基础样式 */
.skeleton-header,
.skeleton-card,
.skeleton-list,
.skeleton-task-list {
  background-color: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
}

/* 通用骨架元素 */
.skeleton-avatar,
.skeleton-line,
.skeleton-icon,
.skeleton-tag,
.skeleton-price,
.skeleton-dot {
  background: linear-gradient(
    90deg,
    var(--color-bg-secondary) 25%,
    var(--color-bg-tertiary) 50%,
    var(--color-bg-secondary) 75%
  );
  background-size: 200% 100%;
  border-radius: var(--radius-md);
}

.skeleton-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  flex-shrink: 0;

  &.small {
    width: 64rpx;
    height: 64rpx;
  }
}

.skeleton-line {
  height: 32rpx;
  margin-bottom: var(--space-3);
  border-radius: var(--radius-sm);

  &:last-child {
    margin-bottom: 0;
  }

  &.short {
    width: 40%;
  }

  &.title {
    width: 60%;
    height: 40rpx;
    margin-bottom: var(--space-4);
  }
}

.skeleton-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}

/* 头部骨架 */
.skeleton-header {
  display: flex;
  align-items: center;

  .skeleton-info {
    flex: 1;
    margin-left: var(--space-5);
  }
}

/* 卡片骨架 */
.skeleton-content {
  .skeleton-line {
    margin-bottom: var(--space-4);
  }
}

/* 列表骨架 */
.skeleton-item {
  display: flex;
  align-items: center;
  padding: var(--space-4) 0;
  border-bottom: 2rpx solid var(--color-divider);

  &:last-child {
    border-bottom: none;
  }

  .skeleton-text {
    flex: 1;
    margin-left: var(--space-4);
  }
}

/* 任务列表骨架 */
.skeleton-task-item {
  padding: var(--space-5) 0;
  border-bottom: 2rpx solid var(--color-divider);

  &:last-child {
    border-bottom: none;
  }
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.skeleton-tag {
  width: 120rpx;
  height: 40rpx;
  border-radius: var(--radius-sm);
}

.skeleton-price {
  width: 100rpx;
  height: 48rpx;
  border-radius: var(--radius-sm);
}

.task-body {
  margin-bottom: var(--space-4);

  .skeleton-line.title {
    margin-bottom: var(--space-4);
  }
}

.skeleton-address {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-3);

  &:last-child {
    margin-bottom: 0;
  }
}

.skeleton-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  margin-right: var(--space-3);
  flex-shrink: 0;

  &.red {
    background: linear-gradient(
      90deg,
      rgba(var(--color-error-rgb), 0.18) 25%,
      rgba(var(--color-error-rgb), 0.28) 50%,
      rgba(var(--color-error-rgb), 0.18) 75%
    );
    background-size: 200% 100%;
  }
}

.task-footer {
  display: flex;
  align-items: center;

  .skeleton-avatar {
    margin-right: var(--space-3);
  }

  .skeleton-line {
    width: 120rpx;
    margin-bottom: 0;
  }
}

/* 动画效果 */
.animate {
  .skeleton-avatar,
  .skeleton-line,
  .skeleton-icon,
  .skeleton-tag,
  .skeleton-price,
  .skeleton-dot {
    animation: shimmer 1.5s infinite;
  }
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
