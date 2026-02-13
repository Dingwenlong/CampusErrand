import { http } from '@/utils/request.js'

export default {
  getConfigByKey(key) {
    return http.get(`/config/${key}`)
  },
  
  getConfigsByCategory(category) {
    return http.get(`/config/category/${category}`)
  }
}
