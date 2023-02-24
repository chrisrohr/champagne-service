import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ErrorsPage from 'pages/ErrorsPage.vue'
import { createTestingPinia } from '@pinia/testing'

installQuasar()

describe('ErrorsPage', () => {
  it('should mount the errors page without errors', () => {
    const wrapper = shallowMount(ErrorsPage, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    expect(wrapper).toBeTruthy()
  })
})
