/**
 * API统一入口
 * 集中管理所有API模块
 */

import auth from './auth.js'
import user from './user.js'
import task from './task.js'
import order from './order.js'
import wallet from './wallet.js'
import evaluation from './evaluation.js'
import message from './message.js'
import config from './config.js'

// 统一导出所有API
export const api = {
  auth,
  user,
  task,
  order,
  wallet,
  evaluation,
  message,
  config
}

// 默认导出
export default api

// 单独导出各模块（便于按需导入）
export { auth, user, task, order, wallet, evaluation, message, config }
