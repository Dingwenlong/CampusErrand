<template>
  <div class="page-container">
    <a-card :bordered="false" title="用户管理">
      <template #extra>
        <a-button type="primary" @click="handleAdd">
          <template #icon><plus-outlined /></template>
          新增用户
        </a-button>
      </template>

      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allow-clear />
        </a-form-item>
        <a-form-item label="姓名">
          <a-input v-model:value="searchForm.realName" placeholder="请输入姓名" allow-clear />
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
        :data-source="filteredUsers"
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        row-key="id"
        class="mt-4"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'role'">
            <a-tag :color="getRoleColor(record.role)">
              {{ getRoleName(record.role) }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'status'">
            <a-badge :status="record.status === 'active' ? 'success' : 'error'" />
            {{ record.status === 'active' ? '正常' : '封禁' }}
          </template>

          <template v-if="column.key === 'creditScore'">
            <span :class="{ 'text-danger': record.creditScore < 60 }">
              {{ record.creditScore }}
            </span>
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a @click="handleEdit(record)">编辑</a>
              <a-divider type="vertical" />
              <a-popconfirm
                v-if="record.status === 'active'"
                title="确定要封禁该用户吗？"
                @confirm="handleStatusChange(record, 'banned')"
              >
                <a class="text-danger">封禁</a>
              </a-popconfirm>
              <a-popconfirm
                v-else
                title="确定要解封该用户吗？"
                @confirm="handleStatusChange(record, 'active')"
              >
                <a class="text-success">解封</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑/新增弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      :confirmLoading="modalLoading"
    >
      <a-form :model="modalForm" layout="vertical" ref="modalFormRef">
        <a-form-item label="用户名" name="username" required>
          <a-input v-model:value="modalForm.username" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="真实姓名" name="realName" required>
          <a-input v-model:value="modalForm.realName" />
        </a-form-item>
        <a-form-item label="角色" name="role" required>
          <a-select v-model:value="modalForm.role">
            <a-select-option value="student">学生</a-select-option>
            <a-select-option value="librarian">图书管理员</a-select-option>
            <a-select-option value="admin">系统管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="modalForm.phone" />
        </a-form-item>
        <a-form-item label="信用分" name="creditScore" v-if="isEdit">
          <a-input-number v-model:value="modalForm.creditScore" :min="0" :max="100" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getUsers, createUser, updateUser, updateUserStatus, type UserManage } from '../../api/user'

const loading = ref(false)
const users = ref<UserManage[]>([])
const searchForm = reactive({
  username: '',
  realName: ''
})

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username' },
  { title: '姓名', dataIndex: 'realName' },
  { title: '角色', key: 'role' },
  { title: '信用分', key: 'creditScore', sorter: (a: UserManage, b: UserManage) => a.creditScore - b.creditScore },
  { title: '手机号', dataIndex: 'phone' },
  { title: '最后登录', dataIndex: 'lastLoginTime' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 150 }
]

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUsers()
    // 兼容分页返回和直接数组返回
    if (res && (res as any).records) {
      users.value = (res as any).records
    } else if (Array.isArray(res)) {
      users.value = res
    } else {
      users.value = []
    }
  } finally {
    loading.value = false
  }
}

// 前端搜索过滤
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const matchUsername = !searchForm.username || user.username.includes(searchForm.username)
    const matchRealName = !searchForm.realName || (user.realName && user.realName.includes(searchForm.realName))
    return matchUsername && matchRealName
  })
})

const handleSearch = () => {
  // 实际项目中应调用后端接口，这里仅触发 computed 更新
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.realName = ''
}

// 角色样式
const getRoleColor = (role: string) => {
  switch (role) {
    case 'admin': return 'purple'
    case 'librarian': return 'cyan'
    default: return 'blue'
  }
}

const getRoleName = (role: string) => {
  switch (role) {
    case 'admin': return '管理员'
    case 'librarian': return '图书管理员'
    default: return '学生'
  }
}

// 模态框逻辑
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const modalTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')
const modalForm = reactive<Partial<UserManage>>({})

const handleAdd = () => {
  isEdit.value = false
  Object.assign(modalForm, {
    username: '',
    realName: '',
    role: 'student',
    phone: '',
    creditScore: 100
  })
  modalVisible.value = true
}

const handleEdit = (record: UserManage) => {
  isEdit.value = true
  Object.assign(modalForm, { ...record })
  modalVisible.value = true
}

const handleModalOk = async () => {
  modalLoading.value = true
  try {
    if (!isEdit.value) {
      await createUser(modalForm)
      message.success('新增成功')
    } else {
      if (modalForm.id) {
        await updateUser(modalForm.id, modalForm)
        message.success('更新成功')
      }
    }
    modalVisible.value = false
    fetchData() // Refresh list
  } catch (error) {
    message.error('操作失败')
  } finally {
    modalLoading.value = false
  }
}

const handleStatusChange = async (record: UserManage, status: 'active' | 'banned') => {
  try {
    await updateUserStatus(record.id, status)
    record.status = status
    message.success(status === 'active' ? '用户已解封' : '用户已封禁')
  } catch (error) {
    message.error('操作失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}
.mt-4 {
  margin-top: 16px;
}
.text-danger {
  color: #ff4d4f;
}
.text-success {
  color: #52c41a;
}
</style>
