import request from '../utils/request'

export interface Seat {
  id?: number
  seatNo: string // Changed from number to seatNo to match backend
  area: string
  status: 'available' | 'occupied' | 'maintenance'
  type: string
  x?: number
  y?: number
}

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// Mock Data
const mockSeats = Array.from({ length: 24 }, (_, i) => ({
  id: i + 1,
  seatNo: `${String.fromCharCode(65 + Math.floor(i / 6))}-${(i % 6) + 1}`,
  area: 'A区',
  status: Math.random() > 0.7 ? 'occupied' : Math.random() > 0.9 ? 'maintenance' : 'available',
  type: '标准座',
  x: 100 + (i % 6) * 100,
  y: 100 + Math.floor(i / 6) * 80
}))

export function getSeats(params?: { area?: string }) {
  if (USE_MOCK) {
    return Promise.resolve(mockSeats as unknown as Seat[])
  }
  return request<Seat[]>({
    url: '/seats',
    method: 'get',
    params
  })
}

export function addSeat(data: Partial<Seat>) {
  if (USE_MOCK) {
    return Promise.resolve(true)
  }
  return request<boolean>({
    url: '/seats',
    method: 'post',
    data
  })
}

export function updateSeat(data: Partial<Seat>) {
  if (USE_MOCK) {
    return Promise.resolve(true)
  }
  return request<boolean>({
    url: '/seats',
    method: 'put',
    data
  })
}

export function updateSeatStatus(id: number, status: string) {
  if (USE_MOCK) {
    const seat = mockSeats.find(s => s.id === id)
    if (seat) seat.status = status as any
    return Promise.resolve(true)
  }
  return request<boolean>({
    url: `/seats/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function deleteSeat(id: number) {
  if (USE_MOCK) {
    return Promise.resolve(true)
  }
  return request<boolean>({
    url: `/seats/${id}`,
    method: 'delete'
  })
}

export function batchDeleteSeats(ids: number[]) {
  if (USE_MOCK) {
    return Promise.resolve(true)
  }
  return request<boolean>({
    url: '/seats/batch/delete',
    method: 'post',
    data: ids
  })
}

export function batchImportSeats(seats: Partial<Seat>[]) {
  if (USE_MOCK) {
    return Promise.resolve(true)
  }
  return request<boolean>({
    url: '/seats/batch/import',
    method: 'post',
    data: seats
  })
}

export function deleteAllSeats() {
  if (USE_MOCK) {
    return Promise.resolve(true)
  }
  return request<boolean>({
    url: '/seats/all',
    method: 'delete'
  })
}
