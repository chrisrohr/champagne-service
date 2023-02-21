import { setActivePinia, createPinia } from 'pinia'
import { useAuditStore } from 'src/stores/auditStore'
import { vi } from "vitest";
import { doPagedRequest } from "src/utils/data";

vi.mock('src/utils/data', () => {
  const doPagedRequest = vi.fn()

  return { doPagedRequest }
})

describe('Audit Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('load', () => {
    const auditStore = useAuditStore()

    auditStore.load()

    expect(doPagedRequest).toHaveBeenCalled()
  })

})
