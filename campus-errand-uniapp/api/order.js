import request from '@/utils/request.js'

export default {
  // 获取我发布的订单列表
  getMyPublishedOrders(params) {
    return request.get('/task/list', { ...params, status: params.status })
  },

  // 获取我接的订单列表
  getMyAcceptedOrders(params) {
    return request.get('/task/list', { ...params, status: params.status })
  },

  // 获取订单详情
  getDetail(id) {
    return request.get(`/task/${id}`)
  },

  // 取消订单
  cancel(id, data) {
    return request.post(`/task/${id}/cancel`, data)
  },

  // 确认送达（跑腿员）
  deliver(id) {
    return request.post(`/task/${id}/deliver`)
  },

  // 确认收货（发单者）
  receive(id) {
    return request.post(`/task/${id}/receive`)
  }
}
