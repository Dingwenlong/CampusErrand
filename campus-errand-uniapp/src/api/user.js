import http, { BASE_URL } from '@/utils/request.js'

/**
 * 用户相关API
 */
export default {
  /**
   * 获取当前用户信息
   * @returns {Promise} 用户信息和钱包信息
   */
  getInfo() {
    return http.get('/user/info')
  },

  /**
   * 获取用户信息（兼容profile页面）
   * @returns {Promise} 用户信息
   */
  getUserInfo() {
    return http.get('/user/info')
  },

  /**
   * 更新用户信息
   * @param {Object} data - 用户数据
   * @param {string} data.nickname - 昵称
   * @param {string} data.avatar - 头像URL
   * @param {number} data.gender - 性别
   * @param {string} data.phone - 手机号
   * @returns {Promise} 更新结果
   */
  updateProfile(data) {
    if (!data || Object.keys(data).length === 0) {
      return Promise.reject(new Error('更新数据不能为空'))
    }
    return http.put('/user/profile', data)
  },

  /**
   * 上传头像
   * @param {string} filePath - 本地文件路径
   * @returns {Promise} 上传结果
   */
  uploadAvatar(filePath) {
    if (!filePath) {
      return Promise.reject(new Error('文件路径不能为空'))
    }
    
    return http.upload('/user/avatar', filePath, {
      name: 'file'
    })
  },

  /**
   * 获取用户信息（根据ID）
   * @param {number|string} id - 用户ID
   * @returns {Promise} 用户信息
   */
  getById(id) {
    if (!id) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    return http.get(`/user/${id}`)
  },

  /**
   * 获取实名认证状态
   * @returns {Promise} 认证状态
   */
  getVerifyStatus() {
    return http.get('/user/verify-status')
  },

  /**
   * 提交实名认证
   * @param {Object} data - 认证数据
   * @param {string} data.realName - 真实姓名
   * @param {string} data.idCard - 身份证号
   * @param {string} data.frontImage - 身份证正面照片URL
   * @param {string} data.backImage - 身份证反面照片URL
   * @returns {Promise} 提交结果
   */
  submitVerify(data) {
    if (!data.realName || !data.idCard) {
      return Promise.reject(new Error('姓名和身份证号不能为空'))
    }
    if (!data.frontImage || !data.backImage) {
      return Promise.reject(new Error('请上传身份证正反面照片'))
    }
    return http.post('/user/verify', data)
  },

  /**
   * 上传身份证照片
   * @param {string} filePath - 本地文件路径
   * @returns {Promise} 上传结果
   */
  uploadIdCard(filePath) {
    if (!filePath) {
      return Promise.reject(new Error('文件路径不能为空'))
    }
    return http.upload('/user/upload-idcard', filePath, {
      name: 'file'
    })
  }
}
