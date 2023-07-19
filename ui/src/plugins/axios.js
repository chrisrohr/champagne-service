import axios from "axios";
import {notifyError} from "@/utils/alerts";
import {useCurrentUserStore} from "@/stores/currentUser";

const api = axios.create({
  baseURL: 'http://127.0.0.1:8080',
  withCredentials: true
});

function initAxios(router) {
  api.interceptors.request.use(config => {
    const currentUserStore = useCurrentUserStore();
    config.headers['Champagne-Deployable-System'] = currentUserStore.activeDeployableSystem?.id;
    return config;
  }, error => {
    return Promise.reject(error);
  });

  api.interceptors.response.use(response => {
    return response;
  }, async (error) => {
    const currentUserStore = useCurrentUserStore();
    if (error.response.status) {
      switch (error.response.status) {
        case 401:
          if (error.request.responseURL.indexOf('/current') === -1) {
            await currentUserStore.logout();
            await router.push({ name: 'login' });
          }
          break;
        case 500:
          notifyError(error.response.data.message);
          break;
      }
    }
  });
}

export { initAxios, api };
