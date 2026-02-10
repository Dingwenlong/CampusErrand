<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-wrapper">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor"/>
                <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span class="logo-text">CampusErrand</span>
          </div>
          <h2 class="brand-title">校园跑腿管理系统</h2>
          <p class="brand-desc">高效、便捷、智能的校园服务平台</p>
          <div class="feature-list">
            <div class="feature-item">
              <div class="feature-icon">📦</div>
              <span>任务管理</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">👥</div>
              <span>用户管理</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">📊</div>
              <span>数据统计</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-section">
        <div class="login-card">
          <div class="login-header">
            <h3 class="welcome-text">欢迎回来</h3>
            <p class="login-subtitle">请登录您的管理员账号</p>
          </div>

          <a-form
            :model="formState"
            :rules="rules"
            @finish="handleLogin"
            class="login-form"
          >
            <a-form-item name="username">
              <div class="input-label">用户名</div>
              <a-input
                v-model:value="formState.username"
                size="large"
                placeholder="请输入用户名"
                class="custom-input"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item name="password">
              <div class="input-label">
                <span>密码</span>
                <a class="forgot-link" @click="handleForgotPassword">忘记密码？</a>
              </div>
              <a-input-password
                v-model:value="formState.password"
                size="large"
                placeholder="请输入密码"
                class="custom-input"
                @pressEnter="handleLogin"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item class="remember-row">
              <a-checkbox v-model:checked="rememberMe">记住我</a-checkbox>
            </a-form-item>

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
                <ArrowRightOutlined class="btn-icon" />
              </a-button>
            </a-form-item>
          </a-form>

          <div class="login-footer">
            <p>默认账号: admin / 密码: admin123</p>
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -100px;
  animation: float 20s infinite ease-in-out;
}

.circle-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  left: -100px;
  animation: float 15s infinite ease-in-out reverse;
}

.circle-3 {
  width: 300px;
  height: 300px;
  top: 50%;
  left: 30%;
  background: rgba(255, 255, 255, 0.05);
  animation: float 25s infinite ease-in-out;
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

/* 登录包装器 */
.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #fff;
}

.brand-content {
  max-width: 360px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.logo-icon svg {
  width: 28px;
  height: 28px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.3;
}

.brand-desc {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 48px;
  line-height: 1.6;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}

.feature-icon {
  font-size: 24px;
}

.feature-item span {
  font-size: 15px;
  font-weight: 500;
}

/* 右侧登录区域 */
.login-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: #fff;
}

.login-card {
  width: 100%;
  max-width: 360px;
}

.login-header {
  margin-bottom: 32px;
}

.welcome-text {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 15px;
  color: #6b7280;
}

/* 表单样式 */
.input-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.forgot-link {
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  transition: color 0.3s;
}

.forgot-link:hover {
  color: #764ba2;
}

.custom-input {
  border-radius: 10px;
  height: 48px;
}

.custom-input :deep(.ant-input) {
  font-size: 15px;
}

.input-icon {
  color: #9ca3af;
  font-size: 18px;
}

.remember-row {
  margin-bottom: 16px;
}

.remember-row :deep(.ant-checkbox-wrapper) {
  color: #6b7280;
  font-size: 14px;
}

/* 登录按钮 */
.login-btn {
  height: 48px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 14px 0 rgba(102, 126, 234, 0.39);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px 0 rgba(102, 126, 234, 0.5);
}

.login-btn:active {
  transform: translateY(0);
}

.btn-text {
  letter-spacing: 2px;
}

.btn-icon {
  font-size: 16px;
  transition: transform 0.3s;
}

.login-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* 登录页脚 */
.login-footer {
  margin-top: 24px;
  text-align: center;
}

.login-footer p {
  font-size: 13px;
  color: #9ca3af;
  padding: 12px 16px;
  background: #f3f4f6;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    max-width: 400px;
  }

  .brand-section {
    padding: 40px 30px;
    min-height: 200px;
  }

  .brand-title {
    font-size: 24px;
  }

  .feature-list {
    display: none;
  }

  .login-section {
    padding: 40px 30px;
  }
}

/* 输入框聚焦效果 */
.custom-input :deep(.ant-input-affix-wrapper) {
  border-radius: 10px;
  border-color: #e5e7eb;
  transition: all 0.3s;
}

.custom-input :deep(.ant-input-affix-wrapper:hover) {
  border-color: #667eea;
}

.custom-input :deep(.ant-input-affix-wrapper-focused) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}
</style>
