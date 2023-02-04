import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import _ from 'lodash'

export const useEnvStore = defineStore({
  id: 'env',
  state: () => ({
    envs: []
  }),
  getters: {
    getActiveEnvs () {
      return this.envs.filter((env) => !env.deleted)
    },
    getEnvIdForName () {
      return (name) => _.first(this.getActiveEnvs.filter((env) => env.name === name)).id
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
