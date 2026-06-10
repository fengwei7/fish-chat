<template>
  <FcDrawer
    :model-value="modelValue"
    title="个人资料"
    width="450px"
    placement="right"
    :mask="false"
    @update:model-value="handleUpdateVisible"
    @close="handleClose"
  >
    <div class="profile-drawer">
      <!-- 头像 -->
      <div class="profile-drawer__avatar">
        <FcAvatar
          :src="userStore.userInfo?.avatarUrl"
          :size="80"
          :name="userStore.userInfo?.nickname || userStore.userInfo?.username"
        />
        <button class="profile-drawer__avatar-btn" @click="triggerAvatarUpload">
          点击修改
        </button>
        <input
          ref="avatarInputRef"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleAvatarChange"
        />
      </div>

      <!-- 表单 -->
      <div class="profile-drawer__form">
        <div class="profile-drawer__field">
          <label class="profile-drawer__label">昵称</label>
          <FcInput
            v-model="form.nickname"
            placeholder="请输入昵称"
            size="large"
          />
        </div>

        <div class="profile-drawer__field">
          <label class="profile-drawer__label">个性签名</label>
          <FcInput
            v-model="form.signature"
            placeholder="写点什么介绍自己吧..."
            size="large"
            type="textarea"
            :rows="3"
          />
        </div>

        <div class="profile-drawer__field">
          <label class="profile-drawer__label">用户名</label>
          <div class="profile-drawer__readonly">{{ userStore.userInfo?.username || '-' }}</div>
        </div>

        <div class="profile-drawer__field">
          <label class="profile-drawer__label">注册时间</label>
          <div class="profile-drawer__readonly">{{ formatDate(userStore.userInfo?.createdAt) }}</div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="profile-drawer__actions">
        <FcButton
          type="primary"
          size="large"
          block
          :loading="saving"
          @click="handleSave"
        >
          保存修改
        </FcButton>

        <FcButton
          size="large"
          block
          @click="showPasswordModal = true"
        >
          修改密码
        </FcButton>

        <FcButton
          type="danger"
          size="large"
          block
          @click="handleLogout"
        >
          退出登录
        </FcButton>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <FcModal
      :visible="showPasswordModal"
      title="修改密码"
      size="md"
      @update:visible="showPasswordModal = $event"
      @close="showPasswordModal = false"
    >
      <div class="password-form">
        <div class="password-form__field">
          <label class="password-form__label">旧密码</label>
          <FcInput
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            size="large"
          />
        </div>

        <div class="password-form__field">
          <label class="password-form__label">新密码</label>
          <FcInput
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            size="large"
          />
        </div>

        <div class="password-form__field">
          <label class="password-form__label">确认密码</label>
          <FcInput
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            size="large"
          />
        </div>
      </div>

      <template #footer>
        <FcButton @click="showPasswordModal = false">取消</FcButton>
        <FcButton
          type="primary"
          :loading="changingPassword"
          @click="handleChangePassword"
        >
          确定
        </FcButton>
      </template>
    </FcModal>
  </FcDrawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { FcDrawer, FcModal, FcInput, FcButton, FcAvatar } from '@/components/ui/index.js'
import { Message } from '@/components/ui/index.js'
import { useUserStore } from '@/stores/user.js'
import { uploadAvatar, changePassword, updateProfile } from '@/api/user.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const userStore = useUserStore()

// 处理 Drawer 可见性更新
function handleUpdateVisible(val) {
  emit('update:modelValue', val)
}

// 表单数据
const form = ref({
  nickname: '',
  signature: ''
})

// 头像上传
const avatarInputRef = ref(null)
const uploadingAvatar = ref(false)

// 修改密码
const showPasswordModal = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const changingPassword = ref(false)

// 保存
const saving = ref(false)

// 初始化表单
watch(() => props.modelValue, (val) => {
  if (val && userStore.userInfo) {
    form.value.nickname = userStore.userInfo.nickname || ''
    form.value.signature = userStore.userInfo.signature || ''
  }
})

// 触发头像上传
function triggerAvatarUpload() {
  avatarInputRef.value?.click()
}

// 处理头像上传
async function handleAvatarChange(event) {
  const file = event.target.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    Message.error('请选择图片文件')
    return
  }

  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    Message.error('图片大小不能超过5MB')
    return
  }

  uploadingAvatar.value = true
  try {
    const res = await uploadAvatar(file)
    // 更新store中的头像
    userStore.setUserInfo({
      ...userStore.userInfo,
      avatarUrl: res.data
    })
    Message.success('头像上传成功')
  } catch (error) {
    console.error('[ProfileDrawer] 上传头像失败:', error)
    Message.error('上传失败，请重试')
  } finally {
    uploadingAvatar.value = false
    // 清空input，允许重复选择同一文件
    event.target.value = ''
  }
}

// 保存修改
async function handleSave() {
  if (!form.value.nickname.trim()) {
    Message.error('昵称不能为空')
    return
  }

  saving.value = true
  try {
    await updateProfile({
      nickname: form.value.nickname,
      signature: form.value.signature
    })
    
    // 更新store
    userStore.setUserInfo({
      ...userStore.userInfo,
      nickname: form.value.nickname,
      signature: form.value.signature
    })
    
    Message.success('保存成功')
  } catch (error) {
    console.error('[ProfileDrawer] 保存失败:', error)
    Message.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 修改密码
async function handleChangePassword() {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value

  if (!oldPassword) {
    Message.error('请输入旧密码')
    return
  }

  if (!newPassword) {
    Message.error('请输入新密码')
    return
  }

  if (newPassword.length < 6) {
    Message.error('密码长度不能少于6位')
    return
  }

  if (newPassword !== confirmPassword) {
    Message.error('两次输入的密码不一致')
    return
  }

  changingPassword.value = true
  try {
    await changePassword({
      oldPassword,
      newPassword
    })
    
    Message.success('密码修改成功')
    showPasswordModal.value = false
    
    // 清空表单
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('[ProfileDrawer] 修改密码失败:', error)
    Message.error('修改失败，请检查旧密码是否正确')
  } finally {
    changingPassword.value = false
  }
}

// 退出登录
async function handleLogout() {
  try {
    await userStore.logout()
    Message.success('已退出登录')
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    console.error('[ProfileDrawer] 退出登录失败:', error)
    Message.error('退出失败，请重试')
  }
}

// 关闭抽屉
function handleClose() {
  visible.value = false
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>

<style scoped>
.profile-drawer {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 头像 */
.profile-drawer__avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.profile-drawer__avatar-btn {
  padding: 6px 16px;
  font-size: var(--fc-text-sm);
  color: var(--fc-primary);
  background: var(--fc-primary-light);
  border: 1px solid var(--fc-primary);
  border-radius: var(--fc-radius);
  cursor: pointer;
  transition: all 0.2s ease;
}

.profile-drawer__avatar-btn:hover {
  background: var(--fc-primary);
  color: white;
}

/* 表单 */
.profile-drawer__form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-drawer__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-drawer__label {
  font-size: var(--fc-text-sm);
  font-weight: 500;
  color: var(--fc-text-secondary);
}

.profile-drawer__readonly {
  padding: 12px;
  font-size: var(--fc-text-base);
  color: var(--fc-text-secondary);
  background: var(--fc-bg-secondary);
  border-radius: var(--fc-radius);
}

/* 操作按钮 */
.profile-drawer__actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid var(--fc-border);
}

/* 修改密码表单 */
.password-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.password-form__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.password-form__label {
  font-size: var(--fc-text-sm);
  font-weight: 500;
  color: var(--fc-text-secondary);
}
</style>
