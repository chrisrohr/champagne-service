import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import LoginPage from 'pages/LoginPage.vue'
import { createTestingPinia } from '@pinia/testing'

installQuasar()

describe('LoginPage', () => {
  it('should mount the login page without errors', () => {
    const wrapper = shallowMount(LoginPage, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    expect(wrapper).toBeTruthy()
  })
})
