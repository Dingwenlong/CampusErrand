import request from '@/utils/request'

export const getTransactions = (params: any) => {
  return request.get('/admin/wallet/transactions', { params })
}

export const getWalletStats = () => {
  return request.get('/admin/wallet/stats')
}

export const adminRecharge = (data: { userId: number; amount: number; remark?: string }) => {
  return request.post('/admin/wallet/recharge', data)
}
