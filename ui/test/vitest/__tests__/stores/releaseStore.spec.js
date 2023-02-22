import { setActivePinia, createPinia } from 'pinia'
import { useReleaseStore } from 'src/stores/releaseStore'
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
      get: vi.fn().mockImplementation(() => Promise.resolve({data: { name: 'dev' }})),
      post: vi.fn().mockImplementation(() => Promise.resolve(true)),
      delete: vi.fn().mockImplementation(() => Promise.resolve(true)),
      put: vi.fn().mockImplementation(() => Promise.resolve(true))
    }

    return { api }
  })
})

describe('load', () => {

  it('should load releases', async () => {
    const releaseStore = useReleaseStore()
    releaseStore.environmentFilter = { value: 1 }

    await releaseStore.load()

    expect(doPagedRequest).toHaveBeenCalled()
  })

})

describe('create', () => {
  it('should make call to create release', async () => {
    const releaseStore = useReleaseStore()

    await releaseStore.create({ releaseNumber: '2023.42' })

    expect(api.post).toHaveBeenCalled()
    expect(api.post).toHaveBeenCalledWith('/manual/deployment/tasks/releases', { releaseNumber: '2023.42' })
  })
})

describe('deleteRelease', () => {
  it('should make call to delete release', async () => {
    const releaseStore = useReleaseStore()

    await releaseStore.deleteRelease(1)

    expect(api.delete).toHaveBeenCalled()
    expect(api.delete).toHaveBeenCalledWith('/manual/deployment/tasks/releases/1')
  })
})

describe('updateStatus', () => {
  it('should make call to update release status', async () => {
    const releaseStore = useReleaseStore()

    await releaseStore.updateStatus(1, 'COMPLETE')

    expect(api.put).toHaveBeenCalled()
    expect(api.put).toHaveBeenCalledWith('/manual/deployment/tasks/releases/1/COMPLETE')
  })
})
