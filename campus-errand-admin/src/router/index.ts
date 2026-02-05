import { createRouter, createWebHistory } from 'vue-router'
import { message } from 'ant-design-vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '数据仪表盘', icon: 'DashboardOutlined' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User/index.vue'),
        meta: { title: '用户管理', icon: 'UserOutlined' }
      },
      {
        path: 'task',
        name: 'Task',
        component: () => import('@/views/Task/index.vue'),
        meta: { title: '任务管理', icon: 'FileTextOutlined' }
      },
      {
        path: 'wallet',
        name: 'Wallet',
        component: () => import('@/views/Wallet/index.vue'),
        meta: { title: '钱包管理', icon: 'WalletOutlined' }
      },
      {
        path: 'evaluation',
        name: 'Evaluation',
        component: () => import('@/views/Evaluation/index.vue'),
        meta: { title: '评价管理', icon: 'StarOutlined' }
      },
      {
        path: 'setting',
        name: 'Setting',
        component: () => import('@/views/Setting/index.vue'),
        meta: { title: '系统设置', icon: 'SettingOutlined' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  
  if (to.meta.public) {
    next()
  } else if (!token) {
    message.error('请先登录')
    next('/login')
  } else {
    next()
  }
})

export default router
