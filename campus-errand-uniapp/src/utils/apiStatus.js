import { reactive, readonly } from 'vue'

/**
 * API调用状态管理
 * 用于管理全局API调用状态，包括加载中、错误信息等
 */

// 全局API状态
const globalApiState = reactive({
  loading: false,
  error: null,
  requestCount: 0 // 正在进行的请求数
})

// 创建局部API状态（用于组件内）
export const createApiState = () => {
  return reactive({
    loading: false,
    error: null,
    data: null
  })
}

/**
 * 包装API调用，自动管理loading和error状态
 * @param {Function} apiCall - API调用函数
 * @param {Object} state - 状态对象（组件内的state或全局state）
 * @param {Object} options - 配置选项
 * @param {boolean} options.showLoading - 是否显示加载提示，默认true
 * @param {string} options.loadingText - 加载提示文字
 * @param {boolean} options.showError - 是否显示错误提示，默认true
 * @returns {Promise} API调用结果
 */
export const useApi = async (apiCall, state = null, options = {}) => {
  const {
    showLoading = true,
    loadingText = '加载中...',
    showError = true
  } = options

  // 使用传入的state或全局state
  const targetState = state || globalApiState

  // 开始请求
  targetState.loading = true
  targetState.error = null
  globalApiState.requestCount++

  // 显示加载提示
  if (showLoading && globalApiState.requestCount === 1) {
    uni.showLoading({
      title: loadingText,
      mask: true
    })
  }

  try {
    const result = await apiCall()
    targetState.data = result.data
    return result
  } catch (error) {
    targetState.error = error
    // 错误已在request.js中统一处理，这里不再重复提示
    throw error
  } finally {
    // 结束请求
    targetState.loading = false
    globalApiState.requestCount--

    // 隐藏加载提示
    if (globalApiState.requestCount === 0) {
      uni.hideLoading()
    }
  }
}

/**
 * 组合式函数：在Vue组件中使用API
 * @param {Function} apiCall - API调用函数
 * @param {Object} options - 配置选项
 * @returns {Object} { loading, error, data, execute }
 */
export const useApiComposable = (apiCall, options = {}) => {
  const state = createApiState()

  const execute = async (...args) => {
    return useApi(() => apiCall(...args), state, options)
  }

  return {
    loading: readonly(state.loading),
    error: readonly(state.error),
    data: readonly(state.data),
    execute
  }
}

/**
 * 获取全局API状态（只读）
 */
export const useGlobalApiState = () => {
  return readonly(globalApiState)
}

/**
 * 检查是否正在加载
 */
export const isLoading = () => {
  return globalApiState.loading || globalApiState.requestCount > 0
}

/**
 * 清除错误状态
 */
export const clearError = (state = null) => {
  if (state) {
    state.error = null
  } else {
    globalApiState.error = null
  }
}

export default {
  createApiState,
  useApi,
  useApiComposable,
  useGlobalApiState,
  isLoading,
  clearError
}
