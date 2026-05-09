<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo">
        <h2>后台管理</h2>
      </div>
      <nav class="menu">
        <div 
          v-for="menu in sidebarMenus" 
          :key="menu.path"
          class="menu-item"
        >
          <!-- 父级菜单项 -->
          <div 
            v-if="menu.meta.isParent && menu.children && menu.children.length > 0"
            class="parent-menu"
          >
            <div class="menu-title" @click="toggleMenu(menu.path)">
              <span>{{ menu.meta.title }}</span>
              <i :class="{'arrow': true, 'expanded': expandedMenus.includes(menu.path)}">▼</i>
            </div>
            <!-- 子菜单 -->
            <div 
              v-show="expandedMenus.includes(menu.path)"
              class="sub-menu"
            >
              <router-link
                v-for="child in menu.children"
                :key="child.path"
                :to="child.path"
                class="sub-menu-item"
                :class="{ active: isActiveRoute(child.path) }"
              >
                {{ child.meta.title }}
              </router-link>
            </div>
          </div>
          
          <!-- 单独菜单项 -->
          <router-link
            v-else
            :to="menu.path"
            class="single-menu-item"
            :class="{ active: isActiveRoute(menu.path) }"
          >
            {{ menu.meta.title }}
          </router-link>
        </div>
      </nav>
    </aside>

    <!-- 主内容区域 -->
    <main class="main-content">
      <header class="admin-header">
        <h1>{{ currentPageTitle }}</h1>
        <div class="header-actions">
          <div class="user-info">
            <img v-if="userStore.userBaseInfo.avatar" :src="userStore.userBaseInfo.avatar" alt="avatar" class="avatar">
            <span class="user-name">{{ userStore.userBaseInfo.name || '-' }}</span>
          </div>
          <button class="logout-btn" @click="logout">退出</button>
        </div>
      </header>
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminMenusRoutes } from '@/router/menus.js'
import { useUserStore } from '@/stores/user.js'
import { logout as logoutApi } from '@/api/auth.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 展开的菜单项
const expandedMenus = ref([])

// 获取带前缀的菜单
const prefixedMenus = adminMenusRoutes

// 过滤出需要显示的菜单项
const sidebarMenus = computed(() => {
  return prefixedMenus.filter(menu => 
    menu.path !== '/admin' && 
    menu.meta.isShow
  )
})

// 当前页面标题
const currentPageTitle = computed(() => {
  for (const menu of prefixedMenus) {
    if (route.path === menu.path) {
      return menu.meta.title
    }
    
    if (menu.children) {
      for (const child of menu.children) {
        if (route.path === child.path) {
          return child.meta.title
        }
      }
    }
  }
  
  return '后台管理'
})

// 检查当前路由是否激活
const isActiveRoute = (path) => {
  return route.path === path
}

// 切换菜单展开/收起
const toggleMenu = (menuPath) => {
  const index = expandedMenus.value.indexOf(menuPath)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(menuPath)
  }
}

// 退出登录
const logout = async () => {
  try {
    await logoutApi()
  } catch (e) {
    // ignore
  }
  userStore.clearUserInfo()
  router.push('/login')
}

// 初始化时展开当前路由对应的菜单
onMounted(() => {
  for (const menu of sidebarMenus.value) {
    if (menu.children) {
      for (const child of menu.children) {
        if (isActiveRoute(child.path)) {
          expandedMenus.value.push(menu.path)
          break
        }
      }
    }
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: white;
  padding: 20px 0;
  height: 100vh;
  position: fixed;
  overflow-y: auto;
}

.sidebar .logo {
  padding: 0 20px 20px;
  border-bottom: 1px solid #34495e;
  margin-bottom: 20px;
}

.sidebar .logo h2 {
  margin: 0;
  font-size: 1.2em;
  color: white;
}

.menu {
  padding: 0;
  margin: 0;
}

.menu-item {
  list-style: none;
  margin-bottom: 5px;
}

.parent-menu {
  cursor: pointer;
}

.menu-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  color: #bdc3c7;
  text-decoration: none;
  transition: background-color 0.3s;
  cursor: pointer;
}

.menu-title:hover {
  background-color: #34495e;
}

.arrow {
  transition: transform 0.3s;
  font-size: 0.8em;
}

.arrow.expanded {
  transform: rotate(180deg);
}

.sub-menu {
  padding-left: 20px;
  background-color: #34495e;
}

.sub-menu-item {
  display: block;
  color: #95a5a6;
  text-decoration: none;
  padding: 10px 20px;
  margin: 5px 0;
  transition: background-color 0.3s;
}

.sub-menu-item:hover,
.sub-menu-item.active {
  background-color: #3d566e;
  color: #fff;
}

.single-menu-item {
  display: block;
  color: #bdc3c7;
  text-decoration: none;
  padding: 12px 20px;
  transition: background-color 0.3s;
}

.single-menu-item:hover,
.single-menu-item.active {
  background-color: #34495e;
  color: #fff;
}

.main-content {
  flex: 1;
  margin-left: 250px;
  display: flex;
  flex-direction: column;
}

.admin-header {
  padding: 20px;
  border-bottom: 1px solid #ddd;
  background-color: #f8f9fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-header h1 {
  margin: 0;
  font-size: 1.5rem;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  color: #555;
}

.logout-btn {
  padding: 8px 16px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.logout-btn:hover {
  background-color: #c0392b;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  background-color: #ecf0f1;
  min-height: calc(100vh - 80px);
}
</style>