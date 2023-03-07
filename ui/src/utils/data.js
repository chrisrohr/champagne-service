import { api } from 'src/boot/axios'
import { _ } from 'lodash'

async function doPagedRequest (loading, props, pagination, path, contents, filters) {
  loading.value = true

  if (props !== undefined) {
    pagination.value.page = props.pagination.page
    pagination.value.rowsPerPage = props.pagination.rowsPerPage
  }

  const params = {
    pageNumber: pagination.value.page,
    pageSize: pagination.value.rowsPerPage
  }

  if (filters !== undefined) {
    _.merge(params, filters)
  }

  const response = await api.get(path, { params })
  contents.value = response.data.content
  pagination.value.rowsNumber = response.data.totalElements
  loading.value = false

  return Promise.resolve(contents)
}

export { doPagedRequest }
