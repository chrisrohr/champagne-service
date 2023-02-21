<template>
  <q-page padding>
    <q-table :columns="releaseColumns" :rows="releaseStore.releases" :loading="releaseStore.loading" v-model:pagination="releaseStore.pagination" row-key="releaseId" @request="releaseStore.load" title="Manual Deployment Tasks">
      <template v-slot:body="props">
        <q-tr :props="props">
          <q-td>
            <q-btn round size="sm" color="primary" :icon="props.expand ? 'remove' : 'add'" @click="handleExpansion(props)"/>
          </q-td>
          <q-td key="release" :props="props">
            {{ props.row.releaseNumber }}
          </q-td>
          <q-td v-for="env in envColumns()" :key="env.name" :props="props">
            <span @click="startStatusUpdate('RELEASE', props.row, env.label)">
              <q-icon name="pending" class="text-red" size="sm" v-if="isInStatus(env.label, props.row, 'PENDING')">
                <q-tooltip class="bg-red text-black">
                  Manual deployment tasks are pending
                </q-tooltip>
              </q-icon>
              <q-icon name="check_circle" class="text-lime" size="sm" v-if="isInStatus(env.label, props.row, 'COMPLETE')">
                <q-tooltip class="bg-lime text-black">
                  Manual deployment tasks are complete
                </q-tooltip>
              </q-icon>
              <q-icon name="do_not_disturb_on" class="text-grey-5" size="sm" v-if="isInStatus(env.label, props.row, 'NOT_REQUIRED')">
                <q-tooltip class="bg-grey-5 text-black">
                  Manual deployment tasks are not needed for this environment
                </q-tooltip>
              </q-icon>
            </span>
          </q-td>
          <q-td key="actions" :props="props">
            <q-btn size="sm" icon="add" @click="startAddTask(props.row)">
              <q-tooltip>Create Task</q-tooltip>
            </q-btn>
            <q-btn size="sm" icon="delete" @click="deleteRelease(props.row)">
              <q-tooltip>Delete Release</q-tooltip>
            </q-btn>
          </q-td>
        </q-tr>
        <q-tr v-show="props.expand" :props="props">
          <q-td colspan="100%">
            <q-table :columns="taskColumns" :rows="taskStore.tasksForRelease(props.row.id)" :pagination="taskListingPagination" hide-pagination row-key="id" :loading="taskStore.loadingForTasks(props.row.id)">
              <template v-slot:body="props">
                <q-td key="stage" :props="props">
                  {{ props.row.stage }}
                </q-td>
                <q-td key="component" :props="props">
                  {{ props.row.component }}
                </q-td>
                <q-td key="summary" :props="props">
                  {{ props.row.summary }}
                </q-td>
                <q-td key="description" :props="props">
                  {{ props.row.description }}
                </q-td>
                <q-td v-for="env in envColumns()" :key="env.name" :props="props">
                  <span @click="startStatusUpdate('TASK', props.row, env.label)">
                    <q-icon name="pending" class="text-red" size="sm" v-if="isInStatus(env.label, props.row, 'PENDING')">
                      <q-tooltip class="bg-red text-black">
                        Manual deployment tasks are pending
                      </q-tooltip>
                    </q-icon>
                    <q-icon name="check_circle" class="text-lime" size="sm" v-if="isInStatus(env.label, props.row, 'COMPLETE')">
                      <q-tooltip class="bg-lime text-black">
                        Manual deployment tasks are complete
                      </q-tooltip>
                    </q-icon>
                    <q-icon name="do_not_disturb_on" class="text-grey-5" size="sm" v-if="isInStatus(env.label, props.row, 'NOT_REQUIRED')">
                      <q-tooltip class="bg-grey-5 text-black">
                        Manual deployment tasks are not needed for this environment
                      </q-tooltip>
                    </q-icon>
                  </span>
                </q-td>
                <q-td key="actions" :props="props">
                  <q-btn size="sm" icon="delete" @click="deleteTask(props.row)">
                    <q-tooltip>Delete Task</q-tooltip>
                  </q-btn>
                </q-td>
              </template>
            </q-table>
          </q-td>
        </q-tr>
      </template>
    </q-table>

    <q-page-sticky position="bottom-right" :offset="[18, 18]">
      <q-btn fab icon="add" color="accent" @click="showReleaseAdd = true" />
    </q-page-sticky>

    <q-dialog v-model="showReleaseAdd">
      <q-card>
        <q-card-section>
          <div class="text-h6">Create a Manual Deployment Release</div>
        </q-card-section>
        <q-card-section class="row">
          <q-input v-model="release.releaseNumber" type="text" outlined dense style="min-width: 205px" label="Release Number"/>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" v-close-popup />
          <q-btn flat label="Create" class="text-primary" @click="createRelease"/>
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showUpdateStatusDialog">
      <q-card>
        <q-card-section>
          <div class="text-h6">Update Status</div>
        </q-card-section>
        <q-card-section class="row">
          <q-input v-model="update.env" type="text" disabled="true" outlined dense style="min-width: 205px" label="Environment"/>
        </q-card-section>
        <q-card-section class="row">
          <q-select v-model="update.status" :options="statusOptions" label="Status" outlined dense style="min-width: 205px"/>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" v-close-popup />
          <q-btn flat label="Update" class="text-primary" @click="updateStatus"/>
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showAddTaskDialog">
      <q-card>
        <q-card-section>
          <div class="text-h6">Create a Manual Deployment Task</div>
        </q-card-section>
        <q-card-section class="row">
          <q-input v-model="task.summary" type="text" disabled="true" outlined dense style="min-width: 205px" label="Summary"/>
        </q-card-section>
        <q-card-section class="row">
          <q-input v-model="task.description" type="text" disabled="true" outlined dense style="min-width: 205px" label="Description"/>
        </q-card-section>
        <q-card-section class="row">
          <q-input v-model="task.component" type="text" disabled="true" outlined dense style="min-width: 205px" label="Component"/>
        </q-card-section>
        <q-card-section class="row">
          <q-select v-model="task.stage" :options="releaseStagesStore.stages" label="Stage" outlined dense style="min-width: 205px"/>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" v-close-popup />
          <q-btn flat label="Create" class="text-primary" @click="createTask"/>
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { notifyError } from 'src/utils/alerts'
import { useEnvStore } from 'stores/envStore'
import { useReleaseStageStore } from 'src/stores/releaseStageStore'
import { useReleaseStore } from 'stores/releaseStore'
import { useTaskStore } from 'stores/taskStore'

// Stores
const envStore = useEnvStore()
const releaseStagesStore = useReleaseStageStore()
const releaseStore = useReleaseStore()
const taskStore = useTaskStore()

// Reactive data
const showReleaseAdd = ref(false)
const release = ref({
  releaseNumber: ''
})

const showAddTaskDialog = ref(false)
const task = ref({
  releaseId: null,
  stage: '',
  summary: '',
  description: '',
  component: ''
})

const showUpdateStatusDialog = ref(false)
const update = ref({
  type: '',
  id: null,
  env: '',
  envId: null,
  status: '',
  releaseId: null
})

const releaseColumns = ref([
  {
    name: 'expand',
    field: 'expand'
  },
  {
    name: 'release',
    label: 'Release',
    field: 'releaseNumber',
    align: 'left'
  },
  {
    name: 'actions',
    label: 'Actions',
    align: 'left'
  }
])

const taskColumns = ref([
  {
    name: 'stage',
    label: 'Stage',
    field: 'stage',
    align: 'left'
  },
  {
    name: 'component',
    label: 'Component',
    field: 'component',
    align: 'left'
  },
  {
    name: 'summary',
    label: 'Summary',
    field: 'summary',
    align: 'left'
  },
  {
    name: 'description',
    label: 'Description',
    field: 'description',
    align: 'left'
  },
  {
    name: 'actions',
    label: 'Actions',
    align: 'left'
  }
])

// Constant data
const statusOptions = ['PENDING', 'COMPLETE', 'NOT_REQUIRED']
const taskListingPagination = {
  rowsPerPage: 200
}

// Methods
function envColumns () {
  return envStore.getActiveEnvs.map((env) => {
    return {
      name: `${env.name}Status`,
      label: env.name,
      align: 'center'
    }
  })
}

function createRelease () {
  if (release.value.releaseNumber === '') {
    notifyError('Release Number is required')
    return
  }

  releaseStore.create(release.value).then(() => {
    showReleaseAdd.value = false
    release.value.releaseNumber = ''
  })
}

function handleExpansion (props) {
  props.expand = !props.expand

  if (props.expand) {
    loadTasksForRelease(props.row.id)
  }
}

function isInStatus (env, release, status) {
  const envId = envStore.getEnvIdForName(env)
  return release.environmentStatus[envId].status === status
}

function startStatusUpdate (type, row, env) {
  update.value.type = type
  update.value.env = env
  update.value.envId = envStore.getEnvIdForName(env)
  update.value.id = row.environmentStatus[update.value.envId].id
  update.value.status = row.environmentStatus[update.value.envId].status
  update.value.releaseId = row.releaseId
  showUpdateStatusDialog.value = true
}

function updateStatus () {
  if (update.value.status === '') {
    notifyError('Status is required')
    return
  }

  const updateType = update.value.type

  if (updateType === 'RELEASE') {
    releaseStore.updateStatus(update.value.id, update.value.status).then(resetUpdateStatusDialog)
  } else {
    taskStore.updateStatus(update.value.id, update.value.status).then(() => {
      taskStore.load(update.value.releaseId)
      releaseStore.load()
      resetUpdateStatusDialog()
    })
  }
}

function resetUpdateStatusDialog () {
  update.value.type = ''
  update.value.id = null
  update.value.env = ''
  update.value.envId = null
  update.value.status = ''
  update.value.releaseId = null
  showUpdateStatusDialog.value = false
}

function startAddTask (release) {
  task.value.releaseId = release.id
  showAddTaskDialog.value = true
}

function createTask () {
  if (task.value.stage === '') {
    notifyError('Stage is required')
    return
  }

  if (task.value.summary === '') {
    notifyError('Summary is required')
    return
  }

  if (task.value.component === '') {
    notifyError('Component is required')
    return
  }

  taskStore.create(task.value).then(() => {
    task.value.releaseId = null
    task.value.summary = ''
    task.value.description = ''
    task.value.component = ''
    task.value.stage = ''

    showAddTaskDialog.value = false
  })
}

function loadTasksForRelease (releaseId) {
  taskStore.load(releaseId)
}

function deleteTask (task) {
  taskStore.deleteTask(task.id, task.releaseId)
}

function deleteRelease (release) {
  releaseStore.deleteRelease(release.id)
}

onMounted(() => {
  releaseStagesStore.load()
  envStore.load()
    .then(() => {
      const envCols = envColumns()
      releaseColumns.value.splice(releaseColumns.value.length - 1, 0, ...envCols)
      taskColumns.value.splice(taskColumns.value.length - 1, 0, ...envCols)

      releaseStore.load()
    })
})
</script>
