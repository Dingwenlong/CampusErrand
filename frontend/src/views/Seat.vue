<template>
  <div class="seat-container">
    <a-card :bordered="false" title="座位预约">
      <template #extra>
        <a-radio-group v-model:value="viewMode" button-style="solid">
          <a-radio-button value="map">
            <template #icon><compass-outlined /></template>
            平面图
          </a-radio-button>
          <a-radio-button value="list">
            <template #icon><bars-outlined /></template>
            列表
          </a-radio-button>
          <a-radio-button value="grid">
            <template #icon><appstore-outlined /></template>
            卡片
          </a-radio-button>
        </a-radio-group>
      </template>

      <!-- 搜索区域 -->
      <a-form layout="inline" class="search-form">
        <a-form-item label="区域">
          <a-select v-model:value="areaFilter" style="width: 120px" placeholder="选择区域">
            <a-select-option value="all">全部</a-select-option>
            <a-select-option v-for="area in availableAreas" :key="area" :value="area">
              {{ area }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="座位号">
          <a-input
            v-model:value="searchText"
            placeholder="请输入座位号"
            allow-clear
            style="width: 150px"
          >
            <template #prefix>
              <search-outlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="状态">
          <a-select v-model:value="statusFilter" style="width: 120px" placeholder="选择状态">
            <a-select-option value="all">全部</a-select-option>
            <a-select-option value="available">空闲</a-select-option>
            <a-select-option value="occupied">占用</a-select-option>
            <a-select-option value="maintenance">维护中</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="类型">
          <a-select v-model:value="typeFilter" style="width: 120px" placeholder="选择类型">
            <a-select-option value="all">全部</a-select-option>
            <a-select-option value="standard">普通座</a-select-option>
            <a-select-option value="window">靠窗座</a-select-option>
            <a-select-option value="sofa">沙发座</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" @click="onSearch">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>

      <div class="seat-content mt-4">
        <!-- Map View -->
        <div v-if="viewMode === 'map'" class="seat-map-view">
          <seat-map :seats="mapSeats" @select="handleMapSelect" />
        </div>

        <!-- Grid View -->
        <div v-else-if="viewMode === 'grid'" class="seat-grid">
          <div v-for="(groupSeats, area) in groupedSeats" :key="area" class="area-section">
            <h2 class="area-title">{{ area }}</h2>
            <a-row :gutter="[16, 16]">
              <a-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4" v-for="seat in groupSeats" :key="seat.id">
                <a-card hoverable class="seat-card" :class="seat.status">
                  <div class="seat-icon">
                    <component :is="getSeatIcon(seat.status)" />
                  </div>
                  <div class="seat-info">
                    <h3>{{ seat.seatNo }}</h3>
                    <a-tag :color="getStatusColor(seat.status)">{{ getStatusText(seat.status) }}</a-tag>
                    <!-- 3段式状态条 -->
                    <div class="slot-bar">
                      <div 
                        v-for="slot in ['morning', 'afternoon', 'evening']" 
                        :key="slot"
                        class="slot-dot"
                        :style="{ backgroundColor: getSlotColor(seat, slot) }"
                        v-tooltip="getSlotTooltip(slot)"
                      ></div>
                    </div>
                  </div>
                  <template #actions>
                    <span v-if="seat.status === 'available'" @click="handleBook(seat)">预约</span>
                    <span v-else style="cursor: not-allowed; color: #ccc">不可用</span>
                  </template>
                </a-card>
              </a-col>
            </a-row>
          </div>
        </div>

        <!-- List View -->
        <a-table
          v-else-if="viewMode === 'list'"
          :columns="columns"
          :data-source="filteredSeats"
          :pagination="{ pageSize: 10, simple: isMobile }"
          :scroll="{ x: 600 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button
                type="link"
                :disabled="record.status !== 'available'"
                @click="handleBook(record)"
              >
                预约
              </a-button>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>

    <a-modal
      v-model:open="isModalVisible"
      title="座位预约"
      @ok="confirmBooking"
      :confirmLoading="bookingLoading"
    >
      <a-form layout="vertical">
        <a-form-item label="当前选择">
          <a-tag color="blue">{{ selectedSeat?.seatNo }}</a-tag>
          <a-tag>{{ selectedSeat?.type }}</a-tag>
        </a-form-item>
        
        <a-form-item label="预约时段" required>
          <a-checkbox-group v-model:value="bookingForm.slots">
            <a-checkbox value="morning" :disabled="isSlotDisabled('morning')">上午 (08:00-12:00)</a-checkbox>
            <a-checkbox value="afternoon" :disabled="isSlotDisabled('afternoon')">下午 (13:00-17:00)</a-checkbox>
            <a-checkbox value="evening" :disabled="isSlotDisabled('evening')">晚间 (18:00-22:00)</a-checkbox>
          </a-checkbox-group>
          <p v-if="allSlotsDisabled" class="text-error mt-2">今日预约已结束</p>
        </a-form-item>

        <a-alert
          message="预约规则"
          type="info"
          show-icon
        >
          <template #description>
            <ul style="padding-left: 20px; margin: 0;">
              <li>起始时间前后15分钟内必须完成签到</li>
              <li>如需取消，请在起始时间15分钟前办理</li>
              <li>违规取消或超时未签到将扣除10信用分</li>
              <li>违约多次将被限制预约权限</li>
            </ul>
          </template>
        </a-alert>
      </a-form>

      <template #footer>
        <div v-if="userInfo?.role === 'admin' || userInfo?.role === 'librarian'" class="admin-actions">
          <a-button danger @click="handleForceRelease" v-if="selectedSeat?.status !== 'available'">强制释放</a-button>
          <a-button danger type="dashed" @click="handleSetFaulty" v-if="selectedSeat?.status !== 'maintenance'">设为故障</a-button>
          <a-button @click="isModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmBooking" :loading="bookingLoading" v-if="selectedSeat?.status === 'available'">确定预约</a-button>
        </div>
        <div v-else>
          <a-button @click="isModalVisible = false">取消</a-button>
          <a-button type="primary" @click="confirmBooking" :loading="bookingLoading" :disabled="selectedSeat?.status !== 'available'">确定预约</a-button>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, reactive } from 'vue'
import {
  BarsOutlined,
  AppstoreOutlined,
  CompassOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  WarningOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import SeatMap, { type Seat as MapSeat } from '../components/Seat/SeatMap.vue'

const viewMode = ref('map')
const searchText = ref('')
const areaFilter = ref('all')
const statusFilter = ref('all')
const typeFilter = ref('all')

const availableAreas = computed(() => {
  const areas = new Set(seats.value.map(s => s.area))
  return Array.from(areas).filter(Boolean).sort()
})

const isModalVisible = ref(false)
const selectedSeat = ref<Seat | null>(null)
const isMobile = ref(false)
const bookingLoading = ref(false)
const bookingForm = reactive({
  slots: [] as string[]
})

const isSlotPast = (slot: string) => {
  const now = new Date()
  const currentHour = now.getHours()
  const currentMinute = now.getMinutes()
  const currentTime = currentHour * 60 + currentMinute

  // 设定时段结束时间
  if (slot === 'morning') return currentTime >= 12 * 60 // 12:00 以后禁用
  if (slot === 'afternoon') return currentTime >= 17 * 60 // 17:00 以后禁用
  if (slot === 'evening') return currentTime >= 22 * 60 // 22:00 以后禁用
  return false
}

const isSlotDisabled = (slot: string) => {
  // 如果该时段已被占用，也禁用
  const seat = selectedSeat.value as any
  if (seat?.slotStatuses?.[slot] && seat.slotStatuses[slot] !== 'available') {
    return true
  }

  return isSlotPast(slot)
}

const allSlotsDisabled = computed(() => {
  return isSlotDisabled('morning') && isSlotDisabled('afternoon') && isSlotDisabled('evening')
})

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

import { wsService } from '../utils/websocket'
import { useUserStore } from '../stores/user'
import { getSeats, updateSeatStatus, type Seat } from '../api/seat'
import { createReservation } from '../api/reservation'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const seats = ref<Seat[]>([])

const fetchSeats = async () => {
  try {
    const res = await getSeats()
    seats.value = Array.isArray(res) ? res : (res as any).data || []
  } catch (error) {
    console.error(error)
  }
}

const handleSeatUpdate = (data: { id: number, status: string }) => {
  const seat = seats.value.find(s => s.id === data.id)
  if (seat) {
    seat.status = data.status as any
  }
}

const handleReservationUpdate = (data: { event: string, reason: string }) => {
  if (data.event === 'reservation_ended') {
    // 如果当前选中的座位正是被释放的那个，清除选中状态
    if (selectedSeat.value && selectedSeat.value.status !== 'available') {
      // 可以在这里做一些 UI 上的反馈
    }
  }
}

onMounted(async () => {
  await userStore.syncReservationStatus()
  fetchSeats()
  checkMobile()
  window.addEventListener('resize', checkMobile)
  
  // 连接 WebSocket
  wsService.connect()
  wsService.on('seat_update', handleSeatUpdate)
  wsService.on('reservation_update', handleReservationUpdate)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  wsService.off('seat_update', handleSeatUpdate)
  wsService.off('reservation_update', handleReservationUpdate)
  // Remove disconnect() here to prevent killing the global connection in SPA
})

const getStatusColor = (status: string) => {
  switch (status) {
    case 'available': return 'success'
    case 'occupied': return 'error'
    case 'maintenance': return 'warning'
    default: return 'default'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'available': return '空闲'
    case 'occupied': return '占用'
    case 'maintenance': return '维护中'
    default: return '未知'
  }
}

const getSeatIcon = (status: string) => {
  switch (status) {
    case 'available': return CheckCircleOutlined
    case 'occupied': return CloseCircleOutlined
    case 'maintenance': return WarningOutlined
    default: return CheckCircleOutlined
  }
}

const getSlotColor = (seat: Seat, slot: string) => {
  if (isSlotPast(slot)) return '#bfbfbf' // 已过时段显示为灰色
  const s = seat as any
  const status = s.slotStatuses?.[slot] || 'available'
  switch (status) {
    case 'available': return '#10b981'
    case 'reserved': return '#3b82f6'
    case 'checked_in':
    case 'away':
    case 'occupied': return '#ef4444'
    case 'maintenance': return '#64748b'
    default: return '#e2e8f0'
  }
}

const getSlotTooltip = (slot: string) => {
  switch (slot) {
    case 'morning': return '上午'
    case 'afternoon': return '下午'
    case 'evening': return '晚间'
    default: return ''
  }
}

const mapSeats = computed<MapSeat[]>(() => {
  return filteredSeats.value
    .filter(seat => seat.id !== undefined)
    .map((seat, index) => {
      // 这里的 x, y 是后端存储的真实坐标，如果不存在则按网格排列显示
      const s = seat as any
      return {
        id: seat.id!,
        label: seat.seatNo,
        x: seat.x || 100 + (index % 6) * 100,
        y: seat.y || 100 + Math.floor(index / 6) * 80,
        status: seat.status,
        type: 'normal',
        slotStatuses: s.slotStatuses
      }
    })
})

const filteredSeats = computed(() => {
  return seats.value.filter(seat => {
    const matchArea = areaFilter.value === 'all' || seat.area === areaFilter.value
    const matchText = seat.seatNo.toLowerCase().includes(searchText.value.toLowerCase())
    const matchStatus = statusFilter.value === 'all' || seat.status === statusFilter.value
    const matchType = typeFilter.value === 'all' || seat.type === typeFilter.value
    return matchArea && matchText && matchStatus && matchType
  })
})

const groupedSeats = computed(() => {
  const groups: Record<string, Seat[]> = {}
  filteredSeats.value.forEach(seat => {
    const area = seat.area || '其他'
    if (!groups[area]) groups[area] = []
    groups[area].push(seat)
  })
  return groups
})

const handleBook = (seat: Seat) => {
  if (userStore.userInfo?.role === 'guest') {
    message.info('访客仅可查看座位状态，请登录后预约')
    return
  }
  selectedSeat.value = seat
  
  // 设置默认时段为第一个可用的时段
  bookingForm.slots = []
  if (!isSlotDisabled('morning')) {
    bookingForm.slots.push('morning')
  } else if (!isSlotDisabled('afternoon')) {
    bookingForm.slots.push('afternoon')
  } else if (!isSlotDisabled('evening')) {
    bookingForm.slots.push('evening')
  }
  
  isModalVisible.value = true
}

const handleMapSelect = (mapSeat: MapSeat) => {
  const seat = seats.value.find(s => s.id === mapSeat.id)
  if (seat) {
    handleBook(seat)
  }
}

const onSearch = () => {
  // Logic handled by computed property
}

const handleReset = () => {
  searchText.value = ''
  areaFilter.value = 'all'
  statusFilter.value = 'all'
  typeFilter.value = 'all'
}

const columns = [
  { title: '区域', dataIndex: 'area', key: 'area' },
  { title: '座位号', dataIndex: 'seatNo', key: 'seatNo' },
  { title: '类型', dataIndex: 'type', key: 'type' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

const handleForceRelease = async () => {
  if (selectedSeat.value) {
    try {
      await updateSeatStatus(selectedSeat.value.id!, 'available')
      selectedSeat.value.status = 'available'
      message.success('已强制释放该座位')
      isModalVisible.value = false
    } catch (error) {
      message.error('操作失败')
    }
  }
}

const handleSetFaulty = async () => {
  if (selectedSeat.value) {
    try {
      await updateSeatStatus(selectedSeat.value.id!, 'maintenance')
      selectedSeat.value.status = 'maintenance'
      message.warning('已将该座位设为故障状态')
      isModalVisible.value = false
    } catch (error) {
      message.error('操作失败')
    }
  }
}

const confirmBooking = async () => {
  if (!bookingForm.slots || bookingForm.slots.length === 0) {
    message.warning('请选择预约时段')
    return
  }

  bookingLoading.value = true
  try {
    if (selectedSeat.value) {
      // 逐个时段创建预约
      let firstRes: any = null
      for (const slot of bookingForm.slots) {
        const res = await createReservation({
          seatId: selectedSeat.value.id!,
          slot: slot
        })
        if (!firstRes) firstRes = res
      }

      const resData = (firstRes as any)?.data

      if (resData && resData.id) {
        // 更新座位状态 (如果是当前时段，则设为 occupied)
        const currentSlot = getCurrentSlot()
        if (bookingForm.slots.includes(currentSlot)) {
          selectedSeat.value.status = 'occupied'
        }

        // 触发全局倒计时 (针对第一个时段)
        userStore.setReservation(
          resData.id,
          selectedSeat.value.id!,
          selectedSeat.value.seatNo,
          new Date(resData.startTime).getTime(),
          new Date(resData.deadline).getTime()
        )
      }

      message.success(`预约成功！请在规定时间内签到。`)
      isModalVisible.value = false
      fetchSeats() // 刷新列表以获取最新的 slotStatuses
    }
  } catch (error) {
    message.error((error as any).response?.data?.msg || '预约失败')
  } finally {
    bookingLoading.value = false
  }
}

const getCurrentSlot = () => {
  const hour = new Date().getHours()
  if (hour >= 8 && hour < 12) return 'morning'
  if (hour >= 13 && hour < 17) return 'afternoon'
  if (hour >= 18 && hour < 22) return 'evening'
  return ''
}
</script>

<style scoped>
.seat-container {
  padding: 24px;
}

.mt-4 {
  margin-top: 16px;
}

.seat-grid {
  margin-top: 16px;
}

.area-section {
  margin-bottom: 32px;
}

.area-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 4px solid var(--color-primary);
  line-height: 1.2;
}

.seat-card {
  text-align: center;
  transition: all 0.3s;
}

.seat-card.available:hover {
  border-color: #52c41a;
  transform: translateY(-4px);
}

.seat-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.available .seat-icon { color: #52c41a; }
.occupied .seat-icon { color: #ff4d4f; }
.maintenance .seat-icon { color: #faad14; }

.seat-info h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.slot-bar {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin-top: 8px;
}

.slot-dot {
  width: 20px;
  height: 4px;
  border-radius: 2px;
  background-color: #e2e8f0;
}

.admin-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 16px;
}

.text-error {
  color: #ff4d4f;
  font-size: 12px;
}
</style>
