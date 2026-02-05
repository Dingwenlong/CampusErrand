import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

class WebSocketService {
  private stompClient: Stomp.Client | null = null
  private listeners: Map<string, Function[]> = new Map()
  private isConnected: boolean = false

  connect(url: string = 'http://localhost:8082/ws') {
    if (this.stompClient && this.isConnected) return

    const token = localStorage.getItem('token')
    const socket = new SockJS(url)
    this.stompClient = Stomp.over(socket)
    
    // Disable debug logging in production
    this.stompClient.debug = () => {}

    const headers = token ? { 'Authorization': 'Bearer ' + token } : {}

    this.stompClient.connect(headers,
      () => {
        this.isConnected = true
        console.log('WebSocket connected')
        
        // Subscribe to seat updates
        this.stompClient?.subscribe('/topic/seats', (message) => {
          try {
            const data = JSON.parse(message.body)
            if (data.event === 'seat_update') {
              this.notify('seat_update', data)
            }
          } catch (e) {
            console.error('Failed to parse WebSocket message:', e)
          }
        })

        // Subscribe to private alerts
        this.stompClient?.subscribe('/user/queue/alerts', (message) => {
          this.notify('alert', { message: message.body, type: 'warning' })
        })

        // Subscribe to private reservation updates
        this.stompClient?.subscribe('/user/queue/reservation_update', (message) => {
          try {
            const data = JSON.parse(message.body)
            this.notify('reservation_update', data)
          } catch (e) {
            console.error('Failed to parse reservation update:', e)
          }
        })

        // Subscribe to private notifications
        this.stompClient?.subscribe('/user/queue/notifications', (message) => {
          try {
            const data = JSON.parse(message.body)
            this.notify('new_notification', data)
          } catch (e) {
            console.error('Failed to parse new notification:', e)
          }
        })

        // Subscribe to message square updates
        this.stompClient?.subscribe('/topic/messages', (message) => {
          try {
            const data = JSON.parse(message.body)
            this.notify('new_message', data)
          } catch (e) {
            console.error('Failed to parse new message:', e)
          }
        })

        // Subscribe to online status updates
        this.stompClient?.subscribe('/topic/online_status', (message) => {
          try {
            const data = JSON.parse(message.body)
            this.notify('online_status', data)
          } catch (e) {
            console.error('Failed to parse online status:', e)
          }
        })

        // Subscribe to user seat status updates
        this.stompClient?.subscribe('/topic/user_seat_status', (message) => {
          try {
            const data = JSON.parse(message.body)
            this.notify('user_seat_status', data)
          } catch (e) {
            console.error('Failed to parse user seat status:', e)
          }
        })

        // Subscribe to global stats updates
        this.stompClient?.subscribe('/topic/stats', (message) => {
          try {
            const data = JSON.parse(message.body)
            this.notify('stats_update', data)
          } catch (e) {
            console.error('Failed to parse stats update:', e)
          }
        })
      },
      (error) => {
        this.isConnected = false
        console.error('WebSocket connection error:', error)
        // Try to reconnect after 5 seconds if not explicitly disconnected
        if (this.stompClient) {
          setTimeout(() => this.connect(url), 5000)
        }
      }
    )
  }

  disconnect(force: boolean = false) {
    // In SPA, we usually don't want to disconnect on page navigation
    // only on actual logout or if explicitly forced
    if (!force) return

    if (this.stompClient) {
      this.stompClient.disconnect(() => {
        this.isConnected = false
        console.log('WebSocket disconnected')
      })
      this.stompClient = null
    }
  }

  on(event: string, callback: Function) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event)?.push(callback)
  }

  off(event: string, callback: Function) {
    if (!this.listeners.has(event)) return
    const callbacks = this.listeners.get(event)
    const index = callbacks?.indexOf(callback)
    if (index !== undefined && index > -1) {
      callbacks?.splice(index, 1)
    }
  }

  private notify(event: string, data: any) {
    const callbacks = this.listeners.get(event)
    callbacks?.forEach(cb => cb(data))
  }
}

export const wsService = new WebSocketService()
