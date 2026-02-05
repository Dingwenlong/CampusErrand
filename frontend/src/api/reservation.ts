import request from '../utils/request'

export interface ReservationRecord {
  id: number
  seatId: number
  seatNo: string
  slot: string
  startTime: string
  endTime: string
  status: string
  type: 'appointment' | 'checkin'
}

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// Mock Data
const mockHistory: ReservationRecord[] = [
  {
    id: 101,
    seatId: 1,
    seatNo: 'A-01',
    slot: 'morning',
    startTime: '2023-10-20 08:00',
    endTime: '2023-10-20 12:00',
    status: 'completed',
    type: 'appointment'
  },
  {
    id: 102,
    seatId: 2,
    seatNo: 'B-05',
    slot: 'afternoon',
    startTime: '2023-10-18 14:00',
    endTime: '2023-10-18 18:00',
    status: 'violation',
    type: 'appointment'
  },
  {
    id: 103,
    seatId: 3,
    seatNo: 'C-10',
    slot: 'evening',
    startTime: '2023-10-15 18:00',
    endTime: '2023-10-15 22:00',
    status: 'cancelled',
    type: 'appointment'
  },
  {
    id: 104,
    seatId: 4,
    seatNo: 'A-02',
    slot: 'morning',
    startTime: '2023-10-22 09:00',
    endTime: '2023-10-22 11:30',
    status: 'active',
    type: 'checkin'
  }
]

export function createReservation(data: { seatId: number, slot: string }) {
  if (USE_MOCK) {
    return new Promise((resolve) => setTimeout(resolve, 1000))
  }
  return request({
    url: '/reservations',
    method: 'post',
    data
  })
}

export function getMyHistory() {
  if (USE_MOCK) {
    return Promise.resolve(mockHistory)
  }
  return request<ReservationRecord[]>({
    url: '/reservations/my-history',
    method: 'get'
  })
}

export function getActiveReservation() {
  if (USE_MOCK) return Promise.resolve(null)
  return request<any>({
    url: '/reservations/active',
    method: 'get'
  })
}

export interface AppealData {
  reason: string
  appealType: 'PHONE_DEAD' | 'QR_CODE_DAMAGED' | 'GPS_ERROR' | 'SYSTEM_ERROR' | 'OTHER'
  images?: string[]
}

export interface AppealRecord {
  id: number
  reservationId: number
  userId: number
  appealType: string
  reason: string
  images: string
  status: 'pending' | 'approved' | 'rejected'
  reply: string
  creditReturned: number
  creditAmount: number
  createTime: string
  updateTime: string
  username?: string
  realName?: string
  seatNo?: string
}

export function submitAppeal(id: number, data: AppealData) {
  if (USE_MOCK) {
    return new Promise((resolve) => setTimeout(resolve, 1000))
  }
  return request({
    url: `/reservations/${id}/appeal`,
    method: 'post',
    data
  })
}

export function getMyAppeals() {
  if (USE_MOCK) {
    return Promise.resolve([])
  }
  return request<AppealRecord[]>({
    url: '/reservations/my-appeals',
    method: 'get'
  })
}

export function getAllAppeals() {
  if (USE_MOCK) {
    return Promise.resolve([])
  }
  return request<AppealRecord[]>({
    url: '/reservations/appeals',
    method: 'get'
  })
}

export function reviewAppeal(id: number, data: { status: 'approved' | 'rejected', reply: string }) {
  if (USE_MOCK) {
    return new Promise((resolve) => setTimeout(resolve, 500))
  }
  return request({
    url: `/reservations/appeals/${id}/review`,
    method: 'post',
    data
  })
}

export function checkIn(id: number, params: { qrCode?: string; lat?: number; lng?: number }) {
  if (USE_MOCK) return Promise.resolve()
  return request({
    url: `/reservations/${id}/check-in`,
    method: 'post',
    data: params
  })
}

export function temporaryLeave(id: number) {
  if (USE_MOCK) return Promise.resolve()
  return request({
    url: `/reservations/${id}/leave`,
    method: 'post'
  })
}

export function releaseSeat(id: number) {
  if (USE_MOCK) return Promise.resolve()
  return request({
    url: `/reservations/${id}/release`,
    method: 'post'
  })
}