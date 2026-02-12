<template>
  <view class="container">
    <!-- 任务信息 -->
    <view class="task-info">
      <text class="label">评价任务</text>
      <text class="task-title">{{ taskTitle }}</text>
    </view>

    <!-- 评分 -->
    <view class="rating-section">
      <text class="section-title">总体评分</text>
      <view class="star-container">
        <view 
          class="star-item" 
          v-for="(item, index) in 5" 
          :key="index"
          @click="setRating(index + 1)"
        >
          <text class="star-icon" :class="{ active: rating >= index + 1 }">★</text>
        </view>
      </view>
      <text class="rating-text">{{ ratingText }}</text>
    </view>

    <!-- 评价标签 -->
    <view class="tags-section" v-if="rating > 0">
      <text class="section-title">选择标签</text>
      <view class="tags-container">
        <view 
          class="tag-item" 
          v-for="(tag, index) in tagOptions" 
          :key="index"
          :class="{ active: selectedTags.includes(tag) }"
          @click="toggleTag(tag)"
        >
          {{ tag }}
        </view>
      </view>
    </view>

    <!-- 评价内容 -->
    <view class="content-section">
      <text class="section-title">评价内容</text>
      <textarea 
        v-model="content" 
        placeholder="请输入您的评价内容..."
        maxlength="200"
      />
      <text class="word-count">{{ content.length }}/200</text>
    </view>

    <!-- 匿名评价 -->
    <view class="anonymous-section">
      <text class="label">匿名评价</text>
      <switch :checked="isAnonymous" @change="toggleAnonymous" color="var(--color-primary)"/>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button 
        class="btn-submit" 
        :disabled="rating === 0"
        @click="submitEvaluation"
      >
        提交评价
      </button>
    </view>
  </view>
</template>

<script>
import evaluationApi from '@/api/evaluation.js'

export default {
  data() {
    return {
      taskId: null,
      toUserId: null,
      taskTitle: '',
      rating: 0,
      content: '',
      selectedTags: [],
      isAnonymous: 0,
      tagOptions: [
        '服务态度好',
        '送达速度快',
        '包装完好',
        '沟通顺畅',
        '非常专业',
        '准时到达',
        '细心周到',
        '强烈推荐'
      ]
    }
  },
  computed: {
    ratingText() {
      const texts = ['', '非常差', '差', '一般', '满意', '非常满意']
      return texts[this.rating] || ''
    }
  },
  onLoad(options) {
    this.taskId = options.taskId
    this.toUserId = options.toUserId
    this.taskTitle = options.taskTitle || '任务评价'
  },
  methods: {
    setRating(value) {
      this.rating = value
    },
    
    toggleTag(tag) {
      const index = this.selectedTags.indexOf(tag)
      if (index > -1) {
        this.selectedTags.splice(index, 1)
      } else {
        if (this.selectedTags.length < 3) {
          this.selectedTags.push(tag)
        } else {
          uni.showToast({
            title: '最多选择3个标签',
            icon: 'none'
          })
        }
      }
    },
    
    toggleAnonymous(e) {
      this.isAnonymous = e.detail.value ? 1 : 0
    },
    
    async submitEvaluation() {
      if (this.rating === 0) {
        uni.showToast({
          title: '请选择评分',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({ title: '提交中...' })
        
        const res = await evaluationApi.submit({
          taskId: this.taskId,
          toUserId: this.toUserId,
          rating: this.rating,
          content: this.content,
          tags: this.selectedTags,
          isAnonymous: this.isAnonymous
        })
        
        uni.hideLoading()
        
        if (res.code === 200) {
          uni.showToast({
            title: '评价成功',
            icon: 'success'
          })
          
          // 通知上一页刷新
          const pages = getCurrentPages()
          const prevPage = pages[pages.length - 2]
          if (prevPage && prevPage.$vm && prevPage.$vm.refreshData) {
            prevPage.$vm.refreshData()
          }
          
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.message || '评价失败',
            icon: 'none'
          })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({
          title: '评价失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@use '@/static/styles/mixins.scss' as *;

.container {
  min-height: 100vh;
  padding: var(--space-5);
  background: radial-gradient(circle at right top, rgba(var(--color-primary-rgb), 0.1) 0%, transparent 40%), var(--color-bg);
}

.task-info,
.rating-section,
.tags-section,
.content-section,
.anonymous-section {
  margin-bottom: var(--space-4);
  padding: var(--space-5);
  border-radius: var(--radius-xl);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
}

.task-info {
  .label {
    display: block;
    margin-bottom: var(--space-2);
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
  }

  .task-title {
    font-size: var(--font-size-lg);
    color: var(--color-text-primary);
    font-weight: var(--font-weight-semibold);
    line-height: var(--line-height-snug);
  }
}

.rating-section {
  text-align: center;

  .section-title {
    display: block;
    margin-bottom: var(--space-5);
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
  }

  .star-container {
    display: flex;
    justify-content: center;
    margin-bottom: var(--space-3);

    .star-item {
      padding: 0 var(--space-2);

      .star-icon {
        font-size: 56rpx;
        color: var(--color-border-dark);
        transition: all var(--duration-fast) var(--ease-out);

        &.active {
          color: var(--color-primary);
          text-shadow: 0 4rpx 10rpx rgba(var(--color-primary-rgb), 0.28);
        }
      }
    }
  }

  .rating-text {
    font-size: var(--font-size-sm);
    color: var(--color-info);
    font-weight: var(--font-weight-medium);
  }
}

.tags-section {
  .section-title {
    display: block;
    margin-bottom: var(--space-3);
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
  }

  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-2);

    .tag-item {
      padding: var(--space-2) var(--space-4);
      border-radius: var(--radius-full);
      border: 2rpx solid transparent;
      background: var(--color-bg-secondary);
      color: var(--color-text-secondary);
      font-size: var(--font-size-sm);
      transition: all var(--duration-fast) var(--ease-out);

      &.active {
        background: var(--color-task-other-soft);
        color: var(--color-task-other);
        border-color: rgba(29, 78, 216, 0.35);
      }
    }
  }
}

.content-section {
  position: relative;

  .section-title {
    display: block;
    margin-bottom: var(--space-3);
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
  }

  textarea {
    width: 100%;
    height: 220rpx;
    padding: var(--space-4);
    border-radius: var(--radius-lg);
    background: var(--color-bg-secondary);
    border: 2rpx solid var(--color-border-light);
    font-size: var(--font-size-base);
    color: var(--color-text-primary);
    line-height: var(--line-height-relaxed);
  }

  .word-count {
    position: absolute;
    right: var(--space-6);
    bottom: var(--space-5);
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
  }
}

.anonymous-section {
  @include flex-between;

  .label {
    font-size: var(--font-size-base);
    color: var(--color-text-primary);
    font-weight: var(--font-weight-medium);
  }
}

.submit-section {
  padding: var(--space-5) 0;

  .btn-submit {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    border-radius: var(--radius-full);
    background: var(--color-primary-gradient);
    color: var(--color-text-primary);
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-semibold);
    box-shadow: var(--shadow-primary);
    border: none;
    transition: all var(--duration-fast) var(--ease-out);

    &:disabled {
      background: var(--color-bg-tertiary);
      color: var(--color-text-disabled);
      box-shadow: none;
    }

    &:active {
      transform: scale(0.98);
    }
  }
}
</style>

