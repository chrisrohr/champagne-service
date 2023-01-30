import { describe, expect, it } from '@jest/globals';
import { Notify, QNotifyCreateOptions } from 'quasar'
import { notifyError } from '../../../src/utils/alerts';
import {jest} from '@jest/globals'

describe('notifyError', () => {
    it('displays error without caption if error is undefined', () => {
        Notify.create = (opts: QNotifyCreateOptions | string) => {
            if (typeof opts === 'string') {
                opts = JSON.parse(opts) as QNotifyCreateOptions;
            }

            expect(opts.color).toBe('negative');
            expect(opts.icon).toBe('warning');
            expect(opts.message).toBe('Foo message');
            expect(opts.caption).toBe('');
            expect(opts.position).toBe('top');
            expect(opts.timeout).toBe(3000);
            return jest.fn();
        };
        notifyError("Foo message", undefined);
    });
});
