import request from '@/utils/request.js'

export default {
  // 获取消息列表
  getList(params) {
    return request.get('/message/list', params)
  },

  // 标记消息已读
  markAsRead(id) {
    return request.post(`/message/read/${id}`)
  },

  // 标记所有消息已读
  markAllAsRead() {
    return request.post('/message/read-all')
  },

  // 获取未读消息数
  getUnreadCount() {
    return request.get('/message/unread-count')
  }
}
