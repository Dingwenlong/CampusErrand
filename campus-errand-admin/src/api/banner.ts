import request from '@/utils/request'

export const getBannerList = () => {
  return request.get('/banner/all')
}

export const getBannerById = (id: number) => {
  return request.get(`/banner/${id}`)
}

export const addBanner = (data: any) => {
  return request.post('/banner', data)
}

export const updateBanner = (id: number, data: any) => {
  return request.put(`/banner/${id}`, data)
}

export const deleteBanner = (id: number) => {
  return request.delete(`/banner/${id}`)
}

export const updateBannerStatus = (id: number, status: number) => {
  return request.put(`/banner/${id}/status?status=${status}`)
}
