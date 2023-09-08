<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of manual deployment tasks" :columns="releaseColumns" :rows="releaseRows" row-id="id" title="Manual Tasks" :loading="releaseLoading" :pagination="releasePagination" table-key="releases" @table-page="changePage" @table-page-size="changePageSize">
        <template #body="props">
          <tr class="hover:bg-gray-200">
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <button type="button" @click="toggleExpand(props.row)" class="border-0 bg-transparent text-xs text-emerald-500">
                <i class="fas" :class="props.row.expand ? 'fa-caret-down' : 'fa-caret-right'"></i>
              </button>
            </td>
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              {{ props.row.releaseNumber }}
            </td>
            <td v-for="env in envColumns()" :key="env.name" class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <row-status-indicator :row-id="props.row.environmentStatus[env.id].id" :sub-id="env.id" :status="props.row.environmentStatus[env.id].status" :status-options="releaseStatusOptions" @row-status-change="startReleaseStatusUpdate"/>
            </td>
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <table-actions-dropdown :action-list="releaseActions" :row="props.row"/>
            </td>
          </tr>
          <tr class="bg-gray-200" v-if="props.row.expand">
            <td colspan="6" class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <card-table caption="List of tasks in release" :columns="taskColumns" :rows="taskRows[props.row.id] || []" row-id="id" :loading="taskLoading[props.row.id] || false">
                <template #body="props">
                  <tr class="hover:bg-gray-200">
                    <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                      {{ props.row.stage }}
                    </td>
                    <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                      {{ props.row.component }}
                    </td>
                    <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                      {{ props.row.summary }}
                    </td>
                    <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                      {{ props.row.description }}
                    </td>
                    <td v-for="env in envColumns()" :key="env.name" class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                      <row-status-indicator :row-id="props.row.environmentStatus[env.id].id" :sub-id="env.id" :status="props.row.environmentStatus[env.id].status" :status-options="releaseStatusOptions" :row-data="props.row" @row-status-change="startTaskStatusUpdate"/>
                    </td>
                    <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                      <table-actions-dropdown :action-list="taskActions" :row="props.row"/>
                    </td>
                  </tr>
                </template>
              </card-table>
            </td>
          </tr>
        </template>
      </card-table>
    </div>

    <div v-if="showReleaseStatusDialog" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Update Release Status
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" disabled="disabled" v-model="releaseStatusUpdate.env" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0">
              <Multiselect v-model="releaseStatusUpdate.status" :options="Object.keys(releaseStatusOptions)"/>
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showReleaseStatusDialog = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="updateReleaseStatus">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showReleaseStatusDialog" class="opacity-25 fixed inset-0 z-40 bg-black"></div>

    <confirmation-prompt title="Hold up!" :message="deleteReleaseConfirmationMessage" @cancelConfirm="deleteReleaseConfirmationState = false" @acceptConfirm="deleteRelease" v-if="deleteReleaseConfirmationState"/>

    <button class="fixed bottom-3 right-14 bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase text-xs w-10 h-10 p-0 rounded-full shadow hover:shadow-md outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="startCreateRelease">
      <i class="fas fa-plus"></i>
    </button>

    <div v-if="showAddRelease" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Add Manual Deployment Release
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Release Number" v-model="release.releaseNumber" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="release.showReleaseNumberError">
              Release Number is required!
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddRelease = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="createRelease">
              Create
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddRelease" class="opacity-25 fixed inset-0 z-40 bg-black"></div>

    <div v-if="showTaskStatusDialog" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Update Task Status
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" disabled="disabled" v-model="taskStatusUpdate.env" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0">
              <Multiselect v-model="taskStatusUpdate.status" :options="Object.keys(releaseStatusOptions)"/>
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showTaskStatusDialog = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="updateTaskStatus">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showTaskStatusDialog" class="opacity-25 fixed inset-0 z-40 bg-black"></div>

    <div v-if="showAddTask" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Add Manual Deployment Task
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Summary" v-model="task.summary" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="task.showTaskSummaryError">
              Task summary is required!
            </div>
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Description" v-model="task.description" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Component" v-model="task.component" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="task.showTaskComponentError">
              Task component is required!
            </div>
            <div class="mb-3 pt-0">
              <Multiselect v-model="task.stage" :options="taskStages" placeholder="Deployment Stage"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="task.showTaskStageError">
              Task stage is required!
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddTask = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="createTask">
              Create
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddTask" class="opacity-25 fixed inset-0 z-40 bg-black"></div>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import {useEnvironmentStore} from "@/stores/environments";
import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import ConfirmationPrompt from "@/components/Alerts/ConfirmationPrompt.vue";
import RowStatusIndicator from "@/components/Tables/RowStatusIndicator.vue";
import TableActionsDropdown from "@/components/Dropdowns/TableActionsDropdown.vue";
import Multiselect from "@vueform/multiselect";
import _ from "lodash";

const pageInfoStore = usePageInfoStore();
const envStore = useEnvironmentStore();

// Release related data
const deleteReleaseConfirmationMessage = ref('');
const deleteReleaseConfirmationState = ref(false);
const currentRelease = ref(null);

const releaseLoading = ref(false);

const showAddRelease = ref(false);
const release = ref({
  releaseNumber: '',
  showReleaseNumberError: false
});

const releasePagination = ref({
  rowsPerPage: 10,
  page: 1,
  totalElements: 0
});

const showReleaseStatusDialog = ref(false);
const releaseStatusUpdate = ref({
  id: null,
  env: '',
  envId: null,
  status: ''
});

const releaseStatusOptions = {
  'PENDING': {
    color: 'text-red-500',
    icon: 'fa-ellipsis'
  },
  'COMPLETE': {
    color: 'text-lime-300',
    icon: 'fa-check'
  },
  'NOT_REQUIRED': {
    color: 'text-gray-500',
    icon: 'fa-minus'
  }
};

const releaseRows = ref([]);
const releaseColumns = ref([
  {
    name: 'expand',
    field: 'expand'
  },
  {
    name: 'release',
    label: 'Release',
    field: 'releaseNumber'
  },
  {
    name: 'actions'
  }
]);

const releaseActions = [
  {
    label: 'Create Task',
    icon: 'fa-calendar-plus',
    onClick: startAddTask
  },
  {
    label: 'Delete Release',
    icon: 'fa-trash',
    onClick: confirmDeleteRelease
  }
];

const expandedReleases = ref([]);

// Release related functions
async function loadReleases() {
  return api.get(`/manual/deployment/tasks/releases`, { params: { pageNumber: releasePagination.value.page, pageSize: releasePagination.value.rowsPerPage }})
      .then(response => {
        const data = response.data;
        releaseRows.value = data.content;
        releasePagination.value.totalElements = data.totalElements;
        releaseLoading.value = false;

        expandedReleases.value.forEach(releaseId => {
          const release = _.find(releaseRows.value, release => release.id === releaseId);
          release.expand = true;
          loadTasksForRelease(releaseId);
        });
      });
}

function startReleaseStatusUpdate(releaseStatusId, envId, currentStatus) {
  releaseStatusUpdate.value.id = releaseStatusId;
  releaseStatusUpdate.value.status = currentStatus;
  releaseStatusUpdate.value.envId = envId;
  releaseStatusUpdate.value.env = envColumns().filter(env => env.id === envId)[0].name;
  showReleaseStatusDialog.value = true;
}

function updateReleaseStatus() {
  showReleaseStatusDialog.value = false;
  api.put(`/manual/deployment/tasks/releases/${releaseStatusUpdate.value.id}/${releaseStatusUpdate.value.status}`)
      .then(loadReleases);
}

function startCreateRelease() {
  release.value.releaseNumber = '';
  release.value.showReleaseNumberError = false;
  showAddRelease.value = true;
}

function createRelease() {
  if (release.value.releaseNumber === '') {
    release.value.showReleaseNumberError = true;
    return;
  }

  showAddRelease.value = false;
  api.post(`/manual/deployment/tasks/releases`, { releaseNumber: release.value.releaseNumber })
      .then(loadReleases);
}

function confirmDeleteRelease(release) {
  deleteReleaseConfirmationMessage.value = `Are you sure you want to delete release ${release.releaseNumber}?`;
  currentRelease.value = release;
  deleteReleaseConfirmationState.value = true;
}

function deleteRelease() {
  deleteReleaseConfirmationState.value = false;
  api.delete(`/manual/deployment/tasks/releases/${currentRelease.value.id}`)
      .then(() => {
        loadReleases();
      });
}

async function toggleExpand(row) {
  if (!row.expand) {
    await loadTasksForRelease(row.id);
    expandedReleases.value.push(row.id);
  } else {
    _.pull(expandedReleases.value, row.id);
  }

  row.expand = !row.expand;
}

// Task related data
const taskColumns = ref([
  {
    name: 'stage',
    label: 'Stage'
  },
  {
    name: 'component',
    label: 'Component'
  },
  {
    name: 'summary',
    label: 'Summary'
  },
  {
    name: 'description',
    label: 'Description'
  },
  {
    name: 'actions'
  }
]);

const taskRows = ref({});
const taskLoading = ref({});

const taskActions = [
  {
    label: 'Delete Task',
    icon: 'fa-trash',
    onClick: deleteTask
  }
];

const showTaskStatusDialog = ref(false);
const taskStatusUpdate = ref({
  id: null,
  env: '',
  envId: null,
  status: '',
  releaseId: null
});

const showAddTask = ref(false);
const task = ref({
  releaseId: null,
  stage: '',
  showTaskStageError: false,
  summary: '',
  showTaskSummaryError: false,
  description: '',
  component: '',
  showTaskComponentError: false
});

const taskStages = ref([]);

// Task related functions
function loadTasksForRelease(releaseId) {
  taskLoading.value[releaseId] = true;
  api.get(`/manual/deployment/tasks/releases/${releaseId}`)
      .then(response => {
        taskLoading.value[releaseId] = false;
        taskRows.value[releaseId] = response.data;
      });
}

function startAddTask(release) {
  task.value.releaseId = release.id;
  showAddTask.value = true;
}

function createTask() {
  task.value.showTaskStageError = false;
  task.value.showTaskSummaryError = false;
  task.value.showTaskComponentError = false;

  let missingFields = false;
  if (task.value.stage === '') {
    task.value.showTaskStageError = true;
    missingFields = true;
  }

  if (task.value.summary === '') {
    task.value.showTaskSummaryError = true;
    missingFields = true;
  }

  if (task.value.component === '') {
    task.value.showTaskComponentError = true;
    missingFields = true;
  }

  if (missingFields) {
    return;
  }

  showAddTask.value = false;
  api.post('/manual/deployment/tasks', {
    releaseId: task.value.releaseId,
    summary: task.value.summary,
    description: task.value.description,
    component: task.value.component,
    stage: task.value.stage
  }).then(() => {
    task.value.releaseId = null;
    task.value.summary = '';
    task.value.showTaskSummaryError = false;
    task.value.description = '';
    task.value.component = '';
    task.value.showTaskComponentError = false;
    task.value.stage = '';
    task.value.showTaskStageError = false;
    loadReleases();
  });
}

function deleteTask(task) {
  api.delete(`/manual/deployment/tasks/${task.id}`)
      .then(() => {
        loadTasksForRelease(task.releaseId);
      });
}

function startTaskStatusUpdate(taskStatusId, envId, currentStatus, taskData) {
  taskStatusUpdate.value.id = taskStatusId;
  taskStatusUpdate.value.status = currentStatus;
  taskStatusUpdate.value.envId = envId;
  taskStatusUpdate.value.env = envColumns().filter(env => env.id === envId)[0].name;
  taskStatusUpdate.value.releaseId = taskData.releaseId;
  showTaskStatusDialog.value = true;
}

function updateTaskStatus() {
  showTaskStatusDialog.value = false;
  api.put(`/manual/deployment/tasks/${taskStatusUpdate.value.id}/${taskStatusUpdate.value.status}`)
      .then(() => {
        loadReleases();
      });
}

function loadTaskStages() {
  api.get('/manual/deployment/tasks/stages')
      .then(response => {
        taskStages.value = response.data;
      });
}

// Full page functions
function envColumns() {
  return envStore.environmentsForSystem.filter(env => !env.deleted);
}

function changePage(tableKey, pageNum) {
  if (tableKey === 'releases') {
    releasePagination.value.page = pageNum;
    loadReleases();
  }
}

function changePageSize(tableKey, pageSize) {
  if (tableKey === 'releases') {
    releasePagination.value.rowsPerPage = pageSize;
    loadReleases();
  }
}

onMounted(() => {
  pageInfoStore.setPageTitle('Manual Tasks');
  loadTaskStages();
  envStore.loadEnvironments().then(() => {
    const envs = envColumns().map(env => {
      return {
        name: `${env.name}Status`,
        label: env.name
      }
    });

    releaseColumns.value.splice(releaseColumns.value.length - 1, 0, ...envs);
    taskColumns.value.splice(taskColumns.value.length - 1, 0, ...envs);

    loadReleases();
  });
});
</script>

<style>
@import '@vueform/multiselect/themes/tailwind.css';
</style>
