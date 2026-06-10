import axios from 'axios'
import { useUserStore } from '@/stores/user.js'
import router from '@/router'

// ============================================
// HTTP 请求实例
// ============================================

const TOKEN = 'token'

const http = axios.create({
  baseURL: '/api',
  timeout: 50000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

// ============================================
// 请求拦截器
// ============================================

http.interceptors.request.use(
  (config) => {
    // 添加 Sa-Token：从 localStorage 读取（userStore 持久化的主源）
    const token = localStorage.getItem(TOKEN) || ''
    if (token) {
      config.headers[TOKEN] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ============================================
// 响应拦截器 - 统一错误处理
// ============================================

// 动态导入 Message 组件（避免循环依赖）
let showMessage = null
async function initMessage() {
  if (!showMessage) {
    try {
      const { message } = await import('@/components/ui/Message/message.js')
      showMessage = message
    } catch (e) {
      console.warn('[HTTP] Message 组件加载失败，降级为 console.error')
      showMessage = null
    }
  }
  return showMessage
}

// 错误消息去重：防止同一个错误重复弹出
let lastErrorMessage = ''
let lastErrorTime = 0
const ERROR_DEBOUNCE_MS = 500 // 500ms 内相同消息不重复弹出

/**
 * 显示错误消息（带防抖去重）
 * @param {String} message - 错误消息
 */
function showError(message) {
  const now = Date.now()
  if (message === lastErrorMessage && now - lastErrorTime < ERROR_DEBOUNCE_MS) {
    return // 500ms 内相同消息不重复弹出
  }
  lastErrorMessage = message
  lastErrorTime = now

  // 尝试使用 Message 组件
  if (showMessage) {
    showMessage.error(message)
  } else {
    // 降级方案：console.error
    console.error('[HTTP Error]', message)
    // 异步加载 Message 组件
    initMessage()
  }
}

/**
 * 显示成功消息
 * @param {String} message - 成功消息
 */
function showSuccess(message) {
  if (showMessage) {
    showMessage.success(message)
  } else {
    console.log('[HTTP Success]', message)
    initMessage()
  }
}

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
        // 如果有 message 且不是空字符串，显示成功提示（可选）
        // 通常登录、注册等接口需要提示，查询接口不需要
        if (res.message && res.message !== '成功' && res.message !== 'success') {
          showSuccess(res.message)
        }
        return res.data
      }
      // 业务错误：统一拦截，业务代码不需要再 catch 处理
      showError(res.message || '操作失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }

    // 兼容未包装的直接返回
    return res
  },

  async (error) => {
    const { response } = error

    // 网络错误（无响应）
    if (!response) {
      showError('网络连接失败，请检查网络设置')
      return Promise.reject(error)
    }

    // 401 未授权：清除 token 并跳转登录
    if (response.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      const currentPath = router.currentRoute.value.fullPath
      router.push({
        path: '/login',
        query: { redirect: currentPath }
      })
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }

    // 其他 HTTP 错误
    const message = response.data?.message || `请求出错 (${response.status})`
    showError(message)
    return Promise.reject(error)
  }
)

export default http