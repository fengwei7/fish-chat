<template>
  <FcModal
    :visible="modelValue"
    title="创建群组"
    size="md"
    @update:visible="handleUpdateVisible"
    @close="handleClose"
  >
    <div class="create-group">
      <div class="create-group__form">
        <!-- 群组名称 -->
        <div class="create-group__field">
          <label class="create-group__label">群组名称 <span class="create-group__required">*</span></label>
          <FcInput
            v-model="form.name"
            placeholder="请输入群组名称"
            size="large"
            maxlength="50"
            show-word-limit
          />
        </div>

        <!-- 群组头像 -->
        <div class="create-group__field">
          <label class="create-group__label">群组头像</label>
          <div class="create-group__avatar">
            <FcAvatar :src="form.avatar" :size="80" />
            <div class="create-group__avatar-upload" @click="triggerFileInput">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
              <span>更换头像</span>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleFileChange"
            />
          </div>
        </div>

        <!-- 群组描述 -->
        <div class="create-group__field">
          <label class="create-group__label">群组描述</label>
          <FcInput
            v-model="form.description"
            type="textarea"
            placeholder="请输入群组描述（可选）"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="create-group__actions">
        <FcButton size="large" block @click="handleClose">取消</FcButton>
        <FcButton
          type="primary"
          size="large"
          block
          :loading="submitting"
          :disabled="!form.name.trim()"
          @click="handleSubmit"
        >
          创建群组
        </FcButton>
      </div>
    </div>
  </FcModal>
</template>

<script setup>
import { ref } from 'vue'
import { FcModal, FcInput, FcButton, FcAvatar } from '@/components/ui/index.js'
import { createGroup } from '@/api/group.js'
import { uploadFile } from '@/api/file.js'
import { Message } from '@/components/ui/index.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'created'])

// 处理 Modal 可见性更新
function handleUpdateVisible(val) {
  emit('update:modelValue', val)
}

// 表单数据
const form = ref({
  name: '',
  avatar: '',
  description: ''
})

const fileInput = ref(null)
const submitting = ref(false)

// 触发文件选择
function triggerFileInput() {
  fileInput.value?.click()
}

// 处理文件选择
async function handleFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    Message.error('请选择图片文件')
    return
  }

  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    Message.error('图片大小不能超过 5MB')
    return
  }

  try {
    Message.info('上传中...')
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await uploadFile(formData)
    form.value.avatar = res.data?.url || res.data?.fileName
    
    Message.success('上传成功')
  } catch (error) {
    console.error('[CreateGroup] 上传头像失败:', error)
    Message.error('上传失败，请重试')
  }

  // 清空文件输入
  event.target.value = ''
}

// 提交
async function handleSubmit() {
  if (!form.value.name.trim()) {
    Message.warning('请输入群组名称')
    return
  }

  submitting.value = true
  try {
    const data = {
      name: form.value.name.trim(),
      avatar: form.value.avatar || undefined,
      description: form.value.description.trim() || undefined
    }

    await createGroup(data)
    
    Message.success('群组创建成功')
    emit('created', data)
    handleClose()
  } catch (error) {
    console.error('[CreateGroup] 创建群组失败:', error)
    Message.error('创建失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 关闭
function handleClose() {
  emit('update:modelValue', false)
  // 重置表单
  form.value = {
    name: '',
    avatar: '',
    description: ''
  }
}
</script>

<style scoped>
.create-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 表单 */
.create-group__form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.create-group__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.create-group__label {
  font-size: var(--fc-text-sm);
  color: var(--fc-text-primary);
  font-weight: 500;
}

.create-group__required {
  color: #ff4d4f;
}

/* 头像上传 */
.create-group__avatar {
  display: flex;
  align-items: center;
  gap: 16px;
}

.create-group__avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
  background: var(--fc-bg-secondary);
  border: 1px dashed var(--fc-border);
  border-radius: var(--fc-radius);
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--fc-text-secondary);
}

.create-group__avatar-upload:hover {
  background: var(--fc-bg-hover);
  border-color: var(--fc-primary);
  color: var(--fc-primary);
}

.create-group__avatar-upload svg {
  flex-shrink: 0;
}

.create-group__avatar-upload span {
  font-size: var(--fc-text-xs);
  white-space: nowrap;
}

/* 按钮 */
.create-group__actions {
  display: flex;
  gap: 12px;
}
</style>
