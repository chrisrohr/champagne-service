import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { computed, ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(sessionStorage.getItem('champagne-user')))
  const returnUrl = ref(null)

  const isLoggedIn = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.admin)
  const loggedInUserAvatar = computed(() => {
    if (user.value === null) {
      return ''
    }
    return `${user.value.firstName.charAt(0)}${user.value.lastName.charAt(0)}`
  })

  async function login (username, password) {
    const response = await api.post('/auth/login', { username })

    user.value = response.data

    // store user details and jwt in local storage to keep user logged in between page refreshes
    sessionStorage.setItem('champagne-user', JSON.stringify(user.value))

    // redirect to previous url or default to home page
    console.log('Redirect', returnUrl.value)
    this.router.push(returnUrl.value || '/')
  }

  function logout () {
    user.value = null
    sessionStorage.removeItem('champagne-user')
    this.router.push('/login')
  }

  return { user, returnUrl, isLoggedIn, isAdmin, loggedInUserAvatar, login, logout }
})
