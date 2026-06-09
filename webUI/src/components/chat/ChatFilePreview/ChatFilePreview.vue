<template>
  <Teleport to="body">
    <transition name="fc-modal">
      <div v-if="visible" class="chat-file-preview-overlay" @click="handleClose">
        <div class="chat-file-preview" @click.stop>
          <!-- 头部 -->
          <div class="chat-file-preview__header">
            <h3 class="chat-file-preview__title">{{ fileName }}</h3>
            <button class="chat-file-preview__close" @click="handleClose">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          <!-- 内容 -->
          <div class="chat-file-preview__content">
            <!-- 图片预览 -->
            <div v-if="fileType === 'image'" class="chat-file-preview__image">
              <img :src="fileUrl" :alt="fileName" />
            </div>

            <!-- PDF预览 -->
            <div v-else-if="fileType === 'pdf'" class="chat-file-preview__pdf">
              <iframe :src="fileUrl" frameborder="0"></iframe>
            </div>

            <!-- 视频预览 -->
            <div v-else-if="fileType === 'video'" class="chat-file-preview__video">
              <video controls autoplay>
                <source :src="fileUrl" />
                您的浏览器不支持视频播放
              </video>
            </div>

            <!-- 不支持预览的文件类型 -->
            <div v-else class="chat-file-preview__fallback">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              <p>该文件类型暂不支持预览</p>
              <button class="chat-file-preview__download" @click="handleDownload">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 15 17 10" />
                  <line x1="12" y1="15" x2="12" y2="3" />
                </svg>
                下载文件
              </button>
            </div>
          </div>

          <!-- 底部操作 -->
          <div class="chat-file-preview__footer">
            <button class="chat-file-preview__action" @click="handleDownload">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="7 10 12 15 17 10" />
                <line x1="12" y1="15" x2="12" y2="3" />
              </svg>
              下载
            </button>
            <slot name="actions"></slot>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'

/**
 * ChatFilePreview 文件预览组件
 * 
 * @example
 * ```vue
 * <ChatFilePreview
 *   v-model:visible="showPreview"
 *   :file-url="fileUrl"
 *   :file-name="fileName"
 * />
 * ```
 */

const props = defineProps({
  /**
   * 是否可见
   */
  visible: {
    type: Boolean,
    default: false
  },
  
  /**
   * 文件URL
   */
  fileUrl: {
    type: String,
    default: ''
  },
  
  /**
   * 文件名
   */
  fileName: {
    type: String,
    default: ''
  },
  
  /**
   * 文件类型（自动检测如果未提供）
   */
  fileType: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'close', 'download'])

// 自动检测文件类型
const detectedFileType = computed(() => {
  if (props.fileType) return props.fileType
  
  if (!props.fileUrl) return ''
  
  const url = props.fileUrl.toLowerCase()
  
  // 先检查 URL 中的查询参数（可能是MIME类型）
  if (url.includes('application/pdf') || url.includes('contentType=image')) {
    return url.includes('application/pdf') ? 'pdf' : 'image'
  }
  
  // 从URL路径中提取扩展名
  const urlPath = url.split('?')[0] // 去掉查询参数
  const extension = urlPath.split('.').pop()
  
  // 图片
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'].includes(extension)) {
    return 'image'
  }
  
  // PDF
  if (extension === 'pdf') {
    return 'pdf'
  }
  
  // 视频
  if (['mp4', 'webm', 'ogg', 'mov'].includes(extension)) {
    return 'video'
  }
  
  // 如果URL看起来像图片（包含picsum、unsplash等），默认当作图片
  if (url.includes('picsum') || url.includes('unsplash') || url.includes('image')) {
    return 'image'
  }
  
  return 'unknown'
})

const fileType = computed(() => detectedFileType.value)

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const handleDownload = () => {
  emit('download', {
    url: props.fileUrl,
    fileName: props.fileName
  })
  
  // 默认下载行为
  const link = document.createElement('a')
  link.href = props.fileUrl
  link.download = props.fileName
  link.click()
}

// ESC键关闭
const handleEsc = (event) => {
  if (event.key === 'Escape' && props.visible) {
    handleClose()
  }
}

if (typeof window !== 'undefined') {
  window.addEventListener('keydown', handleEsc)
}
</script>

<style scoped>
.chat-file-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--fc-z-modal);
  padding: 20px;
}

.chat-file-preview {
  width: 100%;
  max-width: 1200px;
  max-height: 90vh;
  background: white;
  border-radius: var(--fc-radius-lg);
  display: flex;
  flex-direction: column;
  animation: fc-scale-in var(--fc-transition-slow) var(--fc-easing);
}

/* 头部 */
.chat-file-preview__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--fc-border);
}

.chat-file-preview__title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--fc-gray-900);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.chat-file-preview__close {
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

.chat-file-preview__close:hover {
  background: var(--fc-gray-100);
  color: var(--fc-gray-700);
}

.chat-file-preview__close svg {
  width: 20px;
  height: 20px;
}

/* 内容 */
.chat-file-preview__content {
  flex: 1;
  overflow: auto;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

/* 图片预览 */
.chat-file-preview__image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-file-preview__image img {
  max-width: 100%;
  max-height: calc(90vh - 200px);
  object-fit: contain;
  border-radius: var(--fc-radius-md);
}

/* PDF预览 */
.chat-file-preview__pdf {
  width: 100%;
  height: 100%;
}

.chat-file-preview__pdf iframe {
  width: 100%;
  height: calc(90vh - 200px);
  border-radius: var(--fc-radius-md);
}

/* 视频预览 */
.chat-file-preview__video {
  width: 100%;
  height: 100%;
}

.chat-file-preview__video video {
  max-width: 100%;
  max-height: calc(90vh - 200px);
  border-radius: var(--fc-radius-md);
  background: black;
}

/* 不支持的文件类型 */
.chat-file-preview__fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 60px;
  color: var(--fc-gray-500);
}

.chat-file-preview__fallback svg {
  width: 80px;
  height: 80px;
  opacity: 0.3;
}

.chat-file-preview__fallback p {
  font-size: 16px;
  margin: 0;
}

.chat-file-preview__download {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--fc-primary-500);
  color: white;
  border: none;
  border-radius: var(--fc-radius-md);
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all var(--fc-transition-fast) var(--fc-easing);
}

.chat-file-preview__download:hover {
  background: var(--fc-primary-600);
  transform: translateY(-2px);
  box-shadow: var(--fc-shadow-md);
}

.chat-file-preview__download svg {
  width: 20px;
  height: 20px;
}

/* 底部操作 */
.chat-file-preview__footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--fc-border);
}

.chat-file-preview__action {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--fc-gray-100);
  color: var(--fc-gray-700);
  border: none;
  border-radius: var(--fc-radius-md);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--fc-transition-fast) var(--fc-easing);
}

.chat-file-preview__action:hover {
  background: var(--fc-gray-200);
}

.chat-file-preview__action svg {
  width: 18px;
  height: 18px;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-file-preview-overlay {
    padding: 0;
  }
  
  .chat-file-preview {
    max-height: 100vh;
    border-radius: 0;
  }
  
  .chat-file-preview__content {
    padding: 16px;
    min-height: 300px;
  }
  
  .chat-file-preview__header,
  .chat-file-preview__footer {
    padding: 12px 16px;
  }
  
  .chat-file-preview__title {
    font-size: 16px;
  }
}
</style>
