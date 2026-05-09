<template>
  <div class="friend-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的好友</span>
              <el-input v-model="friendSearch" placeholder="搜索好友" size="small" style="width:160px" clearable />
            </div>
          </template>
          <el-table :data="filteredFriends" size="small">
            <el-table-column label="头像" width="60">
              <template #default="{ row }">
                <el-avatar :size="32" :src="row.avatarUrl">{{ row.nickname?.[0] || row.username?.[0] }}</el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.online" size="small" type="success">在线</el-tag>
                <el-tag v-else size="small" type="info">离线</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="handleRemove(row.code)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="friends.length === 0" description="暂无好友" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>好友申请</span>
              <el-badge :value="requests.length" v-if="requests.length > 0" />
            </div>
          </template>
          <el-table :data="requests" size="small">
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleAccept(row.code)">接受</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="requests.length === 0" description="暂无申请" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>添加好友 / 搜索用户</span>
          </template>
          <el-input v-model="searchKeyword" placeholder="输入用户名搜索" style="margin-bottom:12px">
            <template #append>
              <el-button @click="doSearch">搜索</el-button>
            </template>
          </el-input>
          <el-table :data="searchResults" size="small">
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleAdd(row.code)">添加</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listFriends, listFriendRequests, searchFriends, addFriend, acceptFriend, removeFriend } from '@/api/friend.js'

const friends = ref([])
const requests = ref([])
const friendSearch = ref('')
const searchKeyword = ref('')
const searchResults = ref([])

const filteredFriends = computed(() => {
  if (!friendSearch.value) return friends.value
  const k = friendSearch.value.toLowerCase()
  return friends.value.filter(f =>
    (f.nickname || '').toLowerCase().includes(k) ||
    (f.username || '').toLowerCase().includes(k)
  )
})

async function loadData() {
  try {
    const [fRes, rRes] = await Promise.all([
      listFriends(0, 100),
      listFriendRequests(0, 100)
    ])
    friends.value = fRes?.data || []
    requests.value = rRes?.data || []
  } catch (e) {
    console.error(e)
  }
}

async function doSearch() {
  if (!searchKeyword.value.trim()) return
  try {
    const res = await searchFriends(searchKeyword.value.trim(), 0, 20)
    searchResults.value = res?.data || []
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

async function handleAdd(code) {
  try {
    await addFriend(code)
    ElMessage.success('好友请求已发送')
    loadData()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

async function handleAccept(code) {
  try {
    await acceptFriend(code)
    ElMessage.success('已接受好友申请')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleRemove(code) {
  try {
    await ElMessageBox.confirm('确定删除该好友吗？', '提示', { type: 'warning' })
    await removeFriend(code)
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.friend-page {
  padding: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
