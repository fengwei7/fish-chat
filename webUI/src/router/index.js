import {createRouter, createWebHashHistory, createWebHistory} from 'vue-router'
import { setupRouterGuard } from './guard'

const routes = [
  // 用户资料页面
  {
    path: '/',
    name: 'UserProfile',
    component: () => import('@/views/user/UserProfile.vue'),
    meta: { requiresAuth: true }
  },
  // UI组件展示页面
  {
    path: '/ui-showcase',
    name: 'UiShowcase',
    component: () => import('@/views/ui/UiShowcase.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫开关
const enableRouterGuard = false
setupRouterGuard(router, enableRouterGuard)

export default router