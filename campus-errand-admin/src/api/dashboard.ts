import request from '@/utils/request'
import type { DashboardData } from '@/types'

export const getDashboardData = () => {
  return request.get('/admin/dashboard')
}

export const getUserStats = () => {
  return request.get('/admin/stats/user')
}

export const getTaskStats = () => {
  return request.get('/admin/stats/task')
}

export const getAmountStats = () => {
  return request.get('/admin/stats/amount')
}
