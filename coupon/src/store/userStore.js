import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const username = ref(localStorage.getItem('username') || '')
  const isLoggedIn = ref(!!localStorage.getItem('token'))

  function updateLoginStatus() {
    username.value = localStorage.getItem('username') || ''
    isLoggedIn.value = !!localStorage.getItem('token')
  }

  return {
    username,
    isLoggedIn,
    updateLoginStatus
  }
})
