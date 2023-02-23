import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from 'src/stores/userStore'
import { vi } from "vitest";
import { api } from "src/boot/axios";
import { doPagedRequest } from "src/utils/data";

vi.mock('src/utils/data', () => {
  const doPagedRequest = vi.fn()

  return { doPagedRequest }
})

beforeEach(() => {
  setActivePinia(createPinia())

  vi.mock('src/boot/axios', () => {
    const api = {
      post: vi.fn().mockImplementation(() => Promise.resolve(true)),
      delete: vi.fn().mockImplementation(() => Promise.resolve(true)),
      put: vi.fn().mockImplementation(() => Promise.resolve(true))
    }

    return { api }
  })
})

describe('load', () => {

  it('should load users', async () => {
    const userStore = useUserStore()
    await userStore.load()

    expect(doPagedRequest).toHaveBeenCalled()
  })

})

describe('create', () => {
  it('should make call to create user', async () => {
    const userStore = useUserStore()
    await userStore.create({ firstName: 'joe' })

    expect(api.post).toHaveBeenCalled()
    expect(api.post).toHaveBeenCalledWith('/users', { firstName: 'joe' })
  })
})

describe('update', () => {
  it('should make call to update user', async () => {
    const userStore = useUserStore()
    await userStore.update({ firstName: 'joe' })

    expect(api.put).toHaveBeenCalled()
    expect(api.put).toHaveBeenCalledWith('/users', { firstName: 'joe' })
  })
})

describe('deleteUser', () => {
  it('should make call to delete user', async () => {
    const userStore = useUserStore()
    await userStore.deleteUser(1)

    expect(api.delete).toHaveBeenCalled()
    expect(api.delete).toHaveBeenCalledWith('/users/1')
  })
})
