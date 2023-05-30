import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { computed, ref } from 'vue'

export const useComponentStore = defineStore('component', () => {
  const components = ref({})
  const loading = ref({})
  const expandedHosts = ref([])

  const componentsByHost = computed(() => {
    return (hostId) => components.value[hostId]
  })
  const loadingForComponents = computed(() => {
    return (hostId) => loading.value[hostId]
  })

  async function load (hostId) {
    loading.value[hostId] = true

    const response = await api.get(`/host/${hostId}/components`)
    components.value[hostId] = response.data
    loading.value[hostId] = false
  }

  function create (componentData) {
    return api.post('/host/component', componentData)
      .then(() => reloadExpandedComponentLists())
  }

  function deleteComponent (id) {
    return api.delete(`/host/component/${id}`)
      .then(() => reloadExpandedComponentLists())
  }

  async function reloadExpandedComponentLists () {
    const loadPromises = []
    expandedHosts.value.forEach((id) => {
      loadPromises.push(load(id))
    })

    await loadPromises.all(loadPromises)
  }

  return { componentsByHost, loadingForComponents, expandedHosts, load, create, deleteComponent }
})
