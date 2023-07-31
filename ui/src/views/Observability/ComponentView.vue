<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of deployable components" :columns="componentColumns" :rows="componentRows" row-id="id" title="Components" :loading="loading">
        <template #body-cell-currentVersion>
          Coming Soon
        </template>

        <template #body-cell-tag="props">
          <span v-if="props.row.tag !== null" class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-blue-600 bg-blue-200 last:mr-0 mr-1">
            {{ props.row.tag?.name }}
          </span>
        </template>

        <template #body-cell-actions="props">
          <button type="button" v-if="currentUserStore.isDeployableSystemAdmin" @click="startUpdate(props.row)" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-edit"></i>
          </button>
          <button type="button" v-if="currentUserStore.isDeployableSystemAdmin" @click="confirmDeleteComponent(props.row)" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-trash"></i>
          </button>
        </template>
      </card-table>
    </div>

    <confirmation-prompt title="Hold up!" :message="deleteComponentConfirmationMessage" @cancelConfirm="deleteComponentConfirmationState = false" @acceptConfirm="deleteComponent" v-if="deleteComponentConfirmationState"/>

    <button v-if="currentUserStore.isDeployableSystemAdmin" class="fixed bottom-3 right-14 bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase text-xs w-10 h-10 p-0 rounded-full shadow hover:shadow-md outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="startCreate">
      <i class="fas fa-plus"></i>
    </button>

    <div v-if="showAddComponent" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              <span v-if="componentToCreate.id === null">Add new component</span>
              <span v-else>Update component</span>
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
              <Multiselect v-model="componentToCreate.tag" :options="allTags" value-prop="id" label="name"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="componentToCreate.showTagError">
              Tag is required!
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddComponent = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="componentToCreate.id === null ? createComponent() : updateComponent()">
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
import {onMounted, ref} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import {useCurrentUserStore} from "@/stores/currentUser";
import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import ConfirmationPrompt from "@/components/Alerts/ConfirmationPrompt.vue";
import Multiselect from "@vueform/multiselect";

const pageInfoStore = usePageInfoStore();
const currentUserStore = useCurrentUserStore();

const deleteComponentConfirmationMessage = ref('');
const deleteComponentConfirmationState = ref(false);
const currentComponent = ref(null);

const loading = ref(false);

const showAddComponent = ref(false);
const componentToCreate = ref({
  id: null,
  name: '',
  tag: null,
  showNameError: false,
  showTagError: false
});

const allTags = ref([]);

const componentRows = ref({});

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

function loadComponents() {
  return api.get(`/host/components`)
      .then(response => {
        componentRows.value = response.data;
      });
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
        loadComponents();
      });
}

function startCreate() {
  componentToCreate.value.id = null;
  componentToCreate.value.name = '';
  componentToCreate.value.tag = null;
  componentToCreate.value.showNameError = false;
  componentToCreate.value.showTagError = false;
  showAddComponent.value = true;
  loadAllTags();
}

async function startUpdate(component) {
  await loadAllTags();

  componentToCreate.value.id = component.id;
  componentToCreate.value.name = component.componentName;
  componentToCreate.value.tag = component.tagId;
  componentToCreate.value.showNameError = false;
  componentToCreate.value.showTagError = false;
  showAddComponent.value = true;
}

function createComponent() {
  if (componentToCreate.value.name === '') {
    componentToCreate.value.showNameError = true;
  }

  if (componentToCreate.value.tag === null) {
    componentToCreate.value.showTagError = true;
  }

  if (componentToCreate.value.name !== '' && componentToCreate.value.tag !== null) {
    showAddComponent.value = false;
    api.post('/host/component', {
      componentName: componentToCreate.value.name,
      tagId: componentToCreate.value.tag
    })
        .then(() => {
          loadComponents();
        });
  }
}

function updateComponent() {
  if (componentToCreate.value.name === '') {
    componentToCreate.value.showNameError = true;
  }

  if (componentToCreate.value.tag === null) {
    componentToCreate.value.showTagError = true;
  }

  if (componentToCreate.value.name !== '' && componentToCreate.value.tag !== null) {
    showAddComponent.value = false;
    api.put(`/host/component/${componentToCreate.value.id}`, {
      componentName: componentToCreate.value.name,
      tagId: componentToCreate.value.tag
    })
        .then(() => {
          loadComponents();
        });
  }
}

async function loadAllTags() {
  const tagResponse = await api.get('/tag');
  allTags.value = tagResponse.data;
}

onMounted(() => {
  pageInfoStore.setPageTitle('Components');

  loadComponents();
});
</script>

<style>
@import '@vueform/multiselect/themes/tailwind.css';
</style>
