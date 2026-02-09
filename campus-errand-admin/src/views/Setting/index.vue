<template>
  <div class="setting">
    <a-card title="系统配置管理">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="basic" tab="基础配置">
          <a-form :model="basicForm" layout="vertical">
            <a-form-item label="系统名称">
              <a-input v-model:value="basicForm.siteName" placeholder="请输入系统名称" />
            </a-form-item>
            <a-form-item label="系统Logo">
              <a-input v-model:value="basicForm.siteLogo" placeholder="请输入系统Logo URL" />
            </a-form-item>
            <a-form-item label="客服电话">
              <a-input v-model:value="basicForm.servicePhone" placeholder="请输入客服电话" />
            </a-form-item>
            <a-form-item label="客服邮箱">
              <a-input v-model:value="basicForm.serviceEmail" placeholder="请输入客服邮箱" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="saveBasicConfig">保存配置</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="task" tab="任务配置">
          <a-form :model="taskForm" layout="vertical">
            <a-form-item label="最小赏金金额">
              <a-input-number v-model:value="taskForm.minReward" :min="1" :precision="2" style="width: 100%" />
            </a-form-item>
            <a-form-item label="最大赏金金额">
              <a-input-number v-model:value="taskForm.maxReward" :min="1" :precision="2" style="width: 100%" />
            </a-form-item>
            <a-form-item label="任务自动取消时间（小时）">
              <a-input-number v-model:value="taskForm.autoCancelHours" :min="1" style="width: 100%" />
            </a-form-item>
            <a-form-item label="任务超时提醒时间（小时）">
              <a-input-number v-model:value="taskForm.timeoutWarningHours" :min="1" style="width: 100%" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="saveTaskConfig">保存配置</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="wallet" tab="钱包配置">
          <a-form :model="walletForm" layout="vertical">
            <a-form-item label="最低提现金额">
              <a-input-number v-model:value="walletForm.minWithdraw" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
            <a-form-item label="最高提现金额">
              <a-input-number v-model:value="walletForm.maxWithdraw" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
            <a-form-item label="平台抽成比例（%）">
              <a-input-number v-model:value="walletForm.platformFee" :min="0" :max="100" :precision="2" style="width: 100%" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="saveWalletConfig">保存配置</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="other" tab="其他配置">
          <a-form :model="otherForm" layout="vertical">
            <a-form-item label="用户注册开关">
              <a-switch v-model:checked="otherForm.registerEnabled" />
            </a-form-item>
            <a-form-item label="任务发布开关">
              <a-switch v-model:checked="otherForm.taskPublishEnabled" />
            </a-form-item>
            <a-form-item label="实名认证开关">
              <a-switch v-model:checked="otherForm.verifyEnabled" />
            </a-form-item>
            <a-form-item label="系统维护开关">
              <a-switch v-model:checked="otherForm.maintenanceMode" />
            </a-form-item>
            <a-form-item label="维护提示信息" v-if="otherForm.maintenanceMode">
              <a-textarea v-model:value="otherForm.maintenanceMessage" :rows="3" placeholder="请输入维护提示信息" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="saveOtherConfig">保存配置</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getConfigList, updateConfig } from '@/api/config'

const activeKey = ref('basic')

// 基础配置表单
const basicForm = ref({
  siteName: '',
  siteLogo: '',
  servicePhone: '',
  serviceEmail: ''
})

// 任务配置表单
const taskForm = ref({
  minReward: 1,
  maxReward: 1000,
  autoCancelHours: 24,
  timeoutWarningHours: 2
})

// 钱包配置表单
const walletForm = ref({
  minWithdraw: 1,
  maxWithdraw: 5000,
  platformFee: 0
})

// 其他配置表单
const otherForm = ref({
  registerEnabled: true,
  taskPublishEnabled: true,
  verifyEnabled: false,
  maintenanceMode: false,
  maintenanceMessage: ''
})

// 加载配置
const loadConfigs = async () => {
  try {
    const res = await getConfigList()
    if (res.code === 200) {
      const configs = res.data || []
      
      // 将配置映射到表单
      configs.forEach((config: any) => {
        const key = config.configKey
        const value = config.configValue
        
        // 基础配置
        if (key === 'site.name') basicForm.value.siteName = value
        if (key === 'site.logo') basicForm.value.siteLogo = value
        if (key === 'service.phone') basicForm.value.servicePhone = value
        if (key === 'service.email') basicForm.value.serviceEmail = value
        
        // 任务配置
        if (key === 'task.min.reward') taskForm.value.minReward = parseFloat(value)
        if (key === 'task.max.reward') taskForm.value.maxReward = parseFloat(value)
        if (key === 'task.auto.cancel.hours') taskForm.value.autoCancelHours = parseInt(value)
        if (key === 'task.timeout.warning.hours') taskForm.value.timeoutWarningHours = parseInt(value)
        
        // 钱包配置
        if (key === 'wallet.min.withdraw') walletForm.value.minWithdraw = parseFloat(value)
        if (key === 'wallet.max.withdraw') walletForm.value.maxWithdraw = parseFloat(value)
        if (key === 'wallet.platform.fee') walletForm.value.platformFee = parseFloat(value)
        
        // 其他配置
        if (key === 'user.register.enabled') otherForm.value.registerEnabled = value === 'true'
        if (key === 'task.publish.enabled') otherForm.value.taskPublishEnabled = value === 'true'
        if (key === 'user.verify.enabled') otherForm.value.verifyEnabled = value === 'true'
        if (key === 'system.maintenance.mode') otherForm.value.maintenanceMode = value === 'true'
        if (key === 'system.maintenance.message') otherForm.value.maintenanceMessage = value
      })
    }
  } catch (error) {
    console.error('加载配置失败', error)
    message.error('加载配置失败')
  }
}

// 保存基础配置
const saveBasicConfig = async () => {
  try {
    await updateConfig('site.name', basicForm.value.siteName)
    await updateConfig('site.logo', basicForm.value.siteLogo)
    await updateConfig('service.phone', basicForm.value.servicePhone)
    await updateConfig('service.email', basicForm.value.serviceEmail)
    message.success('基础配置保存成功')
  } catch (error) {
    console.error('保存配置失败', error)
    message.error('保存配置失败')
  }
}

// 保存任务配置
const saveTaskConfig = async () => {
  try {
    await updateConfig('task.min.reward', taskForm.value.minReward.toString())
    await updateConfig('task.max.reward', taskForm.value.maxReward.toString())
    await updateConfig('task.auto.cancel.hours', taskForm.value.autoCancelHours.toString())
    await updateConfig('task.timeout.warning.hours', taskForm.value.timeoutWarningHours.toString())
    message.success('任务配置保存成功')
  } catch (error) {
    console.error('保存配置失败', error)
    message.error('保存配置失败')
  }
}

// 保存钱包配置
const saveWalletConfig = async () => {
  try {
    await updateConfig('wallet.min.withdraw', walletForm.value.minWithdraw.toString())
    await updateConfig('wallet.max.withdraw', walletForm.value.maxWithdraw.toString())
    await updateConfig('wallet.platform.fee', walletForm.value.platformFee.toString())
    message.success('钱包配置保存成功')
  } catch (error) {
    console.error('保存配置失败', error)
    message.error('保存配置失败')
  }
}

// 保存其他配置
const saveOtherConfig = async () => {
  try {
    await updateConfig('user.register.enabled', otherForm.value.registerEnabled.toString())
    await updateConfig('task.publish.enabled', otherForm.value.taskPublishEnabled.toString())
    await updateConfig('user.verify.enabled', otherForm.value.verifyEnabled.toString())
    await updateConfig('system.maintenance.mode', otherForm.value.maintenanceMode.toString())
    await updateConfig('system.maintenance.message', otherForm.value.maintenanceMessage)
    message.success('其他配置保存成功')
  } catch (error) {
    console.error('保存配置失败', error)
    message.error('保存配置失败')
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.setting {
  padding: 0;
}

:deep(.ant-form-item) {
  margin-bottom: 24px;
}

:deep(.ant-input-number) {
  width: 100%;
}
</style>
