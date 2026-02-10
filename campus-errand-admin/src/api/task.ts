import request from '@/utils/request'

export const getTaskList = (params: any) => {
  return request.get('/admin/task/list', { params })
}

export const getTaskDetail = (id: number) => {
  return request.get(`/admin/task/${id}`)
}

export const cancelTask = (id: number, reason: string) => {
  return request.post(`/admin/task/${id}/cancel?reason=${encodeURIComponent(reason)}`)
}

export const deleteTask = (id: number) => {
  return request.delete(`/admin/task/${id}`)
}

export const getTaskStats = () => {
  return request.get('/admin/task/stats')
}
