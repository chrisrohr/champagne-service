<template>
  <q-page class="q-pa-md">
    <q-table :columns="userColumns" :rows="users" :loading="loading" :pagination="pagination">
      <template v-slot:top>
        <q-space/>
        <span>
          <q-btn icon="add" @click="showUserAdd = true"/>
          <q-tooltip>Add new user</q-tooltip>
        </span>
        <span>
          <q-btn icon="sync" @click="loadUsers"/>
          <q-tooltip>Refresh users</q-tooltip>
        </span>
      </template>

      <template v-slot:body-cell-createdAt="props">
        <q-td :props="props">
          <span>
            {{ fromNow(props.row.createdAt) }}
            <q-tooltip class="bg-grey-5 text-black">
                {{ formatDate(props.row.createdAt) }}
            </q-tooltip>
          </span>
        </q-td>
      </template>

      <template v-slot:body-cell-updatedAt="props">
        <q-td :props="props">
          <span>
            {{ fromNow(props.row.updatedAt) }}
            <q-tooltip class="bg-grey-5 text-black">
                {{ formatDate(props.row.updatedAt) }}
            </q-tooltip>
          </span>
        </q-td>
      </template>

      <template v-slot:body-cell-actions="props">
        <q-td :props="props">
          <q-btn size="sm" icon="delete" @click="deleteUser(props.row.displayName, props.row.id)">
            <q-tooltip>Delete User</q-tooltip>
          </q-btn>
        </q-td>
      </template>

    </q-table>

    <q-dialog v-model="showUserAdd">
      <q-card>
        <q-card-section>
          <div class="text-h5">Add new user</div>
        </q-card-section>
        <q-card-section>
          <div class="row q-mb-md">
            <q-input dense style="min-width: 500px" v-model="activeUser.firstName" label="First Name"
                     @keyup="updateDisplayName" :rules="[val => !!val || 'First Name is required']"/>
          </div>
          <div class="row q-mb-md">
            <q-input dense style="min-width: 500px" v-model="activeUser.lastName" label="Last Name"
                     @keyup="updateDisplayName"
                     :rules="[val => !!val || 'Last Name is required']"/>
          </div>
          <div class="row q-mb-md">
            <q-input dense style="min-width: 500px" v-model="activeUser.displayName" label="Display Name"
                     :rules="[val => !!val || 'Display Name is required']"/>
          </div>
          <div class="row q-mb-md">
            <q-input dense style="min-width: 500px" v-model="activeUser.systemIdentifier" label="Username"
                     :rules="[val => !!val || 'Username is required']"/>
          </div>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat v-close-popup>Cancel</q-btn>
          <q-btn flat color="primary" @click="createUser">Save</q-btn>
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { formatDate, fromNow } from '../utils/time'
import { useQuasar } from 'quasar'
import { api } from 'boot/axios'

const $q = useQuasar()

// Reactive data
const users = ref([])
const loading = ref(false)
const pagination = ref({
  rowsPerPage: 20
})
const showUserAdd = ref(false)
const activeUser = ref({
  firstName: '',
  lastName: '',
  displayName: '',
  systemIdentifier: ''
})

// Constant data
const userColumns = [
  {
    name: 'name',
    label: 'Name',
    field: 'displayName',
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

// Methods
// TODO: Should we switch this local use of users to a store?
function loadUsers () {
  loading.value = true
  api.get('/users')
    .then((response) => {
      users.value = response.data.content
    })
    .finally(() => {
      loading.value = false
    })
}

function updateDisplayName () {
  activeUser.value.displayName = `${activeUser.value.firstName} ${activeUser.value.lastName}`
}

function createUser () {
  showUserAdd.value = false

  api.post('/users', activeUser.value)
    .then(() => loadUsers())
}

function deleteUser (name, id) {
  $q.dialog({
    title: 'Hold Up!',
    message: `Are you sure you want to deactivate user ${name}?`,
    cancel: true,
    persistent: true
  }).onOk(() => {
    api.delete(`/users/${id}`)
      .then(() => loadUsers())
  })
}

onMounted(() => {
  loadUsers()
})
</script>
