<template>
  <view class="login-container">
    <!-- 背景装饰 -->
    <view class="bg-decoration">
      <view class="gradient-orb orb-1"></view>
      <view class="gradient-orb orb-2"></view>
      <view class="gradient-orb orb-3"></view>
      <view class="grid-pattern"></view>
    </view>

    <!-- 主内容区 -->
    <view class="content-wrapper">
      <!-- 品牌展示区域 -->
      <view class="brand-section">
        <view class="logo-container">
          <view class="logo-glow"></view>
          <view class="logo-wrapper">
            <text class="logo-icon">🏃</text>
          </view>
        </view>
        <view class="brand-text">
          <text class="brand-name">校园跑腿</text>
          <text class="brand-slogan">让校园生活更便捷</text>
        </view>
      </view>

      <!-- 登录表单区域 -->
      <view class="login-form">
        <!-- 协议同意区域 -->
        <view class="agreement-section">
          <view
            class="checkbox-wrapper"
            :class="{ checked: agreed }"
            @click="toggleAgreement"
          >
            <view class="checkbox">
              <view v-if="agreed" class="check-mark">
                <text class="check-icon">✓</text>
              </view>
            </view>
            <view class="agreement-text">
              <text class="text-normal">我已阅读并同意</text>
              <text class="text-link" @click.stop="showUserAgreement">《用户协议》</text>
              <text class="text-normal">和</text>
              <text class="text-link" @click.stop="showPrivacyPolicy">《隐私政策》</text>
            </view>
          </view>
        </view>

        <!-- 登录按钮 -->
        <button
          class="login-btn"
          :class="{ disabled: !agreed || loading, active: agreed && !loading }"
          :disabled="!agreed || loading"
          @click="handleLogin"
        >
          <view class="btn-content">
            <view v-if="loading" class="loading-wrapper">
              <view class="loading-spinner"></view>
            </view>
            <template v-else>
              <view class="wechat-icon">
                <text class="wechat-icon-text">微</text>
              </view>
              <text class="btn-text">微信一键登录</text>
            </template>
          </view>
          <view class="btn-shine"></view>
        </button>

        <!-- 用户二登录入口 -->
        <view class="guest-section" v-if="user2LoginEnabled">
          <button class="guest-btn" @click="loginAsUser2">
            <text class="guest-text">用户二登录</text>
            <view class="arrow-icon">→</view>
          </button>
        </view>

        <!-- 游客入口 -->
        <view class="guest-section" v-else>
          <button class="guest-btn" @click="enterAsGuest">
            <text class="guest-text">暂不登录，先看看</text>
            <view class="arrow-icon">→</view>
          </button>
        </view>
      </view>
    </view>

    <!-- 底部版权 -->
    <view class="footer-section">
      <view class="footer-content">
        <view class="safe-badge">
          <text class="badge-icon">🔒</text>
          <text class="badge-text">安全登录保障</text>
        </view>
        <text class="copyright">登录即代表您同意相关服务条款</text>
      </view>
    </view>

    <!-- 用户协议弹窗 -->
    <view v-if="showAgreementModal" class="modal-overlay">
      <view class="modal-container">
        <view class="modal-header">
          <view class="header-icon">📋</view>
          <text class="modal-title">用户协议</text>
          <view class="close-btn" @click="closeAgreement">
            <text class="close-icon">✕</text>
          </view>
        </view>
        <scroll-view class="modal-body" scroll-y>
          <view class="agreement-content">
            <view v-if="userAgreementContent" class="dynamic-content">
              <rich-text :nodes="userAgreementContent"></rich-text>
            </view>
            <template v-else>
              <view class="content-intro">
                <text class="intro-title">欢迎使用校园跑腿</text>
                <text class="intro-desc">请您仔细阅读以下条款，确保您充分理解并同意后再开始使用我们的服务。</text>
              </view>
              <view class="content-sections">
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-number">01</view>
                    <text class="item-title">服务条款</text>
                  </view>
                  <text class="item-desc">本协议是您与校园跑腿平台之间关于使用本平台服务所订立的协议。通过使用我们的服务，您同意接受本协议的所有条款和条件。</text>
                </view>
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-number">02</view>
                    <text class="item-title">账号安全</text>
                  </view>
                  <text class="item-desc">您在使用微信登录时，我们仅获取您的公开信息（昵称、头像），用于创建账号。我们承诺保护您的账号安全，不会将您的信息用于未经授权的用途。</text>
                </view>
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-number">03</view>
                    <text class="item-title">用户行为规范</text>
                  </view>
                  <text class="item-desc">您承诺遵守国家法律法规，不得利用本平台从事违法活动。您应当对自己的行为负责，不得发布违法违规内容。</text>
                </view>
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-number">04</view>
                    <text class="item-title">免责声明</text>
                  </view>
                  <text class="item-desc">平台将尽力保障服务的稳定性，但不保证服务不会中断。对于因不可抗力或第三方原因导致的服务中断，平台不承担责任。</text>
                </view>
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-number">05</view>
                    <text class="item-title">协议修改</text>
                  </view>
                  <text class="item-desc">平台有权在必要时修改本协议，修改后会通过适当方式通知用户。继续使用服务即视为接受修改后的协议。</text>
                </view>
              </view>
            </template>
            <view class="content-footer">
              <text class="update-time">最后更新时间：{{ userAgreementUpdateTime || '2024年1月' }}</text>
            </view>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <button class="footer-btn cancel" @click="closeAgreement">取消</button>
          <button class="footer-btn confirm" @click="agreeAndClose('agreement')">同意并继续</button>
        </view>
      </view>
      <!-- 遮罩层点击区域 -->
      <view class="modal-mask" @click="closeAgreement"></view>
    </view>

    <!-- 隐私政策弹窗 -->
    <view v-if="showPrivacyModal" class="modal-overlay">
      <view class="modal-container">
        <view class="modal-header">
          <view class="header-icon privacy">🛡️</view>
          <text class="modal-title">隐私政策</text>
          <view class="close-btn" @click="closePrivacy">
            <text class="close-icon">✕</text>
          </view>
        </view>
        <scroll-view class="modal-body" scroll-y>
          <view class="agreement-content">
            <view v-if="privacyPolicyContent" class="dynamic-content">
              <rich-text :nodes="privacyPolicyContent"></rich-text>
            </view>
            <template v-else>
              <view class="content-intro privacy-intro">
                <view class="privacy-lock">🔐</view>
                <text class="intro-title">您的隐私对我们很重要</text>
                <text class="intro-desc">我们高度重视您的隐私保护，承诺仅收集必要信息并采用严格的安全措施保护您的数据。</text>
              </view>
              <view class="content-sections">
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-icon">📊</view>
                    <text class="item-title">信息收集</text>
                  </view>
                  <text class="item-desc">我们仅收集必要的用户信息：微信昵称、头像、手机号（实名认证时）。我们不会收集与提供服务无关的个人信息。</text>
                </view>
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-icon">🎯</view>
                    <text class="item-title">信息使用</text>
                  </view>
                  <text class="item-desc">您的信息仅用于提供服务，包括：身份验证、订单处理、客户服务。我们不会向第三方出售或共享您的个人信息。</text>
                </view>
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-icon">🔒</view>
                    <text class="item-title">信息保护</text>
                  </view>
                  <text class="item-desc">我们采用业界标准的加密技术保护您的数据安全。所有数据传输均通过HTTPS加密，存储数据经过脱敏处理。</text>
                </view>
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-icon">✅</view>
                    <text class="item-title">用户权利</text>
                  </view>
                  <text class="item-desc">您有权查看、修改或删除您的个人信息。如需行使这些权利，请联系我们的客服团队。</text>
                </view>
                <view class="content-item">
                  <view class="item-header">
                    <view class="item-icon">📞</view>
                    <text class="item-title">联系我们</text>
                  </view>
                  <text class="item-desc">如有隐私相关问题，请联系客服：privacy@campuserrand.com 或拨打客服热线 400-XXX-XXXX。</text>
                </view>
              </view>
            </template>
            <view class="content-footer">
              <text class="update-time">最后更新时间：{{ privacyPolicyUpdateTime || '2024年1月' }}</text>
            </view>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <button class="footer-btn cancel" @click="closePrivacy">取消</button>
          <button class="footer-btn confirm" @click="agreeAndClose('privacy')">同意并继续</button>
        </view>
      </view>
      <!-- 遮罩层点击区域 -->
      <view class="modal-mask" @click="closePrivacy"></view>
    </view>
  </view>
</template>

<script>
import authApi from '@/api/auth.js'
import configApi from '@/api/config.js'
import socket from '@/utils/socket.js'
import { setSession } from '@/utils/auth.js'

export default {
  data() {
    return {
      agreed: false,
      loading: false,
      redirectUrl: '',
      showAgreementModal: false,
      showPrivacyModal: false,
      userAgreementContent: '',
      privacyPolicyContent: '',
      userAgreementUpdateTime: '',
      privacyPolicyUpdateTime: '',
      user2LoginEnabled: false
    }
  },
  onLoad(options) {
    if (options.redirect) {
      this.redirectUrl = decodeURIComponent(options.redirect)
    }
    this.loadAgreementContent()
    this.loadUser2LoginConfig()
  },
  methods: {
    async loadUser2LoginConfig() {
      try {
        const res = await configApi.getConfigByKey('user2_login_enabled')
        if (res.code === 200 && (res.data === 'true' || res.data === true)) {
          this.user2LoginEnabled = true
        }
      } catch (error) {
        console.log('加载用户二登录配置失败')
      }
    },

    async loadAgreementContent() {
      try {
        const [agreementRes, privacyRes, agreementTimeRes, privacyTimeRes] = await Promise.allSettled([
          configApi.getConfigByKey('user_agreement'),
          configApi.getConfigByKey('privacy_policy'),
          configApi.getConfigByKey('user_agreement_update_time'),
          configApi.getConfigByKey('privacy_policy_update_time')
        ])
        
        if (agreementRes.status === 'fulfilled' && agreementRes.value?.code === 200 && agreementRes.value.data) {
          this.userAgreementContent = agreementRes.value.data
        }
        if (privacyRes.status === 'fulfilled' && privacyRes.value?.code === 200 && privacyRes.value.data) {
          this.privacyPolicyContent = privacyRes.value.data
        }
        if (agreementTimeRes.status === 'fulfilled' && agreementTimeRes.value?.code === 200 && agreementTimeRes.value.data) {
          this.userAgreementUpdateTime = agreementTimeRes.value.data
        }
        if (privacyTimeRes.status === 'fulfilled' && privacyTimeRes.value?.code === 200 && privacyTimeRes.value.data) {
          this.privacyPolicyUpdateTime = privacyTimeRes.value.data
        }
      } catch (error) {
        console.log('加载协议内容失败，使用默认内容')
      }
    },

    toggleAgreement() {
      this.agreed = !this.agreed
    },

    showUserAgreement() {
      this.showAgreementModal = true
    },

    closeAgreement() {
      this.showAgreementModal = false
    },

    showPrivacyPolicy() {
      this.showPrivacyModal = true
    },

    closePrivacy() {
      this.showPrivacyModal = false
    },

    agreeAndClose(type) {
      this.agreed = true
      if (type === 'agreement') {
        this.closeAgreement()
      } else {
        this.closePrivacy()
      }
    },

    async handleLogin() {
      console.log('=== 登录按钮被点击 ===')
      
      if (!this.agreed) {
        uni.showToast({
          title: '请先同意用户协议和隐私政策',
          icon: 'none'
        })
        return
      }

      this.loading = true

      try {
        // 步骤1：获取用户信息（必须在点击后立即调用）
        console.log('步骤1：开始获取用户信息...')
        const userProfile = await this.getUserProfile()
        console.log('获取用户信息成功:', userProfile)

        // 步骤2：获取微信登录 code
        console.log('步骤2：开始获取微信登录 code...')
        const loginRes = await this.wxLogin()
        console.log('获取 code 成功:', loginRes.code)

        const loginData = {
          code: loginRes.code,
          nickname: userProfile.nickname,
          avatar: userProfile.avatar,
          gender: userProfile.gender
        }

        // 步骤3：调用后端登录接口
        console.log('步骤3：调用后端登录接口...')
        console.log('请求数据:', loginData)
        const res = await authApi.wxLogin(loginData)
        console.log('后端响应:', res)

        if (res.code === 200) {
          const { token, userId, nickname, avatar, isNewUser } = res.data
          setSession({
            token,
            userId,
            nickname,
            avatar,
            isNewUser
          })
          socket.connect()

          uni.showToast({
            title: '登录成功',
            icon: 'success'
          })

          setTimeout(() => {
            this.navigateAfterLogin()
          }, 1500)
        } else {
          throw new Error(res.message || '登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        uni.showToast({
          title: error.message || '登录失败，请重试',
          icon: 'none',
          duration: 2000
        })
      } finally {
        this.loading = false
      }
    },

    wxLogin() {
      return new Promise((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: (res) => {
            if (res.code) {
              resolve(res)
            } else {
              reject(new Error('获取登录凭证失败'))
            }
          },
          fail: (err) => {
            reject(new Error('微信登录失败: ' + err.errMsg))
          }
        })
      })
    },

    getUserProfile() {
      return new Promise((resolve, reject) => {
        if (uni.getUserProfile) {
          uni.getUserProfile({
            desc: '用于完善用户资料',
            success: (res) => {
              const userInfo = res.userInfo
              resolve({
                nickname: userInfo.nickName,
                avatar: userInfo.avatarUrl,
                gender: userInfo.gender
              })
            },
            fail: (err) => {
              console.log('获取用户信息失败:', err)
              resolve({
                nickname: '微信用户',
                avatar: '',
                gender: 0
              })
            }
          })
        } else {
          uni.getUserInfo({
            success: (res) => {
              const userInfo = res.userInfo
              resolve({
                nickname: userInfo.nickName,
                avatar: userInfo.avatarUrl,
                gender: userInfo.gender
              })
            },
            fail: () => {
              resolve({
                nickname: '微信用户',
                avatar: '',
                gender: 0
              })
            }
          })
        }
      })
    },

    navigateAfterLogin() {
      if (this.redirectUrl) {
        uni.navigateTo({
          url: this.redirectUrl,
          fail: () => {
            uni.switchTab({
              url: this.redirectUrl,
              fail: () => {
                uni.switchTab({
                  url: '/pages/index/index'
                })
              }
            })
          }
        })
      } else {
        uni.switchTab({
          url: '/pages/index/index'
        })
      }
    },

    enterAsGuest() {
      uni.showModal({
        title: '提示',
        content: '游客模式下部分功能将受到限制，是否继续？',
        confirmColor: '#FF6B35',
        success: (res) => {
          if (res.confirm) {
            uni.switchTab({
              url: '/pages/index/index'
            })
          }
        }
      })
    },

    loginAsUser2() {
      const mockUser2 = {
        token: 'mock_user2_token_' + Date.now(),
        userId: 2,
        nickname: '用户二',
        avatar: '',
        gender: 1,
        phone: '13800000002',
        studentId: '20240002',
        verified: true,
        balance: 100.00,
        reputation: 95
      }
      
      setSession(mockUser2, { isMockToken: true })
      socket.connect()
      
      uni.showToast({
        title: '用户二登录成功',
        icon: 'success'
      })
      
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/index/index'
        })
      }, 1500)
    }
  }
}
</script>

<style lang="scss" scoped>
// ============================================
// 设计系统变量
// ============================================
$primary-gradient: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
$primary-gradient-hover: linear-gradient(135deg, #FF8C5A 0%, #FF6B35 100%);
$wechat-green: #07C160;
$wechat-green-hover: #06ad56;
$bg-gradient: linear-gradient(180deg, #FFF8F0 0%, #FFFFFF 50%, #FFF5EB 100%);

$shadow-sm: 0 2rpx 8rpx rgba(74, 55, 40, 0.04);
$shadow-md: 0 4rpx 16rpx rgba(74, 55, 40, 0.08);
$shadow-lg: 0 8rpx 32rpx rgba(74, 55, 40, 0.12);
$shadow-primary: 0 8rpx 24rpx rgba(255, 107, 53, 0.35);
$shadow-wechat: 0 8rpx 24rpx rgba(7, 193, 96, 0.35);

$radius-sm: 12rpx;
$radius-md: 16rpx;
$radius-lg: 24rpx;
$radius-xl: 32rpx;
$radius-full: 9999rpx;

// ============================================
// 登录页面主体
// ============================================
.login-container {
  min-height: 100vh;
  background: $bg-gradient;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

// ============================================
// 背景装饰
// ============================================
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80rpx);
  opacity: 0.4;
}

.orb-1 {
  width: 600rpx;
  height: 600rpx;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  top: -200rpx;
  right: -200rpx;
  animation: float 8s ease-in-out infinite;
}

.orb-2 {
  width: 400rpx;
  height: 400rpx;
  background: linear-gradient(135deg, #FFB347 0%, #FF6B35 100%);
  top: 400rpx;
  left: -150rpx;
  animation: float 10s ease-in-out infinite 1s;
}

.orb-3 {
  width: 300rpx;
  height: 300rpx;
  background: linear-gradient(135deg, #FFF0E5 0%, #FFB347 100%);
  bottom: 200rpx;
  right: -100rpx;
  animation: float 12s ease-in-out infinite 2s;
}

.grid-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(255, 107, 53, 0.03) 1rpx, transparent 1rpx),
    linear-gradient(90deg, rgba(255, 107, 53, 0.03) 1rpx, transparent 1rpx);
  background-size: 60rpx 60rpx;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30rpx) scale(1.05); }
}

// ============================================
// 主内容区
// ============================================
.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  position: relative;
  z-index: 1;
}

// ============================================
// 品牌展示区域
// ============================================
.brand-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.logo-container {
  position: relative;
  margin-bottom: 40rpx;
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 240rpx;
  height: 240rpx;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.1); }
}

.logo-wrapper {
  width: 160rpx;
  height: 160rpx;
  background: $primary-gradient;
  border-radius: $radius-xl;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-primary;
  position: relative;
  z-index: 1;
  
  &::before {
    content: '';
    position: absolute;
    inset: -4rpx;
    background: linear-gradient(135deg, #FF8C5A 0%, #E85A2A 100%);
    border-radius: $radius-xl;
    z-index: -1;
    opacity: 0.5;
    filter: blur(8rpx);
  }
}

.logo-icon {
  font-size: 80rpx;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}

.brand-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.brand-name {
  font-size: 48rpx;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12rpx;
  letter-spacing: 4rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.05);
}

.brand-slogan {
  font-size: 28rpx;
  color: var(--color-text-secondary);
  font-weight: 400;
  letter-spacing: 2rpx;
}

// ============================================
// 登录表单区域（扁平化设计）
// ============================================
.login-form {
  width: 100%;
  max-width: 640rpx;
  padding: 0 40rpx;
}

// ============================================
// 协议同意区域
// ============================================
.agreement-section {
  margin-bottom: 48rpx;
}

.checkbox-wrapper {
  display: flex;
  align-items: flex-start;
  padding: 20rpx 0;
  transition: all 0.3s ease;

  &.checked {
    .checkbox {
      background: var(--color-primary);
      border-color: var(--color-primary);
    }
  }
}

.checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #d9d9d9;
  border-radius: 10rpx;
  margin-right: 16rpx;
  margin-top: 2rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  background: #fff;
  
  .checkbox-wrapper.checked & {
    background: var(--color-primary);
    border-color: var(--color-primary);
  }
}

.check-mark {
  animation: checkPop 0.3s ease;
}

@keyframes checkPop {
  0% { transform: scale(0); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.check-icon {
  color: #fff;
  font-size: 24rpx;
  font-weight: bold;
}

.agreement-text {
  flex: 1;
  font-size: 26rpx;
  line-height: 1.6;
  color: var(--color-text-secondary);
}

.text-normal {
  color: var(--color-text-secondary);
}

.text-link {
  color: var(--color-primary-dark);
  font-weight: 600;
  text-decoration: none;
  position: relative;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2rpx;
    background: var(--color-primary);
    transform: scaleX(0);
    transition: transform 0.3s ease;
  }
  
  &:active::after {
    transform: scaleX(1);
  }
}

// ============================================
// 登录按钮
// ============================================
.login-btn {
  width: 100%;
  height: 96rpx;
  background: #e8e8e8;
  border-radius: $radius-lg;
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &.active {
    background: linear-gradient(135deg, $wechat-green 0%, $wechat-green-hover 100%);
    box-shadow: $shadow-wechat;
    
    &:active {
      transform: translateY(2rpx);
      box-shadow: 0 4rpx 12rpx rgba(7, 193, 96, 0.25);
    }
    
    .btn-shine {
      animation: shine 2s infinite;
    }
  }
  
  &.disabled {
    opacity: 0.6;
  }
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  position: relative;
  z-index: 1;
}

.wechat-icon {
  width: 46rpx;
  height: 46rpx;
  margin-right: 16rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
}

.wechat-icon-text {
  color: #fff;
  font-size: 26rpx;
  font-weight: 700;
  line-height: 1;
}

.btn-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.btn-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  pointer-events: none;
}

@keyframes shine {
  0% { left: -100%; }
  100% { left: 200%; }
}

// 加载动画
.loading-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// ============================================
// 游客入口
// ============================================
.guest-section {
  margin-top: 32rpx;
  display: flex;
  justify-content: center;
}

.guest-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 32rpx;
  background: transparent;
  border: none;

  &:active {
    opacity: 0.7;
  }
}

.guest-text {
  font-size: 28rpx;
  color: var(--color-text-tertiary);
  margin-right: 8rpx;
}

.arrow-icon {
  font-size: 28rpx;
  color: #94a3b8;
  transition: transform 0.3s ease;

  .guest-btn:active & {
    transform: translateX(8rpx);
  }
}

// ============================================
// 底部版权
// ============================================
.footer-section {
  padding: 40rpx;
  position: relative;
  z-index: 1;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.safe-badge {
  display: flex;
  align-items: center;
  padding: 12rpx 24rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: $radius-full;
  margin-bottom: 20rpx;
  box-shadow: $shadow-sm;
}

.badge-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.badge-text {
  font-size: 24rpx;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.copyright {
  font-size: 22rpx;
  color: #94a3b8;
  text-align: center;
}

// ============================================
// 弹窗遮罩层
// ============================================
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.modal-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8rpx);
  z-index: 1;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

// ============================================
// 弹窗容器
// ============================================
.modal-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 680rpx;
  max-height: 85vh;
  background: #fff;
  border-radius: $radius-xl;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
  animation: modalSlideUp 0.3s ease;
  z-index: 2;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translate(-50%, calc(-50% + 40rpx));
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%);
  }
}

// ============================================
// 弹窗头部
// ============================================
.modal-header {
  display: flex;
  align-items: center;
  padding: 32rpx 40rpx;
  background: linear-gradient(135deg, #fafafa 0%, var(--color-bg-secondary) 100%);
  border-bottom: 1rpx solid var(--color-border-light);
}

.header-icon {
  width: 64rpx;
  height: 64rpx;
  background: $primary-gradient;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-right: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
  
  &.privacy {
    background: linear-gradient(135deg, rgba(123, 196, 127, 0.12) 0%, rgba(123, 196, 127, 0.24) 100%);
  }
}

.modal-title {
  flex: 1;
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a1a;
}

.close-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-border-light);
  transition: all 0.3s ease;
  
  &:active {
    background: var(--color-border);
    transform: rotate(90deg);
  }
}

.close-icon {
  font-size: 28rpx;
  color: var(--color-text-tertiary);
  font-weight: 300;
}

// ============================================
// 弹窗内容区
// ============================================
.modal-body {
  flex: 1;
  max-height: 600rpx;
}

.agreement-content {
  padding: 40rpx;
}

.dynamic-content {
  font-size: 28rpx;
  line-height: 1.8;
  color: #333;
  
  :deep(h1), :deep(h2), :deep(h3) {
    margin: 24rpx 0 16rpx;
    font-weight: 600;
  }
  
  :deep(p) {
    margin-bottom: 16rpx;
  }
  
  :deep(ul), :deep(ol) {
    padding-left: 40rpx;
    margin-bottom: 16rpx;
  }
  
  :deep(li) {
    margin-bottom: 8rpx;
  }
}

// 内容介绍
.content-intro {
  text-align: center;
  padding: 32rpx;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 248, 240, 0.5) 100%);
  border-radius: $radius-lg;
  margin-bottom: 40rpx;
  
  &.privacy-intro {
    background: linear-gradient(135deg, rgba(123, 196, 127, 0.08) 0%, rgba(232, 245, 233, 0.5) 100%);
  }
}

.privacy-lock {
  font-size: 64rpx;
  margin-bottom: 16rpx;
}

.intro-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12rpx;
}

.intro-desc {
  display: block;
  font-size: 26rpx;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

// 内容章节
.content-sections {
  margin-bottom: 40rpx;
}

.content-item {
  margin-bottom: 32rpx;
  padding-bottom: 32rpx;
  border-bottom: 1rpx solid var(--color-border-light);
  
  &:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }
}

.item-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.item-number {
  width: 48rpx;
  height: 48rpx;
  background: $primary-gradient;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
  color: #fff;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.item-icon {
  width: 48rpx;
  height: 48rpx;
  background: linear-gradient(135deg, rgba(123, 196, 127, 0.12) 0%, rgba(123, 196, 127, 0.24) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.item-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.item-desc {
  display: block;
  font-size: 26rpx;
  color: var(--color-text-secondary);
  line-height: 1.8;
  padding-left: 64rpx;
}

// 内容底部
.content-footer {
  text-align: center;
  padding-top: 24rpx;
  border-top: 1rpx solid var(--color-border-light);
}

.update-time {
  font-size: 24rpx;
  color: #94a3b8;
}

// ============================================
// 弹窗底部按钮
// ============================================
.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 40rpx 40rpx;
  background: #fafafa;
  border-top: 1rpx solid var(--color-border-light);
}

.footer-btn {
  flex: 1;
  height: 80rpx;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
  
  &.cancel {
    background: var(--color-border-light);
    color: var(--color-text-secondary);
    
    &:active {
      background: var(--color-border);
    }
  }
  
  &.confirm {
    background: $primary-gradient;
    color: #fff;
    box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.35);
    
    &:active {
      transform: translateY(2rpx);
      box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.25);
    }
  }
}

// ============================================
// 响应式适配
// ============================================
@media (min-width: 768px) {
  .login-card {
    max-width: 480rpx;
    padding: 56rpx 48rpx;
  }
  
  .brand-name {
    font-size: 56rpx;
  }
  
  .modal-container {
    width: 600rpx;
  }
}

@media (max-width: 375px) {
  .content-wrapper {
    padding: 32rpx;
  }
  
  .login-card {
    padding: 40rpx 32rpx;
  }
  
  .brand-name {
    font-size: 44rpx;
  }
  
  .modal-container {
    width: 620rpx;
  }
}
</style>

