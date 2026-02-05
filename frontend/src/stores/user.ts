import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { type LoginParams, type RegisterParams, type UserInfo, login, register, guestLogin, wechatLogin, logout } from '../api/auth'
import { getMenus, type MenuItem } from '../api/menu'
import { getActiveReservation } from '../api/reservation'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  // 从 localStorage 恢复 userInfo，防止刷新丢失
  const userInfo = ref<UserInfo | null>(
    localStorage.getItem('userInfo') 
      ? JSON.parse(localStorage.getItem('userInfo')!) 
      : null
  )
  const menus = ref<MenuItem[]>([])
  
  // 是否绑定了个人信息
  const isInfoBound = computed(() => {
    if (!userInfo.value) return true // 未登录时不触发重定向
    if (userInfo.value.role === 'admin' || userInfo.value.role === 'guest') return true
    return !!(userInfo.value.phone && userInfo.value.realName)
  })

  // 新用户标记：如果未绑定信息，则视为新用户/需要绑定用户
  const isNewUser = computed(() => !isInfoBound.value)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 预约状态管理
  const reservation = ref<{
    id: number
    seatId: number
    seatNo: string
    startTime: number // 开始时间戳
    deadline: number // 截止时间戳
    status: 'reserved' | 'checked_in' | 'away'
  } | null>(
    localStorage.getItem('reservation') 
      ? JSON.parse(localStorage.getItem('reservation')!) 
      : null
  )

  function setReservation(id: number, seatId: number, seatNo: string, startTime: number, deadline: number) {
    const data = { id, seatId, seatNo, startTime, deadline, status: 'reserved' as const }
    reservation.value = data
    localStorage.setItem('reservation', JSON.stringify(data))
  }

  function setAway() {
    if (!reservation.value) return
    const deadline = Date.now() + 30 * 60 * 1000 // 30分钟暂离时间
    const data = { ...reservation.value, deadline, status: 'away' as const }
    reservation.value = data
    localStorage.setItem('reservation', JSON.stringify(data))
  }

  function checkIn() {
    if (!reservation.value) return
    const data = { ...reservation.value, status: 'checked_in' as const }
    reservation.value = data
    localStorage.setItem('reservation', JSON.stringify(data))
  }

  function clearReservation() {
    reservation.value = null
    localStorage.removeItem('reservation')
  }

  function clearToken() {
    token.value = ''
    userInfo.value = null
    menus.value = []
    clearReservation() // 登出时清除预约
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  async function handleLogin(params: LoginParams) {
    try {
      const res = await login(params)
      
      setToken(res.token)
      setUserInfo(res.userInfo)
      
      // 登录成功后获取菜单
      await fetchMenus()
      
      return true
    } catch (error) {
      console.error(error)
      return false
    }
  }

  async function handleRegister(params: RegisterParams) {
    try {
      const res = await register(params)
      
      setToken(res.token)
      setUserInfo(res.userInfo)
      
      await fetchMenus()
      
      return true
    } catch (error) {
      console.error(error)
      return false
    }
  }

  async function handleGuestLogin() {
    try {
      const res = await guestLogin()
      
      setToken(res.token)
      setUserInfo(res.userInfo)
      
      await fetchMenus()
      
      return true
    } catch (error) {
      console.error(error)
      return false
    }
  }

  // 微信登录模拟
  async function handleWeChatLogin(code: string) {
    try {
      const res = await wechatLogin(code)
      
      setToken(res.token)
      setUserInfo(res.userInfo)
      await fetchMenus()
      
      return true
    } catch (error) {
      return false
    }
  }

  // 完成绑定后更新用户信息
  function completeBinding(updatedInfo?: Partial<UserInfo>) {
    if (userInfo.value && updatedInfo) {
      setUserInfo({ ...userInfo.value, ...updatedInfo })
    }
  }

  async function handleLogout() {
    try {
      await logout()
    } finally {
      clearToken()
    }
  }

  async function fetchMenus() {
    if (!userInfo.value?.role) return
    try {
      const res = await getMenus(userInfo.value.role)
      menus.value = res
    } catch (error) {
      console.error('Failed to fetch menus', error)
    }
  }

  async function syncReservationStatus() {
    try {
      const res = await getActiveReservation()
      const activeRes = (res as any).data
      
      if (activeRes) {
        // 同步后端状态到本地
        const deadline = activeRes.deadline ? new Date(activeRes.deadline).getTime() : Date.now() + 15 * 60 * 1000
        const startTime = activeRes.startTime ? new Date(activeRes.startTime).getTime() : Date.now()
        const data = {
          id: activeRes.id,
          seatId: activeRes.seatId,
          seatNo: activeRes.seatNo || '',
          startTime: startTime,
          deadline: deadline,
          status: activeRes.status as any
        }
        reservation.value = data
        localStorage.setItem('reservation', JSON.stringify(data))
      } else {
        // 如果后端没有活跃预约，清理本地缓存
        clearReservation()
      }
    } catch (e) {
      console.error('Failed to sync reservation status:', e)
    }
  }

  return {
    token,
    userInfo,
    menus,
    isNewUser,
    reservation,
    setReservation,
    setAway,
    checkIn,
    clearReservation,
    clearToken,
    handleLogin,
    handleRegister,
    handleGuestLogin,
    handleWeChatLogin,
    completeBinding,
    handleLogout,
    fetchMenus,
    syncReservationStatus
  }
})
