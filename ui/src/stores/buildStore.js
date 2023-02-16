import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { ref } from 'vue'

export const useBuildStore = defineStore('build', () => {
  const builds = ref([])
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    rowsPerPage: 25,
    rowsNumber: 0
  })
  const filter = ref('')
  const filterType = ref('componentIdentifierFilter')

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

    if (filter.value !== '') {
      params[filterType.value] = filter.value
    }

    const response = await api.get('/build', { params })
    builds.value = response.data.content
    pagination.value.rowsNumber = response.data.totalElements
    loading.value = false
  }

  return { builds, loading, pagination, filter, filterType, load }
})
