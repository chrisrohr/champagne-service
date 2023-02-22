import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import HostConfigurationPage from 'pages/HostConfigurationPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { useEnvStore } from 'stores/envStore'
import { useHostStore } from 'stores/hostStore'
import { useComponentStore } from 'src/stores/componentStore'
import { confirmAction } from "src/utils/alerts";

installQuasar()

vi.mock('src/utils/alerts', () => {
  const confirmAction = vi.fn()

  return { confirmAction }
})

const pinia = createTestingPinia()
const envStore = useEnvStore(pinia)
const hostStore = useHostStore(pinia)

envStore.load.mockImplementation(() => Promise.resolve(123))
hostStore.load.mockImplementation(() => Promise.resolve(123))

const wrapper = shallowMount(HostConfigurationPage, {
  global: {
    plugins: [pinia]
  }
})

afterEach(() => {
  vi.clearAllMocks()
  vi.resetAllMocks()
})

describe('HostConfigurationPage', () => {
  it('should mount the host configuration page without errors', () => {
    expect(wrapper).toBeTruthy()
  })
})

describe('addTag', () => {
  it('should add the current host tag to the list of tags', () => {
    wrapper.vm.host.tag = 'foo'
    expect(wrapper.vm.host.tags).toEqual([])

    wrapper.vm.addTag()

    expect(wrapper.vm.host.tags).toEqual(['foo'])
    expect(wrapper.vm.host.tag).toBe('')
  })
})

describe('deleteTag', () => {
  it('should delete the given tag from the list of tags', () => {
    wrapper.vm.host.tags = ['foo']

    wrapper.vm.deleteTag('foo')

    expect(wrapper.vm.host.tags).toEqual([])
  })
})

describe('createHost', () => {
  it('should call the host store to create a new host', () => {
    hostStore.create.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.host.hostname = 'localhost'
    wrapper.vm.host.tags = ['foo']

    wrapper.vm.createHost()

    expect(hostStore.create).toHaveBeenLastCalledWith({ hostname: 'localhost', tags: ['foo'], source: 'CHAMPAGNE' })
  })
})

describe('tagListAsCsv', () => {
  it('should convert the list of tags into a csv string', () => {
    const list = ['foo', 'bar']
    const csv = wrapper.vm.tagListAsCsv(list)

    expect(csv).toEqual('foo, bar')
  })
})

describe('deleteHost', () => {
  it('should call deleteHost on the host store', () => {
    wrapper.vm.deleteHost(1)

    expect(hostStore.deleteHost).toHaveBeenCalledTimes(1)
    expect(hostStore.deleteHost).toHaveBeenLastCalledWith(1)
  })
})

describe('handleHostExpansion', () => {
  const componentStore = useComponentStore(pinia)

  it('should load the components for the host and track host being expanded when expanded', () => {
    wrapper.vm.handleHostExpansion({ expand: false, row: { id: 1 } })

    expect(componentStore.load).toHaveBeenCalledTimes(1)
    expect(componentStore.load).toHaveBeenLastCalledWith(1)
    expect(componentStore.expandedHosts).toEqual([1])
  })

  it('should stop tracking host being expanded when collapsed', () => {
    componentStore.expandedHosts = [1]

    wrapper.vm.handleHostExpansion({ expand: true, row: { id: 1 } })

    expect(componentStore.load).not.toHaveBeenCalled()
    expect(componentStore.expandedHosts).toEqual([])
  })
})

describe('createComponent', () => {
  it('should call the component store to create a new component', async () => {
    const componentStore = useComponentStore(pinia)
    componentStore.create.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.component.componentName = 'my-service'
    wrapper.vm.component.tag = 'foo'

    await wrapper.vm.createComponent()

    expect(componentStore.create).toHaveBeenLastCalledWith({ componentName: 'my-service', tag: 'foo' })
    expect(hostStore.load).toHaveBeenCalled()
  })
})

describe('deleteComponent', () => {
  it('should call deleteComponent on the component store', async () => {
    const componentStore = useComponentStore(pinia)
    componentStore.deleteComponent.mockImplementation(() => Promise.resolve(1))

    await wrapper.vm.deleteComponent(1)

    expect(componentStore.deleteComponent).toHaveBeenCalledTimes(1)
    expect(componentStore.deleteComponent).toHaveBeenLastCalledWith(1)

    expect(hostStore.load).toHaveBeenCalled()
  })
})

describe('confirmDeleteHost', () => {
  it('should call confirm utility', () => {
    wrapper.vm.confirmDeleteHost({hostname: 'localhost'})

    expect(confirmAction).toHaveBeenCalled()
    expect(confirmAction).toHaveBeenCalledWith('Are you sure you want to delete host localhost?', expect.any(Function))
  })
})

describe('confirmDeleteComponent', () => {
  it('should call confirm utility', () => {
    wrapper.vm.confirmDeleteComponent({componentName: 'foo-service'})

    expect(confirmAction).toHaveBeenCalled()
    expect(confirmAction).toHaveBeenCalledWith('Are you sure you want to delete component foo-service?', expect.any(Function))
  })
})
