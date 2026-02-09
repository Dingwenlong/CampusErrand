import request from '@/utils/request.js'

export default {
  // 获取消息列表
  getList(params) {
    return request.get('/message/list', params)
  },

  // 标记消息已读
  markAsRead(messageId) {
    return request.post(`/message/read/${messageId}`)
  },

  // 标记所有消息已读
  markAllRead() {
    return request.post('/message/read-all')
  },

  // 获取未读消息数
  getUnreadCount() {
    return request.get('/message/unread-count')
  },

  // 删除消息
  delete(messageId) {
    return request.delete(`/message/${messageId}`)
  }
}
