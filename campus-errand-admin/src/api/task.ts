import request from '@/utils/request'
import type { Task, PageResult } from '@/types'

export const getTaskList = (params: { current?: number; size?: number; status?: number; keyword?: string }) => {
  return request.get('/admin/task/list', { params })
}

export const getTaskDetail = (id: number) => {
  return request.get(`/admin/task/${id}`)
}

export const cancelTask = (id: number, reason: string) => {
  return request.post(`/admin/task/${id}/cancel`, { reason })
}
