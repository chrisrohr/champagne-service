import { Notify } from 'quasar';

function notifyError(message: string, error: Error|undefined) {
    Notify.create({
        color: 'negative',
        icon: 'warning',
        message,
        caption: error ? error.message : '',
        position: 'top',
        timeout: 3000
    });
}

export { notifyError };
