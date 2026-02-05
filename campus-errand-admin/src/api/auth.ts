import request from '@/utils/request'

export const login = (data: { username: string; password: string }) => {
  return request.post('/admin/login', data)
}

export const getAdminInfo = () => {
  return request.get('/admin/info')
}
