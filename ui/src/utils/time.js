import { DateTime } from 'luxon'

function fromNow (val) {
  return DateTime.fromMillis(val).toUTC().toRelative()
}

function formatDate (val) {
  return DateTime.fromMillis(val).toUTC().toLocaleString(DateTime.DATETIME_FULL)
}

export { fromNow, formatDate }
