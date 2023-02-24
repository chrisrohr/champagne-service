import { setActivePinia, createPinia } from 'pinia'
import { useErrorStore } from 'src/stores/errorStore'
import { beforeEach, vi, describe, it, expect } from 'vitest'
import { api } from 'src/boot/axios'

beforeEach(() => {
  setActivePinia(createPinia())

  vi.mock('src/boot/axios', () => {
    const api = {
      get: vi.fn().mockImplementation(() => Promise.resolve({ data: { name: 'dev' } })),
      put: vi.fn().mockImplementation(() => Promise.resolve(true))
    }

    return { api }
  })
})

describe('load', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should use defaults', () => {
    const errorStore = useErrorStore()

    errorStore.load()

    expect(api.get).toHaveBeenCalled()
    expect(api.get).toHaveBeenCalledWith('/kiwi/application-errors', { params: { pageNumber: 1, pageSize: 25, status: 'UNRESOLVED' } })
  })

  it('should use provided props', () => {
    const errorStore = useErrorStore()

    errorStore.load({ pagination: { page: 5, rowsPerPage: 50 } })

    expect(api.get).toHaveBeenCalled()
    expect(api.get).toHaveBeenCalledWith('/kiwi/application-errors', { params: { pageNumber: 5, pageSize: 50, status: 'UNRESOLVED' } })
  })
})

describe('resolve', () => {
  it('should call resolve for given id', () => {
    const errorStore = useErrorStore()

    errorStore.resolve(1)

    expect(api.put).toHaveBeenCalledWith('/kiwi/application-errors/resolve/1')
  })
})

describe('resolveAll', () => {
  it('should call resolveAll', () => {
    const errorStore = useErrorStore()

    errorStore.resolveAll()

    expect(api.put).toHaveBeenCalledWith('/kiwi/application-errors/resolve')
  })
})
