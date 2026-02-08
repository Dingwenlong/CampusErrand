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
      <button class="btn-primary" @click="publishTask">发布任务</button>
    </view>
  </view>
</template>

<script>
import taskApi from '@/api/task.js'

export default {
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
      ]
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
    
    async publishTask() {
      if (!this.validateForm()) return
      
      const data = {
        ...this.form,
        reward: parseFloat(this.form.reward),
        weightFee: this.form.weightFee ? parseFloat(this.form.weightFee) : 0,
        urgencyFee: this.form.urgencyFee ? parseFloat(this.form.urgencyFee) : 0
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
  
  &:active {
    box-shadow: var(--shadow-md);
    transform: translateY(-2rpx);
  }
  
  .section-title {
    font-size: var(--font-size-lg);
    font-weight: bold;
    color: var(--text-primary);
    margin-bottom: var(--spacing-md);
  }
}

.type-list {
  display: flex;
  flex-wrap: wrap;
  
  .type-item {
    width: calc(25% - 18rpx);
    margin-right: var(--spacing-md);
    margin-bottom: var(--spacing-md);
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: var(--spacing-md) 0;
    background: var(--bg-color);
    border-radius: var(--border-radius-sm);
    transition: all 0.3s ease;
    
    &:nth-child(4n) {
      margin-right: 0;
    }
    
    &:active {
      background: var(--border-color);
      transform: scale(0.95);
    }
    
    &.active {
      background: var(--primary-color);
      color: white;
      box-shadow: var(--shadow-sm);
    }
    
    .iconfont {
      font-size: 48rpx;
      margin-bottom: var(--spacing-xs);
      transition: all 0.3s ease;
    }
    
    &.active .iconfont {
      transform: scale(1.1);
    }
    
    .type-name {
      font-size: var(--font-size-sm);
    }
  }
}

.form-item {
  margin-bottom: var(--spacing-md);
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(10rpx);
  
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
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .label {
    display: block;
    font-size: var(--font-size-base);
    color: var(--text-secondary);
    margin-bottom: var(--spacing-xs);
    font-weight: 500;
    
    &.required::after {
      content: '*';
      color: var(--error-color);
      margin-left: var(--spacing-xs);
    }
  }
  
  input, textarea, .picker-value {
    width: 100%;
    height: 80rpx;
    background: var(--bg-color);
    border-radius: var(--border-radius-sm);
    padding: 0 var(--spacing-md);
    font-size: var(--font-size-base);
    color: var(--text-primary);
    transition: all 0.3s ease;
    border: 2rpx solid transparent;
  }
  
  input:focus, textarea:focus {
    border-color: var(--primary-color);
    box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.1);
    transform: translateY(-2rpx);
  }
  
  textarea {
    height: 160rpx;
    padding: var(--spacing-md);
    resize: vertical;
  }
  
  .picker-value {
    display: flex;
    align-items: center;
    color: var(--text-tertiary);
    border: 2rpx solid transparent;
    
    &:active {
      border-color: var(--primary-color);
      background: rgba(0, 122, 255, 0.05);
    }
  }
  
  .input-with-unit {
    display: flex;
    align-items: center;
    background: var(--bg-color);
    border-radius: var(--border-radius-sm);
    padding: 0 var(--spacing-md);
    transition: all 0.3s ease;
    border: 2rpx solid transparent;
    
    &:focus-within {
      border-color: var(--primary-color);
      box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.1);
      transform: translateY(-2rpx);
    }
    
    input {
      flex: 1;
      background: transparent;
      padding: 0;
      border: none;
    }
    
    .unit {
      font-size: var(--font-size-base);
      color: var(--text-secondary);
      margin-left: var(--spacing-sm);
    }
  }
  
  .label-with-switch {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-xs);
    
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
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(10rpx);
  animation-delay: 0.5s;
  
  .label {
    font-size: var(--font-size-lg);
    color: var(--text-primary);
    font-weight: 500;
  }
  
  .amount {
    font-size: var(--font-size-xl);
    font-weight: bold;
    color: var(--error-color);
  }
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--card-bg);
  padding: var(--spacing-md) var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  border-top: 2rpx solid var(--border-color);
  animation: slideUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(100%);
  animation-delay: 0.6s;
  
  .amount-info {
    display: flex;
    align-items: center;
    
    .label {
      font-size: var(--font-size-base);
      color: var(--text-secondary);
      margin-right: var(--spacing-sm);
    }
    
    .amount {
      font-size: var(--font-size-xl);
      font-weight: bold;
      color: var(--error-color);
    }
  }
  
  .btn-primary {
    background: var(--primary-color);
    color: white;
    font-size: var(--font-size-base);
    padding: var(--spacing-sm) var(--spacing-xl);
    border-radius: var(--border-radius-lg);
    line-height: 1.5;
    transition: all 0.2s ease;
    border: none;
    outline: none;
    box-shadow: var(--shadow-sm);
    
    &:active {
      background-color: var(--primary-active);
      transform: scale(0.98);
      box-shadow: var(--shadow-md);
    }
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
