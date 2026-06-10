<template>
  <div class="fc-avatar-wrapper" :class="wrapperClass">
    <div class="fc-avatar" :class="avatarClass" :style="avatarStyle">
      <!-- 图片头像 -->
      <img
        v-if="src"
        :src="src"
        :alt="alt"
        class="fc-avatar__image"
        @error="handleImageError"
      />
      
      <!-- 文字头像 -->
      <span v-else-if="text" class="fc-avatar__text">{{ displayText }}</span>
      
      <!-- 图标头像 -->
      <svg v-else class="fc-avatar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
        <circle cx="12" cy="7" r="4" />
      </svg>
    </div>

    <!-- 状态指示器 -->
    <div v-if="showStatus" class="fc-avatar__status" :class="statusClass">
      <div class="fc-avatar__status-dot"></div>
    </div>

    <!-- 徽章 -->
    <slot name="badge"></slot>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

/**
 * FcAvatar 头像组件
 * 
 * @example
 * ```vue
 * <FcAvatar src="https://example.com/avatar.jpg" />
 * <FcAvatar text="张三" size="lg" />
 * <FcAvatar :src="avatarUrl" status="online" />
 * ```
 */

const props = defineProps({
  /**
   * 图片地址
   */
  src: {
    type: String,
    default: ''
  },
  
  /**
   * 替代文本
   */
  alt: {
    type: String,
    default: 'avatar'
  },
  
  /**
   * 文字头像内容
   */
  text: {
    type: String,
    default: ''
  },
  
  /**
   * 头像尺寸
   * @values xs, sm, md, lg, xl（字符串）或数字（像素）
   */
  size: {
    type: [String, Number],
    default: 'md'
  },
  
  /**
   * 形状
   * @values circle, square
   */
  shape: {
    type: String,
    default: 'circle'
  },
  
  /**
   * 在线状态
   * @values online, offline, busy
   */
  status: {
    type: String,
    default: ''
  },
  
  /**
   * 是否显示状态指示器
   */
  showStatus: {
    type: Boolean,
    default: false
  },
  
  /**
   * 背景色
   */
  bgColor: {
    type: String,
    default: ''
  }
})

const imageError = ref(false)

const sizeMap = {
  xs: '32px',
  sm: '40px',
  md: '48px',
  lg: '64px',
  xl: '80px'
}

const avatarStyle = computed(() => {
  const style = {}
  
  // 尺寸
  let size
  if (typeof props.size === 'number') {
    // 如果是数字，直接作为像素值
    size = `${props.size}px`
  } else {
    // 如果是字符串，从 sizeMap 中查找
    size = sizeMap[props.size] || sizeMap.md
  }
  style.width = size
  style.height = size
  
  // 背景色
  if (props.bgColor) {
    style.backgroundColor = props.bgColor
  }
  
  return style
})

const avatarClass = computed(() => ({
  'fc-avatar--circle': props.shape === 'circle',
  'fc-avatar--square': props.shape === 'square'
}))

const wrapperClass = computed(() => ({
  'fc-avatar-wrapper--has-status': props.showStatus && props.status
}))

const statusClass = computed(() => ({
  'fc-avatar__status--online': props.status === 'online',
  'fc-avatar__status--offline': props.status === 'offline',
  'fc-avatar__status--busy': props.status === 'busy'
}))

const displayText = computed(() => {
  if (!props.text) return ''
  // 最多显示2个字符
  return props.text.slice(0, 2).toUpperCase()
})

const handleImageError = () => {
  imageError.value = true
}
</script>

<style scoped>
.fc-avatar-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.fc-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, var(--fc-primary-400) 0%, var(--fc-primary-600) 100%);
  color: white;
  font-weight: var(--fc-font-semibold);
  transition: all var(--fc-transition-normal) var(--fc-easing);
}

.fc-avatar--circle {
  border-radius: 50%;
}

.fc-avatar--square {
  border-radius: var(--fc-radius-md);
}

/* 图片头像 */
.fc-avatar__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 文字头像 */
.fc-avatar__text {
  font-size: inherit;
  line-height: 1;
}

/* 图标 */
.fc-avatar__icon {
  width: 60%;
  height: 60%;
  opacity: 0.8;
}

/* 状态指示器 */
.fc-avatar__status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fc-avatar__status-dot {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

/* 在线 */
.fc-avatar__status--online {
  background: var(--fc-success);
}

.fc-avatar__status--online .fc-avatar__status-dot {
  background: var(--fc-success);
}

/* 离线 */
.fc-avatar__status--offline {
  background: var(--fc-gray-300);
}

.fc-avatar__status--offline .fc-avatar__status-dot {
  background: var(--fc-gray-300);
}

/* 忙碌 */
.fc-avatar__status--busy {
  background: var(--fc-error);
}

.fc-avatar__status--busy .fc-avatar__status-dot {
  background: var(--fc-error);
}

/* 不同尺寸的文字大小 */
.fc-avatar:has(.fc-avatar__text) {
  font-size: var(--fc-text-lg);
}

.fc-avatar--xs {
  font-size: var(--fc-text-xs);
}

.fc-avatar--sm {
  font-size: var(--fc-text-sm);
}

.fc-avatar--md {
  font-size: var(--fc-text-base);
}

.fc-avatar--lg {
  font-size: var(--fc-text-xl);
}

.fc-avatar--xl {
  font-size: var(--fc-text-2xl);
}

/* 悬停效果 */
.fc-avatar:hover {
  transform: scale(1.05);
}

/* 响应式 */
@media (max-width: 768px) {
  .fc-avatar--xl {
    width: 64px;
    height: 64px;
  }
}
</style>
