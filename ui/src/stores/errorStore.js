import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'

export const useErrorStore = defineStore('error', () => {
  const errors = ref([])
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    rowsPerPage: 25,
    rowsNumber: 0
  })
  const status = ref('UNRESOLVED')

  async function load (props) {
    loading.value = true

    if (props !== undefined) {
      pagination.value.page = props.pagination.page
      pagination.value.rowsPerPage = props.pagination.rowsPerPage
    }

    const params = {
      pageNumber: pagination.value.page,
      pageSize: pagination.value.rowsPerPage,
      status: status.value
    }

    const response = await api.get('/kiwi/application-errors', { params })
    errors.value = response.data.items
    pagination.value.rowsNumber = response.data.totalCount
    loading.value = false
  }

  function resolve (id) {
    return api.put(`/kiwi/application-errors/resolve/${id}`)
      .then(() => load())
  }

  function resolveAll () {
    return api.put('/kiwi/application-errors/resolve')
      .then(() => load())
  }

  return { errors, loading, pagination, load, resolve, resolveAll, status }
})
