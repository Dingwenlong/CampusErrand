import request from '@/utils/request'

export const getConfigList = () => {
  return request.get('/config/list')
}

export const getConfigByCategory = (category: string) => {
  return request.get(`/config/category/${category}`)
}

export const getConfigByKey = (key: string) => {
  return request.get(`/config/${key}`)
}

export const updateConfig = (key: string, value: string) => {
  return request.put(`/config/${key}?value=${value}`)
}

export const batchUpdateConfig = (configs: any[]) => {
  return request.put('/config/batch', configs)
}
