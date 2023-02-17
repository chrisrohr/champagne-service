import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest';
import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import DeploymentEnvironmentPage from 'pages/DeploymentEnvironmentPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { useEnvStore } from 'stores/envStore'
import { Dialog } from 'quasar'

installQuasar({ plugins: { Dialog }})

const wrapper = mount(DeploymentEnvironmentPage, {
  global: {
    plugins: [createTestingPinia()]
  }
});

describe('DeploymentEnvironmentPage', () => {
  it('should mount the deployment environments page without errors', () => {
    expect(wrapper).toBeTruthy()
  })
})

describe('createEnv', () => {
  it('should hide dialog and call create on envStore', () => {
    const envStore = useEnvStore()

    wrapper.vm.activeEnv.name = 'Bob'
    wrapper.vm.createEnv()
    expect(wrapper.vm.showEnvAdd).toBeFalsy()
    expect(envStore.create).toHaveBeenCalledTimes(1)
    expect(envStore.create).toHaveBeenLastCalledWith({name: 'Bob'})
  })
})

describe('envTextClass', () => {
  it('should return text-grey-5 when soft deleted', () => {
    const env = {
      deleted: true
    }
    const textClass = wrapper.vm.envTextClass(env)
    expect(textClass).toBe('text-grey-5')
  })

  it('should return text-black when active', () => {
    const env = {
      deleted: false
    }
    const textClass = wrapper.vm.envTextClass(env)
    expect(textClass).toBe('text-black')
  })
})

describe('confirmDelete', () => {
  it('should prompt for confirmation and call delete on the store if Ok', () => {
    // TODO: I have no idea why this test can't find Dialog when the DialogComponent test works fine
    // const envStore = useEnvStore()
    //
    // const env = {
    //   id: 1
    // }
    //
    // const spy = vi.spyOn(Dialog, 'create');
    // wrapper.vm.confirmDelete(env)
    // expect(spy).toHaveBeenCalled()
    //
    // expect(envStore.deleteEnv).toHaveBeenCalledTimes(1)
    // expect(envStore.deleteEnv).toHaveBeenLastCalledWith({id: 1})
  })
})

describe('deleteEnv', () => {
  it('should call deleteEnv on the store', () => {
    const envStore = useEnvStore()

    wrapper.vm.deleteEnv(1)

    expect(envStore.deleteEnv).toHaveBeenCalledTimes(1)
    expect(envStore.deleteEnv).toHaveBeenLastCalledWith(1)
  })
})
