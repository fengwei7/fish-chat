import {createRouter, createWebHashHistory, createWebHistory} from 'vue-router'
import { setupRouterGuard } from './guard'

const routes = [
  // 默认重定向到聊天页面
  {
    path: '/',
    redirect: '/chat'
  },
  // 登录注册页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresAuth: false, title: '登录 - Fish-Chat' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { requiresAuth: false, title: '注册 - Fish-Chat' }
  },
  // 聊天页面（待开发）
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/chat/ChatView.vue'),
    meta: { requiresAuth: true, title: '聊天 - Fish-Chat' }
  },
  // UI组件展示页面
  {
    path: '/ui-showcase',
    name: 'UiShowcase',
    component: () => import('@/views/ui/UiShowcase.vue'),
    meta: { requiresAuth: false, title: 'UI组件库 - Fish-Chat' }
  }
]

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫开关
const enableRouterGuard = true
// const enableRouterGuard = false
setupRouterGuard(router, enableRouterGuard)

export default router