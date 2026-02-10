import request from '@/utils/request'

export const getEvaluationList = (params: any) => {
  return request.get('/admin/evaluation/list', { params })
}

export const deleteEvaluation = (id: number) => {
  return request.delete(`/admin/evaluation/${id}`)
}

export const getEvaluationStats = () => {
  return request.get('/admin/evaluation/stats')
}
