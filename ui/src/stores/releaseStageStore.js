import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'

export const useReleaseStageStore = defineStore('releaseStage', () => {
  const stages = ref([])
  async function load () {
    const response = await api.get('/manual/deployment/tasks/stages')
    stages.value = response.data
  }

  return { stages, load }
})
