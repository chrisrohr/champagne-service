import { defineStore } from 'pinia'
import { ref } from 'vue'
import { doPagedRequest } from 'src/utils/data'

export const useAuditStore = defineStore('audit', () => {
  const audits = ref([])
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    rowsPerPage: 25,
    rowsNumber: 0
  })

  async function load (props) {
    doPagedRequest(loading, props, pagination, '/audit', audits)
  }

  return { audits, loading, pagination, load }
})
