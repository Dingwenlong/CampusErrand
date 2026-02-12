import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'
import type { ApiResponse } from '@/types'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      message.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('admin_token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res as any
  },
  (error) => {
    message.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

type RequestConfig = AxiosRequestConfig

interface RequestClient {
  request<T = any>(config: RequestConfig): Promise<ApiResponse<T>>
  get<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>>
  post<T = any>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>>
  put<T = any>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>>
  delete<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>>
}

const request: RequestClient = {
  request<T = any>(config: RequestConfig) {
    return instance.request<unknown, ApiResponse<T>>(config)
  },
  get<T = any>(url: string, config?: RequestConfig) {
    return instance.get<unknown, ApiResponse<T>>(url, config)
  },
  post<T = any>(url: string, data?: unknown, config?: RequestConfig) {
    return instance.post<unknown, ApiResponse<T>>(url, data, config)
  },
  put<T = any>(url: string, data?: unknown, config?: RequestConfig) {
    return instance.put<unknown, ApiResponse<T>>(url, data, config)
  },
  delete<T = any>(url: string, config?: RequestConfig) {
    return instance.delete<unknown, ApiResponse<T>>(url, config)
  }
}

export default request
