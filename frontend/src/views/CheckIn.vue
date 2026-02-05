<template>
  <div class="check-in-page">
    <a-card :bordered="false" class="checkin-card">
      <template #title>
        <div class="card-title-wrapper">
          <check-circle-outlined class="title-icon" />
          <span>座位签到</span>
        </div>
      </template>

      <!-- 无预约记录提示 -->
      <div v-if="!hasReservation && !isAdmin" class="no-reservation-alert">
        <a-result
          status="warning"
          title="暂无预约记录"
          sub-title="您当前没有进行中的座位预约，请先预约座位后再来签到"
        >
          <template #icon>
            <calendar-outlined style="color: #faad14; font-size: 72px;" />
          </template>
          <template #extra>
            <a-button type="primary" size="large" @click="goToReserve">
              <template #icon><search-outlined /></template>
              去预约座位
            </a-button>
            <a-button size="large" @click="goToHome">
              <template #icon><home-outlined /></template>
              返回首页
            </a-button>
          </template>
        </a-result>
      </div>

      <div v-else class="checkin-content">
        <!-- 步骤指示器 -->
        <div class="steps-indicator">
          <div class="step" :class="{ active: gpsStatus !== 'success', completed: gpsStatus === 'success' }">
            <div class="step-number">1</div>
            <div class="step-info">
              <div class="step-title">位置验证</div>
              <div class="step-desc">确认您在图书馆范围内</div>
            </div>
          </div>
          <div class="step-connector" :class="{ completed: gpsStatus === 'success' }"></div>
          <div class="step" :class="{ active: gpsStatus === 'success' }">
            <div class="step-number">2</div>
            <div class="step-info">
              <div class="step-title">扫码签到</div>
              <div class="step-desc">扫描座位二维码完成签到</div>
            </div>
          </div>
        </div>

        <!-- 定位状态卡片 -->
        <div class="location-card" :class="gpsStatus">
          <div class="location-status">
            <div class="status-icon-wrapper">
              <loading-outlined v-if="locating" class="spin-icon" />
              <environment-two-tone v-else-if="gpsStatus === 'success'" two-tone-color="#52c41a" class="status-icon success" />
              <environment-two-tone v-else-if="gpsStatus === 'error'" two-tone-color="#ff4d4f" class="status-icon error" />
              <environment-two-tone v-else two-tone-color="#1890ff" class="status-icon" />
            </div>
            <div class="status-info">
              <div class="status-title">{{ gpsMessage }}</div>
              <div v-if="coords" class="status-detail">
                <span class="coord-tag">经度 {{ coords.longitude.toFixed(6) }}</span>
                <span class="coord-tag">纬度 {{ coords.latitude.toFixed(6) }}</span>
              </div>
            </div>
          </div>
          <a-button
            v-if="gpsStatus !== 'success'"
            type="primary"
            :loading="locating"
            @click="getLocation"
            class="location-btn"
          >
            <template #icon><aim-outlined /></template>
            {{ locating ? '定位中...' : '开始定位' }}
          </a-button>
          <div v-else class="success-badge">
            <check-outlined />
            定位成功
          </div>
        </div>

        <!-- 管理员二维码生成器 -->
        <div v-if="isAdmin" class="admin-section">
          <a-divider orientation="left">
            <span class="divider-text">
              <setting-outlined />
              管理员测试工具
            </span>
          </a-divider>
          <div class="admin-controls">
            <a-select v-model:value="selectedArea" placeholder="选择区域" style="width: 160px">
              <a-select-option v-for="area in areas" :key="area" :value="area">{{ area }}</a-select-option>
            </a-select>
            <a-select v-model:value="selectedSeatNo" placeholder="选择座位" style="width: 160px" :disabled="!selectedArea">
              <a-select-option v-for="no in availableSeatsInArea" :key="no" :value="no">{{ no }}</a-select-option>
            </a-select>
          </div>
          <div v-if="generatedQrText" class="admin-qr-display">
            <div class="qr-card" draggable="true" @dragstart="(e) => e.dataTransfer?.setData('text', generatedQrText)">
              <qrcode-vue :value="generatedQrText" :size="140" level="H" />
              <p class="qr-text">{{ generatedQrText }}</p>
              <p class="qr-drag-hint">可拖拽到扫码区</p>
            </div>
            <a-button type="dashed" @click="simulateScan" :disabled="gpsStatus !== 'success'">
              <template #icon><scan-outlined /></template>
              模拟扫码签到
            </a-button>
          </div>
        </div>

        <!-- 扫码区域 -->
        <div class="scanner-section" :class="{ disabled: gpsStatus !== 'success' }">
          <div class="scanner-header-bar">
            <div class="scanner-title">
              <qrcode-outlined />
              二维码扫描
            </div>
            <a-tag v-if="gpsStatus !== 'success'" color="warning">请先完成定位</a-tag>
            <a-tag v-else color="success">准备就绪</a-tag>
          </div>

          <div class="scanner-wrapper" @dragover.prevent @drop.prevent="onDrop">
            <div v-if="gpsStatus !== 'success'" class="scanner-overlay">
              <div class="overlay-content">
                <lock-outlined class="overlay-icon" />
                <p>完成定位后即可扫码</p>
              </div>
            </div>
            <qr-scanner
              :allow-file="isAdmin"
              @scan="handleQrScan"
              @error="handleScanError"
              @cancel="handleScanCancel"
            />
          </div>
        </div>

        <!-- 签到提示 -->
        <div class="checkin-tips">
          <a-alert
            message="签到须知"
            description="请确保您已到达预约座位附近，定位成功后再扫描二维码完成签到。签到成功后系统将自动跳转至首页。"
            type="info"
            show-icon
          />
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  EnvironmentTwoTone,
  CheckCircleOutlined,
  LoadingOutlined,
  AimOutlined,
  CheckOutlined,
  SettingOutlined,
  ScanOutlined,
  QrcodeOutlined,
  LockOutlined,
  CalendarOutlined,
  SearchOutlined,
  HomeOutlined
} from '@ant-design/icons-vue'
import QrcodeVue from 'qrcode.vue'
import QrScanner from '../components/CheckIn/QrScanner.vue'
import { checkIn } from '../api/reservation'
import { getSeats, type Seat } from '../api/seat'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userInfo.value?.role === 'admin')
const reservation = computed(() => userStore.reservation)

// 检查是否有预约记录
const hasReservation = computed(() => {
  return !!reservation.value && reservation.value.id
})

// 跳转到预约页面
const goToReserve = () => {
  router.push('/seat')
}

// 跳转到首页
const goToHome = () => {
  router.push('/dashboard')
}

// Admin QR Generation Logic
const allSeats = ref<Seat[]>([])
const areas = computed(() => [...new Set(allSeats.value.map(s => s.area))])
const selectedArea = ref<string>('')
const selectedSeatNo = ref<string>('')
const availableSeatsInArea = computed(() =>
  allSeats.value.filter(s => s.area === selectedArea.value).map(s => s.seatNo)
)

const generatedQrText = computed(() => {
  if (selectedArea.value && selectedSeatNo.value) {
    return `Area:${selectedArea.value},SeatNo:${selectedSeatNo.value}`
  }
  return ''
})

onMounted(async () => {
  try {
    const data = await getSeats()
    allSeats.value = data as any
  } catch (e) {
    console.error('Failed to fetch seats', e)
  }
})

watch(selectedArea, () => {
  selectedSeatNo.value = ''
})

const simulateScan = () => {
  if (generatedQrText.value) {
    handleQrScan(generatedQrText.value)
  }
}

const onDrop = (e: DragEvent) => {
  const text = e.dataTransfer?.getData('text')
  if (text) {
    handleQrScan(text)
  }
}

const handleScanError = (errorMsg: string) => {
  message.error(errorMsg)
}

const handleScanCancel = () => {
  // 用户取消扫码，可以在这里添加额外的处理逻辑
  console.log('用户取消扫码')
}

// QR Code Logic
const handleQrScan = async (text: string) => {
  if (!reservation.value) {
    message.warning('您当前没有活跃的预约记录')
    return
  }

  if (gpsStatus.value !== 'success' || !coords.value) {
    message.error('签到失败：请先完成定位')
    return
  }

  try {
    await checkIn(reservation.value.id, {
      qrCode: text,
      lat: coords.value.latitude,
      lng: coords.value.longitude
    })
    message.success('扫码签到成功！')
    userStore.checkIn()
    router.push('/dashboard')
  } catch (e: any) {
    message.error(e.response?.data?.msg || '签到失败')
  }
}

// GPS Logic
const locating = ref(false)
const gpsStatus = ref<'idle' | 'success' | 'error'>('idle')
const gpsMessage = ref('等待定位...')
const coords = ref<GeolocationCoordinates | null>(null)

const getLocation = () => {
  if (!navigator.geolocation) {
    message.error('您的浏览器不支持地理定位')
    gpsStatus.value = 'error'
    gpsMessage.value = '浏览器不支持定位'
    return
  }

  locating.value = true
  gpsStatus.value = 'idle'
  gpsMessage.value = '正在获取位置...'

  navigator.geolocation.getCurrentPosition(
    (position) => {
      coords.value = position.coords
      locating.value = false
      gpsStatus.value = 'success'
      gpsMessage.value = '定位成功，已在图书馆范围内'
    },
    (error) => {
      locating.value = false
      gpsStatus.value = 'error'
      gpsMessage.value = '定位失败，请检查定位权限'
      message.error('定位失败：' + error.message)
    },
    { enableHighAccuracy: true, timeout: 10000 }
  )
}
</script>

<style scoped>
.check-in-page {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.checkin-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 无预约记录提示 */
.no-reservation-alert {
  padding: 48px 24px;
}

.no-reservation-alert :deep(.ant-result-extra) {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
}

.title-icon {
  color: #1890ff;
  font-size: 24px;
}

.checkin-content {
  padding: 8px 0;
}

/* 步骤指示器 */
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  padding: 0 24px;
}

.step {
  display: flex;
  align-items: center;
  gap: 12px;
  opacity: 0.5;
  transition: all 0.3s;
}

.step.active {
  opacity: 1;
}

.step.completed {
  opacity: 1;
}

.step.completed .step-number {
  background: #52c41a;
  border-color: #52c41a;
  color: white;
}

.step-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s;
}

.step.active .step-number {
  border-color: #1890ff;
  color: #1890ff;
}

.step-info {
  text-align: left;
}

.step-title {
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
}

.step-desc {
  font-size: 12px;
  color: #6b7280;
}

.step-connector {
  flex: 1;
  max-width: 80px;
  height: 2px;
  background: #e5e7eb;
  margin: 0 16px;
  transition: all 0.3s;
}

.step-connector.completed {
  background: #52c41a;
}

/* 定位状态卡片 */
.location-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 24px;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.location-card.success {
  background: #f6ffed;
  border-color: #b7eb8f;
}

.location-card.error {
  background: #fff2f0;
  border-color: #ffccc7;
}

.location-status {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.status-icon {
  font-size: 24px;
}

.status-icon.success {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.spin-icon {
  font-size: 24px;
  color: #1890ff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.status-title {
  font-weight: 600;
  font-size: 16px;
  color: #1f2937;
  margin-bottom: 4px;
}

.status-detail {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.coord-tag {
  font-size: 12px;
  color: #6b7280;
  background: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.location-btn {
  border-radius: 8px;
}

.success-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #52c41a;
  font-weight: 600;
  padding: 8px 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.15);
}

/* 管理员区域 */
.admin-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
}

.divider-text {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 14px;
}

.admin-controls {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.admin-qr-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.qr-card {
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
  cursor: grab;
  transition: all 0.3s;
}

.qr-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.qr-text {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #6b7280;
  font-family: monospace;
}

.qr-drag-hint {
  margin: 8px 0 0 0;
  font-size: 11px;
  color: #1890ff;
}

/* 扫码区域 */
.scanner-section {
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.scanner-section.disabled {
  opacity: 0.7;
}

.scanner-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e5e7eb;
}

.scanner-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1f2937;
}

.scanner-wrapper {
  position: relative;
  padding: 24px;
  display: flex;
  justify-content: center;
  background: white;
}

.scanner-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.overlay-content {
  text-align: center;
  color: #6b7280;
}

.overlay-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #d9d9d9;
}

/* 签到提示 */
.checkin-tips {
  margin-top: 24px;
}

/* 响应式 */
@media (max-width: 640px) {
  .check-in-page {
    padding: 16px;
  }

  .steps-indicator {
    flex-direction: column;
    gap: 16px;
  }

  .step-connector {
    width: 2px;
    height: 24px;
    max-width: none;
  }

  .location-card {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .location-status {
    flex-direction: column;
  }

  .admin-controls {
    flex-direction: column;
  }

  .admin-controls .ant-select {
    width: 100% !important;
  }
}
</style>
