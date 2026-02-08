import http from '@/utils/request.js'

/**
 * 认证相关API
 */
export default {
  /**
   * 微信小程序登录
   * @param {Object} data - 登录参数
   * @param {string} data.code - 微信登录code
   * @param {string} data.nickname - 用户昵称
   * @param {string} data.avatar - 用户头像URL
   * @param {number} data.gender - 性别 0-未知 1-男 2-女
   * @returns {Promise} 登录结果，包含token和用户信息
   */
  wxLogin(data) {
    // 参数验证
    if (!data.code) {
      return Promise.reject(new Error('登录code不能为空'))
    }
    
    return http.post('/auth/wx-login', data)
  },

  /**
   * 退出登录
   * 清除本地存储的token和用户信息
   */
  logout() {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    uni.showToast({
      title: '已退出登录',
      icon: 'success'
    })
  }
}
