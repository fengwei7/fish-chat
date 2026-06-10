<template>
  <div class="chat-layout">
    <!-- 左侧：会话列表 -->
    <div
      class="chat-layout__sidebar"
      :class="{ 'chat-layout__sidebar--hidden': !showSidebar }"
    >
      <div class="chat-layout__header">
        <h2 class="chat-layout__title">Fish-Chat</h2>
        <div class="chat-layout__user" @click="showProfile = true">
          <FcAvatar
            :src="userStore.userInfo?.avatarUrl"
            :size="32"
            :name="userStore.userInfo?.nickname || userStore.userInfo?.username"
          />
          <span class="chat-layout__username">
            {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}
          </span>
        </div>
      </div>
      <div class="chat-layout__sidebar-content">
        <slot name="sidebar"></slot>
      </div>
    </div>

    <!-- 右侧：聊天区域 -->
    <div class="chat-layout__main">
      <!-- 移动端返回按钮 -->
      <button
        v-if="isMobile && !showSidebar"
        class="chat-layout__back"
        @click="showSidebar = true"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
        返回
      </button>
      <slot name="main"></slot>
    </div>

    <!-- 个人资料抽屉 -->
    <ProfileDrawer v-model="showProfile" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { FcAvatar } from '@/components/ui/index.js'
import ProfileDrawer from '@/components/chat/ProfileDrawer/ProfileDrawer.vue'

const userStore = useUserStore()

// 响应式状态
const isMobile = ref(false)
const showSidebar = ref(true)
const showProfile = ref(false) // 显示个人资料抽屉

// 检测屏幕尺寸
function checkMobile() {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    showSidebar.value = true
  }
}

// 切换侧边栏
function toggleSidebar() {
  showSidebar.value = !showSidebar.value
}

// 监听屏幕尺寸变化
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// 暴露方法给父组件
defineExpose({ toggleSidebar, showSidebar })
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background: var(--fc-bg-primary);
  padding: 16px;
  gap: 16px;
}

/* 左侧边栏 */
.chat-layout__sidebar {
  width: 380px;
  flex-shrink: 0;
  background: var(--fc-bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--fc-border);
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease;
  overflow: hidden;
}

.chat-layout__sidebar--hidden {
  transform: translateX(-100%);
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  z-index: 10;
}

/* 顶部用户信息 */
.chat-layout__header {
  padding: 16px;
  border-bottom: 1px solid var(--fc-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-layout__title {
  margin: 0;
  font-size: var(--fc-text-lg);
  font-weight: 600;
  color: var(--fc-primary);
}

.chat-layout__user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-layout__username {
  font-size: var(--fc-text-sm);
  color: var(--fc-text-primary);
}

/* 侧边栏内容 */
.chat-layout__sidebar-content {
  flex: 1;
  overflow-y: auto;
}

/* 主聊天区域 */
.chat-layout__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  position: relative;
  background: var(--fc-bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--fc-border);
  overflow: hidden;
}

/* 移动端返回按钮 */
.chat-layout__back {
  display: none;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: transparent;
  border: none;
  color: var(--fc-primary);
  font-size: var(--fc-text-sm);
  cursor: pointer;
  border-bottom: 1px solid var(--fc-border);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .chat-layout {
    padding: 0;
    gap: 0;
  }

  .chat-layout__sidebar {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 100%;
    z-index: 10;
    border-radius: 0;
    border: none;
  }

  .chat-layout__sidebar--hidden {
    transform: translateX(-100%);
  }

  .chat-layout__back {
    display: flex;
  }

  .chat-layout__main {
    width: 100%;
    border-radius: 0;
    border: none;
  }
}
</style>
