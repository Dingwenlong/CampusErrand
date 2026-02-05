import request from '@/utils/request.js'

export default {
  // 获取当前用户信息
  getInfo() {
    return request.get('/user/info')
  },

  // 获取用户信息
  getById(id) {
    return request.get(`/user/${id}`)
  }
}
