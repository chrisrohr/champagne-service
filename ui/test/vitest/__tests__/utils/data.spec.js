import { describe, expect, it, vi } from 'vitest';
import { doPagedRequest } from "src/utils/data";
import { api } from "src/boot/axios";
import { ref } from "vue";

vi.mock('src/boot/axios', () => {
  const api = {
    get: vi.fn().mockImplementation(() => Promise.resolve({data: {totalElements: 1, content: [{name: 'foo'}]}}))
  }

  return { api }
})

describe('doPagedRequest', () => {
  it('should use default paging when props are not provided', async () => {
    const loading = ref(false)
    const pagination = ref({
      page: 1,
      rowsPerPage: 10
    })
    const contents = ref([])

    await doPagedRequest(loading, undefined, pagination, '/foo', contents, undefined)

    expect(loading.value).toEqual(false)
    expect(pagination.value.rowsNumber).toEqual(1)
    expect(contents.value).toEqual([{name: 'foo'}])
    expect(api.get).toHaveBeenCalledWith('/foo', {params: {pageNumber: 1, pageSize: 10}})
  })

  it('should use provided paging props when props are provided', async () => {
    const loading = ref(false)
    const pagination = ref({
      page: 1,
      rowsPerPage: 10
    })
    const contents = ref([])

    await doPagedRequest(loading, { pagination: { page: 5, rowsPerPage: 25}}, pagination, '/foo', contents, undefined)

    expect(loading.value).toEqual(false)
    expect(pagination.value.rowsNumber).toEqual(1)
    expect(contents.value).toEqual([{name: 'foo'}])
    expect(api.get).toHaveBeenCalledWith('/foo', {params: {pageNumber: 5, pageSize: 25}})
  })

  it('should add filters when filters provided', async () => {
    const loading = ref(false)
    const pagination = ref({
      page: 1,
      rowsPerPage: 10
    })
    const contents = ref([])

    await doPagedRequest(loading, undefined, pagination, '/foo', contents, { someFilter: 'bar' })

    expect(loading.value).toEqual(false)
    expect(pagination.value.rowsNumber).toEqual(1)
    expect(contents.value).toEqual([{name: 'foo'}])
    expect(api.get).toHaveBeenCalledWith('/foo', {params: {pageNumber: 1, pageSize: 10, someFilter: 'bar'}})
  })
})
