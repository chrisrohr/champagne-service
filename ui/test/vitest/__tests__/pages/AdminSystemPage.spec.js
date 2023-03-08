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

describe('startAssignUsers', () => {
  it('should setup assign users dialog', () => {
    const userStore = useUserStore()
    userStore.load.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.startAssignUsers({ id: 1, users: [] })

    expect(userStore.load).toHaveBeenCalled()
  })
})

describe('populateNameOnExistingUsers', () => {
  it('should add display name', () => {
    wrapper.vm.allUsers = [{ label: 'John Doe', value: 1 }]

    const users = [
      { userId: 1, admin: false }
    ]

    wrapper.vm.populateNameOnExistingUsers(users)

    expect(users[0].displayName).toEqual('John Doe')
  })
})

describe('addUserToSystem', () => {
  it('should add the user to the list for update', () => {
    wrapper.vm.selectedUser = { label: 'John Doe', value: 1 }

    wrapper.vm.addUserToSystem()

    expect(wrapper.vm.systemUsers.users).toEqual([{ userId: 1, displayName: 'John Doe', admin: false }])
  })
})

describe('removeSelectedUser', () => {
  it('should remove the user from the list for update', () => {
    const userToRemove = { userId: 1, displayName: 'John Doe', admin: false }
    wrapper.vm.systemUsers.users = userToRemove

    wrapper.vm.removeSelectedUser([userToRemove])

    expect(wrapper.vm.systemUsers.users).toEqual([])
  })
})

describe('addUsersToSystem', () => {
  it('should call the adminSystemStore to update users assigned', () => {
    const adminSystemStore = useAdminSystemStore()
    adminSystemStore.assignUsersToSystem.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.addUsersToSystem()

    expect(adminSystemStore.assignUsersToSystem).toHaveBeenCalled()
  })
})
