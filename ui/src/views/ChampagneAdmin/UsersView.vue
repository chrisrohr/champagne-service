<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of users" :columns="userColumns" :rows="userRows" row-id="id" title="Users" :loading="loading" :pagination="pagination" table-key="users" @table-page="changePage" @table-page-size="changePageSize">
        <template #body-cell-admin="props">
          <i class="fas fa-check-square text-lime-400 text-lg" v-if="props.row.admin"></i>
          <i class="fas fa-xmark-square text-red-400 text-lg" v-if="!props.row.admin"></i>
        </template>

        <template #body-cell-systems="props">
          <ul>
            <li v-for="system in props.row.systems" v-bind:key="system.id">
              {{ system.name }} <span v-if="system.admin">(Admin)</span>
            </li>
          </ul>
        </template>

        <template #body-cell-actions="props">
          <table-actions-dropdown :action-list="userActions" :row="props.row" v-if="userActions.length > 0"/>
        </template>
      </card-table>
    </div>

    <confirmation-prompt title="Hold up!" :message="deleteUserConfirmationMessage" @cancelConfirm="deleteUserConfirmationState = false" @acceptConfirm="deleteUser" v-if="deleteUserConfirmationState"/>

    <button class="fixed bottom-3 right-14 bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase text-xs w-10 h-10 p-0 rounded-full shadow hover:shadow-md outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="startCreateUser">
      <i class="fas fa-plus"></i>
    </button>

    <div v-if="showAddUser" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              <span v-if="userToCreate.id === null">Add new user</span>
              <span v-else>Update user</span>
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" placeholder="First Name" v-model="userToCreate.firstName" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="userToCreate.showFirstNameError">
              First Name is required!
            </div>
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Last Name" v-model="userToCreate.lastName" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="userToCreate.showLastNameError">
              Last Name is required!
            </div>
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Username" v-model="userToCreate.systemIdentifier" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="userToCreate.showSystemIdentifierError">
              Username is required!
            </div>
            <div class="mb-3 pt-0">
              <label for="adminCheckbox" class="text-sm font-medium text-gray-900 dark:text-gray-300">Admin?</label>
              <input type="checkbox" v-model="userToCreate.admin" id="adminCheckbox" class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600 ml-2"/>
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddUser = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="userToCreate.id === null ? createUser() : updateUser()">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddUser" class="opacity-25 fixed inset-0 z-40 bg-black"></div>

    <div v-if="showAddUserToSystem" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Add User to System
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <Multiselect v-model="systemToAdd" :options="allSystems" value-prop="id" label="name"/>
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddUserToSystem = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="addUserToSystem">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddUserToSystem" class="opacity-25 fixed inset-0 z-40 bg-black"></div>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";

import {usePageInfoStore} from "@/stores/pageInfo";

import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import TableActionsDropdown from "@/components/Dropdowns/TableActionsDropdown.vue";
import ConfirmationPrompt from "@/components/Alerts/ConfirmationPrompt.vue";
import Multiselect from "@vueform/multiselect";

const pageInfo = usePageInfoStore();

const deleteUserConfirmationMessage = ref('');
const deleteUserConfirmationState = ref(false);
const currentUser = ref(null);

const showAddUser = ref(false);
const userToCreate = ref({
  id: null,
  firstName: '',
  lastName: '',
  systemIdentifier: '',
  admin: false,
  showFirstNameError: false,
  showLastNameError: false,
  showSystemIdentifierError: false
});

const showAddUserToSystem = ref(false);
const systemToAdd = ref('');
const allSystems = ref([]);

const userRows = ref([]);
const loading = ref(false);
const pagination = ref({
  rowsPerPage: 10,
  page: 1,
  totalElements: 0
});

const userColumns = [
  {
    name: 'displayName',
    label: 'Name',
    field: 'displayName',
  },
  {
    name: 'systemIdentifier',
    label: 'System Id',
    field: 'systemIdentifier',
  },
  {
    name: 'admin',
    label: 'Champagne Admin?'
  },
  {
    name: 'systems',
    label: 'Systems'
  },
  {
    name: 'actions',
  }
];

const userActions = [
  {
    label: 'Remove User',
    icon: 'fa-trash',
    onClick: confirmDeleteUser
  },
  {
    label: 'Edit User',
    icon: 'fa-user-pen',
    onClick: startUpdateUser
  },
  {
    label: 'Add to System',
    icon: 'fa-user-plus',
    onClick: startAddUserToSystem
  }
];

function loadUsers() {
  loading.value = true;
  api.get('/users/all', { params: { pageNumber: pagination.value.page, pageSize: pagination.value.rowsPerPage }})
      .then(response => {
        const data = response.data;
        userRows.value = data.content;
        pagination.value.totalElements = data.totalElements;
        loading.value = false;
      });
}

function changePage(tableKey, pageNum) {
  pagination.value.page = pageNum;
  loadUsers();
}

function changePageSize(tableKey, pageSize) {
  pagination.value.rowsPerPage = pageSize;
  loadUsers();
}

function startAddUserToSystem(user) {
  api.get('/systems/admin', { params: { pageSize: 1000 }})
      .then(response => {
        const userSystems = user.systems.map(system => system.id);
        allSystems.value = response.data.content.filter(system => userSystems.indexOf(system.id) < 0);
        currentUser.value = user;
        showAddUserToSystem.value = true;
      });
}

function addUserToSystem() {
  showAddUserToSystem.value = false;

  if (systemToAdd.value !== null) {
    api.post(`/users/system/${systemToAdd.value}/${currentUser.value.id}`)
        .then(loadUsers);
  }
}

function confirmDeleteUser(user) {
  deleteUserConfirmationMessage.value = `Are you sure you want to delete user ${user.displayName}?`;
  currentUser.value = user;
  deleteUserConfirmationState.value = true;
}

function deleteUser() {
  deleteUserConfirmationState.value = false;
  api.delete(`/users/${currentUser.value.id}`)
      .then(loadUsers);
}

function startCreateUser() {
  userToCreate.value.id = null;
  userToCreate.value.firstName = '';
  userToCreate.value.lastName = '';
  userToCreate.value.systemIdentifier = '';
  userToCreate.value.admin = false;
  userToCreate.value.showFirstNameError = false;
  userToCreate.value.showLastNameError = false;
  userToCreate.value.showSystemIdentifierError = false;

  showAddUser.value = true;
}

function createUser() {
  if (userFormHasErrors()) {
    return;
  }

  const userData = {
    firstName: userToCreate.value.firstName,
    lastName: userToCreate.value.lastName,
    systemIdentifier: userToCreate.value.systemIdentifier,
    displayName: `${userToCreate.value.firstName} ${userToCreate.value.lastName}`,
    admin: userToCreate.value.admin
  };

  showAddUser.value = false;
  api.post('/users', userData)
      .then(loadUsers);
}

function startUpdateUser(user) {
  userToCreate.value.id = user.id;
  userToCreate.value.firstName = user.firstName;
  userToCreate.value.lastName = user.lastName;
  userToCreate.value.systemIdentifier = user.systemIdentifier;
  userToCreate.value.admin = user.admin;
  userToCreate.value.showFirstNameError = false;
  userToCreate.value.showLastNameError = false;
  userToCreate.value.showSystemIdentifierError = false;

  showAddUser.value = true;
}

function updateUser() {
  if (userFormHasErrors()) {
    return;
  }

  const userData = {
    id: userToCreate.value.id,
    firstName: userToCreate.value.firstName,
    lastName: userToCreate.value.lastName,
    systemIdentifier: userToCreate.value.systemIdentifier,
    displayName: `${userToCreate.value.firstName} ${userToCreate.value.lastName}`,
    admin: userToCreate.value.admin
  };

  showAddUser.value = false;
  api.put('/users', userData)
      .then(loadUsers);
}

function userFormHasErrors() {
  let hasErrors = false;

  if (userToCreate.value.firstName === '') {
    userToCreate.value.showFirstNameError = true;
    hasErrors = true;
  }

  if (userToCreate.value.lastName === '') {
    userToCreate.value.showLastNameError = true;
    hasErrors = true;
  }

  if (userToCreate.value.systemIdentifier === '') {
    userToCreate.value.showSystemIdentifierError = true;
    hasErrors = true;
  }

  return hasErrors;
}

onMounted(() => {
  pageInfo.setPageTitle("Users");
  loadUsers();
})
</script>
