import http from '@/utils/request.js'

/**
 * 任务相关API
 */
export default {
  /**
   * 发布任务
   * @param {Object} data - 任务数据
   * @param {string} data.title - 任务标题（必填）
   * @param {string} data.description - 任务描述
   * @param {number} data.taskType - 任务类型 1-取快递 2-代买 3-送件 4-其他（必填）
   * @param {string} data.pickupAddress - 取件地址（必填）
   * @param {string} data.deliveryAddress - 送达地址（必填）
   * @param {number} data.reward - 赏金金额（必填）
   * @param {string} data.payPassword - 支付密码（必填）
   * @param {string} data.pickupContact - 取件联系人
   * @param {string} data.pickupPhone - 取件联系电话
   * @param {string} data.deliveryContact - 送达联系人
   * @param {string} data.deliveryPhone - 送达联系电话
   * @param {string} data.remark - 备注
   * @returns {Promise} 发布结果
   */
  publish(data) {
    // 参数验证
    const required = ['title', 'taskType', 'pickupAddress', 'deliveryAddress', 'reward', 'payPassword']
    for (const field of required) {
      if (!data[field]) {
        return Promise.reject(new Error(`${field}不能为空`))
      }
    }
    
    if (data.reward <= 0) {
      return Promise.reject(new Error('赏金金额必须大于0'))
    }
    
    return http.post('/task/publish', data)
  },

  /**
   * 获取任务列表
   * @param {Object} params - 查询参数
   * @param {number} params.taskType - 任务类型筛选
   * @param {number} params.status - 任务状态筛选 0-待接单 1-已接单 2-待取件 3-配送中 4-待确认 5-已完成 6-已取消
   * @param {number} params.current - 当前页码，默认1
   * @param {number} params.size - 每页数量，默认10
   * @returns {Promise} 任务列表分页数据
   */
  getList(params = {}) {
    return http.get('/task/list', params)
  },

  /**
   * 获取任务详情
   * @param {number|string} id - 任务ID
   * @returns {Promise} 任务详情
   */
  getDetail(id) {
    if (!id) {
      return Promise.reject(new Error('任务ID不能为空'))
    }
    return http.get(`/task/${id}`)
  },

  /**
   * 抢单/接单
   * @param {number|string} id - 任务ID
   * @returns {Promise} 抢单结果
   */
  accept(id) {
    if (!id) {
      return Promise.reject(new Error('任务ID不能为空'))
    }
    return http.post(`/task/${id}/accept`)
  },

  /**
   * 更新任务状态
   * @param {number|string} id - 任务ID
   * @param {number} status - 状态值 0-待接单 1-已接单 2-待取件 3-配送中 4-待确认 5-已完成 6-已取消
   * @returns {Promise} 更新结果
   */
  updateStatus(id, status) {
    if (!id) {
      return Promise.reject(new Error('任务ID不能为空'))
    }
    if (status === undefined || status === null) {
      return Promise.reject(new Error('状态值不能为空'))
    }
    return http.post(`/task/${id}/status`, { status })
  },

  /**
   * 取消任务
   * @param {number|string} id - 任务ID
   * @param {Object} data - 取消原因等信息
   * @param {string} data.reason - 取消原因
   * @returns {Promise} 取消结果
   */
  cancel(id, data = {}) {
    if (!id) {
      return Promise.reject(new Error('任务ID不能为空'))
    }
    return http.post(`/task/${id}/cancel`, data)
  },

  /**
   * 确认送达（跑腿员）
   * @param {number|string} id - 任务ID
   * @returns {Promise} 操作结果
   */
  deliver(id) {
    if (!id) {
      return Promise.reject(new Error('任务ID不能为空'))
    }
    return http.post(`/task/${id}/deliver`)
  },

  /**
   * 确认收货（发单者）
   * @param {number|string} id - 任务ID
   * @returns {Promise} 操作结果
   */
  receive(id) {
    if (!id) {
      return Promise.reject(new Error('任务ID不能为空'))
    }
    return http.post(`/task/${id}/receive`)
  }
}
