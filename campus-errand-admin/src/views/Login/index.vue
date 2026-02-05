<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">校园跑腿后台管理</h1>
      <a-form
        :model="formState"
        :rules="rules"
        @finish="handleLogin"
        class="login-form"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="formState.username"
            size="large"
            placeholder="用户名"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>
        
        <a-form-item name="password">
          <a-input-password
            v-model:value="formState.password"
            size="large"
            placeholder="密码"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>
        
        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            :loading="loading"
            block
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const formState = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await login(formState)
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      message.success('登录成功')
      router.push('/')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
  font-size: 24px;
}

.login-form {
  width: 100%;
}
</style>
