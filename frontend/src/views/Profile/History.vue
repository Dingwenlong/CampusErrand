<template>
  <div class="page-container">
    <a-card :bordered="false" title="我的预约/签到记录">
      <a-timeline mode="left" class="mt-4">
        <a-timeline-item
          v-for="record in history"
          :key="record.id"
          :color="getStatusColor(record.status)"
        >
          <template #dot>
            <component :is="getStatusIcon(record.status)" style="font-size: 16px" />
          </template>
          
          <div class="record-item">
            <div class="record-header">
              <span class="seat-no">{{ record.seatNo }} 座位</span>
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
              <div class="action-buttons">
                <a-button 
                  type="link" 
                  size="small" 
                  danger 
                  v-if="record.status === 'violation'"
                  @click="handleAppeal(record)"
                >
                  申诉
                </a-button>
                <a-button 
                  type="link" 
                  size="small" 
                  danger 
                  v-if="record.status === 'reserved'"
                  @click="handleCancel(record)"
                >
                  取消
                </a-button>
              </div>
            </div>
            <div class="record-time">
              {{ formatTimeRange(record) }}
            </div>
            <div class="record-type">
              时段：{{ getSlotText(record.slot) }} | 类型：{{ record.type === 'appointment' ? '预约' : '现场签到' }}
            </div>
          </div>
        </a-timeline-item>
      </a-timeline>

      <a-empty v-if="history.length === 0" description="暂无记录" />
    </a-card>

    <!-- 申诉弹窗 -->
    <a-modal
      v-model:open="appealVisible"
      title="违规申诉"
      @ok="submitAppeal"
      :confirmLoading="submitting"
    >
      <a-form layout="vertical">
        <a-form-item label="违规记录">
          <p>{{ currentRecord?.startTime }} - {{ currentRecord?.seatNo }}</p>
        </a-form-item>
        <a-form-item label="申诉理由" required>
          <a-textarea v-model:value="appealReason" :rows="4" placeholder="请详细描述申诉理由..." />
        </a-form-item>
        <a-form-item label="上传凭证">
          <a-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :before-upload="() => false"
          >
            <div v-if="fileList.length < 3">
              <plus-outlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { 
  ClockCircleOutlined, 
  CheckCircleOutlined, 
  ExclamationCircleOutlined,
  PlusOutlined,
  PlayCircleOutlined,
  StopOutlined
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { getMyHistory, submitAppeal as submitAppealApi, releaseSeat, type ReservationRecord } from '../../api/reservation'
import dayjs from 'dayjs'

const history = ref<ReservationRecord[]>([])
const appealVisible = ref(false)
const submitting = ref(false)
const currentRecord = ref<ReservationRecord | null>(null)
const appealReason = ref('')
const fileList = ref([])

const fetchHistory = async () => {
  try {
    const res = await getMyHistory()
    history.value = Array.isArray(res) ? res : (res as any).data || []
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchHistory()
})

const handleAppeal = (record: ReservationRecord) => {
  currentRecord.value = record
  appealReason.value = ''
  fileList.value = []
  appealVisible.value = true
}

const handleCancel = (record: ReservationRecord) => {
  Modal.confirm({
    title: '确认取消预约',
    content: `确定要取消 ${getSlotText(record.slot)} 的座位预约吗？`,
    onOk: async () => {
      try {
        await releaseSeat(record.id)
        message.success('预约已取消')
        fetchHistory()
      } catch (e) {}
    }
  })
}

const formatTimeRange = (record: ReservationRecord) => {
  if (!record.startTime) return ''
  const start = dayjs(record.startTime).format('YYYY-MM-DD HH:mm')
  const end = dayjs(record.endTime).format('HH:mm')
  return `${start} ~ ${end}`
}

const getSlotText = (slot?: string) => {
  switch (slot) {
    case 'morning': return '上午'
    case 'afternoon': return '下午'
    case 'evening': return '晚间'
    default: return '普通'
  }
}

const submitAppeal = async () => {
  if (!appealReason.value) {
    message.warning('请填写申诉理由')
    return
  }
  
  if (!currentRecord.value) return

  submitting.value = true
  try {
    await submitAppealApi(currentRecord.value.id, {
      reason: appealReason.value
    })
    message.success('申诉已提交，请等待管理员审核')
    appealVisible.value = false
  } catch (error) {
    message.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'reserved': return 'blue'
    case 'checked_in': return 'green'
    case 'completed': return 'gray'
    case 'violation': return 'red'
    case 'cancelled': return 'gray'
    case 'away': return 'orange'
    default: return 'blue'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'reserved': return '已预约'
    case 'checked_in': return '使用中'
    case 'completed': return '已完成'
    case 'violation': return '违规'
    case 'cancelled': return '已取消'
    case 'away': return '暂离'
    default: return status
  }
}

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'reserved': return ClockCircleOutlined
    case 'checked_in': return PlayCircleOutlined
    case 'completed': return CheckCircleOutlined
    case 'violation': return ExclamationCircleOutlined
    case 'cancelled': return StopOutlined
    case 'away': return ClockCircleOutlined
    default: return ClockCircleOutlined
  }
}
</script>

<style scoped>
.page-container {
  padding: 24px;
}
.mt-4 {
  margin-top: 24px;
}
.record-item {
  margin-bottom: 20px;
}
.record-header {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 4px;
}
.seat-no {
  margin-right: 8px;
}
.record-time {
  color: #666;
  font-size: 13px;
}
.record-type {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>
