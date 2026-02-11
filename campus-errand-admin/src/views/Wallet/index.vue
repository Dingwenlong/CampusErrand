<template>
  <div class="wallet-management">
    <!-- 统计卡片 - 响应式 -->
    <a-row :gutter="[16, 16]" class="stat-cards">
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);">
            <TransactionOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">总交易笔数</div>
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);">
            <ArrowDownOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">总收入</div>
            <div class="stat-value">¥{{ (stats.totalIncome || 0).toFixed(2) }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);">
            <ArrowUpOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">总支出</div>
            <div class="stat-value">¥{{ (stats.totalExpense || 0).toFixed(2) }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);">
            <CalendarOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">今日交易</div>
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
            <a-form-item label="交易类型" class="form-item">
              <a-select v-model:value="searchForm.transactionType" placeholder="全部类型" allowClear>
                <a-select-option :value="1">充值</a-select-option>
                <a-select-option :value="2">提现</a-select-option>
                <a-select-option :value="3">任务支付</a-select-option>
                <a-select-option :value="4">任务收入</a-select-option>
                <a-select-option :value="5">退款</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
            <a-form-item label="交易方向" class="form-item">
              <a-select v-model:value="searchForm.direction" placeholder="全部方向" allowClear>
                <a-select-option :value="1">收入</a-select-option>
                <a-select-option :value="2">支出</a-select-option>
              </a-select>
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

    <!-- 交易流水列表 - 桌面端 -->
    <a-card class="table-card desktop-table" v-if="!isMobile">
      <a-table
        :columns="columns"
        :data-source="transactionList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'transactionType'">
            <a-tag :color="getTypeColor(record.transactionType)">
              {{ record.transactionTypeName }}
            </a-tag>
          </template>

          <template v-if="column.key === 'direction'">
            <a-tag :color="record.direction === 1 ? 'green' : 'red'">
              {{ record.directionName }}
            </a-tag>
          </template>

          <template v-if="column.key === 'amount'">
            <span :style="{ color: record.direction === 1 ? '#52c41a' : '#ff4d4f', fontWeight: 'bold' }">
              {{ record.direction === 1 ? '+' : '-' }}¥{{ record.amount }}
            </span>
          </template>

          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusName }}
            </a-tag>
          </template>

          <template v-if="column.key === 'user'">
            <div class="user-info">
              <span>{{ record.userName }}</span>
              <span style="color: #999; margin-left: 8px;">{{ record.userPhone }}</span>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 移动端卡片列表 -->
    <div class="mobile-list" v-else>
      <a-card v-for="item in transactionList" :key="item.id" class="transaction-card">
        <div class="transaction-header">
          <a-tag :color="getTypeColor(item.transactionType)">{{ item.transactionTypeName }}</a-tag>
          <span :style="{ color: item.direction === 1 ? '#52c41a' : '#ff4d4f', fontWeight: 'bold' }">
            {{ item.direction === 1 ? '+' : '-' }}¥{{ item.amount }}
          </span>
        </div>
        <div class="transaction-body">
          <div class="info-row">
            <span class="info-label">用户:</span>
            <span class="info-value">{{ item.userName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">方向:</span>
            <a-tag :color="item.direction === 1 ? 'green' : 'red'" size="small">
              {{ item.directionName }}
            </a-tag>
          </div>
          <div class="info-row">
            <span class="info-label">状态:</span>
            <a-tag :color="getStatusColor(item.status)" size="small">{{ item.statusName }}</a-tag>
          </div>
          <div class="info-row">
            <span class="info-label">时间:</span>
            <span class="info-value">{{ item.createTime }}</span>
          </div>
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
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  TransactionOutlined,
  ArrowDownOutlined,
  ArrowUpOutlined,
  CalendarOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'
import { getTransactions, getWalletStats } from '@/api/wallet'
import { useResponsive } from '@/composables/useResponsive'

const { isMobile } = useResponsive()
const loading = ref(false)
const transactionList = ref([])
const stats = ref<any>({})

// 搜索表单
const searchForm = reactive<{
  transactionType: number | undefined
  direction: number | undefined
}>({
  transactionType: undefined,
  direction: undefined
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
  { title: '交易编号', dataIndex: 'transactionNo', key: 'transactionNo', width: 200 },
  { title: '用户', key: 'user', width: 180 },
  { title: '交易类型', key: 'transactionType', width: 120 },
  { title: '方向', key: 'direction', width: 100 },
  { title: '金额', key: 'amount', width: 120, align: 'right' as const },
  { title: '余额', dataIndex: 'balance', key: 'balance', width: 120, align: 'right' as const },
  { title: '状态', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
  { title: '交易时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getWalletStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

// 加载交易流水
const loadTransactions = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.pageSize,
      transactionType: searchForm.transactionType,
      direction: searchForm.direction
    }

    const res = await getTransactions(params)
    if (res.code === 200) {
      transactionList.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载交易流水失败', error)
    message.error('加载交易流水失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadTransactions()
}

// 重置
const handleReset = () => {
  searchForm.transactionType = undefined
  searchForm.direction = undefined
  pagination.current = 1
  loadTransactions()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadTransactions()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.current = page
  loadTransactions()
}

// 获取类型颜色
const getTypeColor = (type: number) => {
  const colors: Record<number, string> = {
    1: 'blue',
    2: 'orange',
    3: 'red',
    4: 'green',
    5: 'purple'
  }
  return colors[type] || 'default'
}

// 获取状态颜色
const getStatusColor = (status: number) => {
  const colors: Record<number, string> = {
    0: 'processing',
    1: 'success',
    2: 'error'
  }
  return colors[status] || 'default'
}

onMounted(() => {
  loadStats()
  loadTransactions()
})
</script>

<style scoped>
.wallet-management {
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
  font-size: 18px;
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
  flex-direction: column;
}

/* 移动端卡片列表 */
.mobile-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transaction-card {
  border-radius: 12px;
}

.transaction-card :deep(.ant-card-body) {
  padding: 16px;
}

.transaction-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.transaction-body {
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
  min-width: 50px;
}

.info-value {
  font-size: 13px;
  color: #333;
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
    font-size: 15px;
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
