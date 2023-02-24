<template>
    <q-page padding>
      <q-table title="Application Errors" :columns="errorColumns" :rows="errorStore.errors" :loading="errorStore.loading" v-model:pagination="errorStore.pagination" @request="errorStore.load">

        <template v-slot:top-right>
          <q-select outlined emit-value map-options v-model="errorStore.status" :options="filterOptions" label="Filter By" dense style="min-width: 150px" @update:model-value="errorStore.load()"/>
          <q-btn icon="fa-solid fa-wand-sparkles" label="Resolve All" @click="errorStore.resolveAll()" class="on-right"/>
        </template>

        <template v-slot:body-cell-firstOccurred="props">
          <q-td :props="props">
            <span>
              {{ fromNow(props.row.createdAt) }}
              <q-tooltip class="bg-grey-5 text-black">
                  {{ formatDate(props.row.createdAt) }}
              </q-tooltip>
            </span>
          </q-td>
        </template>

        <template v-slot:body-cell-lastOccurred="props">
          <q-td :props="props">
            <span>
              {{ fromNow(props.row.updatedAt) }}
              <q-tooltip class="bg-grey-5 text-black">
                  {{ formatDate(props.row.updatedAt) }}
              </q-tooltip>
            </span>
          </q-td>
        </template>

        <template v-slot:body-cell-actions="props">
          <q-td :props="props">
            <q-btn size="sm" icon="fa-solid fa-wand-sparkles" @click="errorStore.resolve(props.row.id)">
              <q-tooltip>Resolve error</q-tooltip>
            </q-btn>
          </q-td>
        </template>

      </q-table>
    </q-page>
</template>

<script setup>
import { onMounted } from 'vue'
import { formatDate, fromNow } from '../utils/time'
import { useErrorStore } from 'stores/errorStore'

// Stores
const errorStore = useErrorStore()

// Reactive data

// Constant data
const filterOptions = ['UNRESOLVED', 'RESOLVED']
const errorColumns = [
  {
    name: 'description',
    label: 'Description',
    field: 'description',
    align: 'left'
  },
  {
    name: 'numberOfOccurrences',
    label: 'Number of Occurrences',
    field: 'numTimesOccurred',
    align: 'left'
  },
  {
    name: 'firstOccurred',
    label: 'First Occurred',
    align: 'left'
  },
  {
    name: 'lastOccurred',
    label: 'Last Occurred',
    align: 'left'
  },
  {
    name: 'actions',
    label: 'Actions',
    align: 'left'
  }
]

// Methods

onMounted(() => {
  errorStore.load()
})
</script>
