<template>
  <div class="chat-page">
    <!-- 左侧会话列表 -->
    <div class="chat-sidebar">
      <div class="session-header">
        <el-input v-model="sessionSearch" placeholder="搜索会话" size="small" clearable />
      </div>
      <el-tabs v-model="sessionTab" stretch>
        <el-tab-pane label="好友" name="friend">
          <div class="session-list">
            <div
              v-for="item in filteredFriends"
              :key="item.code"
              class="session-item"
              :class="{ active: currentSession?.roomCode === buildPrivateRoomCode(item.code) }"
              @click="selectFriend(item)"
            >
              <el-avatar :size="36" :src="item.avatarUrl" />
              <div class="session-info">
                <div class="session-name">{{ item.nickname || item.username }}</div>
                <div class="session-meta">
                  <el-tag v-if="item.online" size="small" type="success">在线</el-tag>
                  <el-tag v-else size="small" type="info">离线</el-tag>
                </div>
              </div>
            </div>
            <el-empty v-if="friends.length === 0" description="暂无好友" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="群组" name="group">
          <div class="session-list">
            <div
              v-for="item in filteredGroups"
              :key="item.code"
              class="session-item"
              :class="{ active: currentSession?.roomCode === 'group:' + item.code }"
              @click="selectGroup(item)"
            >
              <el-avatar :size="36" :src="item.avatar">{{ item.name?.[0] }}</el-avatar>
              <div class="session-info">
                <div class="session-name">{{ item.name }}</div>
                <div class="session-meta">{{ item.memberCount || 0 }} 人</div>
              </div>
            </div>
            <el-empty v-if="groups.length === 0" description="暂无群组" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="频道" name="channel">
          <div class="session-list">
            <div
              v-for="item in filteredChannels"
              :key="item.code"
              class="session-item"
              :class="{ active: currentSession?.roomCode === 'channel:' + item.code }"
              @click="selectChannel(item)"
            >
              <el-avatar :size="36" :src="item.avatar">{{ item.name?.[0] }}</el-avatar>
              <div class="session-info">
                <div class="session-name">{{ item.name }}</div>
                <div class="session-meta">{{ item.subscriberCount || 0 }} 订阅</div>
              </div>
            </div>
            <el-empty v-if="channels.length === 0" description="暂无频道" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 右侧聊天区域 -->
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

        <div ref="msgListRef" class="message-list">
          <div
            v-for="msg in messages"
            :key="msg.msgId || msg.reqId"
            class="message-row"
            :class="{ self: msg.senderCode === myCode }"
          >
            <el-avatar :size="32" :src="msg.senderAvatar">{{ msg.senderName?.[0] }}</el-avatar>
            <div class="message-bubble">
              <div class="message-sender">{{ msg.senderName }}</div>
              <div v-if="msg.msgType === 'TEXT'" class="message-content">{{ msg.content }}</div>
              <div v-else-if="msg.msgType === 'IMAGE'" class="message-content">
                <el-image :src="msg.content" style="max-width:200px;max-height:200px;border-radius:4px" fit="cover" :preview-src-list="[msg.content]" />
              </div>
              <div v-else-if="msg.msgType === 'FILE'" class="message-content">
                <el-link :href="msg.content" target="_blank" type="primary">
                  <el-icon><Document /></el-icon> {{ msg.fileName || '文件' }}
                </el-link>
              </div>
              <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
            </div>
          </div>
          <el-empty v-if="messages.length === 0" description="暂无消息" />
        </div>

        <div class="chat-input">
          <div class="input-tools">
            <el-upload
              action=""
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              accept="image/*"
            >
              <el-button :icon="Picture" size="small" circle />
            </el-upload>
            <el-upload
              action=""
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileUpload"
            >
              <el-button :icon="FolderOpened" size="small" circle />
            </el-upload>
          </div>
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            placeholder="输入消息，按 Enter 发送，Shift+Enter 换行"
            @keydown.enter.prevent="onEnterSend"
          />
          <div class="input-actions">
            <el-button type="primary" @click="sendTextMessage">发送</el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, FolderOpened, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user.js'
import { listFriends } from '@/api/friend.js'
import { listMyGroups } from '@/api/group.js'
import { listMyChannels } from '@/api/channel.js'
import { getHistoryMessages } from '@/api/message.js'
import { uploadFile } from '@/api/file.js'
import { v4 as uuidv4 } from 'uuid'

const userStore = useUserStore()
const myCode = computed(() => userStore.userBaseInfo.code)
const myName = computed(() => userStore.userBaseInfo.name)

// 会话列表
const sessionTab = ref('friend')
const sessionSearch = ref('')
const friends = ref([])
const groups = ref([])
const channels = ref([])

const filteredFriends = computed(() => {
  if (!sessionSearch.value) return friends.value
  const k = sessionSearch.value.toLowerCase()
  return friends.value.filter(f => (f.nickname || f.username).toLowerCase().includes(k))
})
const filteredGroups = computed(() => {
  if (!sessionSearch.value) return groups.value
  return groups.value.filter(g => g.name.toLowerCase().includes(sessionSearch.value.toLowerCase()))
})
const filteredChannels = computed(() => {
  if (!sessionSearch.value) return channels.value
  return channels.value.filter(c => c.name.toLowerCase().includes(sessionSearch.value.toLowerCase()))
})

// WebSocket
const ws = ref(null)
const wsConnected = ref(false)
let heartbeatTimer = null
let reconnectTimer = null

const currentSession = ref(null)
const messages = ref([])
const msgListRef = ref(null)
const inputText = ref('')

function buildPrivateRoomCode(targetCode) {
  const c1 = myCode.value
  const c2 = targetCode
  const sorted = [c1, c2].sort()
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
    scrollToBottom()
  } catch (e) {
    console.error('加载历史消息失败', e)
  }
}

// WebSocket
function connectWS() {
  const token = userStore.getToken()
  if (!token) {
    ElMessage.error('未登录，无法连接')
    return
  }
  const url = `ws://localhost:8081/ws?token=${encodeURIComponent(token)}`
  ws.value = new WebSocket(url)

  ws.value.onopen = () => {
    wsConnected.value = true
    startHeartbeat()
  }

  ws.value.onmessage = (event) => {
    try {
      const pkt = JSON.parse(event.data)
      handlePacket(pkt)
    } catch (e) {
      console.warn('收到非JSON消息', event.data)
    }
  }

  ws.value.onclose = () => {
    wsConnected.value = false
    stopHeartbeat()
    ws.value = null
    // 3秒后自动重连
    reconnectTimer = setTimeout(() => {
      if (!wsConnected.value) connectWS()
    }, 3000)
  }

  ws.value.onerror = () => {
    wsConnected.value = false
  }
}

function disconnectWS() {
  stopHeartbeat()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (ws.value) {
    ws.value.close()
    ws.value = null
  }
}

function handlePacket(pkt) {
  switch (pkt.cmd) {
    case 'MSG': {
      const b = pkt.body || {}
      if (currentSession.value && b.roomCode === currentSession.value.roomCode) {
        messages.value.push({
          msgId: b.msgId,
          senderCode: b.senderCode,
          senderName: b.senderName,
          senderAvatar: b.senderAvatar,
          roomCode: b.roomCode,
          msgType: b.msgType || 'TEXT',
          content: b.content,
          fileName: b.fileName,
          timestamp: b.timestamp || Date.now()
        })
        scrollToBottom()
      }
      break
    }
    case 'NOTIFY':
      ElMessage.info(pkt.body?.content || '系统通知')
      break
    case 'ERROR':
      ElMessage.error(pkt.body?.content || '消息发送失败')
      break
    case 'ACK':
      // 静默处理确认
      break
    case 'HEARTBEAT':
      break
  }
}

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify({ cmd: 'HEARTBEAT' }))
    }
  }, 30000)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

function sendRaw(cmd, body) {
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
    ElMessage.error('WebSocket 未连接')
    return
  }
  const pkt = { cmd, reqId: uuidv4(), body }
  ws.value.send(JSON.stringify(pkt))
}

function sendTextMessage() {
  const content = inputText.value.trim()
  if (!content || !currentSession.value) return
  sendMessage(content, 'TEXT')
  inputText.value = ''
}

function onEnterSend(e) {
  if (!e.shiftKey) {
    sendTextMessage()
  }
}

function sendMessage(content, msgType, fileName, fileSize) {
  const s = currentSession.value
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
  sendRaw('MSG', body)
}

async function handleFileChange(uploadFile) {
  await uploadAndSend(uploadFile.raw, 'IMAGE')
}

async function handleFileUpload(uploadFile) {
  await uploadAndSend(uploadFile.raw, 'FILE')
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
    ElMessage.error('文件上传失败')
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

watch(() => messages.value.length, scrollToBottom)

onMounted(() => {
  loadSessions()
  connectWS()
})

onBeforeUnmount(() => {
  disconnectWS()
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

.chat-sidebar {
  width: 260px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.session-header {
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.session-list {
  padding: 8px;
  overflow-y: auto;
  flex: 1;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.session-item:hover,
.session-item.active {
  background: #ecf5ff;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
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

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.message-row.self {
  flex-direction: row-reverse;
}

.message-bubble {
  max-width: 60%;
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.message-row.self .message-bubble {
  background: #d9ecff;
}

.message-sender {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.message-content {
  font-size: 14px;
  color: #303133;
  word-break: break-all;
}

.message-time {
  font-size: 11px;
  color: #909399;
  text-align: right;
  margin-top: 4px;
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #e4e7ed;
  background: #fff;
}

.input-tools {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
