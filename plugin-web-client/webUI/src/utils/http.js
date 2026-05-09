import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user.js'

// ============================================
// HTTP 请求实例
// ============================================

const LS_TOKEN_KEY = 'fish-token'

const http = axios.create({
  baseURL: '/api',
  // baseURL: 'http://localhost:8080',
  timeout: 50000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

// ============================================
// 请求拦截器
// ============================================

http.interceptors.request.use(
  (config) => {
    // 添加 Sa-Token：从 localStorage 读取（userStore 持久化的主源）
    const token = localStorage.getItem(LS_TOKEN_KEY) || ''
    if (token) {
      config.headers['fish-token'] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ============================================
// 响应拦截器
// ============================================

http.interceptors.response.use(
  (response) => {
    // 二进制数据直接返回
    const { responseType } = response.config
    if (responseType === 'blob' || responseType === 'arraybuffer') {
      return response
    }

    const res = response.data
    // 统一结果包装 { code, message, data }
    if (res && typeof res.code === 'number') {
      if (res.code === 200) {
        return res.data
      }
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }

    // 兼容未包装的直接返回
    return res
  },

  async (error) => {
    const { response } = error
    if (!response) {
      ElMessage.error('网络连接失败')
      return Promise.reject(error)
    }
    if (response.status === 401) {
      // 401 时同时清除 localStorage 和 pinia
      try {
        const userStore = useUserStore()
        userStore.clearUserInfo()
      } catch {
        localStorage.removeItem(LS_TOKEN_KEY)
      }
      window.location.href = '/login'
      return Promise.reject(new Error('登录已过期'))
    }
    ElMessage.error(response.data?.message || '请求出错')
    return Promise.reject(error)
  }
)

export default http