<template>
  <view class="container">
    <!-- 标题区域 -->
    <view class="header animate-fade-in-down">
      <view class="step-indicator">
        <view class="step" :class="{ active: step >= 1, completed: step > 1 }">
          <text class="step-number">1</text>
          <text class="step-label">设置密码</text>
        </view>
        <view class="step-line" :class="{ active: step >= 2 }"></view>
        <view class="step" :class="{ active: step >= 2 }">
          <text class="step-number">2</text>
          <text class="step-label">确认密码</text>
        </view>
      </view>
      <text class="tip-text">{{ step === 1 ? '请设置6位数字支付密码' : '请再次输入以确认' }}</text>
    </view>

    <!-- 密码输入显示 -->
    <view class="password-display animate-fade-in-up delay-100">
      <view 
        class="password-item" 
        v-for="(item, index) in 6" 
        :key="index"
        :class="{ active: password.length === index, filled: password.length > index }"
      >
        <text v-if="password.length > index" class="password-dot"></text>
        <text v-else-if="password.length === index" class="cursor"></text>
      </view>
    </view>

    <!-- 安全提示 -->
    <view class="security-tips animate-fade-in-up delay-200">
      <view class="tip-item">
        <text class="iconfont icon-shield tip-icon"></text>
        <text class="tip-text-small">支付密码用于任务结算和提现</text>
      </view>
      <view class="tip-item">
        <text class="iconfont icon-lock tip-icon"></text>
        <text class="tip-text-small">请勿将密码告知他人</text>
      </view>
    </view>

    <!-- 数字键盘 -->
    <view class="keyboard animate-fade-in-up delay-300">
      <view class="keyboard-row" v-for="(row, rowIndex) in keyboardKeys" :key="rowIndex">
        <view 
          class="keyboard-key" 
          v-for="(key, keyIndex) in row" 
          :key="keyIndex" 
          @click="inputKey(key)"
          :class="{ 'key-delete': key === 'delete', 'key-empty': key === '' }"
          :hover-class="key !== '' ? 'key-hover' : ''"
          :hover-start-time="0"
          :hover-stay-time="100"
        >
          <text v-if="key !== 'delete' && key !== ''" class="key-text">{{ key }}</text>
          <text v-else-if="key === 'delete'" class="iconfont icon-delete key-icon"></text>
        </view>
      </view>
    </view>

    <!-- 底部安全区域 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script>
import walletApi from '@/api/wallet.js'

export default {
  data() {
    return {
      password: '',
      confirmPassword: '',
      step: 1,
      keyboardKeys: [
        ['1', '2', '3'],
        ['4', '5', '6'],
        ['7', '8', '9'],
        ['', '0', 'delete']
      ]
    }
  },
  methods: {
    inputKey(key) {
      if (key === 'delete') {
        this.handleDelete()
      } else if (key !== '') {
        this.handleInput(key)
      }
    },
    handleInput(key) {
      if (this.step === 1 && this.password.length < 6) {
        this.password += key
        if (this.password.length === 6) {
          setTimeout(() => {
            this.step = 2
          }, 200)
        }
      } else if (this.step === 2 && this.confirmPassword.length < 6) {
        this.confirmPassword += key
        if (this.confirmPassword.length === 6) {
          setTimeout(() => {
            this.submitPassword()
          }, 200)
        }
      }
    },
    handleDelete() {
      if (this.step === 1) {
        this.password = this.password.slice(0, -1)
      } else {
        if (this.confirmPassword.length === 0) {
          this.step = 1
          this.password = ''
        } else {
          this.confirmPassword = this.confirmPassword.slice(0, -1)
        }
      }
    },
    async submitPassword() {
      if (this.password !== this.confirmPassword) {
        uni.showToast({
          title: '两次输入的密码不一致',
          icon: 'none'
        })
        this.resetForm()
        return
      }

      // 验证密码不能是简单序列
      if (this.isSimplePassword(this.password)) {
        uni.showToast({
          title: '密码过于简单，请重新设置',
          icon: 'none'
        })
        this.resetForm()
        return
      }

      try {
        uni.showLoading({ title: '设置中...' })
        const res = await walletApi.setPayPassword({
          payPassword: this.password,
          confirmPassword: this.confirmPassword
        })
        uni.hideLoading()

        if (res.code === 200) {
          uni.showToast({
            title: '设置成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.message || '设置失败',
            icon: 'none'
          })
          this.resetForm()
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({
          title: '设置失败',
          icon: 'none'
        })
        this.resetForm()
      }
    },
    resetForm() {
      this.step = 1
      this.password = ''
      this.confirmPassword = ''
    },
    isSimplePassword(pwd) {
      // 检查是否是连续数字
      const sequences = ['012345', '123456', '234567', '345678', '456789', '567890',
                        '098765', '987654', '876543', '765432', '654321', '543210']
      if (sequences.includes(pwd)) return true
      
      // 检查是否是完全相同的数字
      if (/^(\d)\1{5}$/.test(pwd)) return true
      
      return false
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/static/styles/mixins.scss';

.container {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
}

// 头部区域
.header {
  padding: var(--space-12) var(--space-8) var(--space-8);
  
  .step-indicator {
    @include flex-center;
    margin-bottom: var(--space-8);
    
    .step {
      @include flex-column;
      align-items: center;
      
      .step-number {
        width: 48rpx;
        height: 48rpx;
        @include flex-center;
        background: var(--color-bg-tertiary);
        color: var(--color-text-tertiary);
        border-radius: var(--radius-full);
        font-size: var(--font-size-sm);
        font-weight: var(--font-weight-semibold);
        margin-bottom: var(--space-2);
        transition: all var(--duration-normal) var(--ease-out);
      }
      
      .step-label {
        font-size: var(--font-size-xs);
        color: var(--color-text-tertiary);
        transition: all var(--duration-normal) var(--ease-out);
      }
      
      &.active {
        .step-number {
          background: var(--color-primary);
          color: var(--color-text-inverse);
        }
        
        .step-label {
          color: var(--color-primary);
          font-weight: var(--font-weight-medium);
        }
      }
      
      &.completed {
        .step-number {
          background: var(--color-success);
          color: var(--color-text-inverse);
        }
      }
    }
    
    .step-line {
      width: 80rpx;
      height: 2rpx;
      background: var(--color-border);
      margin: 0 var(--space-4);
      margin-bottom: var(--space-6);
      transition: all var(--duration-normal) var(--ease-out);
      
      &.active {
        background: var(--color-primary);
      }
    }
  }
  
  .tip-text {
    display: block;
    text-align: center;
    font-size: var(--font-size-lg);
    color: var(--color-text-primary);
    font-weight: var(--font-weight-medium);
  }
}

// 密码显示区域
.password-display {
  @include flex-center;
  gap: var(--space-4);
  margin-bottom: var(--space-8);
  
  .password-item {
    width: 88rpx;
    height: 88rpx;
    @include flex-center;
    background: var(--color-surface);
    border: 2rpx solid var(--color-border);
    border-radius: var(--radius-lg);
    transition: all var(--duration-fast) var(--ease-out);
    
    &.active {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 4rpx rgba(var(--color-primary-rgb), 0.1);
    }
    
    &.filled {
      border-color: var(--color-primary);
      background: rgba(var(--color-primary-rgb), 0.05);
    }
    
    .password-dot {
      width: 20rpx;
      height: 20rpx;
      background: var(--color-text-primary);
      border-radius: var(--radius-full);
      animation: scaleIn var(--duration-fast) var(--ease-out);
    }
    
    .cursor {
      width: 2rpx;
      height: 40rpx;
      background: var(--color-primary);
      animation: blink 1s infinite;
    }
  }
}

// 安全提示
.security-tips {
  @include flex-column;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-8);
  
  .tip-item {
    @include flex-vcenter;
    
    .tip-icon {
      font-size: var(--font-size-sm);
      color: var(--color-text-tertiary);
      margin-right: var(--space-2);
    }
    
    .tip-text-small {
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
    }
  }
}

// 数字键盘
.keyboard {
  background: var(--color-surface);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  padding: var(--space-6) var(--space-4);
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
  margin-top: auto;
  
  .keyboard-row {
    @include flex-between;
    margin-bottom: var(--space-4);
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .keyboard-key {
      flex: 1;
      height: 100rpx;
      @include flex-center;
      margin: 0 var(--space-2);
      background: var(--color-bg-secondary);
      border-radius: var(--radius-lg);
      transition: all var(--duration-fast) var(--ease-out);
      
      &.key-hover {
        background: var(--color-bg-tertiary);
        transform: scale(0.96);
      }
      
      &.key-delete {
        background: rgba(255, 77, 79, 0.1);
        
        &.key-hover {
          background: rgba(255, 77, 79, 0.2);
        }
        
        .key-icon {
          color: var(--color-error);
        }
      }
      
      &.key-empty {
        background: transparent;
        pointer-events: none;
      }
      
      .key-text {
        font-size: var(--font-size-2xl);
        font-weight: var(--font-weight-medium);
        color: var(--color-text-primary);
      }
      
      .key-icon {
        font-size: var(--font-size-xl);
        color: var(--color-text-secondary);
      }
    }
  }
}

// 安全区域
.safe-area-bottom {
  height: env(safe-area-inset-bottom);
  background: var(--color-surface);
}

// 动画
@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}
</style>
