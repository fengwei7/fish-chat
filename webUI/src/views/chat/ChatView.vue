<template>
  <ChatLayout ref="layoutRef">
    <!-- 左侧：会话列表 -->
    <template #sidebar>
      <ConversationList
        :conversations="conversations"
        :active-id="activeConversation?.id"
        :loading="loadingConversations"
        @select="handleSelectConversation"
        @create="showCreateModal = true"
        @delete="handleDeleteConversation"
        @open-profile="showProfileDrawer = true"
        @add-friend="showAddFriendModal = true"
        @create-group="showCreateGroupModal = true"
        @subscribe-channel="showSubscribeChannelModal = true"
      />
    </template>

    <!-- 右侧：聊天面板 -->
    <template #main>
      <div v-if="activeConversation" class="chat-view__panel">
        <ChatPanel
          :key="activeConversation.id"
          :room-code="activeConversation.roomCode"
          :room-type="activeConversation.roomType"
          :messages="currentMessages"
          :loading="loadingMessages"
          @send="handleSendMessage"
          @load-more="handleLoadMore"
        />
      </div>
      <div v-else class="chat-view__empty">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <p>选择一个会话开始聊天</p>
        <FcButton type="primary" size="lg" @click="showCreateModal = true">
          发起聊天
        </FcButton>
      </div>
    </template>
  </ChatLayout>

  <!-- 新建会话弹窗 -->
  <CreateConversationModal
    v-model="showCreateModal"
    @create="handleCreateConversation"
  />

  <!-- 个人资料抽屉 -->
  <ProfileDrawer v-model="showProfileDrawer" />

  <!-- 添加好友弹窗 -->
  <AddFriendModal
    v-model="showAddFriendModal"
    @added="handleFriendAdded"
  />

  <!-- 创建群组弹窗 -->
  <CreateGroupModal
    v-model="showCreateGroupModal"
    @created="handleGroupCreated"
  />

  <!-- 订阅频道弹窗 -->
  <SubscribeChannelModal
    v-model="showSubscribeChannelModal"
    @subscribed="handleChannelSubscribed"
  />
</template>

<script setup>
import {ref, computed, onMounted, onUnmounted, nextTick, reactive} from 'vue'
import { listConversations, markAsRead, removeConversation } from '@/api/conversation.js'
import { getHistoryMessages } from '@/api/message.js'
import { useUserStore } from '@/stores/user.js'
import { useWebSocket } from '@/composables/useWebSocket.js'
import { Message, FcButton } from '@/components/ui/index.js'
import ChatLayout from './ChatLayout.vue'
import ConversationList from '@/components/chat/ConversationList/ConversationList.vue'
import ChatPanel from '@/components/chat/ChatPanel/ChatPanel.vue'
import CreateConversationModal from '@/components/chat/CreateConversationModal/CreateConversationModal.vue'
import ProfileDrawer from '@/components/chat/ProfileDrawer/ProfileDrawer.vue'
import AddFriendModal from '@/components/chat/AddFriendModal/AddFriendModal.vue'
import CreateGroupModal from '@/components/chat/CreateGroupModal/CreateGroupModal.vue'
import SubscribeChannelModal from '@/components/chat/SubscribeChannelModal/SubscribeChannelModal.vue'

const userStore = useUserStore()
const { connect, disconnect, sendMessage, onMessage, onNotify, onSystem } = useWebSocket()

// 响应式状态
const layoutRef = ref(null)
const conversations = ref([])
const loadingConversations = ref(false)
const activeConversation = ref(null)
const messagesMap = ref({}) // 每个会话的消息映射：{ roomCode: [messages] }
const loadingMessages = ref(false) // 消息加载中
const hasMoreMessages = ref({}) // 每个会话是否有更多消息：{ roomCode: boolean }
const currentPage = ref({}) // 每个会话的当前页码：{ roomCode: page }
const showCreateModal = ref(false) // 显示新建会话弹窗
const showProfileDrawer = ref(false) // 显示个人资料抽屉
const showAddFriendModal = ref(false) // 显示添加好友弹窗
const showCreateGroupModal = ref(false) // 显示创建群组弹窗
const showSubscribeChannelModal = ref(false) // 显示订阅频道弹窗

// 当前选中会话的消息列表
const currentMessages = computed(() => {
  if (!activeConversation.value) return []
  return messagesMap.value[activeConversation.value.roomCode] || []
})

// 加载会话列表
async function loadConversations() {
  loadingConversations.value = true
  try {
    const res = await listConversations()
    conversations.value = res.data || []
  } catch (error) {
    console.error('[ChatView] 加载会话列表失败:', error)
  } finally {
    loadingConversations.value = false
  }
}

// 加载历史消息
async function loadMessages(roomCode, page = 1) {
  // 如果已加载过且不是加载更多，直接返回
  if (page === 1 && messagesMap.value[roomCode]) return

  // 如果没有更多消息，不加载
  if (page > 1 && hasMoreMessages.value[roomCode] === false) return

  loadingMessages.value = true
  try {
    const res = await getHistoryMessages(roomCode, { page, size: 50 })
    const newMessages = res.data?.messages || []
    
    // 记录是否有更多消息
    hasMoreMessages.value[roomCode] = newMessages.length === 50
    currentPage.value[roomCode] = page
    
    if (page === 1) {
      // 首次加载
      messagesMap.value[roomCode] = newMessages
    } else {
      // 加载更多，prepend 到列表顶部
      messagesMap.value[roomCode] = [...newMessages, ...messagesMap.value[roomCode]]
    }
    
    // 滚动到底部（首次加载）
    if (page === 1) {
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('[ChatView] 加载历史消息失败:', error)
    if (!messagesMap.value[roomCode]) {
      messagesMap.value[roomCode] = []
    }
  } finally {
    loadingMessages.value = false
  }
}

// 滚动到底部
function scrollToBottom() {
  // ChatPanel 暴露了 scrollToBottom 方法
  // 这里可以通过 ref 调用
}

// 加载更多消息
async function handleLoadMore() {
  if (!activeConversation.value || loadingMessages.value) return
  
  const roomCode = activeConversation.value.roomCode
  const nextPage = (currentPage.value[roomCode] || 1) + 1
  
  await loadMessages(roomCode, nextPage)
}

// 选中会话
async function handleSelectConversation(conv) {
  activeConversation.value = conv
  
  // 加载该会话的历史消息
  await loadMessages(conv.roomCode)
  
  // 标记为已读
  await markConversationAsRead(conv.roomCode)
}

// 标记会话为已读
async function markConversationAsRead(roomCode) {
  try {
    await markAsRead(roomCode)
    
    // 更新本地状态
    const convIndex = conversations.value.findIndex(c => c.roomCode === roomCode)
    if (convIndex !== -1) {
      conversations.value[convIndex].unreadCount = 0
    }
  } catch (error) {
    console.error('[ChatView] 标记已读失败:', error)
  }
}

// 创建新会话
async function handleCreateConversation(data) {
  try {
    console.log('[ChatView] 创建会话:', data)
    // TODO: 调用后端 API 创建会话
    // const res = await createConversation(data)
    
    // 重新加载会话列表
    await loadConversations()
    
    Message.success('会话创建成功')
  } catch (error) {
    console.error('[ChatView] 创建会话失败:', error)
    Message.error('创建失败，请重试')
  }
}

// 删除会话
async function handleDeleteConversation(conv) {
  try {
    await removeConversation(conv.roomCode)
    
    // 从列表中移除
    const index = conversations.value.findIndex(c => c.id === conv.id)
    if (index !== -1) {
      conversations.value.splice(index, 1)
    }
    
    // 如果删除的是当前会话，清空当前会话
    if (activeConversation.value?.id === conv.id) {
      activeConversation.value = null
    }
    
    // 清除该会话的消息缓存和分页状态
    delete messagesMap.value[conv.roomCode]
    delete hasMoreMessages.value[conv.roomCode]
    delete currentPage.value[conv.roomCode]
    
    Message.success('会话已删除')
  } catch (error) {
    console.error('[ChatView] 删除会话失败:', error)
    Message.error('删除失败，请重试')
  }
}

// 好友添加成功
function handleFriendAdded(user) {
  console.log('[ChatView] 好友已添加:', user)
  // 重新加载会话列表
  loadConversations()
}

// 群组创建成功
function handleGroupCreated(data) {
  console.log('[ChatView] 群组已创建:', data)
  // 重新加载会话列表
  loadConversations()
}

// 频道订阅成功
function handleChannelSubscribed(channel) {
  console.log('[ChatView] 频道已订阅:', channel)
  // 重新加载会话列表
  loadConversations()
}

// 发送消息
async function handleSendMessage(messageData) {
  try {
    await sendMessage(messageData)
    
    // 乐观更新：添加到本地消息列表
    const roomCode = messageData.roomCode
    if (!messagesMap.value[roomCode]) {
      messagesMap.value[roomCode] = []
    }
    
    messagesMap.value[roomCode].push({
      ...messageData,
      senderCode: userStore.userInfo?.code,
      senderName: userStore.userInfo?.nickname || userStore.userInfo?.username,
      senderAvatar: userStore.userInfo?.avatarUrl,
      timestamp: Date.now()
    })
  } catch (error) {
    console.error('[ChatView] 发送消息失败:', error)
  }
}

// WebSocket 消息监听
function setupWebSocketListeners() {
  // 收到聊天消息
  onMessage((msg) => {
    const roomCode = msg.roomCode
    if (!messagesMap.value[roomCode]) {
      messagesMap.value[roomCode] = []
    }
    messagesMap.value[roomCode].push(msg)
    
    // 如果当前正在查看该会话，自动标记已读
    if (activeConversation.value?.roomCode === roomCode) {
      markConversationAsRead(roomCode)
    }
    
    // 更新会话列表中的最后消息和未读数
    const convIndex = conversations.value.findIndex(c => c.roomCode === roomCode)
    if (convIndex !== -1) {
      conversations.value[convIndex] = {
        ...conversations.value[convIndex],
        lastMessage: msg.content,
        lastMessageType: msg.msgType,
        lastMessageTime: msg.timestamp,
        unreadCount: (conversations.value[convIndex].unreadCount || 0) + 1
      }
    }
  })
  
  // 收到系统通知
  onNotify((notify) => {
    console.log('[ChatView] 收到通知:', notify)
    // TODO: 处理通知（如好友申请、群邀请等）
  })
  
  // 收到系统消息
  onSystem((msg) => {
    console.log('[ChatView] 收到系统消息:', msg)
    // TODO: 显示系统消息（如被踢出、服务器维护等）
  })
}

// 生命周期
onMounted(async () => {
  try {
    // 临时设置 WebSocket 选项
    const options = reactive({
      wsUrl: 'ws://localhost:8081/ws-api',
    })
    // 连接 WebSocket（全局连接）
    await connect(options)
    
    // 加载会话列表
    await loadConversations()
    
    // 设置 WebSocket 监听器
    setupWebSocketListeners()
  } catch (error) {
    console.error('[ChatView] 初始化失败:', error)
  }
})

onUnmounted(() => {
  // 不断开 WebSocket 连接（全局共享）
  // 只在用户登出时断开
})
</script>

<style scoped>
.chat-view__panel {
  flex: 1;
  height: 100%;
  min-height: 0;
}

.chat-view__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--fc-text-secondary);
}

.chat-view__empty svg {
  opacity: 0.3;
}

.chat-view__empty p {
  margin: 0;
  font-size: var(--fc-text-base);
}
</style>
