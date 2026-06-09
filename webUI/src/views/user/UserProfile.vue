<template>
  <div class="user-profile-container">
    <!-- 主卡片 -->
    <FcCard class="profile-card" shadow>
      <!-- 头部背景 -->
      <div class="card-header">
        <div class="header-pattern"></div>
      </div>

      <!-- 用户信息区 -->
      <div class="user-info-section">
        <!-- 头像 -->
        <div class="avatar-wrapper">
          <FcAvatar
            :src="userProfile.avatarUrl || defaultAvatar"
            size="xl"
            :status="userProfile.online ? 'online' : 'offline'"
            :show-status="true"
          />
        </div>

        <!-- 基本信息 -->
        <div class="basic-info">
          <h1 class="nickname">{{ userProfile.nickname || userProfile.username }}</h1>
          <p class="username">@{{ userProfile.username }}</p>
          <p class="profile-text">{{ userProfile.profile || '这个人很懒,什么都没留下~' }}</p>
        </div>

        <!-- 编辑按钮 -->
        <FcButton type="primary" @click="showEditModal = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="18" height="18">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
          </svg>
          编辑资料
        </FcButton>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <div class="stat-item">
          <div class="stat-value">{{ stats.friends }}</div>
          <div class="stat-label">好友</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.groups }}</div>
          <div class="stat-label">群组</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.channels }}</div>
          <div class="stat-label">频道</div>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="details-section">
        <h3 class="section-title">详细信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <div class="info-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                <polyline points="22,6 12,13 2,6" />
              </svg>
            </div>
            <div class="info-content">
              <div class="info-label">邮箱</div>
              <div class="info-value">{{ userProfile.email || '未设置' }}</div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
              </svg>
            </div>
            <div class="info-content">
              <div class="info-label">手机号</div>
              <div class="info-value">{{ userProfile.mobile || '未设置' }}</div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <div class="info-content">
              <div class="info-label">用户ID</div>
              <div class="info-value copyable" @click="copyUserId">{{ userProfile.code }}</div>
            </div>
          </div>
        </div>
      </div>
    </FcCard>

    <!-- 编辑资料弹窗 -->
    <FcModal v-model:visible="showEditModal" title="编辑资料" size="md">
      <div class="edit-form">
        <!-- 头像上传 -->
        <div class="form-group">
          <label class="form-label">头像</label>
          <div class="avatar-upload">
            <FcAvatar :src="editForm.avatarUrl || defaultAvatar" size="lg" />
            <FcButton type="outline" size="sm" @click="triggerFileUpload">
              更换头像
            </FcButton>
            <input 
              ref="fileInput" 
              type="file" 
              accept="image/*" 
              @change="handleFileChange"
              style="display: none"
            />
          </div>
        </div>

        <!-- 昵称 -->
        <div class="form-group">
          <label class="form-label">昵称</label>
          <FcInput 
            v-model="editForm.nickname" 
            placeholder="请输入昵称"
            :maxlength="20"
          />
        </div>

        <!-- 个人简介 -->
        <div class="form-group">
          <label class="form-label">个人简介</label>
          <FcInput 
            v-model="editForm.profile" 
            type="textarea"
            placeholder="介绍一下自己吧~"
            :maxlength="200"
            :rows="4"
            :show-word-limit="true"
          />
        </div>
      </div>
      
      <template #footer>
        <FcButton @click="showEditModal = false">取消</FcButton>
        <FcButton type="primary" @click="handleSave" :loading="saving">
          {{ saving ? '保存中...' : '保存' }}
        </FcButton>
      </template>
    </FcModal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProfile, updateProfile } from '@/api/user.js'
import { uploadFile } from '@/api/file.js'
import { listFriends } from '@/api/friend.js'
import { listMyGroups } from '@/api/group.js'
import { listMyChannels } from '@/api/channel.js'
import { Message } from '@/components/ui'
import { FcButton, FcInput, FcModal, FcAvatar, FcCard } from '@/components/ui'

// 默认头像
const defaultAvatar = '/neko.png'

// 用户资料
const userProfile = ref({})
const stats = ref({
  friends: 0,
  groups: 0,
  channels: 0
})

// 编辑弹窗
const showEditModal = ref(false)
const saving = ref(false)
const fileInput = ref(null)

const editForm = ref({
  nickname: '',
  profile: '',
  avatarUrl: ''
})

// 加载用户资料
const loadProfile = async () => {
  try {
    const profile = await getProfile()
    userProfile.value = profile
    editForm.value = {
      nickname: profile.nickname || '',
      profile: profile.profile || '',
      avatarUrl: profile.avatarUrl || ''
    }
  } catch (error) {
    console.error('加载用户资料失败:', error)
    Message.error('加载用户资料失败')
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const [friendsRes, groupsRes, channelsRes] = await Promise.allSettled([
      listFriends({ pageNum: 0, pageSize: 1 }),
      listMyGroups({ pageNum: 0, pageSize: 1 }),
      listMyChannels({ pageNum: 0, pageSize: 1 })
    ])

    stats.value.friends = friendsRes.status === 'fulfilled' ? friendsRes.value.total : 0
    stats.value.groups = groupsRes.status === 'fulfilled' ? groupsRes.value.total : 0
    stats.value.channels = channelsRes.status === 'fulfilled' ? channelsRes.value.total : 0
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 触发文件上传
const triggerFileUpload = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    Message.error('请选择图片文件')
    return
  }

  // 验证文件大小 (5MB)
  if (file.size > 5 * 1024 * 1024) {
    Message.error('图片大小不能超过5MB')
    return
  }

  try {
    Message.info('上传中...')
    const formData = new FormData()
    formData.append('file', file)
    
    const result = await uploadFile(formData)
    editForm.value.avatarUrl = result.url
    Message.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
    Message.error('上传失败')
  }
}

// 复制用户ID
const copyUserId = async () => {
  try {
    await navigator.clipboard.writeText(userProfile.value.code)
    Message.success('已复制用户ID')
  } catch (error) {
    Message.error('复制失败')
  }
}

// 保存资料
const handleSave = async () => {
  if (!editForm.value.nickname.trim()) {
    Message.warning('昵称不能为空')
    return
  }

  try {
    saving.value = true
    await updateProfile({
      nickname: editForm.value.nickname,
      profile: editForm.value.profile,
      avatarUrl: editForm.value.avatarUrl
    })
    
    Message.success('保存成功')
    showEditModal.value = false
    await loadProfile()
  } catch (error) {
    console.error('保存失败:', error)
    Message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 初始化
onMounted(() => {
  loadProfile()
  loadStats()
})
</script>

<style scoped>
.user-profile-container {
  min-height: 100vh;
  padding: 40px 20px;
  background: linear-gradient(180deg, #f0f5ff 0%, #ffffff 100%);
}

/* 主卡片 */
.profile-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 20px;
  overflow: hidden;
}

/* 头部背景 */
.card-header {
  height: 180px;
  background: linear-gradient(135deg, var(--fc-primary-400) 0%, var(--fc-primary-600) 100%);
  position: relative;
  overflow: hidden;
}

.header-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(255, 255, 255, 0.15) 0%, transparent 50%);
}

/* 用户信息区 */
.user-info-section {
  position: relative;
  padding: 0 40px 40px;
  text-align: center;
}

.avatar-wrapper {
  margin-top: -60px;
  margin-bottom: 24px;
}

.basic-info {
  margin-bottom: 32px;
}

.nickname {
  font-size: 32px;
  font-weight: 700;
  color: var(--fc-gray-900);
  margin: 0 0 8px 0;
}

.username {
  font-size: 16px;
  color: var(--fc-gray-500);
  margin: 0 0 12px 0;
}

.profile-text {
  font-size: 15px;
  color: var(--fc-gray-600);
  line-height: 1.6;
  margin: 0;
}

/* 统计信息 */
.stats-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32px 40px;
  background: var(--fc-gray-50);
  border-top: 1px solid var(--fc-gray-200);
  border-bottom: 1px solid var(--fc-gray-200);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--fc-primary-600);
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--fc-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-divider {
  width: 1px;
  height: 50px;
  background: var(--fc-gray-200);
}

/* 详细信息 */
.details-section {
  padding: 40px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--fc-gray-900);
  margin: 0 0 24px 0;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--fc-gray-50);
  border-radius: 12px;
  transition: all 0.2s ease;
}

.info-item:hover {
  background: var(--fc-primary-50);
  transform: translateX(4px);
}

.info-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--fc-primary-500) 0%, var(--fc-primary-600) 100%);
  border-radius: 12px;
  color: white;
  flex-shrink: 0;
}

.info-icon svg {
  width: 24px;
  height: 24px;
}

.info-content {
  flex: 1;
}

.info-label {
  font-size: 13px;
  color: var(--fc-gray-500);
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 16px;
  color: var(--fc-gray-900);
  font-weight: 500;
}

.info-value.copyable {
  cursor: pointer;
  user-select: all;
}

.info-value.copyable:hover {
  color: var(--fc-primary-600);
}

/* 编辑表单 */
.edit-form {
  padding: 8px 0;
}

.form-group {
  margin-bottom: 24px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--fc-gray-700);
  margin-bottom: 8px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile-container {
    padding: 20px 12px;
  }

  .user-info-section {
    padding: 0 20px 30px;
  }

  .nickname {
    font-size: 26px;
  }

  .stats-section {
    padding: 24px 20px;
  }

  .stat-value {
    font-size: 28px;
  }

  .details-section {
    padding: 24px;
  }
}
</style>
