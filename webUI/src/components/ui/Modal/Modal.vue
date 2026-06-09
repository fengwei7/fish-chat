<template>
  <Teleport to="body">
    <transition name="fc-modal">
      <div v-if="visible" class="fc-modal-overlay" @click="handleOverlayClick">
        <div
          ref="modalRef"
          class="fc-modal"
          :class="[
            `fc-modal--${size}`,
            { 'fc-modal--center': center }
          ]"
          @click.stop
        >
          <!-- 头部 -->
          <div v-if="$slots.header || title" class="fc-modal__header">
            <slot name="header">
              <h3 class="fc-modal__title">{{ title }}</h3>
            </slot>
            <button v-if="showClose" class="fc-modal__close" @click="handleClose">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          <!-- 内容 -->
          <div class="fc-modal__body">
            <slot></slot>
          </div>

          <!-- 底部 -->
          <div v-if="$slots.footer" class="fc-modal__footer">
            <slot name="footer"></slot>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'

/**
 * FcModal 弹窗组件
 * 
 * @example
 * ```vue
 <FcModal v-model:visible="showModal" title="标题">
 *   <p>内容</p>
 *   <template #footer>
 *     <FcButton @click="showModal = false">取消</FcButton>
 *     <FcButton type="primary">确定</FcButton>
 *   </template>
 * </FcModal>
 * ```
 */

const props = defineProps({
  /**
   * 是否显示
   */
  visible: {
    type: Boolean,
    default: false
  },
  
  /**
   * 标题
   */
  title: {
    type: String,
    default: ''
  },
  
  /**
   * 弹窗尺寸
   * @values sm, md, lg, xl, fullscreen
   */
  size: {
    type: String,
    default: 'md'
  },
  
  /**
   * 是否居中显示
   */
  center: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否显示关闭按钮
   */
  showClose: {
    type: Boolean,
    default: true
  },
  
  /**
   * 点击遮罩是否关闭
   */
  closeOnOverlay: {
    type: Boolean,
    default: true
  },
  
  /**
   * 按ESC是否关闭
   */
  closeOnEsc: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:visible', 'close'])

const modalRef = ref(null)

const handleOverlayClick = () => {
  if (props.closeOnOverlay) {
    handleClose()
  }
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

// ESC键关闭
const handleEsc = (event) => {
  if (props.closeOnEsc && event.key === 'Escape' && props.visible) {
    handleClose()
  }
}

// 阻止背景滚动
watch(() => props.visible, (val) => {
  if (val) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleEsc)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleEsc)
  document.body.style.overflow = ''
})
</script>

<style scoped>
.fc-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--fc-z-modal);
  padding: var(--fc-spacing-xl);
}

.fc-modal {
  background: white;
  border-radius: var(--fc-radius-lg);
  box-shadow: var(--fc-shadow-xl);
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  animation: fc-scale-in var(--fc-transition-slow) var(--fc-easing);
}

/* 尺寸 */
.fc-modal--sm {
  width: 100%;
  max-width: 400px;
}

.fc-modal--md {
  width: 100%;
  max-width: 600px;
}

.fc-modal--lg {
  width: 100%;
  max-width: 800px;
}

.fc-modal--xl {
  width: 100%;
  max-width: 1200px;
}

.fc-modal--fullscreen {
  width: calc(100% - var(--fc-spacing-2xl));
  height: calc(100% - var(--fc-spacing-2xl));
  max-width: none;
  max-height: none;
}

/* 头部 */
.fc-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--fc-spacing-xl) var(--fc-spacing-2xl);
}

.fc-modal__title {
  margin: 0;
  font-size: var(--fc-text-xl);
  font-weight: var(--fc-font-semibold);
  color: var(--fc-gray-900);
}

.fc-modal__close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--fc-radius-sm);
  cursor: pointer;
  color: var(--fc-gray-400);
  transition: all var(--fc-transition-fast) var(--fc-easing);
}

.fc-modal__close:hover {
  background: var(--fc-gray-100);
  color: var(--fc-gray-600);
}

.fc-modal__close svg {
  width: 20px;
  height: 20px;
}

/* 内容 */
.fc-modal__body {
  flex: 1;
  padding: var(--fc-spacing-2xl);
  overflow-y: auto;
}

/* 底部 */
.fc-modal__footer {
  padding: var(--fc-spacing-lg) var(--fc-spacing-2xl);
  display: flex;
  justify-content: flex-end;
  gap: var(--fc-spacing-md);
}

/* 动画 */
.fc-modal-enter-active,
.fc-modal-leave-active {
  transition: opacity var(--fc-transition-slow) var(--fc-easing);
}

.fc-modal-enter-from,
.fc-modal-leave-to {
  opacity: 0;
}

.fc-modal-enter-active .fc-modal,
.fc-modal-leave-active .fc-modal {
  transition: transform var(--fc-transition-slow) var(--fc-easing);
}

.fc-modal-enter-from .fc-modal,
.fc-modal-leave-to .fc-modal {
  transform: scale(0.95);
}

/* 响应式 */
@media (max-width: 768px) {
  .fc-modal-overlay {
    padding: var(--fc-spacing-md);
  }
  
  .fc-modal--md,
  .fc-modal--lg,
  .fc-modal--xl {
    max-width: 100%;
  }
  
  .fc-modal__header,
  .fc-modal__body,
  .fc-modal__footer {
    padding-left: var(--fc-spacing-lg);
    padding-right: var(--fc-spacing-lg);
  }
}
</style>
