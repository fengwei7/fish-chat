<template>
  <div class="chat-message-list" ref="containerRef">
    <!-- 加载更多提示 -->
    <div v-if="loading" class="chat-message-list__loading">
      <FcSkeleton shape="circle" size="24px" />
      <span>加载中...</span>
    </div>

    <!-- 空消息状态 -->
    <div v-if="messages.length === 0 && !loading" class="chat-message-list__empty">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
      </svg>
      <p>暂无消息</p>
    </div>

    <!-- 消息列表 -->
    <div class="chat-message-list__container" @scroll="handleScroll">
      <!-- 消息项 -->
      <div
        v-for="(message, index) in messages"
        :key="message.id || index"
        class="chat-message-list__item"
      >
        <slot name="message" :message="message" :index="index">
          <!-- 默认渲染 -->
          <component
            :is="messageComponent"
            :message="message"
            :is-mine="message.isMine"
            v-bind="$attrs"
          />
        </slot>
      </div>
    </div>

    <!-- 回到底部按钮 -->
    <transition name="fc-fade">
      <button
        v-if="showScrollButton"
        class="chat-message-list__scroll-bottom"
        @click="scrollToBottom"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <polyline points="6 9 12 15 18 9" />
        </svg>
        <span v-if="unreadCount > 0" class="chat-message-list__badge">{{ unreadCount }}</span>
      </button>
    </transition>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, computed } from 'vue'
import { FcSkeleton } from '@/components/ui'

/**
 * ChatMessageList 消息列表组件
 * 
 * @example
 * ```vue
 * <ChatMessageList :messages="messages" @load-more="handleLoadMore">
 *   <template #message="{ message }">
 *     <ChatBubble :message="message" :is-mine="message.isMine" />
 *   </template>
 * </ChatMessageList>
 * ```
 */

const props = defineProps({
  /**
   * 消息列表
   */
  messages: {
    type: Array,
    default: () => []
  },
  
  /**
   * 是否加载中
   */
  loading: {
    type: Boolean,
    default: false
  },
  
  /**
   * 消息组件（可选，用于自定义）
   */
  messageComponent: {
    type: Object,
    default: null
  },
  
  /**
   * 是否自动滚动到底部
   */
  autoScroll: {
    type: Boolean,
    default: true
  },
  
  /**
   * 未读消息阈值（超过多少条显示回到底部按钮）
   */
  unreadThreshold: {
    type: Number,
    default: 5
  }
})

const emit = defineEmits(['load-more', 'scroll'])

const containerRef = ref(null)
const showScrollButton = ref(false)
const unreadCount = ref(0)
let isUserScrolling = false

// 监听消息变化，自动滚动到底部
watch(
  () => props.messages,
  (newMessages, oldMessages) => {
    // 新消息增加时滚动到底部
    if (newMessages.length > oldMessages.length && props.autoScroll) {
      // 如果用户正在查看历史消息，不自动滚动
      if (!isUserScrolling) {
        nextTick(() => {
          scrollToBottom()
        })
      }
    }
  },
  { deep: true }
)

const handleScroll = (event) => {
  const container = event.target
  const scrollTop = container.scrollTop
  const scrollHeight = container.scrollHeight
  const clientHeight = container.clientHeight
  
  // 判断是否滚动到顶部（触发加载更多）
  if (scrollTop < 100 && !props.loading) {
    emit('load-more')
  }
  
  // 判断是否在底部
  const isAtBottom = scrollHeight - scrollTop - clientHeight < 100
  isUserScrolling = !isAtBottom
  
  // 显示/隐藏回到底部按钮
  if (!isAtBottom) {
    // 计算未读消息数（简化版）
    unreadCount.value = Math.max(0, unreadCount.value + 1)
    showScrollButton.value = unreadCount.value >= props.unreadThreshold
  } else {
    unreadCount.value = 0
    showScrollButton.value = false
  }
  
  emit('scroll', {
    scrollTop,
    scrollHeight,
    clientHeight,
    isAtBottom
  })
}

const scrollToBottom = () => {
  if (!containerRef.value) return
  
  const container = containerRef.value.querySelector('.chat-message-list__container')
  if (container) {
    container.scrollTop = container.scrollHeight
    unreadCount.value = 0
    showScrollButton.value = false
    isUserScrolling = false
  }
}

const scrollToMessage = (messageId) => {
  if (!containerRef.value) return
  
  const container = containerRef.value.querySelector('.chat-message-list__container')
  const messageElement = container.querySelector(`[data-message-id="${messageId}"]`)
  
  if (messageElement) {
    messageElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

// 暴露方法给父组件
defineExpose({
  scrollToBottom,
  scrollToMessage
})
</script>

<style scoped>
.chat-message-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--chat-bg);
  position: relative;
  overflow: hidden;
  height: 100%; /* 确保占满父容器高度 */
  min-height: 0; /* Flexbox溢出修复 */
}

/* 加载中 */
.chat-message-list__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  color: var(--fc-gray-500);
  font-size: 14px;
}

/* 空状态 */
.chat-message-list__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--fc-gray-400);
}

.chat-message-list__empty svg {
  width: 64px;
  height: 64px;
  opacity: 0.3;
}

.chat-message-list__empty p {
  font-size: 16px;
  margin: 0;
}

/* 消息容器 */
.chat-message-list__container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden; /* 防止横向溢出 */
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: var(--chat-message-gap);
  min-height: 0; /* Flexbox溢出修复 */
}

.chat-message-list__container::-webkit-scrollbar {
  width: 6px;
}

.chat-message-list__container::-webkit-scrollbar-track {
  background: transparent;
}

.chat-message-list__container::-webkit-scrollbar-thumb {
  background: var(--fc-gray-300);
  border-radius: var(--fc-radius-full);
}

.chat-message-list__container::-webkit-scrollbar-thumb:hover {
  background: var(--fc-gray-400);
}

/* 消息项 */
.chat-message-list__item {
  animation: fc-slide-up var(--fc-transition-fast) var(--fc-easing);
}

/* 回到底部按钮 */
.chat-message-list__scroll-bottom {
  position: absolute;
  bottom: 20px;
  right: 20px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid var(--fc-border);
  border-radius: 50%;
  cursor: pointer;
  box-shadow: var(--fc-shadow-md);
  transition: all var(--fc-transition-fast) var(--fc-easing);
  z-index: 10;
}

.chat-message-list__scroll-bottom:hover {
  background: var(--fc-primary-500);
  color: white;
  border-color: var(--fc-primary-500);
  transform: scale(1.1);
}

.chat-message-list__scroll-bottom svg {
  width: 20px;
  height: 20px;
}

.chat-message-list__badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: var(--fc-error);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: var(--fc-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-message-list__container {
    padding: 12px;
  }
  
  .chat-message-list__scroll-bottom {
    bottom: 12px;
    right: 12px;
    width: 40px;
    height: 40px;
  }
}
</style>
