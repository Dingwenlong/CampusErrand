import request from '@/utils/request.js'

export default {
  // 发布任务
  publish(data) {
    return request.post('/task/publish', data)
  },

  // 获取任务列表
  getList(params) {
    return request.get('/task/list', params)
  },

  // 获取任务详情
  getDetail(id) {
    return request.get(`/task/${id}`)
  },

  // 抢单
  accept(id) {
    return request.post(`/task/${id}/accept`)
  },

  // 更新任务状态
  updateStatus(id, status) {
    return request.post(`/task/${id}/status`, { status })
  }
}
