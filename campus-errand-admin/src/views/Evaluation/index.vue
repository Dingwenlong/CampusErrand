<template>
  <div class="evaluation-management">
    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stat-cards">
      <a-col :span="6">
        <a-card>
          <Statistic title="总评价数" :value="stats.totalCount" :value-style="{ color: '#3f8600' }">
            <template #prefix>
              <StarOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <Statistic title="平均评分" :value="stats.averageRating" :precision="1" :value-style="{ color: '#faad14' }">
            <template #prefix>
              <TrophyOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <Statistic title="好评数(4-5星)" :value="goodCount" :value-style="{ color: '#52c41a' }">
            <template #prefix>
              <LikeOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <Statistic title="今日新增" :value="stats.todayCount || 0" :value-style="{ color: '#722ed1' }">
            <template #prefix>
              <PlusOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 评分分布 -->
    <a-row :gutter="16" class="rating-stats">
      <a-col :span="24">
        <a-card title="评分分布">
          <a-row :gutter="16">
            <a-col :span="4" v-for="rating in [5, 4, 3, 2, 1]" :key="rating">
              <div class="rating-stat-item" :class="'rating-' + rating">
                <div class="rating-star">{{ rating }}星</div>
                <div class="rating-count">{{ stats.ratingCount?.[rating] || 0 }}条</div>
                <a-progress 
                  :percent="getRatingPercent(rating)" 
                  :show-info="false"
                  :stroke-color="getRatingColor(rating)"
                />
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!-- 搜索筛选 -->
    <a-card class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="评分">
          <a-select v-model:value="searchForm.rating" placeholder="全部评分" style="width: 120px" allowClear>
            <a-select-option :value="5">5星</a-select-option>
            <a-select-option :value="4">4星</a-select-option>
            <a-select-option :value="3">3星</a-select-option>
            <a-select-option :value="2">2星</a-select-option>
            <a-select-option :value="1">1星</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键词">
          <a-input v-model:value="searchForm.keyword" placeholder="评价内容" allowClear />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">
            <SearchOutlined />
            搜索
          </a-button>
          <a-button style="margin-left: 8px" @click="handleReset">
            重置
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 评价列表 -->
    <a-card title="评价列表">
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

// 获取评分占比
const getRatingPercent = (rating: number) => {
  const ratingCount = stats.value.ratingCount || {}
  const count = ratingCount[rating] || 0
  const total = stats.value.totalCount || 1
  return Math.round((count / total) * 100)
}

// 获取评分颜色
const getRatingColor = (rating: number) => {
  const colors: Record<number, string> = {
    5: '#52c41a',
    4: '#52c41a',
    3: '#faad14',
    2: '#ff7a45',
    1: '#ff4d4f'
  }
  return colors[rating] || '#999'
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

.stat-cards {
  margin-bottom: 24px;
}

.rating-stats {
  margin-bottom: 24px;
}

.rating-stat-item {
  text-align: center;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.rating-stat-item.rating-5,
.rating-stat-item.rating-4 {
  background: #f6ffed;
}

.rating-stat-item.rating-3 {
  background: #fffbe6;
}

.rating-stat-item.rating-2,
.rating-stat-item.rating-1 {
  background: #fff2f0;
}

.rating-star {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.rating-count {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.search-card {
  margin-bottom: 24px;
}

.user-info {
  display: flex;
  align-items: center;
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
</style>
