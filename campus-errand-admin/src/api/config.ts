import request from '@/utils/request'

// 获取所有配置
export const getConfigList = () => {
  return request.get('/config/list')
}

// 根据分类获取配置
export const getConfigByCategory = (category: string) => {
  return request.get(`/config/category/${category}`)
}

// 根据key获取配置
export const getConfigByKey = (key: string) => {
  return request.get(`/config/${key}`)
}

// 更新配置（单个）
export const updateConfig = (key: string, value: string) => {
  return request.put(`/config/${key}?value=${value}`)
}

// 批量更新配置
export const batchUpdateConfig = (configs: any[]) => {
  return request.put('/config/batch', configs)
}

// 保存配置（POST请求，支持新增和更新）
export const saveConfigs = (configMap: Record<string, string>) => {
  return request.post('/config/save', configMap)
}
