import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'
import { doPagedRequest } from 'src/utils/data'

export const useReleaseStore = defineStore('release', () => {
  const releases = ref([])
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    rowsPerPage: 25,
    rowsNumber: 0
  })

  async function load (props) {
    doPagedRequest(loading, props, pagination, '/manual/deployment/tasks/releases', releases)
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
