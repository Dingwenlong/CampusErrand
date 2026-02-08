import http from '@/utils/request.js'

/**
 * 订单相关API
 * 注意：后端使用/task路径处理订单相关操作
 */
export default {
  /**
   * 获取订单列表（我发布的）
   * @param {Object} params - 查询参数
   * @param {number} params.status - 订单状态筛选
   * @param {number} params.current - 当前页码，默认1
   * @param {number} params.size - 每页数量，默认10
   * @returns {Promise} 订单列表分页数据
   */
  getMyPublishedOrders(params = {}) {
    return http.get('/task/list', { 
      ...params, 
      myPublish: true 
    })
  },

  /**
   * 获取订单列表（我承接的）
   * @param {Object} params - 查询参数
   * @param {number} params.status - 订单状态筛选
   * @param {number} params.current - 当前页码，默认1
   * @param {number} params.size - 每页数量，默认10
   * @returns {Promise} 订单列表分页数据
   */
  getMyAcceptedOrders(params = {}) {
    return http.get('/task/list', { 
      ...params, 
      myAccept: true 
    })
  },

  /**
   * 获取订单详情
   * @param {number|string} id - 订单ID（任务ID）
   * @returns {Promise} 订单详情
   */
  getDetail(id) {
    if (!id) {
      return Promise.reject(new Error('订单ID不能为空'))
    }
    return http.get(`/task/${id}`)
  },

  /**
   * 取消订单
   * @param {number|string} id - 订单ID
   * @param {Object} data - 取消原因等信息
   * @param {string} data.reason - 取消原因
   * @returns {Promise} 取消结果
   */
  cancel(id, data = {}) {
    if (!id) {
      return Promise.reject(new Error('订单ID不能为空'))
    }
    return http.post(`/task/${id}/cancel`, data)
  },

  /**
   * 确认送达（跑腿员）
   * @param {number|string} id - 订单ID
   * @returns {Promise} 操作结果
   */
  deliver(id) {
    if (!id) {
      return Promise.reject(new Error('订单ID不能为空'))
    }
    return http.post(`/task/${id}/deliver`)
  },

  /**
   * 确认收货（发单者）
   * @param {number|string} id - 订单ID
   * @returns {Promise} 操作结果
   */
  receive(id) {
    if (!id) {
      return Promise.reject(new Error('订单ID不能为空'))
    }
    return http.post(`/task/${id}/receive`)
  }
}
