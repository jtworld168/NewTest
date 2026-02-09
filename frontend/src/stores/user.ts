import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '../types'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null)

  function setUser(user: User) {
    currentUser.value = user
  }

  function clearUser() {
    currentUser.value = null
  }

  return { currentUser, setUser, clearUser }
})
