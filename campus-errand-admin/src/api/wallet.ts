import request from '@/utils/request'
import type { Transaction, PageResult } from '@/types'

export const getTransactionList = (params: { current?: number; size?: number; userId?: number; type?: number }) => {
  return request.get('/admin/transaction/list', { params })
}

export const getWalletStats = () => {
  return request.get('/admin/wallet/stats')
}
