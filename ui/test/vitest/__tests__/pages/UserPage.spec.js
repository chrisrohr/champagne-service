import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import UserPage from 'pages/UserPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { useUserStore } from 'src/stores/userStore'
import { confirmAction } from "src/utils/alerts";

installQuasar()

vi.mock('src/utils/alerts', () => {
  const confirmAction = vi.fn()

  return { confirmAction }
})

const wrapper = shallowMount(UserPage, {
  global: {
    plugins: [createTestingPinia()]
  }
})

describe('UserPage', () => {
  it('should mount the user page without errors', () => {
    expect(wrapper).toBeTruthy()
  })
})

describe('updateDisplayName', () => {
  it('should return the display name combining first and last name', () => {
    wrapper.vm.activeUser.firstName = 'Bob'
    wrapper.vm.activeUser.lastName = 'Roberts'

    wrapper.vm.updateDisplayName()

    expect(wrapper.vm.activeUser.displayName).toEqual('Bob Roberts')
  })
})

describe('createOrUpdateUser', () => {
  it('should call the user store to create a new user when no id', () => {
    const userStore = useUserStore()
    userStore.create.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.activeUser.firstName = 'Bob'
    wrapper.vm.activeUser.lastName = 'Roberts'
    wrapper.vm.activeUser.displayName = 'Bob Roberts'
    wrapper.vm.activeUser.systemIdentifier = 'brob'

    wrapper.vm.createOrUpdateUser()

    expect(userStore.create).toHaveBeenLastCalledWith({ id: null, firstName: 'Bob', lastName: 'Roberts', displayName: 'Bob Roberts', systemIdentifier: 'brob', admin: false })
    expect(wrapper.vm.showUserForm).toEqual(false)
  })

  it('should call the user store to update a new user when id', () => {
    const userStore = useUserStore()
    userStore.update.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.activeUser.id = 1
    wrapper.vm.activeUser.firstName = 'Bob'
    wrapper.vm.activeUser.lastName = 'Roberts'
    wrapper.vm.activeUser.displayName = 'Bob Roberts'
    wrapper.vm.activeUser.systemIdentifier = 'brob'

    wrapper.vm.createOrUpdateUser()

    expect(userStore.create).toHaveBeenLastCalledWith({ id: 1, firstName: 'Bob', lastName: 'Roberts', displayName: 'Bob Roberts', systemIdentifier: 'brob', admin: false })
    expect(wrapper.vm.showUserForm).toEqual(false)
  })
})

describe('deleteUser', () => {
  it('should call deleteUser on the user store', () => {
    const userStore = useUserStore()

    wrapper.vm.deleteUser(1)

    expect(userStore.deleteUser).toHaveBeenCalledTimes(1)
    expect(userStore.deleteUser).toHaveBeenLastCalledWith(1)
  })
})

describe('confirmDeleteUser', () => {
  it('should call confirm utility', () => {
    wrapper.vm.confirmDelete('Joe', 1)

    expect(confirmAction).toHaveBeenCalled()
    expect(confirmAction).toHaveBeenCalledWith('Are you sure you want to deactivate user Joe?', expect.any(Function))
  })
})

describe('setupUserFormForCreate', () => {
  it('should open a clean form', () => {
    wrapper.vm.setupUserFormForCreate()

    expect(wrapper.vm.showUserForm).toBeTruthy()
    expect(wrapper.vm.userFormTitle).toEqual('Add new user')
  })
})

describe('setupUserFormForEdit', () => {
  it('should open a form prepopulated', () => {
    const row = {
      id: 1,
      firstName: 'Joe',
      lastName: 'Doe',
      displayName: 'Joe Doe',
      systemIdentifier: 'jdoe',
      admin: false
    }
    wrapper.vm.setupUserFormForEdit(row)

    expect(wrapper.vm.showUserForm).toBeTruthy()
    expect(wrapper.vm.userFormTitle).toEqual('Update user')
    expect(wrapper.vm.activeUser).toEqual(row)
  })
})
