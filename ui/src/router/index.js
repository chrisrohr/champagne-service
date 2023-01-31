import { route } from 'quasar/wrappers'
import { createRouter, createMemoryHistory, createWebHistory, createWebHashHistory } from 'vue-router'
import routes from './routes'
import { useAuthStore } from 'stores/auth'

/*
 * If not building with SSR mode, you can
 * directly export the Router instantiation;
 *
 * The function below can be async too; either use
 * async/await or return a Promise which resolves
 * with the Router instance.
 */

export default route(function () {
  const webHistoryMethod = process.env.VUE_ROUTER_MODE === 'history' ? createWebHistory : createWebHashHistory

  const createHistory = process.env.SERVER ? createMemoryHistory : webHistoryMethod

  const Router = createRouter({
    scrollBehavior: () => ({ left: 0, top: 0 }),
    routes,

    // Leave this as is and make changes in quasar.conf.js instead!
    // quasar.conf.js -> build -> vueRouterMode
    // quasar.conf.js -> build -> publicPath
    history: createHistory(process.env.VUE_ROUTER_BASE)
  })

  let isFirstTransition = true
  Router.beforeEach((to, from, next) => {
    // redirect to login page if not logged in and trying to access a restricted page
    const publicPages = ['LoginPage']
    const authRequired = !publicPages.includes(to.name)

    const auth = useAuthStore()

    if (authRequired && !auth.user) {
      auth.returnUrl = to.fullPath
      next({ name: 'LoginPage' })
      return true
    }

    // Redirect to last used page on first transition
    const lastRouteName = localStorage.getItem('champagne-last-page') ?? ''
    const shouldRedirect = to.name === 'dashboard' && lastRouteName && isFirstTransition

    if (shouldRedirect) {
      next({ name: lastRouteName })
    } else {
      next()
    }

    isFirstTransition = false
  })

  Router.afterEach((to) => {
    if (to.path !== '/login') {
      localStorage.setItem('champagne-last-page', to.name)
    }
  })

  return Router
})
