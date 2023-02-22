import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import DeploymentEnvironmentPage from 'pages/DeploymentEnvironmentPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { useEnvStore } from 'stores/envStore'
import { confirmAction } from 'src/utils/alerts'

installQuasar()

vi.mock('src/utils/alerts', () => {
  const confirmAction = vi.fn()

  return { confirmAction }
})

const wrapper = mount(DeploymentEnvironmentPage, {
  global: {
    plugins: [createTestingPinia()]
  }
})

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
    expect(envStore.create).toHaveBeenLastCalledWith({ name: 'Bob' })
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

describe('deleteEnv', () => {
  it('should call deleteEnv on the store', () => {
    const envStore = useEnvStore()

    wrapper.vm.deleteEnv(1)

    expect(envStore.deleteEnv).toHaveBeenCalledTimes(1)
    expect(envStore.deleteEnv).toHaveBeenLastCalledWith(1)
  })
})

describe('confirmDelete', () => {
  it('should call confirm utility', () => {
    wrapper.vm.confirmDelete({name: 'dev'})

    expect(confirmAction).toHaveBeenCalled()
    expect(confirmAction).toHaveBeenCalledWith('Are you sure you want to delete environment dev? This will cause all related deployments to be deleted as well!', expect.any(Function))
  })
})
