<template>
  <div v-if="visible" class="chat-emoji-picker" @click.stop>
    <!-- 分类标签 -->
    <div class="chat-emoji-picker__tabs">
      <button
        v-for="category in categories"
        :key="category.name"
        class="chat-emoji-picker__tab"
        :class="{ 'chat-emoji-picker__tab--active': activeCategory === category.name }"
        @click="activeCategory = category.name"
      >
        {{ category.icon }}
      </button>
    </div>

    <!-- 表情网格 -->
    <div class="chat-emoji-picker__grid">
      <button
        v-for="emoji in currentEmojis"
        :key="emoji"
        class="chat-emoji-picker__emoji"
        @click="handleSelect(emoji)"
      >
        {{ emoji }}
      </button>
    </div>

    <!-- 最近使用 -->
    <div v-if="recentEmojis.length > 0" class="chat-emoji-picker__recent">
      <div class="chat-emoji-picker__recent-title">最近使用</div>
      <div class="chat-emoji-picker__recent-list">
        <button
          v-for="emoji in recentEmojis"
          :key="emoji"
          class="chat-emoji-picker__emoji"
          @click="handleSelect(emoji)"
        >
          {{ emoji }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

/**
 * ChatEmojiPicker 表情选择器组件
 * 
 * @example
 * ```vue
 * <ChatEmojiPicker v-model:visible="showPicker" @select="handleEmojiSelect" />
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
   * 最大最近使用数量
   */
  maxRecent: {
    type: Number,
    default: 20
  }
})

const emit = defineEmits(['select', 'update:visible'])

const activeCategory = ref('smileys')
const recentEmojis = ref([])

// 表情分类
const categories = [
  { name: 'smileys', icon: '😀' },
  { name: 'animals', icon: '🐱' },
  { name: 'food', icon: '🍕' },
  { name: 'activities', icon: '⚽' },
  { name: 'travel', icon: '🚗' },
  { name: 'objects', icon: '💡' },
  { name: 'symbols', icon: '❤️' },
  { name: 'flags', icon: '🏁' }
]

// 表情数据
const emojiData = {
  smileys: [
    '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '😊',
    '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙', '😋', '😛',
    '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨',
    '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌', '😔',
    '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥵',
    '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓', '🧐', '😕'
  ],
  animals: [
    '🐱', '🐶', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯',
    '🦁', '🐮', '🐷', '🐸', '🐵', '🐔', '🐧', '🐦', '🐤', '🦆',
    '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄', '🐝', '🐛', '🦋',
    '🐌', '🐞', '🐜', '🦟', '🦗', '🕷', '🦂', '🐢', '🐍', '🦎',
    '🦖', '🦕', '🐙', '🦑', '🦐', '🦞', '🦀', '🐡', '🐠', '🐟'
  ],
  food: [
    '🍕', '🍔', '🍟', '🌭', '🍿', '🧂', '🥓', '🥚', '🍳', '🧇',
    '🥞', '🧈', '🍞', '🥐', '🥨', '🥯', '🥖', '🧀', '🥗', '🥙',
    '🥪', '🌮', '🌯', '🫔', '🥘', '🍲', '🍜', '🍝', '🍠', '🍢',
    '🍣', '🍤', '🍥', '🥮', '🍡', '🧁', '🍰', '🎂', '🍮', '🍭',
    '🍬', '🍫', '🍩', '🍪', '🌰', '🥜', '🍯', '🥛', '🍼', '☕'
  ],
  activities: [
    '⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉', '🥏', '🎱',
    '🪀', '🏓', '🏸', '🏒', '🏑', '🥍', '🏏', '🥅', '⛳', '🪁',
    '🏹', '🎣', '🤿', '🥊', '🥋', '🎽', '🛹', '🛷', '⛸', '🥌',
    '🎿', '⛷', '🏂', '🪂', '🏋', '🤼', '🤸', '⛹', '🤺', '🤾',
    '🏌', '🏇', '🧘', '🏄', '🏊', '🤽', '🚣', '🧗', '🚵', '🚴'
  ],
  travel: [
    '🚗', '🚕', '🚙', '🚌', '🚎', '🏎', '🚓', '🚑', '🚒', '🚐',
    '🚚', '🚛', '🚜', '🦯', '🦽', '🦼', '🛴', '🚲', '🛵', '🏍',
    '🛺', '🚨', '🚔', '🚍', '🚘', '🚖', '🚡', '🚠', '🚟', '🚃',
    '🚋', '🚞', '🚝', '🚄', '🚅', '🚈', '🚂', '🚆', '🚇', '🚊',
    '🚉', '✈️', '🛫', '🛬', '💺', '🛰', '🚀', '🛸', '🚁', '🛶'
  ],
  objects: [
    '💡', '🔦', '🕯', '🧯', '🛢', '💸', '💵', '💴', '💶', '💷',
    '💰', '💳', '💎', '⚖️', '🧰', '🔧', '🔨', '⚒', '🛠', '⛏',
    '🔩', '⚙️', '🧲', '🔬', '🔭', '📡', '💉', '🩸', '💊', '🩹',
    '🧬', '🦠', '🧫', '🧪', '🌡', '🧹', '🧺', '🧻', '🚽', '🚰',
    '🚿', '🛁', '🛀', '🧼', '🪒', '🧽', '🔑', '🗝', '🚪', '🪑'
  ],
  symbols: [
    '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
    '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️',
    '✝️', '☪️', '🕉', '☸️', '✡️', '🔯', '🕎', '☯️', '☦️', '🛐',
    '⛎', '♈', '♉', '♊', '♋', '♌', '♍', '♎', '♏', '♐',
    '♑', '♒', '♓', '🆔', '⚛️', '🉑', '☢️', '☣️', '📴', '📳',
    '🈶', '🈚', '🈸', '🈺', '🈷️', '✴️', '🆚', '💮', '🉐', '㊙️'
  ],
  flags: [
    '🏁', '🚩', '🎌', '🏴', '🏳️', '🏳️‍🌈', '🏳️‍⚧️', '🏴‍☠️', '🇨🇳', '🇺🇸',
    '🇬🇧', '🇯🇵', '🇰🇷', '🇫🇷', '🇩🇪', '🇮🇹', '🇪🇸', '🇷🇺', '🇨🇦', '🇦🇺',
    '🇧🇷', '🇮🇳', '🇲🇽', '🇿🇦', '🇸🇦', '🇦🇪', '🇹🇷', '🇸🇬', '🇹🇭', '🇻🇳'
  ]
}

const currentEmojis = computed(() => {
  return emojiData[activeCategory.value] || []
})

const handleSelect = (emoji) => {
  emit('select', emoji)
  
  // 添加到最近使用
  if (!recentEmojis.value.includes(emoji)) {
    recentEmojis.value.unshift(emoji)
    if (recentEmojis.value.length > props.maxRecent) {
      recentEmojis.value.pop()
    }
  }
}

// 点击外部关闭
const handleClickOutside = (event) => {
  if (props.visible && !event.target.closest('.chat-emoji-picker')) {
    emit('update:visible', false)
  }
}

watch(() => props.visible, (visible) => {
  if (visible) {
    setTimeout(() => {
      document.addEventListener('click', handleClickOutside)
    }, 0)
  } else {
    document.removeEventListener('click', handleClickOutside)
  }
})
</script>

<style scoped>
.chat-emoji-picker {
  position: absolute;
  bottom: 100%;
  left: 0;
  width: 320px;
  background: white;
  border-radius: var(--fc-radius-lg);
  box-shadow: var(--fc-shadow-xl);
  border: 1px solid var(--fc-border);
  z-index: var(--fc-z-popover);
  animation: fc-slide-up var(--fc-transition-fast) var(--fc-easing);
}

/* 分类标签 */
.chat-emoji-picker__tabs {
  display: flex;
  gap: 4px;
  padding: 12px;
  border-bottom: 1px solid var(--fc-border);
  overflow-x: auto;
}

.chat-emoji-picker__tab {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--fc-radius-md);
  cursor: pointer;
  font-size: 20px;
  transition: all var(--fc-transition-fast) var(--fc-easing);
  flex-shrink: 0;
}

.chat-emoji-picker__tab:hover {
  background: var(--fc-gray-100);
}

.chat-emoji-picker__tab--active {
  background: var(--fc-primary-100);
}

/* 表情网格 */
.chat-emoji-picker__grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  padding: 12px;
  max-height: 240px;
  overflow-y: auto;
}

.chat-emoji-picker__grid::-webkit-scrollbar {
  width: 6px;
}

.chat-emoji-picker__grid::-webkit-scrollbar-thumb {
  background: var(--fc-gray-300);
  border-radius: var(--fc-radius-full);
}

.chat-emoji-picker__emoji {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--fc-radius-sm);
  cursor: pointer;
  font-size: 20px;
  transition: all var(--fc-transition-fast) var(--fc-easing);
}

.chat-emoji-picker__emoji:hover {
  background: var(--fc-gray-100);
  transform: scale(1.2);
}

/* 最近使用 */
.chat-emoji-picker__recent {
  border-top: 1px solid var(--fc-border);
  padding: 12px;
}

.chat-emoji-picker__recent-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--fc-gray-600);
  margin-bottom: 8px;
}

.chat-emoji-picker__recent-list {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-emoji-picker {
    width: 280px;
  }
  
  .chat-emoji-picker__grid {
    grid-template-columns: repeat(7, 1fr);
  }
}
</style>
