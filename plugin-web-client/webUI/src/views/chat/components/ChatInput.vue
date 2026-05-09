<template>
  <div class="chat-input">
    <div class="input-tools">
      <el-upload
        action=""
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleImageChange"
        accept="image/*"
      >
        <el-button :icon="Picture" size="small" circle />
      </el-upload>
      <el-upload
        action=""
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleFileChange"
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
      <el-button type="primary" @click="sendText">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Picture, FolderOpened } from '@element-plus/icons-vue'

const emit = defineEmits(['sendText', 'uploadImage', 'uploadFile'])

const inputText = ref('')

function sendText() {
  const content = inputText.value.trim()
  if (!content) return
  emit('sendText', content)
  inputText.value = ''
}

function onEnterSend(e) {
  if (!e.shiftKey) {
    sendText()
  }
}

function handleImageChange(uploadFile) {
  emit('uploadImage', uploadFile.raw)
}

function handleFileChange(uploadFile) {
  emit('uploadFile', uploadFile.raw)
}
</script>

<style scoped>
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
