import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest';
import { mount } from '@vue/test-utils';
import { Dialog } from 'quasar';
import { describe, expect, it, vi } from 'vitest';
import DialogComponent from './demo/DialogComponent.vue';

installQuasar({ plugins: { Dialog } });

describe('dialog example', () => {
  it('should call dialog on click', async () => {
    expect(DialogComponent).toBeTruthy();

    const wrapper = mount(DialogComponent, {});
    const spy = vi.spyOn(Dialog, 'create');
    expect(spy).not.toHaveBeenCalled();
    wrapper.trigger('click');
    expect(spy).toHaveBeenCalled();
  });
});
