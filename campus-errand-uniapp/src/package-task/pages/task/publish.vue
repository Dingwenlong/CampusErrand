<template>
  <view class="publish-container">
    <!-- 主内容区 -->
    <scroll-view class="content-scroll" scroll-y>
      <!-- 任务类型选择卡片 -->
      <view class="type-card">
        <view class="card-header">
          <view class="header-icon">📦</view>
          <text class="header-title">选择任务类型</text>
        </view>
        <view class="type-grid">
          <view 
            v-for="(item, index) in taskTypes" 
            :key="index"
            class="type-item" 
            :class="{ active: form.taskType === item.value }"
            @click="selectType(item.value)"
          >
            <view class="type-icon-wrapper" :class="'bg-' + item.value">
              <text class="type-icon">{{ item.emoji }}</text>
            </view>
            <text class="type-name">{{ item.name }}</text>
            <view v-if="form.taskType === item.value" class="selected-mark">
              <text class="mark-icon">✓</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 任务信息卡片 -->
      <view class="form-card">
        <view class="card-header">
          <view class="header-icon">📝</view>
          <text class="header-title">任务信息</text>
        </view>
        <view class="form-body">
          <view class="input-group">
            <text class="group-label required">任务标题</text>
            <input 
              v-model="form.title" 
              class="group-input"
              placeholder="例如：帮忙取个快递到3号楼"
              maxlength="50"
            />
            <text class="input-count">{{ form.title.length }}/50</text>
          </view>
          <view class="input-group">
            <text class="group-label">任务描述</text>
            <textarea 
              v-model="form.description" 
              class="group-textarea"
              placeholder="请详细描述任务内容、物品信息、注意事项等..."
              maxlength="500"
            />
            <text class="input-count">{{ form.description.length }}/500</text>
          </view>
        </view>
      </view>

      <!-- 地址信息卡片 -->
      <view class="form-card">
        <view class="card-header">
          <view class="header-icon">📍</view>
          <text class="header-title">地址信息</text>
        </view>
        <view class="form-body">
          <!-- 取件地址 -->
          <view class="address-section">
            <view class="address-label">
              <view class="label-dot pickup"></view>
              <text class="label-text required">取件地址</text>
            </view>
            <input 
              v-model="form.pickupAddress" 
              class="address-input"
              placeholder="请输入取件地址"
            />
            <view class="contact-row">
              <input 
                v-model="form.pickupContact" 
                class="contact-input"
                placeholder="联系人"
              />
              <input 
                v-model="form.pickupPhone" 
                class="contact-input phone"
                placeholder="联系电话"
                type="number"
                maxlength="11"
              />
            </view>
          </view>

          <!-- 地址分隔线 -->
          <view class="address-divider">
            <view class="divider-line"></view>
            <view class="divider-arrow">↓</view>
            <view class="divider-line"></view>
          </view>

          <!-- 送达地址 -->
          <view class="address-section">
            <view class="address-label">
              <view class="label-dot delivery"></view>
              <text class="label-text required">送达地址</text>
            </view>
            <input 
              v-model="form.deliveryAddress" 
              class="address-input"
              placeholder="请输入送达地址"
            />
            <view class="contact-row">
              <input 
                v-model="form.deliveryContact" 
                class="contact-input"
                placeholder="联系人"
              />
              <input 
                v-model="form.deliveryPhone" 
                class="contact-input phone"
                placeholder="联系电话"
                type="number"
                maxlength="11"
              />
            </view>
          </view>
        </view>
      </view>

      <!-- 时间设置卡片 -->
      <view class="form-card">
        <view class="card-header">
          <view class="header-icon">⏰</view>
          <text class="header-title">时间设置</text>
        </view>
        <view class="form-body">
          <view class="time-item">
            <view class="time-label">
              <text class="label-icon">🎯</text>
              <text class="label-text">期望送达</text>
            </view>
            <picker mode="multiSelector" :value="expectTimeIndex" :range="timeRange" @change="onExpectTimeChange">
              <view class="time-picker" :class="{ placeholder: !form.expectTime }">
                <text>{{ form.expectTime || '请选择期望送达时间' }}</text>
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>
          <view class="time-item">
            <view class="time-label">
              <text class="label-icon">⏳</text>
              <text class="label-text">截止时间</text>
            </view>
            <picker mode="multiSelector" :value="deadlineTimeIndex" :range="timeRange" @change="onDeadlineTimeChange">
              <view class="time-picker" :class="{ placeholder: !form.deadlineTime }">
                <text>{{ form.deadlineTime || '请选择截止时间' }}</text>
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- 费用设置卡片 -->
      <view class="form-card">
        <view class="card-header">
          <view class="header-icon">💰</view>
          <text class="header-title">费用设置</text>
        </view>
        <view class="form-body">
          <!-- 基础赏金 -->
          <view class="fee-item">
            <view class="fee-label">
              <text class="label-text required">基础赏金</text>
              <text class="label-desc">跑腿员完成任务的报酬</text>
            </view>
            <view class="fee-input-wrapper">
              <text class="fee-symbol">¥</text>
              <input 
                v-model="form.reward" 
                class="fee-input"
                placeholder="0.00"
                type="digit"
              />
            </view>
          </view>

          <!-- 重量附加费 -->
          <view class="fee-item">
            <view class="fee-label">
              <text class="label-text">重量附加费</text>
              <text class="label-desc">大件物品额外费用</text>
            </view>
            <view class="fee-input-wrapper">
              <text class="fee-symbol">¥</text>
              <input 
                v-model="form.weightFee" 
                class="fee-input"
                placeholder="0.00"
                type="digit"
              />
            </view>
          </view>

          <!-- 加急费 -->
          <view class="fee-item urgent-item">
            <view class="fee-label">
              <view class="label-with-tag">
                <text class="label-text">加急服务</text>
                <view class="urgent-tag">⚡ 优先处理</view>
              </view>
            </view>
            <switch :checked="form.isUrgent === 1" @change="toggleUrgent" color="var(--color-primary)"/>
          </view>
          <view v-if="form.isUrgent === 1" class="fee-item urgent-fee">
            <view class="fee-input-wrapper full">
              <text class="fee-symbol">¥</text>
              <input 
                v-model="form.urgencyFee" 
                class="fee-input"
                placeholder="请输入加急费用"
                type="digit"
              />
            </view>
          </view>
        </view>
      </view>

      <!-- 备注卡片 -->
      <view class="form-card">
        <view class="card-header">
          <view class="header-icon">💬</view>
          <text class="header-title">备注说明</text>
        </view>
        <view class="form-body">
          <textarea 
            v-model="form.remark" 
            class="remark-textarea"
            placeholder="请输入其他备注信息（选填）"
            maxlength="200"
          />
          <text class="input-count">{{ form.remark.length }}/200</text>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 底部结算栏 -->
    <view class="bottom-bar">
      <view class="total-section">
        <text class="total-label">合计金额</text>
        <view class="total-price">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ calculateTotal }}</text>
        </view>
      </view>
      <button class="publish-btn" @click="handlePublish">
        <text class="btn-text">发布任务</text>
        <text class="btn-icon">→</text>
      </button>
    </view>

    <!-- 支付密码弹窗 -->
    <pay-password-modal 
      :show="showPasswordModal"
      @confirm="onPasswordConfirm"
      @cancel="showPasswordModal = false"
    />
  </view>
</template>

<script>
import taskApi from '@/api/task.js'
import PayPasswordModal from '@/components/pay-password-modal.vue'

export default {
  components: {
    PayPasswordModal
  },
  created() {
    const savedType = uni.getStorageSync('taskListType')
    if (savedType) {
      this.form.taskType = savedType
      uni.removeStorageSync('taskListType')
    }
  },
  data() {
    return {
      taskTypes: [
        { name: '取快递', value: 1, emoji: '📦', icon: 'icon-express' },
        { name: '代买', value: 2, emoji: '🛒', icon: 'icon-shopping' },
        { name: '送件', value: 3, emoji: '📄', icon: 'icon-delivery' },
        { name: '其他', value: 4, emoji: '✨', icon: 'icon-other' }
      ],
      timeBufferMinutes: 15,
      form: {
        taskType: 1,
        title: '',
        description: '',
        pickupAddress: '',
        pickupContact: '',
        pickupPhone: '',
        deliveryAddress: '',
        deliveryContact: '',
        deliveryPhone: '',
        expectTime: '',
        deadlineTime: '',
        reward: '',
        weightFee: '',
        urgencyFee: '',
        remark: '',
        isUrgent: 0
      },
      expectTimeIndex: [0, 0],
      deadlineTimeIndex: [0, 0],
      timeRange: [
        ['今天', '明天', '后天'],
        ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00']
      ],
      showPasswordModal: false
    }
  },
  computed: {
    calculateTotal() {
      let total = 0
      if (this.form.reward) {
        total += parseFloat(this.form.reward)
      }
      if (this.form.weightFee) {
        total += parseFloat(this.form.weightFee)
      }
      if (this.form.isUrgent === 1 && this.form.urgencyFee) {
        total += parseFloat(this.form.urgencyFee)
      }
      return total.toFixed(2)
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    
    selectType(value) {
      this.form.taskType = value
    },
    
    toggleUrgent(e) {
      this.form.isUrgent = e.detail.value ? 1 : 0
      if (!e.detail.value) {
        this.form.urgencyFee = ''
      }
    },
    
    onExpectTimeChange(e) {
      const dateIndex = e.detail.value[0]
      const timeIndex = e.detail.value[1]
      const date = this.timeRange[0][dateIndex]
      const time = this.timeRange[1][timeIndex]
      
      const dateMap = {
        '今天': this.getToday(),
        '明天': this.getTomorrow(),
        '后天': this.getDayAfterTomorrow()
      }
      
      this.form.expectTime = dateMap[date] + ' ' + time + ':00'
    },
    
    onDeadlineTimeChange(e) {
      const dateIndex = e.detail.value[0]
      const timeIndex = e.detail.value[1]
      const date = this.timeRange[0][dateIndex]
      const time = this.timeRange[1][timeIndex]
      
      const dateMap = {
        '今天': this.getToday(),
        '明天': this.getTomorrow(),
        '后天': this.getDayAfterTomorrow()
      }
      
      this.form.deadlineTime = dateMap[date] + ' ' + time + ':00'
    },
    
    getToday() {
      const date = new Date()
      return this.formatDate(date)
    },
    
    getTomorrow() {
      const date = new Date()
      date.setDate(date.getDate() + 1)
      return this.formatDate(date)
    },
    
    getDayAfterTomorrow() {
      const date = new Date()
      date.setDate(date.getDate() + 2)
      return this.formatDate(date)
    },
    
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    
    validateForm() {
      if (!this.form.title.trim()) {
        uni.showToast({ title: '请输入任务标题', icon: 'none' })
        return false
      }
      if (!this.form.pickupAddress.trim()) {
        uni.showToast({ title: '请输入取件地址', icon: 'none' })
        return false
      }
      if (!this.form.deliveryAddress.trim()) {
        uni.showToast({ title: '请输入送达地址', icon: 'none' })
        return false
      }
      if (!this.form.reward || parseFloat(this.form.reward) <= 0) {
        uni.showToast({ title: '请输入基础赏金', icon: 'none' })
        return false
      }
      if (!this.validateTime()) {
        return false
      }
      return true
    },
    
    validateTime() {
      const now = new Date()
      const publishTime = now.getTime()
      
      if (this.form.expectTime) {
        const expectTime = new Date(this.form.expectTime.replace(' ', 'T')).getTime()
        
        if (expectTime <= publishTime) {
          uni.showToast({ title: '期望送达时间必须晚于当前时间', icon: 'none' })
          return false
        }
        
        if (this.form.deadlineTime) {
          const deadlineTime = new Date(this.form.deadlineTime.replace(' ', 'T')).getTime()
          
          if (deadlineTime <= publishTime) {
            uni.showToast({ title: '截止时间必须晚于当前时间', icon: 'none' })
            return false
          }
          
          if (deadlineTime >= expectTime) {
            uni.showToast({ title: '截止时间必须早于期望送达时间', icon: 'none' })
            return false
          }
          
          const bufferMillis = this.timeBufferMinutes * 60 * 1000
          const diff = expectTime - deadlineTime
          if (diff < bufferMillis) {
            uni.showToast({ title: `截止时间与期望送达时间至少间隔${this.timeBufferMinutes}分钟`, icon: 'none' })
            return false
          }
        }
      }
      
      return true
    },
    
    handlePublish() {
      if (!this.validateForm()) return
      
      this.showPasswordModal = true
    },
    
    async onPasswordConfirm(password) {
      this.showPasswordModal = false
      
      const data = {
        ...this.form,
        reward: parseFloat(this.form.reward),
        weightFee: this.form.weightFee ? parseFloat(this.form.weightFee) : 0,
        urgencyFee: this.form.urgencyFee ? parseFloat(this.form.urgencyFee) : 0,
        payPassword: password
      }
      
      try {
        uni.showLoading({ title: '发布中...' })
        const res = await taskApi.publish(data)
        uni.hideLoading()
        
        if (res.code === 200) {
          uni.showToast({
            title: '发布成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.$emit('taskPublished')
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.message || '发布失败',
            icon: 'none'
          })
        }
      } catch (e) {
        uni.hideLoading()
        console.error('发布任务失败:', e)
        const errorMsg = e?.message || e?.data?.message || '发布失败'
        uni.showToast({
          title: errorMsg,
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
// 去除 input/textarea focus 时的 outline
input, textarea {
  outline: none;
  
  &:focus {
    outline: none;
  }
}

// 设计变量
$primary: #FF6B35;
$primary-light: #FF8C5A;
$primary-dark: #E85A2A;
$text-primary: #4A3728;
$text-secondary: #7A6B5E;
$text-tertiary: #A89888;
$bg-primary: #FFF5EB;
$bg-card: var(--color-surface);
$border-color: #F0E0D0;
$shadow-sm: 0 2rpx 12rpx rgba(74, 55, 40, 0.06);
$shadow-md: 0 4rpx 20rpx rgba(74, 55, 40, 0.08);
$shadow-lg: 0 8rpx 32rpx rgba(74, 55, 40, 0.12);
$shadow-focus: 0 4rpx 16rpx rgba(255, 107, 53, 0.2);
$radius-sm: 12rpx;
$radius-md: 20rpx;
$radius-lg: 28rpx;

.publish-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8F0 0%, #FFF5EB 200rpx);
}

// 内容滚动区
.content-scroll {
  height: 100vh;
  padding: 0 24rpx;
}

// 卡片通用样式
.type-card, .form-card {
  background: $bg-card;
  border-radius: $radius-lg;
  margin-bottom: 24rpx;
  box-shadow: $shadow-sm;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  padding: 28rpx 28rpx 20rpx;
  border-bottom: 2rpx solid $border-color;
  
  .header-icon {
    font-size: 40rpx;
    margin-right: 16rpx;
  }
  
  .header-title {
    font-size: 32rpx;
    font-weight: 700;
    color: $text-primary;
  }
}

// 任务类型选择
.type-grid {
  display: flex;
  padding: 28rpx;
  gap: 20rpx;
}

.type-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 16rpx;
  background: $bg-primary;
  border-radius: $radius-md;
  border: 3rpx solid transparent;
  position: relative;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
  }
  
  &.active {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 140, 90, 0.1) 100%);
    border-color: $primary;
    
    .type-name {
      color: $primary-dark;
      font-weight: 600;
    }
  }
  
  .type-icon-wrapper {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16rpx;
    
    &.bg-1 { background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%); }
    &.bg-2 { background: linear-gradient(135deg, #7BC47F 0%, #9DD9A0 100%); }
    &.bg-3 { background: linear-gradient(135deg, #FFB347 0%, #FFC970 100%); }
    &.bg-4 { background: linear-gradient(135deg, #FFB088 0%, #FFC4A8 100%); }
  }
  
  .type-icon {
    font-size: 44rpx;
  }
  
  .type-name {
    font-size: 26rpx;
    color: $text-secondary;
  }
  
  .selected-mark {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    width: 32rpx;
    height: 32rpx;
    background: $primary;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .mark-icon {
      font-size: 20rpx;
      color: var(--color-white);
      font-weight: bold;
    }
  }
}

// 表单内容区
.form-body {
  padding: 28rpx;
}

// 输入组
.input-group {
  margin-bottom: 28rpx;
  position: relative;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .group-label {
    display: block;
    font-size: 28rpx;
    color: $text-secondary;
    margin-bottom: 16rpx;
    font-weight: 500;
    
    &.required::after {
      content: '*';
      color: var(--color-error);
      margin-left: 8rpx;
    }
  }
  
  .group-input, .group-textarea {
    width: 100%;
    background: $bg-primary;
    border-radius: $radius-md;
    padding: 24rpx;
    font-size: 28rpx;
    color: $text-primary;
    border: 2rpx solid transparent;
    transition: all 0.3s ease;
    box-sizing: border-box;
    
    &:focus {
      border-color: $primary;
      background: var(--color-surface);
      box-shadow: $shadow-focus;
      transform: translateY(-2rpx);
    }
    
    &::placeholder {
      color: $text-tertiary;
    }
  }
  
  .group-input {
    height: 96rpx;
    line-height: 48rpx;
  }
  
  .group-textarea {
    height: 200rpx;
    padding-bottom: 48rpx;
  }
  
  .input-count {
    position: absolute;
    bottom: 20rpx;
    right: 20rpx;
    font-size: 22rpx;
    color: $text-tertiary;
    background: rgba(255, 255, 255, 0.8);
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
  }
}

// 地址区域
.address-section {
  margin-bottom: 28rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.address-label {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  
  .label-dot {
    width: 24rpx;
    height: 24rpx;
    border-radius: 50%;
    margin-right: 12rpx;
    
    &.pickup { background: #4CAF50; }
    &.delivery { background: #FF5722; }
  }
  
  .label-text {
    font-size: 28rpx;
    color: $text-secondary;
    font-weight: 500;
    
    &.required::after {
      content: '*';
      color: var(--color-error);
      margin-left: 8rpx;
    }
  }
}

.address-input {
  width: 100%;
  height: 96rpx;
  background: $bg-primary;
  border-radius: $radius-md;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: $text-primary;
  margin-bottom: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  box-sizing: border-box;
  
  &:focus {
    border-color: $primary;
    background: var(--color-surface);
    box-shadow: $shadow-focus;
    transform: translateY(-2rpx);
  }
  
  &::placeholder {
    color: $text-tertiary;
  }
}

.contact-row {
  display: flex;
  gap: 16rpx;
}

.contact-input {
  flex: 1;
  height: 88rpx;
  background: $bg-primary;
  border-radius: $radius-md;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: $text-primary;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  box-sizing: border-box;
  
  &.phone {
    flex: 1.5;
  }
  
  &:focus {
    border-color: $primary;
    background: var(--color-surface);
    box-shadow: $shadow-focus;
    transform: translateY(-2rpx);
  }
  
  &::placeholder {
    color: $text-tertiary;
  }
}

// 地址分隔线
.address-divider {
  display: flex;
  align-items: center;
  padding: 16rpx 0 32rpx;
  
  .divider-line {
    flex: 1;
    height: 2rpx;
    background: linear-gradient(90deg, transparent, $border-color, transparent);
  }
  
  .divider-arrow {
    width: 48rpx;
    height: 48rpx;
    background: $bg-primary;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 20rpx;
    font-size: 24rpx;
    color: $text-tertiary;
  }
}

// 时间选择
.time-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 2rpx solid $border-color;
  
  &:last-child {
    border-bottom: none;
  }
}

.time-label {
  display: flex;
  align-items: center;
  
  .label-icon {
    font-size: 32rpx;
    margin-right: 16rpx;
  }
  
  .label-text {
    font-size: 28rpx;
    color: $text-primary;
  }
}

.time-picker {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: $text-primary;
  
  &.placeholder {
    color: $text-tertiary;
  }
  
  .picker-arrow {
    font-size: 32rpx;
    color: $text-tertiary;
    margin-left: 12rpx;
  }
}

// 费用设置
.fee-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 2rpx solid $border-color;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.urgent-item {
    padding: 16rpx 0;
  }
  
  &.urgent-fee {
    padding-top: 0;
    padding-bottom: 24rpx;
  }
}

.fee-label {
  .label-text {
    display: block;
    font-size: 28rpx;
    color: $text-primary;
    margin-bottom: 8rpx;
    font-weight: 500;
    
    &.required::after {
      content: '*';
      color: var(--color-error);
      margin-left: 8rpx;
    }
  }
  
  .label-desc {
    font-size: 24rpx;
    color: $text-tertiary;
  }
  
  .label-with-tag {
    display: flex;
    align-items: center;
    gap: 16rpx;
    
    .urgent-tag {
      padding: 6rpx 16rpx;
      background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
      border-radius: 20rpx;
      font-size: 22rpx;
      color: #FF9800;
      font-weight: 500;
    }
  }
}

.fee-input-wrapper {
  display: flex;
  align-items: center;
  background: $bg-primary;
  border-radius: $radius-md;
  padding: 16rpx 24rpx;
  min-width: 180rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  
  &:focus-within {
    border-color: $primary;
    background: var(--color-surface);
    box-shadow: $shadow-focus;
    transform: translateY(-2rpx);
  }
  
  &.full {
    width: 100%;
    margin-top: 16rpx;
  }
  
  .fee-symbol {
    font-size: 28rpx;
    color: $primary;
    margin-right: 8rpx;
    font-weight: 600;
  }
  
  .fee-input {
    flex: 1;
    font-size: 32rpx;
    color: $text-primary;
    font-weight: 600;
    text-align: right;
    background: transparent;
    border: none;
    padding: 0;
    
    &::placeholder {
      color: $text-tertiary;
      font-weight: 400;
    }
  }
}

// 备注
.remark-textarea {
  width: 100%;
  height: 180rpx;
  background: $bg-primary;
  border-radius: $radius-md;
  padding: 24rpx;
  padding-bottom: 48rpx;
  font-size: 28rpx;
  color: $text-primary;
  border: 2rpx solid transparent;
  margin-bottom: 16rpx;
  transition: all 0.3s ease;
  box-sizing: border-box;
  
  &:focus {
    border-color: $primary;
    background: var(--color-surface);
    box-shadow: $shadow-focus;
    transform: translateY(-2rpx);
  }
  
  &::placeholder {
    color: $text-tertiary;
  }
}

// 底部占位
.bottom-placeholder {
  height: 160rpx;
}

// 底部结算栏
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--color-surface);
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4rpx 24rpx rgba(74, 55, 40, 0.08);
  z-index: 100;
}

.total-section {
  .total-label {
    display: block;
    font-size: 24rpx;
    color: $text-tertiary;
    margin-bottom: 8rpx;
  }
  
  .total-price {
    display: flex;
    align-items: baseline;
    
    .price-symbol {
      font-size: 28rpx;
      color: var(--color-error);
      font-weight: 600;
      margin-right: 4rpx;
    }
    
    .price-value {
      font-size: 48rpx;
      color: var(--color-error);
      font-weight: 700;
    }
  }
}

.publish-btn {
  display: flex;
  align-items: center;
  padding: 24rpx 48rpx;
  background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
  border-radius: 44rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.35);
  border: none;
  
  &:active {
    transform: scale(0.96);
    box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.25);
  }
  
  .btn-text {
    font-size: 30rpx;
    color: #fff;
    font-weight: 600;
    margin-right: 12rpx;
  }
  
  .btn-icon {
    font-size: 28rpx;
    color: #fff;
  }
}
</style>

