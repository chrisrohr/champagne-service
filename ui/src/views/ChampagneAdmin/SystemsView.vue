<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of deployable systems" :columns="systemColumns" :rows="systemRows" row-id="id" title="Systems" :loading="loading" :pagination="pagination" table-key="systems" @table-page="changePage" @table-page-size="changePageSize">
        <template #body-cell-createdAt="props">
          {{ fromNow(props.row.createdAt) }}
        </template>

        <template #body-cell-updatedAt="props">
          {{ fromNow(props.row.updatedAt) }}
        </template>

        <template #body-cell-devEnvironment="props">
          <span v-if="props.row.devEnvName">{{ props.row.devEnvName }}</span>
          <span v-else class="italic">Not Yet Set</span>
        </template>

        <template #body-cell-promotionOrder="props">
          <ol class="list-decimal" v-if="props.row.envOrder">
            <li v-for="env in props.row.envOrder" :key="env">{{ env }}</li>
          </ol>
          <span v-else class="italic">Not Yet Set</span>
        </template>

        <template #body-cell-users="props">
          <ul>
            <li v-for="user in props.row.users" :key="user.id">
              {{ user.displayName }} ({{ user.systemIdentifier }}) <i class="fas fa-star text-yellow-400" v-if="user.admin"></i>
            </li>
          </ul>
        </template>

        <template #body-cell-actions="props">
          <table-actions-dropdown :action-list="systemActions" :row="props.row"/>
        </template>
      </card-table>
    </div>

    <confirmation-prompt title="Hold up!" :message="deleteSystemConfirmationMessage" @cancelConfirm="deleteSystemConfirmationState = false" @acceptConfirm="deleteSystem" v-if="deleteSystemConfirmationState"/>

    <button class="fixed bottom-3 right-14 bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase text-xs w-10 h-10 p-0 rounded-full shadow hover:shadow-md outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="startCreate">
      <i class="fas fa-plus"></i>
    </button>

    <div v-if="showAddSystem" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Add new system
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Name" v-model="systemToCreate.name" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="systemToCreate.showNameError">
              Name is required!
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddSystem = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="createSystem()">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddSystem" class="opacity-25 fixed inset-0 z-40 bg-black"></div>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import ConfirmationPrompt from "@/components/Alerts/ConfirmationPrompt.vue";
import TableActionsDropdown from "@/components/Dropdowns/TableActionsDropdown.vue";
import {fromNow} from "@/utils/time";

const pageInfoStore = usePageInfoStore();

const deleteSystemConfirmationMessage = ref('');
const deleteSystemConfirmationState = ref(false);
const currentSystem = ref(null);

const loading = ref(false);
const pagination = ref({
  rowsPerPage: 10,
  page: 1,
  totalElements: 0
});

const showAddSystem = ref(false);
const systemToCreate = ref({
  name: '',
  showNameError: false
});

const systemRows = ref({});

const systemColumns = [
  {
    name: 'name',
    label: 'System Name',
    field: 'name'
  },
  {
    name: 'createdAt',
    label: 'Created',
  },
  {
    name: 'updatedAt',
    label: 'Updated',
  },
  {
    name: 'devEnvironment',
    label: 'Development Environment',
    field: 'devEnvName'
  },
  {
    name: 'promotionOrder',
    label: 'Env Promotion Order'
  },
  {
    name: 'users',
    label: 'Users'
  },
  {
    name: 'actions'
  }
];

const systemActions = [
  {
    label: 'Remove',
    icon: 'fa-trash',
    onClick: confirmDeleteSystem
  }
];

function changePage(tableKey, pageNum) {
  pagination.value.page = pageNum;
  loadSystems();
}

function changePageSize(tableKey, pageSize) {
  pagination.value.rowsPerPage = pageSize;
  loadSystems();
}

function loadSystems() {
  return api.get('/systems/admin', { params: { pageNumber: pagination.value.page, pageSize: pagination.value.rowsPerPage }})
      .then(response => {
        const data = response.data;
        systemRows.value = data.content;
        pagination.value.totalElements = data.totalElements;
        loading.value = false;
      });
}

function confirmDeleteSystem(system) {
  deleteSystemConfirmationMessage.value = `Are you sure you want to delete system ${system.name}?`;
  currentSystem.value = system;
  deleteSystemConfirmationState.value = true;
}

function deleteSystem() {
  deleteSystemConfirmationState.value = false;
  api.delete(`/systems/${currentSystem.value.id}`)
      .then(loadSystems);
}

function startCreate() {
  systemToCreate.value.name = '';
  systemToCreate.value.showNameError = false;
  showAddSystem.value = true;
}

function createSystem() {
  if (systemToCreate.value.name === '') {
    systemToCreate.value.showNameError = true;
    return;
  }

  showAddSystem.value = false;
  api.post('/systems', { name: systemToCreate.value.name })
      .then(loadSystems);
}

onMounted(() => {
  pageInfoStore.setPageTitle('Deployable Systems');

  loadSystems();
});
</script>

<style>
@import '@vueform/multiselect/themes/tailwind.css';
</style>
