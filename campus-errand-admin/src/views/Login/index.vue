<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="yellow-dot dot-1"></div>
      <div class="yellow-dot dot-2"></div>
      <div class="yellow-dot dot-3"></div>
    </div>

    <div class="login-wrapper">
      <!-- 左侧品牌区域 - 深色背景 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo">
            <div class="logo-icon">
              <span class="logo-emoji">🏃</span>
            </div>
            <div class="logo-text-wrapper">
              <span class="logo-text">校园跑腿</span>
              <span class="logo-tag">ADMIN</span>
            </div>
          </div>
          <h2 class="brand-title">
            <span class="title-line"></span>
            后台管理系统
          </h2>
          <p class="brand-desc">高效、便捷、智能的校园服务平台</p>
          <div class="feature-list">
            <div class="feature-item">
              <div class="feature-icon-wrapper">
                <span class="feature-icon">📦</span>
              </div>
              <div class="feature-info">
                <span class="feature-title">任务管理</span>
                <span class="feature-desc">轻松管理所有跑腿任务</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon-wrapper">
                <span class="feature-icon">👥</span>
              </div>
              <div class="feature-info">
                <span class="feature-title">用户管理</span>
                <span class="feature-desc">全面掌控用户信息</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon-wrapper">
                <span class="feature-icon">📊</span>
              </div>
              <div class="feature-info">
                <span class="feature-title">数据统计</span>
                <span class="feature-desc">实时查看运营数据</span>
              </div>
            </div>
          </div>
        </div>
        <!-- 装饰元素 -->
        <div class="brand-decoration">
          <div class="deco-circle"></div>
          <div class="deco-line"></div>
        </div>
      </div>

      <!-- 右侧登录表单 - 白色背景 -->
      <div class="login-section">
        <div class="login-card">
          <div class="login-header">
            <div class="header-tag">管理员登录</div>
            <h3 class="welcome-text">
              <span class="text-highlight">欢迎</span>回来
            </h3>
            <p class="login-subtitle">请登录您的管理员账号</p>
          </div>

          <a-form
            :model="formState"
            :rules="rules"
            @finish="handleLogin"
            class="login-form"
          >
            <a-form-item name="username">
              <div class="input-wrapper">
                <div class="input-label">
                  <UserOutlined class="label-icon" />
                  <span>用户名</span>
                </div>
                <a-input
                  v-model:value="formState.username"
                  size="large"
                  placeholder="请输入用户名"
                  class="custom-input"
                />
              </div>
            </a-form-item>

            <a-form-item name="password">
              <div class="input-wrapper">
                <div class="input-label">
                  <LockOutlined class="label-icon" />
                  <span>密码</span>
                </div>
                <a-input-password
                  v-model:value="formState.password"
                  size="large"
                  placeholder="请输入密码"
                  class="custom-input"
                  @pressEnter="handleLogin"
                />
              </div>
            </a-form-item>

            <div class="form-options">
              <a-checkbox v-model:checked="rememberMe" class="remember-checkbox">
                <span class="checkbox-text">记住我</span>
              </a-checkbox>
              <a class="forgot-link" @click="handleForgotPassword">
                忘记密码？
                <ArrowRightOutlined class="link-arrow" />
              </a>
            </div>

            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                size="large"
                :loading="loading"
                block
                class="login-btn"
              >
                <span class="btn-text">登 录</span>
                <div class="btn-arrow">
                  <ArrowRightOutlined />
                </div>
              </a-button>
            </a-form-item>

          </a-form>

          <div class="login-footer">
            <div class="footer-line"></div>
            <p>请使用已分配的管理员账号登录</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, ArrowRightOutlined } from '@ant-design/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const rememberMe = ref(false)

const formState = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 页面加载时检查是否有记住的账号
onMounted(() => {
  const savedUsername = localStorage.getItem('admin_remember_username')
  if (savedUsername) {
    formState.username = savedUsername
    rememberMe.value = true
  }
})

const handleLogin = async () => {
  if (!formState.username || !formState.password) {
    return
  }

  loading.value = true
  try {
    const res = await login(formState)
    if (res.code === 200) {
      userStore.setToken(res.data.token)

      // 记住账号
      if (rememberMe.value) {
        localStorage.setItem('admin_remember_username', formState.username)
      } else {
        localStorage.removeItem('admin_remember_username')
      }

      message.success('登录成功，欢迎回来！')
      router.push('/')
    } else {
      message.error(res.message || '登录失败')
    }
  } catch (error: any) {
    message.error(error?.message || '登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const handleForgotPassword = () => {
  message.info('请联系系统管理员重置密码')
}

</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0f0f1e;
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  filter: blur(15px);
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 195, 0, 0.1) 0%, rgba(255, 195, 0, 0.05) 100%);
}

.shape-1 {
  width: 800px;
  height: 800px;
  top: -400px;
  right: -200px;
  animation: float 20s infinite ease-in-out;
}

.shape-2 {
  width: 600px;
  height: 600px;
  bottom: -300px;
  left: -200px;
  animation: float 15s infinite ease-in-out reverse;
}

.shape-3 {
  width: 400px;
  height: 400px;
  top: 50%;
  left: 20%;
  background: linear-gradient(135deg, rgba(255, 195, 0, 0.08) 0%, transparent 100%);
  animation: float 25s infinite ease-in-out;
}

.yellow-dot {
  position: absolute;
  border-radius: 50%;
  background: #FFC300;
}

.dot-1 {
  width: 20px;
  height: 20px;
  top: 20%;
  right: 30%;
  animation: pulse 2s infinite;
}

.dot-2 {
  width: 12px;
  height: 12px;
  top: 60%;
  right: 20%;
  animation: pulse 2s infinite 0.5s;
}

.dot-3 {
  width: 8px;
  height: 8px;
  bottom: 30%;
  right: 40%;
  animation: pulse 2s infinite 1s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.05);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.95);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

/* 登录包装器 */
.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1100px;
  min-height: 650px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 24px;
  overflow: hidden;
  position: relative;
  z-index: 1;
  box-shadow: 0 25px 80px -20px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 左侧品牌区域 - 深色撞色 */
.brand-section {
  flex: 1;
  background: linear-gradient(145deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.brand-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 300px;
  height: 300px;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  top: -100px;
  right: -100px;
  width: 300px;
  height: 300px;
  border: 40px solid rgba(255, 195, 0, 0.1);
  border-radius: 50%;
}

.deco-line {
  position: absolute;
  top: 50%;
  right: 0;
  width: 100px;
  height: 4px;
  background: linear-gradient(90deg, transparent, #FFC300);
}

.brand-content {
  max-width: 380px;
  position: relative;
  z-index: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 48px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #FFC300 0%, #FFB300 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(255, 195, 0, 0.3);
}

.logo-emoji {
  font-size: 32px;
}

.logo-text-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.logo-text {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: #fff;
}

.logo-tag {
  font-size: 12px;
  font-weight: 600;
  color: #FFC300;
  letter-spacing: 2px;
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.3;
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-line {
  width: 4px;
  height: 36px;
  background: linear-gradient(180deg, #FFC300 0%, #FFB300 100%);
  border-radius: 2px;
}

.brand-desc {
  font-size: 16px;
  opacity: 0.7;
  margin-bottom: 48px;
  line-height: 1.6;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 195, 0, 0.3);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 195, 0, 0.1);
  border-color: rgba(255, 195, 0, 0.4);
  transform: translateX(8px);
}

.feature-icon-wrapper {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #FFC300 0%, #FFB300 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-icon {
  font-size: 24px;
}

.feature-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.feature-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

/* 右侧登录区域 - 白色 */
.login-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: rgba(255, 255, 255, 0.6);
  position: relative;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.login-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(255, 195, 0, 0.3), transparent);
}

.login-card {
  width: 100%;
  max-width: 380px;
}

.login-header {
  margin-bottom: 40px;
}

.header-tag {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(255, 195, 0, 0.1);
  color: #FFC300;
  font-size: 12px;
  font-weight: 600;
  border-radius: 20px;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.welcome-text {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.text-highlight {
  color: #FFC300;
  position: relative;
}

.text-highlight::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 0;
  right: 0;
  height: 8px;
  background: rgba(255, 195, 0, 0.2);
  z-index: -1;
}

.login-subtitle {
  font-size: 15px;
  color: #6b7280;
}

/* 表单样式 */
.input-wrapper {
  margin-bottom: 8px;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.label-icon {
  color: #FFC300;
  font-size: 16px;
}

.custom-input {
  border-radius: 12px;
  height: 52px;
}

.custom-input :deep(.ant-input) {
  font-size: 15px;
}

.custom-input :deep(.ant-input-affix-wrapper) {
  border-radius: 12px;
  border-color: #e5e7eb;
  transition: all 0.3s;
  padding: 0 16px;
}

.custom-input :deep(.ant-input-affix-wrapper:hover) {
  border-color: #FFC300;
}

.custom-input :deep(.ant-input-affix-wrapper-focused) {
  border-color: #FFC300;
  box-shadow: 0 0 0 3px rgba(255, 195, 0, 0.15);
}

/* 表单选项 */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  margin-top: 8px;
}

.remember-checkbox :deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #FFC300;
  border-color: #FFC300;
}

.remember-checkbox :deep(.ant-checkbox-inner) {
  border-radius: 4px;
}

.checkbox-text {
  color: #6b7280;
  font-size: 14px;
}

.forgot-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #1a1a2e;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.forgot-link:hover {
  color: #FFC300;
}

.link-arrow {
  font-size: 12px;
  transition: transform 0.3s;
}

.forgot-link:hover .link-arrow {
  transform: translateX(4px);
}

/* 登录按钮 */
.login-btn {
  height: 52px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #FFC300 0%, #FFB300 100%);
  border: none;
  color: #1a1a2e;
  box-shadow: 0 4px 20px rgba(255, 195, 0, 0.4);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(255, 195, 0, 0.5);
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:active {
  transform: translateY(0);
}

.btn-text {
  letter-spacing: 2px;
}

.btn-arrow {
  width: 28px;
  height: 28px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.3s;
}

.login-btn:hover .btn-arrow {
  background: rgba(0, 0, 0, 0.15);
  transform: translateX(4px);
}

/* 登录页脚 */
.login-footer {
  margin-top: 32px;
  text-align: center;
}

.footer-line {
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, #FFC300, #FFB300);
  border-radius: 2px;
  margin: 0 auto 16px;
}

.login-footer p {
  font-size: 13px;
  color: #9ca3af;
}

.highlight-text {
  color: #FFC300;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 900px) {
  .login-wrapper {
    flex-direction: column;
    max-width: 450px;
  }

  .brand-section {
    padding: 40px 30px;
    min-height: 280px;
  }

  .brand-title {
    font-size: 28px;
  }

  .feature-list {
    display: none;
  }

  .login-section {
    padding: 40px 30px;
  }

  .login-section::before {
    display: none;
  }
}
</style>
