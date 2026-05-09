import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const LS_TOKEN_KEY = 'fish-token'

function readLsToken() {
  return localStorage.getItem(LS_TOKEN_KEY) || ''
}

function writeLsToken(token) {
  if (token) localStorage.setItem(LS_TOKEN_KEY, token)
  else localStorage.removeItem(LS_TOKEN_KEY)
}

export const useUserStore = defineStore('user', () => {
    // 用户基本信息
    const userBaseInfo = ref({
      code: '',
      name: '',
      email: '',
      avatar: ''
    })

    // 用户角色
    const roles = ref(['user'])

    // token 存于 pinia（运行时状态），同时同步到 localStorage（持久化主源）
    const tokenValue = ref(readLsToken())

    // 获取 token：优先 localStorage，备选 pinia
    const getToken = () => {
      return readLsToken() || tokenValue.value || ''
    }

    // 登录状态：优先 localStorage，备选 pinia
    const isLoggedIn = computed(() => {
      return !!(readLsToken() || tokenValue.value)
    })

    // 设置用户信息
    const setUserInfo = (authData) => {
      if (authData.token) {
        tokenValue.value = authData.token
        writeLsToken(authData.token)
      }
      userBaseInfo.value = {
        code: authData.code || '',
        name: authData.nickname || authData.username || '',
        email: authData.email || '',
        avatar: authData.avatarUrl || ''
      }
      if (authData.role) {
        roles.value = [authData.role]
      }
    }

    // 更新用户信息
    const updateUserInfo = (data) => {
      userBaseInfo.value = { ...userBaseInfo.value, ...data }
    }

    // 检查是否有权限
    const hasPermission = (permission) => {
      return roles.value.some(role => role === permission || role === 'admin')
    }

    // 登出：同时清除 pinia 和 localStorage
    const clearUserInfo = () => {
      userBaseInfo.value = {
        code: '',
        name: '',
        email: '',
        avatar: ''
      }
      roles.value = []
      tokenValue.value = ''
      writeLsToken('')
    }

    return {
      userBaseInfo,
      roles,
      tokenValue,
      isLoggedIn,
      getToken,
      setUserInfo,
      updateUserInfo,
      hasPermission,
      clearUserInfo
    }
  },
  {
    persist: true
  })