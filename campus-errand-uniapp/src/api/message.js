import http from '@/utils/request.js'

/**
 * 消息相关API
 */
export default {
  /**
   * 获取消息列表
   * @param {Object} params - 查询参数
   * @param {number} params.current - 当前页码，默认1
   * @param {number} params.size - 每页数量，默认10
   * @returns {Promise} 消息列表分页数据
   */
  getList(params = {}) {
    return http.get('/message/list', params)
  },

  /**
   * 标记消息已读
   * @param {number|string} id - 消息ID
   * @returns {Promise} 操作结果
   */
  markAsRead(id) {
    if (!id) {
      return Promise.reject(new Error('消息ID不能为空'))
    }
    return http.post(`/message/read/${id}`)
  },

  /**
   * 标记所有消息已读
   * @returns {Promise} 操作结果，返回标记为已读的消息数量
   */
  markAllAsRead() {
    return http.post('/message/read-all')
  },

  /**
   * 获取未读消息数
   * @returns {Promise} 未读消息数量
   */
  getUnreadCount() {
    return http.get('/message/unread-count')
  }
}
