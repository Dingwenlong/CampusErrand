<template>
  <div class="page-container">
    <a-card :bordered="false" class="profile-card">
      <template #title>
        <span class="card-title">个人信息配置</span>
      </template>
      
      <a-alert
        v-if="isNewUser"
        message="完善信息提醒"
        description="欢迎加入！请先完善您的真实姓名，完成后方可使用系统功能。"
        type="warning"
        show-icon
        class="mb-6"
      />

      <div class="profile-content">
        <!-- 左侧头像区域 -->
        <div class="avatar-section">
          <a-avatar :size="100" :src="userInfo?.avatar" class="profile-avatar">
            <template #icon><user-outlined /></template>
          </a-avatar>
          <a-button type="link" class="mt-4">更换头像</a-button>
        </div>

        <!-- 右侧表单区域 -->
        <div class="form-section">
          <a-form
            :model="formState"
            :rules="rules"
            layout="vertical"
            @finish="handleFinish"
            class="profile-form"
          >
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item label="学号/工号" name="studentId">
                  <a-input v-model:value="formState.studentId" placeholder="请输入学号" :disabled="!isNewUser" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="真实姓名" name="realName">
                  <a-input v-model:value="formState.realName" placeholder="请输入真实姓名" :disabled="!isNewUser" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item label="昵称" name="alias">
                  <a-input v-model:value="formState.alias" placeholder="请输入昵称" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="手机号" name="phone">
                  <a-input v-model:value="formState.phone" placeholder="请输入手机号" :disabled="!isNewUser" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item class="mt-4">
              <a-button type="primary" html-type="submit" :loading="loading" size="large">
                保存信息
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { UserOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '../../stores/user'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { useRouter } from 'vue-router'
import { updateProfile } from '../../api/auth'

const userStore = useUserStore()
const router = useRouter()
const isNewUser = computed(() => userStore.isNewUser)
const userInfo = computed(() => userStore.userInfo)

const loading = ref(false)

const formState = reactive({
  studentId: '',
  realName: '',
  alias: '',
  phone: ''
})

const rules = computed(() => {
  const baseRules: Record<string, Rule[]> = {
    alias: [{ required: true, message: '请输入昵称' }],
    realName: [{ required: true, message: '请输入真实姓名' }]
  }

  return baseRules
})

const handleFinish = async (values: any) => {
  loading.value = true
  try {
    // 准备更新数据
    const updatedInfo = {
      realName: values.realName,
      studentId: values.studentId,
      phone: userInfo.value?.phone || '', // 手机号已在注册时绑定，保持不变
      username: userInfo.value?.username // 保持登录账号不变
    }

    // 调用后端接口保存
    await updateProfile(updatedInfo)

    // 更新本地状态
    if (isNewUser.value) {
      userStore.completeBinding({ ...updatedInfo, username: userInfo.value?.username || '' })
      message.success('信息完善成功，欢迎使用！')
      // 绑定完成后跳转首页
      router.push('/')
    } else {
      userStore.completeBinding({ ...updatedInfo, username: userInfo.value?.username || '' })
      message.success('保存成功')
    }
  } catch (error: any) {
    message.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (userInfo.value) {
    // 回填信息 (如果是已有用户)
    formState.alias = userInfo.value.username || ''
    // 新用户学号/工号应为空，需要用户自行填写
    formState.studentId = userInfo.value.studentId || ''
    formState.realName = userInfo.value.realName || ''
    formState.phone = userInfo.value.phone || ''
  }
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.profile-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03), 0 1px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px 0 rgba(0, 0, 0, 0.02);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.profile-content {
  display: flex;
  gap: 48px;
  padding: 24px 0;
}

.avatar-section {
  flex: 0 0 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 24px;
  border-right: 1px solid #f1f5f9;
}

.profile-avatar {
  background-color: #f1f5f9;
  border: 4px solid #fff;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.form-section {
  flex: 1;
  max-width: 800px;
}

.profile-form {
  padding-right: 24px;
}

.mb-6 {
  margin-bottom: 24px;
}

.mt-4 {
  margin-top: 16px;
}

.flex {
  display: flex;
}

.gap-2 {
  gap: 8px;
}

@media screen and (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    gap: 24px;
  }
  
  .avatar-section {
    border-right: none;
    border-bottom: 1px solid #f1f5f9;
    padding-bottom: 24px;
    flex: auto;
  }
  
  .profile-form {
    padding-right: 0;
  }
}
</style>
