import {createToaster} from "@meforma/vue-toaster";

function notifyError(msg) {
  const toaster = createToaster({
    position: 'top-right'
  });
  toaster.error(msg);
}

export { notifyError };
