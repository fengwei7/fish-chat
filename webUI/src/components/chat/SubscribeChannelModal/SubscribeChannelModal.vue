<template>
  <FcModal
    :visible="modelValue"
    title="订阅频道"
    size="md"
    @update:visible="handleUpdateVisible"
    @close="handleClose"
  >
    <div class="subscribe-channel">
      <!-- 搜索框 -->
      <div class="subscribe-channel__search">
        <FcInput
          v-model="searchKeyword"
          placeholder="搜索频道名称..."
          size="large"
          clearable
          @search="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.35-4.35"/>
            </svg>
          </template>
          <template #suffix>
            <FcButton type="primary" size="sm" @click="handleSearch">搜索</FcButton>
          </template>
        </FcInput>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="subscribe-channel__loading">
        <FcSkeleton :rows="5" :avatar="true" />
      </div>

      <!-- 空状态 -->
      <div v-else-if="!hasSearched" class="subscribe-channel__empty">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M19 20H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v1m2 13a2 2 0 0 1-2-2V7m2 13a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/>
        </svg>
        <p>搜索并订阅感兴趣的频道</p>
      </div>

      <div v-else-if="channelList.length === 0" class="subscribe-channel__empty">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.35-4.35"/>
        </svg>
        <p>未找到相关频道</p>
      </div>

      <!-- 频道列表 -->
      <div v-else class="subscribe-channel__list">
        <div
          v-for="channel in channelList"
          :key="channel.code"
          class="subscribe-channel__item"
        >
          <div class="subscribe-channel__item-info">
            <div class="subscribe-channel__item-name">{{ channel.name }}</div>
            <div class="subscribe-channel__item-desc">{{ channel.description || '暂无描述' }}</div>
            <div v-if="channel.subscriberCount" class="subscribe-channel__item-stats">
              {{ channel.subscriberCount }} 订阅者
            </div>
          </div>
          <FcButton
            :type="channel.subscribed ? 'default' : 'primary'"
            size="sm"
            :loading="channel.subscribing"
            :disabled="channel.subscribed || channel.subscribing"
            @click="handleSubscribe(channel)"
          >
            {{ channel.subscribed ? '已订阅' : channel.subscribing ? '订阅中...' : '订阅' }}
          </FcButton>
        </div>
      </div>
    </div>
  </FcModal>
</template>

<script setup>
import { ref } from 'vue'
import { FcModal, FcInput, FcButton, FcSkeleton } from '@/components/ui/index.js'
import { searchChannels, subscribeChannel } from '@/api/channel.js'
import { Message } from '@/components/ui/index.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'subscribed'])

// 处理 Modal 可见性更新
function handleUpdateVisible(val) {
  emit('update:modelValue', val)
}

// 搜索关键词
const searchKeyword = ref('')
const channelList = ref([])
const loading = ref(false)
const hasSearched = ref(false)

// 搜索频道
async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    Message.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  hasSearched.value = true
  try {
    const res = await searchChannels({
      keyword: searchKeyword.value.trim(),
      pageNum: 1,
      pageSize: 20
    })
    
    channelList.value = (res.data?.records || []).map(channel => ({
      ...channel,
      subscribing: false,
      subscribed: false // TODO: 后端需要返回是否已订阅的标识
    }))
  } catch (error) {
    console.error('[SubscribeChannel] 搜索频道失败:', error)
    Message.error('搜索失败，请重试')
  } finally {
    loading.value = false
  }
}

// 订阅频道
async function handleSubscribe(channel) {
  channel.subscribing = true
  try {
    await subscribeChannel(channel.code)
    
    Message.success('订阅成功')
    emit('subscribed', channel)
    
    // 标记为已订阅
    channel.subscribed = true
  } catch (error) {
    console.error('[SubscribeChannel] 订阅频道失败:', error)
    Message.error('订阅失败，请重试')
  } finally {
    channel.subscribing = false
  }
}

// 关闭
function handleClose() {
  emit('update:modelValue', false)
  // 重置状态
  searchKeyword.value = ''
  channelList.value = []
  hasSearched.value = false
}
</script>

<style scoped>
.subscribe-channel {
  display: flex;
  flex-direction: column;
  height: 400px;
}

/* 搜索框 */
.subscribe-channel__search {
  margin-bottom: 16px;
}

/* 加载状态 */
.subscribe-channel__loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

/* 空状态 */
.subscribe-channel__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--fc-text-secondary);
}

.subscribe-channel__empty svg {
  opacity: 0.3;
}

.subscribe-channel__empty p {
  margin: 0;
  font-size: var(--fc-text-sm);
}

/* 列表 */
.subscribe-channel__list {
  flex: 1;
  overflow-y: auto;
}

.subscribe-channel__list::-webkit-scrollbar {
  width: 6px;
}

.subscribe-channel__list::-webkit-scrollbar-thumb {
  background: var(--fc-border);
  border-radius: 3px;
}

/* 列表项 */
.subscribe-channel__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: var(--fc-radius);
  transition: background 0.2s ease;
}

.subscribe-channel__item:hover {
  background: var(--fc-bg-hover);
}

.subscribe-channel__item-info {
  flex: 1;
  min-width: 0;
}

.subscribe-channel__item-name {
  font-size: var(--fc-text-base);
  color: var(--fc-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.subscribe-channel__item-desc {
  font-size: var(--fc-text-xs);
  color: var(--fc-text-secondary);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.subscribe-channel__item-stats {
  font-size: var(--fc-text-xs);
  color: var(--fc-text-secondary);
  margin-top: 4px;
}
</style>
