<template>
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
          <el-image
            :src="msg.content"
            style="max-width:200px;max-height:200px;border-radius:4px"
            fit="cover"
            :preview-src-list="[msg.content]"
          />
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
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { Document } from '@element-plus/icons-vue'

const props = defineProps({
  messages: { type: Array, default: () => [] },
  myCode: { type: String, default: '' }
})

const msgListRef = ref(null)

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

watch(() => props.messages.length, scrollToBottom)

defineExpose({ scrollToBottom })
</script>

<style scoped>
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
</style>
