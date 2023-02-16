import { api } from 'src/boot/axios'

async function doPagedRequest (loading, props, pagination, path, contents) {
  loading.value = true

  if (props !== undefined) {
    pagination.value.page = props.pagination.page
    pagination.value.rowsPerPage = props.pagination.rowsPerPage
  }

  const params = {
    pageNumber: pagination.value.page,
    pageSize: pagination.value.rowsPerPage
  }

  const response = await api.get(path, { params })
  contents.value = response.data.content
  pagination.value.rowsNumber = response.data.totalElements
  loading.value = false
}

export { doPagedRequest }
