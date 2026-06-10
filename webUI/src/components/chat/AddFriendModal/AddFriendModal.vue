<template>
  <FcModal
    :visible="modelValue"
    title="添加好友"
    size="md"
    @update:visible="handleUpdateVisible"
    @close="handleClose"
  >
    <div class="add-friend">
      <!-- 搜索框 -->
      <div class="add-friend__search">
        <FcInput
          v-model="searchKeyword"
          placeholder="搜索用户名或昵称..."
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
      <div v-if="loading" class="add-friend__loading">
        <FcSkeleton :rows="5" :avatar="true" />
      </div>

      <!-- 空状态 -->
      <div v-else-if="!hasSearched" class="add-friend__empty">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="8.5" cy="7" r="4"/>
          <line x1="20" y1="8" x2="20" y2="14"/>
          <line x1="23" y1="11" x2="17" y2="11"/>
        </svg>
        <p>搜索用户并添加好友</p>
      </div>

      <div v-else-if="userList.length === 0" class="add-friend__empty">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.35-4.35"/>
        </svg>
        <p>未找到相关用户</p>
      </div>

      <!-- 用户列表 -->
      <div v-else class="add-friend__list">
        <div
          v-for="user in userList"
          :key="user.code"
          class="add-friend__item"
        >
          <div class="add-friend__item-info">
            <div class="add-friend__item-name">{{ user.nickname || user.username }}</div>
            <div class="add-friend__item-desc">{{ user.signature || '暂无签名' }}</div>
            <div v-if="user.isFriend" class="add-friend__item-status">已是好友</div>
          </div>
          <FcButton
            v-if="!user.isFriend"
            :type="user.adding ? 'default' : 'primary'"
            size="sm"
            :loading="user.adding"
            :disabled="user.adding"
            @click="handleAddFriend(user)"
          >
            {{ user.adding ? '发送中...' : '添加好友' }}
          </FcButton>
        </div>
      </div>
    </div>
  </FcModal>
</template>

<script setup>
import { ref } from 'vue'
import { FcModal, FcInput, FcButton, FcSkeleton } from '@/components/ui/index.js'
import { searchFriends, addFriend } from '@/api/friend.js'
import { Message } from '@/components/ui/index.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'added'])

// 处理 Modal 可见性更新
function handleUpdateVisible(val) {
  emit('update:modelValue', val)
}

// 搜索关键词
const searchKeyword = ref('')
const userList = ref([])
const loading = ref(false)
const hasSearched = ref(false)

// 搜索用户
async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    Message.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  hasSearched.value = true
  try {
    const res = await searchFriends({
      keyword: searchKeyword.value.trim(),
      pageNum: 1,
      pageSize: 20
    })
    
    userList.value = (res.data?.records || []).map(user => ({
      ...user,
      adding: false,
      isFriend: false // TODO: 后端需要返回是否已是好友的标识
    }))
  } catch (error) {
    console.error('[AddFriend] 搜索用户失败:', error)
    Message.error('搜索失败，请重试')
  } finally {
    loading.value = false
  }
}

// 添加好友
async function handleAddFriend(user) {
  user.adding = true
  try {
    await addFriend({
      friendCode: user.code,
      remark: user.nickname || user.username
    })
    
    Message.success('好友申请已发送')
    emit('added', user)
    
    // 标记为已申请
    user.isFriend = true
  } catch (error) {
    console.error('[AddFriend] 添加好友失败:', error)
    Message.error('添加失败，请重试')
  } finally {
    user.adding = false
  }
}

// 关闭
function handleClose() {
  emit('update:modelValue', false)
  // 重置状态
  searchKeyword.value = ''
  userList.value = []
  hasSearched.value = false
}
</script>

<style scoped>
.add-friend {
  display: flex;
  flex-direction: column;
  height: 400px;
}

/* 搜索框 */
.add-friend__search {
  margin-bottom: 16px;
}

/* 加载状态 */
.add-friend__loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

/* 空状态 */
.add-friend__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--fc-text-secondary);
}

.add-friend__empty svg {
  opacity: 0.3;
}

.add-friend__empty p {
  margin: 0;
  font-size: var(--fc-text-sm);
}

/* 列表 */
.add-friend__list {
  flex: 1;
  overflow-y: auto;
}

.add-friend__list::-webkit-scrollbar {
  width: 6px;
}

.add-friend__list::-webkit-scrollbar-thumb {
  background: var(--fc-border);
  border-radius: 3px;
}

/* 列表项 */
.add-friend__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: var(--fc-radius);
  transition: background 0.2s ease;
}

.add-friend__item:hover {
  background: var(--fc-bg-hover);
}

.add-friend__item-info {
  flex: 1;
  min-width: 0;
}

.add-friend__item-name {
  font-size: var(--fc-text-base);
  color: var(--fc-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.add-friend__item-desc {
  font-size: var(--fc-text-xs);
  color: var(--fc-text-secondary);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.add-friend__item-status {
  font-size: var(--fc-text-xs);
  color: var(--fc-primary);
  margin-top: 4px;
}
</style>
