<template>
  <div class="stats-container">
    <a-page-header
      title="数据统计分析"
      sub-title="查看座位使用热力图、违规趋势及区域拥堵情况"
    >
      <template #extra>
        <a-space>
          <span>数据模拟:</span>
          <a-switch v-model:checked="isSimulate" @change="handleSimulateChange" />
        </a-space>
      </template>
    </a-page-header>
    
    <a-row :gutter="[24, 24]">
      <!-- 区域拥堵度 (柱状图) -->
      <a-col :xs="24" :lg="12">
        <a-card title="区域拥堵度分析" :bordered="false">
          <template #extra>
            <a-date-picker 
              v-model:value="congestionDate" 
              size="small"
              :allow-clear="false"
              :disabled-date="disabledDate"
              @change="handleCongestionDateChange"
            />
          </template>
          <a-spin :spinning="congestionLoading">
            <base-chart :options="congestionOption" height="350px" />
          </a-spin>
        </a-card>
      </a-col>

      <!-- 违规趋势 (折线图) -->
      <a-col :xs="24" :lg="12">
        <a-card title="违规次数统计 (近7天)" :bordered="false">
          <base-chart :options="violationOption" height="350px" />
        </a-card>
      </a-col>

      <!-- 座位热力图 -->
      <a-col :span="24">
        <a-card title="图书馆座位使用热力图" :bordered="false">
          <template #extra>
            <a-space>
              <span>选择日期:</span>
              <a-date-picker 
                v-model:value="selectedDate" 
                :allow-clear="false"
                :disabled-date="disabledDate"
                @change="handleDateChange"
                placeholder="选择统计日期"
              />
            </a-space>
          </template>
          <a-spin :spinning="heatmapLoading">
            <base-chart :options="heatmapOption" height="500px" />
          </a-spin>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import BaseChart from '../components/Chart/BaseChart.vue'
import { getHeatmapData, getCongestionData, getViolationTrend } from '../api/stats'
import dayjs, { Dayjs } from 'dayjs'

const hours = ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00']
const days = ref(['A区', 'B区', 'C区', 'D区', 'E区'])

const heatmapData = ref([])
const heatmapLoading = ref(false)
const selectedDate = ref<Dayjs>(dayjs())
const congestionData = ref<Record<string, number>>({})
const congestionDate = ref<Dayjs>(dayjs())
const congestionLoading = ref(false)
const violationData = ref({ dates: [], counts: [] })
const isSimulate = ref(true)

const fetchHeatmap = async (date?: string) => {
  heatmapLoading.value = true
  try {
    const res = await getHeatmapData(date, isSimulate.value)
    const result = (res as any).data || res
    heatmapData.value = result.data || result
    if (result.areas) {
      days.value = result.areas
    }
  } catch (error) {
    console.error(error)
  } finally {
    heatmapLoading.value = false
  }
}

const fetchCongestion = async (date?: string) => {
  congestionLoading.value = true
  try {
    const res = await getCongestionData(date, isSimulate.value)
    congestionData.value = (res as any).data || res
  } catch (error) {
    console.error(error)
  } finally {
    congestionLoading.value = false
  }
}

const handleDateChange = (date: Dayjs) => {
  if (date) {
    fetchHeatmap(date.format('YYYY-MM-DD'))
  }
}

const handleCongestionDateChange = (date: Dayjs) => {
  if (date) {
    fetchCongestion(date.format('YYYY-MM-DD'))
  }
}

const handleSimulateChange = () => {
  fetchHeatmap(selectedDate.value.format('YYYY-MM-DD'))
  fetchCongestion(congestionDate.value.format('YYYY-MM-DD'))
}

const disabledDate = (current: Dayjs) => {
  return current && current > dayjs().endOf('day')
}

onMounted(async () => {
  try {
    const [vRes] = await Promise.all([
      getViolationTrend()
    ])
    violationData.value = (vRes as any).data || vRes
    
    // 默认获取当天数据
    fetchHeatmap(selectedDate.value.format('YYYY-MM-DD'))
    fetchCongestion(congestionDate.value.format('YYYY-MM-DD'))
  } catch (error) {
    console.error(error)
  }
})

// 1. 区域拥堵度配置
const congestionOption = computed(() => ({
  tooltip: { 
    trigger: 'axis', 
    axisPointer: { type: 'shadow' },
    formatter: '{b}: {c}%'
  },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: Object.keys(congestionData.value),
    axisTick: { alignWithLabel: true }
  },
  yAxis: { type: 'value', name: '占用率(%)', max: 100 },
  series: [
    {
      name: '占用率',
      type: 'bar',
      barWidth: '60%',
      data: Object.values(congestionData.value),
      itemStyle: {
        color: (params: any) => {
          const val = params.value
          if (val > 80) return '#cf1322' // 极度拥堵
          if (val > 50) return '#faad14' // 拥堵
          return '#3f8600' // 舒适
        }
      }
    }
  ]
}))

// 2. 违规趋势配置
const violationOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: violationData.value.dates
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: '违规次数',
      type: 'line',
      smooth: true,
      data: violationData.value.counts,
      itemStyle: { color: '#ff4d4f' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(255, 77, 79, 0.4)' },
            { offset: 1, color: 'rgba(255, 77, 79, 0.1)' }
          ]
        }
      }
    }
  ]
}))

const heatmapOption = computed(() => ({
  tooltip: { position: 'top' },
  grid: { height: '50%', top: '10%' },
  xAxis: {
    type: 'category',
    data: hours,
    splitArea: { show: true }
  },
  yAxis: {
    type: 'category',
    data: days.value,
    splitArea: { show: true }
  },
  visualMap: {
    min: 0,
    max: 100,
    calculable: true,
    orient: 'horizontal',
    left: 'center',
    bottom: '15%',
    inRange: {
      color: ['#fff7ed', '#ffedd5', '#fdba74', '#f97316', '#ea580c', '#9a3412'] // 橙红色系热力图
    }
  },
  series: [
    {
      name: '拥挤度',
      type: 'heatmap',
      data: heatmapData.value,
      label: { 
        show: true,
        formatter: (params: any) => params.data[2] > 20 ? params.data[2] + '%' : '' // 只在较热区域显示百分比
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
}))
</script>

<style scoped>
.stats-container {
  padding: 24px;
}
</style>
