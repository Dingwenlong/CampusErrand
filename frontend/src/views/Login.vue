<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧品牌/装饰区域 -->
      <div class="login-branding">
        <!-- 动态背景图形 -->
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>

        <div class="branding-content">
          <div class="brand-header">
            <div class="logo-container">
              <img src="/vite.svg" alt="logo" class="brand-logo" />
            </div>
            <h1 class="brand-title">图书馆座位预约与占座监督系统</h1>
          </div>
          <div class="brand-text">
            <p class="brand-slogan">静心阅读 · 智慧相伴 · 公平使用</p>
            <div class="divider"></div>
            <p class="brand-desc">Library Seat Reservation & Occupancy Supervision System</p>
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="login-form-section">
        <div class="form-header">
          <h2 class="welcome-text">
            {{ loginType === 'password' ? '欢迎回来' : (loginType === 'register' ? '欢迎加入' : '微信登录') }}
          </h2>
          <p class="sub-text">
            {{ loginType === 'password' ? '请登录您的账号以继续' : (loginType === 'register' ? '注册新账号开启智慧阅读之旅' : '请使用微信扫描二维码登录') }}
          </p>
        </div>

        <div v-if="loginType === 'password'">
          <a-form
            :model="formData"
            name="login-form"
            @finish="handleFinish"
            :rules="rules"
            layout="vertical"
            class="modern-form"
          >
            <a-form-item name="username" class="form-item">
              <template #label>
                <span class="input-label">账号</span>
              </template>
              <a-input 
                v-model:value="formData.username" 
                size="large" 
                placeholder="请输入用户名/学号" 
                class="modern-input"
              >
                <template #prefix>
                  <user-outlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item name="password" class="form-item">
              <template #label>
                <span class="input-label">密码</span>
              </template>
              <a-input-password 
                v-model:value="formData.password" 
                size="large" 
                placeholder="请输入密码"
                class="modern-input"
              >
                <template #prefix>
                  <lock-outlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <div class="form-actions">
              <a-checkbox class="remember-me">记住我</a-checkbox>
              <a class="forgot-password">忘记密码？</a>
            </div>

            <div class="test-accounts">
              <p>测试账号 (密码任意):</p>
              <div class="account-tags">
                <a-tag color="blue" @click="fillAccount('student')">student (学生)</a-tag>
                <a-tag color="cyan" @click="fillAccount('lib')">lib (图书管理员)</a-tag>
                <a-tag color="purple" @click="fillAccount('admin')">admin (系统管理员)</a-tag>
              </div>
            </div>

            <a-form-item class="submit-item">
              <a-button type="primary" html-type="submit" size="large" block :loading="loading" class="modern-btn">
                立即登录
              </a-button>
              <a-button 
                size="large" 
                block 
                class="wechat-login-btn"
                @click="switchLoginType('wechat')"
              >
                <template #icon><wechat-outlined /></template>
                微信扫码登录
              </a-button>
              
              <a-button 
                size="large" 
                block 
                style="margin-top: 16px;"
                @click="handleGuestLogin"
              >
                <template #icon><user-switch-outlined /></template>
                访客身份进入
              </a-button>

              <div class="register-link" style="text-align: center; margin-top: 16px;">
                <span style="color: #64748b;">还没有账号？</span>
                <a @click="switchLoginType('register')">立即注册</a>
              </div>
            </a-form-item>
          </a-form>
        </div>

        <div v-else-if="loginType === 'register'">
          <a-form
            :model="registerFormData"
            name="register-form"
            @finish="handleRegisterSubmit"
            :rules="registerRules"
            layout="vertical"
            class="modern-form register-form-compact"
          >
            <a-form-item name="username" class="form-item">
              <template #label>
                <span class="input-label">账号</span>
              </template>
              <a-input 
                v-model:value="registerFormData.username" 
                size="large" 
                placeholder="请输入用户名/学号" 
                class="modern-input"
              >
                <template #prefix>
                  <user-outlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item name="password" class="form-item">
              <template #label>
                <span class="input-label">密码</span>
              </template>
              <a-input-password 
                v-model:value="registerFormData.password" 
                size="large" 
                placeholder="请输入密码"
                class="modern-input"
              >
                <template #prefix>
                  <lock-outlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item name="confirmPassword" class="form-item">
              <template #label>
                <span class="input-label">确认密码</span>
              </template>
              <a-input-password 
                v-model:value="registerFormData.confirmPassword" 
                size="large" 
                placeholder="请再次输入密码"
                class="modern-input"
              >
                <template #prefix>
                  <lock-outlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item name="phone" class="form-item">
              <template #label>
                <span class="input-label">手机号</span>
              </template>
              <a-input 
                v-model:value="registerFormData.phone" 
                size="large" 
                placeholder="请输入手机号" 
                class="modern-input"
              >
                <template #prefix>
                  <mobile-outlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item name="verifyCode" class="form-item">
              <template #label>
                <span class="input-label">验证码</span>
              </template>
              <div class="flex gap-2">
                <a-input 
                  v-model:value="registerFormData.verifyCode" 
                  size="large" 
                  placeholder="请输入验证码"
                  class="modern-input"
                >
                  <template #prefix>
                    <mail-outlined class="input-icon" />
                  </template>
                </a-input>
                <a-button 
                  size="large" 
                  :disabled="countdown > 0" 
                  @click="sendRegisterCode"
                  :loading="sendingCode"
                  class="code-btn"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </a-button>
              </div>
            </a-form-item>

            <a-form-item class="submit-item">
              <a-button type="primary" html-type="submit" size="large" block :loading="loading" class="modern-btn">
                立即注册
              </a-button>
              <div class="register-link" style="text-align: center; margin-top: 16px;">
                <span style="color: #64748b;">已有账号？</span>
                <a @click="switchLoginType('password')">返回登录</a>
              </div>
            </a-form-item>
          </a-form>
        </div>

        <div v-else class="qr-login-container">
          <div class="qr-box" @click="handleQrClick">
            <qrcode-outlined v-if="qrCodeStatus === 'loading'" class="qr-placeholder" />
            <img v-else src="https://api.dicebear.com/9.x/icons/svg?seed=qr" class="qr-img" alt="QR Code" />
            
            <div class="qr-status" v-if="qrCodeStatus === 'scanned'">
              <check-circle-outlined style="font-size: 48px; color: #07c160; margin-bottom: 8px;" />
              <span>扫码成功</span>
            </div>
            <div class="qr-status" v-if="qrCodeStatus === 'loading'">
              <a-spin />
            </div>
          </div>
          <p class="qr-tip">请使用微信扫描二维码登录</p>
          <p class="qr-tip" style="font-size: 12px; margin-top: 4px; color: #3b82f6; cursor: pointer;" @click="handleQrClick">
            (点击二维码模拟扫码成功)
          </p>

          <a-button 
            type="link" 
            @click="switchLoginType('password')" 
            class="back-to-password"
          >
            返回账号密码登录
          </a-button>
        </div>
      </div>
    </div>
    
    <!-- 新用户绑定 Modal -->
    <!-- <bind-user-modal
      :open="showBindModal"
      @success="handleBindSuccess"
    /> -->
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onUnmounted } from 'vue'
import { UserOutlined, LockOutlined, WechatOutlined, QrcodeOutlined, CheckCircleOutlined, UserSwitchOutlined, MobileOutlined, MailOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const loginType = ref<'password' | 'wechat' | 'register'>('password')

// 微信登录相关
const qrCodeStatus = ref<'loading' | 'active' | 'scanned' | 'expired'>('active')
let qrTimer: number | null = null

const formData = reactive({
  username: '',
  password: ''
})

const registerFormData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  verifyCode: ''
})

const countdown = ref(0)
const sendingCode = ref(false)
let timer: number | null = null
let validCode = '' // 模拟验证码

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const validatePass2 = async (_rule: any, value: string) => {
  if (value === '') {
    return Promise.reject('请再次输入密码');
  } else if (value !== registerFormData.password) {
    return Promise.reject("两次输入密码不一致!");
  } else {
    return Promise.resolve();
  }
};

const rules = {
  username: [{ required: true, message: '请输入用户名！' }],
  password: [{ required: true, message: '请输入密码！' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名！' }],
  password: [{ required: true, message: '请输入密码！' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号！' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号！' }
  ],
  verifyCode: [{ required: true, message: '请输入验证码！' }]
}

const sendRegisterCode = async () => {
  if (!registerFormData.phone || !/^1[3-9]\d{9}$/.test(registerFormData.phone)) {
    message.warning('请先输入有效的手机号')
    return
  }
  
  sendingCode.value = true
  try {
    // 模拟接口延迟
    await new Promise(resolve => setTimeout(resolve, 800))
    
    validCode = Math.floor(100000 + Math.random() * 900000).toString()
    message.success(`验证码已发送: ${validCode} (模拟)`)
    
    countdown.value = 120
    timer = window.setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        if (timer) clearInterval(timer)
        timer = null
      }
    }, 1000)
  } finally {
    sendingCode.value = false
  }
}

// 切换登录方式
const switchLoginType = (type: 'password' | 'wechat' | 'register') => {
  loginType.value = type
  if (type === 'wechat') {
    qrCodeStatus.value = 'loading'
    setTimeout(() => {
      qrCodeStatus.value = 'active'
    }, 500)
  } else {
    if (qrTimer) clearTimeout(qrTimer)
  }
}

// 处理登录后的跳转逻辑
const handleLoginSuccess = () => {
  if (userStore.isNewUser) {
    message.info('检测到您是新用户，请前往个人中心完善信息')
    // 新用户跳转到个人信息页进行绑定
    router.push('/profile/info')
  } else {
    message.success('登录成功')
    router.push('/')
  }
}

const handleFinish = async (values: any) => {
  loading.value = true
  try {
    const success = await userStore.handleLogin(values)
    if (success) {
      handleLoginSuccess()
    } else {
      message.error('登录失败')
    }
  } catch (error) {
    message.error('登录出错')
  } finally {
    loading.value = false
  }
}

const handleRegisterSubmit = async (values: any) => {
  if (values.verifyCode !== validCode && values.verifyCode !== '123456') {
    message.error('验证码错误')
    return
  }

  loading.value = true
  try {
    const success = await userStore.handleRegister({
      username: values.username,
      password: values.password,
      phone: values.phone
    })
    if (success) {
      message.success('注册成功')
      handleLoginSuccess()
    } else {
      // 这里的错误信息通常由拦截器或 store 处理，如果没有处理则显示通用提示
    }
  } catch (error: any) {
    message.error(error.message || '注册出错')
  } finally {
    loading.value = false
  }
}

const handleGuestLogin = async () => {
  loading.value = true
  try {
    const success = await userStore.handleGuestLogin()
    if (success) {
      message.success('访客登录成功')
      router.push('/')
    } else {
      message.error('访客登录失败')
    }
  } catch (error) {
    message.error('登录出错')
  } finally {
    loading.value = false
  }
}

// 点击二维码触发模拟登录
const handleQrClick = () => {
  if (qrCodeStatus.value !== 'active') return
  
  qrCodeStatus.value = 'scanned'
  message.success('扫码成功，正在登录...')
  
  // 模拟后端验证延迟
  setTimeout(async () => {
    const success = await userStore.handleWeChatLogin('mock_code')
    if (success) {
      handleLoginSuccess()
    } else {
      message.error('微信登录失败')
      qrCodeStatus.value = 'active'
    }
  }, 1000)
}

const fillAccount = (role: string) => {
  formData.username = role
  formData.password = '123456'
}
</script>

<style scoped>
.qr-login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding-bottom: 40px;
}

.wechat-login-btn {
  margin-top: 16px;
  border-color: #07c160;
  color: #07c160;
}
.wechat-login-btn:hover {
  background-color: #f0fdf4;
  border-color: #059669;
  color: #059669;
}

.qr-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.qr-box {
  width: 240px;
  height: 240px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.qr-box:hover {
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  border-color: #38bdf8;
}

.qr-img {
  width: 100%;
  height: 100%;
  padding: 20px;
}

.qr-placeholder {
  font-size: 100px;
  color: #cbd5e1;
}

.qr-status {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #0f172a;
  font-weight: 500;
}

.qr-tip {
  color: #64748b;
  font-size: 14px;
}

.back-to-password {
  margin-top: 20px;
  color: #64748b;
}

.test-accounts {
  margin-bottom: 20px;
  font-size: 12px;
  color: #64748b;
}
.test-accounts p {
  margin-bottom: 8px;
}
.account-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.account-tags .ant-tag {
  cursor: pointer;
  margin-right: 0;
}
/* 整体容器：柔和的米白/浅灰背景 */
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 登录卡片包装器 */
.login-wrapper {
  display: flex;
  width: 1000px;
  max-width: 100%;
  min-height: 600px;
  max-height: 90vh;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 注册表单紧凑布局 */
.register-form-compact .form-item {
  margin-bottom: 16px;
}

.register-form-compact :deep(.ant-form-item-label) {
  padding-bottom: 4px;
}

.register-form-compact .input-label {
  font-size: 13px;
}

.register-form-compact :deep(.ant-input-affix-wrapper) {
  padding-top: 6px;
  padding-bottom: 6px;
}

.register-form-compact :deep(.ant-input) {
  font-size: 14px;
}

/* 左侧品牌区域 */
.login-branding {
  flex: 1;
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  padding: 40px;
}

/* 锯齿边缘装饰 - 仅在移动端启用 */
.login-branding::after {
  display: none;
}

/* 动态背景图形 */
.shape {
  position: absolute;
  filter: blur(45px);
  z-index: 0;
  animation: float 8s infinite ease-in-out;
  opacity: 0.85; /* 增加不透明度 */
  mix-blend-mode: multiply; /* 混合模式增强色彩叠加 */
}

.shape-1 {
  width: 320px;
  height: 320px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); /* 更深的蓝 */
  border-radius: 40% 60% 70% 30% / 40% 50% 60% 50%;
  top: -60px;
  left: -60px;
  animation-duration: 12s;
}

.shape-2 {
  width: 280px;
  height: 280px;
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%); /* 更深的青 */
  border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
  bottom: -50px;
  right: -50px;
  animation-delay: -3s;
  animation-duration: 10s;
}

.shape-3 {
  width: 140px;
  height: 140px;
  background: #8b5cf6; /* 更鲜艳的紫 */
  border-radius: 50%;
  top: 35%;
  right: 15%;
  opacity: 0.6;
  animation-delay: -5s;
  animation-duration: 8s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); border-radius: 40% 60% 70% 30% / 40% 50% 60% 50%; }
  33% { transform: translate(30px, -50px) rotate(10deg); border-radius: 70% 30% 50% 50% / 30% 30% 70% 70%; }
  66% { transform: translate(-20px, 20px) rotate(-5deg); border-radius: 100% 60% 60% 100% / 100% 100% 60% 60%; }
}

.branding-content {
  position: relative;
  z-index: 2;
  text-align: center;
  /* 玻璃卡片效果 */
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 40px 32px;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.05);
  width: 100%;
  max-width: 360px;
  transition: transform 0.3s ease;
}

.branding-content:hover {
  transform: translateY(-5px);
}

.brand-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.logo-container {
  width: 72px;
  height: 72px;
  background: #ffffff;
  border-radius: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
  box-shadow: 0 10px 25px rgba(56, 189, 248, 0.15);
}

.brand-logo {
  width: 40px;
  height: 40px;
  margin-bottom: 0;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.05));
}

.brand-title {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.5px;
  line-height: 1.3;
}

.brand-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.brand-slogan {
  font-size: 16px;
  color: #334155;
  font-weight: 500;
  letter-spacing: 1px;
  margin: 0 0 12px 0;
}

.divider {
  width: 32px;
  height: 3px;
  background: linear-gradient(90deg, #38bdf8, #818cf8);
  border-radius: 2px;
  margin-bottom: 12px;
}

.brand-desc {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin: 0;
  opacity: 0.8;
}

/* 右侧表单区域 */
.login-form-section {
  flex: 1;
  padding: 40px 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #ffffff;
  overflow-y: auto;
}

.form-header {
  margin-bottom: 24px;
}

.form-header:has(+ div .register-form-compact) {
  margin-bottom: 16px;
}

.welcome-text {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}

.sub-text {
  color: #94a3b8;
  font-size: 13px;
}

/* 表单样式定制 */
.modern-form {
  width: 100%;
}

.input-label {
  color: #334155;
  font-weight: 500;
  font-size: 14px;
}

.modern-input {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 10px 11px;
  transition: all 0.3s ease;
}

.modern-input:hover, .modern-input:focus {
  background: #ffffff;
  border-color: #38bdf8;
  box-shadow: 0 0 0 4px rgba(56, 189, 248, 0.1);
}

:deep(.ant-input-affix-wrapper) {
  padding-top: 10px;
  padding-bottom: 10px;
  border-radius: 12px;
  border-color: #e2e8f0;
  background-color: #f8fafc;
}

:deep(.ant-input-affix-wrapper:hover), :deep(.ant-input-affix-wrapper:focus), :deep(.ant-input-affix-wrapper-focused) {
  border-color: #38bdf8;
  box-shadow: 0 0 0 4px rgba(56, 189, 248, 0.1);
  background-color: #ffffff;
}

:deep(.ant-input) {
  background-color: transparent !important;
}

.input-icon {
  color: #94a3b8;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.remember-me {
  color: #64748b;
}

.forgot-password {
  color: #38bdf8;
  font-weight: 500;
}

.forgot-password:hover {
  color: #0ea5e9;
  text-decoration: underline;
}

.modern-btn {
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #38bdf8 0%, #0ea5e9 100%);
  border: none;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
  box-shadow: 0 8px 16px rgba(14, 165, 233, 0.2);
  transition: all 0.3s ease;
}

.modern-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 25px rgba(14, 165, 233, 0.3);
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
}

.modern-btn:active {
  transform: translateY(0);
}

/* 响应式设计 */
@media screen and (max-width: 992px) {
  .login-wrapper {
    width: 700px;
    height: auto;
    flex-direction: column;
  }
  
  .login-branding {
    padding: 30px;
    flex: 0 0 auto;
    min-height: 200px;
  }
  
  /* 移动端锯齿背景色需匹配表单背景 */
  .login-branding::after {
    display: block;
    content: "";
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 12px;
    background: linear-gradient(-45deg, #ffffff 10px, transparent 0), 
                linear-gradient(45deg, #ffffff 10px, transparent 0);
    background-size: 20px 20px;
    background-position: left bottom;
    background-repeat: repeat-x;
    z-index: 10;
  }
  
  .shape {
    opacity: 0.5; /* 移动端适当降低透明度，避免干扰 */
  }
  
  .branding-content {
    background: transparent;
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
    padding: 0;
    border: none;
    box-shadow: none;
    max-width: 100%;
  }
  
  .branding-content:hover {
    transform: none;
  }

  .brand-title {
    font-size: 24px;
  }
  
  .brand-desc {
    display: none;
  }

  .login-form-section {
    padding: 40px 50px;
  }
}

@media screen and (max-width: 576px) {
  .login-container {
    padding: 0;
    background: #ffffff;
    display: block; /* 禁用 Flex 布局，防止高度不足时无法滚动 */
    height: auto;
    min-height: 100vh;
  }
  
  .login-wrapper {
    width: 100%;
    min-height: 100vh;
    height: auto;
    overflow: visible;
    border-radius: 0;
    box-shadow: none;
  }

  .login-branding {
    flex: 0 0 auto;
    padding: 40px 20px;
    border-bottom-right-radius: 0; /* 移除圆角，由锯齿接管 */
    background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  }

  /* 锯齿在移动端全屏模式下的适配 */
  .login-branding::after {
     bottom: -1px; /* 修复可能出现的微小缝隙 */
  }
  
  .logo-container {
    width: 64px;
    height: 64px;
    margin-bottom: 12px;
    border-radius: 16px;
  }
  
  .brand-logo {
    width: 32px;
    height: 32px;
  }

  .brand-title {
    font-size: 20px;
  }
  
  .brand-slogan {
    font-size: 14px;
    margin-bottom: 0;
    letter-spacing: 2px;
  }
  
  .divider {
    display: none;
  }

  .login-form-section {
    flex: 1;
    padding: 30px 24px;
    padding-bottom: 60px; /* 增加底部间距 */
    justify-content: flex-start;
    padding-top: 40px;
  }
  
  .welcome-text {
    font-size: 24px;
  }
}
</style>
