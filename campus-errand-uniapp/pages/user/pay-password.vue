<template>
  <view class="container">
    <view class="tip-text">
      <text>请设置6位数字支付密码</text>
    </view>

    <view class="password-input">
      <view class="password-item" v-for="(item, index) in 6" :key="index">
        <text v-if="password.length > index" class="password-dot">●</text>
      </view>
    </view>

    <view class="keyboard">
      <view class="keyboard-row" v-for="(row, rowIndex) in keyboardKeys" :key="rowIndex">
        <view class="keyboard-key" v-for="(key, keyIndex) in row" :key="keyIndex" @click="inputKey(key)">
          <text v-if="key !== 'delete'" class="key-text">{{ key }}</text>
          <text v-else class="iconfont icon-delete key-icon"></text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import walletApi from '@/api/wallet.js'

export default {
  data() {
    return {
      password: '',
      confirmPassword: '',
      step: 1, // 1-输入密码 2-确认密码
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
        if (this.step === 1) {
          this.password = this.password.slice(0, -1)
        } else {
          this.confirmPassword = this.confirmPassword.slice(0, -1)
        }
      } else if (key !== '') {
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
      }
    },
    async submitPassword() {
      if (this.password !== this.confirmPassword) {
        uni.showToast({
          title: '两次输入的密码不一致',
          icon: 'none'
        })
        this.step = 1
        this.password = ''
        this.confirmPassword = ''
        return
      }

      try {
        const res = await walletApi.setPayPassword({
          payPassword: this.password,
          confirmPassword: this.confirmPassword
        })

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
          this.step = 1
          this.password = ''
          this.confirmPassword = ''
        }
      } catch (e) {
        uni.showToast({
          title: '设置失败',
          icon: 'none'
        })
        this.step = 1
        this.password = ''
        this.confirmPassword = ''
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60rpx;
}

.tip-text {
  text-align: center;
  margin-bottom: 60rpx;

  text {
    font-size: 32rpx;
    color: #666;
  }
}

.password-input {
  display: flex;
  justify-content: center;
  margin-bottom: 100rpx;

  .password-item {
    width: 80rpx;
    height: 80rpx;
    background: #fff;
    border: 1rpx solid #ddd;
    border-right: none;
    display: flex;
    align-items: center;
    justify-content: center;

    &:first-child {
      border-radius: 8rpx 0 0 8rpx;
    }

    &:last-child {
      border-right: 1rpx solid #ddd;
      border-radius: 0 8rpx 8rpx 0;
    }

    .password-dot {
      font-size: 40rpx;
      color: #333;
    }
  }
}

.keyboard {
  background: #fff;
  padding: 20rpx 0;

  .keyboard-row {
    display: flex;
    justify-content: center;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .keyboard-key {
      width: 200rpx;
      height: 100rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      border-radius: 8rpx;
      margin: 0 10rpx;

      &:active {
        background: #e0e0e0;
      }

      .key-text {
        font-size: 40rpx;
        color: #333;
      }

      .key-icon {
        font-size: 40rpx;
        color: #666;
      }
    }
  }
}
</style>
