<template>
    <q-page padding>
      <q-table title="Deployable Systems" :columns="systemColumns" :rows="adminSystemStore.systems" :loading="adminSystemStore.loading" v-model:pagination="adminSystemStore.pagination" @request="adminSystemStore.load">

        <template v-slot:body-cell-createdAt="props">
          <q-td :props="props">
            {{ fromNow(props.row.createdAt) }}
            <q-tooltip class="bg-grey-5 text-black">
                {{ formatDate(props.row.createdAt) }}
            </q-tooltip>
          </q-td>
        </template>

        <template v-slot:body-cell-updatedAt="props">
          <q-td :props="props">
            {{ fromNow(props.row.updatedAt) }}
            <q-tooltip class="bg-grey-5 text-black">
                {{ formatDate(props.row.updatedAt) }}
            </q-tooltip>
          </q-td>
        </template>

        <template v-slot:body-cell-actions="props">
          <q-td :props="props">
            <q-btn size="sm" icon="delete" @click="confirmDelete(props.row)">
              <q-tooltip>Delete System</q-tooltip>
            </q-btn>
          </q-td>
        </template>

      </q-table>

      <q-page-sticky position="bottom-right" :offset="[18, 18]">
        <q-btn fab icon="add" color="accent" @click="showSystemAdd = true" />
      </q-page-sticky>

      <q-dialog v-model="showSystemAdd">
        <q-card>
          <q-card-section>
            <div class="text-h5">Add new deployable system</div>
          </q-card-section>
          <q-card-section>
            <div class="row q-mb-md">
              <q-input dense style="min-width: 500px" v-model="activeSystem.name" label="System Name" :rules="[val => !!val || 'System Name is required']"/>
            </div>
          </q-card-section>
          <q-card-actions align="right">
            <q-btn flat v-close-popup>Cancel</q-btn>
            <q-btn flat color="primary" @click="createSystem">Save</q-btn>
          </q-card-actions>
        </q-card>
      </q-dialog>
    </q-page>
  </template>

<script setup>
import { onMounted, ref } from 'vue'
import { formatDate, fromNow } from '../utils/time'
import { confirmAction } from '../utils/alerts'
import { useAdminSystemStore } from 'stores/adminSystemStore'

const adminSystemStore = useAdminSystemStore()

// Reactive data
const showSystemAdd = ref(false)
const activeSystem = ref({
  name: ''
})

// Constant data
const systemColumns = [
  {
    name: 'name',
    label: 'Name',
    field: 'name',
    align: 'left'
  },
  {
    name: 'createdAt',
    label: 'Created At',
    align: 'left'
  },
  {
    name: 'updatedAt',
    label: 'Updated At',
    align: 'left'
  },
  {
    name: 'actions',
    label: 'Actions',
    align: 'left'
  }
]

// Methods
function createSystem () {
  showSystemAdd.value = false
  adminSystemStore.create(activeSystem.value)
}

function confirmDelete (system) {
  confirmAction(
      `Are you sure you want to delete system ${system.name}? This will cause all related components to be deleted as well!`,
      () => deleteSystem(system.id)
  )
}

function deleteSystem (id) {
  adminSystemStore.deleteSystem(id)
}

onMounted(() => {
  adminSystemStore.load()
})

</script>
