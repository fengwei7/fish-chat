<template>
  <Teleport to="body">
    <!-- 遮罩层 -->
    <Transition name="fc-drawer-mask">
      <div
        v-if="visible && mask"
        class="fc-drawer__mask"
        @click="handleMaskClick"
      />
    </Transition>

    <!-- 抽屉 -->
    <Transition name="fc-drawer">
      <div
        v-if="visible"
        class="fc-drawer"
        :class="[
          `fc-drawer--${placement}`,
          { 'fc-drawer--no-mask': !mask }
        ]"
        :style="drawerStyle"
      >
        <!-- 头部 -->
        <div v-if="title || $slots.header" class="fc-drawer__header">
          <slot name="header">
            <span class="fc-drawer__title">{{ title }}</span>
            <button v-if="closable" class="fc-drawer__close" @click="close">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </slot>
        </div>

        <!-- 内容 -->
        <div class="fc-drawer__body">
          <slot />
        </div>

        <!-- 底部 -->
        <div v-if="$slots.footer" class="fc-drawer__footer">
          <slot name="footer" />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  width: {
    type: String,
    default: '400px'
  },
  placement: {
    type: String,
    default: 'right', // left | right
    validator: (val) => ['left', 'right'].includes(val)
  },
  mask: {
    type: Boolean,
    default: true
  },
  maskClosable: {
    type: Boolean,
    default: true
  },
  closable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const drawerStyle = computed(() => {
  return {
    width: props.width
  }
})

function close() {
  visible.value = false
  emit('close')
}

function handleMaskClick() {
  if (props.maskClosable) {
    close()
  }
}

// 监听 body 滚动
watch(visible, (val) => {
  if (val) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})
</script>

<style scoped>
/* 遮罩层 */
.fc-drawer__mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2000;
}

.fc-drawer-mask-enter-active,
.fc-drawer-mask-leave-active {
  transition: opacity 0.3s ease;
}

.fc-drawer-mask-enter-from,
.fc-drawer-mask-leave-to {
  opacity: 0;
}

/* 抽屉 */
.fc-drawer {
  position: fixed;
  top: 0;
  height: 100%;
  background: var(--fc-bg-primary);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 2001;
  display: flex;
  flex-direction: column;
}

.fc-drawer--right {
  right: 0;
}

.fc-drawer--left {
  left: 0;
}

.fc-drawer--no-mask {
  z-index: 1001;
}

.fc-drawer-enter-active,
.fc-drawer-leave-active {
  transition: transform 0.3s ease;
}

.fc-drawer--right.fc-drawer-enter-from,
.fc-drawer--right.fc-drawer-leave-to {
  transform: translateX(100%);
}

.fc-drawer--left.fc-drawer-enter-from,
.fc-drawer--left.fc-drawer-leave-to {
  transform: translateX(-100%);
}

/* 头部 */
.fc-drawer__header {
  padding: 20px;
  border-bottom: 1px solid var(--fc-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.fc-drawer__title {
  font-size: var(--fc-text-lg);
  font-weight: 600;
  color: var(--fc-text-primary);
}

.fc-drawer__close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--fc-radius-sm);
  color: var(--fc-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.fc-drawer__close:hover {
  background: var(--fc-bg-hover);
  color: var(--fc-text-primary);
}

/* 内容 */
.fc-drawer__body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.fc-drawer__body::-webkit-scrollbar {
  width: 6px;
}

.fc-drawer__body::-webkit-scrollbar-thumb {
  background: var(--fc-border);
  border-radius: 3px;
}

/* 底部 */
.fc-drawer__footer {
  padding: 16px 20px;
  border-top: 1px solid var(--fc-border);
}
</style>
