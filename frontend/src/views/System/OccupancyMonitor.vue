<template>
  <div class="occupancy-monitor-page">
    <a-card :bordered="false" class="monitor-card">
      <template #title>
        <div class="card-title-wrapper">
          <monitor-outlined class="title-icon" />
          <span>占座监控</span>
        </div>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleRefresh" :loading="loading">
            <reload-outlined />
            刷新
          </a-button>
          <a-button @click="handleCheckNow" :loading="checking">
            <play-circle-outlined />
            立即检测
          </a-button>
        </a-space>
      </template>

      <!-- 统计卡片 -->
      <a-row :gutter="[16, 16]" class="stats-row">
        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="stat-card normal">
            <div class="stat-value">{{ stats.normal }}</div>
            <div class="stat-label">正常使用</div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="stat-card warning">
            <div class="stat-value">{{ stats.warning }}</div>
            <div class="stat-label">占座预警</div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="stat-card occupied">
            <div class="stat-value">{{ stats.occupied }}</div>
            <div class="stat-label">占座违规</div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="stat-card total">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">监控总数</div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 占座监控列表 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        class="monitor-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userInfo'">
            <div class="user-info">
              <span class="username">{{ record.realName || record.username }}</span>
              <span class="user-id">ID: {{ record.userId }}</span>
            </div>
          </template>

          <template v-if="column.key === 'seatInfo'">
            <a-tag color="blue">{{ record.area }}</a-tag>
            <span class="seat-no">{{ record.seatNo }}</span>
          </template>

          <template v-if="column.key === 'awayTime'">
            <div class="away-time">
              <span class="minutes" :class="getAwayTimeClass(record.totalAwayMinutes)">
                {{ record.totalAwayMinutes }} 分钟
              </span>
              <a-progress
                :percent="getAwayProgress(record.totalAwayMinutes)"
                :stroke-color="getProgressColor(record.totalAwayMinutes)"
                :show-info="false"
                size="small"
              />
            </div>
          </template>

          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.occupancyStatus)">
              {{ getStatusText(record.occupancyStatus) }}
            </a-tag>
            <span v-if="record.warningCount > 0" class="warning-count">
              (预警{{ record.warningCount }}次)
            </span>
          </template>

          <template v-if="column.key === 'checkInTime'">
            <span class="time-text">{{ formatTime(record.checkInTime) }}</span>
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a-button
                type="primary"
                size="small"
                danger
                @click="handleCheckout(record)"
                :disabled="record.occupancyStatus === 'occupied'"
              >
                强制签退
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 强制签退确认弹窗 -->
    <a-modal
      v-model:open="checkoutModalVisible"
      title="强制签退确认"
      @ok="confirmCheckout"
      @cancel="checkoutModalVisible = false"
    >
      <a-form :model="checkoutForm" layout="vertical">
        <a-form-item label="签退原因">
          <a-textarea
            v-model:value="checkoutForm.reason"
            placeholder="请输入强制签退原因"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  MonitorOutlined,
  ReloadOutlined,
  PlayCircleOutlined
} from '@ant-design/icons-vue'
import { getOccupancyMonitoring, manualCheckout, performOccupancyCheck, type OccupancyRecord } from '../../api/occupancy'

const loading = ref(false)
const checking = ref(false)
const dataSource = ref<OccupancyRecord[]>([])
const checkoutModalVisible = ref(false)
const currentRecord = ref<OccupancyRecord | null>(null)
const checkoutForm = ref({ reason: '' })

const columns = [
  {
    title: '用户信息',
    key: 'userInfo',
    width: 150
  },
  {
    title: '座位信息',
    key: 'seatInfo',
    width: 150
  },
  {
    title: '离开时长',
    key: 'awayTime',
    width: 200
  },
  {
    title: '状态',
    key: 'status',
    width: 150
  },
  {
    title: '签到时间',
    key: 'checkInTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right'
  }
]

const pagination = {
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条记录`
}

const stats = computed(() => {
  const normal = dataSource.value.filter(r => r.occupancyStatus === 'normal').length
  const warning = dataSource.value.filter(r => r.occupancyStatus === 'warning').length
  const occupied = dataSource.value.filter(r => r.occupancyStatus === 'occupied').length
  return {
    normal,
    warning,
    occupied,
    total: dataSource.value.length
  }
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOccupancyMonitoring()
    dataSource.value = (res as any).data || res || []
  } catch (e) {
    message.error('获取监控数据失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  fetchData()
}

const handleCheckNow = async () => {
  checking.value = true
  try {
    await performOccupancyCheck()
    message.success('占座检测已执行')
    await fetchData()
  } catch (e) {
    message.error('检测执行失败')
  } finally {
    checking.value = false
  }
}

const handleCheckout = (record: OccupancyRecord) => {
  currentRecord.value = record
  checkoutForm.value.reason = ''
  checkoutModalVisible.value = true
}

const confirmCheckout = async () => {
  if (!currentRecord.value) return
  if (!checkoutForm.value.reason.trim()) {
    message.warning('请输入签退原因')
    return
  }

  try {
    await manualCheckout(currentRecord.value.reservationId, checkoutForm.value.reason)
    message.success('强制签退成功')
    checkoutModalVisible.value = false
    await fetchData()
  } catch (e) {
    message.error('操作失败')
  }
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    normal: 'success',
    warning: 'warning',
    occupied: 'error'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    normal: '正常使用',
    warning: '占座预警',
    occupied: '占座违规'
  }
  return texts[status] || status
}

const getAwayTimeClass = (minutes: number) => {
  if (minutes >= 60) return 'danger'
  if (minutes >= 45) return 'warning'
  return 'normal'
}

const getAwayProgress = (minutes: number) => {
  const threshold = 60
  return Math.min((minutes / threshold) * 100, 100)
}

const getProgressColor = (minutes: number) => {
  if (minutes >= 60) return '#ff4d4f'
  if (minutes >= 45) return '#faad14'
  return '#52c41a'
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.occupancy-monitor-page {
  padding: 24px;
}

.monitor-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
}

.title-icon {
  color: #1890ff;
  font-size: 24px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
  border-radius: 12px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.normal {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border: 1px solid #b7eb8f;
}

.stat-card.warning {
  background: linear-gradient(135deg, #fffbe6 0%, #ffe58f 100%);
  border: 1px solid #ffd666;
}

.stat-card.occupied {
  background: linear-gradient(135deg, #fff2f0 0%, #ffccc7 100%);
  border: 1px solid #ff7875;
}

.stat-card.total {
  background: linear-gradient(135deg, #e6fffb 0%, #b5f5ec 100%);
  border: 1px solid #87e8de;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-card.normal .stat-value {
  color: #52c41a;
}

.stat-card.warning .stat-value {
  color: #faad14;
}

.stat-card.occupied .stat-value {
  color: #ff4d4f;
}

.stat-card.total .stat-value {
  color: #13c2c2;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.monitor-table {
  margin-top: 16px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 500;
  color: #1f2937;
}

.user-id {
  font-size: 12px;
  color: #9ca3af;
}

.seat-no {
  margin-left: 8px;
  font-weight: 500;
}

.away-time {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.away-time .minutes {
  font-weight: 500;
}

.away-time .minutes.normal {
  color: #52c41a;
}

.away-time .minutes.warning {
  color: #faad14;
}

.away-time .minutes.danger {
  color: #ff4d4f;
}

.warning-count {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.time-text {
  color: #666;
  font-size: 13px;
}

@media (max-width: 768px) {
  .occupancy-monitor-page {
    padding: 16px;
  }
}
</style>
