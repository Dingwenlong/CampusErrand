import request from '@/utils/request.js'

export default {
  // 获取钱包余额
  getBalance() {
    return request.get('/wallet/balance')
  },

  // 获取交易流水
  getTransactions(params) {
    return request.get('/wallet/transactions', params)
  },

  // 设置支付密码
  setPayPassword(data) {
    return request.post('/wallet/pay-password', data)
  },

  // 验证支付密码
  verifyPayPassword(password) {
    return request.post('/wallet/verify-pay-password', { payPassword: password })
  },

  // 检查是否已设置支付密码
  hasPayPassword() {
    return request.get('/wallet/has-pay-password')
  }
}
