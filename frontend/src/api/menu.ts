import request from '../utils/request'

export interface MenuItem {
  id: number
  parentId: number | null
  name: string
  path: string
  title?: string // Backend SysMenu has title and icon at root
  icon?: string
  roles?: string // Backend SysMenu has roles as JSON string at root
  meta?: {
    title: string
    icon?: string
    keepAlive?: boolean
    requiresAuth?: boolean
    roles?: string[]
  }
  children?: MenuItem[]
}

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// 模拟后端数据库中的完整菜单列表
const mockMenus: MenuItem[] = [
  {
    id: 1,
    parentId: null,
    name: 'Dashboard',
    path: '/dashboard',
    meta: { title: '系统首页', icon: 'DashboardOutlined', roles: ['student', 'librarian', 'admin', 'guest'] }
  },
  {
    id: 2,
    parentId: null,
    name: 'Seat',
    path: '/seat',
    meta: { title: '座位预约', icon: 'DesktopOutlined', roles: ['student', 'librarian', 'admin', 'guest'] }
  },
  {
    id: 3,
    parentId: null,
    name: 'CheckIn',
    path: '/checkin',
    meta: { title: '座位签到', icon: 'EnvironmentOutlined', roles: ['student', 'librarian', 'admin'] }
  },
  {
    id: 4,
    parentId: null,
    name: 'Square',
    path: '/square',
    meta: { title: '消息广场', icon: 'CommentOutlined', roles: ['student', 'librarian', 'admin'] }
  },
  {
    id: 5,
    parentId: null,
    name: 'Stats',
    path: '/stats',
    meta: { title: '数据统计', icon: 'BarChartOutlined', roles: ['librarian', 'admin'] }
  },
  {
    id: 6,
    parentId: null,
    name: 'System',
    path: '/system',
    meta: { title: '系统管理', icon: 'SettingOutlined', roles: ['admin', 'librarian'] },
    children: [
      {
        id: 7,
        parentId: 6,
        name: 'SystemUser',
        path: '/system/user',
        meta: { title: '用户管理', roles: ['admin'] }
      },
      {
        id: 8,
        parentId: 6,
        name: 'SystemSeat',
        path: '/system/seat',
        meta: { title: '座位管理', roles: ['admin', 'librarian'] }
      },
      {
        id: 9,
        parentId: 6,
        name: 'SystemLog',
        path: '/system/log',
        meta: { title: '系统日志', roles: ['admin'] }
      },
      {
        id: 10,
        parentId: 6,
        name: 'SystemConfig',
        path: '/system/config',
        meta: { title: '系统配置', roles: ['admin'] }
      }
    ]
  }
]

// 获取用户菜单
export const getMenus = async (role: string): Promise<MenuItem[]> => {
  if (USE_MOCK) {
    // 简单的递归过滤函数
    const filterMenus = (menus: MenuItem[]): MenuItem[] => {
      return menus
        .filter(menu => {
          if (!menu.meta?.roles || menu.meta.roles.length === 0) return true
          return menu.meta.roles.includes(role)
        })
        .map(menu => {
          if (menu.children) {
            return { ...menu, children: filterMenus(menu.children) }
          }
          return menu
        })
    }
    return filterMenus(mockMenus)
  }

  const res = await request<MenuItem[]>({
    url: '/menu/list',
    method: 'get'
  })
  
  // 后端返回的是 SysMenu 实体，需要转换或适配前端 meta 结构
  const data = (res as any).data || res
  
  const adaptMenus = (menus: any[]): MenuItem[] => {
    return menus.map(m => {
      // 解析后端返回的 roles JSON 字符串
      let rolesArr: string[] = []
      if (typeof m.roles === 'string' && m.roles.startsWith('[')) {
        try {
          rolesArr = JSON.parse(m.roles)
        } catch (e) {
          console.error('Failed to parse roles JSON:', m.roles)
        }
      } else if (Array.isArray(m.meta?.roles)) {
        rolesArr = m.meta.roles
      }

      return {
        ...m,
        meta: {
          title: m.title || m.meta?.title,
          icon: m.icon || m.meta?.icon,
          roles: rolesArr
        },
        children: m.children ? adaptMenus(m.children) : undefined
      }
    })
  }
  
  return adaptMenus(data)
}
