<template>
  <view class="container">
    <!-- 任务类型选择 -->
    <view class="section">
      <view class="section-title">任务类型</view>
      <view class="type-list">
        <view 
          class="type-item" 
          v-for="(item, index) in taskTypes" 
          :key="index"
          :class="{ active: form.taskType === item.value }"
          @click="selectType(item.value)"
        >
          <text class="iconfont" :class="item.icon"></text>
          <text class="type-name">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <!-- 任务信息 -->
    <view class="section">
      <view class="section-title">任务信息</view>
      <view class="form-item">
        <text class="label">任务标题</text>
        <input 
          v-model="form.title" 
          placeholder="请输入任务标题，如：帮忙取快递"
          maxlength="50"
        />
      </view>
      <view class="form-item">
        <text class="label">任务描述</text>
        <textarea 
          v-model="form.description" 
          placeholder="请详细描述任务内容..."
          maxlength="500"
        />
      </view>
    </view>

    <!-- 取件地址 -->
    <view class="section">
      <view class="section-title">取件地址</view>
      <view class="form-item">
        <text class="label required">取件地址</text>
        <input 
          v-model="form.pickupAddress" 
          placeholder="请输入取件地址"
        />
      </view>
      <view class="form-item">
        <text class="label">联系人</text>
        <input 
          v-model="form.pickupContact" 
          placeholder="请输入联系人姓名"
        />
      </view>
      <view class="form-item">
        <text class="label">联系电话</text>
        <input 
          v-model="form.pickupPhone" 
          placeholder="请输入联系电话"
          type="number"
          maxlength="11"
        />
      </view>
    </view>

    <!-- 送达地址 -->
    <view class="section">
      <view class="section-title">送达地址</view>
      <view class="form-item">
        <text class="label required">送达地址</text>
        <input 
          v-model="form.deliveryAddress" 
          placeholder="请输入送达地址"
        />
      </view>
      <view class="form-item">
        <text class="label">联系人</text>
        <input 
          v-model="form.deliveryContact" 
          placeholder="请输入联系人姓名"
        />
      </view>
      <view class="form-item">
        <text class="label">联系电话</text>
        <input 
          v-model="form.deliveryPhone" 
          placeholder="请输入联系电话"
          type="number"
          maxlength="11"
        />
      </view>
    </view>

    <!-- 时间设置 -->
    <view class="section">
      <view class="section-title">时间设置</view>
      <view class="form-item">
        <text class="label">期望送达时间</text>
        <picker mode="multiSelector" :value="expectTimeIndex" :range="timeRange" @change="onExpectTimeChange">
          <view class="picker-value">{{ form.expectTime || '请选择期望送达时间' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">截止时间</text>
        <picker mode="multiSelector" :value="deadlineTimeIndex" :range="timeRange" @change="onDeadlineTimeChange">
          <view class="picker-value">{{ form.deadlineTime || '请选择截止时间' }}</view>
        </picker>
      </view>
    </view>

    <!-- 费用设置 -->
    <view class="section">
      <view class="section-title">费用设置</view>
      <view class="form-item">
        <text class="label required">基础赏金</text>
        <view class="input-with-unit">
          <input 
            v-model="form.reward" 
            placeholder="请输入赏金金额"
            type="digit"
          />
          <text class="unit">元</text>
        </view>
      </view>
      <view class="form-item">
        <text class="label">重量附加费</text>
        <view class="input-with-unit">
          <input 
            v-model="form.weightFee" 
            placeholder="0.00"
            type="digit"
          />
          <text class="unit">元</text>
        </view>
      </view>
      <view class="form-item">
        <view class="label-with-switch">
          <text class="label">加急</text>
          <switch :checked="form.isUrgent === 1" @change="toggleUrgent" color="#667eea"/>
        </view>
        <view class="input-with-unit" v-if="form.isUrgent === 1">
          <input 
            v-model="form.urgencyFee" 
            placeholder="0.00"
            type="digit"
          />
          <text class="unit">元</text>
        </view>
      </view>
      <view class="total-amount">
        <text class="label">总金额</text>
        <text class="amount">¥{{ calculateTotal }}</text>
      </view>
    </view>

    <!-- 备注 -->
    <view class="section">
      <view class="section-title">备注</view>
      <view class="form-item">
        <textarea 
          v-model="form.remark" 
          placeholder="请输入备注信息（选填）"
          maxlength="200"
        />
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <view class="amount-info">
        <text class="label">合计</text>
        <text class="amount">¥{{ calculateTotal }}</text>
      </view>
      <button class="btn-primary" @click="handlePublish">发布任务</button>
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
  data() {
    return {
      taskTypes: [
        { name: '取快递', value: 1, icon: 'icon-express' },
        { name: '代买', value: 2, icon: 'icon-shopping' },
        { name: '送件', value: 3, icon: 'icon-delivery' },
        { name: '其他', value: 4, icon: 'icon-other' }
      ],
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
      return true
    },
    
    handlePublish() {
      if (!this.validateForm()) return
      
      // 显示支付密码弹窗
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
        uni.showToast({
          title: '发布失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  padding: var(--spacing-md);
  padding-bottom: 140rpx;
  background: var(--bg-color);
  min-height: 100vh;
}

.section {
  background: var(--card-bg);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20rpx);
  
  &:nth-child(1) {
    animation-delay: 0.1s;
  }
  
  &:nth-child(2) {
    animation-delay: 0.2s;
  }
  
  &:nth-child(3) {
    animation-delay: 0.3s;
  }
  
  &:nth-child(4) {
    animation-delay: 0.4s;
  }
  
  &:nth-child(5) {
    animation-delay: 0.5s;
  }
  
  &:nth-child(6) {
    animation-delay: 0.6s;
  }
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: var(--spacing-md);
    padding-bottom: var(--spacing-sm);
    border-bottom: 2rpx solid var(--border-color);
  }
}

.type-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  
  .type-item {
    flex: 1;
    min-width: 140rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: var(--spacing-md);
    background: var(--bg-color);
    border-radius: var(--border-radius-md);
    border: 2rpx solid transparent;
    transition: all 0.3s ease;
    
    &.active {
      background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-light) 100%);
      color: #fff;
      transform: scale(1.05);
      box-shadow: var(--shadow-md);
      
      .type-name {
        color: #fff;
      }
    }
    
    &:active {
      transform: scale(0.95);
    }
    
    .iconfont {
      font-size: 48rpx;
      margin-bottom: var(--spacing-xs);
    }
    
    .type-name {
      font-size: 26rpx;
      color: var(--text-primary);
      font-weight: 500;
    }
  }
}

.form-item {
  margin-bottom: var(--spacing-md);
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .label {
    display: block;
    font-size: 28rpx;
    color: var(--text-secondary);
    margin-bottom: var(--spacing-sm);
    font-weight: 500;
    
    &.required::after {
      content: '*';
      color: #ff4d4f;
      margin-left: 8rpx;
    }
  }
  
  input, textarea, .picker-value {
    width: 100%;
    height: 80rpx;
    background: var(--bg-color);
    border-radius: var(--border-radius-sm);
    padding: 0 var(--spacing-md);
    font-size: 28rpx;
    color: var(--text-primary);
    border: 2rpx solid transparent;
    transition: all 0.3s ease;
    
    &:focus {
      border-color: var(--primary-color);
      background: #fff;
    }
  }
  
  textarea {
    height: 160rpx;
    padding: var(--spacing-md);
  }
  
  .picker-value {
    display: flex;
    align-items: center;
    color: var(--text-secondary);
  }
  
  .input-with-unit {
    display: flex;
    align-items: center;
    background: var(--bg-color);
    border-radius: var(--border-radius-sm);
    padding: 0 var(--spacing-md);
    border: 2rpx solid transparent;
    transition: all 0.3s ease;
    
    &:focus-within {
      border-color: var(--primary-color);
      background: #fff;
    }
    
    input {
      flex: 1;
      background: transparent;
      padding: 0;
      border: none;
    }
    
    .unit {
      font-size: 28rpx;
      color: var(--text-secondary);
      margin-left: var(--spacing-sm);
      font-weight: 500;
    }
  }
  
  .label-with-switch {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-sm);
    
    .label {
      margin-bottom: 0;
    }
  }
}

.total-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--spacing-md);
  border-top: 2rpx solid var(--border-color);
  margin-top: var(--spacing-md);
  
  .label {
    font-size: 30rpx;
    color: var(--text-primary);
    font-weight: 600;
  }
  
  .amount {
    font-size: 40rpx;
    font-weight: bold;
    color: #ff4d4f;
  }
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: var(--spacing-md) var(--spacing-lg);
  padding-bottom: calc(var(--spacing-md) + constant(safe-area-inset-bottom));
  padding-bottom: calc(var(--spacing-md) + env(safe-area-inset-bottom));
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
  z-index: 100;
  
  .amount-info {
    .label {
      display: block;
      font-size: 24rpx;
      color: var(--text-secondary);
      margin-bottom: var(--spacing-xs);
    }
    
    .amount {
      font-size: 44rpx;
      font-weight: bold;
      color: #ff4d4f;
    }
  }
  
  .btn-primary {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-light) 100%);
    color: #fff;
    font-size: 30rpx;
    padding: 24rpx 60rpx;
    border-radius: 40rpx;
    line-height: 1.5;
    font-weight: 600;
    box-shadow: var(--shadow-md);
    transition: all 0.3s ease;
    
    &:active {
      transform: scale(0.95);
      box-shadow: var(--shadow-sm);
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
