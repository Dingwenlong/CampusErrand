<template>
  <div v-if="timeLeft > 0" class="timer-container" :class="{ 'mobile': isMobile, 'away': reservation?.status === 'away' }">
    <a-popover placement="bottomRight" trigger="click">
      <template #content>
        <div class="timer-actions">
          <a-button type="primary" size="small" @click="handleCheckIn" v-if="reservation?.status === 'reserved'">
            签到
          </a-button>
          <a-button size="small" @click="handleLeave" v-if="reservation?.status !== 'away'">
            暂离
          </a-button>
          <a-button danger size="small" @click="handleRelease">
            退座
          </a-button>
        </div>
      </template>
      <div class="timer-content">
        <clock-circle-outlined class="timer-icon" spin />
        <span class="seat-info" v-if="!isMobile">
          {{ reservation?.seatNo }}
          <span v-if="reservation?.status === 'away'">(暂离)</span>
        </span>
        <span class="time-text">
          {{ formatTime(timeLeft) }}
        </span>
      </div>
    </a-popover>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ClockCircleOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '../../stores/user'
import { message, Modal } from 'ant-design-vue'
import { checkIn, temporaryLeave, releaseSeat } from '../../api/reservation'

const userStore = useUserStore()
const reservation = computed(() => userStore.reservation)
const timeLeft = ref(0)
const isMobile = ref(false)
const loading = ref(false)
let timer: number | null = null

const formatTime = (ms: number) => {
  const totalSeconds = Math.floor(ms / 1000)
  const m = Math.floor(totalSeconds / 60)
  const s = totalSeconds % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const updateTimer = () => {
  if (!reservation.value) {
    timeLeft.value = 0
    return
  }

  const now = Date.now()
  const diff = reservation.value.deadline - now

  if (diff <= 0) {
    timeLeft.value = 0
    // Don't auto clear here, wait for backend push or manual refresh to avoid race conditions
    // userStore.clearReservation() 
  } else {
    timeLeft.value = diff
  }
}

const handleCheckIn = async () => {
  if (!reservation.value?.id) return
  loading.value = true
  try {
    await checkIn(reservation.value.id, {})
    userStore.checkIn()
    message.success('签到成功')
  } catch (e) {
    // message.error('签到失败') // Interceptor already handles error
  } finally {
    loading.value = false
  }
}

const handleLeave = async () => {
  if (!reservation.value?.id) return
  loading.value = true
  try {
    await temporaryLeave(reservation.value.id)
    userStore.setAway()
    message.success('已设置为暂离状态，请在30分钟内返回')
  } catch (e) {
    // message.error('操作失败')
  } finally {
    loading.value = false
  }
}

const handleRelease = async () => {
  if (!reservation.value?.id) return

  const doRelease = async () => {
    loading.value = true
    try {
      await releaseSeat(reservation.value!.id)
      userStore.clearReservation()
      message.success('已释放座位')
    } catch (e) {
      // Error handled by interceptor
    } finally {
      loading.value = false
    }
  }

  // 检查是否在起始时间前15分钟内
  if (reservation.value.status === 'reserved') {
    const now = Date.now()
    const startTime = reservation.value.startTime
    const windowMs = 15 * 60 * 1000

    if (now > startTime - windowMs) {
      Modal.confirm({
        title: '违规取消提醒',
        content: '当前已进入预约起始时间前15分钟，此时取消将被视为违约并扣除10信用分。确定要取消吗？',
        okText: '确定取消',
        okType: 'danger',
        cancelText: '暂不取消',
        onOk: doRelease
      })
      return
    }
  }

  doRelease()
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  
  // 立即执行一次
  updateTimer()
  
  // 启动定时器
  timer = window.setInterval(updateTimer, 1000)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.timer-container {
  display: inline-flex;
  align-items: center;
  margin-right: 16px;
  background: #fff1f0;
  border: 1px solid #ffccc7;
  padding: 0 12px;
  height: 32px; /* 固定高度，确保垂直居中 */
  border-radius: 16px;
  color: #cf1322;
  font-weight: 500;
  transition: all 0.3s;
  box-sizing: border-box;
  cursor: pointer;
}

.timer-container:hover {
  background: #ffccc7;
}

.timer-container.away {
  background: #fff7e6;
  border-color: #ffe58f;
  color: #faad14;
}

.timer-container.away:hover {
  background: #ffe58f;
}

.timer-content {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 100%;
}

.seat-info {
  font-size: 13px;
  opacity: 0.85;
  line-height: 1;
}

.time-text {
  font-family: monospace;
  font-size: 14px;
  font-weight: bold;
  line-height: 1;
}

.timer-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Mobile Adaptation */
.timer-container.mobile {
  padding: 0 8px;
  margin-right: 8px;
  height: 28px; /* 移动端稍微调小高度 */
}

.timer-container.mobile .time-text {
  font-size: 12px;
}
</style>
