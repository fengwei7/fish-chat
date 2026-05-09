<template>
  <div class="channel-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的频道</span>
              <el-button size="small" type="primary" @click="createDialogVisible = true">创建频道</el-button>
            </div>
          </template>
          <el-table :data="channels" size="small">
            <el-table-column label="头像" width="60">
              <template #default="{ row }">
                <el-avatar :size="32" :src="row.avatar">{{ row.name?.[0] }}</el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="频道名称" />
            <el-table-column prop="subscriberCount" label="订阅数" width="80" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="handleUnsubscribe(row.code)">取消订阅</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="channels.length === 0" description="暂无频道" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>发现频道</span>
          </template>
          <el-input v-model="searchKeyword" placeholder="输入频道名称搜索" style="margin-bottom:12px">
            <template #append>
              <el-button @click="doSearch">搜索</el-button>
            </template>
          </el-input>
          <el-table :data="searchResults" size="small">
            <el-table-column prop="name" label="频道名称" />
            <el-table-column prop="subscriberCount" label="订阅数" width="80" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleSubscribe(row.code)">订阅</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建频道弹窗 -->
    <el-dialog v-model="createDialogVisible" title="创建频道" width="400px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="频道名称">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="createForm.avatar" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listMyChannels, searchChannels, createChannel, subscribeChannel, unsubscribeChannel } from '@/api/channel.js'

const channels = ref([])
const searchKeyword = ref('')
const searchResults = ref([])
const createDialogVisible = ref(false)
const createForm = ref({ name: '', avatar: '', description: '' })

async function loadData() {
  try {
    const res = await listMyChannels(0, 100)
    channels.value = res?.data || []
  } catch (e) {
    console.error(e)
  }
}

async function doSearch() {
  if (!searchKeyword.value.trim()) return
  try {
    const res = await searchChannels(searchKeyword.value.trim(), 0, 20)
    searchResults.value = res?.data || []
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

async function handleCreate() {
  if (!createForm.value.name.trim()) {
    ElMessage.warning('请输入频道名称')
    return
  }
  try {
    await createChannel(createForm.value)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    createForm.value = { name: '', avatar: '', description: '' }
    loadData()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

async function handleSubscribe(code) {
  try {
    await subscribeChannel(code)
    ElMessage.success('订阅成功')
    loadData()
  } catch (e) {
    ElMessage.error('订阅失败')
  }
}

async function handleUnsubscribe(code) {
  try {
    await unsubscribeChannel(code)
    ElMessage.success('已取消订阅')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.channel-page {
  padding: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
