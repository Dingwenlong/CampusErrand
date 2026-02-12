<script>
export default {
  globalData: {
    tabBarIndex: 0
  },
  onLaunch: function() {
    console.log('App Launch')
    this.detectPlatform()
    this.initSystemInfo()
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  },
  methods: {
    detectPlatform() {
      // 检测平台并添加相应类名
      const systemInfo = uni.getSystemInfoSync()
      const platform = systemInfo.platform
      
      // 只在H5环境下操作DOM
      // #ifdef H5
      if (typeof document !== 'undefined' && document.body) {
        // 添加平台类名到 body
        if (platform === 'ios') {
          document.body.classList.add('ios')
        } else if (platform === 'android') {
          document.body.classList.add('android')
        }
        document.body.classList.add('h5')
      }
      // #endif
      
      // 小程序平台检测
      // #ifdef MP-WEIXIN
      console.log('当前平台: 微信小程序')
      // #endif
    },
    initSystemInfo() {
      try {
        const systemInfo = uni.getSystemInfoSync()
        // 存储系统信息供全局使用
        uni.setStorageSync('systemInfo', {
          platform: systemInfo.platform,
          screenWidth: systemInfo.screenWidth,
          screenHeight: systemInfo.screenHeight,
          windowWidth: systemInfo.windowWidth,
          windowHeight: systemInfo.windowHeight,
          statusBarHeight: systemInfo.statusBarHeight,
          safeArea: systemInfo.safeArea,
          pixelRatio: systemInfo.pixelRatio
        })
      } catch (e) {
        console.error('获取系统信息失败', e)
      }
    }
  }
}
</script>

<style>
/* 引入全局样式变量 */
@import './static/styles/variables.css';

/* 引入平台适配样式 */
@import './static/styles/platform.scss';

/* ============================================
   全局重置样式 - Global Reset Styles
   ============================================ */

/* 页面通用样式 */
page {
  font-family: var(--font-family-base);
  font-size: var(--font-size-base);
  line-height: var(--line-height-normal);
  color: var(--color-text-primary);
  background-color: var(--color-bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  -webkit-tap-highlight-color: transparent;
}

/* 安全区域适配 */
.safe-area-top {
  padding-top: env(safe-area-inset-top);
}

.safe-area-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}

/* 隐藏滚动条 */
.hide-scrollbar::-webkit-scrollbar {
  display: none;
}

.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 按钮重置 */
button {
  margin: 0;
  padding: 0;
  background: none;
  border: none;
  font-size: inherit;
  line-height: inherit;
}

button::after {
  border: none;
}

/* 输入框重置 */
input, textarea {
  outline: none;
  border: none;
  background: none;
}

/* 图片默认样式 */
image {
  display: block;
  max-width: 100%;
}

/* 链接默认样式 - 仅在H5环境 */
/* #ifdef H5 */
a {
  color: var(--color-primary);
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
/* #endif */

/* ============================================
   通用工具类 - Utility Classes
   ============================================ */

/* 显示/隐藏 */
.hidden {
  display: none !important;
}

.invisible {
  visibility: hidden;
}

/* 浮动 */
.float-left {
  float: left;
}

.float-right {
  float: right;
}

.clearfix::after {
  content: '';
  display: table;
  clear: both;
}

/* 文本对齐 */
.text-left {
  text-align: left;
}

.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

/* 字体粗细 */
.font-normal {
  font-weight: var(--font-weight-normal);
}

.font-medium {
  font-weight: var(--font-weight-medium);
}

.font-semibold {
  font-weight: var(--font-weight-semibold);
}

.font-bold {
  font-weight: var(--font-weight-bold);
}

/* 文本颜色 */
.text-primary {
  color: var(--color-text-primary);
}

.text-secondary {
  color: var(--color-text-secondary);
}

.text-tertiary {
  color: var(--color-text-tertiary);
}

.text-placeholder {
  color: var(--color-text-placeholder);
}

.text-disabled {
  color: var(--color-text-disabled);
}

.text-inverse {
  color: var(--color-text-inverse);
}

/* 背景颜色 */
.bg-primary {
  background-color: var(--color-primary);
}

.bg-success {
  background-color: var(--color-success);
}

.bg-warning {
  background-color: var(--color-warning);
}

.bg-error {
  background-color: var(--color-error);
}

.bg-info {
  background-color: var(--color-info);
}

.bg-white {
  background-color: var(--color-white);
}

.bg-gray {
  background-color: var(--color-bg);
}

/* 间距工具类 */
.m-0 { margin: 0; }
.m-1 { margin: var(--space-1); }
.m-2 { margin: var(--space-2); }
.m-3 { margin: var(--space-3); }
.m-4 { margin: var(--space-4); }
.m-5 { margin: var(--space-5); }
.m-6 { margin: var(--space-6); }

.mt-0 { margin-top: 0; }
.mt-1 { margin-top: var(--space-1); }
.mt-2 { margin-top: var(--space-2); }
.mt-3 { margin-top: var(--space-3); }
.mt-4 { margin-top: var(--space-4); }
.mt-5 { margin-top: var(--space-5); }
.mt-6 { margin-top: var(--space-6); }

.mb-0 { margin-bottom: 0; }
.mb-1 { margin-bottom: var(--space-1); }
.mb-2 { margin-bottom: var(--space-2); }
.mb-3 { margin-bottom: var(--space-3); }
.mb-4 { margin-bottom: var(--space-4); }
.mb-5 { margin-bottom: var(--space-5); }
.mb-6 { margin-bottom: var(--space-6); }

.ml-0 { margin-left: 0; }
.ml-1 { margin-left: var(--space-1); }
.ml-2 { margin-left: var(--space-2); }
.ml-3 { margin-left: var(--space-3); }
.ml-4 { margin-left: var(--space-4); }
.ml-5 { margin-left: var(--space-5); }
.ml-6 { margin-left: var(--space-6); }

.mr-0 { margin-right: 0; }
.mr-1 { margin-right: var(--space-1); }
.mr-2 { margin-right: var(--space-2); }
.mr-3 { margin-right: var(--space-3); }
.mr-4 { margin-right: var(--space-4); }
.mr-5 { margin-right: var(--space-5); }
.mr-6 { margin-right: var(--space-6); }

.p-0 { padding: 0; }
.p-1 { padding: var(--space-1); }
.p-2 { padding: var(--space-2); }
.p-3 { padding: var(--space-3); }
.p-4 { padding: var(--space-4); }
.p-5 { padding: var(--space-5); }
.p-6 { padding: var(--space-6); }

.pt-0 { padding-top: 0; }
.pt-1 { padding-top: var(--space-1); }
.pt-2 { padding-top: var(--space-2); }
.pt-3 { padding-top: var(--space-3); }
.pt-4 { padding-top: var(--space-4); }
.pt-5 { padding-top: var(--space-5); }
.pt-6 { padding-top: var(--space-6); }

.pb-0 { padding-bottom: 0; }
.pb-1 { padding-bottom: var(--space-1); }
.pb-2 { padding-bottom: var(--space-2); }
.pb-3 { padding-bottom: var(--space-3); }
.pb-4 { padding-bottom: var(--space-4); }
.pb-5 { padding-bottom: var(--space-5); }
.pb-6 { padding-bottom: var(--space-6); }

.pl-0 { padding-left: 0; }
.pl-1 { padding-left: var(--space-1); }
.pl-2 { padding-left: var(--space-2); }
.pl-3 { padding-left: var(--space-3); }
.pl-4 { padding-left: var(--space-4); }
.pl-5 { padding-left: var(--space-5); }
.pl-6 { padding-left: var(--space-6); }

.pr-0 { padding-right: 0; }
.pr-1 { padding-right: var(--space-1); }
.pr-2 { padding-right: var(--space-2); }
.pr-3 { padding-right: var(--space-3); }
.pr-4 { padding-right: var(--space-4); }
.pr-5 { padding-right: var(--space-5); }
.pr-6 { padding-right: var(--space-6); }

/* 圆角工具类 */
.rounded-none { border-radius: 0; }
.rounded-sm { border-radius: var(--radius-sm); }
.rounded-md { border-radius: var(--radius-md); }
.rounded-lg { border-radius: var(--radius-lg); }
.rounded-xl { border-radius: var(--radius-xl); }
.rounded-full { border-radius: var(--radius-full); }

/* 阴影工具类 */
.shadow-none { box-shadow: none; }
.shadow-sm { box-shadow: var(--shadow-sm); }
.shadow-md { box-shadow: var(--shadow-md); }
.shadow-lg { box-shadow: var(--shadow-lg); }
.shadow-xl { box-shadow: var(--shadow-xl); }

/* 动画工具类 */
.transition { transition: all var(--duration-fast) var(--ease-out); }
.transition-fast { transition: all var(--duration-fast) var(--ease-out); }
.transition-normal { transition: all var(--duration-normal) var(--ease-out); }
.transition-slow { transition: all var(--duration-slow) var(--ease-out); }

/* 禁用状态 */
.disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* 加载状态 */
.loading {
  pointer-events: none;
  opacity: 0.7;
}

/* 点击效果 */
.clickable {
  cursor: pointer;
  transition: opacity var(--duration-fast) var(--ease-out);
}

.clickable:active {
  opacity: 0.7;
}

/* 按压效果 */
.pressable {
  transition: transform var(--duration-fast) var(--ease-out);
}

.pressable:active {
  transform: scale(0.96);
}

/* 悬浮效果 */
.hoverable {
  transition: all var(--duration-fast) var(--ease-out);
}

.hoverable:hover {
  transform: translateY(-2rpx);
  box-shadow: var(--shadow-md);
}

/* ============================================
   动画关键帧 - Animation Keyframes
   ============================================ */

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideLeft {
  from {
    opacity: 0;
    transform: translateX(20rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideRight {
  from {
    opacity: 0;
    transform: translateX(-20rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes scaleOut {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.9);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* 动画类 */
.animate-fade-in {
  animation: fadeIn var(--duration-normal) var(--ease-out);
}

.animate-slide-up {
  animation: slideUp var(--duration-normal) var(--ease-out);
}

.animate-scale-in {
  animation: scaleIn var(--duration-normal) var(--ease-spring);
}

.animate-spin {
  animation: spin 1s linear infinite;
}

.animate-pulse {
  animation: pulse 2s ease-in-out infinite;
}

.animate-bounce {
  animation: bounce 1s ease-in-out infinite;
}

.animate-shimmer {
  background: linear-gradient(90deg, var(--color-bg-secondary) 25%, var(--color-bg-tertiary) 50%, var(--color-bg-secondary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

/* ============================================
   小程序兼容性样式 - Mini Program Compatibility
   ============================================ */

/* 微信小程序特殊处理 */
/* #ifdef MP-WEIXIN */
page {
  /* 确保页面背景色一致 */
  background-color: var(--color-bg);
}

/* 修复微信小程序按钮默认样式 */
button[type="primary"] {
  background-color: var(--color-primary);
}

button[type="primary"]::after {
  border: none;
}

/* 修复微信小程序输入框默认样式 */
input {
  min-height: auto;
}

/* 修复微信小程序 scroll-view 样式 */
scroll-view {
  box-sizing: border-box;
}

/* 修复微信小程序 rich-text 样式 */
rich-text {
  display: block;
}

/* 修复微信小程序 image 组件样式 */
image {
  will-change: transform;
}

/* 修复微信小程序 view 组件的默认边距 */
view {
  box-sizing: border-box;
}
/* #endif */

/* 支付宝小程序特殊处理 */
/* #ifdef MP-ALIPAY */
page {
  background-color: var(--color-bg);
}

.my-ali-button {
  background: var(--color-primary);
}
/* #endif */

/* H5 特殊处理 */
/* #ifdef H5 */
html {
  /* 禁止页面缩放 */
  touch-action: manipulation;
}

body {
  /* 优化字体渲染 */
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

/* 自定义滚动条样式 */
::-webkit-scrollbar {
  width: 6rpx;
  height: 6rpx;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: var(--radius-full);
}

::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-tertiary);
}
/* #endif */

/* App 特殊处理 */
/* #ifdef APP-PLUS */
page {
  background-color: var(--color-bg);
}

/* 修复 App 端状态栏问题 */
.status-bar {
  height: var(--status-bar-height);
}
/* #endif */

/* ============================================
   安全区域适配 - Safe Area Adaptation
   ============================================ */

/* iPhone X+ 安全区域适配 */
.safe-area-top {
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
}

.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.safe-area-left {
  padding-left: constant(safe-area-inset-left);
  padding-left: env(safe-area-inset-left);
}

.safe-area-right {
  padding-right: constant(safe-area-inset-right);
  padding-right: env(safe-area-inset-right);
}

/* 底部固定元素安全区域 */
.fixed-bottom {
  padding-bottom: calc(var(--space-4) + env(safe-area-inset-bottom));
}

/* ============================================
   性能优化样式 - Performance Optimization
   ============================================ */

/* 启用硬件加速 */
.gpu-accelerated {
  transform: translateZ(0);
  will-change: transform;
}

/* 优化动画性能 */
.optimize-animation {
  will-change: transform, opacity;
  transform: translateZ(0);
}

/* 减少重绘区域 */
.contain-layout {
  contain: layout;
}

.contain-paint {
  contain: paint;
}

.contain-strict {
  contain: strict;
}

/* 图片懒加载占位 */
.img-placeholder {
  background-color: var(--color-bg-secondary);
  animation: shimmer 1.5s infinite;
}

/* 内容可见性优化 */
.content-visibility-auto {
  content-visibility: auto;
}

/* ============================================
   可访问性 - Accessibility
   ============================================ */

/* 减少动画偏好 */
/* #ifdef H5 */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
/* #endif */

/* 焦点样式 */
:focus-visible {
  outline: 2rpx solid var(--color-primary);
  outline-offset: 2rpx;
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .card-hover,
  .pressable,
  .btn-primary,
  .btn-ghost {
    border: 2rpx solid currentColor;
  }
}

/* 暗色模式支持（预留） */
@media (prefers-color-scheme: dark) {
  /* 暗色模式样式将在后续版本中实现 */
}
</style>
