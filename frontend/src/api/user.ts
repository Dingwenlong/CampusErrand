import request from '../utils/request'
import type { UserInfo } from './auth' // Reuse User interface or define a fuller one here

export interface UserManage extends UserInfo {
  realName: string | null
  status: 'active' | 'banned'
  creditScore: number
  lastLoginTime: string | null
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// Mock Data
let mockUsers: UserManage[] = [
  {
    id: 1,
    username: 'student01',
    realName: '张三',
    role: 'student',
    status: 'active',
    creditScore: 95,
    phone: '13800138000',
    lastLoginTime: '2023-10-20 09:30:00'
  },
  {
    id: 2,
    username: 'lib01',
    realName: '李四',
    role: 'librarian',
    status: 'active',
    creditScore: 100,
    phone: '13900139000',
    lastLoginTime: '2023-10-21 08:00:00'
  },
  {
    id: 3,
    username: 'student02',
    realName: '王五',
    role: 'student',
    status: 'banned',
    creditScore: 50,
    phone: '13700137000',
    lastLoginTime: '2023-10-19 14:20:00'
  },
  {
    id: 4,
    username: 'admin',
    realName: '系统管理员',
    role: 'admin',
    status: 'active',
    creditScore: 100,
    phone: '13600136000',
    lastLoginTime: '2023-10-22 10:00:00'
  }
]

export function getUsers(params?: any) {
  if (USE_MOCK) {
    return Promise.resolve(mockUsers)
  }
  return request<PageResult<UserManage>>({
    url: '/users/list',
    method: 'get',
    params
  })
}

export function createUser(data: Partial<UserManage>) {
  if (USE_MOCK) {
    const newUser = { 
      id: Date.now(), 
      ...data, 
      status: 'active', 
      lastLoginTime: '-' 
    } as UserManage
    mockUsers.unshift(newUser)
    return Promise.resolve(newUser)
  }
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

export function updateUser(id: number, data: Partial<UserManage>) {
  if (USE_MOCK) {
    const index = mockUsers.findIndex(u => u.id === id)
    if (index > -1) {
      mockUsers[index] = { ...mockUsers[index], ...data } as UserManage
    }
    return Promise.resolve()
  }
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

export function updateUserStatus(id: number, status: string) {
  if (USE_MOCK) {
    const user = mockUsers.find(u => u.id === id)
    if (user) user.status = status as any
    return Promise.resolve()
  }
  return request({
    url: `/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}
