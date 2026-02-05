<template>
  <div class="dashboard-container">
    <div class="quick-nav-buttons">
      <div class="jelly-button reservation" @click="router.push('/seat')">
        <div class="icon-wrapper">
          <video-camera-outlined />
        </div>
        <div class="text-content">
          <span class="title">座位预约</span>
          <span class="desc">预定您的专属座位</span>
        </div>
      </div>
      <div class="jelly-button checkin" @click="router.push('/checkin')">
        <div class="icon-wrapper">
          <check-circle-outlined />
        </div>
        <div class="text-content">
          <span class="title">打卡签到</span>
          <span class="desc">快速完成入座签到</span>
        </div>
      </div>
    </div>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :sm="24" :md="8">
        <a-card>
          <a-statistic title="总座位数" :value="stats.totalSeats" style="margin-right: 50px">
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="24" :md="8">
        <a-card>
          <a-statistic title="空闲" :value="stats.available" :value-style="{ color: '#3f8600' }">
            <template #suffix>
              <check-circle-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="24" :md="8">
        <a-card>
          <a-statistic title="使用中" :value="stats.occupied" :value-style="{ color: '#cf1322' }">
            <template #suffix>
              <close-circle-outlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 24px">
      <a-col :xs="24" :md="12">
        <a-card title="实时座位利用率" :bordered="false">
          <base-chart :options="usageChartOption" height="300px" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card title="近7日预约趋势" :bordered="false">
          <base-chart :options="trendChartOption" height="300px" />
        </a-card>
      </a-col>
    </a-row>

    <a-card title="近期动态" style="margin-top: 24px" :bordered="false">
      <a-list item-layout="horizontal" :data-source="activities">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta
              :description="item.time"
            >
              <template #title>
                <a href="javascript:;">{{ item.title }}</a>
              </template>
              <template #avatar>
                <a-avatar :style="{ backgroundColor: item.color }">
                  <template #icon>
                    <component :is="item.icon" />
                  </template>
                </a-avatar>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  VideoCameraOutlined,
  HistoryOutlined,
  ClockCircleOutlined,
  WarningOutlined
} from '@ant-design/icons-vue'
import BaseChart from '../components/Chart/BaseChart.vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import request from '../utils/request'
import { getDashboardStats } from '../api/stats'
import { wsService } from '../utils/websocket'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

const stats = ref({
  totalSeats: 0,
  available: 0,
  occupied: 0,
  maintenance: 0,
  trend: {
    dates: [] as string[],
    counts: [] as number[]
  }
})

onMounted(async () => {
  try {
    const data = await getDashboardStats()
    stats.value = (data as any).data || data
    await fetchActivities()
    
    // 监听实时统计更新
    wsService.on('stats_update', (newStats: any) => {
      stats.value = newStats
    })
  } catch (error) {
    console.error(error)
  }
})

onUnmounted(() => {
  wsService.off('stats_update', () => {})
})

const usageChartOption = computed(() => ({
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: '5%',
    left: 'center'
  },
  series: [
    {
      name: '座位状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 40,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: stats.value.available, name: '空闲', itemStyle: { color: '#3f8600' } },
        { value: stats.value.occupied, name: '使用中', itemStyle: { color: '#cf1322' } },
        { value: stats.value.maintenance, name: '维修中', itemStyle: { color: '#faad14' } }
      ]
    }
  ]
}))

const trendChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: stats.value.trend.dates
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '预约人数',
      type: 'line',
      stack: 'Total',
      smooth: true,
      lineStyle: { width: 3, color: '#1890ff' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(24,144,255,0.3)' },
            { offset: 1, color: 'rgba(24,144,255,0.01)' }
          ]
        }
      },
      data: stats.value.trend.counts
    }
  ]
}))

const activities = ref<any[]>([])

const fetchActivities = async () => {
  try {
    const res = await request<any>({
      url: '/logs/list',
      method: 'get',
      params: { page: 1, size: 5 }
    })
    const data = res.data || res
    activities.value = data.records.map((log: any) => {
      let icon = HistoryOutlined
      let color = '#108ee9'
      
      if (log.operation.includes('预约') && !log.operation.includes('违规')) {
        icon = CheckCircleOutlined
        color = '#87d068'
      } else if (log.operation.includes('签到')) {
        icon = CheckCircleOutlined
        color = '#52c41a'
      } else if (log.operation.includes('取消') || log.operation.includes('删除')) {
        icon = CloseCircleOutlined
        color = '#f50'
      } else if (log.operation.includes('违规')) {
        icon = WarningOutlined
        color = '#fadb14'
      } else if (log.operation.includes('释放')) {
        icon = ClockCircleOutlined
        color = '#2db7f5'
      }

      return {
        title: `${log.username} ${log.operation}`,
        time: dayjs(log.createTime).fromNow(),
        icon,
        color
      }
    })
  } catch (e) {}
}
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.quick-nav-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
  padding: 15px 10px; /* 增加更多内边距 */
}

.jelly-button {
  display: flex;
  align-items: center;
  padding: 24px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  color: white;
  position: relative;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.jelly-button.reservation {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.jelly-button.checkin {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.jelly-button:hover {
  transform: scale(1.02) translateY(-3px); /* 减小缩放和位移幅度 */
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.jelly-button:active {
  transform: scale(0.95);
}

.icon-wrapper {
  font-size: 36px;
  margin-right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  width: 64px;
  height: 64px;
  border-radius: 18px;
}

.text-content {
  display: flex;
  flex-direction: column;
}

.text-content .title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.text-content .desc {
  font-size: 14px;
  opacity: 0.8;
}

@media (max-width: 768px) {
  .quick-nav-buttons {
    grid-template-columns: 1fr;
  }
}
</style>
