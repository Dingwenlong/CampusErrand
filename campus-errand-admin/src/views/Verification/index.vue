<template>
  <div class="verification-page">
    <a-card class="filter-card">
      <a-form layout="inline" :model="query">
        <a-form-item label="审核状态">
          <a-select v-model:value="query.status" allowClear style="width: 140px" placeholder="全部">
            <a-select-option :value="1">待审核</a-select-option>
            <a-select-option :value="2">已通过</a-select-option>
            <a-select-option :value="3">已驳回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allowClear placeholder="昵称/姓名/手机号" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="reload">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card>
      <a-table
        row-key="id"
        :columns="columns"
        :data-source="list"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'user'">
            <div class="user-cell">
              <a-avatar :src="record.avatar">{{ record.nickname?.slice(0, 1) || '用' }}</a-avatar>
              <div>
                <div class="name">{{ record.nickname || '-' }}</div>
                <div class="muted">{{ record.phone || '-' }}</div>
              </div>
            </div>
          </template>
          <template v-if="column.key === 'realName'">
            <div>{{ record.realName || '-' }}</div>
            <div class="muted">{{ maskIdCard(record.idCard) }}</div>
          </template>
          <template v-if="column.key === 'images'">
            <a-image-preview-group>
              <a-space>
                <a-image v-if="record.idCardFrontImage" :width="54" :src="record.idCardFrontImage" />
                <a-image v-if="record.idCardBackImage" :width="54" :src="record.idCardBackImage" />
              </a-space>
            </a-image-preview-group>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.verifyStatus)">{{ statusText(record.verifyStatus) }}</a-tag>
            <div v-if="record.verifyRejectReason" class="muted">{{ record.verifyRejectReason }}</div>
          </template>
          <template v-if="column.key === 'action'">
            <a-space v-if="record.verifyStatus === 1">
              <a-popconfirm title="确认通过该实名认证？" @confirm="approve(record.id)">
                <a-button type="link" size="small">通过</a-button>
              </a-popconfirm>
              <a-button type="link" danger size="small" @click="openReject(record)">驳回</a-button>
            </a-space>
            <span v-else class="muted">已处理</span>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="rejectVisible" title="驳回实名认证" :confirm-loading="rejecting" @ok="submitReject">
      <a-textarea v-model:value="rejectReason" :rows="4" placeholder="请输入驳回原因" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { approveVerification, getVerificationList, rejectVerification } from '@/api/verification'

const loading = ref(false)
const rejecting = ref(false)
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectingRecord = ref<any>(null)
const list = ref<any[]>([])

const query = reactive({
  status: 1 as number | undefined,
  keyword: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: '用户', key: 'user', width: 220 },
  { title: '实名信息', key: 'realName', width: 220 },
  { title: '证件照片', key: 'images', width: 150 },
  { title: '状态', key: 'status', width: 160 },
  { title: '提交时间', dataIndex: 'verifySubmitTime', key: 'verifySubmitTime', width: 180 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' as const }
]

const load = async () => {
  loading.value = true
  try {
    const res = await getVerificationList({
      status: query.status,
      keyword: query.keyword || undefined,
      current: pagination.current,
      size: pagination.pageSize
    })
    if (res.code === 200) {
      list.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

const reload = () => {
  pagination.current = 1
  load()
}

const reset = () => {
  query.status = 1
  query.keyword = ''
  reload()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  load()
}

const approve = async (id: number) => {
  const res = await approveVerification(id)
  if (res.code === 200) {
    message.success('已通过')
    load()
  }
}

const openReject = (record: any) => {
  rejectingRecord.value = record
  rejectReason.value = ''
  rejectVisible.value = true
}

const submitReject = async () => {
  if (!rejectReason.value.trim()) {
    message.warning('请输入驳回原因')
    return
  }
  rejecting.value = true
  try {
    const res = await rejectVerification(rejectingRecord.value.id, rejectReason.value.trim())
    if (res.code === 200) {
      message.success('已驳回')
      rejectVisible.value = false
      load()
    }
  } finally {
    rejecting.value = false
  }
}

const statusText = (status: number) => ({ 1: '待审核', 2: '已通过', 3: '已驳回' }[status] || '未提交')
const statusColor = (status: number) => ({ 1: 'processing', 2: 'success', 3: 'error' }[status] || 'default')
const maskIdCard = (idCard?: string) => idCard ? `${idCard.slice(0, 3)}***********${idCard.slice(-4)}` : '-'

onMounted(load)
</script>

<style scoped>
.verification-page {
  animation: fade-up 0.32s ease;
}

.filter-card {
  margin-bottom: 16px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.name {
  font-weight: 600;
  color: #1f2937;
}

.muted {
  color: #64748b;
  font-size: 12px;
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
