import request from '@/utils/request'

export const getDashboardData = () => {
  return request.get('/admin/dashboard')
}

export const getTaskStatusStats = () => {
  return request.get('/admin/task-status-stats')
}

export const getAmountTrend = () => {
  return request.get('/admin/amount-trend')
}

export const getUserGrowth = () => {
  return request.get('/admin/user-growth')
}
