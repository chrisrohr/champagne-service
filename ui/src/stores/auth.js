import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'

export const useAuthStore = defineStore({
  id: 'auth',
  state: () => ({
    // initialize state from local storage to enable user to stay logged in
    user: JSON.parse(localStorage.getItem('champagne-user')),
    returnUrl: null
  }),
  actions: {
    async login (username, password) {
      const user = await api.post('/auth/login', { username })

      // update pinia state
      this.user = user

      // store user details and jwt in local storage to keep user logged in between page refreshes
      localStorage.setItem('champagne-user', JSON.stringify(user))

      // redirect to previous url or default to home page
      this.$router.push(this.returnUrl || '/')
    },
    logout () {
      this.user = null
      localStorage.removeItem('champagne-user')
      this.$router.push('/login')
    }
  }
})
