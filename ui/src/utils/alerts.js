import { Notify } from 'quasar'

function notifyError (message, error) {
  Notify.create({
    color: 'negative',
    icon: 'warning',
    message,
    caption: error ? error.message : '',
    position: 'top',
    timeout: 3000
  })
}

export { notifyError }
