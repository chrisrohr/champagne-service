import { describe, expect, it } from '@jest/globals';
import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-jest';
import moment from 'moment';
import { fromNow, formatDate } from '../../../src/utils/time';

installQuasarPlugin();

describe('fromNow', () => {
    it('returns how long in the past the given epoch milliseconds is', () => {
        const twoHoursAgo = moment().add(-2, 'hours').valueOf();
        expect(fromNow(twoHoursAgo)).toBe("2 hours ago")
    });
});

describe('formatDate', () => {
    it ('returns the utc format for the given milliseconds', () => {
        const now = moment();
        const expected = now.utc().format('YYYY-MM-DD HH:mm:ss[Z]');

        expect(formatDate(now.valueOf())).toBe(expected);
    });
});