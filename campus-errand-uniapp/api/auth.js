import request from '@/utils/request.js'

export default {
  // 微信小程序登录
  wxLogin(data) {
    return request.post('/auth/wx-login', data)
  }
}
