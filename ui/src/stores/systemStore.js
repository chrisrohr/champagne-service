import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'

export const useSystemStore = defineStore('system', () => {
  const systems = ref([])
  const loading = ref(false)
  const currentSystem = ref(null)

  async function load () {
    loading.value = true

    const response = await api.get('/systems')
    systems.value = response.data
    loading.value = false
  }

  return { systems, loading, currentSystem, load }
})
