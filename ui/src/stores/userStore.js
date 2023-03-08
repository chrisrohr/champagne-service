import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'
import { doPagedRequest } from 'src/utils/data'
import _ from 'lodash'

export const useUserStore = defineStore('user', () => {
  const users = ref([])
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    rowsPerPage: 25,
    rowsNumber: 0
  })

  async function load (props) {
    return doPagedRequest(loading, props, pagination, '/users', users)
  }

  function create (userData) {
    return api.post('/users', userData)
      .then(() => load())
  }

  function update (userData) {
    return api.put('/users', userData)
      .then(() => load())
  }

  function deleteUser (id) {
    api.delete(`/users/${id}`)
      .then(() => load())
  }

  function userForId (id) {
    return _.find(users.value, { id })
  }

  return { users, loading, pagination, load, create, update, deleteUser, userForId }
})
