/**
 * API测试模块
 * 用于验证API调用的正确性及异常处理逻辑
 */

import api from '@/api/index.js'
import { useApi, createApiState } from './apiStatus.js'

// 测试配置
const TEST_CONFIG = {
  // 测试用户数据
  testUser: {
    code: 'test_code_' + Date.now(),
    nickname: '测试用户',
    avatar: 'https://example.com/avatar.jpg',
    gender: 1
  },
  // 测试任务数据
  testTask: {
    title: '测试任务',
    description: '这是一个测试任务',
    taskType: 1,
    pickupAddress: '宿舍楼A',
    deliveryAddress: '图书馆',
    reward: 10.00,
    payPassword: '123456'
  }
}

/**
 * 运行所有API测试
 * @returns {Promise<Object>} 测试结果
 */
export const runAllTests = async () => {
  const results = {
    total: 0,
    passed: 0,
    failed: 0,
    details: []
  }

  console.log('========== 开始API测试 ==========')

  // 1. 测试认证模块
  await testAuthModule(results)

  // 2. 测试用户模块
  await testUserModule(results)

  // 3. 测试钱包模块
  await testWalletModule(results)

  // 4. 测试任务模块
  await testTaskModule(results)

  // 5. 测试消息模块
  await testMessageModule(results)

  // 6. 测试评价模块
  await testEvaluationModule(results)

  console.log('========== API测试完成 ==========')
  console.log(`总计: ${results.total}, 通过: ${results.passed}, 失败: ${results.failed}`)

  return results
}

/**
 * 测试认证模块
 */
const testAuthModule = async (results) => {
  console.log('\n----- 测试认证模块 -----')

  // 测试1: 微信登录（参数验证）
  await runTest(results, '认证-微信登录参数验证', async () => {
    try {
      await api.auth.wxLogin({})
      throw new Error('应该抛出参数错误')
    } catch (error) {
      if (error.message === '登录code不能为空') {
        return true
      }
      throw error
    }
  })

  // 测试2: 正常登录
  await runTest(results, '认证-微信登录', async () => {
    const res = await api.auth.wxLogin(TEST_CONFIG.testUser)
    if (res.code === 200 && res.data.token) {
      // 保存token供后续测试使用
      uni.setStorageSync('token', res.data.token)
      uni.setStorageSync('userInfo', res.data)
      return true
    }
    return false
  })
}

/**
 * 测试用户模块
 */
const testUserModule = async (results) => {
  console.log('\n----- 测试用户模块 -----')

  // 测试1: 获取用户信息
  await runTest(results, '用户-获取用户信息', async () => {
    const res = await api.user.getInfo()
    return res.code === 200 && res.data.user && res.data.wallet
  })

  // 测试2: 参数验证
  await runTest(results, '用户-参数验证', async () => {
    try {
      await api.user.getById('')
      throw new Error('应该抛出参数错误')
    } catch (error) {
      return error.message === '用户ID不能为空'
    }
  })
}

/**
 * 测试钱包模块
 */
const testWalletModule = async (results) => {
  console.log('\n----- 测试钱包模块 -----')

  // 测试1: 获取钱包余额
  await runTest(results, '钱包-获取余额', async () => {
    const res = await api.wallet.getBalance()
    return res.code === 200 && res.data.hasOwnProperty('balance')
  })

  // 测试2: 检查支付密码
  await runTest(results, '钱包-检查支付密码', async () => {
    const res = await api.wallet.hasPayPassword()
    return res.code === 200 && typeof res.data === 'boolean'
  })

  // 测试3: 设置支付密码（参数验证）
  await runTest(results, '钱包-设置支付密码参数验证', async () => {
    try {
      await api.wallet.setPayPassword({ payPassword: '123456' })
      throw new Error('应该抛出参数错误')
    } catch (error) {
      return error.message === '确认密码不能为空'
    }
  })

  // 测试4: 密码一致性验证
  await runTest(results, '钱包-密码一致性验证', async () => {
    try {
      await api.wallet.setPayPassword({
        payPassword: '123456',
        confirmPassword: '654321'
      })
      throw new Error('应该抛出密码不一致错误')
    } catch (error) {
      return error.message === '两次输入的密码不一致'
    }
  })

  // 测试5: 获取交易流水
  await runTest(results, '钱包-获取交易流水', async () => {
    const res = await api.wallet.getTransactions()
    return res.code === 200 && Array.isArray(res.data.records)
  })
}

/**
 * 测试任务模块
 */
const testTaskModule = async (results) => {
  console.log('\n----- 测试任务模块 -----')

  // 测试1: 获取任务列表
  await runTest(results, '任务-获取列表', async () => {
    const res = await api.task.getList()
    return res.code === 200 && Array.isArray(res.data.records)
  })

  // 测试2: 发布任务参数验证
  await runTest(results, '任务-发布参数验证', async () => {
    try {
      await api.task.publish({ title: '测试' })
      throw new Error('应该抛出参数错误')
    } catch (error) {
      return error.message.includes('不能为空')
    }
  })

  // 测试3: 赏金验证
  await runTest(results, '任务-赏金验证', async () => {
    try {
      await api.task.publish({
        ...TEST_CONFIG.testTask,
        reward: -10
      })
      throw new Error('应该抛出赏金错误')
    } catch (error) {
      return error.message === '赏金金额必须大于0'
    }
  })

  // 测试4: 获取任务详情（参数验证）
  await runTest(results, '任务-详情参数验证', async () => {
    try {
      await api.task.getDetail('')
      throw new Error('应该抛出参数错误')
    } catch (error) {
      return error.message === '任务ID不能为空'
    }
  })
}

/**
 * 测试消息模块
 */
const testMessageModule = async (results) => {
  console.log('\n----- 测试消息模块 -----')

  // 测试1: 获取消息列表
  await runTest(results, '消息-获取列表', async () => {
    const res = await api.message.getList()
    return res.code === 200 && Array.isArray(res.data.records)
  })

  // 测试2: 获取未读消息数
  await runTest(results, '消息-未读数', async () => {
    const res = await api.message.getUnreadCount()
    return res.code === 200 && typeof res.data === 'number'
  })

  // 测试3: 标记已读（参数验证）
  await runTest(results, '消息-标记已读参数验证', async () => {
    try {
      await api.message.markAsRead('')
      throw new Error('应该抛出参数错误')
    } catch (error) {
      return error.message === '消息ID不能为空'
    }
  })
}

/**
 * 测试评价模块
 */
const testEvaluationModule = async (results) => {
  console.log('\n----- 测试评价模块 -----')

  // 测试1: 获取收到的评价
  await runTest(results, '评价-获取收到的评价', async () => {
    const res = await api.evaluation.getReceivedEvaluations()
    return res.code === 200 && Array.isArray(res.data.records)
  })

  // 测试2: 提交评价参数验证
  await runTest(results, '评价-提交参数验证', async () => {
    try {
      await api.evaluation.submit({ rating: 5 })
      throw new Error('应该抛出参数错误')
    } catch (error) {
      return error.message === '任务ID不能为空'
    }
  })

  // 测试3: 评分范围验证
  await runTest(results, '评价-评分范围验证', async () => {
    try {
      await api.evaluation.submit({
        taskId: 1,
        toUserId: 2,
        rating: 6
      })
      throw new Error('应该抛出评分错误')
    } catch (error) {
      return error.message === '评分必须在1-5之间'
    }
  })
}

/**
 * 运行单个测试
 */
const runTest = async (results, name, testFn) => {
  results.total++
  try {
    const startTime = Date.now()
    const passed = await testFn()
    const duration = Date.now() - startTime

    if (passed) {
      results.passed++
      results.details.push({ name, status: 'passed', duration })
      console.log(`✓ ${name} (${duration}ms)`)
    } else {
      results.failed++
      results.details.push({ name, status: 'failed', duration, error: '测试断言失败' })
      console.log(`✗ ${name} (${duration}ms) - 断言失败`)
    }
  } catch (error) {
    results.failed++
    results.details.push({ name, status: 'failed', error: error.message })
    console.log(`✗ ${name} - ${error.message}`)
  }
}

/**
 * 在页面中使用API的示例
 */
export const usageExample = `
// ========== 在Vue组件中使用API示例 ==========

// 方式1: 直接调用API
import api from '@/api/index.js'

async function loadUserInfo() {
  try {
    const res = await api.user.getInfo()
    console.log('用户信息:', res.data)
  } catch (error) {
    console.error('获取失败:', error)
  }
}

// 方式2: 使用API状态管理（自动处理loading）
import { useApi, createApiState } from '@/utils/apiStatus.js'

export default {
  setup() {
    const state = createApiState()
    
    const loadData = async () => {
      await useApi(() => api.task.getList(), state)
    }
    
    return { state, loadData }
  }
}

// 方式3: 使用组合式函数
import { useApiComposable } from '@/utils/apiStatus.js'

export default {
  setup() {
    const { loading, error, data, execute } = useApiComposable(api.task.getList)
    
    // 在onMounted中调用
    onMounted(() => execute())
    
    return { loading, error, data, refresh: execute }
  }
}

// 方式4: 带参数调用
const publishTask = async (taskData) => {
  try {
    const res = await api.task.publish(taskData)
    uni.showToast({ title: '发布成功', icon: 'success' })
    return res.data
  } catch (error) {
    // 错误已在request.js中统一提示
    console.error('发布失败:', error)
  }
}
`

export default {
  runAllTests,
  usageExample
}
