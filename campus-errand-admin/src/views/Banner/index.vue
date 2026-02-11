<template>
  <div class="banner-management">
    <a-card class="banner-card">
      <template #title>
        <div class="card-header">
          <span class="title">轮播图管理</span>
          <a-button type="primary" @click="showAddModal">
            <PlusOutlined />
            新增轮播图
          </a-button>
        </div>
      </template>

      <a-table
        :columns="columns"
        :data-source="bannerList"
        :loading="loading"
        row-key="id"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'preview'">
            <div
              class="banner-preview"
              :style="{ background: record.bgColor || 'linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%)' }"
            >
              <div class="preview-content">
                <div class="preview-title">{{ record.title }}</div>
                <div class="preview-desc">{{ record.content }}</div>
              </div>
            </div>
          </template>

          <template v-if="column.key === 'bgColor'">
            <div class="color-cell">
              <div class="color-block" :style="{ background: record.bgColor }"></div>
              <span class="color-value">{{ record.bgColor || '默认' }}</span>
            </div>
          </template>

          <template v-if="column.key === 'sortOrder'">
            <a-input-number
              v-model:value="record.sortOrder"
              :min="0"
              @change="(val) => handleSortChange(record, val)"
              style="width: 80px"
            />
          </template>

          <template v-if="column.key === 'status'">
            <a-switch
              :checked="record.status === 1"
              @change="(checked) => handleStatusChange(record, checked)"
              checked-children="启用"
              un-checked-children="禁用"
            />
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showEditModal(record)">
                <EditOutlined />
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这个轮播图吗？"
                @confirm="handleDelete(record)"
                ok-text="确定"
                cancel-text="取消"
              >
                <a-button type="link" danger size="small">
                  <DeleteOutlined />
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-empty v-if="!loading && bannerList.length === 0" description="暂无轮播图数据" />
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      :confirmLoading="modalLoading"
      width="600px"
    >
      <a-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        layout="vertical"
        class="banner-form"
      >
        <a-form-item label="标题" name="title">
          <a-input
            v-model:value="formData.title"
            placeholder="请输入轮播图标题"
            maxlength="20"
            show-count
          />
        </a-form-item>

        <a-form-item label="内容描述" name="content">
          <a-textarea
            v-model:value="formData.content"
            placeholder="请输入内容描述"
            :rows="2"
            maxlength="50"
            show-count
          />
        </a-form-item>

        <a-form-item label="背景色" name="bgColor">
          <div class="color-picker-wrapper">
            <a-input
              v-model:value="formData.bgColor"
              placeholder="请输入渐变色值，如：linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%)"
            />
            <div class="preset-colors">
              <span class="preset-label">预设颜色：</span>
              <div
                v-for="color in presetColors"
                :key="color"
                class="preset-color"
                :style="{ background: color }"
                @click="formData.bgColor = color"
              ></div>
            </div>
          </div>
        </a-form-item>

        <a-form-item label="预览">
          <div
            class="banner-preview-large"
            :style="{ background: formData.bgColor || 'linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%)' }"
          >
            <div class="preview-content">
              <div class="preview-title">{{ formData.title || '标题预览' }}</div>
              <div class="preview-desc">{{ formData.content || '内容描述预览' }}</div>
            </div>
          </div>
        </a-form-item>

        <a-form-item label="排序" name="sortOrder">
          <a-input-number
            v-model:value="formData.sortOrder"
            :min="0"
            style="width: 100%"
            placeholder="数字越小排序越靠前"
          />
        </a-form-item>

        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { getBannerList, addBanner, updateBanner, deleteBanner, updateBannerStatus } from '@/api/banner'

const loading = ref(false)
const bannerList = ref<any[]>([])
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()

const presetColors = [
  'linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%)',
  'linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%)',
  'linear-gradient(135deg, #667eea 0%, #8B5CF6 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #30cfd0 0%, #330867 100%)'
]

const columns = [
  {
    title: '预览',
    key: 'preview',
    width: 200
  },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    width: 150
  },
  {
    title: '内容',
    dataIndex: 'content',
    key: 'content',
    ellipsis: true
  },
  {
    title: '背景色',
    key: 'bgColor',
    width: 180
  },
  {
    title: '排序',
    key: 'sortOrder',
    width: 100
  },
  {
    title: '状态',
    key: 'status',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 150
  }
]

const formData = reactive({
  id: null as number | null,
  title: '',
  content: '',
  bgColor: '',
  sortOrder: 0,
  status: 1
})

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容描述', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序值', trigger: 'blur' }]
}

const fetchBannerList = async () => {
  loading.value = true
  try {
    const res = await getBannerList()
    if (res.code === 200) {
      bannerList.value = res.data || []
    }
  } catch (error) {
    console.error('获取轮播图列表失败', error)
    message.error('获取轮播图列表失败')
  } finally {
    loading.value = false
  }
}

const showAddModal = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

const showEditModal = (record: any) => {
  isEdit.value = true
  Object.assign(formData, {
    id: record.id,
    title: record.title,
    content: record.content,
    bgColor: record.bgColor,
    sortOrder: record.sortOrder,
    status: record.status
  })
  modalVisible.value = true
}

const resetForm = () => {
  formData.id = null
  formData.title = ''
  formData.content = ''
  formData.bgColor = ''
  formData.sortOrder = 0
  formData.status = 1
  formRef.value?.resetFields()
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()
    modalLoading.value = true

    const data = {
      title: formData.title,
      content: formData.content,
      bgColor: formData.bgColor,
      sortOrder: formData.sortOrder,
      status: formData.status
    }

    if (isEdit.value && formData.id) {
      const res = await updateBanner(formData.id, data)
      if (res.code === 200) {
        message.success('更新成功')
      }
    } else {
      const res = await addBanner(data)
      if (res.code === 200) {
        message.success('添加成功')
      }
    }

    modalVisible.value = false
    fetchBannerList()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    modalLoading.value = false
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetForm()
}

const handleDelete = async (record: any) => {
  try {
    const res = await deleteBanner(record.id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchBannerList()
    }
  } catch (error) {
    console.error('删除失败', error)
    message.error('删除失败')
  }
}

const handleStatusChange = async (record: any, checked: boolean) => {
  try {
    const status = checked ? 1 : 0
    const res = await updateBannerStatus(record.id, status)
    if (res.code === 200) {
      message.success('状态更新成功')
      record.status = status
    }
  } catch (error) {
    console.error('状态更新失败', error)
    message.error('状态更新失败')
  }
}

const handleSortChange = async (record: any, value: number) => {
  try {
    const data = {
      title: record.title,
      content: record.content,
      bgColor: record.bgColor,
      sortOrder: value,
      status: record.status
    }
    const res = await updateBanner(record.id, data)
    if (res.code === 200) {
      message.success('排序更新成功')
    }
  } catch (error) {
    console.error('排序更新失败', error)
    message.error('排序更新失败')
  }
}

onMounted(() => {
  fetchBannerList()
})
</script>

<style scoped>
.banner-management {
  padding: 0;
}

.banner-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.banner-preview {
  width: 160px;
  height: 80px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  overflow: hidden;
}

.preview-content {
  text-align: center;
  padding: 8px;
}

.preview-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.preview-desc {
  font-size: 11px;
  opacity: 0.9;
}

.color-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-block {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.color-value {
  font-size: 12px;
  color: #666;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.banner-form {
  padding: 16px 0;
}

.color-picker-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preset-colors {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.preset-label {
  font-size: 13px;
  color: #666;
}

.preset-color {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.preset-color:hover {
  border-color: #FFC300;
  transform: scale(1.1);
}

.banner-preview-large {
  width: 100%;
  height: 120px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.banner-preview-large .preview-title {
  font-size: 20px;
  margin-bottom: 8px;
}

.banner-preview-large .preview-desc {
  font-size: 14px;
}

:deep(.ant-table-cell) {
  vertical-align: middle;
}
</style>
