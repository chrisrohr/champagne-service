
const routes = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'DashboardPage', component: () => import('pages/DashboardPage.vue') },
      { path: '/users', name: 'UsersPage', component: () => import('pages/UserPage.vue') },
      { path: '/login', name: 'LoginPage', component: () => import('pages/LoginPage.vue') },
      { path: '/environments', name: 'DeploymentEnvironmentPage', component: () => import('pages/DeploymentEnvironmentPage.vue') },
      { path: '/audits', name: 'AuditRecordsPage', component: () => import('pages/AuditRecordsPage.vue') },
      { path: '/tasks', name: 'ManualTaskPage', component: () => import('pages/ManualTaskPage.vue') },
      { path: '/builds', name: 'BuildsPage', component: () => import('pages/BuildsPage.vue') },
      { path: '/hostConfig', name: 'HostConfigurationPage', component: () => import('pages/HostConfigurationPage.vue') },
      { path: '/errors', name: 'ErrorsPage', component: () => import('pages/ErrorsPage.vue') }
    ]
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue')
  }
]

export default routes
