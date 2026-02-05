// 通用响应类型
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 分页类型
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 用户类型
export interface User {
  id: number
  nickname: string
  avatar: string
  gender: number
  phone: string
  creditScore: number
  status: number
  createTime: string
}

// 任务类型
export interface Task {
  id: number
  userId: number
  runnerId: number | null
  title: string
  description: string
  taskType: number
  reward: number
  status: number
  pickupAddress: string
  deliveryAddress: string
  contactName: string
  contactPhone: string
  createTime: string
  acceptTime: string | null
  deliveryTime: string | null
  completeTime: string | null
}

// 订单类型
export interface Order {
  id: number
  taskId: number
  publisherId: number
  runnerId: number
  status: number
  reward: number
  createTime: string
}

// 交易记录类型
export interface Transaction {
  id: number
  userId: number
  type: number
  direction: number
  amount: number
  balance: number
  relatedId: number | null
  status: number
  remark: string
  createTime: string
}

// 评价类型
export interface Evaluation {
  id: number
  taskId: number
  fromUserId: number
  toUserId: number
  rating: number
  content: string
  tags: string
  isAnonymous: number
  createTime: string
}

// 仪表盘数据类型
export interface DashboardData {
  userCount: number
  taskCount: number
  orderCount: number
  totalAmount: number
  todayUserCount: number
  todayTaskCount: number
  todayOrderCount: number
  todayAmount: number
}
