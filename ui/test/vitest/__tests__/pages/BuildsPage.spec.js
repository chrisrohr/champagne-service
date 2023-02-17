import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest';
import { shallowMount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';
import BuildsPage from 'pages/BuildsPage.vue'
import { createTestingPinia } from '@pinia/testing'

installQuasar()

const wrapper = shallowMount(BuildsPage, {
  global: {
    plugins: [createTestingPinia()],
  }
});

describe('BuildsPage', () => {
  it('should mount the builds page without errors', () => {
    expect(wrapper).toBeTruthy()
  })
})

describe('formatChangeLog', () => {
  it('should return the given line', () => {
    const logLine = wrapper.vm.formatChangeLog('foo')
    expect(logLine).toBe('foo')
  })
})

describe('startPromotionOfBuild', () => {
  it('should alert the text "Coming Soon"', () => {
    alert = vi.fn()

    wrapper.vm.startPromotionOfBuild()

    expect(alert.mock.calls.length).toBe(1)
  })
})

describe('startPreviewPromotion', () => {
  it('should alert the text "Coming Soon"', () => {
    alert = vi.fn()

    wrapper.vm.startPreviewPromotion()

    expect(alert.mock.calls.length).toBe(1)
  })
})
