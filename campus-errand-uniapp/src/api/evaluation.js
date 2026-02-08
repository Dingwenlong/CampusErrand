import http from '@/utils/request.js'

/**
 * 评价相关API
 */
export default {
  /**
   * 提交评价
   * @param {Object} data - 评价数据
   * @param {number} data.taskId - 任务ID（必填）
   * @param {number} data.toUserId - 被评价用户ID（必填）
   * @param {number} data.rating - 评分 1-5（必填）
   * @param {string} data.content - 评价内容
   * @param {string} data.tags - 评价标签，逗号分隔
   * @returns {Promise} 提交结果
   */
  submit(data) {
    // 参数验证
    if (!data.taskId) {
      return Promise.reject(new Error('任务ID不能为空'))
    }
    if (!data.toUserId) {
      return Promise.reject(new Error('被评价用户ID不能为空'))
    }
    if (!data.rating || data.rating < 1 || data.rating > 5) {
      return Promise.reject(new Error('评分必须在1-5之间'))
    }
    
    return http.post('/evaluation/submit', data)
  },

  /**
   * 获取任务的评价列表
   * @param {number|string} taskId - 任务ID
   * @returns {Promise} 评价列表
   */
  getTaskEvaluations(taskId) {
    if (!taskId) {
      return Promise.reject(new Error('任务ID不能为空'))
    }
    return http.get(`/evaluation/task/${taskId}`)
  },

  /**
   * 获取我收到的评价
   * @param {Object} params - 查询参数
   * @param {number} params.current - 当前页码，默认1
   * @param {number} params.size - 每页数量，默认10
   * @returns {Promise} 评价列表分页数据
   */
  getReceivedEvaluations(params = {}) {
    return http.get('/evaluation/received', params)
  },

  /**
   * 获取我发出的评价
   * @param {Object} params - 查询参数
   * @param {number} params.current - 当前页码，默认1
   * @param {number} params.size - 每页数量，默认10
   * @returns {Promise} 评价列表分页数据
   */
  getSentEvaluations(params = {}) {
    return http.get('/evaluation/sent', params)
  },

  /**
   * 获取用户平均评分
   * @param {number|string} userId - 用户ID
   * @returns {Promise} 平均评分
   */
  getUserAverageRating(userId) {
    if (!userId) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    return http.get(`/evaluation/average-rating/${userId}`)
  }
}
