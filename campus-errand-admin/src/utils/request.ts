import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
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
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      message.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('admin_token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    message.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
