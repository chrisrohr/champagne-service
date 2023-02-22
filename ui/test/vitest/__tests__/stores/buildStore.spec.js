import { setActivePinia, createPinia } from 'pinia'
import { useBuildStore } from 'src/stores/buildStore'
import { vi } from "vitest";
import { doPagedRequest } from "src/utils/data";

vi.mock('src/utils/data', () => {
  const doPagedRequest = vi.fn()

  return { doPagedRequest }
})

describe('Build Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should call load without filters', () => {
    const buildStore = useBuildStore()

    buildStore.load()

    expect(doPagedRequest).toHaveBeenCalled()
  })

  it('should call load with filters', () => {
    const buildStore = useBuildStore()
    buildStore.filter = 'foo'
    buildStore.filterType = 'componentIdentifierFilter'

    buildStore.load()

    expect(doPagedRequest).toHaveBeenCalled()
  })

})
