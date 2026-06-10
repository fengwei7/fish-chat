<template>
  <button
    class="fc-button"
    :class="[
      `fc-button--${type}`,
      `fc-button--${size}`,
      {
        'fc-button--block': block,
        'fc-button--disabled': disabled,
        'fc-button--loading': loading,
        'fc-button--round': round,
        'fc-button--circle': circle
      }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <!-- 加载动画 -->
    <svg v-if="loading" class="fc-button__spinner" viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-dasharray="31.4 31.4">
        <animateTransform attributeName="transform" type="rotate" from="0 12 12" to="360 12 12" dur="1s" repeatCount="indefinite"/>
      </circle>
    </svg>

    <!-- 前置图标 -->
    <span v-if="$slots.prefix || prefixIcon" class="fc-button__icon fc-button__icon--prefix">
      <slot name="prefix">
        <component v-if="prefixIcon" :is="prefixIcon" />
      </slot>
    </span>

    <!-- 按钮文字 -->
    <span class="fc-button__text">
      <slot></slot>
    </span>

    <!-- 后置图标 -->
    <span v-if="$slots.suffix || suffixIcon" class="fc-button__icon fc-button__icon--suffix">
      <slot name="suffix">
        <component v-if="suffixIcon" :is="suffixIcon" />
      </slot>
    </span>
  </button>
</template>

<script setup>
/**
 * FcButton 按钮组件
 * 
 * @example
 * ```vue
 * <FcButton type="primary">主要按钮</FcButton>
 * <FcButton type="secondary" size="sm">小号按钮</FcButton>
 * <FcButton type="outline" loading>加载中</FcButton>
 * <FcButton type="danger" :disabled="true">禁用按钮</FcButton>
 * ```
 */

defineProps({
  /**
   * 按钮类型
   * @values primary, secondary, outline, ghost, danger
   */
  type: {
    type: String,
    default: 'primary'
  },
  
  /**
   * 按钮尺寸
   * @values sm, md, lg, large
   */
  size: {
    type: String,
    default: 'md'
  },
  
  /**
   * 是否禁用
   */
  disabled: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否加载中
   */
  loading: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否块级按钮
   */
  block: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否圆角按钮
   */
  round: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否圆形按钮
   */
  circle: {
    type: Boolean,
    default: false
  },
  
  /**
   * 前置图标组件
   */
  prefixIcon: {
    type: Object,
    default: null
  },
  
  /**
   * 后置图标组件
   */
  suffixIcon: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['click'])

const handleClick = (event) => {
  emit('click', event)
}
</script>

<style scoped>
.fc-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--fc-spacing-sm);
  font-weight: var(--fc-font-medium);
  border: 2px solid transparent;
  border-radius: var(--fc-radius-md);
  cursor: pointer;
  transition: all var(--fc-transition-normal) var(--fc-easing);
  user-select: none;
  white-space: nowrap;
  font-family: var(--fc-font-sans);
}

.fc-button:hover:not(.fc-button--disabled):not(.fc-button--loading) {
  transform: translateY(-2px);
}

.fc-button:active:not(.fc-button--disabled):not(.fc-button--loading) {
  transform: translateY(0);
}

.fc-button--disabled,
.fc-button--loading {
  cursor: not-allowed;
  opacity: 0.6;
}

/* 类型样式 */
.fc-button--primary {
  background: linear-gradient(135deg, var(--fc-primary-500) 0%, var(--fc-primary-600) 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.fc-button--primary:hover:not(.fc-button--disabled) {
  background: linear-gradient(135deg, var(--fc-primary-600) 0%, var(--fc-primary-700) 100%);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.fc-button--secondary {
  background: var(--fc-gray-100);
  color: var(--fc-gray-700);
}

.fc-button--secondary:hover:not(.fc-button--disabled) {
  background: var(--fc-gray-200);
}

.fc-button--outline {
  background: transparent;
  border-color: var(--fc-primary-500);
  color: var(--fc-primary-500);
}

.fc-button--outline:hover:not(.fc-button--disabled) {
  background: var(--fc-primary-50);
}

.fc-button--ghost {
  background: transparent;
  color: var(--fc-primary-500);
}

.fc-button--ghost:hover:not(.fc-button--disabled) {
  background: var(--fc-primary-50);
}

.fc-button--danger {
  background: linear-gradient(135deg, var(--fc-error) 0%, var(--fc-error-dark) 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.fc-button--danger:hover:not(.fc-button--disabled) {
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4);
}

/* 尺寸样式 */
.fc-button--sm {
  padding: var(--fc-spacing-sm) var(--fc-spacing-lg);
  font-size: var(--fc-text-sm);
  min-height: 32px;
}

.fc-button--md {
  padding: var(--fc-spacing-md) var(--fc-spacing-xl);
  font-size: var(--fc-text-base);
  min-height: 40px;
}

.fc-button--lg,
.fc-button--large {
  padding: 16px 32px;
  font-size: var(--fc-text-lg);
  min-height: 56px;
  border-radius: var(--fc-radius-lg);
}

/* 块级按钮 */
.fc-button--block {
  width: 100%;
}

/* 圆角按钮 */
.fc-button--round {
  border-radius: var(--fc-radius-full);
}

/* 圆形按钮 */
.fc-button--circle {
  width: var(--button-size, 40px);
  height: var(--button-size, 40px);
  padding: 0;
  border-radius: 50%;
}

/* 加载动画 */
.fc-button__spinner {
  width: 16px;
  height: 16px;
  animation: fc-spin 1s linear infinite;
}

/* 图标 */
.fc-button__icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.fc-button__icon svg {
  width: 16px;
  height: 16px;
}

.fc-button__text {
  line-height: 1;
}

/* 响应式 */
@media (max-width: 768px) {
  .fc-button--lg {
    padding: var(--fc-spacing-md) var(--fc-spacing-lg);
  }
}
</style>
