<template>
  <div class="appeal-page">
    <a-card :bordered="false" class="appeal-card">
      <template #title>
        <div class="card-title-wrapper">
          <file-protect-outlined class="title-icon" />
          <span>违规申诉</span>
        </div>
      </template>

      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="submit" tab="提交申诉">
          <div class="submit-section">
            <a-alert
              v-if="!hasViolationRecord"
              message="暂无违规记录"
              description="您当前没有违规的预约记录，无需申诉。"
              type="info"
              show-icon
              class="info-alert"
            />

            <div v-else>
              <a-form
                :model="appealForm"
                :rules="rules"
                layout="vertical"
                @finish="handleSubmit"
              >
                <a-form-item label="选择违规记录" name="reservationId">
                  <a-select
                    v-model:value="appealForm.reservationId"
                    placeholder="请选择要申诉的违规记录"
                    style="width: 100%"
                  >
                    <a-select-option
                      v-for="record in violationRecords"
                      :key="record.id"
                      :value="record.id"
                    >
                      {{ record.seatNo }} - {{ formatTime(record.startTime) }} ({{ getStatusText(record.status) }})
                    </a-select-option>
                  </a-select>
                </a-form-item>

                <a-form-item label="申诉类型" name="appealType">
                  <a-select
                    v-model:value="appealForm.appealType"
                    placeholder="请选择申诉类型"
                    style="width: 100%"
                  >
                    <a-select-option value="PHONE_DEAD">手机没电</a-select-option>
                    <a-select-option value="QR_CODE_DAMAGED">二维码损坏</a-select-option>
                    <a-select-option value="GPS_ERROR">定位故障</a-select-option>
                    <a-select-option value="SYSTEM_ERROR">系统故障</a-select-option>
                    <a-select-option value="OTHER">其他原因</a-select-option>
                  </a-select>
                </a-form-item>

                <a-form-item label="申诉理由" name="reason">
                  <a-textarea
                    v-model:value="appealForm.reason"
                    placeholder="请详细描述您的情况..."
                    :rows="4"
                    show-count
                    :maxlength="500"
                  />
                </a-form-item>

                <a-form-item label="图片凭证">
                  <a-upload
                    list-type="picture-card"
                    :file-list="fileList"
                    @preview="handlePreview"
                    @change="handleChange"
                    :before-upload="beforeUpload"
                  >
                    <div v-if="fileList.length < 3">
                      <plus-outlined />
                      <div style="margin-top: 8px">上传</div>
                    </div>
                  </a-upload>
                  <div class="upload-hint">最多上传3张图片，支持jpg/png格式</div>
                </a-form-item>

                <a-form-item>
                  <a-button
                    type="primary"
                    html-type="submit"
                    :loading="submitting"
                    size="large"
                    block
                  >
                    提交申诉
                  </a-button>
                </a-form-item>
              </a-form>
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="history" tab="申诉记录">
          <a-table
            :columns="columns"
            :data-source="appealHistory"
            :loading="loading"
            row-key="id"
            :pagination="pagination"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'appealType'">
                <a-tag>{{ getAppealTypeText(record.appealType) }}</a-tag>
              </template>

              <template v-if="column.key === 'status'">
                <a-tag :color="getStatusColor(record.status)">
                  {{ getAppealStatusText(record.status) }}
                </a-tag>
              </template>

              <template v-if="column.key === 'credit'">
                <span v-if="record.creditReturned" class="credit-returned">
                  +{{ record.creditAmount }}分
                </span>
                <span v-else class="credit-pending">-</span>
              </template>

              <template v-if="column.key === 'createTime'">
                {{ formatTime(record.createTime) }}
              </template>

              <template v-if="column.key === 'action'">
                <a-button type="link" @click="viewDetail(record)">查看详情</a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 图片预览 -->
    <a-modal
      :open="previewVisible"
      :footer="null"
      @cancel="previewVisible = false"
    >
      <img alt="preview" style="width: 100%" :src="previewImage" />
    </a-modal>

    <!-- 申诉详情 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="申诉详情"
      :footer="null"
    >
      <div v-if="selectedAppeal" class="appeal-detail">
        <div class="detail-item">
          <span class="label">申诉类型：</span>
          <span>{{ getAppealTypeText(selectedAppeal.appealType) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">申诉理由：</span>
          <p class="reason-text">{{ selectedAppeal.reason }}</p>
        </div>
        <div class="detail-item">
          <span class="label">处理状态：</span>
          <a-tag :color="getStatusColor(selectedAppeal.status)">
            {{ getAppealStatusText(selectedAppeal.status) }}
          </a-tag>
        </div>
        <div v-if="selectedAppeal.reply" class="detail-item">
          <span class="label">管理员回复：</span>
          <p class="reply-text">{{ selectedAppeal.reply }}</p>
        </div>
        <div v-if="selectedAppeal.creditReturned" class="detail-item">
          <span class="label">信用分返还：</span>
          <span class="credit-returned">+{{ selectedAppeal.creditAmount }}分</span>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  FileProtectOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import {
  submitAppeal,
  getMyAppeals,
  getMyHistory,
  type AppealData,
  type AppealRecord,
  type ReservationRecord
} from '../../api/reservation'

const activeTab = ref('submit')
const submitting = ref(false)
const loading = ref(false)
const violationRecords = ref<ReservationRecord[]>([])
const appealHistory = ref<AppealRecord[]>([])

const appealForm = ref<AppealData & { reservationId?: number }>({
  reservationId: undefined,
  appealType: 'OTHER',
  reason: '',
  images: []
})

const rules = {
  reservationId: [{ required: true, message: '请选择违规记录' }],
  appealType: [{ required: true, message: '请选择申诉类型' }],
  reason: [{ required: true, message: '请填写申诉理由', min: 10 }]
}

const columns = [
  { title: '申诉类型', key: 'appealType', width: 120 },
  { title: '申诉理由', dataIndex: 'reason', key: 'reason', ellipsis: true },
  { title: '处理状态', key: 'status', width: 100 },
  { title: '信用分返还', key: 'credit', width: 100 },
  { title: '提交时间', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 100 }
]

const pagination = {
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条记录`
}

const hasViolationRecord = computed(() => violationRecords.value.length > 0)

// 文件上传相关
const fileList = ref<any[]>([])
const previewVisible = ref(false)
const previewImage = ref('')
const detailModalVisible = ref(false)
const selectedAppeal = ref<AppealRecord | null>(null)

const fetchViolationRecords = async () => {
  try {
    const res = await getMyHistory()
    const records = (res as any).data || res || []
    violationRecords.value = records.filter((r: ReservationRecord) => r.status === 'violation')
  } catch (e) {
    console.error('获取违规记录失败', e)
  }
}

const fetchAppealHistory = async () => {
  loading.value = true
  try {
    const res = await getMyAppeals()
    appealHistory.value = (res as any).data || res || []
  } catch (e) {
    message.error('获取申诉记录失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!appealForm.value.reservationId) {
    message.warning('请选择违规记录')
    return
  }

  submitting.value = true
  try {
    const images = fileList.value.map(file => file.url || file.thumbUrl)
    await submitAppeal(appealForm.value.reservationId, {
      appealType: appealForm.value.appealType,
      reason: appealForm.value.reason,
      images
    })
    message.success('申诉提交成功，请等待管理员审核')
    appealForm.value = {
      reservationId: undefined,
      appealType: 'OTHER',
      reason: '',
      images: []
    }
    fileList.value = []
    fetchAppealHistory()
    activeTab.value = 'history'
  } catch (e) {
    message.error('申诉提交失败')
  } finally {
    submitting.value = false
  }
}

const handlePreview = (file: any) => {
  previewImage.value = file.url || file.thumbUrl
  previewVisible.value = true
}

const handleChange = ({ fileList: newFileList }: any) => {
  fileList.value = newFileList
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件')
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过2MB')
  }
  return false // 阻止自动上传
}

const viewDetail = (record: AppealRecord) => {
  selectedAppeal.value = record
  detailModalVisible.value = true
}

const getAppealTypeText = (type: string) => {
  const texts: Record<string, string> = {
    PHONE_DEAD: '手机没电',
    QR_CODE_DAMAGED: '二维码损坏',
    GPS_ERROR: '定位故障',
    SYSTEM_ERROR: '系统故障',
    OTHER: '其他原因'
  }
  return texts[type] || type
}

const getAppealStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回'
  }
  return texts[status] || status
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'error'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    violation: '违规',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || status
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
  fetchViolationRecords()
  fetchAppealHistory()
})
</script>

<style scoped>
.appeal-page {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.appeal-card {
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

.submit-section {
  padding: 16px 0;
}

.info-alert {
  margin-bottom: 24px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.credit-returned {
  color: #52c41a;
  font-weight: 500;
}

.credit-pending {
  color: #999;
}

.appeal-detail {
  padding: 16px;
}

.detail-item {
  margin-bottom: 16px;
}

.detail-item .label {
  font-weight: 500;
  color: #666;
}

.reason-text,
.reply-text {
  margin-top: 8px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .appeal-page {
    padding: 16px;
  }
}
</style>
