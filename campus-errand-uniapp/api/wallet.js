import request from '@/utils/request.js'

export default {
  // 获取钱包信息
  getInfo() {
    return request.get('/wallet/info')
  },

  // 设置支付密码
  setPayPassword(data) {
    return request.post('/wallet/pay-password', data)
  },

  // 验证支付密码
  verifyPayPassword(data) {
    return request.post('/wallet/verify-pay-password', data)
  },

  // 虚拟充值
  recharge(data) {
    return request.post('/wallet/recharge', data)
  },

  // 虚拟提现
  withdraw(data) {
    return request.post('/wallet/withdraw', data)
  },

  // 获取交易流水
  getTransactions(params) {
    return request.get('/wallet/transactions', params)
  },

  // 获取交易类型
  getTransactionTypes() {
    return request.get('/wallet/transaction-types')
  }
}
