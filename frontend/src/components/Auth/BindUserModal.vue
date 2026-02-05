<template>
  <a-modal
    :open="open"
    title="完善个人信息"
    :maskClosable="false"
    :closable="false"
    :footer="null"
    width="500px"
  >
    <a-alert
      message="欢迎新用户！"
      description="为了提供更好的服务，请完善您的个人信息并验证手机号。"
      type="info"
      show-icon
      class="mb-4"
    />
    
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      layout="vertical"
      @finish="handleFinish"
    >
      <a-form-item label="学号" name="studentId">
        <a-input v-model:value="formState.studentId" placeholder="请输入学号" />
      </a-form-item>

      <a-form-item label="姓名" name="realName">
        <a-input v-model:value="formState.realName" placeholder="请输入真实姓名" />
      </a-form-item>

      <a-form-item label="别名 (昵称)" name="alias">
        <a-input v-model:value="formState.alias" placeholder="请输入昵称" />
      </a-form-item>

      <a-form-item label="手机号" name="phone">
        <a-input v-model:value="formState.phone" placeholder="请输入手机号" />
      </a-form-item>

      <a-form-item label="验证码" name="verifyCode">
        <div class="flex gap-2">
          <a-input v-model:value="formState.verifyCode" placeholder="请输入验证码" />
          <a-button 
            :disabled="countdown > 0" 
            @click="sendCode"
            :loading="sending"
          >
            {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
          </a-button>
        </div>
      </a-form-item>

      <a-form-item label="设置密码" name="password">
        <a-input-password v-model:value="formState.password" placeholder="请设置登录密码" />
      </a-form-item>

      <a-form-item label="确认密码" name="confirmPassword">
        <a-input-password v-model:value="formState.confirmPassword" placeholder="请再次输入密码" />
      </a-form-item>

      <a-form-item class="mt-6">
        <a-button type="primary" html-type="submit" block size="large" :loading="loading">
          提交绑定
        </a-button>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits(['success'])

const formRef = ref()
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
let timer: number | null = null

// 模拟验证码有效期（这里简单用前端变量模拟，实际应由后端校验）
let validCode = ''
let codeExpireTime = 0

const formState = reactive({
  studentId: '',
  realName: '',
  alias: '',
  phone: '',
  verifyCode: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (value === '') {
    return Promise.reject('请再次输入密码')
  } else if (value !== formState.password) {
    return Promise.reject('两次输入密码不一致')
  } else {
    return Promise.resolve()
  }
}

const rules: Record<string, Rule[]> = {
  studentId: [{ required: true, message: '请输入学号' }],
  realName: [{ required: true, message: '请输入真实姓名' }],
  phone: [
    { required: true, message: '请输入手机号' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }
  ],
  verifyCode: [{ required: true, message: '请输入验证码' }],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, message: '密码长度不能少于6位' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'change' }
  ]
}

const sendCode = async () => {
  try {
    await formRef.value.validateFields(['phone'])
    sending.value = true
    
    // 模拟发送请求
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 生成模拟验证码
    validCode = Math.floor(100000 + Math.random() * 900000).toString()
    codeExpireTime = Date.now() + 2 * 60 * 1000 // 2分钟有效期
    
    message.success(`验证码已发送: ${validCode} (测试用，有效期2分钟)`)
    console.log('Verify Code:', validCode)
    
    // 开始倒计时
    countdown.value = 120
    timer = window.setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer!)
        timer = null
      }
    }, 1000)
    
  } catch (error) {
    // 校验失败
  } finally {
    sending.value = false
  }
}

const handleFinish = async (values: any) => {
  // 校验验证码
  if (Date.now() > codeExpireTime) {
    message.error('验证码已过期，请重新获取')
    return
  }
  if (values.verifyCode !== validCode) {
    message.error('验证码错误')
    return
  }

  loading.value = true
  try {
    // 模拟提交绑定请求
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    message.success('绑定成功')
    emit('success', { ...values })
  } catch (error) {
    message.error('绑定失败，请重试')
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.mb-4 {
  margin-bottom: 1rem;
}
.mt-6 {
  margin-top: 1.5rem;
}
.flex {
  display: flex;
}
.gap-2 {
  gap: 0.5rem;
}
</style>
