import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminSystemPage from 'pages/AdminSystemPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { useAdminSystemStore } from 'src/stores/adminSystemStore'
import { confirmAction } from 'src/utils/alerts'
import { useUserStore } from 'src/stores/userStore'

installQuasar()

vi.mock('src/utils/alerts', () => {
  const confirmAction = vi.fn()

  return { confirmAction }
})

const pinia = createTestingPinia()
const adminSystemStore = useAdminSystemStore(pinia)
const userStore = useUserStore(pinia)
userStore.load.mockImplementation(() => Promise.resolve(1))
userStore.users = [{ id: 1, displayName: 'John Doe' }]

const wrapper = shallowMount(AdminSystemPage, {
  global: {
    plugins: [pinia]
  }
})

describe('AdminSystemPage', () => {
  it('should mount the admin system page without errors', () => {
    expect(wrapper).toBeTruthy()
  })
})

describe('createSystem', () => {
  it('should call the admin system store to create a new system', () => {
    adminSystemStore.create.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.activeSystem.name = 'kiwi'

    wrapper.vm.createSystem()

    expect(adminSystemStore.create).toHaveBeenLastCalledWith({ name: 'kiwi' })
    expect(wrapper.vm.showSystemAdd).toEqual(false)
  })
})

describe('deleteSystem', () => {
  it('should call deleteSystem on the admin system store', () => {
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

describe('startAssignUsers', () => {
  it('should setup assign users dialog', () => {
    wrapper.vm.startAssignUsers({ id: 1, users: [] })

    expect(wrapper.vm.selectedSystem).toEqual(1)
    expect(wrapper.vm.showAssignUsers).toBeTruthy()
  })
})

describe('addOrUpdateUserInSystem', () => {
  it('should call the adminSystemStore to add or update user assigned', () => {
    adminSystemStore.assignUserToSystem.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.selectedSystem = 1
    wrapper.vm.selectedUser = { label: 'kiwi', value: 1 }
    wrapper.vm.selectedUserAdmin = false

    wrapper.vm.addOrUpdateUserInSystem()

    expect(adminSystemStore.assignUserToSystem).toHaveBeenCalled()
    expect(adminSystemStore.assignUserToSystem).toHaveBeenCalledWith(1, { userId: 1, admin: false })
  })
})

describe('removeUserFromSystem', () => {
  it('should call the adminSystemStore to remove assigned user', () => {
    // adminSystemStore.removeUserFromSystem.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.removeUserFromSystem(1, 2)

    expect(adminSystemStore.removeUserFromSystem).toHaveBeenCalled()
    expect(adminSystemStore.removeUserFromSystem).toHaveBeenCalledWith(1, 2)
  })
})
