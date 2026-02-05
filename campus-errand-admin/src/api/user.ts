import request from '@/utils/request'
import type { User, PageResult } from '@/types'

export const getUserList = (params: { current?: number; size?: number; keyword?: string }) => {
  return request.get('/admin/user/list', { params })
}

export const getUserDetail = (id: number) => {
  return request.get(`/admin/user/${id}`)
}

export const updateUserStatus = (id: number, status: number) => {
  return request.post(`/admin/user/${id}/status`, { status })
}

export const updateUserCredit = (id: number, creditScore: number) => {
  return request.post(`/admin/user/${id}/credit`, { creditScore })
}
