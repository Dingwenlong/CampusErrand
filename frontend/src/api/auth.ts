import request from '../utils/request'

export interface LoginParams {
  username: string
  password?: string
}

export interface RegisterParams {
  username: string
  password: string
  phone: string
}

export interface LoginResult {
  token: string
  userInfo: UserInfo
}

export interface UserInfo {
  id: number
  username: string
  role: string
  avatar?: string | null
  phone?: string | null
  realName?: string | null
  studentId?: string | null
}

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// 模拟后端存储，解决 Mock 环境下登录重置的问题
const getMockUsers = () => {
  const users = localStorage.getItem('mock_db_users')
  if (!users) {
    // 初始化一些默认用户
    const initialUsers: Record<string, UserInfo> = {
      'admin': { id: 1, username: 'admin', role: 'admin', avatar: 'https://api.dicebear.com/9.x/icons/svg?seed=admin', phone: '13800138000', realName: '系统管理员' },
      'student': { id: 3, username: 'student', role: 'student', avatar: 'https://api.dicebear.com/9.x/icons/svg?seed=student', phone: '13800138001', realName: '学生' },
      'lib': { id: 2, username: 'lib', role: 'librarian', avatar: 'https://api.dicebear.com/9.x/icons/svg?seed=lib', phone: '13800138002', realName: '图书馆员' }
    }
    localStorage.setItem('mock_db_users', JSON.stringify(initialUsers))
    return initialUsers
  }
  return JSON.parse(users)
}

const saveMockUser = (username: string, info: UserInfo) => {
  const users = getMockUsers()
  users[username] = info
  localStorage.setItem('mock_db_users', JSON.stringify(users))
}

// Mock implementation
const mockLogin = (data: LoginParams): Promise<LoginResult> => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const users = getMockUsers()
      if (users[data.username]) {
        resolve({
          token: 'mock-token-' + Date.now(),
          userInfo: users[data.username]
        })
        return
      }

      let role = 'student'
      let phone = '13800138000'
      let realName = '张三'
      if (data.username.includes('admin')) {
        role = 'admin'
      } else if (data.username.includes('lib')) {
        role = 'librarian'
      } else if (data.username.includes('new')) {
        phone = ''
        realName = ''
      }
      
      const userInfo = {
        id: 1,
        username: data.username,
        role: role,
        avatar: 'https://api.dicebear.com/9.x/icons/svg?seed=' + data.username,
        phone: phone,
        realName: realName
      }

      // 只有非空信息才存入模拟数据库
      if (phone && realName) {
        saveMockUser(data.username, userInfo)
      }

      resolve({
        token: 'mock-token-' + Date.now(),
        userInfo: userInfo
      })
    }, 500)
  })
}

const mockRegister = (data: RegisterParams): Promise<LoginResult> => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const users = getMockUsers()
      
      // 检查手机号唯一性
      const phoneExists = Object.values(users).some((u: any) => u.phone === data.phone)
      if (phoneExists) {
        reject(new Error('该手机号已被注册'))
        return
      }

      const userInfo = {
        id: Date.now(),
        username: data.username,
        role: 'student',
        avatar: 'https://api.dicebear.com/9.x/icons/svg?seed=' + data.username,
        phone: data.phone,
        realName: ''
      }
      // 注册时不保存，等到绑定成功再保存（或者这里可以保存，因为手机号已经有了）
      // 根据用户要求，手机号在注册时绑定，所以这里存入模拟数据库
      saveMockUser(data.username, userInfo as UserInfo)

      resolve({
        token: 'mock-token-' + Date.now(),
        userInfo
      })
    }, 500)
  })
}

export function login(data: LoginParams) {
  if (USE_MOCK) {
    return mockLogin(data)
  }
  return request<LoginResult>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data: RegisterParams) {
  if (USE_MOCK) {
    return mockRegister(data)
  }
  return request<LoginResult>({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function guestLogin() {
  // 访客登录始终使用前端模拟数据，不调用后端接口
  return new Promise<LoginResult>((resolve) => {
    setTimeout(() => {
      resolve({
        token: 'guest-token-' + Date.now(),
        userInfo: {
          id: 0,
          username: 'guest',
          role: 'guest',
          avatar: 'https://api.dicebear.com/9.x/icons/svg?seed=guest'
        }
      })
    }, 500)
  })
}

export function getUserInfo() {
  if (USE_MOCK) {
    return Promise.resolve({
      id: 1,
      username: 'mockUser',
      role: 'student'
    })
  }
  return request<UserInfo>({
    url: '/auth/info',
    method: 'get'
  })
}

export function logout() {
  if (USE_MOCK) {
    return Promise.resolve()
  }
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export function wechatLogin(code: string) {
  if (USE_MOCK) {
    return new Promise<LoginResult>((resolve) => {
      setTimeout(() => {
        resolve({
          token: 'wechat-token-' + Date.now(),
          userInfo: {
            id: 2,
            username: 'WeChatUser',
            role: 'student',
            avatar: 'https://api.dicebear.com/9.x/icons/svg?seed=WeChat',
            phone: '',
            realName: ''
          }
        })
      }, 500)
    })
  }
  return request<LoginResult>({
    url: '/auth/wechat-login',
    method: 'post',
    data: { code }
  })
}

export function updateProfile(data: Partial<UserInfo>) {
  if (USE_MOCK) {
    return new Promise<boolean>((resolve) => {
      setTimeout(() => {
        const username = data.username || ''
        if (username) {
          const users = getMockUsers()
          const currentInfo = users[username] || {}
          saveMockUser(username, { ...currentInfo, ...data } as UserInfo)
        }
        resolve(true)
      }, 500)
    })
  }
  return request<boolean>({
    url: '/auth/profile',
    method: 'put',
    data
  })
}
