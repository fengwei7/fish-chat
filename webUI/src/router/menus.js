// 菜单配置， 目前只有一层子菜单
const baseMenus = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/index/Index.vue'),
    meta: {
      isShow: true,
      title: '首页',
      roles: ['admin', 'user']
    }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/chat/Chat.vue'),
    meta: {
      isShow: true,
      title: '聊天',
      roles: ['admin', 'user']
    }

  }
]

// 函数：添加路径前缀
export function getMenusWithPrefix(prefix = '') {
  return baseMenus.map(menu => {
    // 处理基础菜单项的路径
    const prefixedMenu = {
      ...menu,
      path: prefix ? `${prefix}${menu.path}` : menu.path
    }

    // 如果有子菜单，也需要添加前缀
    if (menu.children && menu.children.length > 0) {
      prefixedMenu.children = menu.children.map(child => ({
        ...child,
        path: prefix ? `${prefix}${child.path}` : child.path
      }))
    }

    return prefixedMenu
  })
}

// 导出带前缀的菜单路由
export const adminMenusRoutes = getMenusWithPrefix('/admin')