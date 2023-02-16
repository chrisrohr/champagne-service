<template>
    <q-page padding>
      <q-table title="Audits" :columns="auditColumns" :rows="auditStore.audits" :loading="auditStore.loading" v-model:pagination="auditStore.pagination" @request="auditStore.load">

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
import { onMounted } from 'vue'
import { formatDate, fromNow } from '../utils/time'
import { useAuditStore } from 'stores/auditStore'

// Stores
const auditStore = useAuditStore()

// Reactive data

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

onMounted(() => {
  auditStore.load()
})
</script>
