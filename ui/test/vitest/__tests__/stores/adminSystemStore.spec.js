import { createPinia, setActivePinia } from 'pinia'
import { useAdminSystemStore } from 'src/stores/adminSystemStore'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { api } from 'src/boot/axios'
import { doPagedRequest } from 'src/utils/data'

vi.mock('src/utils/data', () => {
  const doPagedRequest = vi.fn()

  return { doPagedRequest }
})

beforeEach(() => {
  setActivePinia(createPinia())

  vi.mock('src/boot/axios', () => {
    const api = {
      post: vi.fn().mockImplementation(() => Promise.resolve(true)),
      delete: vi.fn().mockImplementation(() => Promise.resolve(true))
    }

    return { api }
  })
})

describe('load', () => {
  it('should load systems', async () => {
    const adminSystemStore = useAdminSystemStore()
    await adminSystemStore.load()

    expect(doPagedRequest).toHaveBeenCalled()
  })
})

describe('create', () => {
  it('should make call to create system', async () => {
    const adminSystemStore = useAdminSystemStore()
    await adminSystemStore.create({ name: 'kiwi' })

    expect(api.post).toHaveBeenCalled()
    expect(api.post).toHaveBeenCalledWith('/systems', { name: 'kiwi' })
  })
})

describe('deleteSystem', () => {
  it('should make call to delete system', async () => {
    const adminSystemStore = useAdminSystemStore()
    await adminSystemStore.deleteSystem(1)

    expect(api.delete).toHaveBeenCalled()
    expect(api.delete).toHaveBeenCalledWith('/systems/1')
  })
})
