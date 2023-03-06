import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'
import { doPagedRequest } from 'src/utils/data'

export const useAdminSystemStore = defineStore('adminSystem', () => {
  const systems = ref([])
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    rowsPerPage: 25,
    rowsNumber: 0
  })

  async function load (props) {
    doPagedRequest(loading, props, pagination, '/systems/admin', systems)
  }

  function create (systemData) {
    return api.post('/systems', systemData)
      .then(() => load())
  }

  function deleteSystem (id) {
    api.delete(`/systems/${id}`)
      .then(() => load())
  }

  return { systems, loading, load, create, deleteSystem }
})
