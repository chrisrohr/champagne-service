<template>
  <q-page padding>
    <q-table title="Deployment Environments" :columns="envColumns" :rows="envStore.envs" :loading="envStore.loading" :pagination="pagination" hide-pagination>

      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <span :class="envTextClass(props.row)">{{props.row.name}}</span>
        </q-td>
      </template>

      <template v-slot:body-cell-createdAt="props">
        <q-td :props="props">
          <span :class="envTextClass(props.row)">
            {{ fromNow(props.row.createdAt) }}
            <q-tooltip class="bg-grey-5 text-black">
                {{ formatDate(props.row.createdAt) }}
            </q-tooltip>
          </span>
        </q-td>
      </template>

      <template v-slot:body-cell-updatedAt="props">
        <q-td :props="props">
          <span :class="envTextClass(props.row)">
            {{ fromNow(props.row.updatedAt) }}
            <q-tooltip class="bg-grey-5 text-black">
                {{ formatDate(props.row.updatedAt) }}
            </q-tooltip>
          </span>
        </q-td>
      </template>

      <template v-slot:body-cell-actions="props">
        <q-td :props="props">
          <q-btn size="sm" icon="visibility_off" @click="envStore.deactivate(props.row.id)" v-if="!props.row.deleted">
            <q-tooltip>Deactivate Environment</q-tooltip>
          </q-btn>
          <q-btn size="sm" icon="visibility" @click="envStore.activate(props.row.id)" v-if="props.row.deleted">
            <q-tooltip>Activate Environment</q-tooltip>
          </q-btn>
          <q-btn size="sm" icon="delete" @click="deleteEnv(props.row)">
            <q-tooltip>Delete Environment</q-tooltip>
          </q-btn>
        </q-td>
      </template>

    </q-table>

    <q-page-sticky position="bottom-right" :offset="[18, 18]">
      <q-btn fab icon="add" color="accent" @click="showEnvAdd = true" />
    </q-page-sticky>

    <q-dialog v-model="showEnvAdd">
      <q-card>
        <q-card-section>
          <div class="text-h5">Add new deployment environment</div>
        </q-card-section>
        <q-card-section>
          <div class="row q-mb-md">
            <q-input dense style="min-width: 500px" v-model="activeEnv.name" label="Environment Name" :rules="[val => !!val || 'Environment Name is required']"/>
          </div>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat v-close-popup>Cancel</q-btn>
          <q-btn flat color="primary" @click="createEnv">Save</q-btn>
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { formatDate, fromNow } from '../utils/time'
import { useQuasar } from 'quasar'
import { useEnvStore } from 'stores/envStore'

const $q = useQuasar()
const envStore = useEnvStore()

// Reactive data
const showEnvAdd = ref(false)
const activeEnv = ref({
  name: ''
})

// Constant data
const pagination = {
  rowsPerPage: 200
}

const envColumns = [
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
function createEnv () {
  showEnvAdd.value = false
  envStore.create(activeEnv.value)
}

function deleteEnv (env) {
  $q.dialog({
    title: 'Hold Up!',
    message: `Are you sure you want to delete environment ${env.name}? This will cause all related deployments to be deleted as well!`,
    cancel: true,
    persistent: true
  }).onOk(() => {
    envStore.deleteEnv(env.id)
  })
}

function envTextClass (env) {
  return env.deleted ? 'text-grey-5' : 'text-black'
}

onMounted(() => {
  envStore.load()
})
</script>
