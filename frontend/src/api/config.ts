import request from '../utils/request'

export interface SysConfig {
  id: number
  configKey: string
  configValue: string
  configName: string
  updateTime?: string
}

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// Mock Data
const mockConfigs: SysConfig[] = [
  { id: 1, configKey: 'violation_time', configValue: '30', configName: '预约签到截止时间(分钟)' },
  { id: 2, configKey: 'min_credit_score', configValue: '60', configName: '预约所需最低信用分' },
  { id: 3, configKey: 'message_square_enabled', configValue: 'true', configName: '消息广场是否允许发言' },
  { id: 4, configKey: 'library_latitude', configValue: '0', configName: '图书馆纬度' },
  { id: 5, configKey: 'library_longitude', configValue: '0', configName: '图书馆经度' },
  { id: 6, configKey: 'release_buffer_time', configValue: '15', configName: '退座截止时间(分钟)' },
  { id: 7, configKey: 'checkin_before_window', configValue: '15', configName: '预约起始前可签到时间(min)' },
  { id: 8, configKey: 'checkin_after_window', configValue: '15', configName: '预约起始后可签到时间(min)' },
  { id: 9, configKey: 'late_reservation_grace_period', configValue: '5', configName: '过时预约签到宽限时间(min)' },
  { id: 10, configKey: 'occupancy_check_interval', configValue: '5', configName: '占座检测间隔(分钟)' },
  { id: 11, configKey: 'occupancy_threshold', configValue: '60', configName: '占座判定阈值(分钟)' },
  { id: 12, configKey: 'occupancy_warning_time', configValue: '45', configName: '占座预警时间(分钟)' },
  { id: 13, configKey: 'max_away_time', configValue: '120', configName: '单次最大离开时间(分钟)' },
  { id: 14, configKey: 'closing_time', configValue: '22:00', configName: '图书馆闭馆时间' },
  { id: 15, configKey: 'closing_reminder_minutes', configValue: '30', configName: '闭馆提醒提前时间(分钟)' },
  { id: 16, configKey: 'occupancy_credit_deduct', configValue: '15', configName: '占座违规扣分' }
]

export function getConfigs() {
  if (USE_MOCK) {
    return Promise.resolve(mockConfigs)
  }
  return request<SysConfig[]>({
    url: '/configs/list',
    method: 'get'
  })
}

export function updateConfig(data: SysConfig) {
  if (USE_MOCK) {
    const index = mockConfigs.findIndex(c => c.id === data.id)
    if (index > -1) {
      mockConfigs[index] = { ...data, updateTime: new Date().toISOString() }
    }
    return Promise.resolve(true)
  }
  return request<boolean>({
    url: '/configs',
    method: 'put',
    data
  })
}
