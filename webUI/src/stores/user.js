import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {// 用户基本信息
    const token = ref('');

    const isLogin = computed(() => token.value !== '')
    return {
        token,
        isLogin
    }
  },
  {
    persist: true
  })