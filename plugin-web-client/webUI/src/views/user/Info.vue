<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProfile, updateProfile } from '@/api/user.js'
import { uploadFile } from '@/api/file.js'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const isEditing = ref(false)

const profile = reactive({
  code: '',
  username: '',
  nickname: '',
  avatarUrl: '',
  email: '',
  mobile: '',
  profile: '',
  online: false
})

const form = reactive({
  nickname: '',
  avatarUrl: '',
  email: '',
  mobile: '',
  profile: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 30, message: '昵称长度 1-30 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const formRef = ref(null)
const avatarUploading = ref(false)

async function loadProfile() {
  loading.value = true
  try {
    const data = await getProfile()
    Object.assign(profile, data)
    resetForm()
  } catch (e) {
    // ignore, 拦截器已处理
  } finally {
    loading.value = false
  }
}

function resolveUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return window.location.origin + url
  return url
}

function resetForm() {
  form.nickname = profile.nickname || ''
  form.avatarUrl = profile.avatarUrl || ''
  form.email = profile.email || ''
  form.mobile = profile.mobile || ''
  form.profile = profile.profile || ''
}

function startEdit() {
  resetForm()
  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
  resetForm()
}

async function saveProfile() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = await updateProfile(form)
    ElMessage.success('更新成功')
    isEditing.value = false
    Object.assign(profile, data)
    // 同步更新全局 store
    userStore.updateUserInfo({
      name: data.nickname || data.username || '',
      avatar: data.avatarUrl || '',
      email: data.email || ''
    })
  } catch (e) {
    // ignore
  } finally {
    saving.value = false
  }
}

async function handleAvatarChange(file) {
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return
  }
  const isLt2M = file.raw.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  avatarUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    const res = await uploadFile(formData)
    const url = res?.accessUrl || res?.url || res?.data || (typeof res === 'string' ? res : '')
    if (url) {
      form.avatarUrl = url
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error('头像上传失败')
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <div class="profile-page" v-loading="loading">
    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
            </div>
          </template>
          <div class="avatar-section">
            <el-avatar :size="100" :src="resolveUrl(profile.avatarUrl)" />
            <div class="user-name">{{ profile.nickname || profile.username || '-' }}</div>
            <div class="user-code">@{{ profile.code || '-' }}</div>
            <el-tag :type="profile.online ? 'success' : 'info'" size="small">
              {{ profile.online ? '在线' : '离线' }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16">
        <el-card>
            <template #header>
              <div class="card-header">
                <span>详细资料</span>
                <el-button v-if="!isEditing" type="primary" size="small" @click="startEdit">
                  编辑资料
                </el-button>
                <div v-else class="edit-actions">
                  <el-button size="small" @click="cancelEdit">取消</el-button>
                  <el-button type="primary" size="small" :loading="saving" @click="saveProfile">
                    保存
                  </el-button>
                </div>
              </div>
            </template>

            <el-form
              v-if="isEditing"
              ref="formRef"
              :model="form"
              :rules="rules"
              label-width="80px"
            >
              <el-form-item label="头像">
                <el-upload
                  class="avatar-uploader"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleAvatarChange"
                  accept="image/*"
                >
                  <el-avatar
                    v-if="form.avatarUrl"
                    :size="64"
                    :src="resolveUrl(form.avatarUrl)"
                    class="clickable-avatar"
                  />
                  <el-icon v-else class="avatar-uploader-icon" :size="28">
                    <Plus />
                  </el-icon>
                </el-upload>
                <span v-if="avatarUploading" class="upload-tip">上传中...</span>
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="profile.username" disabled />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="30" show-word-limit />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="form.mobile" placeholder="请输入手机号" maxlength="20" />
              </el-form-item>
              <el-form-item label="简介">
                <el-input
                  v-model="form.profile"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入个人简介"
                  maxlength="255"
                  show-word-limit
                />
              </el-form-item>
            </el-form>

            <div v-else class="info-list">
              <div class="info-item">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ profile.username || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">昵称</span>
                <span class="info-value">{{ profile.nickname || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ profile.email || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">手机号</span>
                <span class="info-value">{{ profile.mobile || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">个人简介</span>
                <span class="info-value">{{ profile.profile || '暂无简介' }}</span>
              </div>
            </div>
          </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 8px;
}

@media (max-width: 991px) {
  .profile-page .el-col + .el-col {
    margin-top: 20px;
  }
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.edit-actions {
  display: flex;
  gap: 8px;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
}
.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.user-code {
  font-size: 13px;
  color: #909399;
}
.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.info-item {
  display: flex;
  align-items: baseline;
}
.info-label {
  width: 80px;
  color: #606266;
  font-size: 14px;
  flex-shrink: 0;
}
.info-value {
  flex: 1;
  color: #303133;
  font-size: 14px;
  word-break: break-all;
}
.avatar-uploader {
  display: inline-block;
  cursor: pointer;
}
.clickable-avatar {
  transition: opacity 0.3s;
}
.clickable-avatar:hover {
  opacity: 0.8;
}
.avatar-uploader-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dcdfe6;
  border-radius: 50%;
  color: #8c939d;
}
.avatar-uploader-icon:hover {
  border-color: #409eff;
  color: #409eff;
}
.upload-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}
</style>