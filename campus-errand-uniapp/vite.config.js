import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

export default defineConfig({
  plugins: [
    uni()
  ],
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler',
        silenceDeprecations: ['legacy-js-api']
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  }
})
