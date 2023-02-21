import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref, watch } from 'vue'

export const useHostStore = defineStore('host', () => {
  const hosts = ref([])
  const loading = ref(false)
  const environmentFilter = ref({})
  const componentFilter = ref('')

  watch(environmentFilter, load)

  async function load () {
    loading.value = true
    if (environmentFilter.value !== undefined) {
      api.get(`/host/${environmentFilter.value.value}`, { componentFilter: componentFilter.value })
        .then((response) => {
          hosts.value = response.data
        })
        .finally(() => {
          loading.value = false
        })
    }
  }

  function create (hostData) {
    hostData.environmentId = environmentFilter.value.value
    return api.post('/host', hostData)
      .then(() => load())
  }

  function deleteHost (id) {
    api.delete(`/host/${id}`)
      .then(() => load())
  }

  return { hosts, loading, environmentFilter, componentFilter, load, create, deleteHost }
})
