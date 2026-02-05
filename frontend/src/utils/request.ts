import axios from 'axios'
import { useUserStore } from '../stores/user'
import router from '../router'
import { message } from 'ant-design-vue'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 5000
})

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果包含标准 code 字段，进行解包处理
    if (res && typeof res.code !== 'undefined') {
      if (res.code === 200) {
        return res.data // 自动解包，返回实际数据
      }
      // 业务错误处理
      const errorMsg = res.msg || '请求失败'
      message.error(errorMsg)
      return Promise.reject(new Error(errorMsg))
    }
    // 兼容 Mock 数据或无包装的响应
    return res
  },
  (error) => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      const userStore = useUserStore()
      userStore.clearToken()
      router.push('/login')
      const msg = error.response.status === 401 ? '会话已过期，请重新登录' : '访问受限或权限失效，请重新登录'
      message.error(msg)
    }
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// Wrap the axios instance to define the return type as T instead of AxiosResponse<T>
const request = <T = any>(config: any): Promise<T> => {
  return service(config) as Promise<T>
}

export default request
