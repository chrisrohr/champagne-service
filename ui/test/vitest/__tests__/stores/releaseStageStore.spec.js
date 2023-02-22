import { setActivePinia, createPinia } from 'pinia'
import { useReleaseStageStore } from 'src/stores/releaseStageStore'
import { vi } from "vitest";
import { api } from "boot/axios";

describe('Audit Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())

    vi.mock('src/boot/axios', () => {
      const api = {
        get: vi.fn().mockImplementation(() => Promise.resolve({data: ['PRE', 'POST']})),
      }

      return { api }
    })
  })

  it('load', async () => {
    const releaseStageStore = useReleaseStageStore()

    await releaseStageStore.load()

    expect(api.get).toHaveBeenCalled()
    expect(api.get).toHaveBeenCalledWith('/manual/deployment/tasks/stages')
    expect(releaseStageStore.stages).toEqual(['PRE', 'POST'])

  })

})
