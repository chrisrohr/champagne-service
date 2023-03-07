import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminSystemPage from 'pages/AdminSystemPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { useAdminSystemStore } from 'src/stores/adminSystemStore'
import { confirmAction } from 'src/utils/alerts'

installQuasar()

vi.mock('src/utils/alerts', () => {
  const confirmAction = vi.fn()

  return { confirmAction }
})

const wrapper = shallowMount(AdminSystemPage, {
  global: {
    plugins: [createTestingPinia()]
  }
})

describe('AdminSystemPage', () => {
  it('should mount the admin system page without errors', () => {
    expect(wrapper).toBeTruthy()
  })
})

describe('createSystem', () => {
  it('should call the admin system store to create a new system', () => {
    const adminSystemStore = useAdminSystemStore()
    adminSystemStore.create.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.activeSystem.name = 'kiwi'

    wrapper.vm.createSystem()

    expect(adminSystemStore.create).toHaveBeenLastCalledWith({ name: 'kiwi' })
    expect(wrapper.vm.showSystemAdd).toEqual(false)
  })
})

describe('deleteSystem', () => {
  it('should call deleteSystem on the admin system store', () => {
    const adminSystemStore = useAdminSystemStore()

    wrapper.vm.deleteSystem(1)

    expect(adminSystemStore.deleteSystem).toHaveBeenCalledTimes(1)
    expect(adminSystemStore.deleteSystem).toHaveBeenLastCalledWith(1)
  })
})

describe('confirmDelete', () => {
  it('should call confirm utility', () => {
    wrapper.vm.confirmDelete({ name: 'kiwi', id: 1 })

    expect(confirmAction).toHaveBeenCalled()
    expect(confirmAction).toHaveBeenCalledWith('Are you sure you want to delete system kiwi? This will cause all related components to be deleted as well!', expect.any(Function))
  })
})
