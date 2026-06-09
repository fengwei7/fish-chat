<template>
  <div class="fc-badge-wrapper">
    <slot></slot>
    <sup v-if="isDot" class="fc-badge fc-badge--dot" :class="badgeClass"></sup>
    <span
      v-else-if="computedCount > 0"
      class="fc-badge fc-badge--count"
      :class="badgeClass"
    >
      {{ displayCount }}
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

/**
 * FcBadge 徽章组件
 * 
 * @example
 * ```vue
 * <FcBadge :count="10">
 *   <button>消息</button>
 * </FcBadge>
 * 
 * <FcBadge dot>
 *   <icon />
 * </FcBadge>
 * ```
 */

const props = defineProps({
  /**
   * 数量
   */
  count: {
    type: Number,
    default: 0
  },
  
  /**
   * 最大值（超过显示+）
   */
  maxCount: {
    type: Number,
    default: 99
  },
  
  /**
   * 是否点状徽章
   */
  dot: {
    type: Boolean,
    default: false
  },
  
  /**
   * 徽章类型
   * @values primary, success, warning, error
   */
  type: {
    type: String,
    default: 'error'
  }
})

const isDot = computed(() => props.dot)

const computedCount = computed(() => props.count)

const displayCount = computed(() => {
  return props.count > props.maxCount ? `${props.maxCount}+` : props.count
})

const badgeClass = computed(() => ({
  [`fc-badge--${props.type}`]: true
}))
</script>

<style scoped>
.fc-badge-wrapper {
  position: relative;
  display: inline-block;
}

.fc-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  transform: translate(50%, -50%);
  z-index: var(--fc-z-popover);
  font-family: var(--fc-font-sans);
  font-weight: var(--fc-font-semibold);
}

/* 点状徽章 */
.fc-badge--dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid white;
}

/* 数字徽章 */
.fc-badge--count {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: var(--fc-radius-full);
  font-size: 12px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border: 2px solid white;
}

/* 类型颜色 */
.fc-badge--primary {
  background: var(--fc-primary-500);
}

.fc-badge--success {
  background: var(--fc-success);
}

.fc-badge--warning {
  background: var(--fc-warning);
}

.fc-badge--error {
  background: var(--fc-error);
}
</style>
