<template>
  <view class="container container-safe">
    <!-- 头像区域 -->
    <view class="avatar-section">
      <view class="avatar-wrapper" @click="changeAvatar">
        <view class="avatar avatar-gradient">
          <text class="avatar-text">{{ userInfo.nickname ? userInfo.nickname.charAt(0) : '?' }}</text>
        </view>
        <view class="avatar-mask">
          <text class="iconfont icon-camera"></text>
        </view>
      </view>
      <text class="avatar-tip">点击更换头像</text>
    </view>

    <!-- 表单区域 -->
    <view class="form-section">
      <view class="form-item">
        <text class="form-label">昵称</text>
        <input 
          class="form-input" 
          v-model="form.nickname" 
          placeholder="请输入昵称"
          maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">手机号</text>
        <input 
          class="form-input" 
          v-model="form.phone" 
          placeholder="请输入手机号"
          maxlength="11"
          type="number"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">性别</text>
        <picker class="form-picker" mode="selector" :range="genderOptions" :value="form.gender" @change="onGenderChange">
          <view class="picker-value">
            <text>{{ genderOptions[form.gender] }}</text>
            <text class="iconfont icon-arrow-right"></text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="form-label">学校</text>
        <input 
          class="form-input" 
          v-model="form.school" 
          placeholder="请输入学校名称"
          maxlength="50"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">个性签名</text>
        <textarea 
          class="form-textarea" 
          v-model="form.signature" 
          placeholder="请输入个性签名"
          maxlength="100"
        />
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="action-section">
      <view class="btn btn-lg btn-primary" @click="saveProfile">保存修改</view>
    </view>
  </view>
</template>

<script>
import userApi from '@/api/user.js'

export default {
  data() {
    return {
      userInfo: {},
      form: {
        nickname: '',
        phone: '',
        gender: 0,
        school: '',
        signature: ''
      },
      genderOptions: ['保密', '男', '女']
    }
  },
  onShow() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      try {
        const res = await userApi.getUserInfo()
        if (res.code === 200) {
          this.userInfo = res.data
          this.form = {
            nickname: res.data.nickname || '',
            phone: res.data.phone || '',
            gender: res.data.gender || 0,
            school: res.data.school || '',
            signature: res.data.signature || ''
          }
        }
      } catch (e) {
        console.error('加载用户信息失败', e)
      }
    },
    changeAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.uploadAvatar(res.tempFilePaths[0])
        }
      })
    },
    async uploadAvatar(filePath) {
      try {
        uni.showLoading({ title: '上传中...' })
        const res = await userApi.uploadAvatar(filePath)
        uni.hideLoading()
        if (res.code === 200) {
          this.userInfo.avatar = res.data.url
          uni.showToast({
            title: '上传成功',
            icon: 'success'
          })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
      }
    },
    onGenderChange(e) {
      this.form.gender = parseInt(e.detail.value)
    },
    async saveProfile() {
      if (!this.form.nickname.trim()) {
        uni.showToast({
          title: '请输入昵称',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({ title: '保存中...' })
        const res = await userApi.updateProfile(this.form)
        uni.hideLoading()
        if (res.code === 200) {
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({
          title: '保存失败',
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
  background: var(--color-bg);
  padding: var(--space-6);
}

.avatar-section {
  @include flex-column;
  align-items: center;
  padding: var(--space-10) 0;

  .avatar-wrapper {
    position: relative;
    width: 160rpx;
    height: 160rpx;
    border-radius: var(--radius-full);
    overflow: hidden;
    margin-bottom: var(--space-4);

    .avatar {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;

      &.avatar-gradient {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      .avatar-text {
        color: #fff;
        font-size: 80rpx;
        font-weight: bold;
      }
    }

    .avatar-mask {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.4);
      @include flex-center;
      opacity: 0;
      transition: opacity var(--duration-fast) var(--ease-out);

      .iconfont {
        font-size: var(--font-size-xl);
        color: var(--color-text-inverse);
      }
    }

    &:active .avatar-mask {
      opacity: 1;
    }
  }

  .avatar-tip {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
  }
}

.form-section {
  @include card-base;
  padding: var(--space-6);
  margin-bottom: var(--space-6);

  .form-item {
    margin-bottom: var(--space-6);

    &:last-child {
      margin-bottom: 0;
    }

    .form-label {
      display: block;
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
      margin-bottom: var(--space-2);
    }

    .form-input {
      width: 100%;
      height: 80rpx;
      padding: 0 var(--space-4);
      background: var(--color-bg-secondary);
      border: 2rpx solid transparent;
      border-radius: var(--radius-md);
      font-size: var(--font-size-base);
      color: var(--color-text-primary);
      transition: all var(--duration-fast) var(--ease-out);

      &:focus {
        background: var(--color-surface);
        border-color: var(--color-primary);
        box-shadow: 0 0 0 4rpx rgba(var(--color-primary-rgb), 0.1);
      }
    }

    .form-picker {
      width: 100%;

      .picker-value {
        @include flex-between;
        height: 80rpx;
        padding: 0 var(--space-4);
        background: var(--color-bg-secondary);
        border-radius: var(--radius-md);
        font-size: var(--font-size-base);
        color: var(--color-text-primary);

        .iconfont {
          font-size: var(--font-size-sm);
          color: var(--color-text-tertiary);
        }
      }
    }

    .form-textarea {
      width: 100%;
      min-height: 160rpx;
      padding: var(--space-4);
      background: var(--color-bg-secondary);
      border: 2rpx solid transparent;
      border-radius: var(--radius-md);
      font-size: var(--font-size-base);
      color: var(--color-text-primary);
      line-height: var(--line-height-relaxed);

      &:focus {
        background: var(--color-surface);
        border-color: var(--color-primary);
        box-shadow: 0 0 0 4rpx rgba(var(--color-primary-rgb), 0.1);
      }
    }
  }
}

.action-section {
  .btn {
    width: 100%;
  }
}
</style>
