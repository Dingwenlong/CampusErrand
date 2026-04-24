<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <a-row :gutter="[16, 16]" class="stat-cards">
      <a-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);">
            <UserOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">总用户数</div>
            <div class="stat-value">{{ stats.userCount || 0 }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%);">
            <FileTextOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">总任务数</div>
            <div class="stat-value">{{ stats.taskCount || 0 }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #1d4ed8 0%, #0ea5e9 100%);">
            <DollarOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">交易总额</div>
            <div class="stat-value">¥{{ formatAmount(stats.totalAmount) }}</div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" class="charts">
      <a-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
        <a-card title="📈 任务状态分布" :loading="chartLoading" class="chart-card">
          <div ref="taskStatusChartRef" class="chart-container" style="height: 300px;"></div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
        <a-card title="💹 近7天交易趋势" :loading="chartLoading" class="chart-card">
          <div ref="amountTrendChartRef" class="chart-container" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 用户增长趋势 -->
    <a-row :gutter="[16, 16]" class="charts">
      <a-col :span="24">
        <a-card title="📊 用户增长趋势" :loading="chartLoading" class="chart-card">
          <div ref="userGrowthChartRef" class="chart-container" style="height: 350px;"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import {
  UserOutlined,
  FileTextOutlined,
  DollarOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import type { EChartsType } from 'echarts'
import { getDashboardData, getTaskStatusStats, getAmountTrend, getUserGrowth } from '@/api/dashboard'

interface DashboardStats {
  userCount: number
  taskCount: number
  totalAmount: number | string
}

const stats = ref<DashboardStats>({
  userCount: 0,
  taskCount: 0,
  totalAmount: 0
})
const chartLoading = ref(false)
const taskStatusChartRef = ref<HTMLElement | null>(null)
const amountTrendChartRef = ref<HTMLElement | null>(null)
const userGrowthChartRef = ref<HTMLElement | null>(null)
const taskStatusChartInstance = ref<EChartsType | null>(null)
const amountTrendChartInstance = ref<EChartsType | null>(null)
const userGrowthChartInstance = ref<EChartsType | null>(null)

const statusColors: Record<number, string> = {
  0: '#FFC300',
  1: '#4ECDC4',
  2: '#52c41a',
  3: '#667eea',
  4: '#FF6B6B',
  5: '#52c41a',
  6: '#999999'
}

const formatAmount = (value: number | string | undefined) => Number(value || 0).toFixed(2)

const loadData = async () => {
  try {
    const res = await getDashboardData()
    
    if (res.code === 200 && res.data) {
      const userCount = Number(res.data.userCount || 0)
      const taskCount = Number(res.data.taskCount || 0)
      const totalAmount = Number(res.data.totalAmount || 0)
      stats.value = {
        userCount,
        taskCount,
        totalAmount
      }
    }
  } catch (error) {
    console.error('加载仪表盘数据失败', error)
  }
}

const initTaskStatusChart = async (data: any[]) => {
  if (!taskStatusChartRef.value) {
    console.error('任务状态图表容器未找到')
    return
  }
  
  taskStatusChartInstance.value?.dispose()
  taskStatusChartInstance.value = echarts.init(taskStatusChartRef.value)
  
  const chart = taskStatusChartInstance.value
  chart.setOption({
    tooltip: { 
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: { 
      bottom: '5%',
      type: 'scroll',
      textStyle: {
        fontSize: 12
      }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { 
        show: true,
        formatter: '{b}\n{c}',
        fontSize: 11
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: data.map((item: any) => ({
        name: item.name,
        value: item.value,
        itemStyle: { color: statusColors[item.status] }
      }))
    }]
  })
  
}

const initAmountTrendChart = async (dates: string[], amounts: number[]) => {
  if (!amountTrendChartRef.value) {
    console.error('交易趋势图表容器未找到')
    return
  }
  
  amountTrendChartInstance.value?.dispose()
  amountTrendChartInstance.value = echarts.init(amountTrendChartRef.value)
  
  const chart = amountTrendChartInstance.value
  chart.setOption({
    tooltip: { 
      trigger: 'axis',
      formatter: '{b}<br/>交易额: ¥{c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLabel: {
        fontSize: 11
      }
    },
    yAxis: { 
      type: 'value',
      axisLabel: {
        formatter: '¥{value}',
        fontSize: 11
      }
    },
    series: [{
      data: amounts,
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255, 195, 0, 0.5)' },
          { offset: 1, color: 'rgba(255, 195, 0, 0.1)' }
        ])
      },
      itemStyle: { color: '#FFC300' },
      lineStyle: {
        width: 3
      }
    }]
  })
  
}

const initUserGrowthChart = async (dates: string[], newUsers: number[], totalUsers: number[]) => {
  if (!userGrowthChartRef.value) {
    console.error('用户增长图表容器未找到')
    return
  }
  
  userGrowthChartInstance.value?.dispose()
  userGrowthChartInstance.value = echarts.init(userGrowthChartRef.value)
  
  const chart = userGrowthChartInstance.value
  chart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['新增用户', '总用户数'],
      top: '5%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        fontSize: 11
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '新增用户',
        position: 'left',
        axisLabel: {
          fontSize: 11
        }
      },
      {
        type: 'value',
        name: '总用户数',
        position: 'right',
        axisLabel: {
          fontSize: 11
        }
      }
    ],
    series: [
      {
        name: '新增用户',
        type: 'bar',
        data: newUsers,
        itemStyle: { color: '#FFC300' }
      },
      {
        name: '总用户数',
        type: 'line',
        yAxisIndex: 1,
        data: totalUsers,
        itemStyle: { color: '#4ECDC4' },
        smooth: true
      }
    ]
  })
  
}

const initCharts = async () => {
  chartLoading.value = true
  let statusData: any[] = []
  let trendDates: string[] = []
  let trendAmounts: number[] = []
  let growthDates: string[] = []
  let growthNewUsers: number[] = []
  let growthTotalUsers: number[] = []
  
  try {
    const statusRes = await getTaskStatusStats()
    if (statusRes.code === 200 && statusRes.data?.data?.length > 0) {
      statusData = statusRes.data.data
    }

    const trendRes = await getAmountTrend()
    if (trendRes.code === 200 && trendRes.data?.dates?.length > 0) {
      trendDates = trendRes.data.dates
      trendAmounts = trendRes.data.amounts
    }

    const growthRes = await getUserGrowth()
    if (growthRes.code === 200 && growthRes.data?.dates?.length > 0) {
      growthDates = growthRes.data.dates
      growthNewUsers = growthRes.data.newUsers
      growthTotalUsers = growthRes.data.totalUsers
    }
  } catch (error) {
    console.error('加载图表数据失败', error)
  } finally {
    chartLoading.value = false
  }

  await nextTick()
  await new Promise(resolve => setTimeout(resolve, 200))

  if (statusData.length > 0) {
    await initTaskStatusChart(statusData)
  }

  if (trendDates.length > 0) {
    await initAmountTrendChart(trendDates, trendAmounts)
  }

  if (growthDates.length > 0) {
    await initUserGrowthChart(growthDates, growthNewUsers, growthTotalUsers)
  }
}

const resizeCharts = () => {
  taskStatusChartInstance.value?.resize()
  amountTrendChartInstance.value?.resize()
  userGrowthChartInstance.value?.resize()
}

const disposeCharts = () => {
  taskStatusChartInstance.value?.dispose()
  amountTrendChartInstance.value?.dispose()
  userGrowthChartInstance.value?.dispose()
  taskStatusChartInstance.value = null
  amountTrendChartInstance.value = null
  userGrowthChartInstance.value = null
}

onMounted(async () => {
  await nextTick()
  await loadData()
  await nextTick()
  await initCharts()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  disposeCharts()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
}

.stat-card :deep(.ant-card-body) {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  margin-right: 12px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.charts {
  margin-bottom: 16px;
}

.chart-card {
  border-radius: 12px;
}

.chart-card :deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  font-weight: 600;
}

.chart-container {
  height: 250px;
}

@media (max-width: 768px) {
  .stat-card :deep(.ant-card-body) {
    padding: 12px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
    margin-right: 8px;
  }
  
  .stat-label {
    font-size: 12px;
  }
  
  .stat-value {
    font-size: 16px;
  }
  
  .chart-container {
    height: 220px;
  }
}

@media (max-width: 576px) {
  .stat-card :deep(.ant-card-body) {
    flex-direction: column;
    text-align: center;
    padding: 10px;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }
  
  .stat-content {
    text-align: center;
  }
}
</style>
