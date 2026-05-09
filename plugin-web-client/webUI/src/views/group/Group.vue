<template>
  <div class="group-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的群组</span>
              <el-button size="small" type="primary" @click="createDialogVisible = true">创建群组</el-button>
            </div>
          </template>
          <el-table :data="groups" size="small">
            <el-table-column label="头像" width="60">
              <template #default="{ row }">
                <el-avatar :size="32" :src="row.avatar">{{ row.name?.[0] }}</el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="群组名称" />
            <el-table-column prop="memberCount" label="人数" width="80" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="openAddMember(row.code)">加人</el-button>
                <el-button size="small" type="danger" @click="handleDismiss(row.code)">解散</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="groups.length === 0" description="暂无群组" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>搜索群组</span>
          </template>
          <el-input v-model="searchKeyword" placeholder="输入群组名称搜索" style="margin-bottom:12px">
            <template #append>
              <el-button @click="doSearch">搜索</el-button>
            </template>
          </el-input>
          <el-table :data="searchResults" size="small">
            <el-table-column prop="name" label="群组名称" />
            <el-table-column prop="memberCount" label="人数" width="80" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleJoin(row.code)">加入</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建群组弹窗 -->
    <el-dialog v-model="createDialogVisible" title="创建群组" width="400px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="群组名称">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="createForm.avatar" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 添加成员弹窗 -->
    <el-dialog v-model="addMemberDialogVisible" title="添加成员" width="400px">
      <el-input v-model="addMemberCode" placeholder="输入用户code" />
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMyGroups, searchGroups, createGroup, dismissGroup, addGroupMember } from '@/api/group.js'

const groups = ref([])
const searchKeyword = ref('')
const searchResults = ref([])
const createDialogVisible = ref(false)
const createForm = ref({ name: '', avatar: '' })
const addMemberDialogVisible = ref(false)
const currentGroupCode = ref('')
const addMemberCode = ref('')

async function loadData() {
  try {
    const res = await listMyGroups(0, 100)
    groups.value = res?.data || []
  } catch (e) {
    console.error(e)
  }
}

async function doSearch() {
  if (!searchKeyword.value.trim()) return
  try {
    const res = await searchGroups(searchKeyword.value.trim(), 0, 20)
    searchResults.value = res?.data || []
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

async function handleCreate() {
  if (!createForm.value.name.trim()) {
    ElMessage.warning('请输入群组名称')
    return
  }
  try {
    await createGroup(createForm.value)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    createForm.value = { name: '', avatar: '' }
    loadData()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

async function handleDismiss(code) {
  try {
    await ElMessageBox.confirm('确定解散该群组吗？', '提示', { type: 'warning' })
    await dismissGroup(code)
    ElMessage.success('已解散')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('解散失败')
  }
}

function openAddMember(code) {
  currentGroupCode.value = code
  addMemberCode.value = ''
  addMemberDialogVisible.value = true
}

async function handleAddMember() {
  if (!addMemberCode.value.trim()) return
  try {
    await addGroupMember(currentGroupCode.value, addMemberCode.value.trim())
    ElMessage.success('添加成功')
    addMemberDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

function handleJoin(code) {
  openAddMember(code)
}

onMounted(loadData)
</script>

<style scoped>
.group-page {
  padding: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
