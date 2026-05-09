<template>
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
            @click="$emit('selectFriend', item)"
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
            @click="$emit('selectGroup', item)"
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
            @click="$emit('selectChannel', item)"
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
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  friends: { type: Array, default: () => [] },
  groups: { type: Array, default: () => [] },
  channels: { type: Array, default: () => [] },
  currentSession: { type: Object, default: null },
  myCode: { type: String, default: '' }
})

defineEmits(['selectFriend', 'selectGroup', 'selectChannel'])

const sessionTab = ref('friend')
const sessionSearch = ref('')

function buildPrivateRoomCode(targetCode) {
  const sorted = [props.myCode, targetCode].sort()
  return `private:${sorted[0]}:${sorted[1]}`
}

const filteredFriends = computed(() => {
  if (!sessionSearch.value) return props.friends
  const k = sessionSearch.value.toLowerCase()
  return props.friends.filter(f => (f.nickname || f.username).toLowerCase().includes(k))
})

const filteredGroups = computed(() => {
  if (!sessionSearch.value) return props.groups
  return props.groups.filter(g => g.name.toLowerCase().includes(sessionSearch.value.toLowerCase()))
})

const filteredChannels = computed(() => {
  if (!sessionSearch.value) return props.channels
  return props.channels.filter(c => c.name.toLowerCase().includes(sessionSearch.value.toLowerCase()))
})
</script>

<style scoped>
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
</style>
