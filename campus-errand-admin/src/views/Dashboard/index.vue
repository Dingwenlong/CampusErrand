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
                <div class="today-value">{{ stats.todayUserCount }}</div>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="today-item">
                <div class="today-label">新增任务</div>
                <div class="today-value">{{ stats.todayTaskCount }}</div>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="today-item">
                <div class="today-label">新增订单</div>
                <div class="today-value">{{ stats.todayOrderCount }}</div>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="today-item">
                <div class="today-label">今日交易额</div>
                <div class="today-value">¥{{ stats.todayAmount?.toFixed(2) }}</div>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="charts">
      <a-col :span="12">
        <a-card title="任务状态分布">
          <div ref="taskStatusChart" style="height: 300px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="近7天交易趋势">
          <div ref="amountTrendChart" style="height: 300px;"></div>
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
import { getDashboardData } from '@/api/dashboard'
import type { DashboardData } from '@/types'

const stats = ref<Partial<DashboardData>>({})
const taskStatusChart = ref<HTMLElement>()
const amountTrendChart = ref<HTMLElement>()

const loadData = async () => {
  try {
    const res = await getDashboardData()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const initCharts = () => {
  // 任务状态分布图
  if (taskStatusChart.value) {
    const chart = echarts.init(taskStatusChart.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: 1048, name: '待接单', itemStyle: { color: '#faad14' } },
          { value: 735, name: '进行中', itemStyle: { color: '#1890ff' } },
          { value: 580, name: '已完成', itemStyle: { color: '#52c41a' } },
          { value: 300, name: '已取消', itemStyle: { color: '#ff4d4f' } }
        ]
      }]
    })
  }

  // 交易趋势图
  if (amountTrendChart.value) {
    const chart = echarts.init(amountTrendChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: { type: 'value' },
      series: [{
        data: [120, 200, 150, 80, 70, 110, 130],
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
          ])
        },
        itemStyle: { color: '#667eea' }
      }]
    })
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
