<template>
  <div class="user-management">
    <!-- 搜索栏 -->
    <a-card class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="关键词">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="用户名/手机号"
            allow-clear
          />
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

    <!-- 数据表格 -->
    <a-card class="table-card">
      <a-table
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatar'">
            <a-avatar :src="record.avatar" :size="40" />
          </template>
          <template v-if="column.key === 'gender'">
            <span>{{ record.gender === 1 ? '男' : record.gender === 2 ? '女' : '未知' }}</span>
          </template>
          <template v-if="column.key === 'creditScore'">
            <a-tag :color="getCreditColor(record.creditScore)">
              {{ record.creditScore }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                查看
              </a-button>
              <a-button
                type="link"
                size="small"
                :danger="record.status === 1"
                @click="handleToggleStatus(record)"
              >
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { getUserList, updateUserStatus } from '@/api/user'
import type { User } from '@/types'

const loading = ref(false)
const userList = ref<User[]>([])

const searchForm = reactive({
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
  { title: '头像', key: 'avatar', width: 80 },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname' },
  { title: '性别', key: 'gender', width: 80 },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '信用分', key: 'creditScore', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '注册时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const getCreditColor = (score: number) => {
  if (score >= 90) return 'green'
  if (score >= 70) return 'blue'
  if (score >= 50) return 'orange'
  return 'red'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: searchForm.keyword
    })
    if (res.code === 200) {
      userList.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  pagination.current = 1
  loadData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const handleView = (record: User) => {
  message.info(`查看用户: ${record.nickname}`)
}

const handleToggleStatus = (record: User) => {
  const newStatus = record.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  
  Modal.confirm({
    title: `确认${actionText}`,
    content: `确定要${actionText}用户 "${record.nickname}" 吗？`,
    onOk: async () => {
      try {
        const res = await updateUserStatus(record.id, newStatus)
        if (res.code === 200) {
          message.success(`${actionText}成功`)
          loadData()
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-management {
  padding: 0;
}

.search-card {
  margin-bottom: 24px;
}

.table-card {
  margin-bottom: 24px;
}
</style>
