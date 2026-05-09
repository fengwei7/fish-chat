import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuard } from './guard'
import { adminMenusRoutes } from '@/router/menus.js'
import { pubRoutes } from '@/router/pubRoutes.js'

const routes = [
  {
    path: '/',
    redirect: '/admin'
  },
  // 后台布局路由
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: adminMenusRoutes
  },
  // 公共路由
  ...pubRoutes
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫开关
const enableRouterGuard = true
setupRouterGuard(router, enableRouterGuard)

export default router