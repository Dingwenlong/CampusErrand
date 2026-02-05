<template>
  <div ref="chartRef" :style="{ width: width, height: height }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  options: {
    type: Object,
    required: true
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '400px'
  }
})

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption(props.options)
  }
}

const resizeChart = () => {
  chartInstance?.resize()
}

watch(() => props.options, (newOptions) => {
  chartInstance?.setOption(newOptions, true)
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
})
</script>
