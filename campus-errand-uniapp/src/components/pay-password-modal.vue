<template>
  <view class="modal-mask" v-if="show" @click="onCancel">
    <view class="modal-content" @click.stop>
      <view class="modal-header">
        <text class="title">请输入支付密码</text>
        <text class="close-btn" @click="onCancel">×</text>
      </view>
      
      <view class="password-display">
        <view 
          class="password-dot" 
          v-for="(item, index) in 6" 
          :key="index"
          :class="{ filled: password.length > index }"
        >
          <text v-if="password.length > index">●</text>
        </view>
      </view>
      
      <view class="forget-password" @click="onForgetPassword">
        <text>忘记密码？</text>
      </view>
      
      <view class="keyboard">
        <view class="keyboard-row" v-for="(row, rowIndex) in keyboardLayout" :key="rowIndex">
          <view 
            class="keyboard-key" 
            v-for="(key, keyIndex) in row" 
            :key="keyIndex"
            :class="{ 
              'key-number': key !== '',
              'key-delete': key === 'delete',
              'key-empty': key === ''
            }"
            @click="onKeyPress(key)"
          >
            <text v-if="key === 'delete'" class="delete-icon">⌫</text>
            <text v-else>{{ key }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'PayPasswordModal',
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      password: '',
      keyboardLayout: [
        ['1', '2', '3'],
        ['4', '5', '6'],
        ['7', '8', '9'],
        ['', '0', 'delete']
      ]
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.password = ''
      }
    },
    password(newVal) {
      if (newVal.length === 6) {
        // 密码输入完成，触发确认事件
        setTimeout(() => {
          this.$emit('confirm', this.password)
          this.password = ''
        }, 200)
      }
    }
  },
  methods: {
    onKeyPress(key) {
      if (key === '') return
      
      if (key === 'delete') {
        if (this.password.length > 0) {
          this.password = this.password.slice(0, -1)
        }
      } else {
        if (this.password.length < 6) {
          this.password += key
        }
      }
    },
    
    onCancel() {
      this.password = ''
      this.$emit('cancel')
    },
    
    onForgetPassword() {
      uni.navigateTo({
        url: '/package-user/pages/user/pay-password?type=reset'
      })
      this.onCancel()
    }
  }
}
</script>

<style lang="scss" scoped>
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  background: var(--color-surface);
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx 30rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
  
  .title {
    font-size: 32rpx;
    font-weight: bold;
    color: var(--color-text-primary);
  }
  
  .close-btn {
    font-size: 48rpx;
    color: var(--color-text-tertiary);
    line-height: 1;
  }
}

.password-display {
  display: flex;
  justify-content: center;
  margin-bottom: 30rpx;
  
  .password-dot {
    width: 80rpx;
    height: 80rpx;
    border: 2rpx solid var(--color-border);
    border-radius: 12rpx;
    margin: 0 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    color: var(--color-text-primary);
    
    &.filled {
      border-color: var(--color-primary);
      background: rgba(var(--color-primary-rgb), 0.08);
    }
  }
}

.forget-password {
  text-align: center;
  margin-bottom: 40rpx;
  
  text {
    font-size: 26rpx;
    color: var(--color-primary-dark);
  }
}

.keyboard {
  .keyboard-row {
    display: flex;
    justify-content: center;
    margin-bottom: 20rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .keyboard-key {
      width: 200rpx;
      height: 90rpx;
      margin: 0 10rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--color-bg-secondary);
      border-radius: 12rpx;
      font-size: 36rpx;
      color: var(--color-text-primary);
      
      &.key-number {
        &:active {
          background: var(--color-border);
        }
      }
      
      &.key-delete {
        background: var(--color-border);
        
        &:active {
          background: var(--color-border-dark);
        }
        
        .delete-icon {
          font-size: 40rpx;
        }
      }
      
      &.key-empty {
        background: transparent;
      }
    }
  }
}
</style>

