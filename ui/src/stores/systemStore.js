import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref, watch } from 'vue'

export const useSystemStore = defineStore('system', () => {
  const systems = ref([])
  const loading = ref(false)
  const currentSystem = ref(JSON.parse(localStorage.getItem('champagne-system')))

  watch(currentSystem, () => localStorage.setItem('champagne-system', JSON.stringify(currentSystem.value)))

  async function load () {
    loading.value = true

    const response = await api.get('/systems')
    systems.value = response.data
    loading.value = false

    if (currentSystem.value === null) {
      currentSystem.value = systems.value[0]
    }
  }

  return { systems, loading, currentSystem, load }
})
