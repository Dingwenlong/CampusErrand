import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '../stores/user'
import BasicLayout from '../layouts/BasicLayout.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: BasicLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Home.vue'),
        meta: { title: '系统首页', roles: ['student', 'librarian', 'admin', 'guest'] }
      },
      {
        path: 'seat',
        name: 'Seat',
        component: () => import('../views/Seat.vue'),
        meta: { title: '座位预约', roles: ['student', 'librarian', 'admin', 'guest'] }
      },
      {
        path: 'checkin',
        name: 'CheckIn',
        component: () => import('../views/CheckIn.vue'),
        meta: { title: '座位签到', roles: ['student', 'librarian', 'admin'] }
      },
      {
        path: 'square',
        name: 'Square',
        component: () => import('../views/Square/MessageSquare.vue'),
        meta: { title: '消息广场', roles: ['student', 'librarian', 'admin', 'guest'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        redirect: '/profile/info',
        meta: { title: '个人中心', roles: ['student', 'librarian', 'admin'] },
        children: [
          {
            path: 'info',
            name: 'ProfileInfo',
            component: () => import('../views/Profile/Info.vue'),
            meta: { title: '个人信息配置', roles: ['student', 'librarian', 'admin'] }
          },
          {
            path: 'history',
            name: 'ProfileHistory',
            component: () => import('../views/Profile/History.vue'),
            meta: { title: '预约记录', roles: ['student', 'librarian', 'admin'] }
          },
          {
            path: 'appeal',
            name: 'ProfileAppeal',
            component: () => import('../views/Profile/Appeal.vue'),
            meta: { title: '违规申诉', roles: ['student', 'librarian', 'admin'] }
          }
        ]
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/Stats.vue'),
        meta: { title: '数据统计', roles: ['librarian', 'admin'] }
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/system/seat',
        meta: { title: '系统管理', roles: ['admin', 'librarian'] },
        children: [
          {
            path: 'user',
            name: 'SystemUser',
            component: () => import('../views/System/User.vue'),
            meta: { title: '用户管理', roles: ['admin'] }
          },
          {
            path: 'seat',
            name: 'SystemSeat',
            component: () => import('../views/System/SeatManage.vue'),
            meta: { title: '座位管理', roles: ['admin', 'librarian'] }
          },
          {
            path: 'log',
            name: 'SystemLog',
            component: () => import('../views/System/Log.vue'),
            meta: { title: '系统日志', roles: ['admin'] }
          },
          {
            path: 'config',
            name: 'SystemConfig',
            component: () => import('../views/System/Config.vue'),
            meta: { title: '系统配置', roles: ['admin'] }
          },
          {
            path: 'occupancy',
            name: 'SystemOccupancy',
            component: () => import('../views/System/OccupancyMonitor.vue'),
            meta: { title: '占座监控', roles: ['admin', 'librarian'] }
          },
          {
            path: 'appeal',
            name: 'SystemAppeal',
            component: () => import('../views/System/AppealManage.vue'),
            meta: { title: '申诉审核', roles: ['admin', 'librarian'] }
          }
        ]
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/Forbidden.vue')
  },
  // Catch all -> 404 (这里简单跳回首页或404)
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  
  // 1. 未登录
  if (!token) {
    if (to.path === '/login') {
      next()
    } else {
      next('/login')
    }
    return
  }

  // 2. 已登录但访问登录页
  if (to.path === '/login') {
    next('/')
    return
  }

  // 3. 恢复菜单数据（刷新页面情况）
  if (userStore.menus.length === 0 && userStore.userInfo) {
    await userStore.fetchMenus()
  }

  // 4. 新用户强制绑定信息
  if (userStore.isNewUser && to.path !== '/profile/info') {
    next('/profile/info')
    return
  }

  // 5. 权限校验
  if (to.meta.roles) {
    const userRole = userStore.userInfo?.role || ''
    const allowedRoles = to.meta.roles as string[]
    if (allowedRoles.includes(userRole)) {
      next()
    } else {
      next('/403')
    }
  } else {
    // 没有定义 roles 的路由，默认放行 (或者默认拦截，取决于策略，这里放行)
    next()
  }
})

export default router
