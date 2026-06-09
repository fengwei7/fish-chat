<template>
  <div class="chat-panel">
    <!-- 头部 -->
    <div class="chat-panel__header">
      <div class="chat-panel__header-info">
        <h3 class="chat-panel__title">{{ title }}</h3>
        <p v-if="subtitle" class="chat-panel__subtitle">{{ subtitle }}</p>
      </div>
      <div class="chat-panel__header-actions">
        <slot name="header-actions"></slot>
      </div>
    </div>

    <!-- 消息列表 -->
    <ChatMessageList
      ref="messageListRef"
      :messages="messages"
      :loading="loading"
      @load-more="handleLoadMore"
      @scroll="handleScroll"
    >
      <template #message="{ message }">
        <ChatBubble
          :message="message"
          :is-mine="message.isMine"
          :show-online-status="showOnlineStatus"
          :show-sender="isGroup"
          @click-avatar="handleClickAvatar"
          @click-bubble="handleClickBubble"
          @click-image="handleClickImage"
          @click-file="handleClickFile"
        />
      </template>
    </ChatMessageList>

    <!-- 输入框 -->
    <ChatInput
      v-model="inputText"
      :sending="sending"
      :placeholder="inputPlaceholder"
      @send="handleSend"
      @emoji="handleEmoji"
      @attach="handleAttach"
      @image="handleImage"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatBubble from '../ChatBubble/ChatBubble.vue'
import ChatMessageList from '../ChatMessageList/ChatMessageList.vue'
import ChatInput from '../ChatInput/ChatInput.vue'

/**
 * ChatPanel 聊天主面板组件
 * 
 * @example
 * ```vue
 * <ChatPanel
 *   :messages="messages"
 *   :title="conversationName"
 *   @send="handleSend"
 * />
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
   * 会话标题
   */
  title: {
    type: String,
    default: ''
  },
  
  /**
   * 会话副标题（在线状态、成员数等）
   */
  subtitle: {
    type: String,
    default: ''
  },
  
  /**
   * 是否加载中
   */
  loading: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否发送中
   */
  sending: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否是群聊
   */
  isGroup: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否显示在线状态
   */
  showOnlineStatus: {
    type: Boolean,
    default: false
  },
  
  /**
   * 输入框占位符
   */
  inputPlaceholder: {
    type: String,
    default: '输入消息...'
  }
})

const emit = defineEmits([
  'send',
  'load-more',
  'scroll',
  'click-avatar',
  'click-bubble',
  'click-image',
  'click-file',
  'emoji',
  'attach',
  'image'
])

const messageListRef = ref(null)
const inputText = ref('')

const handleSend = (text) => {
  emit('send', text)
  inputText.value = ''
}

const handleLoadMore = () => {
  emit('load-more')
}

const handleScroll = (scrollInfo) => {
  emit('scroll', scrollInfo)
}

const handleClickAvatar = (message) => {
  emit('click-avatar', message)
}

const handleClickBubble = (message) => {
  emit('click-bubble', message)
}

const handleClickImage = (message) => {
  emit('click-image', message)
}

const handleClickFile = (message) => {
  emit('click-file', message)
}

const handleEmoji = () => {
  emit('emoji')
}

const handleAttach = () => {
  emit('attach')
}

const handleImage = () => {
  emit('image')
}

// 暴露方法
defineExpose({
  scrollToBottom: () => messageListRef.value?.scrollToBottom(),
  scrollToMessage: (messageId) => messageListRef.value?.scrollToMessage(messageId)
})
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0; /* Flexbox溢出修复 */
  background: white;
  border-radius: var(--fc-radius-lg);
  overflow: hidden;
  box-shadow: var(--fc-shadow-lg);
}

/* 头部 */
.chat-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--chat-header-bg);
  border-bottom: 1px solid var(--fc-border);
}

.chat-panel__title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--fc-gray-900);
}

.chat-panel__subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--fc-gray-500);
}

.chat-panel__header-actions {
  display: flex;
  gap: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-panel {
    border-radius: 0;
  }
  
  .chat-panel__header {
    padding: 12px 16px;
  }
  
  .chat-panel__title {
    font-size: 16px;
  }
}
</style>
