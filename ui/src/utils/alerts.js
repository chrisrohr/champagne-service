import { Dialog, Notify } from 'quasar'

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

function confirmAction (message, okCallback) {
  Dialog.create({
    title: 'Hold Up!',
    message,
    cancel: true,
    persistent: true
  }).onOk(okCallback)
}

export { notifyError, confirmAction }
