<template>
  <div class="task-management">
    <!-- 统计卡片 - 响应式 -->
    <a-row :gutter="[16, 16]" class="stat-cards">
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);">
            <FileTextOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">总任务数</div>
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);">
            <ClockCircleOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">待接单</div>
            <div class="stat-value">{{ stats.statusCount?.['待接单'] || 0 }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);">
            <CarryOutOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">进行中</div>
            <div class="stat-value">{{ (stats.statusCount?.['已接单'] || 0) + (stats.statusCount?.['待取件'] || 0) + (stats.statusCount?.['配送中'] || 0) }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);">
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
            <a-form-item label="任务类型" class="form-item">
              <a-select v-model:value="searchForm.taskType" placeholder="全部类型" allowClear>
                <a-select-option :value="1">取快递</a-select-option>
                <a-select-option :value="2">代买</a-select-option>
                <a-select-option :value="3">送件</a-select-option>
                <a-select-option :value="4">其他</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
            <a-form-item label="任务状态" class="form-item">
              <a-select v-model:value="searchForm.status" placeholder="全部状态" allowClear>
                <a-select-option :value="0">待接单</a-select-option>
                <a-select-option :value="1">已接单</a-select-option>
                <a-select-option :value="2">待取件</a-select-option>
                <a-select-option :value="3">配送中</a-select-option>
                <a-select-option :value="4">待确认</a-select-option>
                <a-select-option :value="5">已完成</a-select-option>
                <a-select-option :value="6">已取消</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
            <a-form-item label="关键词" class="form-item">
              <a-input v-model:value="searchForm.keyword" placeholder="任务标题" allowClear />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
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

    <!-- 任务列表 - 桌面端表格 -->
    <a-card class="table-card desktop-table" v-if="!isMobile">
      <a-table
        :columns="columns"
        :data-source="taskList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'taskType'">
            <a-tag :color="getTaskTypeColor(record.taskType)">
              {{ record.taskTypeName }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusName }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'totalAmount'">
            <span style="color: #ff4d4f; font-weight: bold;">¥{{ record.totalAmount }}</span>
          </template>
          
          <template v-if="column.key === 'publisher'">
            <div class="user-info">
              <a-avatar :src="record.publisherAvatar" size="small" />
              <span style="margin-left: 8px;">{{ record.publisherName }}</span>
            </div>
          </template>
          
          <template v-if="column.key === 'runner'">
            <div class="user-info" v-if="record.runnerName">
              <a-avatar :src="record.runnerAvatar" size="small" />
              <span style="margin-left: 8px;">{{ record.runnerName }}</span>
            </div>
            <span v-else style="color: #999;">-</span>
          </template>
          
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                查看
              </a-button>
              <a-button 
                type="link" 
                size="small" 
                danger 
                @click="handleCancel(record)"
                v-if="record.status === 0 || record.status === 1 || record.status === 2"
              >
                取消
              </a-button>
              <a-button 
                type="link" 
                size="small" 
                danger 
                @click="handleDelete(record)"
                v-if="record.status === 6"
              >
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 任务列表 - 移动端卡片 -->
    <div class="mobile-list" v-else>
      <a-card v-for="task in taskList" :key="task.id" class="task-card">
        <div class="task-card-header">
          <div class="task-title-section">
            <a-tag :color="getTaskTypeColor(task.taskType)" class="task-type-tag">
              {{ task.taskTypeName }}
            </a-tag>
            <span class="task-title">{{ task.title }}</span>
          </div>
          <a-tag :color="getStatusColor(task.status)" class="status-tag">
            {{ task.statusName }}
          </a-tag>
        </div>
        
        <div class="task-card-body">
          <div class="info-row">
            <span class="info-label">赏金:</span>
            <span class="info-value price">¥{{ task.totalAmount }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">发布者:</span>
            <div class="user-info">
              <a-avatar :src="task.publisherAvatar" :size="20" />
              <span>{{ task.publisherName }}</span>
            </div>
          </div>
          <div class="info-row">
            <span class="info-label">接单者:</span>
            <div class="user-info" v-if="task.runnerName">
              <a-avatar :src="task.runnerAvatar" :size="20" />
              <span>{{ task.runnerName }}</span>
            </div>
            <span v-else class="info-value">-</span>
          </div>
          <div class="info-row">
            <span class="info-label">创建时间:</span>
            <span class="info-value">{{ task.createTime }}</span>
          </div>
        </div>
        
        <div class="task-card-footer">
          <a-button type="link" size="small" @click="handleView(task)">
            查看
          </a-button>
          <a-button 
            type="link" 
            size="small" 
            danger 
            @click="handleCancel(task)"
            v-if="task.status === 0 || task.status === 1 || task.status === 2"
          >
            取消
          </a-button>
          <a-button 
            type="link" 
            size="small" 
            danger 
            @click="handleDelete(task)"
            v-if="task.status === 6"
          >
            删除
          </a-button>
        </div>
      </a-card>
      
      <!-- 移动端分页 -->
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

    <!-- 任务详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="任务详情"
      width="700px"
      :footer="null"
      class="task-detail-modal"
    >
      <a-descriptions :column="isMobile ? 1 : 2" bordered v-if="currentTask">
        <a-descriptions-item label="任务ID">{{ currentTask.id }}</a-descriptions-item>
        <a-descriptions-item label="任务类型">
          <a-tag :color="getTaskTypeColor(currentTask.taskType)">{{ currentTask.taskTypeName }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="任务标题" :span="isMobile ? 1 : 2">{{ currentTask.title }}</a-descriptions-item>
        <a-descriptions-item label="任务状态">
          <a-tag :color="getStatusColor(currentTask.status)">{{ currentTask.statusName }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="赏金金额">
          <span style="color: #ff4d4f; font-weight: bold;">¥{{ currentTask.totalAmount }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="发布者" :span="isMobile ? 1 : 2">
          <div class="user-info">
            <a-avatar :src="currentTask.publisherAvatar" />
            <span style="margin-left: 8px;">{{ currentTask.publisherName }}</span>
            <span style="margin-left: 8px; color: #999;">{{ currentTask.publisherPhone }}</span>
          </div>
        </a-descriptions-item>
        <a-descriptions-item label="接单者" :span="isMobile ? 1 : 2">
          <div class="user-info" v-if="currentTask.runnerName">
            <a-avatar :src="currentTask.runnerAvatar" />
            <span style="margin-left: 8px;">{{ currentTask.runnerName }}</span>
            <span style="margin-left: 8px; color: #999;">{{ currentTask.runnerPhone }}</span>
          </div>
          <span v-else style="color: #999;">暂无接单者</span>
        </a-descriptions-item>
        <a-descriptions-item label="取件地址" :span="isMobile ? 1 : 2">{{ currentTask.pickupAddress }}</a-descriptions-item>
        <a-descriptions-item label="送达地址" :span="isMobile ? 1 : 2">{{ currentTask.deliveryAddress }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatTime(currentTask.createTime) }}</a-descriptions-item>
        <a-descriptions-item label="接单时间">{{ currentTask.acceptTime ? formatTime(currentTask.acceptTime) : '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 取消任务弹窗 -->
    <a-modal
      v-model:visible="cancelVisible"
      title="取消任务"
      @ok="confirmCancel"
      :confirmLoading="cancelLoading"
    >
      <a-form :model="cancelForm" layout="vertical">
        <a-form-item label="取消原因" required>
          <a-textarea v-model:value="cancelForm.reason" :rows="3" placeholder="请输入取消原因" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  FileTextOutlined,
  ClockCircleOutlined,
  CarryOutOutlined,
  PlusOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'
import { getTaskList, getTaskDetail, cancelTask, deleteTask, getTaskStats } from '@/api/task'
import { useResponsive } from '@/composables/useResponsive'

const { isMobile } = useResponsive()
const loading = ref(false)
const taskList = ref([])
const stats = ref<any>({})

// 搜索表单
const searchForm = reactive({
  taskType: undefined,
  status: undefined,
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
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '任务类型', key: 'taskType', width: 100 },
  { title: '任务标题', dataIndex: 'title', key: 'title', ellipsis: true },
  { title: '状态', key: 'status', width: 100 },
  { title: '赏金', key: 'totalAmount', width: 100 },
  { title: '发布者', key: 'publisher', width: 150 },
  { title: '接单者', key: 'runner', width: 150 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

// 任务详情
const detailVisible = ref(false)
const currentTask = ref<any>(null)

// 取消任务
const cancelVisible = ref(false)
const cancelLoading = ref(false)
const cancelForm = reactive({
  taskId: 0,
  reason: ''
})

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getTaskStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

// 加载任务列表
const loadTaskList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      taskType: searchForm.taskType,
      status: searchForm.status,
      keyword: searchForm.keyword
    }
    const res = await getTaskList(params)
    if (res.code === 200) {
      taskList.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载任务列表失败', error)
    message.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadTaskList()
}

// 重置
const handleReset = () => {
  searchForm.taskType = undefined
  searchForm.status = undefined
  searchForm.keyword = ''
  pagination.current = 1
  loadTaskList()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadTaskList()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.current = page
  loadTaskList()
}

// 查看详情
const handleView = async (record: any) => {
  try {
    const res = await getTaskDetail(record.id)
    if (res.code === 200) {
      currentTask.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    message.error('加载任务详情失败')
  }
}

// 取消任务
const handleCancel = (record: any) => {
  cancelForm.taskId = record.id
  cancelForm.reason = ''
  cancelVisible.value = true
}

// 确认取消
const confirmCancel = async () => {
  if (!cancelForm.reason.trim()) {
    message.error('请输入取消原因')
    return
  }
  
  cancelLoading.value = true
  try {
    const res = await cancelTask(cancelForm.taskId, cancelForm.reason)
    if (res.code === 200) {
      message.success('任务已取消')
      cancelVisible.value = false
      loadTaskList()
      loadStats()
    } else {
      message.error(res.message || '取消失败')
    }
  } catch (error) {
    message.error('取消失败')
  } finally {
    cancelLoading.value = false
  }
}

// 删除任务
const handleDelete = (record: any) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个已取消的任务吗？此操作不可恢复。',
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await deleteTask(record.id)
        if (res.code === 200) {
          message.success('删除成功')
          loadTaskList()
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

// 获取任务类型颜色
const getTaskTypeColor = (type: number) => {
  const colors: Record<number, string> = {
    1: 'blue',
    2: 'green',
    3: 'orange',
    4: 'purple'
  }
  return colors[type] || 'default'
}

// 获取状态颜色
const getStatusColor = (status: number) => {
  const colors: Record<number, string> = {
    0: 'default',
    1: 'processing',
    2: 'processing',
    3: 'processing',
    4: 'warning',
    5: 'success',
    6: 'error'
  }
  return colors[status] || 'default'
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

onMounted(() => {
  loadStats()
  loadTaskList()
})
</script>

<style scoped>
.task-management {
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

/* 移动端卡片列表 */
.mobile-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-card {
  border-radius: 12px;
}

.task-card :deep(.ant-card-body) {
  padding: 16px;
}

.task-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.task-title-section {
  flex: 1;
  min-width: 0;
}

.task-type-tag {
  margin-bottom: 6px;
}

.task-title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.status-tag {
  flex-shrink: 0;
}

.task-card-body {
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
}

.info-value.price {
  color: #ff4d4f;
  font-weight: 600;
}

.task-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
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
  
  .task-card :deep(.ant-card-body) {
    padding: 12px;
  }
  
  .task-card-header {
    gap: 8px;
    margin-bottom: 10px;
    padding-bottom: 10px;
  }
  
  .task-title {
    font-size: 14px;
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
  
  .task-card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .status-tag {
    align-self: flex-start;
  }
}
</style>
