<template>
  <view class="container">
    <!-- 顶部提示区域 -->
    <view class="header-tip">
      <text class="tip-icon">🛡️</text>
      <view class="tip-content">
        <text class="tip-title">实名认证</text>
        <text class="tip-desc">完成实名认证后可享受更多服务</text>
      </view>
    </view>

    <!-- 认证状态卡片 -->
    <view class="status-card" :class="statusClass">
      <view class="status-icon">{{ statusIcon }}</view>
      <view class="status-info">
        <text class="status-title">{{ statusTitle }}</text>
        <text class="status-desc">{{ statusDesc }}</text>
      </view>
    </view>

    <!-- 认证表单 -->
    <view v-if="canEdit" class="form-section">
      <view class="section-title">身份信息</view>
      
      <!-- 真实姓名 -->
      <view class="form-item">
        <text class="form-label required">真实姓名</text>
        <input
          class="form-input"
          v-model="form.realName"
          placeholder="请输入真实姓名"
          maxlength="20"
          :disabled="submitting"
        />
      </view>

      <!-- 身份证号 -->
      <view class="form-item">
        <text class="form-label required">身份证号</text>
        <input
          class="form-input"
          v-model="form.idCard"
          placeholder="请输入18位身份证号"
          maxlength="18"
          :disabled="submitting"
          @blur="validateIdCard"
        />
        <text v-if="idCardError" class="error-text">{{ idCardError }}</text>
      </view>

      <!-- 身份证照片上传 -->
      <view class="form-item">
        <text class="form-label required">身份证照片</text>
        <view class="upload-section">
          <!-- 正面 -->
          <view class="upload-item" @click="chooseImage('front')">
            <view v-if="!images.front" class="upload-placeholder">
              <text class="upload-icon">📷</text>
              <text class="upload-text">身份证正面</text>
              <text class="upload-hint">点击上传</text>
            </view>
            <image
              v-else
              class="upload-image"
              :src="images.front"
              mode="aspectFill"
            />
            <view v-if="images.front" class="delete-btn" @click.stop="deleteImage('front')">
              <text class="delete-icon">×</text>
            </view>
          </view>

          <!-- 反面 -->
          <view class="upload-item" @click="chooseImage('back')">
            <view v-if="!images.back" class="upload-placeholder">
              <text class="upload-icon">📷</text>
              <text class="upload-text">身份证反面</text>
              <text class="upload-hint">点击上传</text>
            </view>
            <image
              v-else
              class="upload-image"
              :src="images.back"
              mode="aspectFill"
            />
            <view v-if="images.back" class="delete-btn" @click.stop="deleteImage('back')">
              <text class="delete-icon">×</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <button
        class="submit-btn"
        :class="{ disabled: !canSubmit || submitting }"
        :disabled="!canSubmit || submitting"
        @click="handleSubmit"
      >
        <view v-if="submitting" class="loading-spinner"></view>
        <text>{{ submitting ? '提交中...' : '提交认证' }}</text>
      </button>
    </view>

    <!-- 认证信息展示（已通过状态） -->
    <view v-else-if="verifyStatus === 'verified'" class="info-section">
      <view class="info-item">
        <text class="info-label">真实姓名</text>
        <text class="info-value">{{ maskName(verifiedInfo.realName) }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">身份证号</text>
        <text class="info-value">{{ maskIdCard(verifiedInfo.idCard) }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">认证时间</text>
        <text class="info-value">{{ verifiedInfo.verifyTime }}</text>
      </view>
    </view>

    <!-- 审核中提示 -->
    <view v-else-if="verifyStatus === 'pending'" class="pending-section">
      <view class="pending-icon">⏳</view>
      <text class="pending-title">审核中</text>
      <text class="pending-desc">您的认证信息正在审核中，请耐心等待</text>
      <text class="pending-hint">预计审核时间：1-3个工作日</text>
    </view>

    <!-- 底部说明 -->
    <view class="footer-section">
      <view class="safe-tip">
        <text class="safe-icon">🔒</text>
        <text class="safe-text">您的信息将被加密保护，仅用于实名认证</text>
      </view>
    </view>
  </view>
</template>

<script>
import userApi from '@/api/user.js'

export default {
  data() {
    return {
      verifyStatus: 'unverified', // unverified, pending, verified, rejected
      verifiedInfo: {
        realName: '',
        idCard: '',
        verifyTime: ''
      },
      form: {
        realName: '',
        idCard: ''
      },
      images: {
        front: '',
        back: ''
      },
      idCardError: '',
      submitting: false,
      rejectReason: ''
    }
  },
  computed: {
    // 状态样式类
    statusClass() {
      const map = {
        unverified: 'status-unverified',
        pending: 'status-pending',
        verified: 'status-verified',
        rejected: 'status-rejected'
      }
      return map[this.verifyStatus] || 'status-unverified'
    },
    // 状态图标
    statusIcon() {
      const map = {
        unverified: '📝',
        pending: '⏳',
        verified: '✅',
        rejected: '❌'
      }
      return map[this.verifyStatus] || '📝'
    },
    // 状态标题
    statusTitle() {
      const map = {
        unverified: '未认证',
        pending: '审核中',
        verified: '已认证',
        rejected: '认证失败'
      }
      return map[this.verifyStatus] || '未认证'
    },
    // 状态描述
    statusDesc() {
      const map = {
        unverified: '请填写真实身份信息完成认证',
        pending: '您的认证信息正在审核中',
        verified: '您已完成实名认证，可享受全部服务',
        rejected: this.rejectReason || '认证失败，请检查信息后重新提交'
      }
      return map[this.verifyStatus] || ''
    },
    // 是否可以编辑
    canEdit() {
      return this.verifyStatus === 'unverified' || this.verifyStatus === 'rejected'
    },
    // 是否可以提交
    canSubmit() {
      return (
        this.form.realName.trim() &&
        this.form.idCard.trim() &&
        this.images.front &&
        this.images.back &&
        !this.idCardError
      )
    }
  },
  onShow() {
    this.loadVerifyStatus()
  },
  methods: {
    // 加载认证状态
    async loadVerifyStatus() {
      try {
        const res = await userApi.getVerifyStatus()
        if (res.code === 200) {
          const data = res.data
          this.verifyStatus = data.status || 'unverified'
          if (data.status === 'verified') {
            this.verifiedInfo = {
              realName: data.realName || '',
              idCard: data.idCard || '',
              verifyTime: data.verifyTime || ''
            }
          } else if (data.status === 'rejected') {
            this.rejectReason = data.reason || ''
          }
        }
      } catch (e) {
        console.error('加载认证状态失败', e)
      }
    },

    // 验证身份证号
    validateIdCard() {
      const idCard = this.form.idCard.trim()
      if (!idCard) {
        this.idCardError = ''
        return
      }

      // 身份证号格式验证
      const reg = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/
      if (!reg.test(idCard)) {
        this.idCardError = '身份证号格式不正确'
        return
      }

      // 校验码验证
      if (!this.checkIdCardCode(idCard)) {
        this.idCardError = '身份证号校验码错误'
        return
      }

      this.idCardError = ''
    },

    // 身份证号校验码验证
    checkIdCardCode(idCard) {
      const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
      const codes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
      let sum = 0
      for (let i = 0; i < 17; i++) {
        sum += parseInt(idCard[i]) * weights[i]
      }
      const code = codes[sum % 11]
      return code === idCard[17].toUpperCase()
    },

    // 选择图片
    chooseImage(type) {
      if (this.submitting) return

      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0]
          // 上传图片
          this.uploadImage(type, tempFilePath)
        }
      })
    },

    // 上传图片
    async uploadImage(type, filePath) {
      try {
        uni.showLoading({ title: '上传中...' })
        const res = await userApi.uploadIdCard(filePath)
        if (res.code === 200) {
          this.images[type] = res.data.url
          uni.showToast({
            title: '上传成功',
            icon: 'success'
          })
        } else {
          throw new Error(res.message || '上传失败')
        }
      } catch (e) {
        uni.showToast({
          title: e.message || '上传失败',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 删除图片
    deleteImage(type) {
      if (this.submitting) return
      uni.showModal({
        title: '提示',
        content: '确定删除该照片吗？',
        success: (res) => {
          if (res.confirm) {
            this.images[type] = ''
          }
        }
      })
    },

    // 提交认证
    async handleSubmit() {
      if (!this.canSubmit || this.submitting) return

      // 表单验证
      if (!this.form.realName.trim()) {
        uni.showToast({ title: '请输入真实姓名', icon: 'none' })
        return
      }

      if (!this.form.idCard.trim()) {
        uni.showToast({ title: '请输入身份证号', icon: 'none' })
        return
      }

      this.validateIdCard()
      if (this.idCardError) {
        uni.showToast({ title: this.idCardError, icon: 'none' })
        return
      }

      if (!this.images.front || !this.images.back) {
        uni.showToast({ title: '请上传身份证正反面照片', icon: 'none' })
        return
      }

      this.submitting = true

      try {
        const submitData = {
          realName: this.form.realName.trim(),
          idCard: this.form.idCard.trim().toUpperCase(),
          frontImage: this.images.front,
          backImage: this.images.back
        }

        const res = await userApi.submitVerify(submitData)

        if (res.code === 200) {
          uni.showToast({
            title: '提交成功',
            icon: 'success'
          })
          // 更新状态为审核中
          this.verifyStatus = 'pending'
          // 清空表单
          this.form = { realName: '', idCard: '' }
          this.images = { front: '', back: '' }
        } else {
          throw new Error(res.message || '提交失败')
        }
      } catch (e) {
        uni.showToast({
          title: e.message || '提交失败，请重试',
          icon: 'none',
          duration: 2000
        })
      } finally {
        this.submitting = false
      }
    },

    // 姓名脱敏
    maskName(name) {
      if (!name) return ''
      if (name.length <= 1) return '*'
      if (name.length === 2) return name[0] + '*'
      return name[0] + '*'.repeat(name.length - 2) + name[name.length - 1]
    },

    // 身份证号脱敏
    maskIdCard(idCard) {
      if (!idCard || idCard.length !== 18) return ''
      return idCard.slice(0, 3) + '*************' + idCard.slice(14)
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: var(--color-bg);
  padding: var(--space-6);
}

/* 顶部提示区域 */
.header-tip {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
}

.tip-icon {
  font-size: 48rpx;
  margin-right: var(--space-4);
}

.tip-content {
  display: flex;
  flex-direction: column;
}

.tip-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}

.tip-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

/* 状态卡片 */
.status-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-sm);
  border-left: 8rpx solid transparent;

  &.status-unverified {
    border-left-color: var(--color-text-tertiary);
  }

  &.status-pending {
    border-left-color: var(--color-warning);
  }

  &.status-verified {
    border-left-color: var(--color-success);
  }

  &.status-rejected {
    border-left-color: var(--color-error);
  }
}

.status-icon {
  font-size: 64rpx;
  margin-right: var(--space-5);
}

.status-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.status-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}

.status-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.5;
}

/* 表单区域 */
.form-section {
  background-color: #fff;
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-sm);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 2rpx solid var(--color-divider);
}

.form-item {
  margin-bottom: var(--space-6);

  &:last-child {
    margin-bottom: 0;
  }
}

.form-label {
  display: block;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  margin-bottom: var(--space-3);

  &.required::before {
    content: '*';
    color: var(--color-error);
    margin-right: var(--space-1);
  }
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 var(--space-4);
  background-color: var(--color-bg-secondary);
  border: 2rpx solid transparent;
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
  transition: all var(--duration-fast);

  &:focus {
    background-color: #fff;
    border-color: var(--color-primary);
  }

  &:disabled {
    opacity: 0.6;
  }
}

.error-text {
  display: block;
  font-size: var(--font-size-sm);
  color: var(--color-error);
  margin-top: var(--space-2);
}

/* 上传区域 */
.upload-section {
  display: flex;
  gap: var(--space-4);
}

.upload-item {
  flex: 1;
  aspect-ratio: 1.6;
  background-color: var(--color-bg-secondary);
  border: 2rpx dashed var(--color-border);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 48rpx;
  margin-bottom: var(--space-2);
}

.upload-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-1);
}

.upload-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.upload-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  width: 44rpx;
  height: 44rpx;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-icon {
  color: #fff;
  font-size: var(--font-size-lg);
  line-height: 1;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  margin-top: var(--space-8);
  box-shadow: var(--shadow-primary);

  text {
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
    color: #fff;
  }

  &:active {
    transform: scale(0.98);
  }

  &.disabled {
    opacity: 0.5;
    box-shadow: none;
  }
}

.loading-spinner {
  width: 36rpx;
  height: 36rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: var(--space-3);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 信息展示区域 */
.info-section {
  background-color: #fff;
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-sm);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) 0;
  border-bottom: 2rpx solid var(--color-divider);

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.info-value {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

/* 审核中区域 */
.pending-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-12) var(--space-6);
  background-color: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.pending-icon {
  font-size: 120rpx;
  margin-bottom: var(--space-6);
}

.pending-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-3);
}

.pending-desc {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-2);
}

.pending-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}

/* 底部区域 */
.footer-section {
  margin-top: var(--space-8);
}

.safe-tip {
  display: flex;
  align-items: center;
  justify-content: center;
}

.safe-icon {
  font-size: var(--font-size-sm);
  margin-right: var(--space-2);
}

.safe-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}
</style>
