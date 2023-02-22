import { defineStore } from 'pinia'
import { ref } from 'vue'
import { doPagedRequest } from 'src/utils/data'

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
    const filters = {}

    if (filter.value !== '') {
      filters[filterType.value] = filter.value
    }

    doPagedRequest(loading, props, pagination, '/build', builds, filters)
  }

  return { builds, loading, pagination, filter, filterType, load }
})
