<template>
  <div class="index-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>好友</template>
          <div class="stat-number">{{ stats.friends }}</div>
          <el-button text type="primary" @click="$router.push('/contacts/friends')">管理好友</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>群组</template>
          <div class="stat-number">{{ stats.groups }}</div>
          <el-button text type="primary" @click="$router.push('/contacts/groups')">管理群组</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>频道</template>
          <div class="stat-number">{{ stats.channels }}</div>
          <el-button text type="primary" @click="$router.push('/contacts/channels')">管理频道</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top:20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速入口</span>
            </div>
          </template>
          <el-space>
            <el-button type="primary" size="large" @click="$router.push('/chat')">
              进入聊天
            </el-button>
            <el-button size="large" @click="$router.push('/user/profile')">
              个人资料
            </el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listFriends } from '@/api/friend.js'
import { listMyGroups } from '@/api/group.js'
import { listMyChannels } from '@/api/channel.js'

const stats = ref({ friends: 0, groups: 0, channels: 0 })

async function loadStats() {
  try {
    const [f, g, c] = await Promise.all([
      listFriends(0, 1),
      listMyGroups(0, 1),
      listMyChannels(0, 1)
    ])
    stats.value.friends = f?.totalElements || f?.data?.length || 0
    stats.value.groups = g?.totalElements || g?.data?.length || 0
    stats.value.channels = c?.totalElements || c?.data?.length || 0
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadStats)
</script>

<style scoped>
.index-page {
  padding: 8px;
}
.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>