import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref<any>(null)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('admin_token', newToken)
  }

  const clearToken = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('admin_token')
  }

  const setUserInfo = (info: any) => {
    userInfo.value = info
  }

  return {
    token,
    userInfo,
    setToken,
    clearToken,
    setUserInfo
  }
})
