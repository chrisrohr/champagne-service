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
            <q-btn size="sm" icon="group_add" @click="startAssignUsers(props.row)" class="on-left">
              <q-tooltip>Assign Users</q-tooltip>
            </q-btn>
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

      <q-dialog v-model="showAssignUsers">
        <q-card>
          <q-card-section>
            <div class="text-h5">Manage Users for System</div>
          </q-card-section>
          <q-card-section class="row">
            <q-select
              outlined
              dense
              style="min-width: 205px"
              v-model="selectedUser"
              :options="allUsers"/>
            <q-btn label="Add User" @click="addUserToSystem" class="on-right" color="primary"/>
          </q-card-section>
          <q-card-section>
            <q-table :rows="systemUsers.users" :columns="selectedUserCols" :pagination="{ rowsNumber: 2000 }" hide-pagination>
              <template v-slot:body-cell-admin="props">
                <q-td :props="props">
                  <q-checkbox v-model="props.row.admin"/>
                </q-td>
              </template>

              <template v-slot:body-cell-action="props">
                <q-td :props="props">
                  <q-btn size="sm" icon="delete" @click="removeSelectedUser(props.row)">
                    <q-tooltip>Remove User</q-tooltip>
                  </q-btn>
                </q-td>
              </template>
            </q-table>
          </q-card-section>
          <q-card-actions align="right">
            <q-btn flat v-close-popup>Cancel</q-btn>
            <q-btn flat color="primary" @click="addUsersToSystem">Save</q-btn>
          </q-card-actions>
        </q-card>
      </q-dialog>
    </q-page>
  </template>

<script setup>
import { onMounted, ref } from 'vue'
import { formatDate, fromNow } from '../utils/time'
import { confirmAction } from '../utils/alerts'
import { _ } from 'lodash'
import { useAdminSystemStore } from 'stores/adminSystemStore'
import { useUserStore } from 'stores/userStore'

const adminSystemStore = useAdminSystemStore()
const userStore = useUserStore()

// Reactive data
const showSystemAdd = ref(false)
const activeSystem = ref({
  name: ''
})
const showAssignUsers = ref(false)
const selectedUser = ref(null)
const systemUsers = ref({
  systemId: null,
  users: []
})
const allUsers = ref([])

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
const selectedUserCols = [
  {
    name: 'username',
    label: 'User',
    field: 'displayName',
    align: 'left'
  },
  {
    name: 'admin',
    label: 'Admin?',
    align: 'left'
  },
  {
    name: 'action',
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

function startAssignUsers (system) {
  userStore.load({ pagination: { page: 1, rowsPerPage: 2000 } }).then(() => {
    allUsers.value = userToOption(userStore.users)

    systemUsers.value.systemId = system.id
    populateNameOnExistingUsers(system.users)
    systemUsers.value.users = system.users
    showAssignUsers.value = true
  })
}

function userToOption (users) {
  return _.sortBy(users.map(e => { return { label: e.displayName, value: e.id } }), ['label'])
}

function populateNameOnExistingUsers (users) {
  users.forEach(user => {
    user.displayName = _.find(allUsers.value, u => u.value === user.userId).label
  })
}

function addUserToSystem () {
  systemUsers.value.users.push({ userId: selectedUser.value.value, displayName: selectedUser.value.label, admin: false })
}

function removeSelectedUser (selectedUser) {
  systemUsers.value.users = _.without(systemUsers.value.users, selectedUser)
}

function addUsersToSystem () {
  adminSystemStore.assignUsersToSystem(systemUsers.value.systemId, systemUsers.value.users)
    .then(() => {
      showAssignUsers.value = false
    })
}

onMounted(() => {
  adminSystemStore.load()
})

</script>
