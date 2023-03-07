import { createPinia, setActivePinia } from 'pinia'
import { useSystemStore } from 'src/stores/systemStore'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { api } from 'src/boot/axios'

beforeEach(() => {
  setActivePinia(createPinia())

  vi.mock('src/boot/axios', () => {
    const api = {
      get: vi.fn().mockImplementation(() => Promise.resolve({ data: [{ name: 'kiwi' }] }))
    }

    return { api }
  })
})

describe('load', () => {
  it('should load systems for user', async () => {
    const systemStore = useSystemStore()

    await systemStore.load()

    expect(api.get).toHaveBeenCalled()
    expect(api.get).toHaveBeenCalledWith('/systems')
    expect(systemStore.systems).toEqual([{ name: 'kiwi' }])
  })
})
