import { setActivePinia, createPinia } from 'pinia'
import { useComponentStore } from 'src/stores/componentStore'
import { vi } from "vitest";
import { api } from "src/boot/axios";

beforeEach(() => {
  setActivePinia(createPinia())

  vi.mock('src/boot/axios', () => {
    const api = {
      get: vi.fn().mockImplementation(() => Promise.resolve({data: { name: 'foo-service' }})),
      post: vi.fn().mockImplementation(() => Promise.resolve(true)),
      delete: vi.fn().mockImplementation(() => Promise.resolve(true))
    }

    return { api }
  })
})

describe('load', () => {

  it('should load components for given host id', async () => {
    const componentStore = useComponentStore()

    await componentStore.load(1)

    expect(api.get).toHaveBeenCalled()
    expect(api.get).toHaveBeenCalledWith('/host/1/components')
    expect(componentStore.componentsByHost(1)).toEqual({ name: 'foo-service' })
    expect(componentStore.loadingForComponents(1)).toBeFalsy()
  })

})

describe('create', () => {
  it('should make call to create component', async () => {
    const componentStore = useComponentStore()

    await componentStore.create({ name: 'foo-service' })

    expect(api.post).toHaveBeenCalled()
    expect(api.post).toHaveBeenCalledWith('/host/component', { name: 'foo-service' })
  })
})

describe('deleteComponent', () => {
  it('should make call to delete component', async () => {
    const componentStore = useComponentStore()

    await componentStore.deleteComponent(1)

    expect(api.delete).toHaveBeenCalled()
    expect(api.delete).toHaveBeenCalledWith('/host/component/1')
  })
})

describe('reloadExpandedComponentLists', () => {
  it('should call load on expanded hosts', async () => {
    const componentStore = useComponentStore()
    componentStore.expandedHosts = [1]

    await componentStore.create({ name: 'foo-service' })

    expect(api.post).toHaveBeenCalled()
    expect(api.post).toHaveBeenCalledWith('/host/component', { name: 'foo-service' })
    expect(api.get).toHaveBeenCalledWith('/host/1/components')
  })
})
