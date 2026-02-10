import request from '@/utils/request'

export const getTransactions = (params: any) => {
  return request.get('/admin/wallet/transactions', { params })
}

export const getWalletStats = () => {
  return request.get('/admin/wallet/stats')
}
