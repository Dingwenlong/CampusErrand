<template>
  <div class="setting">
    <a-card title="系统配置管理" class="setting-card">
      <a-tabs v-model:activeKey="activeKey" class="setting-tabs" @change="handleTabChange">
        <!-- 基础配置 -->
        <a-tab-pane key="basic" tab="基础配置">
          <a-spin :spinning="loading.basic">
            <a-form :model="basicForm" layout="vertical" class="setting-form" :rules="basicRules" ref="basicFormRef">
              <a-row :gutter="[16, 0]">
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="系统名称" name="siteName">
                    <a-input v-model:value="basicForm.siteName" placeholder="请输入系统名称" maxlength="50" show-count />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="系统Logo" name="siteLogo">
                    <a-input v-model:value="basicForm.siteLogo" placeholder="请输入系统Logo URL" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="客服电话" name="servicePhone">
                    <a-input v-model:value="basicForm.servicePhone" placeholder="请输入客服电话" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="客服邮箱" name="serviceEmail">
                    <a-input v-model:value="basicForm.serviceEmail" placeholder="请输入客服邮箱" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item>
                <a-space>
                  <a-button type="primary" @click="saveBasicConfig" :loading="saving.basic">
                    <SaveOutlined />
                    保存配置
                  </a-button>
                  <a-button @click="resetBasicForm">重置</a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-spin>
        </a-tab-pane>

        <!-- 任务配置 -->
        <a-tab-pane key="task" tab="任务配置">
          <a-spin :spinning="loading.task">
            <a-form :model="taskForm" layout="vertical" class="setting-form" :rules="taskRules" ref="taskFormRef">
              <a-row :gutter="[16, 0]">
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="最小赏金金额" name="minReward">
                    <a-input-number v-model:value="taskForm.minReward" :min="0.01" :max="taskForm.maxReward" :precision="2"
                      style="width: 100%" addon-before="¥" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="最大赏金金额" name="maxReward">
                    <a-input-number v-model:value="taskForm.maxReward" :min="taskForm.minReward" :max="10000"
                      :precision="2" style="width: 100%" addon-before="¥" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="任务自动取消时间" name="autoCancelHours">
                    <a-input-number v-model:value="taskForm.autoCancelHours" :min="1" :max="168" style="width: 100%"
                      addon-after="小时" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="任务超时提醒时间" name="timeoutWarningHours">
                    <a-input-number v-model:value="taskForm.timeoutWarningHours" :min="1" :max="24" style="width: 100%"
                      addon-after="小时" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item>
                <a-space>
                  <a-button type="primary" @click="saveTaskConfig" :loading="saving.task">
                    <SaveOutlined />
                    保存配置
                  </a-button>
                  <a-button @click="resetTaskForm">重置</a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-spin>
        </a-tab-pane>

        <!-- 钱包配置 -->
        <a-tab-pane key="wallet" tab="钱包配置">
          <a-spin :spinning="loading.wallet">
            <a-form :model="walletForm" layout="vertical" class="setting-form" :rules="walletRules" ref="walletFormRef">
              <a-row :gutter="[16, 0]">
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="最低提现金额" name="minWithdraw">
                    <a-input-number v-model:value="walletForm.minWithdraw" :min="0.01" :max="walletForm.maxWithdraw"
                      :precision="2" style="width: 100%" addon-before="¥" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="最高提现金额" name="maxWithdraw">
                    <a-input-number v-model:value="walletForm.maxWithdraw" :min="walletForm.minWithdraw" :max="100000"
                      :precision="2" style="width: 100%" addon-before="¥" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                  <a-form-item label="平台抽成比例" name="platformFee">
                    <a-input-number v-model:value="walletForm.platformFee" :min="0" :max="100" :precision="2"
                      style="width: 100%" addon-after="%" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item>
                <a-space>
                  <a-button type="primary" @click="saveWalletConfig" :loading="saving.wallet">
                    <SaveOutlined />
                    保存配置
                  </a-button>
                  <a-button @click="resetWalletForm">重置</a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-spin>
        </a-tab-pane>

        <!-- 其他配置 -->
        <a-tab-pane key="other" tab="其他配置">
          <a-spin :spinning="loading.other">
            <a-form :model="otherForm" layout="vertical" class="setting-form" ref="otherFormRef">
              <a-row :gutter="[16, 0]">
                <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <a-form-item label="用户注册开关">
                    <a-switch v-model:checked="otherForm.registerEnabled" checked-children="开启"
                      un-checked-children="关闭" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <a-form-item label="任务发布开关">
                    <a-switch v-model:checked="otherForm.taskPublishEnabled" checked-children="开启"
                      un-checked-children="关闭" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <a-form-item label="实名认证开关">
                    <a-switch v-model:checked="otherForm.verifyEnabled" checked-children="开启"
                      un-checked-children="关闭" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <a-form-item label="系统维护开关">
                    <a-switch v-model:checked="otherForm.maintenanceMode" checked-children="开启"
                      un-checked-children="关闭" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
                  <a-form-item label="用户二登录开关">
                    <a-switch v-model:checked="otherForm.user2LoginEnabled" checked-children="开启"
                      un-checked-children="关闭" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item label="维护提示信息" v-if="otherForm.maintenanceMode">
                <a-textarea v-model:value="otherForm.maintenanceMessage" :rows="3" placeholder="请输入维护提示信息"
                  maxlength="200" show-count />
              </a-form-item>
              <a-form-item>
                <a-space>
                  <a-button type="primary" @click="saveOtherConfig" :loading="saving.other">
                    <SaveOutlined />
                    保存配置
                  </a-button>
                  <a-button @click="resetOtherForm">重置</a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-spin>
        </a-tab-pane>

        <!-- 协议配置 -->
        <a-tab-pane key="agreement" tab="协议配置">
          <a-spin :spinning="loading.agreement">
            <a-form :model="agreementForm" layout="vertical" class="setting-form" ref="agreementFormRef">
              <a-row :gutter="[16, 0]">
                <a-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
                  <a-form-item label="用户协议更新时间">
                    <a-date-picker v-model:value="agreementForm.userAgreementUpdateTime" style="width: 100%" placeholder="选择更新时间" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
                  <a-form-item label="隐私政策更新时间">
                    <a-date-picker v-model:value="agreementForm.privacyPolicyUpdateTime" style="width: 100%" placeholder="选择更新时间" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item label="用户协议">
                <a-textarea v-model:value="agreementForm.userAgreement" :rows="10" placeholder="请输入用户协议内容，支持HTML格式"
                  maxlength="10000" show-count />
              </a-form-item>
              <a-form-item label="隐私政策">
                <a-textarea v-model:value="agreementForm.privacyPolicy" :rows="10" placeholder="请输入隐私政策内容，支持HTML格式"
                  maxlength="10000" show-count />
              </a-form-item>
              <a-form-item>
                <a-space>
                  <a-button type="primary" @click="saveAgreementConfig" :loading="saving.agreement">
                    <SaveOutlined />
                    保存配置
                  </a-button>
                  <a-button @click="resetAgreementForm">重置</a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-spin>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SaveOutlined } from '@ant-design/icons-vue'
import { getConfigByCategory, saveConfigs } from '@/api/config'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import dayjs from 'dayjs'

const activeKey = ref('basic')

// 加载状态 - 每个tab独立
const loading = reactive({
  basic: false,
  task: false,
  wallet: false,
  other: false,
  agreement: false
})

// 保存状态
const saving = reactive({
  basic: false,
  task: false,
  wallet: false,
  other: false,
  agreement: false
})

// 表单引用
const basicFormRef = ref<FormInstance>()
const taskFormRef = ref<FormInstance>()
const walletFormRef = ref<FormInstance>()
const otherFormRef = ref<FormInstance>()
const agreementFormRef = ref<FormInstance>()

// 基础配置表单
const basicForm = reactive({
  siteName: '',
  siteLogo: '',
  servicePhone: '',
  serviceEmail: ''
})

// 基础配置表单验证规则
const basicRules: Record<string, Rule[]> = {
  siteName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  servicePhone: [
    { pattern: /^1[3-9]\d{9}$|^\d{3,4}-\d{7,8}$/, message: '请输入正确的电话号码', trigger: 'blur' }
  ],
  serviceEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 任务配置表单
const taskForm = reactive({
  minReward: 1,
  maxReward: 1000,
  autoCancelHours: 24,
  timeoutWarningHours: 2
})

// 任务配置表单验证规则
const taskRules: Record<string, Rule[]> = {
  minReward: [
    { required: true, message: '请输入最小赏金金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '最小金额不能小于0.01', trigger: 'blur' }
  ],
  maxReward: [
    { required: true, message: '请输入最大赏金金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '最大金额不能小于0.01', trigger: 'blur' }
  ],
  autoCancelHours: [
    { required: true, message: '请输入自动取消时间', trigger: 'blur' },
    { type: 'number', min: 1, max: 168, message: '时间范围1-168小时', trigger: 'blur' }
  ],
  timeoutWarningHours: [
    { required: true, message: '请输入超时提醒时间', trigger: 'blur' },
    { type: 'number', min: 1, max: 24, message: '时间范围1-24小时', trigger: 'blur' }
  ]
}

// 钱包配置表单
const walletForm = reactive({
  minWithdraw: 1,
  maxWithdraw: 5000,
  platformFee: 0
})

// 钱包配置表单验证规则
const walletRules: Record<string, Rule[]> = {
  minWithdraw: [
    { required: true, message: '请输入最低提现金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额不能小于0.01', trigger: 'blur' }
  ],
  maxWithdraw: [
    { required: true, message: '请输入最高提现金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额不能小于0.01', trigger: 'blur' }
  ],
  platformFee: [
    { required: true, message: '请输入平台抽成比例', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '比例范围0-100%', trigger: 'blur' }
  ]
}

// 其他配置表单
const otherForm = reactive({
  registerEnabled: true,
  taskPublishEnabled: true,
  verifyEnabled: false,
  maintenanceMode: false,
  maintenanceMessage: '',
  user2LoginEnabled: false
})

// 协议配置表单
const agreementForm = reactive({
  userAgreement: '',
  privacyPolicy: '',
  userAgreementUpdateTime: null as any,
  privacyPolicyUpdateTime: null as any
})

// 原始数据备份，用于重置
const originalData = {
  basic: { ...basicForm },
  task: { ...taskForm },
  wallet: { ...walletForm },
  other: { ...otherForm },
  agreement: { ...agreementForm }
}

// 处理Tab切换
const handleTabChange = (key: string) => {
  // 切换到某个tab时加载对应数据
  switch (key) {
    case 'basic':
      loadBasicConfigs()
      break
    case 'task':
      loadTaskConfigs()
      break
    case 'wallet':
      loadWalletConfigs()
      break
    case 'other':
      loadOtherConfigs()
      break
    case 'agreement':
      loadAgreementConfigs()
      break
  }
}

// 加载基础配置
const loadBasicConfigs = async () => {
  loading.basic = true
  try {
    const res = await getConfigByCategory('basic')
    if (res.code === 200) {
      const configs = res.data || []
      configs.forEach((config: any) => {
        const key = config.configKey
        const value = config.configValue
        if (key === 'site_name') basicForm.siteName = value || ''
        if (key === 'site_logo') basicForm.siteLogo = value || ''
        if (key === 'service_phone') basicForm.servicePhone = value || ''
        if (key === 'service_email') basicForm.serviceEmail = value || ''
      })
      originalData.basic = { ...basicForm }
    }
  } catch (error) {
    console.error('加载基础配置失败', error)
    message.error('加载基础配置失败')
  } finally {
    loading.basic = false
  }
}

// 加载任务配置
const loadTaskConfigs = async () => {
  loading.task = true
  try {
    const res = await getConfigByCategory('task')
    if (res.code === 200) {
      const configs = res.data || []
      configs.forEach((config: any) => {
        const key = config.configKey
        const value = config.configValue
        if (key === 'min_reward') taskForm.minReward = parseFloat(value) || 1
        if (key === 'max_reward') taskForm.maxReward = parseFloat(value) || 1000
        if (key === 'auto_cancel_hours') taskForm.autoCancelHours = parseInt(value) || 24
        if (key === 'timeout_warning_hours') taskForm.timeoutWarningHours = parseInt(value) || 2
      })
      originalData.task = { ...taskForm }
    }
  } catch (error) {
    console.error('加载任务配置失败', error)
    message.error('加载任务配置失败')
  } finally {
    loading.task = false
  }
}

// 加载钱包配置
const loadWalletConfigs = async () => {
  loading.wallet = true
  try {
    const res = await getConfigByCategory('wallet')
    if (res.code === 200) {
      const configs = res.data || []
      configs.forEach((config: any) => {
        const key = config.configKey
        const value = config.configValue
        if (key === 'min_withdraw') walletForm.minWithdraw = parseFloat(value) || 1
        if (key === 'max_withdraw') walletForm.maxWithdraw = parseFloat(value) || 5000
        if (key === 'platform_fee') walletForm.platformFee = parseFloat(value) || 0
      })
      originalData.wallet = { ...walletForm }
    }
  } catch (error) {
    console.error('加载钱包配置失败', error)
    message.error('加载钱包配置失败')
  } finally {
    loading.wallet = false
  }
}

// 加载其他配置
const loadOtherConfigs = async () => {
  loading.other = true
  try {
    const res = await getConfigByCategory('other')
    if (res.code === 200) {
      const configs = res.data || []
      configs.forEach((config: any) => {
        const key = config.configKey
        const value = config.configValue
        if (key === 'register_enabled') otherForm.registerEnabled = value === 'true'
        if (key === 'task_publish_enabled') otherForm.taskPublishEnabled = value === 'true'
        if (key === 'verify_enabled') otherForm.verifyEnabled = value === 'true'
        if (key === 'maintenance_mode') otherForm.maintenanceMode = value === 'true'
        if (key === 'maintenance_message') otherForm.maintenanceMessage = value || ''
        if (key === 'user2_login_enabled') otherForm.user2LoginEnabled = value === 'true'
      })
      originalData.other = { ...otherForm }
    }
  } catch (error) {
    console.error('加载其他配置失败', error)
    message.error('加载其他配置失败')
  } finally {
    loading.other = false
  }
}

// 保存基础配置 - 使用POST请求
const saveBasicConfig = async () => {
  try {
    await basicFormRef.value?.validate()
    saving.basic = true

    const configMap: Record<string, string> = {
      site_name: basicForm.siteName,
      site_logo: basicForm.siteLogo,
      service_phone: basicForm.servicePhone,
      service_email: basicForm.serviceEmail
    }

    const res = await saveConfigs(configMap)
    if (res.code === 200) {
      message.success('基础配置保存成功')
      originalData.basic = { ...basicForm }
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败', error)
    message.error('保存配置失败')
  } finally {
    saving.basic = false
  }
}

// 重置基础表单
const resetBasicForm = () => {
  Object.assign(basicForm, originalData.basic)
  basicFormRef.value?.clearValidate()
  message.info('已重置为基础配置')
}

// 保存任务配置 - 使用POST请求
const saveTaskConfig = async () => {
  try {
    await taskFormRef.value?.validate()

    if (taskForm.minReward > taskForm.maxReward) {
      message.error('最小赏金金额不能大于最大赏金金额')
      return
    }

    saving.task = true

    const configMap: Record<string, string> = {
      min_reward: taskForm.minReward.toString(),
      max_reward: taskForm.maxReward.toString(),
      auto_cancel_hours: taskForm.autoCancelHours.toString(),
      timeout_warning_hours: taskForm.timeoutWarningHours.toString()
    }

    const res = await saveConfigs(configMap)
    if (res.code === 200) {
      message.success('任务配置保存成功')
      originalData.task = { ...taskForm }
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败', error)
    message.error('保存配置失败')
  } finally {
    saving.task = false
  }
}

// 重置任务表单
const resetTaskForm = () => {
  Object.assign(taskForm, originalData.task)
  taskFormRef.value?.clearValidate()
  message.info('已重置为任务配置')
}

// 保存钱包配置 - 使用POST请求
const saveWalletConfig = async () => {
  try {
    await walletFormRef.value?.validate()

    if (walletForm.minWithdraw > walletForm.maxWithdraw) {
      message.error('最低提现金额不能大于最高提现金额')
      return
    }

    saving.wallet = true

    const configMap: Record<string, string> = {
      min_withdraw: walletForm.minWithdraw.toString(),
      max_withdraw: walletForm.maxWithdraw.toString(),
      platform_fee: walletForm.platformFee.toString()
    }

    const res = await saveConfigs(configMap)
    if (res.code === 200) {
      message.success('钱包配置保存成功')
      originalData.wallet = { ...walletForm }
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败', error)
    message.error('保存配置失败')
  } finally {
    saving.wallet = false
  }
}

// 重置钱包表单
const resetWalletForm = () => {
  Object.assign(walletForm, originalData.wallet)
  walletFormRef.value?.clearValidate()
  message.info('已重置为钱包配置')
}

// 保存其他配置 - 使用POST请求
const saveOtherConfig = async () => {
  try {
    saving.other = true

    const configMap: Record<string, string> = {
      register_enabled: otherForm.registerEnabled.toString(),
      task_publish_enabled: otherForm.taskPublishEnabled.toString(),
      verify_enabled: otherForm.verifyEnabled.toString(),
      maintenance_mode: otherForm.maintenanceMode.toString(),
      maintenance_message: otherForm.maintenanceMessage,
      user2_login_enabled: otherForm.user2LoginEnabled.toString()
    }

    const res = await saveConfigs(configMap)
    if (res.code === 200) {
      message.success('其他配置保存成功')
      originalData.other = { ...otherForm }
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败', error)
    message.error('保存配置失败')
  } finally {
    saving.other = false
  }
}

// 重置其他表单
const resetOtherForm = () => {
  Object.assign(otherForm, originalData.other)
  message.info('已重置为其他配置')
}

// 加载协议配置
const loadAgreementConfigs = async () => {
  loading.agreement = true
  try {
    const res = await getConfigByCategory('agreement')
    if (res.code === 200) {
      const configs = res.data || []
      configs.forEach((config: any) => {
        const key = config.configKey
        const value = config.configValue
        if (key === 'user_agreement') agreementForm.userAgreement = value || ''
        if (key === 'privacy_policy') agreementForm.privacyPolicy = value || ''
        if (key === 'user_agreement_update_time' && value) {
          agreementForm.userAgreementUpdateTime = dayjs(value)
        }
        if (key === 'privacy_policy_update_time' && value) {
          agreementForm.privacyPolicyUpdateTime = dayjs(value)
        }
      })
      originalData.agreement = { ...agreementForm }
    }
  } catch (error) {
    console.error('加载协议配置失败', error)
    message.error('加载协议配置失败')
  } finally {
    loading.agreement = false
  }
}

// 保存协议配置
const saveAgreementConfig = async () => {
  try {
    saving.agreement = true

    const configMap: Record<string, string> = {
      user_agreement: agreementForm.userAgreement,
      privacy_policy: agreementForm.privacyPolicy,
      user_agreement_update_time: agreementForm.userAgreementUpdateTime ? agreementForm.userAgreementUpdateTime.format('YYYY-MM-DD') : '',
      privacy_policy_update_time: agreementForm.privacyPolicyUpdateTime ? agreementForm.privacyPolicyUpdateTime.format('YYYY-MM-DD') : ''
    }

    const res = await saveConfigs(configMap)
    if (res.code === 200) {
      message.success('协议配置保存成功')
      originalData.agreement = { ...agreementForm }
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败', error)
    message.error('保存配置失败')
  } finally {
    saving.agreement = false
  }
}

// 重置协议表单
const resetAgreementForm = () => {
  Object.assign(agreementForm, originalData.agreement)
  message.info('已重置为协议配置')
}

onMounted(() => {
  // 默认加载基础配置
  loadBasicConfigs()
})
</script>

<style scoped>
.setting {
  padding: 0;
}

.setting-card {
  border-radius: 12px;
}

.setting-card :deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  font-weight: 600;
}

.setting-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 24px;
}

.setting-form :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.setting-form :deep(.ant-input-number) {
  width: 100%;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .setting-card :deep(.ant-card-body) {
    padding: 16px;
  }

  .setting-tabs :deep(.ant-tabs-nav) {
    margin-bottom: 16px;
  }

  .setting-tabs :deep(.ant-tabs-tab) {
    padding: 8px 12px;
    font-size: 14px;
  }

  .setting-form :deep(.ant-form-item) {
    margin-bottom: 16px;
  }

  .setting-form :deep(.ant-form-item-label) {
    padding-bottom: 4px;
  }
}

@media (max-width: 576px) {
  .setting-card :deep(.ant-card-body) {
    padding: 12px;
  }

  .setting-tabs :deep(.ant-tabs-tab) {
    padding: 6px 10px;
    font-size: 13px;
  }
}
</style>
