<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stat-cards">
      <a-col :span="6">
        <a-card>
          <Statistic
            title="总用户数"
            :value="stats.userCount"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <Statistic
            title="总任务数"
            :value="stats.taskCount"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <FileTextOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <Statistic
            title="总订单数"
            :value="stats.orderCount"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <ShoppingCartOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <Statistic
            title="交易总额"
            :value="stats.totalAmount"
            :precision="2"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix>
              <DollarOutlined />
            </template>
          </Statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 今日数据 -->
    <a-row :gutter="16" class="today-stats">
      <a-col :span="24">
        <a-card title="今日数据">
          <a-row :gutter="16">
            <a-col :span="6">
              <div class="today-item">
                <div class="today-label">新增用户</div>
                <div class="today-value">{{ stats.todayUserCount || 0 }}</div>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="today-item">
                <div class="today-label">新增任务</div>
                <div class="today-value">{{ stats.todayTaskCount || 0 }}</div>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="today-item">
                <div class="today-label">新增订单</div>
                <div class="today-value">{{ stats.todayOrderCount || 0 }}</div>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="today-item">
                <div class="today-label">今日交易额</div>
                <div class="today-value">¥{{ (stats.todayAmount || 0).toFixed(2) }}</div>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="charts">
      <a-col :span="12">
        <a-card title="任务状态分布" :loading="chartLoading">
          <div ref="taskStatusChart" style="height: 300px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="近7天交易趋势" :loading="chartLoading">
          <div ref="amountTrendChart" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 用户增长趋势 -->
    <a-row :gutter="16" class="charts">
      <a-col :span="24">
        <a-card title="用户增长趋势" :loading="chartLoading">
          <div ref="userGrowthChart" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Statistic } from 'ant-design-vue'
import {
  UserOutlined,
  FileTextOutlined,
  ShoppingCartOutlined,
  DollarOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import { getDashboardData, getTaskStatusStats, getAmountTrend, getUserGrowth } from '@/api/dashboard'

const stats = ref<Record<string, number>>({})
const chartLoading = ref(false)
const taskStatusChart = ref<HTMLElement>()
const amountTrendChart = ref<HTMLElement>()
const userGrowthChart = ref<HTMLElement>()

// 任务状态颜色映射
const statusColors: Record<number, string> = {
  0: '#faad14', // 待接单 - 黄色
  1: '#1890ff', // 已接单 - 蓝色
  2: '#52c41a', // 待取件 - 绿色
  3: '#722ed1', // 配送中 - 紫色
  4: '#fa8c16', // 待确认 - 橙色
  5: '#52c41a', // 已完成 - 绿色
  6: '#ff4d4f'  // 已取消 - 红色
}

const loadData = async () => {
  try {
    const res = await getDashboardData()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载仪表盘数据失败', error)
  }
}

const initCharts = async () => {
  chartLoading.value = true
  
  try {
    // 加载任务状态分布数据
    const statusRes = await getTaskStatusStats()
    if (statusRes.code === 200 && taskStatusChart.value) {
      const chart = echarts.init(taskStatusChart.value)
      const statusData = statusRes.data.data || []
      
      chart.setOption({
        tooltip: { 
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: { 
          bottom: '5%',
          type: 'scroll'
        },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { 
            show: true,
            formatter: '{b}\n{c}'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: statusData.map((item: any) => ({
            name: item.name,
            value: item.value,
            itemStyle: { color: statusColors[item.status] }
          }))
        }]
      })
    }

    // 加载交易趋势数据
    const trendRes = await getAmountTrend()
    if (trendRes.code === 200 && amountTrendChart.value) {
      const chart = echarts.init(amountTrendChart.value)
      const dates = trendRes.data.dates || []
      const amounts = trendRes.data.amounts || []
      
      chart.setOption({
        tooltip: { 
          trigger: 'axis',
          formatter: '{b}<br/>交易额: ¥{c}'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          boundaryGap: false
        },
        yAxis: { 
          type: 'value',
          axisLabel: {
            formatter: '¥{value}'
          }
        },
        series: [{
          data: amounts,
          type: 'line',
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
              { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
            ])
          },
          itemStyle: { color: '#667eea' },
          lineStyle: {
            width: 3
          }
        }]
      })
    }

    // 加载用户增长趋势数据
    const growthRes = await getUserGrowth()
    if (growthRes.code === 200 && userGrowthChart.value) {
      const chart = echarts.init(userGrowthChart.value)
      const dates = growthRes.data.dates || []
      const newUsers = growthRes.data.newUsers || []
      const totalUsers = growthRes.data.totalUsers || []
      
      chart.setOption({
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['新增用户', '总用户数']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: [
          {
            type: 'value',
            name: '新增用户',
            position: 'left'
          },
          {
            type: 'value',
            name: '总用户数',
            position: 'right'
          }
        ],
        series: [
          {
            name: '新增用户',
            type: 'bar',
            data: newUsers,
            itemStyle: { color: '#667eea' }
          },
          {
            name: '总用户数',
            type: 'line',
            yAxisIndex: 1,
            data: totalUsers,
            itemStyle: { color: '#52c41a' },
            smooth: true
          }
        ]
      })
    }
  } catch (error) {
    console.error('加载图表数据失败', error)
  } finally {
    chartLoading.value = false
  }
}

onMounted(() => {
  loadData()
  initCharts()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-cards {
  margin-bottom: 24px;
}

.today-stats {
  margin-bottom: 24px;
}

.today-item {
  text-align: center;
  padding: 16px;
}

.today-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.today-value {
  color: #333;
  font-size: 24px;
  font-weight: bold;
}

.charts {
  margin-bottom: 24px;
}
</style>
