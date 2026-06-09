<template>
  <div class="chat-input" :class="{ 'chat-input--disabled': disabled }">
    <!-- 工具栏 -->
    <div class="chat-input__toolbar">
      <!-- 表情按钮 -->
      <button class="chat-input__tool-btn" @click="handleEmoji" :disabled="disabled">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <circle cx="12" cy="12" r="10" />
          <path d="M8 14s1.5 2 4 2 4-2 4-2" />
          <line x1="9" y1="9" x2="9.01" y2="9" />
          <line x1="15" y1="9" x2="15.01" y2="9" />
        </svg>
      </button>

      <!-- 附件按钮 -->
      <button class="chat-input__tool-btn" @click="handleAttach" :disabled="disabled">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" />
        </svg>
      </button>

      <!-- 图片按钮 -->
      <button class="chat-input__tool-btn" @click="handleImage" :disabled="disabled">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
          <circle cx="8.5" cy="8.5" r="1.5" />
          <polyline points="21 15 16 10 5 21" />
        </svg>
      </button>
    </div>

    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInputRef"
      type="file"
      accept="*/*"
      style="display: none"
      @change="handleFileChange"
    />
    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleImageChange"
    />

    <!-- 输入区 -->
    <div class="chat-input__wrapper">
      <textarea
        ref="textareaRef"
        v-model="inputValue"
        class="chat-input__textarea"
        :placeholder="placeholder"
        :disabled="disabled"
        @input="handleInput"
        @keydown="handleKeydown"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      
      <!-- 发送按钮 -->
      <button 
        class="chat-input__send" 
        :class="{ 'chat-input__send--active': inputValue.trim() && !disabled }"
        :disabled="!inputValue.trim() || disabled || sending"
        @click="handleSend"
      >
        <svg v-if="!sending" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <line x1="22" y1="2" x2="11" y2="13" />
          <polygon points="22 2 15 22 11 13 2 9 22 2" />
        </svg>
        <svg v-else class="chat-input__spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <circle cx="12" cy="12" r="10" stroke-width="3" stroke-dasharray="31.4 31.4">
            <animateTransform attributeName="transform" type="rotate" from="0 12 12" to="360 12 12" dur="1s" repeatCount="indefinite"/>
          </circle>
        </svg>
      </button>
    </div>

    <!-- 提示文字 -->
    <div class="chat-input__hint">
      {{ hint }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

/**
 * ChatInput 聊天输入组件
 * 
 * @example
 * ```vue
 * <ChatInput v-model="text" @send="handleSend" />
 * ```
 */

const props = defineProps({
  /**
   * 输入值
   */
  modelValue: {
    type: String,
    default: ''
  },
  
  /**
   * 占位符
   */
  placeholder: {
    type: String,
    default: '输入消息...'
  },
  
  /**
   * 是否禁用
   */
  disabled: {
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
   * 提示文字
   */
  hint: {
    type: String,
    default: 'Enter 发送，Ctrl + Enter 换行'
  }
})

const emit = defineEmits(['update:modelValue', 'send', 'emoji', 'attach', 'image', 'focus', 'blur', 'file-select', 'image-select'])

const textareaRef = ref(null)
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const inputValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleInput = (event) => {
  emit('input', event)
}

const handleKeydown = (event) => {
  // Ctrl + Enter 或 Cmd + Enter 换行
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
    return
  }
  
  // Enter 发送
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}

const handleSend = () => {
  if (!inputValue.value.trim() || props.disabled || props.sending) {
    return
  }
  
  emit('send', inputValue.value)
  inputValue.value = ''
  
  // 重置textarea高度
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }
}

const handleEmoji = () => {
  emit('emoji')
}

const handleAttach = () => {
  // 触发文件选择
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
  emit('attach')
}

const handleImage = () => {
  // 触发图片选择
  if (imageInputRef.value) {
    imageInputRef.value.click()
  }
  emit('image')
}

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    emit('file-select', file)
    // 重置input，允许选择同一个文件
    event.target.value = ''
  }
}

const handleImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    emit('image-select', file)
    // 重置input，允许选择同一个文件
    event.target.value = ''
  }
}

const handleFocus = (event) => {
  emit('focus', event)
}

const handleBlur = (event) => {
  emit('blur', event)
}
</script>

<style scoped>
.chat-input {
  background: var(--chat-input-bg);
  border-top: 1px solid var(--fc-border);
  padding: 12px 16px;
}

.chat-input--disabled {
  opacity: 0.6;
}

/* 工具栏 */
.chat-input__toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.chat-input__tool-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--fc-radius-sm);
  cursor: pointer;
  color: var(--fc-gray-500);
  transition: all var(--fc-transition-fast) var(--fc-easing);
}

.chat-input__tool-btn:hover:not(:disabled) {
  background: var(--fc-gray-100);
  color: var(--fc-primary-500);
}

.chat-input__tool-btn:disabled {
  cursor: not-allowed;
}

.chat-input__tool-btn svg {
  width: 20px;
  height: 20px;
}

/* 输入区 */
.chat-input__wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input__textarea {
  flex: 1;
  min-height: 40px;
  max-height: 120px;
  padding: 10px 14px;
  border: 1px solid var(--fc-border);
  border-radius: var(--fc-radius-md);
  font-size: var(--fc-text-base);
  line-height: 1.5;
  color: var(--fc-gray-800);
  background: var(--fc-gray-50);
  resize: none;
  transition: all var(--fc-transition-fast) var(--fc-easing);
  font-family: inherit;
}

.chat-input__textarea:focus {
  outline: none;
  border-color: var(--fc-primary-500);
  background: white;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.chat-input__textarea:disabled {
  background: var(--fc-gray-100);
  cursor: not-allowed;
}

.chat-input__textarea::placeholder {
  color: var(--fc-gray-400);
}

/* 发送按钮 */
.chat-input__send {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--fc-gray-300);
  border: none;
  border-radius: var(--fc-radius-md);
  cursor: not-allowed;
  color: white;
  transition: all var(--fc-transition-fast) var(--fc-easing);
  flex-shrink: 0;
}

.chat-input__send svg {
  width: 20px;
  height: 20px;
}

.chat-input__send--active {
  background: var(--fc-primary-500);
  cursor: pointer;
}

.chat-input__send--active:hover {
  background: var(--fc-primary-600);
  transform: scale(1.05);
}

.chat-input__send--active:active {
  transform: scale(0.95);
}

.chat-input__spinner {
  animation: fc-spin 1s linear infinite;
}

/* 提示文字 */
.chat-input__hint {
  font-size: 12px;
  color: var(--fc-gray-400);
  margin-top: 8px;
  text-align: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-input {
    padding: 8px 12px;
  }
  
  .chat-input__textarea {
    padding: 8px 12px;
    min-height: 36px;
  }
  
  .chat-input__send {
    width: 36px;
    height: 36px;
  }
}
</style>
