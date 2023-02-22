import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest';
import { shallowMount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import DashboardPage from "pages/DashboardPage.vue";

installQuasar()

describe('Dashboard', () => {
  it('should mount the Dashboard page without errors', () => {
    const wrapper = shallowMount(DashboardPage);
    expect(wrapper).toBeTruthy()
  })
})
