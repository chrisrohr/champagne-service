import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest';
import { shallowMount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import App from "src/App.vue";
import { createTestingPinia } from '@pinia/testing'

installQuasar()

describe('App', () => {
  it('should run App without errors', () => {
    const wrapper = shallowMount(App, {
      global: {
        plugins: [createTestingPinia()]
      }
    });
    expect(wrapper).toBeTruthy()
  })
})
