import { useUserStore } from '@/stores/user.js'
import { getPubRoutePaths } from './pubRoutes.js'

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
    // 使用 store 中的 isLoggedIn 检查登录状态（computed）
    const isUserLoggedIn = userStore.isLoggedIn

    // 检查是否为公共路由
    const pubRoutePaths = getPubRoutePaths()
    const isPubRoute = pubRoutePaths.some(route => to.path.startsWith(route))

    if (isPubRoute) {
      // 如果是公共路由，未登录可访问，已登录不可访问
      if (isUserLoggedIn) {
        // 已登录，跳转到首页
        next('/')
      } else {
        // 未登录，允许访问
        next()
      }
    } else {
      // 非公共路由，只有已登录才能访问
      if (isUserLoggedIn) {
        // 已登录，允许访问
        next()
      } else {
        // 未登录，跳转到登录页
        next('/login')
      }
    }
  })
}