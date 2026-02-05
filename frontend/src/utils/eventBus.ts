// 简单的事件总线，用于组件间通信
class EventBus {
  private events: Map<string, Function[]> = new Map()

  on(event: string, callback: Function) {
    if (!this.events.has(event)) {
      this.events.set(event, [])
    }
    this.events.get(event)?.push(callback)
  }

  off(event: string, callback: Function) {
    if (!this.events.has(event)) return
    const callbacks = this.events.get(event)
    const index = callbacks?.indexOf(callback)
    if (index !== undefined && index > -1) {
      callbacks?.splice(index, 1)
    }
  }

  emit(event: string, data?: any) {
    const callbacks = this.events.get(event)
    callbacks?.forEach(cb => cb(data))
  }
}

export const eventBus = new EventBus()
