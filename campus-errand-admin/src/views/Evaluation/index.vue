<template>
  <div class="evaluation-management">
    <!-- 统计卡片 - 响应式 -->
    <a-row :gutter="[16, 16]" class="stat-cards">
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);">
            <StarOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">总评价数</div>
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);">
            <TrophyOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">平均评分</div>
            <div class="stat-value">{{ (stats.averageRating || 0).toFixed(1) }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);">
            <LikeOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">好评数</div>
            <div class="stat-value">{{ goodCount }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);">
            <PlusOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">今日新增</div>
            <div class="stat-value">{{ stats.todayCount || 0 }}</div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 搜索筛选 -->
    <a-card class="search-card">
      <a-form :model="searchForm" class="search-form">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
            <a-form-item label="评分" class="form-item">
              <a-select v-model:value="searchForm.rating" placeholder="全部评分" allowClear>
                <a-select-option :value="5">5星</a-select-option>
                <a-select-option :value="4">4星</a-select-option>
                <a-select-option :value="3">3星</a-select-option>
                <a-select-option :value="2">2星</a-select-option>
                <a-select-option :value="1">1星</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
            <a-form-item label="关键词" class="form-item">
              <a-input v-model:value="searchForm.keyword" placeholder="评价内容" allowClear />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="24" :md="8" :lg="12" :xl="12">
            <a-form-item class="form-item">
              <a-space>
                <a-button type="primary" @click="handleSearch">
                  <SearchOutlined />
                  搜索
                </a-button>
                <a-button @click="handleReset">
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- 评价列表 - 桌面端 -->
    <a-card class="table-card desktop-table" v-if="!isMobile">
      <a-table
        :columns="columns"
        :data-source="evaluationList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'rating'">
            <a-rate :value="record.rating" disabled />
          </template>

          <template v-if="column.key === 'fromUser'">
            <div class="user-info">
              <a-avatar :src="record.fromUserAvatar" size="small" />
              <span style="margin-left: 8px;">{{ record.isAnonymous ? '匿名用户' : record.fromUserName }}</span>
            </div>
          </template>

          <template v-if="column.key === 'toUser'">
            <div class="user-info">
              <a-avatar :src="record.toUserAvatar" size="small" />
              <span style="margin-left: 8px;">{{ record.toUserName }}</span>
            </div>
          </template>

          <template v-if="column.key === 'content'">
            <div class="content-cell">
              <div class="content-text">{{ record.content }}</div>
              <div class="content-tags" v-if="record.tags && record.tags.length > 0">
                <a-tag v-for="tag in record.tags" :key="tag" size="small">{{ tag }}</a-tag>
              </div>
            </div>
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" danger size="small" @click="handleDelete(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 移动端卡片列表 -->
    <div class="mobile-list" v-else>
      <a-card v-for="item in evaluationList" :key="item.id" class="evaluation-card">
        <div class="evaluation-header">
          <a-rate :value="item.rating" disabled size="small" />
          <span class="evaluation-time">{{ item.createTime }}</span>
        </div>
        <div class="evaluation-body">
          <div class="info-row">
            <span class="info-label">评价人:</span>
            <div class="user-info">
              <a-avatar :src="item.fromUserAvatar" :size="20" />
              <span>{{ item.isAnonymous ? '匿名用户' : item.fromUserName }}</span>
            </div>
          </div>
          <div class="info-row">
            <span class="info-label">被评价人:</span>
            <div class="user-info">
              <a-avatar :src="item.toUserAvatar" :size="20" />
              <span>{{ item.toUserName }}</span>
            </div>
          </div>
          <div class="info-row">
            <span class="info-label">任务:</span>
            <span class="info-value">{{ item.taskTitle }}</span>
          </div>
          <div class="content-section">
            <div class="content-text">{{ item.content }}</div>
            <div class="content-tags" v-if="item.tags && item.tags.length > 0">
              <a-tag v-for="tag in item.tags" :key="tag" size="small">{{ tag }}</a-tag>
            </div>
          </div>
        </div>
        <div class="evaluation-footer">
          <a-button type="link" danger size="small" @click="handleDelete(item)">
            删除
          </a-button>
        </div>
      </a-card>
      <div class="mobile-pagination">
        <a-pagination
          v-model:current="pagination.current"
          :total="pagination.total"
          :pageSize="pagination.pageSize"
          size="small"
          show-less-items
          @change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  StarOutlined,
  TrophyOutlined,
  LikeOutlined,
  PlusOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'
import { getEvaluationList, deleteEvaluation, getEvaluationStats } from '@/api/evaluation'
import { useResponsive } from '@/composables/useResponsive'

const { isMobile } = useResponsive()
const loading = ref(false)
const evaluationList = ref([])
const stats = ref<any>({})

// 搜索表单
const searchForm = reactive({
  rating: undefined,
  keyword: ''
})

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列
const columns = [
  { title: '评分', key: 'rating', width: 150 },
  { title: '评价人', key: 'fromUser', width: 150 },
  { title: '被评价人', key: 'toUser', width: 150 },
  { title: '关联任务', dataIndex: 'taskTitle', key: 'taskTitle', ellipsis: true },
  { title: '评价内容', key: 'content', ellipsis: true },
  { title: '评价时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' as const }
]

// 好评数（4-5星）
const goodCount = computed(() => {
  const ratingCount = stats.value.ratingCount || {}
  return (ratingCount[4] || 0) + (ratingCount[5] || 0)
})

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getEvaluationStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

// 加载评价列表
const loadEvaluationList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      rating: searchForm.rating,
      keyword: searchForm.keyword
    }
    const res = await getEvaluationList(params)
    if (res.code === 200) {
      evaluationList.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载评价列表失败', error)
    message.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadEvaluationList()
}

// 重置
const handleReset = () => {
  searchForm.rating = undefined
  searchForm.keyword = ''
  pagination.current = 1
  loadEvaluationList()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadEvaluationList()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.current = page
  loadEvaluationList()
}

// 删除评价
const handleDelete = (record: any) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条评价吗？此操作不可恢复。',
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await deleteEvaluation(record.id)
        if (res.code === 200) {
          message.success('删除成功')
          loadEvaluationList()
          loadStats()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => {
  loadStats()
  loadEvaluationList()
})
</script>

<style scoped>
.evaluation-management {
  padding: 0;
}

/* 统计卡片 */
.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
}

.stat-card :deep(.ant-card-body) {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  margin-right: 12px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-card {
  margin-bottom: 24px;
}

.search-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.table-card {
  margin-bottom: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-cell {
  display: flex;
  flex-direction: column;
}

.content-text {
  margin-bottom: 4px;
}

.content-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

/* 移动端卡片列表 */
.mobile-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.evaluation-card {
  border-radius: 12px;
}

.evaluation-card :deep(.ant-card-body) {
  padding: 16px;
}

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.evaluation-time {
  font-size: 12px;
  color: #999;
}

.evaluation-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-size: 13px;
  color: #999;
  min-width: 60px;
}

.info-value {
  font-size: 13px;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-section {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  margin-top: 4px;
}

.content-text {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
}

.evaluation-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.mobile-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .stat-cards {
    margin-bottom: 16px;
  }
  
  .stat-card :deep(.ant-card-body) {
    padding: 12px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
    margin-right: 8px;
  }
  
  .stat-label {
    font-size: 12px;
  }
  
  .stat-value {
    font-size: 16px;
  }
  
  .search-card {
    margin-bottom: 16px;
  }
  
  .search-form :deep(.ant-form-item-label) {
    padding-bottom: 4px;
  }
  
  .form-item {
    margin-bottom: 0;
  }
}

@media (max-width: 576px) {
  .stat-card :deep(.ant-card-body) {
    flex-direction: column;
    text-align: center;
    padding: 10px;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }
  
  .stat-content {
    text-align: center;
  }
}
</style>
