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

  async function create (systemData) {
    return api.post('/systems', systemData)
      .then(() => load())
  }

  function deleteSystem (id) {
    api.delete(`/systems/${id}`)
      .then(() => load())
  }

  async function assignUserToSystem (systemId, userToSet) {
    return api.post(`/systems/${systemId}/user`, userToSet)
      .then(() => load())
  }

  function removeUserFromSystem (systemId, userId) {
    api.delete(`/systems/${systemId}/users/${userId}`)
      .then(() => load())
  }

  return { systems, loading, load, create, deleteSystem, assignUserToSystem, removeUserFromSystem }
})
