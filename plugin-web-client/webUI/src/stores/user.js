import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {// 用户基本信息
    const token = ref('');

    return {
      token
    }
  },
  {
    persist: true
  })