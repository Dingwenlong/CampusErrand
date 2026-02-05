import request from '@/utils/request.js'

export default {
  // 提交评价
  submit(data) {
    return request.post('/evaluation/submit', data)
  },

  // 获取任务的评价列表
  getTaskEvaluations(taskId) {
    return request.get(`/evaluation/task/${taskId}`)
  },

  // 获取我收到的评价
  getReceivedEvaluations(params) {
    return request.get('/evaluation/received', params)
  },

  // 获取我发出的评价
  getSentEvaluations(params) {
    return request.get('/evaluation/sent', params)
  },

  // 获取用户平均评分
  getUserAverageRating(userId) {
    return request.get(`/evaluation/average-rating/${userId}`)
  }
}
