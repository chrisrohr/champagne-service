import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const users = ref([])
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    rowsPerPage: 25,
    rowsNumber: 0
  })

  async function load (props) {
    loading.value = true

    if (props !== undefined) {
      pagination.value.page = props.pagination.page
      pagination.value.rowsPerPage = props.pagination.rowsPerPage
    }

    const params = {
      pageNumber: pagination.value.page,
      pageSize: pagination.value.rowsPerPage
    }

    const response = await api.get('/users', { params })
    users.value = response.data.content
    pagination.value.rowsNumber = response.data.totalElements
    loading.value = false
  }

  function create (userData) {
    api.post('/users', userData)
      .then(() => load())
  }

  function deleteUser (id) {
    api.delete(`/users/${id}`)
      .then(() => load())
  }

  return { users, loading, pagination, load, create, deleteUser }
})
