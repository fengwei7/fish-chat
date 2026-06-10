<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h1 class="auth-title">Fish-Chat</h1>
        <p class="auth-subtitle">欢迎回来</p>
      </div>

      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="form-item">
          <label class="form-label" for="username">用户名</label>
          <FcInput
            id="username"
            v-model="formData.username"
            placeholder="请输入用户名"
            size="large"
            :maxlength="20"
          />
        </div>

        <div class="form-item">
          <label class="form-label" for="password">密码</label>
          <FcInput
            id="password"
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :maxlength="32"
            show-password
          />
        </div>

        <div class="form-actions">
          <FcButton
            type="primary"
            size="large"
            :loading="loading"
            :disabled="!canSubmit"
            @click="handleLogin"
          >
            登录
          </FcButton>
        </div>
      </form>

      <div class="auth-footer">
        <router-link to="/register" class="auth-link">
          还没有账号？去注册
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, computed, ref} from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { login as loginApi } from '@/api/auth.js'

const router = useRouter()
const userStore = useUserStore()

const formData = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const canSubmit = computed(() => {
  return formData.username.trim() && formData.password.length >= 6
})

async function handleLogin() {
  if (!canSubmit.value || loading.value) return

  loading.value = true
  try {
    const authData = await loginApi({
      username: formData.username.trim(),
      password: formData.password
    })

    // 保存 token 和用户信息
    userStore.login(authData)

    // 跳转到聊天页面
    router.push('/chat')
  } catch (error) {
    // 错误已在 http 拦截器统一处理
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: var(--fc-radius-lg);
  box-shadow: var(--fc-shadow-lg);
  padding: 40px;
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--fc-primary);
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.auth-subtitle {
  font-size: 14px;
  color: var(--fc-text-secondary);
  margin: 0;
}

.auth-form {
  margin-bottom: 24px;
}

.form-item {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--fc-text-primary);
  margin-bottom: 8px;
}

.form-actions {
  margin-top: 24px;
}

.form-actions .fc-button {
  width: 100%;
}

.auth-footer {
  text-align: center;
}

.auth-link {
  font-size: 14px;
  color: var(--fc-primary);
  text-decoration: none;
  transition: color var(--fc-transition-fast);
}

.auth-link:hover {
  color: var(--fc-primary-hover);
  text-decoration: underline;
}

@media (max-width: 480px) {
  .auth-card {
    padding: 32px 24px;
  }

  .auth-title {
    font-size: 28px;
  }
}
</style>
