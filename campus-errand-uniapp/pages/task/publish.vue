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
  padding: 20rpx;
  padding-bottom: 140rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 24rpx;
  }
}

.type-list {
  display: flex;
  flex-wrap: wrap;
  
  .type-item {
    width: calc(25% - 18rpx);
    margin-right: 24rpx;
    margin-bottom: 24rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24rpx 0;
    background: #f5f5f5;
    border-radius: 12rpx;
    
    &:nth-child(4n) {
      margin-right: 0;
    }
    
    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
    
    .iconfont {
      font-size: 48rpx;
      margin-bottom: 12rpx;
    }
    
    .type-name {
      font-size: 26rpx;
    }
  }
}

.form-item {
  margin-bottom: 24rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .label {
    display: block;
    font-size: 28rpx;
    color: #666;
    margin-bottom: 12rpx;
    
    &.required::after {
      content: '*';
      color: #ff4d4f;
      margin-left: 8rpx;
    }
  }
  
  input, textarea, .picker-value {
    width: 100%;
    height: 80rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #333;
  }
  
  textarea {
    height: 160rpx;
    padding: 24rpx;
  }
  
  .picker-value {
    display: flex;
    align-items: center;
    color: #999;
  }
  
  .input-with-unit {
    display: flex;
    align-items: center;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 0 24rpx;
    
    input {
      flex: 1;
      background: transparent;
      padding: 0;
    }
    
    .unit {
      font-size: 28rpx;
      color: #666;
      margin-left: 16rpx;
    }
  }
  
  .label-with-switch {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12rpx;
    
    .label {
      margin-bottom: 0;
    }
  }
}

.total-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24rpx;
  border-top: 1rpx solid #f0f0f0;
  
  .label {
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
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
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .amount-info {
    display: flex;
    align-items: center;
    
    .label {
      font-size: 28rpx;
      color: #666;
      margin-right: 16rpx;
    }
    
    .amount {
      font-size: 40rpx;
      font-weight: bold;
      color: #ff4d4f;
    }
  }
  
  .btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-size: 30rpx;
    padding: 24rpx 60rpx;
    border-radius: 40rpx;
    line-height: 1.5;
  }
}
</style>
