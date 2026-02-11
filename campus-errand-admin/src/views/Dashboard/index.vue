<template>
  <div class="dashboard">
    <!-- 统计卡片 - 响应式布局 -->
    <a-row :gutter="[16, 16]" class="stat-cards">
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
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
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
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
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #8B5CF6 100%);">
            <ShoppingCartOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">总订单数</div>
            <div class="stat-value">{{ stats.orderCount || 0 }}</div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <a-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #FFA07A 0%, #FFB347 100%);">
            <DollarOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-label">交易总额</div>
            <div class="stat-value">¥{{ (stats.totalAmount || 0).toFixed(2) }}</div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 今日数据 -->
    <a-row :gutter="[16, 16]" class="today-stats">
      <a-col :span="24">
        <a-card title="📊 今日数据" class="today-card">
          <a-row :gutter="[16, 16]">
            <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <div class="today-item">
                <div class="today-icon" style="background: rgba(255, 107, 107, 0.1); color: #FF6B6B;">👤</div>
                <div class="today-info">
                  <div class="today-label">新增用户</div>
                  <div class="today-value">{{ stats.todayUserCount || 0 }}</div>
                </div>
              </div>
            </a-col>
            <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <div class="today-item">
                <div class="today-icon" style="background: rgba(78, 205, 196, 0.1); color: #4ECDC4;">📦</div>
                <div class="today-info">
                  <div class="today-label">新增任务</div>
                  <div class="today-value">{{ stats.todayTaskCount || 0 }}</div>
                </div>
              </div>
            </a-col>
            <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <div class="today-item">
                <div class="today-icon" style="background: rgba(102, 126, 234, 0.1); color: #667eea;">🛒</div>
                <div class="today-info">
                  <div class="today-label">新增订单</div>
                  <div class="today-value">{{ stats.todayOrderCount || 0 }}</div>
                </div>
              </div>
            </a-col>
            <a-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <div class="today-item">
                <div class="today-icon" style="background: rgba(255, 195, 0, 0.1); color: #FFC300;">💰</div>
                <div class="today-info">
                  <div class="today-label">今日交易额</div>
                  <div class="today-value">¥{{ (stats.todayAmount || 0).toFixed(2) }}</div>
                </div>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 - 响应式堆叠 -->
    <a-row :gutter="[16, 16]" class="charts">
      <a-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
        <a-card title="📈 任务状态分布" :loading="chartLoading" class="chart-card">
          <div ref="taskStatusChart" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
        <a-card title="💹 近7天交易趋势" :loading="chartLoading" class="chart-card">
          <div ref="amountTrendChart" class="chart-container"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 用户增长趋势 -->
    <a-row :gutter="[16, 16]" class="charts">
      <a-col :span="24">
        <a-card title="📊 用户增长趋势" :loading="chartLoading" class="chart-card">
          <div ref="userGrowthChart" class="chart-container" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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

// 任务状态颜色映射 - 美团风格
const statusColors: Record<number, string> = {
  0: '#FFC300', // 待接单 - 黄色
  1: '#4ECDC4', // 已接单 - 青色
  2: '#52c41a', // 待取件 - 绿色
  3: '#667eea', // 配送中 - 紫色
  4: '#FF6B6B', // 待确认 - 红色
  5: '#52c41a', // 已完成 - 绿色
  6: '#999999'  // 已取消 - 灰色
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

/* 统计卡片 */
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

/* 今日数据 */
.today-stats {
  margin-bottom: 16px;
}

.today-card {
  border-radius: 12px;
}

.today-card :deep(.ant-card-head) {
  border-bottom: none;
  font-size: 16px;
  font-weight: 600;
}

.today-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 12px;
}

.today-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-right: 10px;
  flex-shrink: 0;
}

.today-info {
  flex: 1;
  min-width: 0;
}

.today-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.today-value {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 图表 */
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

/* 移动端适配 */
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
  
  .today-item {
    padding: 10px;
  }
  
  .today-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
    margin-right: 8px;
  }
  
  .today-label {
    font-size: 11px;
  }
  
  .today-value {
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
  
  .today-item {
    flex-direction: column;
    text-align: center;
  }
  
  .today-icon {
    margin-right: 0;
    margin-bottom: 6px;
  }
  
  .today-info {
    text-align: center;
  }
}
</style>
