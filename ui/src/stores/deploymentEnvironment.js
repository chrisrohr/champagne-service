import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'

export const useEnvStore = defineStore({
  id: 'env',
  state: () => ({
    envs: []
  }),
  getters: {
    getActiveEnvs () {
      return this.envs.filter((env) => !env.deleted)
    }
  },
  actions: {
    async load () {
      const response = await api.get('/environments')

      // update pinia state
      this.envs = response.data
    }
  }
})
