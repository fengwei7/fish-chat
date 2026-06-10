<template>
  <FcModal
    :visible="modelValue"
    title="发起聊天"
    size="md"
    @update:visible="handleUpdateVisible"
    @close="handleClose"
  >
    <div class="create-conversation">
      <!-- 选项卡 -->
      <div class="create-conversation__tabs">
        <button
          class="create-conversation__tab"
          :class="{ 'create-conversation__tab--active': activeTab === 'friend' }"
          @click="activeTab = 'friend'"
        >
          好友
        </button>
        <button
          class="create-conversation__tab"
          :class="{ 'create-conversation__tab--active': activeTab === 'group' }"
          @click="activeTab = 'group'"
        >
          群组
        </button>
      </div>

      <!-- 搜索框 -->
      <div class="create-conversation__search">
        <FcInput
          v-model="searchKeyword"
          placeholder="搜索..."
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

      <!-- 加载状态 -->
      <div v-if="loading" class="create-conversation__loading">
        <FcSkeleton :rows="5" :avatar="true" />
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredList.length === 0" class="create-conversation__empty">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/>
          <path d="M8 15s1.5 2 4 2 4-2 4-2"/>
          <line x1="9" y1="9" x2="9.01" y2="9"/>
          <line x1="15" y1="9" x2="15.01" y2="9"/>
        </svg>
        <p>{{ activeTab === 'friend' ? '暂无好友' : '暂无群组' }}</p>
      </div>

      <!-- 列表 -->
      <div v-else class="create-conversation__list">
        <div
          v-for="item in filteredList"
          :key="item.code"
          class="create-conversation__item"
          :class="{ 'create-conversation__item--selected': selectedCode === item.code }"
          @click="handleSelect(item)"
        >
          <FcAvatar
            :src="item.avatar"
            :size="40"
            :name="item.name"
          />
          <div class="create-conversation__item-info">
            <div class="create-conversation__item-name">{{ item.name }}</div>
            <div v-if="item.signature" class="create-conversation__item-desc">{{ item.signature }}</div>
          </div>
          <svg
            v-if="selectedCode === item.code"
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="var(--fc-primary)"
            stroke-width="2"
          >
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
      </div>
    </div>

    <template #footer>
      <FcButton @click="handleClose">取消</FcButton>
      <FcButton
        type="primary"
        :disabled="!selectedCode"
        :loading="submitting"
        @click="handleSubmit"
      >
        发起聊天
      </FcButton>
    </template>
  </FcModal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { FcModal, FcInput, FcButton, FcAvatar, FcSkeleton } from '@/components/ui/index.js'
import { listFriends } from '@/api/friend.js'
import { listMyGroups } from '@/api/group.js'
import { Message } from '@/components/ui/index.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'create'])

// 处理 Modal 可见性更新
function handleUpdateVisible(val) {
  emit('update:modelValue', val)
}

// 选项卡
const activeTab = ref('friend') // friend | group

// 搜索关键词
const searchKeyword = ref('')

// 数据列表
const friendList = ref([])
const groupList = ref([])
const loading = ref(false)
const submitting = ref(false)

// 选中的目标
const selectedCode = ref('')
const selectedItem = ref(null)

// 过滤后的列表
const filteredList = computed(() => {
  const list = activeTab.value === 'friend' ? friendList.value : groupList.value
  
  if (!searchKeyword.value) return list
  
  const keyword = searchKeyword.value.toLowerCase()
  return list.filter(item => 
    item.name.toLowerCase().includes(keyword)
  )
})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 'friend') {
      const res = await listFriends({ pageNum: 1, pageSize: 100 })
      friendList.value = res.data?.records?.map(f => ({
        code: f.code || f.userCode,
        name: f.remark || f.nickname || f.username,
        avatar: f.avatar,
        signature: f.signature
      })) || []
    } else {
      const res = await listMyGroups({ pageNum: 1, pageSize: 100 })
      groupList.value = res.data?.records?.map(g => ({
        code: g.code,
        name: g.name,
        avatar: g.avatar,
        signature: `${g.memberCount || 0} 人`
      })) || []
    }
  } catch (error) {
    console.error('[CreateConversation] 加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择项目
function handleSelect(item) {
  selectedCode.value = item.code
  selectedItem.value = item
}

// 提交
async function handleSubmit() {
  if (!selectedCode.value) return
  
  submitting.value = true
  try {
    emit('create', {
      targetType: activeTab.value === 'friend' ? 'USER' : 'GROUP',
      targetCode: selectedCode.value,
      targetName: selectedItem.value?.name
    })
    handleClose()
  } catch (error) {
    console.error('[CreateConversation] 创建会话失败:', error)
  } finally {
    submitting.value = false
  }
}

// 关闭
function handleClose() {
  emit('update:modelValue', false)
  // 重置状态
  selectedCode.value = ''
  selectedItem.value = null
  searchKeyword.value = ''
}

// 监听选项卡变化
watch(activeTab, () => {
  selectedCode.value = ''
  selectedItem.value = null
  loadData()
})

// 监听弹窗打开
watch(() => props.modelValue, (val) => {
  if (val) {
    loadData()
  }
})
</script>

<style scoped>
.create-conversation {
  display: flex;
  flex-direction: column;
  height: 400px;
}

/* 选项卡 */
.create-conversation__tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.create-conversation__tab {
  flex: 1;
  padding: 10px;
  font-size: var(--fc-text-base);
  color: var(--fc-text-secondary);
  background: var(--fc-bg-secondary);
  border: 1px solid var(--fc-border);
  border-radius: var(--fc-radius);
  cursor: pointer;
  transition: all 0.2s ease;
}

.create-conversation__tab:hover {
  color: var(--fc-text-primary);
  border-color: var(--fc-primary-light);
}

.create-conversation__tab--active {
  color: var(--fc-primary);
  background: var(--fc-primary-light);
  border-color: var(--fc-primary);
  font-weight: 500;
}

/* 搜索框 */
.create-conversation__search {
  margin-bottom: 16px;
}

/* 加载状态 */
.create-conversation__loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

/* 空状态 */
.create-conversation__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--fc-text-secondary);
}

.create-conversation__empty svg {
  opacity: 0.3;
}

.create-conversation__empty p {
  margin: 0;
  font-size: var(--fc-text-sm);
}

/* 列表 */
.create-conversation__list {
  flex: 1;
  overflow-y: auto;
}

.create-conversation__list::-webkit-scrollbar {
  width: 6px;
}

.create-conversation__list::-webkit-scrollbar-thumb {
  background: var(--fc-border);
  border-radius: 3px;
}

/* 列表项 */
.create-conversation__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--fc-radius);
  cursor: pointer;
  transition: all 0.2s ease;
}

.create-conversation__item:hover {
  background: var(--fc-bg-hover);
}

.create-conversation__item--selected {
  background: var(--fc-primary-light);
}

.create-conversation__item-info {
  flex: 1;
  min-width: 0;
}

.create-conversation__item-name {
  font-size: var(--fc-text-base);
  color: var(--fc-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.create-conversation__item-desc {
  font-size: var(--fc-text-xs);
  color: var(--fc-text-secondary);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
