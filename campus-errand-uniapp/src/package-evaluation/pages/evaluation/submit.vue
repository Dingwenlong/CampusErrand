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
      <switch :checked="isAnonymous" @change="toggleAnonymous" color="#667eea"/>
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
.container {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.task-info {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .label {
    display: block;
    font-size: 26rpx;
    color: #999;
    margin-bottom: 12rpx;
  }
  
  .task-title {
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
  }
}

.rating-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  text-align: center;
  
  .section-title {
    display: block;
    font-size: 30rpx;
    color: #333;
    margin-bottom: 30rpx;
    font-weight: 500;
  }
  
  .star-container {
    display: flex;
    justify-content: center;
    margin-bottom: 20rpx;
    
    .star-item {
      padding: 0 15rpx;
      
      .star-icon {
        font-size: 60rpx;
        color: #ddd;
        transition: color 0.2s;
        
        &.active {
          color: #FFC300;
        }
      }
    }
  }
  
  .rating-text {
    font-size: 28rpx;
    color: #667eea;
    font-weight: 500;
  }
}

.tags-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .section-title {
    display: block;
    font-size: 30rpx;
    color: #333;
    margin-bottom: 20rpx;
    font-weight: 500;
  }
  
  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    
    .tag-item {
      padding: 12rpx 24rpx;
      background: #f5f5f5;
      border-radius: 30rpx;
      font-size: 26rpx;
      color: #666;
      border: 2rpx solid transparent;
      transition: all 0.2s;
      
      &.active {
        background: #f0f0ff;
        color: #667eea;
        border-color: #667eea;
      }
    }
  }
}

.content-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  position: relative;
  
  .section-title {
    display: block;
    font-size: 30rpx;
    color: #333;
    margin-bottom: 20rpx;
    font-weight: 500;
  }
  
  textarea {
    width: 100%;
    height: 200rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 20rpx;
    font-size: 28rpx;
    color: #333;
  }
  
  .word-count {
    position: absolute;
    right: 40rpx;
    bottom: 40rpx;
    font-size: 24rpx;
    color: #999;
  }
}

.anonymous-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .label {
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
  }
}

.submit-section {
  padding: 40rpx 0;
  
  .btn-submit {
    width: 100%;
    height: 90rpx;
    line-height: 90rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-size: 32rpx;
    border-radius: 45rpx;
    font-weight: 500;
    
    &:disabled {
      background: #ccc;
    }
    
    &:active {
      opacity: 0.9;
    }
  }
}
</style>
