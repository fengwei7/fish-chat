<template>
  <div class="chat-page">
    <ChatSidebar
      :friends="friends"
      :groups="groups"
      :channels="channels"
      :current-session="currentSession"
      :my-code="myCode"
      @select-friend="selectFriend"
      @select-group="selectGroup"
      @select-channel="selectChannel"
    />

    <div class="chat-main">
      <div v-if="!currentSession" class="chat-empty">
        <el-empty description="选择一个会话开始聊天" />
      </div>
      <template v-else>
        <div class="chat-header">
          <span class="chat-title">{{ currentSession.name }}</span>
          <el-tag size="small" type="info">{{ currentSession.roomType }}</el-tag>
          <el-tag :type="wsConnected ? 'success' : 'danger'" size="small" style="margin-left:8px">
            {{ wsConnected ? '已连接' : '未连接' }}
          </el-tag>
        </div>

        <MessageList ref="messageListRef" :messages="messages" :my-code="myCode" />

        <ChatInput
          @send-text="sendTextMessage"
          @upload-image="handleImageUpload"
          @upload-file="handleFileUpload"
        />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { listFriends } from '@/api/friend.js'
import { listMyGroups } from '@/api/group.js'
import { listMyChannels } from '@/api/channel.js'
import { getHistoryMessages } from '@/api/message.js'
import { uploadFile } from '@/api/file.js'
import { useChatWebSocket } from './composables/useChatWebSocket.js'
import ChatSidebar from './components/ChatSidebar.vue'
import MessageList from './components/MessageList.vue'
import ChatInput from './components/ChatInput.vue'

const userStore = useUserStore()
const myCode = computed(() => userStore.userBaseInfo.code)
const myName = computed(() => userStore.userBaseInfo.name)

// 会话列表
const friends = ref([])
const groups = ref([])
const channels = ref([])

const currentSession = ref(null)
const messages = ref([])
const messageListRef = ref(null)

function buildPrivateRoomCode(targetCode) {
  const sorted = [myCode.value, targetCode].sort()
  return `private:${sorted[0]}:${sorted[1]}`
}

async function loadSessions() {
  try {
    const [fRes, gRes, cRes] = await Promise.all([
      listFriends(0, 100),
      listMyGroups(0, 100),
      listMyChannels(0, 100)
    ])
    friends.value = (fRes?.data || [])
    groups.value = (gRes?.data || [])
    channels.value = (cRes?.data || [])
  } catch (e) {
    console.error('加载会话列表失败', e)
  }
}

function selectFriend(item) {
  const roomCode = buildPrivateRoomCode(item.code)
  currentSession.value = {
    roomCode,
    roomType: 'PRIVATE',
    name: item.nickname || item.username,
    avatar: item.avatarUrl,
    targetCode: item.code
  }
  loadHistory(roomCode)
}

function selectGroup(item) {
  const roomCode = `group:${item.code}`
  currentSession.value = {
    roomCode,
    roomType: 'GROUP',
    name: item.name,
    avatar: item.avatar,
    targetCode: item.code
  }
  loadHistory(roomCode)
}

function selectChannel(item) {
  const roomCode = `channel:${item.code}`
  currentSession.value = {
    roomCode,
    roomType: 'CHANNEL',
    name: item.name,
    avatar: item.avatar,
    targetCode: item.code
  }
  loadHistory(roomCode)
}

async function loadHistory(roomCode) {
  messages.value = []
  try {
    const res = await getHistoryMessages(roomCode, 0, 50)
    const list = res?.messages || []
    messages.value = list.reverse().map(m => ({
      msgId: m.id,
      senderCode: m.from,
      senderName: m.from,
      senderAvatar: '',
      roomCode: m.roomCode,
      msgType: m.type || 'TEXT',
      content: m.content,
      fileName: m.fileName,
      timestamp: m.timestamp
    }))
    messageListRef.value?.scrollToBottom()
  } catch (e) {
    console.error('加载历史消息失败', e)
  }
}

// WS 消息处理
function onWsMessage(body) {
  if (currentSession.value && body.roomCode === currentSession.value.roomCode) {
    messages.value.push({
      msgId: body.msgId,
      senderCode: body.senderCode,
      senderName: body.senderName,
      senderAvatar: body.senderAvatar,
      roomCode: body.roomCode,
      msgType: body.msgType || 'TEXT',
      content: body.content,
      fileName: body.fileName,
      timestamp: body.timestamp || Date.now()
    })
    messageListRef.value?.scrollToBottom()
  }
}

const { wsConnected, connect, disconnect, send } = useChatWebSocket({
  onMessage: onWsMessage
})

function sendMessage(content, msgType, fileName, fileSize) {
  const s = currentSession.value
  if (!s) return
  const body = {
    roomCode: s.roomCode,
    roomType: s.roomType,
    msgType,
    content
  }
  if (s.roomType === 'PRIVATE') {
    body.extra = { targetId: s.targetCode }
  }
  if (fileName) body.fileName = fileName
  if (fileSize) body.fileSize = fileSize
  send(body)
}

function sendTextMessage(content) {
  if (!content || !currentSession.value) return
  sendMessage(content, 'TEXT')
}

async function uploadAndSend(file, msgType) {
  if (!currentSession.value) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await uploadFile(formData)
    const url = `http://localhost:8080${res.accessUrl}`
    sendMessage(url, msgType, file.name, file.size)
  } catch (e) {
    console.error('文件上传失败', e)
  }
}

function handleImageUpload(file) {
  uploadAndSend(file, 'IMAGE')
}

function handleFileUpload(file) {
  uploadAndSend(file, 'FILE')
}

onMounted(() => {
  loadSessions()
  connect(userStore.getToken())
})
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 80px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
