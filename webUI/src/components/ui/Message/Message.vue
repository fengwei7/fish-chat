<template>
  <Teleport to="body">
    <transition-group name="fc-message" tag="div" class="fc-message-container">
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="fc-message"
        :class="[
          `fc-message--${msg.type}`,
          { 'fc-message--closable': msg.showClose }
        ]"
        :style="{ top: msg.offset + 'px' }"
      >
        <!-- 图标 -->
        <div class="fc-message__icon">
          <svg v-if="msg.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
          <svg v-else-if="msg.type === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <circle cx="12" cy="12" r="10" />
            <line x1="15" y1="9" x2="9" y2="15" />
            <line x1="9" y1="9" x2="15" y2="15" />
          </svg>
          <svg v-else-if="msg.type === 'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
            <line x1="12" y1="9" x2="12" y2="13" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
        </div>

        <!-- 内容 -->
        <div class="fc-message__content">{{ msg.message }}</div>

        <!-- 关闭按钮 -->
        <button v-if="msg.showClose" class="fc-message__close" @click="close(msg.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>
    </transition-group>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'

/**
 * FcMessage 消息提示组件（内部使用）
 * 通过 Message API 调用，不直接使用此组件
 */

const messages = ref([])
let messageId = 0

const MESSAGE_OFFSET = 20
const MESSAGE_SPACING = 16

function add(options) {
  const id = `message_${++messageId}`
  const message = {
    id,
    type: options.type || 'info',
    message: options.message,
    duration: options.duration !== undefined ? options.duration : 3000,
    showClose: options.showClose || false,
    offset: 0,
    timer: null
  }

  // 计算偏移量
  messages.value.push(message)
  updateOffsets()

  // 自动关闭
  if (message.duration > 0) {
    message.timer = setTimeout(() => {
      close(id)
    }, message.duration)
  }

  return id
}

function close(id) {
  const index = messages.value.findIndex(msg => msg.id === id)
  if (index === -1) return

  const msg = messages.value[index]
  if (msg.timer) {
    clearTimeout(msg.timer)
  }

  messages.value.splice(index, 1)
  updateOffsets()
}

function updateOffsets() {
  messages.value.forEach((msg, index) => {
    msg.offset = MESSAGE_OFFSET + index * (56 + MESSAGE_SPACING)
  })
}

// 暴露方法供API调用
defineExpose({ add, close })
</script>

<style scoped>
.fc-message-container {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  z-index: var(--fc-z-message);
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
}

.fc-message {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: var(--fc-spacing-md);
  padding: var(--fc-spacing-md) var(--fc-spacing-xl);
  background: white;
  border-radius: var(--fc-radius-md);
  box-shadow: var(--fc-shadow-lg);
  pointer-events: auto;
  min-width: 300px;
  max-width: 500px;
}

/* 类型样式 */
.fc-message--success {
  border-left: 4px solid var(--fc-success);
}

.fc-message--success .fc-message__icon {
  color: var(--fc-success);
}

.fc-message--error {
  border-left: 4px solid var(--fc-error);
}

.fc-message--error .fc-message__icon {
  color: var(--fc-error);
}

.fc-message--warning {
  border-left: 4px solid var(--fc-warning);
}

.fc-message--warning .fc-message__icon {
  color: var(--fc-warning);
}

.fc-message--info {
  border-left: 4px solid var(--fc-info);
}

.fc-message--info .fc-message__icon {
  color: var(--fc-info);
}

/* 图标 */
.fc-message__icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
}

.fc-message__icon svg {
  width: 100%;
  height: 100%;
}

/* 内容 */
.fc-message__content {
  flex: 1;
  font-size: var(--fc-text-base);
  color: var(--fc-gray-700);
  line-height: 1.5;
}

/* 关闭按钮 */
.fc-message__close {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--fc-gray-400);
  border-radius: var(--fc-radius-sm);
  transition: all var(--fc-transition-fast) var(--fc-easing);
}

.fc-message__close:hover {
  background: var(--fc-gray-100);
  color: var(--fc-gray-600);
}

.fc-message__close svg {
  width: 16px;
  height: 16px;
}

/* 动画 */
.fc-message-enter-active {
  transition: all var(--fc-transition-slow) var(--fc-easing);
}

.fc-message-leave-active {
  transition: all var(--fc-transition-fast) var(--fc-easing);
}

.fc-message-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

.fc-message-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

/* 响应式 */
@media (max-width: 768px) {
  .fc-message {
    min-width: 250px;
    max-width: calc(100% - 32px);
    padding: var(--fc-spacing-sm) var(--fc-spacing-lg);
  }
}
</style>
