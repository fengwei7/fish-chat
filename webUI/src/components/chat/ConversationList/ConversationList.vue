<template>
  <div class="conversation-list">
    <!-- 搜索框 -->
    <div class="conversation-list__search">
      <FcInput
        v-model="searchKeyword"
        placeholder="搜索会话..."
        size="large"
        clearable
      >
        <template #prefix>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
        </template>
      </FcInput>
    </div>

    <!-- 分类标签 -->
    <div class="conversation-list__tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="conversation-list__tab"
        :class="{ 'conversation-list__tab--active': activeTab === tab.value }"
        @click="handleTabClick(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="conversation-list__loading">
      <FcSkeleton :rows="5" :avatar="true" />
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredConversations.length === 0" class="conversation-list__empty">
      <!-- 好友tab -->
      <template v-if="activeTab === 'PRIVATE'">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="8.5" cy="7" r="4"/>
          <line x1="20" y1="8" x2="20" y2="14"/>
          <line x1="23" y1="11" x2="17" y2="11"/>
        </svg>
        <p>暂无好友会话</p>
        <FcButton type="primary" size="sm" @click="$emit('addFriend')">
          添加好友
        </FcButton>
      </template>

      <!-- 群组tab -->
      <template v-else-if="activeTab === 'GROUP'">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>
        <p>暂无群组会话</p>
        <div class="conversation-list__empty-actions">
          <FcButton type="primary" size="sm" @click="$emit('createGroup')">
            创建群组
          </FcButton>
        </div>
      </template>

      <!-- 频道tab -->
      <template v-else-if="activeTab === 'CHANNEL'">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M19 20H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v1m2 13a2 2 0 0 1-2-2V7m2 13a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/>
        </svg>
        <p>暂无频道会话</p>
        <FcButton type="primary" size="sm" @click="$emit('subscribeChannel')">
          订阅频道
        </FcButton>
      </template>

      <!-- 其他tab（全部/我的） -->
      <template v-else>
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <p>暂无会话</p>
        <FcButton type="primary" size="sm" @click="$emit('create')">
          发起聊天
        </FcButton>
      </template>
    </div>

    <!-- 会话列表 -->
    <div v-else class="conversation-list__container">
      <ConversationItem
        v-for="conv in filteredConversations"
        :key="conv.id"
        :conversation="conv"
        :active="conv.id === activeId"
        @click="$emit('select', conv)"
        @delete="$emit('delete', conv)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { FcInput, FcButton, FcSkeleton } from '@/components/ui/index.js'
import ConversationItem from '../ConversationItem/ConversationItem.vue'

const props = defineProps({
  conversations: {
    type: Array,
    default: () => []
  },
  activeId: {
    type: [String, Number],
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'search', 'create', 'openProfile', 'addFriend', 'createGroup', 'subscribeChannel'])

// 搜索关键词
const searchKeyword = ref('')

// 分类标签
const tabs = [
  { label: '全部', value: 'all' },
  { label: '好友', value: 'PRIVATE' },
  { label: '群组', value: 'GROUP' },
  { label: '频道', value: 'CHANNEL' },
  { label: '我的', value: 'profile' } // 个人资料
]

const activeTab = ref('all')

// 过滤后的会话列表
const filteredConversations = computed(() => {
  let result = props.conversations

  // 按类型过滤
  if (activeTab.value !== 'all' && activeTab.value !== 'profile') {
    result = result.filter(conv => conv.roomType === activeTab.value)
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(conv =>
      (conv.name && conv.name.toLowerCase().includes(keyword)) ||
      (conv.nickname && conv.nickname.toLowerCase().includes(keyword))
    )
  }

  return result
})

// 处理tab点击
function handleTabClick(value) {
  if (value === 'profile') {
    // 点击「我的」tab，触发打开个人资料
    emit('openProfile')
  } else {
    activeTab.value = value
  }
}

// 监听搜索关键词变化
watch(searchKeyword, (newVal) => {
  emit('search', newVal)
})

// 监听分类标签变化
watch(activeTab, (newVal) => {
  emit('search', searchKeyword.value, newVal)
})
</script>

<style scoped>
.conversation-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--fc-bg-primary);
}

/* 搜索框 */
.conversation-list__search {
  padding: 16px;
  border-bottom: 1px solid var(--fc-border);
}

/* 分类标签 */
.conversation-list__tabs {
  display: flex;
  padding: 12px;
  gap: 6px;
  border-bottom: 1px solid var(--fc-border);
  overflow-x: auto;
}

.conversation-list__tabs::-webkit-scrollbar {
  height: 0;
}

.conversation-list__tab {
  flex: 1;
  min-width: fit-content;
  padding: 8px 10px;
  font-size: var(--fc-text-sm);
  color: var(--fc-text-secondary);
  background: transparent;
  border: none;
  border-radius: var(--fc-radius-sm);
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.conversation-list__tab:hover {
  background: var(--fc-bg-hover);
  color: var(--fc-text-primary);
}

.conversation-list__tab--active {
  background: var(--fc-primary-light);
  color: var(--fc-primary);
  font-weight: 500;
}

/* 加载状态 */
.conversation-list__loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

/* 空状态 */
.conversation-list__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--fc-text-secondary);
}

.conversation-list__empty svg {
  opacity: 0.3;
}

.conversation-list__empty p {
  margin: 0;
  font-size: var(--fc-text-base);
}

.conversation-list__empty-actions {
  display: flex;
  gap: 8px;
}

/* 会话列表容器 */
.conversation-list__container {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.conversation-list__container::-webkit-scrollbar {
  width: 6px;
}

.conversation-list__container::-webkit-scrollbar-thumb {
  background: var(--fc-border);
  border-radius: 3px;
}

.conversation-list__container::-webkit-scrollbar-thumb:hover {
  background: var(--fc-text-secondary);
}
</style>
