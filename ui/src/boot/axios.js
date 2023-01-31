import { boot } from 'quasar/wrappers'
import axios from 'axios'
import { notifyError } from 'src/utils/alerts'

// Be careful when using SSR for cross-request state pollution
// due to creating a Singleton instance here;
// If any client changes this (global) instance, it might be a
// good idea to move this instance creation inside of the
// "export default () => {}" function below (which runs individually
// for each client)
const api = axios.create({
  withCredentials: true,
  baseURL: process.env.API_BASE_URL
})

export default boot(({ app, router }) => {
  // for use inside Vue files (Options API) through this.$axios and this.$api

  app.config.globalProperties.$axios = axios
  // ^ ^ ^ this will allow you to use this.$axios (for Vue Options API form)
  //       so you won't necessarily have to import axios in each vue file

  app.config.globalProperties.$api = api
  // ^ ^ ^ this will allow you to use this.$api (for Vue Options API form)
  //       so you can easily perform requests against your app's API

  api.interceptors.response.use((response) => {
    return response
  }, (error) => {
    if (error.response.status) {
      switch (error.response.status) {
        case 401:
          router.push('/login')
          break
        case 500:
          notifyError(error.response.data.message)
          break
      }
    }
  })
})

export { api }
