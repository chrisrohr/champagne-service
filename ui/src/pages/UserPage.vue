<template>
  <q-page class="q-pa-md">
    <q-table title="System Users" :columns="userColumns" :rows="userStore.users" :loading="userStore.loading" v-model:pagination="userStore.pagination" @request="userStore.load">

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

    <q-page-sticky position="bottom-right" :offset="[18, 18]">
      <q-btn fab icon="add" color="accent" @click="showUserAdd = true" />
    </q-page-sticky>

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
import { useUserStore } from 'stores/userStore'

const $q = useQuasar()

// Stores
const userStore = useUserStore()

// Reactive data
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
function updateDisplayName () {
  activeUser.value.displayName = `${activeUser.value.firstName} ${activeUser.value.lastName}`
}

function createUser () {
  showUserAdd.value = false
  userStore.create(activeUser.value)
}

function deleteUser (name, id) {
  $q.dialog({
    title: 'Hold Up!',
    message: `Are you sure you want to deactivate user ${name}?`,
    cancel: true,
    persistent: true
  }).onOk(() => {
    userStore.deleteUser(id)
  })
}

onMounted(() => {
  userStore.load()
})
</script>
