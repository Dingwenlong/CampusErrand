import request from '@/utils/request'

export const getVerificationList = (params: any) => {
  return request.get('/admin/user/verify/list', { params })
}

export const approveVerification = (id: number) => {
  return request.post(`/admin/user/verify/${id}/approve`)
}

export const rejectVerification = (id: number, reason: string) => {
  return request.post(`/admin/user/verify/${id}/reject`, { reason })
}
