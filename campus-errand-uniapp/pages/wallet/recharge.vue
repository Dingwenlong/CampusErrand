<template>
  <view class="container">
    <!-- 充值金额选择 -->
    <view class="amount-section">
      <view class="section-title">选择充值金额</view>
      <view class="amount-grid">
        <view 
          class="amount-item" 
          v-for="(item, index) in amountList" 
          :key="index"
          :class="{ active: selectedAmount === item }"
          @click="selectAmount(item)"
        >
          <text class="amount-num">{{ item }}</text>
          <text class="amount-unit">元</text>
        </view>
      </view>
      <view class="custom-amount">
        <text class="label">自定义金额</text>
        <view class="input-wrapper">
          <text class="currency">¥</text>
          <input 
            v-model="customAmount" 
            type="digit" 
            placeholder="请输入充值金额"
            @focus="selectedAmount = null"
          />
        </view>
      </view>
    </view>

    <!-- 支付方式 -->
    <view class="payment-section">
      <view class="section-title">支付方式</view>
      <view class="payment-item">
        <view class="payment-info">
          <image src="/static/icons/balance.png" class="payment-icon"></image>
          <view class="payment-detail">
            <text class="payment-name">虚拟充值</text>
            <text class="payment-desc">直接增加账户余额</text>
          </view>
        </view>
        <radio :checked="true" color="#667eea" />
      </view>
    </view>

    <!-- 充值说明 -->
    <view class="tips-section">
      <view class="tip-item">
        <text class="dot"></text>
        <text class="tip-text">充值金额将直接存入您的账户余额</text>
      </view>
      <view class="tip-item">
        <text class="dot"></text>
        <text class="tip-text">余额可用于支付任务赏金</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <view class="total-amount">
        <text class="label">充值金额</text>
        <text class="amount">¥{{ finalAmount }}</text>
      </view>
      <button class="btn-primary" @click="handleRecharge">立即充值</button>
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
import walletApi from '@/api/wallet.js'
import PayPasswordModal from '@/components/pay-password-modal.vue'

export default {
  components: {
    PayPasswordModal
  },
  data() {
    return {
      amountList: ['10', '20', '50', '100', '200', '500'],
      selectedAmount: '50',
      customAmount: '',
      showPasswordModal: false,
      pendingAmount: 0
    }
  },
  computed: {
    finalAmount() {
      if (this.selectedAmount) {
        return this.selectedAmount
      }
      return this.customAmount || '0'
    }
  },
  methods: {
    selectAmount(amount) {
      this.selectedAmount = amount
      this.customAmount = ''
    },
    
    handleRecharge() {
      const amount = parseFloat(this.finalAmount)
      if (!amount || amount <= 0) {
        uni.showToast({
          title: '请选择或输入充值金额',
          icon: 'none'
        })
        return
      }
      
      this.pendingAmount = amount
      this.showPasswordModal = true
    },
    
    async onPasswordConfirm(password) {
      this.showPasswordModal = false
      
      try {
        uni.showLoading({ title: '充值中...' })
        
        const res = await walletApi.recharge({
          amount: this.pendingAmount,
          payPassword: password
        })
        
        uni.hideLoading()
        
        if (res.code === 200) {
          uni.showToast({
            title: '充值成功',
            icon: 'success'
          })
          
          // 延迟返回
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.message || '充值失败',
            icon: 'none'
          })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({
          title: '充值失败',
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

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}

.amount-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .amount-grid {
    display: flex;
    flex-wrap: wrap;
    margin: 0 -10rpx;
    
    .amount-item {
      width: calc(33.33% - 20rpx);
      margin: 10rpx;
      padding: 30rpx 0;
      background: #f5f5f5;
      border-radius: 12rpx;
      display: flex;
      align-items: baseline;
      justify-content: center;
      
      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
      
      .amount-num {
        font-size: 40rpx;
        font-weight: bold;
      }
      
      .amount-unit {
        font-size: 24rpx;
        margin-left: 4rpx;
      }
    }
  }
  
  .custom-amount {
    margin-top: 30rpx;
    padding-top: 30rpx;
    border-top: 1rpx solid #f0f0f0;
    
    .label {
      display: block;
      font-size: 28rpx;
      color: #666;
      margin-bottom: 16rpx;
    }
    
    .input-wrapper {
      display: flex;
      align-items: center;
      background: #f5f5f5;
      border-radius: 12rpx;
      padding: 0 24rpx;
      height: 90rpx;
      
      .currency {
        font-size: 36rpx;
        color: #333;
        font-weight: bold;
        margin-right: 16rpx;
      }
      
      input {
        flex: 1;
        font-size: 32rpx;
        color: #333;
      }
    }
  }
}

.payment-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .payment-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .payment-info {
      display: flex;
      align-items: center;
      
      .payment-icon {
        width: 60rpx;
        height: 60rpx;
        margin-right: 20rpx;
      }
      
      .payment-detail {
        .payment-name {
          display: block;
          font-size: 30rpx;
          color: #333;
          margin-bottom: 8rpx;
        }
        
        .payment-desc {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
}

.tips-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  
  .tip-item {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .dot {
      width: 12rpx;
      height: 12rpx;
      background: #667eea;
      border-radius: 50%;
      margin-right: 16rpx;
    }
    
    .tip-text {
      font-size: 26rpx;
      color: #666;
    }
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
  
  .total-amount {
    display: flex;
    align-items: center;
    
    .label {
      font-size: 28rpx;
      color: #666;
      margin-right: 16rpx;
    }
    
    .amount {
      font-size: 48rpx;
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
