import request from '../utils/request'

export interface OccupancyRecord {
  id: number
  reservationId: number
  userId: number
  seatId: number
  username?: string
  realName?: string
  seatNo?: string
  area?: string
  checkInTime: string
  lastDetectedTime: string
  totalAwayMinutes: number
  occupancyStatus: 'normal' | 'warning' | 'occupied'
  warningCount: number
}

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// Mock Data
const mockOccupancyData: OccupancyRecord[] = [
  {
    id: 1,
    reservationId: 101,
    userId: 3,
    seatId: 1,
    username: 'student',
    realName: '张三',
    seatNo: 'A-01',
    area: 'A区',
    checkInTime: '2024-01-15 08:30:00',
    lastDetectedTime: '2024-01-15 09:00:00',
    totalAwayMinutes: 30,
    occupancyStatus: 'normal',
    warningCount: 0
  },
  {
    id: 2,
    reservationId: 102,
    userId: 4,
    seatId: 2,
    username: 'student2',
    realName: '李四',
    seatNo: 'A-02',
    area: 'A区',
    checkInTime: '2024-01-15 08:00:00',
    lastDetectedTime: '2024-01-15 08:15:00',
    totalAwayMinutes: 75,
    occupancyStatus: 'warning',
    warningCount: 1
  },
  {
    id: 3,
    reservationId: 103,
    userId: 5,
    seatId: 3,
    username: 'student3',
    realName: '王五',
    seatNo: 'B-01',
    area: 'B区',
    checkInTime: '2024-01-15 07:30:00',
    lastDetectedTime: '2024-01-15 07:30:00',
    totalAwayMinutes: 120,
    occupancyStatus: 'occupied',
    warningCount: 2
  }
]

export function getOccupancyMonitoring() {
  if (USE_MOCK) {
    return Promise.resolve(mockOccupancyData)
  }
  return request<OccupancyRecord[]>({
    url: '/occupancy/monitoring',
    method: 'get'
  })
}

export function manualCheckout(reservationId: number, reason: string) {
  if (USE_MOCK) {
    return new Promise((resolve) => setTimeout(resolve, 500))
  }
  return request({
    url: `/occupancy/${reservationId}/checkout`,
    method: 'post',
    data: { reason }
  })
}

export function performOccupancyCheck() {
  if (USE_MOCK) {
    return new Promise((resolve) => setTimeout(resolve, 500))
  }
  return request({
    url: '/occupancy/check-now',
    method: 'post'
  })
}
