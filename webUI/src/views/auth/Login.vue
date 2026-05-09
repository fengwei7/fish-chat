<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>用户登录</h2>
        <el-divider />
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        size="large"
        @keyup.enter="submitForm"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            autocomplete="off"
            placeholder="请输入用户名"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            autocomplete="off"
            placeholder="请输入密码"
            show-password
            type="password"
          />
        </el-form-item>

        <!-- 记住我 -->
        <!--        <el-form-item class="remember-me">-->
        <!--          <el-checkbox v-model="rememberMe">记住我</el-checkbox>-->
        <!--        </el-form-item>-->

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
            :loading="loading"
            class="login-btn"
            round
            type="primary"
            @click="submitForm"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 注册链接 -->
      <div class="switch-mode">
        <span>没有账号？</span>
        <el-link underline="never" type="primary" @click="goToRegister">
          立即注册
        </el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { login as loginApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

// 路由
const router = useRouter()

// 用户状态管理
const userStore = useUserStore()

// 表单引用
const formRef = ref()

// 加载状态
const loading = ref(false)

// 记住我
// const rememberMe = ref(false)

// 表单数据
const form = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ]
})

// 提交表单
const submitForm = async () => {
  if (!formRef.value) {
    return
  }

  try {
    await formRef.value.validate()

    loading.value = true

    // 登录
    const authData = await loginApi({
      username: form.username,
      password: form.password
    })

    if (authData) {
      ElMessage.success('登录成功')

      // 存储用户信息和token（AuthDTO: token, code, username, nickname, avatarUrl）
      userStore.setUserInfo(authData)

      // 跳转到首页或其他页面
      const redirect = router.currentRoute.value.query.redirect || '/'
      router.push(redirect)
    } else {
      ElMessage.error('登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.login-box {
  width: 420px;
  padding: 30px;
  background: white;
  border-radius: 10px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  font-weight: 500;
  margin: 0;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
}

.remember-me {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.switch-mode {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.third-party-login {
  margin-top: 30px;
}

.oauth-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
}

@media (max-width: 768px) {
  .login-container {
    padding: 20px;
  }

  .login-box {
    width: 100%;
    max-width: 420px;
  }
}
</style>