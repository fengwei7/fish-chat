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
  },
  {
    path: '/contacts',
    name: 'Contacts',
    meta: {
      isShow: true,
      title: '通讯录',
      roles: ['admin', 'user'],
      isParent: true
    },
    children: [
      {
        path: '/contacts/friends',
        name: 'Friends',
        component: () => import('@/views/friend/Friend.vue'),
        meta: {
          isShow: true,
          title: '好友管理',
          roles: ['admin', 'user']
        }
      },
      {
        path: '/contacts/groups',
        name: 'Groups',
        component: () => import('@/views/group/Group.vue'),
        meta: {
          isShow: true,
          title: '群组管理',
          roles: ['admin', 'user']
        }
      },
      {
        path: '/contacts/channels',
        name: 'Channels',
        component: () => import('@/views/channel/Channel.vue'),
        meta: {
          isShow: true,
          title: '频道管理',
          roles: ['admin', 'user']
        }
      }
    ]
  },
  {
    path: '/user',
    name: 'User',
    meta: {
      isShow: true,
      title: '用户中心',
      roles: ['admin', 'user'],
      isParent: true
    },
    children: [
      {
        path: '/user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Info.vue'),
        meta: {
          isShow: true,
          title: '个人资料',
          roles: ['admin', 'user']
        }
      }
    ]
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