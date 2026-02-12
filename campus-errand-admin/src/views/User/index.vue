<template>
  <div class="user-management">
    <!-- 搜索栏 -->
    <a-card class="search-card">
      <a-form :model="searchForm" class="search-form">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="24" :md="12" :lg="8" :xl="6">
            <a-form-item label="关键词" class="form-item">
              <a-input
                v-model:value="searchForm.keyword"
                placeholder="用户名/手机号"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="8" :xl="6">
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

    <!-- 数据表格 - 桌面端 -->
    <a-card class="table-card desktop-table" v-if="!isMobile">
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

    <!-- 卡片列表 - 移动端 -->
    <div class="mobile-list" v-else>
      <a-card v-for="user in userList" :key="user.id" class="user-card">
        <div class="user-card-header">
          <a-avatar :src="user.avatar" :size="48" />
          <div class="user-info">
            <div class="user-name">{{ user.nickname || '未设置昵称' }}</div>
            <div class="user-phone">{{ user.phone || '未绑定手机' }}</div>
          </div>
          <a-tag :color="user.status === 1 ? 'green' : 'red'" class="status-tag">
            {{ user.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </div>
        <div class="user-card-body">
          <div class="info-row">
            <span class="info-label">性别:</span>
            <span class="info-value">{{ user.gender === 1 ? '男' : user.gender === 2 ? '女' : '未知' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">信用分:</span>
            <a-tag :color="getCreditColor(user.creditScore)">{{ user.creditScore }}</a-tag>
          </div>
          <div class="info-row">
            <span class="info-label">注册时间:</span>
            <span class="info-value">{{ user.createTime }}</span>
          </div>
        </div>
        <div class="user-card-footer">
          <a-button type="link" size="small" @click="handleView(user)">
            查看
          </a-button>
          <a-button
            type="link"
            size="small"
            :danger="user.status === 1"
            @click="handleToggleStatus(user)"
          >
            {{ user.status === 1 ? '禁用' : '启用' }}
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

    <!-- 用户详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="用户详情"
      width="800px"
      :footer="null"
      class="user-detail-modal"
    >
      <a-spin :spinning="detailLoading">
        <template v-if="currentUser">
          <a-descriptions :column="isMobile ? 1 : 2" bordered>
            <a-descriptions-item label="用户ID">{{ currentUser.id }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="currentUser.status === 1 ? 'green' : 'red'">
                {{ currentUser.status === 1 ? '正常' : '禁用' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="头像" :span="isMobile ? 1 : 2">
              <a-avatar :src="currentUser.avatar" :size="64" />
            </a-descriptions-item>
            <a-descriptions-item label="昵称">{{ currentUser.nickname || '未设置' }}</a-descriptions-item>
            <a-descriptions-item label="手机号">{{ currentUser.phone || '未绑定' }}</a-descriptions-item>
            <a-descriptions-item label="性别">{{ formatGender(currentUser.gender) }}</a-descriptions-item>
            <a-descriptions-item label="用户类型">{{ formatUserType(currentUser.userType) }}</a-descriptions-item>
            <a-descriptions-item label="信用分">
              <a-tag :color="getCreditColor(currentUser.creditScore)">
                {{ currentUser.creditScore }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="实名认证">
              <a-tag :color="currentUser.isVerified === 1 ? 'green' : 'default'">
                {{ currentUser.isVerified === 1 ? '已认证' : '未认证' }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>

          <a-divider>认证信息</a-divider>
          <a-descriptions :column="isMobile ? 1 : 2" bordered>
            <a-descriptions-item label="真实姓名">{{ currentUser.realName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="身份证号">{{ currentUser.idCard || '-' }}</a-descriptions-item>
            <a-descriptions-item label="学号">{{ currentUser.studentId || '-' }}</a-descriptions-item>
            <a-descriptions-item label="学校">{{ currentUser.schoolName || '-' }}</a-descriptions-item>
          </a-descriptions>

          <a-divider>订单统计</a-divider>
          <a-descriptions :column="isMobile ? 1 : 3" bordered>
            <a-descriptions-item label="总订单数">
              <span class="stat-number">{{ currentUser.totalOrders || 0 }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="完成订单">
              <span class="stat-number success">{{ currentUser.completedOrders || 0 }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="取消订单">
              <span class="stat-number danger">{{ currentUser.cancelledOrders || 0 }}</span>
            </a-descriptions-item>
          </a-descriptions>

          <a-divider>钱包信息</a-divider>
          <a-descriptions :column="isMobile ? 1 : 2" bordered>
            <a-descriptions-item label="账户余额">
              <span class="amount">¥{{ formatAmount(currentUser.balance) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="冻结金额">
              <span class="amount frozen">¥{{ formatAmount(currentUser.frozenAmount) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="累计收入">
              <span class="amount income">¥{{ formatAmount(currentUser.totalIncome) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="累计支出">
              <span class="amount expense">¥{{ formatAmount(currentUser.totalExpense) }}</span>
            </a-descriptions-item>
          </a-descriptions>

          <a-divider>其他信息</a-divider>
          <a-descriptions :column="isMobile ? 1 : 2" bordered>
            <a-descriptions-item label="注册时间">{{ currentUser.createTime || '-' }}</a-descriptions-item>
            <a-descriptions-item label="更新时间">{{ currentUser.updateTime || '-' }}</a-descriptions-item>
            <a-descriptions-item label="OpenID" :span="isMobile ? 1 : 2">
              <span class="openid-text">{{ currentUser.openid || '-' }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </template>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { getUserList, getUserDetail, updateUserStatus } from '@/api/user'
import { useResponsive } from '@/composables/useResponsive'
import type { User, UserDetail } from '@/types'

const { isMobile } = useResponsive()
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

const detailVisible = ref(false)
const detailLoading = ref(false)
const currentUser = ref<UserDetail | null>(null)

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

const handlePageChange = (page: number) => {
  pagination.current = page
  loadData()
}

const handleView = async (record: User) => {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const res = await getUserDetail(record.id)
    if (res.code === 200) {
      currentUser.value = res.data
    } else {
      message.error(res.message || '获取用户详情失败')
    }
  } catch (error) {
    console.error(error)
    message.error('获取用户详情失败')
  } finally {
    detailLoading.value = false
  }
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

const formatGender = (gender: number) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

const formatUserType = (userType: number) => {
  if (userType === 1) return '普通用户'
  if (userType === 2) return '跑腿员'
  return '未知'
}

const formatAmount = (amount: number | undefined) => {
  if (amount === undefined || amount === null) return '0.00'
  return amount.toFixed(2)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-management {
  padding: 0;
  animation: fade-up 0.32s ease;
}

.search-card {
  margin-bottom: 20px;
}

.search-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.table-card {
  margin-bottom: 20px;
}

.desktop-table :deep(.ant-table-row) {
  transition: background-color 0.22s ease;
}

/* 移动端卡片列表 */
.mobile-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-card {
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px -24px rgba(15, 23, 42, 0.48);
}

.user-card :deep(.ant-card-body) {
  padding: 16px;
}

.user-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-phone {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
}

.status-tag {
  flex-shrink: 0;
}

.user-card-body {
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
  color: #64748b;
  min-width: 60px;
}

.info-value {
  font-size: 13px;
  color: #1e293b;
}

.user-card-footer {
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
  padding: 14px;
  background: rgba(255, 255, 255, 0.84);
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

/* 响应式适配 */
@media (max-width: 768px) {
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
  .user-card :deep(.ant-card-body) {
    padding: 12px;
  }
  
  .user-card-header {
    gap: 10px;
    margin-bottom: 10px;
    padding-bottom: 10px;
  }
  
  .user-name {
    font-size: 15px;
  }
  
  .user-phone {
    font-size: 12px;
  }
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

/* 用户详情弹窗样式 */
.user-detail-modal :deep(.ant-descriptions) {
  margin-bottom: 0;
}

.user-detail-modal :deep(.ant-divider) {
  margin: 16px 0;
}

.stat-number {
  font-size: 18px;
  font-weight: 700;
  color: #1890ff;
}

.stat-number.success {
  color: #52c41a;
}

.stat-number.danger {
  color: #ff4d4f;
}

.amount {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.amount.frozen {
  color: #faad14;
}

.amount.income {
  color: #52c41a;
}

.amount.expense {
  color: #ff4d4f;
}

.openid-text {
  font-family: monospace;
  font-size: 12px;
  color: #64748b;
  word-break: break-all;
}
</style>
