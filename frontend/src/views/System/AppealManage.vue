<template>
  <div class="appeal-manage-page">
    <a-card :bordered="false" class="manage-card">
      <template #title>
        <div class="card-title-wrapper">
          <file-protect-outlined class="title-icon" />
          <span>申诉审核管理</span>
        </div>
      </template>

      <!-- 统计卡片 -->
      <a-row :gutter="[16, 16]" class="stats-row">
        <a-col :xs="24" :sm="8">
          <a-card class="stat-card pending">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待审核</div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="8">
          <a-card class="stat-card approved">
            <div class="stat-value">{{ stats.approved }}</div>
            <div class="stat-label">已通过</div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="8">
          <a-card class="stat-card rejected">
            <div class="stat-value">{{ stats.rejected }}</div>
            <div class="stat-label">已驳回</div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 筛选标签 -->
      <div class="filter-tabs">
        <a-radio-group v-model:value="filterStatus" @change="handleFilterChange">
          <a-radio-button value="all">全部</a-radio-button>
          <a-radio-button value="pending">待审核</a-radio-button>
          <a-radio-button value="approved">已通过</a-radio-button>
          <a-radio-button value="rejected">已驳回</a-radio-button>
        </a-radio-group>
      </div>

      <!-- 申诉列表 -->
      <a-table
        :columns="columns"
        :data-source="filteredAppeals"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        class="appeal-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userInfo'">
            <div class="user-info">
              <span class="username">{{ record.realName || record.username }}</span>
              <span class="user-id">ID: {{ record.userId }}</span>
            </div>
          </template>

          <template v-if="column.key === 'seatInfo'">
            <span class="seat-no">{{ record.seatNo || '-' }}</span>
          </template>

          <template v-if="column.key === 'appealType'">
            <a-tag>{{ getAppealTypeText(record.appealType) }}</a-tag>
          </template>

          <template v-if="column.key === 'reason'">
            <a-typography-text ellipsis style="max-width: 200px">
              {{ record.reason }}
            </a-typography-text>
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
            <span v-else>-</span>
          </template>

          <template v-if="column.key === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="viewDetail(record)">查看</a-button>
              <template v-if="record.status === 'pending'">
                <a-button type="link" style="color: #52c41a" @click="handleApprove(record)">通过</a-button>
                <a-button type="link" danger @click="handleReject(record)">驳回</a-button>
              </template>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 申诉详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="申诉详情"
      :footer="null"
      width="700px"
    >
      <div v-if="selectedAppeal" class="appeal-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="申诉人">{{ selectedAppeal.realName || selectedAppeal.username }}</a-descriptions-item>
          <a-descriptions-item label="座位号">{{ selectedAppeal.seatNo || '-' }}</a-descriptions-item>
          <a-descriptions-item label="申诉类型">
            <a-tag>{{ getAppealTypeText(selectedAppeal.appealType) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="提交时间">{{ formatTime(selectedAppeal.createTime) }}</a-descriptions-item>
          <a-descriptions-item label="处理状态" :span="2">
            <a-tag :color="getStatusColor(selectedAppeal.status)">
              {{ getAppealStatusText(selectedAppeal.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="申诉理由" :span="2">
            <p class="reason-text">{{ selectedAppeal.reason }}</p>
          </a-descriptions-item>
          <a-descriptions-item v-if="selectedAppeal.images" label="图片凭证" :span="2">
            <div class="image-list">
              <img
                v-for="(img, index) in parseImages(selectedAppeal.images)"
                :key="index"
                :src="img"
                class="appeal-image"
                @click="previewImage(img)"
              />
            </div>
          </a-descriptions-item>
          <a-descriptions-item v-if="selectedAppeal.reply" label="管理员回复" :span="2">
            <p class="reply-text">{{ selectedAppeal.reply }}</p>
          </a-descriptions-item>
          <a-descriptions-item v-if="selectedAppeal.creditReturned" label="信用分返还" :span="2">
            <span class="credit-returned">+{{ selectedAppeal.creditAmount }}分</span>
          </a-descriptions-item>
        </a-descriptions>

        <!-- 审核操作区 -->
        <div v-if="selectedAppeal.status === 'pending'" class="review-actions">
          <a-divider />
          <h4>审核操作</h4>
          <a-form layout="vertical">
            <a-form-item label="审核结果">
              <a-radio-group v-model:value="reviewForm.status">
                <a-radio value="approved">通过</a-radio>
                <a-radio value="rejected">驳回</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="审核回复">
              <a-textarea
                v-model:value="reviewForm.reply"
                placeholder="请输入审核回复..."
                :rows="3"
              />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" :loading="reviewing" @click="submitReview">提交审核</a-button>
                <a-button @click="detailModalVisible = false">取消</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </a-modal>

    <!-- 快速审核弹窗 -->
    <a-modal
      v-model:open="quickReviewModalVisible"
      :title="quickReviewType === 'approve' ? '通过申诉' : '驳回申诉'"
      @ok="submitQuickReview"
      @cancel="quickReviewModalVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="审核回复">
          <a-textarea
            v-model:value="quickReviewReply"
            placeholder="请输入审核回复..."
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 图片预览 -->
    <a-image
      :style="{ display: 'none' }"
      :preview="{
        visible: previewVisible,
        onVisibleChange: (visible: boolean) => previewVisible = visible,
      }"
      :src="currentPreviewImage"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { FileProtectOutlined } from '@ant-design/icons-vue'
import { getAllAppeals, reviewAppeal, type AppealRecord } from '../../api/reservation'

const loading = ref(false)
const appeals = ref<AppealRecord[]>([])
const filterStatus = ref('all')
const detailModalVisible = ref(false)
const quickReviewModalVisible = ref(false)
const selectedAppeal = ref<AppealRecord | null>(null)
const quickReviewType = ref<'approve' | 'reject'>('approve')
const quickReviewReply = ref('')
const reviewing = ref(false)
const previewVisible = ref(false)
const currentPreviewImage = ref('')

const columns = [
  { title: '申诉人', key: 'userInfo', width: 150 },
  { title: '座位', key: 'seatInfo', width: 100 },
  { title: '申诉类型', key: 'appealType', width: 120 },
  { title: '申诉理由', key: 'reason', ellipsis: true },
  { title: '状态', key: 'status', width: 100 },
  { title: '返还分数', key: 'credit', width: 100 },
  { title: '提交时间', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

const pagination = {
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条记录`
}

const stats = computed(() => {
  const pending = appeals.value.filter(a => a.status === 'pending').length
  const approved = appeals.value.filter(a => a.status === 'approved').length
  const rejected = appeals.value.filter(a => a.status === 'rejected').length
  return { pending, approved, rejected }
})

const filteredAppeals = computed(() => {
  if (filterStatus.value === 'all') {
    return appeals.value
  }
  return appeals.value.filter(a => a.status === filterStatus.value)
})

const reviewForm = ref({
  status: 'approved' as 'approved' | 'rejected',
  reply: ''
})

const fetchAppeals = async () => {
  loading.value = true
  try {
    const res = await getAllAppeals()
    appeals.value = (res as any).data || res || []
  } catch (e) {
    message.error('获取申诉列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  // 筛选状态改变时自动刷新
}

const viewDetail = (record: AppealRecord) => {
  selectedAppeal.value = record
  reviewForm.value = {
    status: 'approved',
    reply: ''
  }
  detailModalVisible.value = true
}

const handleApprove = (record: AppealRecord) => {
  selectedAppeal.value = record
  quickReviewType.value = 'approve'
  quickReviewReply.value = '经审核，申诉理由合理，同意返还信用分。'
  quickReviewModalVisible.value = true
}

const handleReject = (record: AppealRecord) => {
  selectedAppeal.value = record
  quickReviewType.value = 'reject'
  quickReviewReply.value = ''
  quickReviewModalVisible.value = true
}

const submitReview = async () => {
  if (!selectedAppeal.value) return

  reviewing.value = true
  try {
    await reviewAppeal(selectedAppeal.value.id, {
      status: reviewForm.value.status,
      reply: reviewForm.value.reply
    })
    message.success('审核提交成功')
    detailModalVisible.value = false
    await fetchAppeals()
  } catch (e) {
    message.error('审核提交失败')
  } finally {
    reviewing.value = false
  }
}

const submitQuickReview = async () => {
  if (!selectedAppeal.value) return

  try {
    await reviewAppeal(selectedAppeal.value.id, {
      status: quickReviewType.value === 'approve' ? 'approved' : 'rejected',
      reply: quickReviewReply.value
    })
    message.success('审核提交成功')
    quickReviewModalVisible.value = false
    await fetchAppeals()
  } catch (e) {
    message.error('审核提交失败')
  }
}

const parseImages = (images: string) => {
  try {
    return JSON.parse(images)
  } catch {
    return images ? images.split(',') : []
  }
}

const previewImage = (img: string) => {
  currentPreviewImage.value = img
  previewVisible.value = true
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
  fetchAppeals()
})
</script>

<style scoped>
.appeal-manage-page {
  padding: 24px;
}

.manage-card {
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
}

.stat-card.pending {
  background: linear-gradient(135deg, #fffbe6 0%, #ffe58f 100%);
  border: 1px solid #ffd666;
}

.stat-card.approved {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border: 1px solid #b7eb8f;
}

.stat-card.rejected {
  background: linear-gradient(135deg, #fff2f0 0%, #ffccc7 100%);
  border: 1px solid #ff7875;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-card.pending .stat-value {
  color: #faad14;
}

.stat-card.approved .stat-value {
  color: #52c41a;
}

.stat-card.rejected .stat-value {
  color: #ff4d4f;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.filter-tabs {
  margin-bottom: 16px;
}

.appeal-table {
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
  font-weight: 500;
}

.credit-returned {
  color: #52c41a;
  font-weight: 500;
}

.appeal-detail {
  padding: 16px;
}

.reason-text,
.reply-text {
  margin: 0;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  line-height: 1.6;
}

.image-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.appeal-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.appeal-image:hover {
  transform: scale(1.05);
}

.review-actions {
  margin-top: 24px;
}

@media (max-width: 768px) {
  .appeal-manage-page {
    padding: 16px;
  }
}
</style>
