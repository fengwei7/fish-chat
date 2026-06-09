<template>
  <div class="fc-skeleton" :class="skeletonClass">
    <!-- 圆形骨架（头像等） -->
    <div v-if="shape === 'circle'" class="fc-skeleton__circle" :style="sizeStyle"></div>
    
    <!-- 矩形骨架 -->
    <div v-else-if="shape === 'rect'" class="fc-skeleton__rect" :style="sizeStyle"></div>
    
    <!-- 文字骨架 -->
    <div v-else class="fc-skeleton__text" :style="textStyle"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

/**
 * FcSkeleton 加载骨架屏组件
 * 
 * @example
 * ```vue
 * <FcSkeleton shape="circle" size="48px" />
 * <FcSkeleton shape="rect" width="100%" height="20px" />
 * <FcSkeleton rows="3" />
 * ```
 */

const props = defineProps({
  /**
   * 形状
   * @values circle, rect, text
   */
  shape: {
    type: String,
    default: 'text'
  },
  
  /**
   * 宽度
   */
  width: {
    type: String,
    default: ''
  },
  
  /**
   * 高度
   */
  height: {
    type: String,
    default: ''
  },
  
  /**
   * 尺寸（圆形用）
   */
  size: {
    type: String,
    default: ''
  },
  
  /**
   * 文字行数
   */
  rows: {
    type: Number,
    default: 1
  },
  
  /**
   * 最后一行宽度（百分比）
   */
  lastRowWidth: {
    type: String,
    default: '60%'
  },
  
  /**
   * 是否动画
   */
  animated: {
    type: Boolean,
    default: true
  }
})

const sizeStyle = computed(() => {
  const style = {}
  
  if (props.shape === 'circle') {
    const size = props.size || '48px'
    style.width = size
    style.height = size
    style.borderRadius = '50%'
  } else {
    if (props.width) style.width = props.width
    if (props.height) style.height = props.height
  }
  
  return style
})

const skeletonClass = computed(() => ({
  'fc-skeleton--animated': props.animated
}))

const textStyle = computed(() => {
  return {
    '--rows': props.rows,
    '--last-row-width': props.lastRowWidth
  }
})
</script>

<style scoped>
.fc-skeleton {
  display: inline-block;
}

/* 动画效果 */
.fc-skeleton--animated > div {
  animation: fc-skeleton 1.5s ease-in-out infinite;
  background: linear-gradient(
    90deg,
    var(--fc-gray-100) 25%,
    var(--fc-gray-200) 50%,
    var(--fc-gray-100) 75%
  );
  background-size: 200% 100%;
}

/* 圆形 */
.fc-skeleton__circle {
  border-radius: 50%;
}

/* 矩形 */
.fc-skeleton__rect {
  border-radius: var(--fc-radius-sm);
}

/* 文字 */
.fc-skeleton__text {
  display: flex;
  flex-direction: column;
  gap: var(--fc-spacing-md);
}

.fc-skeleton__text::before {
  content: '';
  display: block;
  height: var(--fc-text-base);
  background: inherit;
  border-radius: var(--fc-radius-sm);
}

/* 多行文字 */
.fc-skeleton__text[style*="--rows"]::before {
  content: none;
}

.fc-skeleton__text[style*="--rows"] {
  position: relative;
}

.fc-skeleton__text[style*="--rows"]::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: repeating-linear-gradient(
    to bottom,
    transparent,
    transparent calc(100% / var(--rows) - 12px),
    var(--fc-gray-100) calc(100% / var(--rows) - 12px),
    var(--fc-gray-100) calc(100% / var(--rows))
  );
  animation: inherit;
}
</style>
