<template>
  <div class="fc-input-wrapper" :class="wrapperClass">
    <!-- 前置内容 -->
    <div v-if="$slots.prefix || prefixIcon" class="fc-input__prefix">
      <slot name="prefix">
        <component v-if="prefixIcon" :is="prefixIcon" />
      </slot>
    </div>

    <!-- 输入框 -->
    <input
      v-if="type !== 'textarea'"
      ref="inputRef"
      v-model="inputValue"
      :type="computedType"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :maxlength="maxlength"
      :autocomplete="autocomplete"
      class="fc-input"
      @focus="handleFocus"
      @blur="handleBlur"
      @input="handleInput"
      @change="handleChange"
    />

    <!-- 文本域 -->
    <textarea
      v-else
      ref="textareaRef"
      v-model="inputValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :maxlength="maxlength"
      :rows="rows"
      :autocomplete="autocomplete"
      class="fc-textarea"
      @focus="handleFocus"
      @blur="handleBlur"
      @input="handleInput"
      @change="handleChange"
    ></textarea>

    <!-- 清除按钮 -->
    <button
      v-if="clearable && inputValue && !disabled"
      class="fc-input__clear"
      @click="handleClear"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <line x1="18" y1="6" x2="6" y2="18" />
        <line x1="6" y1="6" x2="18" y2="18" />
      </svg>
    </button>

    <!-- 后置内容 -->
    <div v-if="$slots.suffix || suffixIcon" class="fc-input__suffix">
      <slot name="suffix">
        <component v-if="suffixIcon" :is="suffixIcon" />
      </slot>
    </div>

    <!-- 字符计数 -->
    <div v-if="showWordLimit && maxlength" class="fc-input__count">
      {{ inputValue.length }}/{{ maxlength }}
    </div>

    <!-- 错误提示 -->
    <div v-if="errorMessage" class="fc-input__error">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

/**
 * FcInput 输入框组件
 * 
 * @example
 * ```vue
 * <FcInput v-model="value" placeholder="请输入" />
 * <FcInput v-model="value" type="password" clearable />
 * <FcInput v-model="value" :error="true" error-message="输入有误" />
 * ```
 */

const props = defineProps({
  /**
   * 绑定值
   */
  modelValue: {
    type: [String, Number],
    default: ''
  },
  
  /**
   * 输入框类型
   * @values text, password, number, email, tel, url, textarea
   */
  type: {
    type: String,
    default: 'text'
  },
  
  /**
   * 占位符文本
   */
  placeholder: {
    type: String,
    default: ''
  },
  
  /**
   * 是否禁用
   */
  disabled: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否只读
   */
  readonly: {
    type: Boolean,
    default: false
  },
  
  /**
   * 最大长度
   */
  maxlength: {
    type: [String, Number],
    default: undefined
  },
  
  /**
   * 是否显示清除按钮
   */
  clearable: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否显示字符计数
   */
  showWordLimit: {
    type: Boolean,
    default: false
  },
  
  /**
   * 是否有错误
   */
  error: {
    type: Boolean,
    default: false
  },
  
  /**
   * 错误提示信息
   */
  errorMessage: {
    type: String,
    default: ''
  },
  
  /**
   * 前置图标
   */
  prefixIcon: {
    type: Object,
    default: null
  },
  
  /**
   * 后置图标
   */
  suffixIcon: {
    type: Object,
    default: null
  },
  
  /**
   * 自动补全
   */
  autocomplete: {
    type: String,
    default: 'off'
  },
  
  /**
   * 文本域行数
   */
  rows: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['update:modelValue', 'focus', 'blur', 'input', 'change', 'clear'])

const inputRef = ref(null)
const textareaRef = ref(null)
const inputValue = ref(props.modelValue)
const isFocused = ref(false)

// 监听外部值变化
watch(() => props.modelValue, (val) => {
  inputValue.value = val
})

const computedType = computed(() => {
  return props.type === 'textarea' ? 'text' : props.type
})

const wrapperClass = computed(() => ({
  'fc-input-wrapper--focus': isFocused.value,
  'fc-input-wrapper--disabled': props.disabled,
  'fc-input-wrapper--error': props.error || props.errorMessage,
  'fc-input-wrapper--textarea': props.type === 'textarea'
}))

const handleFocus = (event) => {
  isFocused.value = true
  emit('focus', event)
}

const handleBlur = (event) => {
  isFocused.value = false
  emit('blur', event)
}

const handleInput = (event) => {
  inputValue.value = event.target.value
  emit('update:modelValue', inputValue.value)
  emit('input', event)
}

const handleChange = (event) => {
  emit('change', event)
}

const handleClear = () => {
  inputValue.value = ''
  emit('update:modelValue', '')
  emit('clear')
}

// 暴露方法
defineExpose({
  focus: () => {
    if (props.type === 'textarea') {
      textareaRef.value?.focus()
    } else {
      inputRef.value?.focus()
    }
  },
  blur: () => {
    if (props.type === 'textarea') {
      textareaRef.value?.blur()
    } else {
      inputRef.value?.blur()
    }
  }
})
</script>

<style scoped>
.fc-input-wrapper {
  position: relative;
  width: 100%;
}

.fc-input,
.fc-textarea {
  width: 100%;
  padding: var(--fc-spacing-md) var(--fc-spacing-lg);
  background: var(--fc-gray-50);
  border: 2px solid var(--fc-gray-200);
  border-radius: var(--fc-radius-md);
  font-size: var(--fc-text-base);
  color: var(--fc-gray-900);
  transition: all var(--fc-transition-normal) var(--fc-easing);
  font-family: var(--fc-font-sans);
}

.fc-input:hover:not(:disabled),
.fc-textarea:hover:not(:disabled) {
  border-color: var(--fc-primary-300);
}

.fc-input:focus,
.fc-textarea:focus {
  outline: none;
  border-color: var(--fc-primary-500);
  background: white;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.fc-input::placeholder,
.fc-textarea::placeholder {
  color: var(--fc-gray-400);
}

.fc-input:disabled,
.fc-textarea:disabled {
  background: var(--fc-gray-100);
  cursor: not-allowed;
  opacity: 0.6;
}

/* 文本域 */
.fc-textarea {
  resize: vertical;
  min-height: 100px;
  line-height: 1.6;
}

/* 前后缀 */
.fc-input__prefix,
.fc-input__suffix {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--fc-gray-400);
  pointer-events: none;
}

.fc-input__prefix {
  left: var(--fc-spacing-md);
}

.fc-input__suffix {
  right: var(--fc-spacing-md);
}

.fc-input__prefix svg,
.fc-input__suffix svg {
  width: 20px;
  height: 20px;
}

/* 有前缀时调整输入框padding */
.fc-input-wrapper:has(.fc-input__prefix) .fc-input {
  padding-left: calc(var(--fc-spacing-lg) + 20px + var(--fc-spacing-sm));
}

.fc-input-wrapper:has(.fc-input__suffix) .fc-input {
  padding-right: calc(var(--fc-spacing-lg) + 20px + var(--fc-spacing-sm));
}

/* 清除按钮 */
.fc-input__clear {
  position: absolute;
  right: var(--fc-spacing-md);
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--fc-gray-400);
  transition: color var(--fc-transition-fast) var(--fc-easing);
}

.fc-input__clear:hover {
  color: var(--fc-gray-600);
}

.fc-input__clear svg {
  width: 16px;
  height: 16px;
}

/* 字符计数 */
.fc-input__count {
  position: absolute;
  right: var(--fc-spacing-md);
  bottom: -20px;
  font-size: var(--fc-text-xs);
  color: var(--fc-gray-500);
}

/* 错误提示 */
.fc-input__error {
  margin-top: var(--fc-spacing-sm);
  font-size: var(--fc-text-sm);
  color: var(--fc-error);
  animation: fc-slide-down var(--fc-transition-normal) var(--fc-easing);
}

/* 错误状态 */
.fc-input-wrapper--error .fc-input,
.fc-input-wrapper--error .fc-textarea {
  border-color: var(--fc-error);
}

.fc-input-wrapper--error .fc-input:focus,
.fc-input-wrapper--error .fc-textarea:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

/* 响应式 */
@media (max-width: 768px) {
  .fc-input,
  .fc-textarea {
    padding: var(--fc-spacing-sm) var(--fc-spacing-md);
  }
}
</style>
