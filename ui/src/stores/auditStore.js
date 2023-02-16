import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'

export const useAuditStore = defineStore('audit', () => {
  const audits = ref([])
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

    const response = await api.get('/audit', { params })
    audits.value = response.data.content
    pagination.value.rowsNumber = response.data.totalElements
    loading.value = false
  }

  return { audits, loading, pagination, load }
})
