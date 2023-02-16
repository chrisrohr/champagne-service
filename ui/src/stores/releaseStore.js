import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'

export const useReleaseStore = defineStore('release', () => {
  const releases = ref([])
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

    const response = await api.get('/manual/deployment/tasks/releases', { params })
    releases.value = response.data.content
    pagination.value.rowsNumber = response.data.totalElements
    loading.value = false
  }

  function create (releaseData, callback = () => {}) {
    api.post('/manual/deployment/tasks/releases', releaseData)
      .then(() => load())
      .then(callback)
  }

  function deleteRelease (id) {
    api.delete(`/manual/deployment/tasks/releases/${id}`)
      .then(() => load())
  }

  function updateStatus (statusId, status, callback = () => {}) {
    api.put(`/manual/deployment/tasks/releases/${statusId}/${status}`)
      .then(() => load())
      .then(callback)
  }

  return { releases, loading, pagination, load, create, deleteRelease, updateStatus }
})
