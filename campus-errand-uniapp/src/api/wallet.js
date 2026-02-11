import http from '@/utils/request.js'

/**
 * 钱包相关API
 */
export default {
  /**
   * 获取钱包余额
   * @returns {Promise} 钱包信息，包含余额、冻结金额等
   */
  getBalance() {
    return http.get('/wallet/info')
  },

  /**
   * 获取交易流水
   * @param {Object} params - 查询参数
   * @param {number} params.direction - 收支方向 0-全部 1-收入 2-支出
   * @param {number} params.current - 当前页码，默认1
   * @param {number} params.size - 每页数量，默认10
   * @returns {Promise} 交易记录分页数据
   */
  getTransactions(params = {}) {
    return http.get('/wallet/transactions', params)
  },

  /**
   * 设置支付密码
   * @param {Object} data - 密码数据
   * @param {string} data.payPassword - 支付密码（必填）
   * @param {string} data.confirmPassword - 确认密码（必填）
   * @returns {Promise} 设置结果
   */
  setPayPassword(data) {
    if (!data.payPassword) {
      return Promise.reject(new Error('支付密码不能为空'))
    }
    if (!data.confirmPassword) {
      return Promise.reject(new Error('确认密码不能为空'))
    }
    if (data.payPassword !== data.confirmPassword) {
      return Promise.reject(new Error('两次输入的密码不一致'))
    }
    if (data.payPassword.length < 6) {
      return Promise.reject(new Error('支付密码长度不能少于6位'))
    }
    return http.post('/wallet/pay-password', data)
  },

  /**
   * 验证支付密码
   * @param {string} password - 支付密码
   * @returns {Promise} 验证结果
   */
  verifyPayPassword(password) {
    if (!password) {
      return Promise.reject(new Error('支付密码不能为空'))
    }
    return http.post('/wallet/verify-pay-password', { payPassword: password })
  }
}
