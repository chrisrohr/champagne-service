import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'
import { doPagedRequest } from 'src/utils/data'

export const useUserStore = defineStore('user', () => {
  const users = ref([])
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    rowsPerPage: 25,
    rowsNumber: 0
  })

  async function load (props) {
    doPagedRequest(loading, props, pagination, '/users', users)
  }

  function create (userData) {
    return api.post('/users', userData)
      .then(() => load())
  }

  function deleteUser (id) {
    api.delete(`/users/${id}`)
      .then(() => load())
  }

  return { users, loading, pagination, load, create, deleteUser }
})
