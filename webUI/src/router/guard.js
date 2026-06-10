import { useUserStore } from '@/stores/user.js'

/**
 * 路由守卫配置
 * @param {Object} router - Vue Router 实例
 * @param {Boolean} enableGuard - 是否启用路由守卫，默认为 true
 */
export function setupRouterGuard(router, enableGuard = true) {
  if (!enableGuard) {
    // 如果禁用守卫，则直接返回
    return
  }

  router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()

    // 动态设置页面标题
    if (to.meta.title) {
      // console.log('to.meta.title', to.meta.title)
      document.title = to.meta.title
    } else {
      document.title = 'Fish-Chat'
    }

    // 需要登录的页面
    if (to.meta.requiresAuth) {
      if (!userStore.isLogin) {
        // 未登录，重定向到登录页
        return next({
          path: '/login',
          query: { redirect: to.fullPath } // 保存目标路由，登录后可跳回
        })
      }
    }

    // 已登录用户访问登录/注册页，重定向到聊天页
    if (to.path === '/login' || to.path === '/register') {
      if (userStore.isLogin) {
        return next('/chat')
      }
    }

    next()
  })
}