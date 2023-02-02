<template>
    <q-page padding>
      <q-table title="Audits" :columns="auditColumns" :rows="audits" :loading="loading" v-model:pagination="pagination" @request="loadAudits">

        <template v-slot:body-cell-timestamp="props">
          <q-td :props="props">
            <span>
              {{ fromNow(props.row.timestamp) }}
              <q-tooltip class="bg-grey-5 text-black">
                  {{ formatDate(props.row.timestamp) }}
              </q-tooltip>
            </span>
          </q-td>
        </template>

      </q-table>
    </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { formatDate, fromNow } from '../utils/time'
import { api } from 'boot/axios'

// Reactive data
const audits = ref([])
const loading = ref(false)
const pagination = ref({
  page: 1,
  rowsPerPage: 20,
  rowsNumber: 0
})

// Constant data
const auditColumns = [
  {
    name: 'action',
    label: 'Action',
    field: 'action',
    align: 'left'
  },
  {
    name: 'timestamp',
    label: 'Date/Time',
    align: 'left'
  },
  {
    name: 'userSystemIdentifier',
    label: 'Username',
    field: 'userSystemIdentifier',
    align: 'left'
  },
  {
    name: 'recordType',
    label: 'Type',
    field: 'recordType',
    align: 'left'
  },
  {
    name: 'recordId',
    label: 'Record Id',
    field: 'recordId',
    align: 'left'
  }
]

// Methods
function loadAudits (props) {
  loading.value = true

  let { page, rowsPerPage } = pagination.value

  if (props !== undefined) {
    page = props.pagination.page
    rowsPerPage = props.pagination.rowsPerPage
  }

  const params = {
    pageNumber: page,
    pageSize: rowsPerPage
  }

  api.get('/audit', { params })
    .then((response) => {
      const { data } = response
      audits.value = data.content
      pagination.value.page = page
      pagination.value.rowsPerPage = rowsPerPage
      pagination.value.rowsNumber = data.totalElements
    })
    .finally(() => { loading.value = false })
}

onMounted(() => {
  loadAudits()
})
</script>
