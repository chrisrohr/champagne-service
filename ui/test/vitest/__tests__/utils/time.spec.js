import { describe, expect, it } from 'vitest';
import { fromNow, formatDate } from "src/utils/time";
import { DateTime } from "luxon";

describe('fromNow', () => {
  it('should convert date to relative human text', () => {
    const someTime = new Date().getTime()
    const expectedText = DateTime.fromMillis(someTime).toUTC().toRelative()

    const result = fromNow(someTime)

    expect(result).toEqual(expectedText)
  })
})

describe('formatDate', () => {
  it('should format date to standard format', () => {
    const someTime = new Date().getTime()
    const expectedText = DateTime.fromMillis(someTime).toUTC().toLocaleString(DateTime.DATETIME_FULL)

    const result = formatDate(someTime)

    expect(result).toEqual(expectedText)
  })
})
