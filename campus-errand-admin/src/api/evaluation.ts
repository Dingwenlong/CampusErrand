import request from '@/utils/request'
import type { Evaluation, PageResult } from '@/types'

export const getEvaluationList = (params: { current?: number; size?: number; rating?: number }) => {
  return request.get('/admin/evaluation/list', { params })
}

export const deleteEvaluation = (id: number) => {
  return request.post(`/admin/evaluation/${id}/delete`)
}
