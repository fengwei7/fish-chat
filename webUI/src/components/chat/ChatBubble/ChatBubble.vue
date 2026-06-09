<template>
  <div class="chat-bubble" :class="{ 'chat-bubble--mine': isMine }">
    <!-- 头像 -->
    <div v-if="!isMine" class="chat-bubble__avatar" @click="handleClickAvatar">
      <FcAvatar 
        :src="message.avatar" 
        size="md"
        :status="message.onlineStatus"
        :show-status="showOnlineStatus"
      />
    </div>

    <!-- 消息内容 -->
    <div class="chat-bubble__content">
      <!-- 发送者名称（群聊显示） -->
      <div v-if="showSender" class="chat-bubble__sender">
        {{ message.senderName }}
      </div>

      <!-- 气泡 -->
      <div class="chat-bubble__bubble" @click="handleClickBubble">
        <!-- 文本消息 -->
        <div v-if="message.type === 'text'" class="chat-bubble__text">
          {{ message.content }}
        </div>

        <!-- 图片消息 -->
        <div v-else-if="message.type === 'image'" class="chat-bubble__image">
          <img :src="message.imageUrl" :alt="message.content" @click="handleImageClick" />
        </div>

        <!-- 文件消息 -->
        <div v-else-if="message.type === 'file'" class="chat-bubble__file" @click="handleFileClick">
          <svg class="chat-bubble__file-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
          </svg>
          <div class="chat-bubble__file-info">
            <div class="chat-bubble__file-name">{{ message.fileName }}</div>
            <div class="chat-bubble__file-size">{{ message.fileSize }}</div>
          </div>
        </div>

        <!-- 系统消息 -->
        <div v-else-if="message.type === 'system'" class="chat-bubble__system">
          {{ message.content }}
        </div>
      </div>

      <!-- 时间和状态 -->
      <div class="chat-bubble__meta">
        <span class="chat-bubble__time">{{ formattedTime }}</span>
        <svg v-if="isMine && message.status === 'sent'" class="chat-bubble__status" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <polyline points="20 6 9 17 4 12" />
        </svg>
      </div>
    </div>

    <!-- 自己的头像（右侧） -->
    <div v-if="isMine" class="chat-bubble__avatar" @click="handleClickAvatar">
      <FcAvatar 
        :src="message.avatar" 
        size="md"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { FcAvatar } from '@/components/ui'

/**
 * ChatBubble 消息气泡组件
 * 
 * @example
 * ```vue
 * <ChatBubble :message="msg" :is-mine="true" />
 * ```
 */

const props = defineProps({
  /**
   * 消息对象
   */
  message: {
    type: Object,
    required: true
  },
  
  /**
   * 是否是自己发送的消息
   */
  isMine: {
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
   * 是否显示发送者名称（群聊）
   */
  showSender: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click-avatar', 'click-bubble', 'click-image', 'click-file'])

// 格式化时间
const formattedTime = computed(() => {
  if (!props.message.timestamp) return ''
  
  const date = new Date(props.message.timestamp)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
})

const handleClickAvatar = () => {
  emit('click-avatar', props.message)
}

const handleClickBubble = () => {
  emit('click-bubble', props.message)
}

const handleImageClick = () => {
  emit('click-image', props.message)
}

const handleFileClick = () => {
  emit('click-file', props.message)
}
</script>

<style scoped>
.chat-bubble {
  display: flex;
  gap: var(--chat-message-gap);
  align-items: flex-start;
  animation: fc-slide-up var(--fc-transition-fast) var(--fc-easing);
}

.chat-bubble--mine {
  justify-content: flex-end;
}

/* 头像 */
.chat-bubble__avatar {
  flex-shrink: 0;
  cursor: pointer;
}

/* 内容区 */
.chat-bubble__content {
  max-width: var(--chat-bubble-max-width);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 发送者名称 */
.chat-bubble__sender {
  font-size: 12px;
  color: var(--fc-gray-500);
  padding: 0 4px;
}

/* 气泡 */
.chat-bubble__bubble {
  padding: var(--chat-bubble-padding);
  border-radius: var(--chat-bubble-radius);
  cursor: pointer;
  transition: all var(--fc-transition-fast) var(--fc-easing);
}

.chat-bubble__bubble:hover {
  transform: translateY(-1px);
  box-shadow: var(--fc-shadow-md);
}

/* 我的消息 - 蓝色背景 */
.chat-bubble--mine .chat-bubble__bubble {
  background: var(--chat-bubble-mine);
  color: var(--chat-bubble-text-mine);
  border-top-right-radius: 4px;
}

/* 对方的消息 - 白色背景 */
.chat-bubble:not(.chat-bubble--mine) .chat-bubble__bubble {
  background: var(--chat-bubble-other);
  color: var(--chat-bubble-text-other);
  border-top-left-radius: 4px;
  box-shadow: var(--fc-shadow-sm);
}

/* 文本消息 */
.chat-bubble__text {
  font-size: var(--fc-text-base);
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

/* 图片消息 */
.chat-bubble__image {
  border-radius: 8px;
  overflow: hidden;
  max-width: 300px;
}

.chat-bubble__image img {
  width: 100%;
  height: auto;
  display: block;
  cursor: pointer;
  transition: transform var(--fc-transition-fast) var(--fc-easing);
}

.chat-bubble__image img:hover {
  transform: scale(1.02);
}

/* 文件消息 */
.chat-bubble__file {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  cursor: pointer;
  transition: background var(--fc-transition-fast) var(--fc-easing);
}

.chat-bubble__file:hover {
  background: rgba(0, 0, 0, 0.08);
}

.chat-bubble__file-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  opacity: 0.7;
}

.chat-bubble__file-info {
  flex: 1;
  min-width: 0;
}

.chat-bubble__file-name {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-bubble__file-size {
  font-size: 12px;
  opacity: 0.6;
  margin-top: 2px;
}

/* 系统消息 */
.chat-bubble__system {
  font-size: 13px;
  color: var(--fc-gray-500);
  text-align: center;
  padding: 8px 16px;
}

/* 时间和状态 */
.chat-bubble__meta {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 4px;
}

.chat-bubble--mine .chat-bubble__meta {
  justify-content: flex-end;
}

.chat-bubble__time {
  font-size: var(--chat-time-font-size);
  color: var(--fc-gray-400);
}

.chat-bubble__status {
  width: 14px;
  height: 14px;
  color: var(--fc-primary-500);
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-bubble__content {
    max-width: calc(100% - 60px);
  }
  
  .chat-bubble__image {
    max-width: 200px;
  }
}
</style>
