<template>
  <div class="config-container">
    <a-card title="系统配置" :bordered="false">
      <a-table 
        :columns="columns" 
        :data-source="configs" 
        :loading="loading"
        row-key="id"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'configValue'">
            <a-input-number
              v-if="['violation_time', 'min_credit_score', 'release_buffer_time', 'checkin_before_window', 'checkin_after_window', 'late_reservation_grace_period', 'occupancy_check_interval', 'occupancy_threshold', 'occupancy_warning_time', 'max_away_time', 'closing_reminder_minutes', 'occupancy_credit_deduct'].includes(record.configKey)"
              :value="Number(record.configValue)"
              @update:value="(val: number) => record.configValue = String(val)"
              style="width: 100%"
              :min="0"
            />
            <a-switch 
              v-else-if="record.configKey === 'message_square_enabled'"
              :checked="record.configValue === 'true'"
              @update:checked="(val: boolean) => record.configValue = String(val)"
            />
            <div v-else-if="record.configKey === 'library_latitude' || record.configKey === 'library_longitude'" class="coord-input">
              <a-input-number 
                :value="Number(record.configValue)"
                @update:value="(val: number) => record.configValue = String(val)"
                style="flex: 1"
                :step="0.000001"
                :precision="6"
              />
              <a-button type="primary" size="small" @click="getCurrentCoord(record)">获取当前</a-button>
            </div>
            <a-input v-else v-model:value="record.configValue" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" @click="handleUpdate(record)">保存</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getConfigs, updateConfig, type SysConfig } from '../../api/config'

const loading = ref(false)
const configs = ref<SysConfig[]>([])

const columns = [
  { title: '配置名称', dataIndex: 'configName', key: 'configName' },
  { title: '配置项', dataIndex: 'configKey', key: 'configKey' },
  { title: '配置值', dataIndex: 'configValue', key: 'configValue' },
  { title: '操作', key: 'action' }
]

const fetchConfigs = async () => {
  loading.value = true
  try {
    const res = await getConfigs()
    configs.value = (res as any).data || res
  } catch (error) {
    message.error('获取配置失败')
  } finally {
    loading.value = false
  }
}

const handleUpdate = async (record: SysConfig) => {
  try {
    // Ensure value is string for backend
    const data = { ...record, configValue: String(record.configValue) }
    await updateConfig(data)
    message.success('配置更新成功')
    fetchConfigs()
  } catch (error) {
    message.error('更新失败')
  }
}

const getCurrentCoord = (record: SysConfig) => {
  if (!navigator.geolocation) {
    message.error('浏览器不支持地理定位')
    return
  }
  
  navigator.geolocation.getCurrentPosition(
    (position) => {
      if (record.configKey === 'library_latitude') {
        record.configValue = String(position.coords.latitude)
      } else {
        record.configValue = String(position.coords.longitude)
      }
      message.success('已获取当前坐标，请点击保存')
    },
    (err) => {
      message.error('定位失败: ' + err.message)
    }
  )
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.config-container {
  padding: 24px;
}
.coord-input {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
