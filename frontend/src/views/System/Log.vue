<template>
  <div class="log-container">
    <a-card :bordered="false">
      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="操作人">
          <a-input v-model:value="searchForm.username" placeholder="请输入操作人账号" allow-clear />
        </a-form-item>
        <a-form-item label="操作类型">
          <a-input v-model:value="searchForm.operation" placeholder="请输入操作类型" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><search-outlined /></template>
              查询
            </a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="logList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, text }">
          <template v-if="column.key === 'createTime'">
            {{ formatTime(text) }}
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import request from '../../utils/request'
import dayjs from 'dayjs'

const loading = ref(false)
const logList = ref([])
const searchForm = reactive({
  username: '',
  operation: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '操作人', dataIndex: 'username', key: 'username', width: 120 },
  { title: '操作类型', dataIndex: 'operation', key: 'operation', width: 150 },
  { title: '详情内容', dataIndex: 'content', key: 'content' },
  { title: '操作时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await request<any>({
      url: '/logs/list',
      method: 'get',
      params: {
        page: pagination.current,
        size: pagination.pageSize,
        username: searchForm.username,
        operation: searchForm.operation
      }
    })
    const data = res.data || res
    logList.value = data.records
    pagination.total = data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchLogs()
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.operation = ''
  handleSearch()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchLogs()
}

const formatTime = (time: string) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.log-container {
  padding: 0;
}
</style>
