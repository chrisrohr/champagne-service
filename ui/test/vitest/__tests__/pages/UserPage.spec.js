import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import UserPage from 'pages/UserPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { useUserStore } from 'src/stores/userStore'

installQuasar()

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

describe('createUser', () => {
  it('should call the user store to create a new user', () => {
    const userStore = useUserStore()

    wrapper.vm.activeUser.firstName = 'Bob'
    wrapper.vm.activeUser.lastName = 'Roberts'
    wrapper.vm.activeUser.displayName = 'Bob Roberts'
    wrapper.vm.activeUser.systemIdentifier = 'brob'

    wrapper.vm.createUser()

    expect(userStore.create).toHaveBeenLastCalledWith({ firstName: 'Bob', lastName: 'Roberts', displayName: 'Bob Roberts', systemIdentifier: 'brob' })
    expect(wrapper.vm.showUserAdd).toEqual(false)
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
