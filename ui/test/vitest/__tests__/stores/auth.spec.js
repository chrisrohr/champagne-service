import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from 'src/stores/auth'
import { describe, beforeEach, afterEach, it, expect, vi } from 'vitest'
import { api } from 'src/boot/axios'

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('login', () => {
    it('should login the user', async () => {
      vi.mock('src/boot/axios', () => {
        const api = {
          post: vi.fn().mockImplementation(() => Promise.resolve({ data: { username: 'joe' } }))
        }

        return { api }
      })

      const mockRouter = {
        push: vi.fn()
      }

      const authStore = useAuthStore()
      authStore.router = mockRouter

      await authStore.login('joe', 'password')

      expect(api.post).toHaveBeenCalled()
      expect(api.post).toHaveBeenCalledWith('/auth/login', { username: 'joe' })
      expect(authStore.user).toEqual({ username: 'joe' })
      expect(sessionStorage.getItem('champagne-user')).toEqual(JSON.stringify({ username: 'joe' }))
      expect(authStore.router.push).toHaveBeenCalled()
      expect(authStore.router.push).toHaveBeenCalledWith('/')
    })
  })

  describe('logout', () => {
    it('should logout the user', () => {
      const mockRouter = {
        push: vi.fn()
      }

      const authStore = useAuthStore()
      authStore.router = mockRouter

      authStore.logout()

      expect(authStore.user).toBeNull()
      expect(sessionStorage.getItem('champagne-user')).toBeNull()
      expect(authStore.router.push).toHaveBeenCalled()
      expect(authStore.router.push).toHaveBeenCalledWith('/login')
    })
  })

  describe('isLoggedIn', () => {
    it('should return true when logged in', () => {
      const authStore = useAuthStore()
      authStore.user = {}

      expect(authStore.isLoggedIn).toBeTruthy()
    })

    it('should return false when not logged in', () => {
      const authStore = useAuthStore()

      expect(authStore.isLoggedIn).toBeFalsy()
    })
  })

  describe('isAdmin', () => {
    it('should return true when logged in and is and admin', () => {
      const authStore = useAuthStore()
      authStore.user = { admin: true }

      expect(authStore.isAdmin).toBeTruthy()
    })

    it('should return false when not logged in', () => {
      const authStore = useAuthStore()

      expect(authStore.isAdmin).toBeFalsy()
    })

    it('should return false when logged in but not an admin', () => {
      const authStore = useAuthStore()
      authStore.user = { admin: false }

      expect(authStore.isAdmin).toBeFalsy()
    })
  })

  describe('loggedInUserAvatar', () => {
    it('should return initials when user is logged in', () => {
      const authStore = useAuthStore()
      authStore.user = { firstName: 'Joe', lastName: 'Schmoo' }

      expect(authStore.loggedInUserAvatar).toEqual('JS')
    })

    it('should return empty string when not logged in', () => {
      const authStore = useAuthStore()

      expect(authStore.loggedInUserAvatar).toEqual('')
    })
  })
})
