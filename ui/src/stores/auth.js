import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'

export const useAuthStore = defineStore({
  id: 'auth',
  state: () => ({
    // initialize state from local storage to enable user to stay logged in
    user: JSON.parse(sessionStorage.getItem('champagne-user')),
    returnUrl: null
  }),
  getters: {
    isLoggedIn () {
      return this.user !== null
    }
  },
  actions: {
    async login (username, password) {
      const response = await api.post('/auth/login', { username })

      // update pinia state
      this.user = response.data

      // store user details and jwt in local storage to keep user logged in between page refreshes
      sessionStorage.setItem('champagne-user', JSON.stringify(this.user))

      // redirect to previous url or default to home page
      console.log('Redirect', this.returnUrl)
      this.router.push(this.returnUrl || '/')
    },
    logout () {
      this.user = null
      sessionStorage.removeItem('champagne-user')
      this.router.push('/login')
    }
  }
})
