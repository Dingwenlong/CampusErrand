import { getToken, removeToken } from './auth.js'

// API基础配置
const BASE_URL = 'http://localhost:8080/api'
const TIMEOUT = 30000 // 30秒超时
const MAX_RETRY = 3 // 最大重试次数
const RETRY_DELAY = 1000 // 重试延迟(毫秒)

// 请求队列（用于取消重复请求）
const pendingRequests = new Map()

// 生成请求key
const generateRequestKey = (config) => {
  return `${config.method}_${config.url}_${JSON.stringify(config.data || {})}`
}

// 添加请求到队列
const addPendingRequest = (config) => {
  const key = generateRequestKey(config)
  if (pendingRequests.has(key)) {
    pendingRequests.get(key).abort()
  }
  const controller = { abort: () => {} }
  pendingRequests.set(key, controller)
  return controller
}

// 移除请求从队列
const removePendingRequest = (config) => {
  const key = generateRequestKey(config)
  pendingRequests.delete(key)
}

// 请求拦截器
const requestInterceptor = (options) => {
  const token = getToken()
  
  // 设置默认header
  options.header = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Requested-With': 'XMLHttpRequest',
    ...options.header
  }
  
  // 添加认证token
  if (token) {
    options.header['Authorization'] = `Bearer ${token}`
  }
  
  // 设置超时
  options.timeout = options.timeout || TIMEOUT
  
  // 请求日志（开发环境）
  if (process.env.NODE_ENV === 'development') {
    console.log(`[Request] ${options.method || 'GET'} ${options.url}`, options.data)
  }
  
  // 打印完整请求配置用于调试
  console.log('[Request Config] url:', options.url, 'method:', options.method, 'data:', options.data)
  
  return options
}

// 响应拦截器
const responseInterceptor = (response) => {
  // 响应日志（开发环境）
  if (process.env.NODE_ENV === 'development') {
    console.log(`[Response]`, response.data)
  }
  
  // 打印完整响应用于调试
  console.log('[Response Config] statusCode:', response.statusCode, 'data:', response.data)
  
  return response
}

// 错误处理
const handleError = (error, options) => {
  const { showError = true, errorMsg } = options
  
  let message = errorMsg || '请求失败'
  
  if (error.errMsg) {
    // 网络错误
    if (error.errMsg.includes('timeout')) {
      message = '请求超时，请检查网络'
    } else if (error.errMsg.includes('fail')) {
      message = '网络连接失败，请检查网络设置'
    } else {
      message = error.errMsg
    }
  } else if (error.data) {
    // 业务错误
    message = error.data.message || `请求失败(${error.data.code})`
  }
  
  if (showError) {
    uni.showToast({
      title: message,
      icon: 'none',
      duration: 2000
    })
  }
  
  return Promise.reject({ ...error, message })
}

// 延迟函数
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

// 核心请求函数
const request = (options, retryCount = 0) => {
  return new Promise((resolve, reject) => {
    // 应用请求拦截器
    const config = requestInterceptor({ ...options })
    
    // 添加请求到队列（用于防止重复请求）
    const controller = addPendingRequest(config)
    
    // 构建完整URL
    const fullUrl = config.url.startsWith('http') ? config.url : BASE_URL + config.url
    
    const requestTask = uni.request({
      url: fullUrl,
      method: config.method || 'GET',
      data: config.data,
      header: config.header,
      timeout: config.timeout,
      success: (res) => {
        removePendingRequest(config)
        
        // 应用响应拦截器
        const response = responseInterceptor(res)
        
        // HTTP状态码处理
        if (response.statusCode === 200) {
          const data = response.data
          
          // 业务状态码处理
          if (data.code === 200) {
            resolve(data)
          } else if (data.code === 401 || data.code === 401001) {
            // Token过期或未登录
            const isMockToken = uni.getStorageSync('isMockToken') || false
            handleAuthError(undefined, isMockToken)
            reject({ ...data, type: 'auth' })
          } else if (data.code === 401002) {
            // Token无效
            const isMockToken = uni.getStorageSync('isMockToken') || false
            handleAuthError('登录状态无效，请重新登录', isMockToken)
            reject({ ...data, type: 'auth' })
          } else {
            // 其他业务错误
            if (config.showError !== false) {
              uni.showToast({
                title: data.message || '操作失败',
                icon: 'none'
              })
            }
            reject({ ...data, type: 'business' })
          }
        } else if (response.statusCode === 401) {
          const isMockToken = uni.getStorageSync('isMockToken') || false
          handleAuthError(undefined, isMockToken)
          reject({ statusCode: 401, type: 'auth' })
        } else if (response.statusCode >= 500) {
          // 服务器错误，尝试重试
          if (retryCount < MAX_RETRY && config.retry !== false) {
            console.warn(`请求失败(${response.statusCode})，${RETRY_DELAY}ms后重试(${retryCount + 1}/${MAX_RETRY})`)
            delay(RETRY_DELAY).then(() => {
              request(options, retryCount + 1).then(resolve).catch(reject)
            })
          } else {
            handleError({ data: { message: '服务器繁忙，请稍后重试' } }, config)
            reject({ statusCode: response.statusCode, type: 'server' })
          }
        } else {
          handleError({ data: { message: `请求失败(${response.statusCode})` } }, config)
          reject({ statusCode: response.statusCode, type: 'http' })
        }
      },
      fail: (err) => {
        removePendingRequest(config)
        
        // 网络错误重试
        if (retryCount < MAX_RETRY && config.retry !== false) {
          console.warn(`网络错误，${RETRY_DELAY}ms后重试(${retryCount + 1}/${MAX_RETRY})`)
          delay(RETRY_DELAY).then(() => {
            request(options, retryCount + 1).then(resolve).catch(reject)
          })
        } else {
          handleError(err, config)
          reject({ ...err, type: 'network' })
        }
      }
    })
    
    // 保存abort函数
    controller.abort = () => {
      requestTask.abort && requestTask.abort()
    }
  })
}

// 处理认证错误
const handleAuthError = (msg, isMockToken = false) => {
  if (isMockToken) {
    return
  }
  
  removeToken()
  uni.removeStorageSync('userInfo')
  
  uni.showToast({
    title: msg || '登录已过期，请重新登录',
    icon: 'none',
    duration: 2000
  })
  
  setTimeout(() => {
    uni.navigateTo({
      url: '/pages/login/index'
    })
  }, 1500)
}

// 导出请求方法
export const http = {
  get(url, params = {}, options = {}) {
    return request({
      url,
      method: 'GET',
      data: params,
      ...options
    })
  },
  
  post(url, data = {}, options = {}) {
    return request({
      url,
      method: 'POST',
      data,
      ...options
    })
  },
  
  put(url, data = {}, options = {}) {
    return request({
      url,
      method: 'PUT',
      data,
      ...options
    })
  },
  
  delete(url, params = {}, options = {}) {
    return request({
      url,
      method: 'DELETE',
      data: params,
      ...options
    })
  },
  
  // 上传文件
  upload(url, filePath, options = {}) {
    const token = getToken()
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: url.startsWith('http') ? url : BASE_URL + url,
        filePath,
        name: options.name || 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : '',
          ...options.header
        },
        formData: options.formData || {},
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data)
              if (data.code === 200) {
                resolve(data)
              } else {
                uni.showToast({
                  title: data.message || '上传失败',
                  icon: 'none'
                })
                reject(data)
              }
            } catch (e) {
              resolve({ data: res.data })
            }
          } else {
            reject({ statusCode: res.statusCode })
          }
        },
        fail: reject
      })
    })
  }
}

// 默认导出兼容原有代码
export default http

// 导出配置供其他模块使用
export { BASE_URL, TIMEOUT, MAX_RETRY }
