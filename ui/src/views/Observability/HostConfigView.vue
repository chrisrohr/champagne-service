<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of configured Hosts" :columns="hostColumns" :rows="hostRows" row-id="id">
        <template #header>
          <h3 class="font-semibold text-lg text-gray-700">
            Hosts
            <i class="far fa-refresh fa-spin" v-if="loading"></i>
            <select v-model="selectedEnv" class="float-right w-150 py-1 pl-5 pr-10 mb-3 text-xs text-gray-500 bg-white border border-gray-200 appearance-none peer focus:outline-none focus:ring-0 focus:border-gray-200">
              <option v-for="env in environmentStore.environmentsForSystem" :key="env.id" :value="env.id">
                {{ env.name }}
              </option>
            </select>
            <span class="float-right text-xs uppercase text-gray-500 mr-3 pt-1.5">
              Environment:
            </span>
            <button type="button" @click="toggleFilter" class="float-right text-xs text-gray-500 bg-transparent border border-solid rounded border-gray-500 px-2 py-1 mr-2">
              <i class="fas fa-filter"></i>
            </button>
            <span v-if="componentFilter !== ''" class="float-right mr-5 text-xs">
              Filters:
              <span class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-blue-600 bg-blue-200 last:mr-0 mr-1">
                Component: {{ componentFilter }}
              </span>
            </span>
          </h3>
        </template>
        <template #body="props">
          <tr class="hover:bg-gray-200">
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <button type="button" @click="toggleExpand(props.row)" class="border-0 bg-transparent text-xs text-emerald-500">
                <i class="fas" :class="props.row.expand ? 'fa-caret-down' : 'fa-caret-right'"></i>
              </button>
            </td>
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              {{ props.row.hostname }}
            </td>
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              Info coming soon
            </td>
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <span v-for="tag in props.row.tags" :key="tag.id" class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-blue-600 bg-blue-200 last:mr-0 mr-1">
                {{ tag.name }}
              </span>
            </td>
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              {{ props.row.source }}
            </td>
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <table-actions-dropdown :action-list="hostActions" :row="props.row" v-if="hostActions.length > 0"/>
            </td>
          </tr>
          <tr class="bg-gray-200" v-if="props.row.expand">
            <td colspan="6" class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <card-table caption="List of services on host" :columns="componentColumns" :rows="componentRows[props.row.id]" row-id="id">
                <template #body-cell-currentVersion>
                  Coming Soon
                </template>

                <template #body-cell-tag="props">
                  <span v-if="props.row.tag !== null" class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-blue-600 bg-blue-200 last:mr-0 mr-1">
                    {{ props.row.tag?.name }}
                  </span>
                </template>
              </card-table>
            </td>
          </tr>
        </template>
      </card-table>
    </div>

    <confirmation-prompt title="Hold up!" :message="deleteHostConfirmationMessage" @cancelConfirm="deleteHostConfirmationState = false" @acceptConfirm="deleteHost" v-if="deleteHostConfirmationState"/>

    <button v-if="currentUserStore.isDeployableSystemAdmin" class="fixed bottom-3 right-14 bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase text-xs w-10 h-10 p-0 rounded-full shadow hover:shadow-md outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="startCreate">
      <i class="fas fa-plus"></i>
    </button>

    <div v-if="showAddHost" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              <span v-if="hostToCreate.id === null">Add new host</span>
              <span v-else>Update host</span>
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Hostname" v-model="hostToCreate.hostname" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="hostToCreate.showHostnameError">
              Hostname is required!
            </div>
            <div class="mb-3 pt-0">
              <Multiselect v-model="hostToCreate.tags" :options="allTags" mode="multiple"/>
              <div class="pb-1 pt-1">
                <span v-for="tag in hostToCreate.tags" :key="tag.id" class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-emerald-600 bg-emerald-200  last:mr-0 mr-1">
                  {{ tag.name }} <button type="button" @click="removeTag(tag)"><i class="fas fa-close text-white"></i></button>
                </span>
              </div>
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddHost = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="hostToCreate.id === null ? createHost() : updateHost()">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddHost" class="opacity-25 fixed inset-0 z-40 bg-black"></div>

    <div v-if="filterShow" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Filters
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Component Name" v-model="componentFilter" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button v-if="componentFilter !== ''" class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="componentFilter = ''; filterShow = false; loadHosts()">
              Clear
            </button>
            <button class="text-red-500 border border-emerald-500 rounded bg-transparent font-bold uppercase px-6 py-3 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="filterShow = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="filterShow = false; loadHosts()">
              Apply
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="filterShow" class="opacity-25 fixed inset-0 z-40 bg-black"></div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from "vue";

import {usePageInfoStore} from "@/stores/pageInfo";
import {useCurrentUserStore} from "@/stores/currentUser";
import {useEnvironmentStore} from "@/stores/environments";

import CardTable from "@/components/Cards/CardTable.vue";
import ConfirmationPrompt from "@/components/Alerts/ConfirmationPrompt.vue";
import TableActionsDropdown from "@/components/Dropdowns/TableActionsDropdown.vue";

import Multiselect from "@vueform/multiselect";

import {api} from "@/plugins/axios";
import _ from "lodash";

const pageInfoStore = usePageInfoStore();
const environmentStore = useEnvironmentStore();
const currentUserStore = useCurrentUserStore();

const deleteHostConfirmationMessage = ref('');
const deleteHostConfirmationState = ref(false);
const currentHost = ref(null);

const loading = ref(false);
const selectedEnv = ref(currentUserStore.activeDeployableSystem.devEnvironmentId);
watch(selectedEnv, loadHosts);

const filterShow = ref(false);
const componentFilter = ref('');

const showAddHost = ref(false);
const hostToCreate = ref({
  id: null,
  hostname: '',
  tags: [],
  showHostnameError: false
});

const allTags = ref([]);

const expandedHosts = ref([]);

const hostRows = ref([]);
const componentRows = ref({});

const hostColumns = [
  {
    name: 'expand',
    field: 'expand'
  },
  {
    name: 'hostname',
    label: 'Hostname',
    field: 'hostname'
  },
  {
    name: 'info',
    label: 'Info',
    field: 'info'
  },
  {
    name: 'tags',
    label: 'Tags',
    field: 'tags'
  },
  {
    name: 'source',
    label: 'Source',
    field: 'source'
  },
  {
    name: 'actions'
  }
];

const hostActions = computed(() => allHostActions.filter(action => action.permission === undefined || action.permission()));

const allHostActions = [
  {
    label: 'Edit',
    icon: 'fa-edit',
    onClick: startUpdate,
    permission: () => {
      return currentUserStore.isDeployableSystemAdmin;
    }
  },
  {
    label: 'Remove',
    icon: 'fa-trash',
    onClick: confirmDeleteHost,
    permission: () => {
      return currentUserStore.isDeployableSystemAdmin;
    }
  }
];

const componentColumns = [
  {
    name: 'componentName',
    label: 'Component Name',
    field: 'componentName'
  },
  {
    name: 'currentVersion',
    label: 'Current Version'
  },
  {
    name: 'tag',
    label: 'Tag',
    field: 'tag'
  }
];

function loadHosts() {
  loading.value = true;
  api.get(`/host/${selectedEnv.value}`, {
    params: {
      componentFilter: componentFilter.value
    }
  })
      .then(response => {
        hostRows.value = response.data;
        loading.value = false;
        expandedHosts.value.forEach(hostId => {
          loadComponentsForHost(hostId);
        });
      });
}

async function toggleExpand(row) {
  if (!row.expand) {
    await loadComponentsForHost(row.id);
    expandedHosts.value.push(row.id);
  } else {
    _.pull(expandedHosts.value, row.id);
  }

  row.expand = !row.expand;
}

function loadComponentsForHost(hostId) {
  return api.get(`/host/${hostId}/components`)
      .then(response => {
        componentRows.value[hostId] = response.data;
      });
}

function confirmDeleteHost(host) {
  deleteHostConfirmationMessage.value = `Are you sure you want to delete host ${host.hostname}?`;
  currentHost.value = host;
  deleteHostConfirmationState.value = true;
}

function deleteHost() {
  deleteHostConfirmationState.value = false;
  api.delete(`/host/${currentHost.value.id}`)
      .then(() => loadHosts());
}

function removeTag(tag) {
  _.pull(hostToCreate.value.tags, tag);
}

function startCreate() {
  hostToCreate.value.id = null;
  hostToCreate.value.hostname = '';
  hostToCreate.value.tags = [];
  hostToCreate.value.showHostnameError = false;
  showAddHost.value = true;
  loadAllTags();
}

async function startUpdate(host) {
  await loadAllTags();

  hostToCreate.value.id = host.id;
  hostToCreate.value.hostname = host.hostname;
  hostToCreate.value.tags = host.tags;
  hostToCreate.value.showHostnameError = false;
  showAddHost.value = true;
}

function createHost() {
  if (hostToCreate.value.hostname === '') {
    hostToCreate.value.showHostnameError = true;
  } else {
    showAddHost.value = false;
    api.post('/host', {
      hostname: hostToCreate.value.hostname,
      tags: hostToCreate.value.tags,
      environmentId: selectedEnv.value,
      source: 'CHAMPAGNE'
    })
        .then(() => loadHosts());
  }
}

function updateHost() {
  if (hostToCreate.value.hostname === '') {
    hostToCreate.value.showHostnameError = true;
  } else {
    showAddHost.value = false;
    api.put(`/host/${hostToCreate.value.id}`, {
      hostname: hostToCreate.value.hostname,
      tags: hostToCreate.value.tags,
    })
        .then(() => loadHosts());
  }
}

function toggleFilter() {
  filterShow.value = !filterShow.value;
}

async function loadAllTags() {
  const tagResponse = await api.get('/tag');
  allTags.value = tagResponse.data.map(tag => { return { value: tag, label: tag.name } });
}

onMounted(() => {
  pageInfoStore.setPageTitle('Hosts');

  loadHosts();
});
</script>

<style>
  @import '@vueform/multiselect/themes/tailwind.css';
</style>
