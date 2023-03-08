<template>
    <q-page padding>
      <q-table title="Deployable Systems" :columns="systemColumns" :rows="adminSystemStore.systems" :loading="adminSystemStore.loading" v-model:pagination="adminSystemStore.pagination" @request="adminSystemStore.load">
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td>
              <q-btn round size="sm" color="primary" :icon="props.expand ? 'remove' : 'add'" @click="props.expand = !props.expand"/>
            </q-td>
            <q-td key="name" :props="props">
              {{  props.row.name }}
            </q-td>
            <q-td key="createdAt" :props="props">
              {{ fromNow(props.row.createdAt) }}
              <q-tooltip class="bg-grey-5 text-black">
                  {{ formatDate(props.row.createdAt) }}
              </q-tooltip>
            </q-td>
            <q-td key="updatedAt" :props="props">
              {{ fromNow(props.row.updatedAt) }}
              <q-tooltip class="bg-grey-5 text-black">
                  {{ formatDate(props.row.updatedAt) }}
              </q-tooltip>
            </q-td>
            <q-td key="actions" :props="props">
              <q-btn size="sm" icon="group_add" @click="startAssignUsers(props.row)" class="on-left">
                <q-tooltip>Assign Users</q-tooltip>
              </q-btn>
              <q-btn size="sm" icon="delete" @click="confirmDelete(props.row)">
                <q-tooltip>Delete System</q-tooltip>
              </q-btn>
            </q-td>
          </q-tr>
          <q-tr v-show="props.expand" :props="props">
            <q-td colspan="100%">
              <q-table :columns="systemUserCols" :rows="props.row.users" :pagination="userPagination">
                <template v-slot:body="users">
                  <q-td key="username">
                    <q-icon name="fa-solid fa-crown" v-if="users.row.admin" class="text-yellow-14">
                      <q-tooltip class="bg-grey-5 text-black">
                          User is an Admin for this System
                      </q-tooltip>
                    </q-icon>
                    {{ userStore.userForId(users.row.userId).displayName }}
                  </q-td>
                  <q-td key="actions">
                    <q-btn size="sm" icon="delete" @click="removeUserFromSystem(props.row.id, users.row.userId)">
                      <q-tooltip>Remove User</q-tooltip>
                    </q-btn>
                  </q-td>
                </template>
              </q-table>
            </q-td>
          </q-tr>
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
          </q-card-section>
          <q-card-section>
            <q-checkbox v-model="selectedUserAdmin"/>
          </q-card-section>
          <q-card-actions align="right">
            <q-btn flat v-close-popup>Cancel</q-btn>
            <q-btn flat color="primary" @click="addOrUpdateUserInSystem">Save</q-btn>
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
const selectedUserAdmin = ref(false)
const systemUser = ref({
  systemId: null,
  userId: null,
  admin: false
})
const allUsers = ref([])

// Constant data
const systemColumns = [
  {
    name: 'expand'
  },
  {
    name: 'name',
    label: 'Name',
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
const systemUserCols = [
  {
    name: 'username',
    label: 'User Name',
    align: 'left'
  },
  {
    name: 'actions',
    label: 'Actions',
    align: 'left'
  }
]
const userPagination = {
  rowsPerPage: 10
}

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
  allUsers.value = userToOption(userStore.users)
    .filter(user => isNewUser(user, system.users))
  systemUser.value.systemId = system.id
  showAssignUsers.value = true
}

function userToOption (users) {
  return _.sortBy(users.map(e => { return { label: e.displayName, value: e.id } }), ['label'])
}

function isNewUser (user, systemUsers) {
  return _.indexOf(systemUsers.map(u => u.id), user.value) === -1
}

// function addUsersToSystem () {
//   adminSystemStore.assignUsersToSystem(systemUsers.value.systemId, systemUsers.value.users)
//     .then(() => {
//       showAssignUsers.value = false
//     })
// }

function removeUserFromSystem (systemId, userId) {
  adminSystemStore.removeUserFromSystem(systemId, userId)
}

onMounted(() => {
  userStore.load({ pagination: { page: 1, rowsPerPage: 2000 } }).then(() => {
    adminSystemStore.load()
  })
})

</script>
