<template>
  <view class="container">
    <view class="balance-card">
      <text class="label">可提现余额</text>
      <text class="balance">¥{{ wallet.availableBalance || wallet.balance || '0.00' }}</text>
    </view>
    <view class="panel">
      <text class="title">提现申请</text>
      <view class="input-row">
        <text>¥</text>
        <input v-model="amount" type="digit" placeholder="请输入提现金额" />
      </view>
      <view class="hint">提交后将进入管理员人工确认流程，驳回后余额会退回。</view>
    </view>
    <button class="submit-btn" @click="openPayPassword">提交提现</button>
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
      wallet: {},
      amount: '',
      showPasswordModal: false
    }
  },
  onShow() {
    this.loadWallet()
  },
  methods: {
    async loadWallet() {
      const res = await walletApi.getBalance()
      if (res.code === 200) this.wallet = res.data
    },
    openPayPassword() {
      const value = Number(this.amount)
      const available = Number(this.wallet.availableBalance || this.wallet.balance || 0)
      if (!value || value <= 0) {
        uni.showToast({ title: '请输入正确金额', icon: 'none' })
        return
      }
      if (value > available) {
        uni.showToast({ title: '余额不足', icon: 'none' })
        return
      }
      this.showPasswordModal = true
    },
    async submit(payPassword) {
      this.showPasswordModal = false
      try {
        uni.showLoading({ title: '提交中...' })
        const res = await walletApi.withdraw({ amount: Number(this.amount), payPassword })
        if (res.code === 200) {
          uni.showToast({ title: '提现申请已提交', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 900)
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
.balance-card { padding: 40rpx; margin-bottom: 24rpx; border-radius: 24rpx; background: var(--color-primary-gradient); }
.label { display: block; font-size: 26rpx; color: var(--color-text-secondary); margin-bottom: 12rpx; }
.balance { font-size: 56rpx; font-weight: 800; color: var(--color-text-primary); }
.panel { padding: 32rpx; background: #fff; border-radius: 20rpx; box-shadow: var(--shadow-sm); }
.title { display: block; font-size: 34rpx; font-weight: 700; margin-bottom: 24rpx; }
.input-row { display: flex; align-items: center; gap: 18rpx; padding: 24rpx; background: var(--color-bg-secondary); border-radius: 16rpx; font-size: 40rpx; }
.input-row input { flex: 1; font-size: 36rpx; }
.hint { margin-top: 24rpx; color: var(--color-text-secondary); font-size: 26rpx; line-height: 1.5; }
.submit-btn { margin-top: 36rpx; background: var(--color-primary-gradient); color: var(--color-text-primary); border-radius: 18rpx; }
</style>
