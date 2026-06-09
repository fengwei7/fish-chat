// 公共路由配置
export const pubRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: {
      requiresAuth: false
    }
  }
]

// 公共路由路径，未登录可访问，已登录不可访问
export const getPubRoutePaths = () => {
  return pubRoutes.map(route => route.path)
}