<template>
  <view class="container">
    <view class="panel">
      <text class="title">余额充值</text>
      <view class="amount-grid">
        <view
          v-for="item in amountList"
          :key="item"
          class="amount-item"
          :class="{ active: amount === item }"
          @click="amount = item"
        >
          ¥{{ item }}
        </view>
      </view>
      <view class="input-row">
        <text>自定义</text>
        <input v-model="customAmount" type="digit" placeholder="请输入金额" @focus="amount = ''" />
      </view>
      <view class="hint">虚拟余额将直接进入账户，可用于发布任务赏金。</view>
    </view>
    <button class="submit-btn" @click="openPayPassword">确认充值 ¥{{ finalAmount }}</button>
    <pay-password-modal :show="showPasswordModal" @confirm="submit" @cancel="showPasswordModal = false" />
  </view>
</template>

<script>
import walletApi from '@/api/wallet.js'
import PayPasswordModal from '@/components/pay-password-modal.vue'

export default {
  components: { PayPasswordModal },
  data() {
    return {
      amountList: ['10', '20', '50', '100', '200', '500'],
      amount: '50',
      customAmount: '',
      showPasswordModal: false
    }
  },
  computed: {
    finalAmount() {
      return this.amount || this.customAmount || '0'
    }
  },
  methods: {
    openPayPassword() {
      const value = Number(this.finalAmount)
      if (!value || value <= 0) {
        uni.showToast({ title: '请输入正确金额', icon: 'none' })
        return
      }
      this.showPasswordModal = true
    },
    async submit(payPassword) {
      this.showPasswordModal = false
      try {
        uni.showLoading({ title: '处理中...' })
        const res = await walletApi.recharge({ amount: Number(this.finalAmount), payPassword })
        if (res.code === 200) {
          uni.showToast({ title: '充值成功', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 800)
        }
      } finally {
        uni.hideLoading()
      }
    }
  }
}
</script>

<style scoped>
.container { min-height: 100vh; padding: 32rpx; background: var(--color-bg); }
.panel { padding: 32rpx; background: #fff; border-radius: 20rpx; box-shadow: var(--shadow-sm); }
.title { display: block; font-size: 36rpx; font-weight: 700; margin-bottom: 28rpx; }
.amount-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18rpx; }
.amount-item { padding: 28rpx 0; text-align: center; background: var(--color-bg-secondary); border-radius: 16rpx; font-weight: 700; }
.amount-item.active { background: var(--color-primary-gradient); color: var(--color-text-primary); }
.input-row { display: flex; align-items: center; gap: 20rpx; margin-top: 28rpx; padding: 22rpx; background: var(--color-bg-secondary); border-radius: 16rpx; }
.input-row input { flex: 1; }
.hint { margin-top: 24rpx; color: var(--color-text-secondary); font-size: 26rpx; }
.submit-btn { margin-top: 36rpx; background: var(--color-primary-gradient); color: var(--color-text-primary); border-radius: 18rpx; }
</style>
