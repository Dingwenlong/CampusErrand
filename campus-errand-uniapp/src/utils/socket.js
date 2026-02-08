import { getToken } from './auth.js'

const BASE_URL = 'ws://localhost:8080'

class WebSocketClient {
  constructor() {
    this.socket = null
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.isConnected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.listeners = new Map()
  }

  // 连接WebSocket
  connect() {
    const token = getToken()
    if (!token) {
      console.log('未登录，无法连接WebSocket')
      return
    }

    if (this.socket && this.isConnected) {
      console.log('WebSocket已连接')
      return
    }

    const url = `${BASE_URL}/ws/message?token=${token}`
    console.log('连接WebSocket:', url)

    this.socket = uni.connectSocket({
      url: url,
      success: () => {
        console.log('WebSocket连接请求已发送')
      },
      fail: (err) => {
        console.error('WebSocket连接失败:', err)
        this.reconnect()
      }
    })

    this.socket.onOpen(() => {
      console.log('WebSocket连接成功')
      this.isConnected = true
      this.reconnectAttempts = 0
      this.startHeartbeat()
      this.emit('connected', {})
    })

    this.socket.onMessage((res) => {
      console.log('收到WebSocket消息:', res.data)
      try {
        const data = JSON.parse(res.data)
        this.handleMessage(data)
      } catch (e) {
        console.log('收到非JSON消息:', res.data)
      }
    })

    this.socket.onClose(() => {
      console.log('WebSocket连接关闭')
      this.isConnected = false
      this.stopHeartbeat()
      this.reconnect()
    })

    this.socket.onError((err) => {
      console.error('WebSocket错误:', err)
      this.isConnected = false
      this.reconnect()
    })
  }

  // 断开连接
  disconnect() {
    console.log('断开WebSocket连接')
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    this.isConnected = false
    this.reconnectAttempts = 0
  }

  // 重连
  reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('重连次数已达上限')
      return
    }

    this.reconnectAttempts++
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)
    console.log(`${delay}ms后尝试第${this.reconnectAttempts}次重连`)

    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, delay)
  }

  // 心跳
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected) {
        this.send('ping')
      }
    }, 30000) // 30秒心跳
  }

  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  // 发送消息
  send(data) {
    if (!this.isConnected) {
      console.log('WebSocket未连接，无法发送消息')
      return false
    }

    const message = typeof data === 'string' ? data : JSON.stringify(data)
    this.socket.send({
      data: message,
      success: () => {
        console.log('消息发送成功:', message)
      },
      fail: (err) => {
        console.error('消息发送失败:', err)
      }
    })
    return true
  }

  // 处理接收到的消息
  handleMessage(data) {
    // 处理心跳响应
    if (data === 'pong') {
      console.log('收到心跳响应')
      return
    }

    // 处理连接成功消息
    if (data.type === 'connected') {
      console.log('连接成功:', data.message)
      return
    }

    // 处理业务消息
    if (data.type === 'message' && data.data) {
      this.emit('message', data.data)
      return
    }

    // 其他消息类型
    this.emit(data.type || 'unknown', data)
  }

  // 添加监听器
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  // 移除监听器
  off(event, callback) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  // 触发事件
  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach(callback => {
        try {
          callback(data)
        } catch (e) {
          console.error('事件处理错误:', e)
        }
      })
    }
  }

  // 获取连接状态
  getConnectionStatus() {
    return this.isConnected
  }
}

// 单例模式
let instance = null

export function getSocket() {
  if (!instance) {
    instance = new WebSocketClient()
  }
  return instance
}

export default {
  connect() {
    return getSocket().connect()
  },
  disconnect() {
    return getSocket().disconnect()
  },
  send(data) {
    return getSocket().send(data)
  },
  on(event, callback) {
    return getSocket().on(event, callback)
  },
  off(event, callback) {
    return getSocket().off(event, callback)
  },
  getConnectionStatus() {
    return getSocket().getConnectionStatus()
  }
}
