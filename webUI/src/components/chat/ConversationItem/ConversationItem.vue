<template>
  <div
    class="conversation-item"
    :class="{ 'conversation-item--active': active }"
    @click="$emit('click', conversation)"
    @contextmenu.prevent="handleContextMenu"
  >
    <!-- 头像 -->
    <div class="conversation-item__avatar">
      <FcAvatar
        :src="avatarUrl"
        :size="48"
        :name="displayName"
      />
      <!-- 在线状态指示器 -->
      <span
        v-if="online"
        class="conversation-item__online"
      ></span>
    </div>

    <!-- 内容区域 -->
    <div class="conversation-item__content">
      <!-- 标题和时间 -->
      <div class="conversation-item__header">
        <h3 class="conversation-item__title">{{ displayName }}</h3>
        <span class="conversation-item__time">{{ formatTime(lastMessageTime) }}</span>
      </div>

      <!-- 最后消息预览 -->
      <div class="conversation-item__preview">
        <span class="conversation-item__message">
          <IconText v-if="lastMessageType === 'TEXT'" />
          <IconImage v-else-if="lastMessageType === 'IMAGE'" />
          <IconFile v-else-if="lastMessageType === 'FILE'" />
          {{ lastMessagePreview }}
        </span>
        <!-- 未读数徽章 -->
        <FcBadge
          v-if="unreadCount > 0"
          :count="unreadCount"
          :max="99"
        />
      </div>
    </div>

    <!-- 右键菜单 -->
    <div
      v-if="showMenu"
      class="conversation-item__menu"
      :style="{ top: menuPosition.y + 'px', left: menuPosition.x + 'px' }"
      @click.stop
    >
      <div class="conversation-item__menu-item" @click="handleDelete">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="3 6 5 6 21 6"/>
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
        </svg>
        删除会话
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { FcAvatar, FcBadge } from '@/components/ui/index.js'
import { Message } from '@/components/ui/index.js'

// 图标组件（内联 SVG）
const IconText = {
  template: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`
}
const IconImage = {
  template: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>`
}
const IconFile = {
  template: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>`
}

const props = defineProps({
  conversation: {
    type: Object,
    required: true
  },
  active: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'delete'])

// 右键菜单状态
const showMenu = ref(false)
const menuPosition = ref({ x: 0, y: 0 })

// 处理右键菜单
function handleContextMenu(event) {
  event.preventDefault()
  menuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  showMenu.value = true
}

// 处理删除
function handleDelete() {
  showMenu.value = false
  
  // 确认删除
  if (confirm('确定要删除这个会话吗？')) {
    emit('delete', props.conversation)
  }
}

// 点击其他地方关闭菜单
function handleClickOutside(event) {
  if (showMenu.value) {
    showMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 计算属性
const displayName = computed(() => {
  return props.conversation.nickname || props.conversation.name || '未知会话'
})

const avatarUrl = computed(() => {
  return props.conversation.avatarUrl || ''
})

const lastMessagePreview = computed(() => {
  return props.conversation.lastMessage || '暂无消息'
})

const lastMessageType = computed(() => {
  return props.conversation.lastMessageType || 'TEXT'
})

const lastMessageTime = computed(() => {
  return props.conversation.lastMessageTime
})

const unreadCount = computed(() => {
  return props.conversation.unreadCount || 0
})

const online = computed(() => {
  return props.conversation.online || false
})

// 工具函数
function formatTime(timestamp) {
  if (!timestamp) return ''
  
  const now = Date.now()
  const diff = now - timestamp
  
  // 今天
  if (diff < 24 * 60 * 60 * 1000) {
    const date = new Date(timestamp)
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 昨天
  if (diff < 48 * 60 * 60 * 1000) {
    return '昨天'
  }
  
  // 更早
  const date = new Date(timestamp)
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<style scoped>
.conversation-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: var(--fc-radius-md);
  margin: 4px 8px;
}

.conversation-item:hover {
  background: var(--fc-bg-hover);
}

.conversation-item--active {
  background: var(--fc-primary-light);
}

.conversation-item--active:hover {
  background: var(--fc-primary-light-hover);
}

/* 头像区域 */
.conversation-item__avatar {
  position: relative;
  flex-shrink: 0;
  margin-right: 12px;
}

.conversation-item__online {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background: var(--fc-success);
  border: 2px solid var(--fc-bg-primary);
  border-radius: 50%;
}

/* 内容区域 */
.conversation-item__content {
  flex: 1;
  min-width: 0;
}

/* 标题和时间 */
.conversation-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-item__title {
  font-size: var(--fc-text-base);
  font-weight: 500;
  color: var(--fc-text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.conversation-item__time {
  font-size: var(--fc-text-xs);
  color: var(--fc-text-secondary);
  margin-left: 8px;
  flex-shrink: 0;
}

/* 消息预览和未读数 */
.conversation-item__preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.conversation-item__message {
  font-size: var(--fc-text-sm);
  color: var(--fc-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
}

.conversation-item__message svg {
  flex-shrink: 0;
  opacity: 0.6;
}

/* 右键菜单 */
.conversation-item__menu {
  position: fixed;
  background: var(--fc-bg-primary);
  border: 1px solid var(--fc-border);
  border-radius: var(--fc-radius-md);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  min-width: 120px;
  z-index: 1000;
}

.conversation-item__menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  font-size: var(--fc-text-sm);
  color: var(--fc-error);
  cursor: pointer;
  transition: background 0.2s;
}

.conversation-item__menu-item:hover {
  background: var(--fc-bg-hover);
}

.conversation-item__menu-item svg {
  flex-shrink: 0;
}
</style>
