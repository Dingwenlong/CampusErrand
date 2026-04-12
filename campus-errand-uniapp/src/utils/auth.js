// 认证相关工具函数

const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'
const USER_ID_KEY = 'userId'
const MOCK_TOKEN_KEY = 'isMockToken'

// 获取token
export function getToken() {
  return uni.getStorageSync(TOKEN_KEY)
}

// 设置token
export function setToken(token) {
  return uni.setStorageSync(TOKEN_KEY, token)
}

// 移除token
export function removeToken() {
  return uni.removeStorageSync(TOKEN_KEY)
}

// 获取用户信息
export function getUserInfo() {
  return uni.getStorageSync(USER_INFO_KEY) || null
}

// 获取用户ID
export function getUserId() {
  const userId = uni.getStorageSync(USER_ID_KEY)
  if (userId === '' || userId === undefined || userId === null) {
    return null
  }
  const normalized = Number(userId)
  return Number.isNaN(normalized) ? null : normalized
}

// 设置登录会话
export function setSession(session = {}, options = {}) {
  const { token, userId, ...userInfo } = session

  if (token) {
    setToken(token)
  }

  if (userId !== undefined && userId !== null) {
    uni.setStorageSync(USER_ID_KEY, Number(userId))
  }

  if (Object.keys(userInfo).length > 0 || userId !== undefined) {
    uni.setStorageSync(USER_INFO_KEY, {
      id: userId ?? null,
      ...userInfo
    })
  }

  uni.setStorageSync(MOCK_TOKEN_KEY, !!options.isMockToken)
}

// 清除登录会话
export function clearSession() {
  removeToken()
  uni.removeStorageSync(USER_INFO_KEY)
  uni.removeStorageSync(USER_ID_KEY)
  uni.removeStorageSync(MOCK_TOKEN_KEY)
}

// 检查是否已登录
export function isLoggedIn() {
  return !!getToken()
}

export default {
  getToken,
  getUserInfo,
  getUserId,
  setToken,
  setSession,
  removeToken,
  clearSession,
  isLoggedIn
}
