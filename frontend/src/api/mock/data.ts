// Mock User Data
export interface User {
  id: number
  username: string
  realName: string
  role: 'student' | 'librarian' | 'admin'
  status: 'active' | 'banned'
  creditScore: number
  phone: string
  lastLogin: string
}

export interface ReservationRecord {
  id: number
  seatNumber: string
  startTime: string
  endTime: string
  status: 'active' | 'completed' | 'cancelled' | 'violation'
  type: 'appointment' | 'checkin'
}

// Mock API Functions
export const getMockUsers = () => {
  return new Promise<User[]>((resolve) => {
    setTimeout(() => {
      resolve([
        {
          id: 1,
          username: 'student01',
          realName: '张三',
          role: 'student',
          status: 'active',
          creditScore: 95,
          phone: '13800138000',
          lastLogin: '2023-10-20 09:30:00'
        },
        {
          id: 2,
          username: 'lib01',
          realName: '李四',
          role: 'librarian',
          status: 'active',
          creditScore: 100,
          phone: '13900139000',
          lastLogin: '2023-10-21 08:00:00'
        },
        {
          id: 3,
          username: 'student02',
          realName: '王五',
          role: 'student',
          status: 'banned',
          creditScore: 50,
          phone: '13700137000',
          lastLogin: '2023-10-19 14:20:00'
        },
        {
          id: 4,
          username: 'admin',
          realName: '系统管理员',
          role: 'admin',
          status: 'active',
          creditScore: 100,
          phone: '13600136000',
          lastLogin: '2023-10-22 10:00:00'
        }
      ])
    }, 500)
  })
}

export const getMockHistory = () => {
  return new Promise<ReservationRecord[]>((resolve) => {
    setTimeout(() => {
      resolve([
        {
          id: 101,
          seatNumber: 'A-01',
          startTime: '2023-10-20 08:00',
          endTime: '2023-10-20 12:00',
          status: 'completed',
          type: 'appointment'
        },
        {
          id: 102,
          seatNumber: 'B-05',
          startTime: '2023-10-18 14:00',
          endTime: '2023-10-18 18:00',
          status: 'violation',
          type: 'appointment'
        },
        {
          id: 103,
          seatNumber: 'C-10',
          startTime: '2023-10-15 18:00',
          endTime: '2023-10-15 22:00',
          status: 'cancelled',
          type: 'appointment'
        },
        {
          id: 104,
          seatNumber: 'A-02',
          startTime: '2023-10-22 09:00',
          endTime: '2023-10-22 11:30',
          status: 'active',
          type: 'checkin'
        }
      ])
    }, 500)
  })
}
