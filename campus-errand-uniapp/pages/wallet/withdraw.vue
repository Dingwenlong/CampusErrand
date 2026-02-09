<template>
  <view class="container">
    <!-- 余额显示 -->
    <view class="balance-card">
      <text class="balance-label">当前余额</text>
      <text class="balance-amount">¥{{ walletInfo.balance || '0.00' }}</text>
    </view>

    <!-- 提现金额 -->
    <view class="amount-section">
      <view class="section-title">提现金额</view>
      <view class="input-wrapper">
        <text class="currency">¥</text>
        <input 
          v-model="withdrawAmount" 
          type="digit" 
          placeholder="请输入提现金额"
        />
      </view>
      <view class="amount-tips">
        <text>可提现金额 ¥{{ walletInfo.balance || '0.00' }}</text>
        <text class="all-btn" @click="withdrawAll">全部提现</text>
      </view>
    </view>

    <!-- 提现说明 -->
    <view class="tips-section">
      <view class="section-title">提现说明</view>
      <view class="tip-item">
        <text class="dot"></text>
        <text class="tip-text">提现金额将直接扣除您的账户余额</text>
      </view>
      <view class="tip-item">
        <text class="dot"></text>
        <text class="tip-text">虚拟提现仅供演示使用</text>
      </view>
      <view class="tip-item">
        <text class="dot"></text>
        <text class="tip-text">提现前请确保余额充足</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <view class="total-amount">
        <text class="label">提现金额</text>
        <text class="amount">¥{{ withdrawAmount || '0.00' }}</text>
      </view>
      <button class="btn-primary" @click="handleWithdraw">确认提现</button>
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
      walletInfo: {
        balance: '0.00'
      },
      withdrawAmount: '',
      showPasswordModal: false,
      pendingAmount: 0
    }
  },
  onLoad() {
    this.loadWalletInfo()
  },
  methods: {
    async loadWalletInfo() {
      try {
        const res = await walletApi.getInfo()
        if (res.code === 200) {
          this.walletInfo = res.data
        }
      } catch (e) {
        console.error('加载钱包信息失败', e)
      }
    },
    
    withdrawAll() {
      this.withdrawAmount = this.walletInfo.balance
    },
    
    handleWithdraw() {
      const amount = parseFloat(this.withdrawAmount)
      if (!amount || amount <= 0) {
        uni.showToast({
          title: '请输入提现金额',
          icon: 'none'
        })
        return
      }
      
      const balance = parseFloat(this.walletInfo.balance)
      if (amount > balance) {
        uni.showToast({
          title: '余额不足',
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
        uni.showLoading({ title: '提现中...' })
        
        const res = await walletApi.withdraw({
          amount: this.pendingAmount,
          payPassword: password
        })
        
        uni.hideLoading()
        
        if (res.code === 200) {
          uni.showToast({
            title: '提现成功',
            icon: 'success'
          })
          
          // 更新余额
          this.loadWalletInfo()
          this.withdrawAmount = ''
          
          // 延迟返回
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.message || '提现失败',
            icon: 'none'
          })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({
          title: '提现失败',
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

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  padding: 40rpx;
  margin-bottom: 20rpx;
  text-align: center;
  color: #fff;
  
  .balance-label {
    display: block;
    font-size: 28rpx;
    opacity: 0.9;
    margin-bottom: 16rpx;
  }
  
  .balance-amount {
    display: block;
    font-size: 56rpx;
    font-weight: bold;
  }
}

.amount-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 24rpx;
  }
  
  .input-wrapper {
    display: flex;
    align-items: center;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 0 24rpx;
    height: 100rpx;
    margin-bottom: 16rpx;
    
    .currency {
      font-size: 40rpx;
      color: #333;
      font-weight: bold;
      margin-right: 16rpx;
    }
    
    input {
      flex: 1;
      font-size: 36rpx;
      color: #333;
    }
  }
  
  .amount-tips {
    display: flex;
    justify-content: space-between;
    font-size: 26rpx;
    color: #999;
    
    .all-btn {
      color: #667eea;
    }
  }
}

.tips-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  
  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 24rpx;
  }
  
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
