import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // Token
  const token = ref('')

  // 用户信息
  const userInfo = ref({
    code: '',
    username: '',
    nickname: '',
    avatarUrl: ''
  })

  // 是否已登录
  const isLogin = computed(() => token.value !== '')

  /**
   * 登录成功后的数据持久化
   * @param {Object} authData - 后端返回的 AuthDTO
   * @param {String} authData.token - 认证 token
   * @param {String} authData.code - 用户编码
   * @param {String} authData.username - 用户名
   * @param {String} authData.nickname - 昵称
   * @param {String} authData.avatarUrl - 头像 URL
   */
  function login(authData) {
    token.value = authData.token
    userInfo.value = {
      code: authData.code || '',
      username: authData.username || '',
      nickname: authData.nickname || '',
      avatarUrl: authData.avatarUrl || ''
    }
  }

  /**
   * 退出登录，清空所有状态
   */
  function logout() {
    token.value = ''
    userInfo.value = {
      code: '',
      username: '',
      nickname: '',
      avatarUrl: ''
    }
  }

  /**
   * 更新用户信息（如修改昵称/头像后）
   * @param {Object} data - 要更新的用户信息字段
   */
  function updateUserInfo(data) {
    userInfo.value = { ...userInfo.value, ...data }
  }

  return {
    token,
    userInfo,
    isLogin,
    login,
    logout,
    updateUserInfo
  }
}, {
  persist: true
})