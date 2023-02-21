import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from 'src/stores/auth'
import { vi } from "vitest";
import { api } from "src/boot/axios";

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  it('login', async () => {
    vi.mock('src/boot/axios', () => {
      const api = {
        post: vi.fn().mockImplementation(() => Promise.resolve({data: { username: 'joe' }}))
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

  it('logout', () => {
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
