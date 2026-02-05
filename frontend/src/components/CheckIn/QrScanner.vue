<template>
  <div class="qr-scanner-wrapper">
    <!-- 准备扫码状态 - 默认显示 -->
    <div v-if="scannerState === 'ready'" class="ready-card">
      <div class="ready-icon">
        <qrcode-outlined />
      </div>
      <h3 class="ready-title">准备扫码签到</h3>
      <p class="ready-desc">
        请确保您已到达预约座位附近<br>
        点击按钮开始扫描二维码
      </p>
      <a-button type="primary" size="large" @click="startScanning">
        <template #icon><scan-outlined /></template>
        开始扫码
      </a-button>
    </div>

    <!-- 摄像头权限状态提示 -->
    <div v-else-if="scannerState === 'permission_prompt'" class="permission-request-card">
      <div class="permission-icon">
        <video-camera-outlined />
      </div>
      <h3 class="permission-title">需要摄像头权限</h3>
      <p class="permission-desc">请允许使用摄像头以扫描座位二维码完成签到</p>
      <div class="permission-actions">
        <a-button @click="cancelScanning">取消</a-button>
        <a-button type="primary" @click="requestCameraPermission">
          <template #icon><camera-outlined /></template>
          允许使用摄像头
        </a-button>
      </div>
    </div>

    <!-- 权限被拒绝提示 -->
    <div v-else-if="scannerState === 'permission_denied'" class="permission-denied-card">
      <div class="permission-icon denied">
        <stop-outlined />
      </div>
      <h3 class="permission-title">摄像头权限被拒绝</h3>
      <p class="permission-desc">
        您已拒绝摄像头权限，无法使用扫码功能。<br>
        请在浏览器设置中允许摄像头权限后刷新页面。
      </p>
      <div class="permission-actions">
        <a-button @click="cancelScanning">返回</a-button>
        <a-button type="primary" @click="refreshPage">
          <template #icon><reload-outlined /></template>
          刷新页面重试
        </a-button>
      </div>
    </div>

    <!-- 加载中状态 -->
    <div v-else-if="scannerState === 'loading'" class="loading-card">
      <a-spin size="large" />
      <p class="loading-text">正在启动摄像头...</p>
    </div>

    <!-- 扫码区域 -->
    <div v-else-if="scannerState === 'scanning'" class="scanner-active">
      <div class="scanner-header">
        <div class="scanner-frame">
          <div class="corner top-left"></div>
          <div class="corner top-right"></div>
          <div class="corner bottom-left"></div>
          <div class="corner bottom-right"></div>
          <div class="scan-line"></div>
        </div>
        <div id="reader" class="reader-element"></div>
      </div>
      <div class="scanner-footer">
        <p class="scanner-hint">
          <scan-outlined />
          将二维码放入框内即可自动扫描
        </p>
        <a-button type="link" size="small" @click="stopScanning">
          <close-outlined />
          停止扫码
        </a-button>
      </div>
    </div>

    <!-- 扫码成功状态 -->
    <div v-else-if="scannerState === 'success'" class="success-card">
      <div class="success-icon">
        <check-circle-outlined />
      </div>
      <h3 class="success-title">扫码成功</h3>
      <p class="success-desc">正在处理签到...</p>
    </div>

    <!-- 错误提示 -->
    <div v-if="error && scannerState !== 'permission_denied'" class="error-message">
      <close-circle-outlined />
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'
import {
  VideoCameraOutlined,
  CameraOutlined,
  StopOutlined,
  ReloadOutlined,
  ScanOutlined,
  CloseCircleOutlined,
  QrcodeOutlined,
  CheckCircleOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'

type ScannerState = 'ready' | 'permission_prompt' | 'permission_denied' | 'loading' | 'scanning' | 'success'

const props = defineProps<{
  allowFile?: boolean
}>()

const emit = defineEmits(['scan', 'error', 'cancel'])

const error = ref('')
const scannerState = ref<ScannerState>('ready')
let scanner: Html5Qrcode | null = null

// 开始扫码流程
const startScanning = async () => {
  error.value = ''

  // 先检查权限状态
  try {
    if (!navigator.permissions || !navigator.permissions.query) {
      // 浏览器不支持 permissions API，直接尝试请求
      scannerState.value = 'permission_prompt'
      return
    }

    const result = await navigator.permissions.query({ name: 'camera' as PermissionName })

    if (result.state === 'granted') {
      // 已有权限，直接启动
      await initScanner()
    } else if (result.state === 'denied') {
      scannerState.value = 'permission_denied'
    } else {
      // prompt 状态，显示权限请求界面
      scannerState.value = 'permission_prompt'
    }
  } catch (e) {
    // 某些浏览器可能不支持 camera 权限查询，显示权限请求界面
    scannerState.value = 'permission_prompt'
  }
}

// 请求摄像头权限
const requestCameraPermission = async () => {
  scannerState.value = 'loading'
  try {
    // 检查浏览器是否支持摄像头
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      throw new Error('浏览器不支持摄像头功能')
    }

    // 测试获取权限，但不停止流，让 Html5Qrcode 来管理摄像头
    const stream = await navigator.mediaDevices.getUserMedia({ video: true })

    // 立即停止测试流，释放摄像头让 Html5Qrcode 使用
    stream.getTracks().forEach(track => track.stop())

    // 短暂延迟确保摄像头完全释放
    await new Promise(resolve => setTimeout(resolve, 300))

    await initScanner()
  } catch (e: any) {
    console.error('Camera permission error:', e)
    scannerState.value = 'permission_denied'
    if (e.name === 'NotAllowedError' || e.name === 'PermissionDeniedError') {
      error.value = '摄像头权限被拒绝，请在浏览器设置中允许访问摄像头'
    } else if (e.name === 'NotFoundError') {
      error.value = '未找到摄像头设备，请检查摄像头是否连接'
    } else if (e.name === 'NotReadableError') {
      error.value = '摄像头被其他应用占用，请关闭其他使用摄像头的应用'
    } else {
      error.value = '无法访问摄像头: ' + (e.message || '未知错误')
    }
    emit('error', error.value)
  }
}

// 初始化扫码器
const initScanner = async () => {
  error.value = ''

  try {
    // 清理旧的扫码器实例
    if (scanner) {
      try {
        await scanner.stop()
      } catch (e) {
        // 忽略停止错误
      }
      scanner = null
    }

    // 先切换到 scanning 状态，让 DOM 渲染 #reader 元素
    scannerState.value = 'scanning'

    // 等待 DOM 更新和元素渲染
    await new Promise(resolve => setTimeout(resolve, 300))

    // 确保 DOM 元素已存在
    const readerElement = document.getElementById('reader')
    if (!readerElement) {
      throw new Error('扫码容器未找到')
    }

    scanner = new Html5Qrcode('reader')

    await scanner.start(
      { facingMode: 'environment' },
      {
        fps: 10,
        qrbox: { width: 250, height: 250 }
      },
      onScanSuccess,
      onScanFailure
    )

    scannerState.value = 'scanning'
  } catch (e: any) {
    console.error('Failed to start scanner:', e)

    if (e.name === 'NotAllowedError' || e.name === 'PermissionDeniedError') {
      error.value = '摄像头权限被拒绝，请在浏览器设置中允许访问摄像头'
      scannerState.value = 'permission_denied'
    } else if (e.name === 'NotFoundError') {
      error.value = '未找到摄像头设备，请检查摄像头是否连接'
    } else if (e.name === 'NotReadableError' || e.name === 'AbortError') {
      error.value = '摄像头被占用或已断开，请刷新页面重试'
    } else {
      error.value = '启动摄像头失败: ' + (e.message || '未知错误')
    }

    emit('error', error.value)
    scannerState.value = 'ready'
  }
}

const onScanSuccess = (decodedText: string, _decodedResult: any) => {
  scannerState.value = 'success'
  emit('scan', decodedText)
  // 停止扫码器
  stopScanner()
}

const onScanFailure = (_errorMessage: string) => {
  // 持续扫描，不处理错误
}

// 停止扫码
const stopScanning = () => {
  stopScanner()
  scannerState.value = 'ready'
  emit('cancel')
}

// 取消扫码
const cancelScanning = () => {
  stopScanner()
  scannerState.value = 'ready'
  emit('cancel')
}

// 停止扫码器
const stopScanner = () => {
  if (scanner) {
    scanner.stop().catch(error => {
      console.error('Failed to stop scanner:', error)
    })
    scanner = null
  }
}

const refreshPage = () => {
  window.location.reload()
}

onUnmounted(() => {
  stopScanner()
})

// 暴露方法给父组件
defineExpose({
  reset: () => {
    stopScanner()
    scannerState.value = 'ready'
    error.value = ''
  }
})
</script>

<style scoped>
.qr-scanner-wrapper {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}

/* 准备扫码卡片 */
.ready-card,
.permission-request-card,
.permission-denied-card,
.loading-card,
.success-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  border-radius: 16px;
  border: 2px dashed #d9d9d9;
  text-align: center;
  min-height: 300px;
}

.ready-card {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  border-color: #91d5ff;
}

.success-card {
  background: linear-gradient(135deg, #f6ffed 0%, #f0f9ff 100%);
  border-color: #b7eb8f;
}

.ready-icon,
.permission-icon,
.success-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  font-size: 36px;
  color: white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.ready-icon {
  background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.3);
}

.permission-icon {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.3);
}

.permission-icon.denied {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  box-shadow: 0 8px 24px rgba(255, 77, 79, 0.3);
}

.success-icon {
  background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
  box-shadow: 0 8px 24px rgba(82, 196, 26, 0.3);
}

.ready-title,
.permission-title,
.success-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.ready-desc,
.permission-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 24px 0;
  line-height: 1.6;
  max-width: 280px;
}

.permission-actions {
  display: flex;
  gap: 12px;
}

.loading-text {
  margin-top: 16px;
  font-size: 14px;
  color: #6b7280;
}

.success-desc {
  font-size: 14px;
  color: #52c41a;
  margin: 0;
}

/* 扫码区域 */
.scanner-active {
  background: #000;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.scanner-header {
  position: relative;
  padding: 20px;
}

.scanner-frame {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 250px;
  height: 250px;
  pointer-events: none;
  z-index: 10;
}

.corner {
  position: absolute;
  width: 30px;
  height: 30px;
  border-color: #1890ff;
  border-style: solid;
}

.top-left {
  top: 0;
  left: 0;
  border-width: 4px 0 0 4px;
  border-top-left-radius: 8px;
}

.top-right {
  top: 0;
  right: 0;
  border-width: 4px 4px 0 0;
  border-top-right-radius: 8px;
}

.bottom-left {
  bottom: 0;
  left: 0;
  border-width: 0 0 4px 4px;
  border-bottom-left-radius: 8px;
}

.bottom-right {
  bottom: 0;
  right: 0;
  border-width: 0 4px 4px 0;
  border-bottom-right-radius: 8px;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #1890ff, transparent);
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% {
    top: 0;
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    top: 100%;
    opacity: 0;
  }
}

.reader-element {
  width: 100%;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reader-element :deep(video) {
  border-radius: 8px;
  max-width: 100%;
}

.scanner-footer {
  background: #1f2937;
  padding: 16px;
  text-align: center;
}

.scanner-hint {
  margin: 0 0 8px 0;
  color: #9ca3af;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 错误提示 */
.error-message {
  margin-top: 16px;
  padding: 12px 16px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  color: #ff4d4f;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 隐藏 html5-qrcode 默认的头部和文件选择 */
:deep(#reader__header_message) {
  display: none !important;
}

:deep(#reader__dashboard_section_csr span) {
  color: #9ca3af !important;
}

:deep(#reader__dashboard_section_swaplink) {
  display: v-bind(allowFile ? 'inline-block' : 'none') !important;
}

:deep(input[type="file"]) {
  display: v-bind(allowFile ? 'block' : 'none') !important;
}
</style>
