import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ErrorNotFound from 'pages/ErrorNotFound.vue'

installQuasar()

describe('ErrorNotFound', () => {
  it('should mount the error not found page without errors', () => {
    const wrapper = shallowMount(ErrorNotFound)
    expect(wrapper).toBeTruthy()
  })
})
