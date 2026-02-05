<template>
  <div class="p-6">    
    <a-card :bordered="false" title="座位管理">
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">新增座位</a-button>
          <a-button @click="showImportModal">批量导入</a-button>
          <a-button danger @click="handleClearAll">清空所有</a-button>
          <a-button 
            danger 
            type="dashed"
            :disabled="!selectedRowKeys.length" 
            @click="handleBatchDelete"
          >
            批量删除
          </a-button>
        </a-space>
      </template>

      <!-- 搜索栏 -->
      <a-form layout="inline" class="search-form">
        <a-form-item label="区域">
          <a-select
            v-model:value="searchForm.queryArea"
            placeholder="选择区域"
            style="width: 120px"
            allowClear
            @change="fetchSeats"
          >
            <a-select-option value="A区">A区</a-select-option>
            <a-select-option value="B区">B区</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="fetchSeats">查询</a-button>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="seats"
        :loading="loading"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        row-key="id"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number) => `共 ${total} 条`
        }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="handleEdit(record)">编辑</a>
              <a @click="handleShowQr(record)">二维码</a>
              <a-popconfirm
                title="确定删除该座位吗？"
                @confirm="handleDelete(record.id)"
              >
                <a class="text-red-500">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      :confirmLoading="submitting"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="座位号" name="seatNo">
          <a-input v-model:value="formData.seatNo" placeholder="如 A-01" />
        </a-form-item>
        <a-form-item label="区域" name="area">
          <a-select v-model:value="formData.area">
            <a-select-option value="A区">A区</a-select-option>
            <a-select-option value="B区">B区</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="类型" name="type">
          <a-select v-model:value="formData.type">
            <a-select-option value="标准">标准</a-select-option>
            <a-select-option value="靠窗">靠窗</a-select-option>
            <a-select-option value="插座">插座</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="formData.status">
            <a-select-option value="available">可用</a-select-option>
            <a-select-option value="occupied">占用</a-select-option>
            <a-select-option value="maintenance">维修中</a-select-option>
          </a-select>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="X坐标" name="x">
              <a-input-number v-model:value="formData.x" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Y坐标" name="y">
              <a-input-number v-model:value="formData.y" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 批量导入弹窗 -->
    <a-modal
      v-model:open="importVisible"
      title="批量导入座位"
      @ok="handleImport"
      :confirmLoading="importing"
      width="600px"
    >
      <template #footer>
        <a-button @click="importVisible = false">取消</a-button>
        <a-button type="dashed" @click="handleGenerateRandom">生成随机数据</a-button>
        <a-button type="primary" :loading="importing" @click="handleImport">确定导入</a-button>
      </template>
      <a-alert
        message="格式说明"
        description="请输入 JSON 数组格式，例如：[{'seatNo': 'A-01', 'area': 'A区', 'type': '标准', 'status': 'available'}, ...]"
        type="info"
        show-icon
        class="mb-4"
      />
      <a-textarea
        v-model:value="importData"
        :rows="10"
        placeholder="在此粘贴 JSON 数据"
      />
    </a-modal>

    <!-- 二维码预览弹窗 -->
    <a-modal
      v-model:open="qrVisible"
      title="座位二维码"
      :footer="null"
      width="300px"
      centered
    >
      <div class="flex flex-col items-center p-4">
        <div class="bg-white p-4 rounded shadow-sm mb-4 border border-gray-100">
          <qrcode-vue :value="qrValue" :size="200" level="H" />
        </div>
        <div class="text-center">
          <p class="text-lg font-bold mb-1">{{ currentSeat?.seatNo }}</p>
          <p class="text-gray-500">{{ currentSeat?.area }} · {{ currentSeat?.type }}</p>
          <a-button type="primary" class="mt-4" @click="downloadQr">下载二维码</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import QrcodeVue from 'qrcode.vue'
import { wsService } from '../../utils/websocket'
import { 
  getSeats, 
  addSeat, 
  updateSeat, 
  deleteSeat, 
  batchDeleteSeats, 
  batchImportSeats,
  deleteAllSeats,
  type Seat 
} from '../../api/seat'

// 列表相关
const loading = ref(false)
const seats = ref<Seat[]>([])
const searchForm = reactive({
  queryArea: undefined as string | undefined
})
const selectedRowKeys = ref<number[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const handleSeatUpdate = (data: { id: number, status: string }) => {
  const seat = seats.value.find(s => s.id === data.id)
  if (seat) {
    seat.status = data.status as any
  }
}

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '座位号', dataIndex: 'seatNo', key: 'seatNo' },
  { title: '区域', dataIndex: 'area', key: 'area' },
  { title: '类型', dataIndex: 'type', key: 'type' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action', width: 150 }
]

const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    available: 'success',
    occupied: 'warning',
    maintenance: 'error'
  }
  return map[status] || 'default'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    available: '空闲',
    occupied: '占用',
    maintenance: '维修中'
  }
  return map[status] || status
}

// 弹窗相关
const modalVisible = ref(false)
const modalTitle = ref('新增座位')
const submitting = ref(false)
const formRef = ref()
const formData = reactive<Partial<Seat>>({
  seatNo: '',
  area: 'A区',
  type: '标准',
  status: 'available',
  x: 0,
  y: 0
})

const rules = {
  seatNo: [{ required: true, message: '请输入座位号' }],
  area: [{ required: true, message: '请选择区域' }],
  type: [{ required: true, message: '请选择类型' }],
  status: [{ required: true, message: '请选择状态' }]
}

// 导入相关
const importVisible = ref(false)
const importing = ref(false)
const importData = ref('')

// 二维码相关
const qrVisible = ref(false)
const currentSeat = ref<Seat | null>(null)
const qrValue = computed(() => {
  if (!currentSeat.value) return ''
  // 匹配后端 ReservationService.java L349 的格式: "Area:A区,SeatNo:A-01"
  return `Area:${currentSeat.value.area},SeatNo:${currentSeat.value.seatNo}`
})

const handleShowQr = (record: Seat) => {
  currentSeat.value = record
  qrVisible.value = true
}

const downloadQr = () => {
  const canvas = document.querySelector('.flex.flex-col.items-center.p-4 canvas') as HTMLCanvasElement
  if (canvas) {
    const url = canvas.toDataURL('image/png')
    const link = document.createElement('a')
    link.download = `座位二维码_${currentSeat.value?.seatNo}.png`
    link.href = url
    link.click()
    message.success('二维码已开始下载')
  }
}

const handleGenerateRandom = () => {
  const areas = ['A区', 'B区', 'C区', 'D区']
  const types = ['标准', '靠窗', '插座']
  const randomData: any[] = []
  const count = 20 // 默认生成20个
  
  // 获取当前时间戳尾数确保座位号不重复
  const timestamp = Date.now().toString().slice(-4)
  
  // 使用网格布局确保间距不低于 100
  // 每行放 5 个，间距为 120 (x) 和 100 (y)
  const colCount = 5
  
  for (let i = 0; i < count; i++) {
    const randomArea = areas[Math.floor(Math.random() * areas.length)] || 'A区'
    const seatNo = `${randomArea.charAt(0)}-${timestamp}${i.toString().padStart(2, '0')}`
    
    const row = Math.floor(i / colCount)
    const col = i % colCount
    
    randomData.push({
      seatNo,
      area: randomArea,
      type: types[Math.floor(Math.random() * types.length)],
      status: 'available',
      // X 间距 120，Y 间距 100，起始偏移 50
      x: 50 + col * 120,
      y: 50 + row * 100
    })
  }
  
  importData.value = JSON.stringify(randomData, null, 2)
  message.success(`已生成 ${count} 条符合间距要求(>=100)的随机数据`)
}

// Methods
const fetchSeats = async () => {
  loading.value = true
  try {
    const params = {
      area: searchForm.queryArea || undefined,
      page: pagination.current,
      size: pagination.pageSize
    }
    const res = await getSeats(params)
    // 兼容分页返回和直接数组返回
    if (res && (res as any).records) {
      seats.value = (res as any).records
      pagination.total = (res as any).total || (res as any).records.length
    } else if (Array.isArray(res)) {
      seats.value = res
      pagination.total = res.length
    } else {
      seats.value = []
      pagination.total = 0
    }
  } finally {
    loading.value = false
  }
}

const handleTableChange = (paginationConfig: any) => {
  pagination.current = paginationConfig.current
  pagination.pageSize = paginationConfig.pageSize
  fetchSeats()
}

const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const handleAdd = () => {
  modalTitle.value = '新增座位'
  Object.assign(formData, {
    id: undefined,
    seatNo: '',
    area: 'A区',
    type: '标准',
    status: 'available',
    x: 0,
    y: 0
  })
  modalVisible.value = true
}

const handleEdit = (record: Seat) => {
  modalTitle.value = '编辑座位'
  Object.assign(formData, record)
  modalVisible.value = true
}

const handleModalOk = async () => {
  await formRef.value.validate()
  if (formData.id) {
    await updateSeat(formData)
    message.success('修改成功')
  } else {
    await addSeat(formData)
    message.success('新增成功')
  }
  modalVisible.value = false
  fetchSeats()
}

const handleDelete = async (id: number) => {
  await deleteSeat(id)
  message.success('删除成功')
  fetchSeats()
}

const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个座位吗？`,
    onOk: async () => {
      await batchDeleteSeats(selectedRowKeys.value)
      message.success('批量删除成功')
      selectedRowKeys.value = []
      fetchSeats()
    }
  })
}

const handleClearAll = () => {
  Modal.confirm({
    title: '确认清空所有座位',
    content: '此操作将删除系统中所有座位及其关联的活跃预约，操作不可撤销，确定继续吗？',
    okText: '确定清空',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteAllSeats()
        message.success('所有座位已成功清空')
        fetchSeats()
      } catch (error) {
        message.error('清空失败')
      }
    }
  })
}

const showImportModal = () => {
  importData.value = ''
  importVisible.value = true
}

const handleImport = async () => {
  if (!importData.value) return
  try {
    const data = JSON.parse(importData.value)
    if (!Array.isArray(data)) {
      message.error('数据格式错误，必须是 JSON 数组')
      return
    }
    importing.value = true
    await batchImportSeats(data)
    message.success('批量导入成功')
    importVisible.value = false
    fetchSeats()
  } catch (error) {
    message.error('JSON 解析失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  fetchSeats()
  wsService.connect()
  wsService.on('seat_update', handleSeatUpdate)
})

onUnmounted(() => {
  wsService.off('seat_update', handleSeatUpdate)
  // Remove disconnect() here to prevent killing the global connection in SPA
})
</script>

<style scoped>
.text-red-500 {
  color: #ff4d4f;
}
.flex {
  display: flex;
}
.flex-col {
  flex-direction: column;
}
.items-center {
  align-items: center;
}
.justify-center {
  justify-content: center;
}
.p-4 {
  padding: 1rem;
}
.mb-1 {
  margin-bottom: 0.25rem;
}
.mb-4 {
  margin-bottom: 1rem;
}
.mt-4 {
  margin-top: 1rem;
}
.text-lg {
  font-size: 1.125rem;
}
.font-bold {
  font-weight: 700;
}
.text-gray-500 {
  color: #8c8c8c;
}
.rounded {
  border-radius: 4px;
}
.shadow-sm {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}
.border {
  border-width: 1px;
}
.border-gray-100 {
  border-color: #f0f0f0;
}
.bg-white {
  background-color: #fff;
}
</style>
