<template>
  <div class="wallet-management">
    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stat-cards">
      <a-col :span="6">
        <a-card>
          <Statistic title="总交易笔数" :value="stats.totalCount" :value-style="{ color: '#3f8600' }">
            <template #prefix>
              <TransactionOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <Statistic title="总收入" :value="stats.totalIncome" :precision="2" :value-style="{ color: '#52c41a' }">
            <template #prefix>
              <ArrowDownOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <Statistic title="总支出" :value="stats.totalExpense" :precision="2" :value-style="{ color: '#ff4d4f' }">
            <template #prefix>
              <ArrowUpOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <Statistic title="今日交易" :value="stats.todayCount || 0" :value-style="{ color: '#722ed1' }">
            <template #prefix>
              <CalendarOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 交易类型统计 -->
    <a-row :gutter="16" class="type-stats">
      <a-col :span="24">
        <a-card title="交易类型统计">
          <a-row :gutter="16">
            <a-col :span="4" v-for="(stat, type) in stats.typeStats" :key="type">
              <div class="type-stat-item">
                <div class="type-name">{{ type }}</div>
                <div class="type-count">{{ stat.count }}笔</div>
                <div class="type-amount">¥{{ stat.amount?.toFixed(2) }}</div>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!-- 搜索筛选 -->
    <a-card class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="交易类型">
          <a-select v-model:value="searchForm.transactionType" placeholder="全部类型" style="width: 140px" allowClear>
            <a-select-option :value="1">充值</a-select-option>
            <a-select-option :value="2">提现</a-select-option>
            <a-select-option :value="3">任务支付</a-select-option>
            <a-select-option :value="4">任务收入</a-select-option>
            <a-select-option :value="5">退款</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="交易方向">
          <a-select v-model:value="searchForm.direction" placeholder="全部方向" style="width: 120px" allowClear>
            <a-select-option :value="1">收入</a-select-option>
            <a-select-option :value="2">支出</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker v-model:value="searchForm.dateRange" show-time format="YYYY-MM-DD HH:mm:ss" />
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

    <!-- 交易流水列表 -->
    <a-card title="交易流水">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import {
  TransactionOutlined,
  ArrowDownOutlined,
  ArrowUpOutlined,
  CalendarOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'
import { getTransactions, getWalletStats } from '@/api/wallet'

const loading = ref(false)
const transactionList = ref([])
const stats = ref<any>({})

// 搜索表单
const searchForm = reactive<{
  transactionType: number | undefined
  direction: number | undefined
  dateRange: Dayjs[] | undefined
}>({
  transactionType: undefined,
  direction: undefined,
  dateRange: undefined
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

    // 处理时间范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0].format('YYYY-MM-DD HH:mm:ss')
      params.endTime = searchForm.dateRange[1].format('YYYY-MM-DD HH:mm:ss')
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
  searchForm.dateRange = undefined
  pagination.current = 1
  loadTransactions()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
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

.stat-cards {
  margin-bottom: 24px;
}

.type-stats {
  margin-bottom: 24px;
}

.type-stat-item {
  text-align: center;
  padding: 16px;
  background: #f6ffed;
  border-radius: 8px;
}

.type-stat-item:nth-child(2) {
  background: #e6f7ff;
}

.type-stat-item:nth-child(3) {
  background: #fff2f0;
}

.type-stat-item:nth-child(4) {
  background: #f6ffed;
}

.type-stat-item:nth-child(5) {
  background: #f9f0ff;
}

.type-name {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.type-count {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.type-amount {
  font-size: 14px;
  color: #ff4d4f;
}

.search-card {
  margin-bottom: 24px;
}

.user-info {
  display: flex;
  flex-direction: column;
}
</style>
