import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'

export const useReleaseStageStore = defineStore({
  id: 'releaseStage',
  state: () => ({
    stages: []
  }),
  actions: {
    async load () {
      const response = await api.get('/manual/deployment/tasks/stages')

      // update pinia state
      this.stages = response.data
    }
  }
})
