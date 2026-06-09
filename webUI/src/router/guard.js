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

  })
}