import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '../types'

export const useUserStore = defineStore('user', () => {
  const savedUser = localStorage.getItem('currentUser')
  const currentUser = ref<User | null>(savedUser ? JSON.parse(savedUser) : null)

  function setUser(user: User) {
    currentUser.value = user
    localStorage.setItem('currentUser', JSON.stringify(user))
  }

  function clearUser() {
    currentUser.value = null
    localStorage.removeItem('currentUser')
    localStorage.removeItem('satoken')
  }

  return { currentUser, setUser, clearUser }
})
