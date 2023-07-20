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
              <span v-for="tag in props.row.tags" :key="tag" class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-blue-600 bg-blue-200 last:mr-0 mr-1">
                {{ tag }}
              </span>
            </td>
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              {{ props.row.source }}
            </td>
            <td class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <button type="button" v-if="currentUserStore.isDeployableSystemAdmin" @click="confirmDeleteHost(props.row)" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
                <i class="fas fa-trash"></i>
              </button>
            </td>
          </tr>
          <tr class="bg-gray-200" v-if="props.row.expand">
            <td colspan="6" class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              <card-table caption="List of services on host" :columns="componentColumns" :rows="componentRows[props.row.id]" row-id="id">
                <template #body-cell-currentVersion>
                  Coming Soon
                </template>

                <template #body-cell-actions="props">
                  <button type="button" v-if="currentUserStore.isDeployableSystemAdmin" @click="confirmDeleteComponent(props.row)" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
                    <i class="fas fa-trash"></i>
                  </button>
                </template>
              </card-table>
            </td>
          </tr>
        </template>
      </card-table>
    </div>

    <confirmation-prompt title="Hold up!" :message="deleteHostConfirmationMessage" @cancelConfirm="deleteHostConfirmationState = false" @acceptConfirm="deleteHost" v-if="deleteHostConfirmationState"/>
    <confirmation-prompt title="Hold up!" :message="deleteComponentConfirmationMessage" @cancelConfirm="deleteComponentConfirmationState = false" @acceptConfirm="deleteComponent" v-if="deleteComponentConfirmationState"/>

    <button v-if="currentUserStore.isDeployableSystemAdmin" ref="btnDropdownRef" class="fixed bottom-3 right-14 bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase text-xs w-10 h-10 p-0 rounded-full shadow hover:shadow-md outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="toggleActions">
      <i class="fas fa-angle-up"></i>
    </button>
    <div ref="popoverDropdownRef" class="fixed bottom-14 right-14 bg-transparent z-50 py-2 mb-2 text-right min-w-48" v-bind:class="{hidden: !dropdownPopoverShow, block: dropdownPopoverShow}">
      <div class="w-full justify-end">
        <button type="button" class="bg-emerald-500 text-white font-bold text-xs rounded-full px-4 py-2 mb-2" @click="startCreateHost">
          <i class="fas fa-plus"></i> New Host
        </button>
      </div>
      <div class="w-full justify-end">
        <button type="button" class="bg-emerald-500 text-white font-bold text-xs rounded-full px-4 py-2 mb-2" @click="startCreateComponent">
          <i class="fas fa-plus"></i> New Component
        </button>
      </div>
    </div>

    <div v-if="showAddHost" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Add new host
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
              <input type="text" placeholder="Tags - Press enter to add" @keyup.enter="addTag" v-model="hostToCreate.tag" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
              <div class="pb-1 pt-1">
                <span v-for="tag in hostToCreate.tags" :key="tag" class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-emerald-600 bg-emerald-200  last:mr-0 mr-1">
                  {{ tag }} <button type="button" @click="removeTag(tag)"><i class="fas fa-close text-white"></i></button>
                </span>
              </div>
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddHost = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="createHost">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddHost" class="opacity-25 fixed inset-0 z-40 bg-black"></div>

    <div v-if="showAddComponent" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Add new component
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Name" v-model="componentToCreate.name" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="componentToCreate.showNameError">
              Name is required!
            </div>
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Tag" v-model="componentToCreate.tag" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="componentToCreate.showTagError">
              Tag is required!
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddComponent = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="createComponent">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddComponent" class="opacity-25 fixed inset-0 z-40 bg-black"></div>
  </div>
</template>

<script setup>
import {onMounted, ref, watch} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import {useCurrentUserStore} from "@/stores/currentUser";
import {useEnvironmentStore} from "@/stores/environments";
import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import ConfirmationPrompt from "@/components/Alerts/ConfirmationPrompt.vue";
import {createPopper} from "@popperjs/core";
import _ from "lodash";

const dropdownPopoverShow = ref(false);
const popoverDropdownRef = ref(null);
const btnDropdownRef = ref(null);

const pageInfoStore = usePageInfoStore();
const environmentStore = useEnvironmentStore();
const currentUserStore = useCurrentUserStore();

const deleteHostConfirmationMessage = ref('');
const deleteHostConfirmationState = ref(false);
const currentHost = ref(null);

const deleteComponentConfirmationMessage = ref('');
const deleteComponentConfirmationState = ref(false);
const currentComponent = ref(null);

const loading = ref(false);
const selectedEnv = ref(currentUserStore.activeDeployableSystem.devEnvironmentId);
watch(selectedEnv, loadHosts);

const showAddHost = ref(false);
const hostToCreate = ref({
  hostname: '',
  tag: '',
  tags: [],
  showHostnameError: false
});

const showAddComponent = ref(false);
const componentToCreate = ref({
  name: '',
  tag: '',
  showNameError: false,
  showTagError: false
});

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
    name: 'actions',
    label: 'Actions'
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
  },
  {
    name: 'actions',
    label: 'Actions'
  }
];

function toggleActions() {
  dropdownPopoverShow.value = !dropdownPopoverShow.value;

  if (dropdownPopoverShow.value) {
    createPopper(btnDropdownRef, popoverDropdownRef, {
      placement: 'top'
    });
  }
}

function loadHosts() {
  loading.value = true;
  api.get(`/host/${selectedEnv.value}`)
      .then(response => {
        hostRows.value = response.data;
        loading.value = false;
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

function confirmDeleteComponent(component) {
  deleteComponentConfirmationMessage.value = `Are you sure you want to delete component ${component.componentName}?`;
  currentComponent.value = component;
  deleteComponentConfirmationState.value = true;
}

function deleteComponent() {
  deleteComponentConfirmationState.value = false;
  api.delete(`/host/component/${currentComponent.value.id}`)
      .then(() => {
        expandedHosts.value.forEach(hostId => {
          loadComponentsForHost(hostId);
        });
      });
}

function addTag() {
  hostToCreate.value.tags.push(hostToCreate.value.tag);
  hostToCreate.value.tag = '';
}

function removeTag(tag) {
  _.pull(hostToCreate.value.tags, tag);
}

function startCreateHost() {
  hostToCreate.value.hostname = '';
  hostToCreate.value.tag = '';
  hostToCreate.value.tags = [];
  hostToCreate.value.showHostnameError = false;
  showAddHost.value = true;
  dropdownPopoverShow.value = false;
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

function startCreateComponent() {
  componentToCreate.value.name = '';
  componentToCreate.value.tag = '';
  componentToCreate.value.showNameError = false;
  componentToCreate.value.showTagError = false;
  showAddComponent.value = true;
  dropdownPopoverShow.value = false;
}

function createComponent() {
  if (componentToCreate.value.name === '') {
    componentToCreate.value.showNameError = true;
  }

  if (componentToCreate.value.tag === '') {
    componentToCreate.value.showTagError = true;
  }

  if (componentToCreate.value.name !== '' && componentToCreate.value.tag !== '') {
    showAddComponent.value = false;
    api.post('/host/component', {
      componentName: componentToCreate.value.name,
      tag: componentToCreate.value.tag
    })
        .then(() => {
          expandedHosts.value.forEach(hostId => {
            loadComponentsForHost(hostId);
          })
        });
  }
}

onMounted(() => {
  pageInfoStore.setPageTitle('Host Config');

  loadHosts();
});
</script>
