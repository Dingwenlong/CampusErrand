import request from '@/utils/request'
import type { User, UserDetail, PageResult } from '@/types'

export const getUserList = (params: { current?: number; size?: number; keyword?: string }) => {
  return request.get<any, { code: number; data: PageResult<User>; message: string }>('/admin/user/list', { params })
}

export const getUserDetail = (id: number) => {
  return request.get<any, { code: number; data: UserDetail; message: string }>(`/admin/user/${id}`)
}

export const updateUserStatus = (id: number, status: number) => {
  return request.post(`/admin/user/${id}/status`, null, { params: { status } })
}

export const updateUserCredit = (id: number, creditScore: number) => {
  return request.post(`/admin/user/${id}/credit`, null, { params: { creditScore } })
}
