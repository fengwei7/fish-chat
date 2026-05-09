<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>用户注册</h2>
        <el-divider />
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        size="large"
        @keyup.enter="submitForm"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            autocomplete="off"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            autocomplete="off"
            placeholder="请输入邮箱地址"
            prefix-icon="Message"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            autocomplete="off"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            type="password"
          />
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            autocomplete="off"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
            type="password"
          />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
            :loading="loading"
            class="register-btn"
            round
            type="primary"
            @click="submitForm"
          >
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 登录链接 -->
      <div class="switch-mode">
        <span>已有账号？</span>
        <el-link underline="never" type="primary" @click="goToLogin">
          立即登录
        </el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { register as registerApi } from '@/api/auth'

// 路由
const router = useRouter()

// 表单引用
const formRef = ref()

// 加载状态
const loading = ref(false)

// 表单数据
const form = reactive({
  username: '',
  password: '',
  email: '',
  confirmPassword: ''
})

// 自定义验证规则 - 确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
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

    // 注册
    const response = await registerApi({
      username: form.username,
      password: form.password,
      email: form.email
    })

    // 假设API返回 { code: 200, data: { message } } 格式
    if (response) {
      ElMessage.success('注册成功，请登录')
      // 自动跳转到登录页
      router.push('/login')
    } else {
      ElMessage.error('注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.register-box {
  width: 420px;
  padding: 30px;
  background: white;
  border-radius: 10px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  font-weight: 500;
  margin: 0;
}

.register-form {
  margin-top: 20px;
}

.register-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
}

.switch-mode {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

@media (max-width: 768px) {
  .register-container {
    padding: 20px;
  }

  .register-box {
    width: 100%;
    max-width: 420px;
  }
}
</style>