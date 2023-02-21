import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest';
import { shallowMount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import MainLayout from "layouts/MainLayout.vue";
import { createTestingPinia } from '@pinia/testing'

installQuasar()

describe('MainLayout', () => {
  it('should mount the MainLayout without errors', () => {
    const wrapper = shallowMount(MainLayout, {
      global: {
        plugins: [createTestingPinia()]
      }
    });
    expect(wrapper).toBeTruthy()
  })
})
