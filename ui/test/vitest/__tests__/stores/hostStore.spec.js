import { setActivePinia, createPinia } from 'pinia'
import { useHostStore } from 'src/stores/hostStore'
import { vi, beforeEach, describe, it, expect } from 'vitest'
import { api } from 'src/boot/axios'

beforeEach(() => {
  setActivePinia(createPinia())

  vi.mock('src/boot/axios', () => {
    const api = {
      get: vi.fn().mockImplementation(() => Promise.resolve({ data: { name: 'dev' } })),
      post: vi.fn().mockImplementation(() => Promise.resolve(true)),
      delete: vi.fn().mockImplementation(() => Promise.resolve(true)),
      put: vi.fn().mockImplementation(() => Promise.resolve(true))
    }

    return { api }
  })
})

describe('load', () => {
  it('should load hosts', async () => {
    const hostStore = useHostStore()
    hostStore.environmentFilter = { value: 1 }

    await hostStore.load()

    expect(api.get).toHaveBeenCalled()
    expect(api.get).toHaveBeenCalledWith('/host/1', { componentFilter: '' })
  })
})

describe('create', () => {
  it('should make call to create host', async () => {
    const hostStore = useHostStore()
    hostStore.environmentFilter = { value: 1 }

    await hostStore.create({ hostName: 'localhost' })

    expect(api.post).toHaveBeenCalled()
    expect(api.post).toHaveBeenCalledWith('/host', { hostName: 'localhost', environmentId: 1 })
  })
})

describe('deleteHost', () => {
  it('should make call to delete host', async () => {
    const hostStore = useHostStore()

    await hostStore.deleteHost(1)

    expect(api.delete).toHaveBeenCalled()
    expect(api.delete).toHaveBeenCalledWith('/host/1')
  })
})
