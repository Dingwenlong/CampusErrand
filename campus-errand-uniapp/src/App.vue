<script>
export default {
  onLaunch: function() {
    console.log('App Launch')
    this.detectPlatform()
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
    }
  }
}
</script>

<style>
/* 引入全局样式变量 */
@import './static/styles/variables.css';

/* 引入平台适配样式 */
@import './static/styles/platform.scss';

/* 页面通用样式 */
page {
  font-family: var(--font-family-base);
  font-size: var(--font-size-base);
  line-height: var(--line-height-normal);
  color: var(--color-text-primary);
  background-color: var(--color-bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
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
  text-decoration: none;
  color: inherit;
}
/* #endif */

/* 列表重置 */
ul, ol {
  list-style: none;
}
</style>
