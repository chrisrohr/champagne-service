import { setActivePinia, createPinia } from 'pinia'
import { useEnvStore } from 'src/stores/envStore'
import { vi } from "vitest";
import { api } from "src/boot/axios";

beforeEach(() => {
  setActivePinia(createPinia())

  vi.mock('src/boot/axios', () => {
    const api = {
      get: vi.fn().mockImplementation(() => Promise.resolve({data: { name: 'dev' }})),
      post: vi.fn().mockImplementation(() => Promise.resolve(true)),
      delete: vi.fn().mockImplementation(() => Promise.resolve(true)),
      put: vi.fn().mockImplementation(() => Promise.resolve(true))
    }

    return { api }
  })
})

describe('getEnvIdForName', () => {
  it('should return the environment name that matches the current id', () => {
    const envStore = useEnvStore();
    envStore.envs = [{id: 1, deleted: false, name: 'dev'}]

    const id = envStore.getEnvIdForName('dev')

    expect(id).toEqual(1)
  })
})

describe('load', () => {

  it('should load envs', async () => {
    const envStore = useEnvStore()

    await envStore.load()

    expect(api.get).toHaveBeenCalled()
    expect(api.get).toHaveBeenCalledWith('/environments')
    expect(envStore.envs).toEqual({ name: 'dev' })
  })

})

describe('create', () => {
  it('should make call to create env', async () => {
    const envStore = useEnvStore()

    await envStore.create({ name: 'dev' })

    expect(api.post).toHaveBeenCalled()
    expect(api.post).toHaveBeenCalledWith('/environments', { name: 'dev' })
  })
})

describe('deleteEnv', () => {
  it('should make call to delete env', async () => {
    const envStore = useEnvStore()

    await envStore.deleteEnv(1)

    expect(api.delete).toHaveBeenCalled()
    expect(api.delete).toHaveBeenCalledWith('/environments/1/delete')
  })
})

describe('deactivate', () => {
  it('should make call to deactivate env', async () => {
    const envStore = useEnvStore()

    await envStore.deactivate(1)

    expect(api.delete).toHaveBeenCalled()
    expect(api.delete).toHaveBeenCalledWith('/environments/1/deactivate')
  })
})

describe('activate', () => {
  it('should make call to activate env', async () => {
    const envStore = useEnvStore()

    await envStore.activate(1)

    expect(api.put).toHaveBeenCalled()
    expect(api.put).toHaveBeenCalledWith('/environments/1/activate')
  })
})
