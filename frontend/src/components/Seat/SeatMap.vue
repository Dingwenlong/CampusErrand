<template>
  <div class="seat-map-container" ref="containerRef">
    <div class="map-controls">
      <a-space>
        <div class="legend-item">
          <span class="dot available"></span> 空闲
        </div>
        <div class="legend-item">
          <span class="dot occupied"></span> 占用
        </div>
        <div class="legend-item">
          <span class="dot selected"></span> 已选
        </div>
      </a-space>
    </div>

    <svg
      ref="svgRef"
      :width="width"
      :height="height"
      class="seat-svg"
      @mousedown="handleMouseDown"
      @mousemove="handleMouseMove"
      @mouseup="handleMouseUp"
      @wheel="handleWheel"
    >
      <g :transform="`translate(${transform.x}, ${transform.y}) scale(${transform.scale})`">
        <!-- 背景层：教室轮廓 -->
        <rect
          x="0"
          y="0"
          :width="dynamicMapSize.width"
          :height="dynamicMapSize.height"
          fill="#f8fafc"
          stroke="#e2e8f0"
          stroke-width="2"
        />

        <!-- 座位层 -->
        <g v-for="seat in seats" :key="seat.id">
          <!-- 胡桃木色桌台 -->
          <rect
            :x="seat.x"
            :y="seat.y - 15"
            :width="seatWidth"
            :height="12"
            :rx="2"
            fill="#5d4037"
            stroke="#3e2723"
            stroke-width="1"
            class="desk-rect"
          />
          <!-- 座位图标 -->
          <rect
            :x="seat.x"
            :y="seat.y"
            :width="seatWidth"
            :height="seatHeight"
            :rx="4"
            :fill="getSeatColor(seat)"
            :stroke="selectedSeatId === seat.id ? '#1e3a8a' : 'rgba(0,0,0,0.1)'"
            :stroke-width="selectedSeatId === seat.id ? 2 : 1"
            class="seat-rect"
            @click="handleSeatClick(seat)"
          />
          <!-- 座位号 -->
          <text
            :x="seat.x + seatWidth / 2"
            :y="seat.y + seatHeight / 2 + 5"
            text-anchor="middle"
            fill="#fff"
            font-size="12"
            font-weight="bold"
            pointer-events="none"
            class="seat-label"
          >{{ seat.label }}</text>

          <!-- 3段式时段状态条 -->
          <g :transform="`translate(${seat.x}, ${seat.y + seatHeight + 4})`">
            <rect
              v-for="(slot, index) in ['morning', 'afternoon', 'evening']"
              :key="slot"
              :x="index * (seatWidth / 3)"
              y="0"
              :width="seatWidth / 3 - 1"
              :height="4"
              :rx="1"
              :fill="getSlotColor(seat, slot)"
              class="slot-segment"
            >
              <title>{{ getSlotTooltip(slot) }}: {{ getStatusText(seat.slotStatuses?.[slot]) }}</title>
            </rect>
          </g>
        </g>
      </g>
    </svg>
    
    <div class="zoom-controls">
      <a-button-group>
        <a-button @click="zoomIn">
          <template #icon><plus-outlined /></template>
        </a-button>
        <a-button @click="resetZoom">
          <template #icon><reload-outlined /></template>
        </a-button>
        <a-button @click="zoomOut">
          <template #icon><minus-outlined /></template>
        </a-button>
      </a-button-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { PlusOutlined, MinusOutlined, ReloadOutlined } from '@ant-design/icons-vue'

export interface Seat {
  id: number
  label: string
  x: number
  y: number
  status: 'available' | 'occupied' | 'maintenance'
  type?: 'window' | 'power' | 'normal'
  slotStatuses?: Record<string, string>
}

const props = defineProps<{
  seats: Seat[]
}>()

const emit = defineEmits(['select'])

const width = ref(800)
const height = ref(600)
const seatWidth = 60
const seatHeight = 40

const dynamicMapSize = computed(() => {
  if (props.seats.length === 0) return { width: 800, height: 600 }
  
  const maxX = Math.max(...props.seats.map(s => s.x + seatWidth))
  const maxY = Math.max(...props.seats.map(s => s.y + seatHeight))
  
  // 留出 100px 的边距，且最小不低于 800x600
  return {
    width: Math.max(800, maxX + 100),
    height: Math.max(600, maxY + 100)
  }
})

const selectedSeatId = ref<number | null>(null)

const isSlotPast = (slot: string) => {
  const now = new Date()
  const currentHour = now.getHours()
  const currentMinute = now.getMinutes()
  const currentTime = currentHour * 60 + currentMinute

  // 设定时段结束时间
  if (slot === 'morning') return currentTime >= 12 * 60 // 12:00 以后
  if (slot === 'afternoon') return currentTime >= 17 * 60 // 17:00 以后
  if (slot === 'evening') return currentTime >= 22 * 60 // 22:00 以后
  return false
}

const transform = reactive({
  x: 0,
  y: 0,
  scale: 1
})

// 拖拽状态
let isDragging = false
let startX = 0
let startY = 0

const getSeatColor = (seat: Seat) => {
  if (seat.id === selectedSeatId.value) return '#2563eb' // 选中深蓝
  switch (seat.status) {
    case 'available': return '#059669' // 翠绿色
    case 'occupied': return '#dc2626' // 鲜红色
    case 'maintenance': return '#475569' // 深灰色
    default: return '#64748b'
  }
}

const getSlotColor = (seat: Seat, slot: string) => {
  if (isSlotPast(slot)) return '#bfbfbf' // 已过时段显示为灰色
  const status = seat.slotStatuses?.[slot] || 'available'
  switch (status) {
    case 'available': return '#10b981' // 绿色
    case 'reserved': return '#3b82f6' // 蓝色 (预约未签到)
    case 'checked_in':
    case 'away':
    case 'occupied': return '#ef4444' // 红色 (已签到/占用)
    case 'maintenance': return '#64748b' // 灰色
    default: return '#e2e8f0'
  }
}

const getSlotTooltip = (slot: string) => {
  switch (slot) {
    case 'morning': return '上午 (08:00-12:00)'
    case 'afternoon': return '下午 (13:00-17:00)'
    case 'evening': return '晚间 (18:00-22:00)'
    default: return ''
  }
}

const getStatusText = (status?: string) => {
  switch (status) {
    case 'available': return '空闲'
    case 'reserved': return '已预约'
    case 'checked_in': return '使用中'
    case 'away': return '暂离'
    case 'maintenance': return '维护中'
    default: return '空闲'
  }
}

const handleSeatClick = (seat: Seat) => {
  if (seat.status !== 'available') return
  selectedSeatId.value = seat.id
  emit('select', seat)
}

// 缩放/平移逻辑
const handleWheel = (e: WheelEvent) => {
  e.preventDefault()
  const scaleBy = 1.1
  const oldScale = transform.scale
  let newScale = e.deltaY > 0 ? oldScale / scaleBy : oldScale * scaleBy
  
  // 限制缩放范围
  newScale = Math.min(Math.max(0.5, newScale), 3)
  
  transform.scale = newScale
}

const handleMouseDown = (e: MouseEvent) => {
  isDragging = true
  startX = e.clientX - transform.x
  startY = e.clientY - transform.y
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging) return
  transform.x = e.clientX - startX
  transform.y = e.clientY - startY
}

const handleMouseUp = () => {
  isDragging = false
}

const zoomIn = () => {
  transform.scale = Math.min(transform.scale * 1.2, 3)
}

const zoomOut = () => {
  transform.scale = Math.max(transform.scale / 1.2, 0.5)
}

const resetZoom = () => {
  transform.scale = 1
  transform.x = 0
  transform.y = 0
}

onMounted(() => {
  // 自适应容器宽度（可选）
  const container = document.querySelector('.seat-map-container')
  if (container) {
    width.value = container.clientWidth
    height.value = 500
  }
})
</script>

<style scoped>
.seat-map-container {
  width: 100%;
  height: 500px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #e2e8f0;
}

.map-controls {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  padding: 8px 12px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.zoom-controls {
  position: absolute;
  bottom: 16px;
  right: 16px;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #64748b;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
}

.dot.available { background: #059669; }
.dot.occupied { background: #dc2626; }
.dot.selected { background: #2563eb; }

.seat-rect {
  cursor: pointer;
  transition: fill 0.2s;
}

.seat-rect:hover {
  filter: brightness(0.9);
}

.desk-rect {
  pointer-events: none;
  filter: drop-shadow(0 1px 1px rgba(0,0,0,0.2));
}

.seat-label {
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  user-select: none;
}
</style>
