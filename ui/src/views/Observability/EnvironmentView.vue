<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table title="Deployable Environments" caption="List of deployable environments for current system" :columns="envColumns" :rows="environmentStore.environmentsForSystem" :loading="environmentStore.loading" row-id="id">
        <template #body-cell-name="props">
          <span :class="envTestCase(props.row)">
            {{ props.row.name }}
          </span>
        </template>

        <template #body-cell-createdAt="props">
          <span :class="envTestCase(props.row)">
            {{ fromNow(props.row.createdAt) }}
          </span>
        </template>

        <template #body-cell-updatedAt="props">
          <span :class="envTestCase(props.row)">
            {{ fromNow(props.row.updatedAt) }}
          </span>
        </template>

        <template #body-cell-actions="props">
          <button type="button" v-if="!props.row.deleted && currentUserStore.isDeployableSystemAdmin" @click="deactivateEnv(props.row.id)" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-eye-slash"></i>
          </button>
          <button type="button" v-if="props.row.deleted && currentUserStore.isDeployableSystemAdmin" @click="activateEnv(props.row.id)" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-eye"></i>
          </button>
          <button type="button" v-if="currentUserStore.isDeployableSystemAdmin" @click="confirmDelete(props.row)" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-trash"></i>
          </button>
        </template>
      </card-table>
    </div>

    <div>{{ currentUserStore.activeDeployableSystem}}</div>

    <confirmation-prompt title="Hold up!" :message="confirmationMessage" @cancelConfirm="confirmationState = false" @acceptConfirm="deleteEnv" v-if="confirmationState"/>

    <button v-if="currentUserStore.isDeployableSystemAdmin" class="fixed bottom-3 right-14 bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase text-xs w-10 h-10 p-0 rounded-full shadow hover:shadow-md outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="startCreate">
      <i class="fas fa-plus"></i>
    </button>

    <div v-if="showAdd" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-xl font-semibold">
              Add new deployment environment
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Environment Name" v-model="envToCreate.name" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="envToCreate.showNameError">
              Name is required!
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAdd = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="createEnv">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAdd" class="opacity-25 fixed inset-0 z-40 bg-black"></div>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import {useEnvironmentStore} from "@/stores/environments";
import {useCurrentUserStore} from "@/stores/currentUser";
import CardTable from "@/components/Cards/CardTable.vue";
import ConfirmationPrompt from "@/components/Alerts/ConfirmationPrompt.vue";
import {fromNow} from "@/utils/time";
import {api} from "@/plugins/axios";

const pageInfoStore = usePageInfoStore();
const environmentStore = useEnvironmentStore();
const currentUserStore = useCurrentUserStore();

const confirmationMessage = ref('');
const currentEnv = ref(null);
const confirmationState = ref(false);

const showAdd = ref(false);
const envToCreate = ref({
  name: '',
  showNameError: false
});

const envColumns = [
  {
    name: 'name',
    label: 'Name',
    field: 'name'
  },
  {
    name: 'createdAt',
    label: 'Created At',
    field: 'createdAt'
  },
  {
    name: 'updatedAt',
    label: 'Updated At',
    field: 'updatedAt'
  },
  {
    name: 'actions',
    label: 'Actions'
  }
];

function envTestCase(env) {
  return env.deleted ? 'text-gray-300' : 'text-black';
}

function activateEnv(id) {
  api.put(`/environments/${id}/activate`)
      .then(() => environmentStore.loadEnvironments());
}

function deactivateEnv(id) {
  api.delete(`/environments/${id}/deactivate`)
      .then(() => environmentStore.loadEnvironments());
}

function confirmDelete(env) {
  console.log(env)
  confirmationMessage.value = `Are you sure you want to delete environment ${env.name}? This will cause all related deployments to be deleted as well!`;
  currentEnv.value = env;
  confirmationState.value = true;
}

function deleteEnv() {
  confirmationState.value = false;
  api.delete(`/environments/${currentEnv.value.id}/delete`)
      .then(() => environmentStore.loadEnvironments());
}

function startCreate() {
  envToCreate.value.name = '';
  envToCreate.value.showNameError = false;
  showAdd.value = true;
}

function createEnv() {
  if (envToCreate.value.name === '') {
    envToCreate.value.showNameError = true;
  } else {
    showAdd.value = false;
    api.post('/environments', { name: envToCreate.value.name })
        .then(() => environmentStore.loadEnvironments());
  }
}

onMounted(() => {
  pageInfoStore.setPageTitle('Deployable Environments');
});
</script>
