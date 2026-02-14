function getDateStr(daysAgo: number): string {
  const date = new Date()
  date.setDate(date.getDate() - daysAgo)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

export interface MockDataConfig {
  enabled: boolean
  taskStatus: {
    data: Array<{ name: string; value: number; status: number }>
  }
  amountTrend: {
    dates: string[]
    amounts: number[]
  }
  userGrowth: {
    dates: string[]
    newUsers: number[]
    totalUsers: number[]
  }
}

export const mockDataConfig: MockDataConfig = {
  enabled: true,
  taskStatus: {
    data: [
      { name: '待接单', value: 12, status: 0 },
      { name: '已接单', value: 8, status: 1 },
      { name: '待取件', value: 5, status: 2 },
      { name: '配送中', value: 3, status: 3 },
      { name: '待确认', value: 2, status: 4 },
      { name: '已完成', value: 45, status: 5 },
      { name: '已取消', value: 7, status: 6 }
    ]
  },
  amountTrend: {
    dates: [
      getDateStr(6), getDateStr(5), getDateStr(4), getDateStr(3),
      getDateStr(2), getDateStr(1), getDateStr(0)
    ],
    amounts: [128.5, 256.8, 189.2, 312.6, 278.4, 425.0, 356.8]
  },
  userGrowth: {
    dates: [
      getDateStr(6), getDateStr(5), getDateStr(4), getDateStr(3),
      getDateStr(2), getDateStr(1), getDateStr(0)
    ],
    newUsers: [5, 8, 12, 6, 15, 10, 18],
    totalUsers: [85, 93, 105, 111, 126, 136, 154]
  }
}

export const mockStats = {
  userCount: 154,
  taskCount: 82,
  totalAmount: 1947.3
}
