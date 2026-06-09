<template>
  <div class="fc-card" :class="cardClass" @click="handleClick">
    <!-- 头部 -->
    <div v-if="$slots.header || title" class="fc-card__header">
      <slot name="header">
        <h3 class="fc-card__title">{{ title }}</h3>
      </slot>
    </div>

    <!-- 封面图片 -->
    <div v-if="cover" class="fc-card__cover">
      <img :src="cover" :alt="title" />
    </div>

    <!-- 内容 -->
    <div class="fc-card__body">
      <slot></slot>
    </div>

    <!-- 底部 -->
    <div v-if="$slots.footer" class="fc-card__footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
/**
 * FcCard 卡片组件
 * 
 * @example
 * ```vue
 * <FcCard title="标题">
 *   <p>内容</p>
 * </FcCard>
 * 
 * <FcCard shadow hoverable>
 *   <slot>可悬停的卡片</slot>
 * </FcCard>
 * ```
 */

const props = defineProps({
  /**
   * 标题
   */
  title: {
    type: String,
    default: ''
  },
  
  /**
   * 封面图片
   */
  cover: {
    type: String,
    default: ''
  },
  
  /**
   * 样式类型
   * @values default, bordered, shadow
   */
  variant: {
    type: String,
    default: 'default'
  },
  
  /**
   * 是否显示阴影
   */
  shadow: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否可悬停
   */
  hoverable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const cardClass = {
  'fc-card--bordered': props.variant === 'bordered',
  'fc-card--shadow': props.shadow || props.variant === 'shadow',
  'fc-card--hoverable': props.hoverable
}

const handleClick = (event) => {
  emit('click', event)
}
</script>

<style scoped>
.fc-card {
  background: white;
  border-radius: var(--fc-radius-lg);
  overflow: hidden;
  transition: all var(--fc-transition-normal) var(--fc-easing);
}

/* 边框样式 */
.fc-card--bordered {
  border: var(--fc-border);
}

/* 阴影样式 */
.fc-card--shadow {
  box-shadow: var(--fc-shadow-md);
}

/* 可悬停 */
.fc-card--hoverable:hover {
  transform: translateY(-4px);
  box-shadow: var(--fc-shadow-xl);
}

/* 头部 */
.fc-card__header {
  padding: var(--fc-spacing-xl) var(--fc-spacing-2xl);
  border-bottom: var(--fc-border);
}

.fc-card__title {
  margin: 0;
  font-size: var(--fc-text-lg);
  font-weight: var(--fc-font-semibold);
  color: var(--fc-gray-900);
}

/* 封面 */
.fc-card__cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
}

.fc-card__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--fc-transition-slow) var(--fc-easing);
}

.fc-card--hoverable:hover .fc-card__cover img {
  transform: scale(1.05);
}

/* 内容 */
.fc-card__body {
  padding: var(--fc-spacing-2xl);
}

/* 底部 */
.fc-card__footer {
  padding: var(--fc-spacing-lg) var(--fc-spacing-2xl);
  border-top: var(--fc-border);
}

/* 响应式 */
@media (max-width: 768px) {
  .fc-card__header,
  .fc-card__body,
  .fc-card__footer {
    padding-left: var(--fc-spacing-lg);
    padding-right: var(--fc-spacing-lg);
  }
}
</style>
