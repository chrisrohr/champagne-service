import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest';
import { shallowMount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import AuditRecordsPage from 'pages/AuditRecordsPage.vue'
import { createTestingPinia } from '@pinia/testing'

installQuasar()

describe('AuditRecordsPage', () => {
  it('should mount the audit records page without errors', () => {
    const wrapper = shallowMount(AuditRecordsPage, {
      global: {
        plugins: [createTestingPinia()],
      }
    });

    expect(wrapper).toBeTruthy()
  })
})
