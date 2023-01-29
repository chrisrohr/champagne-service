<template>
  <q-page class="q-pa-md">
    <q-table :columns="userColumns" :rows="users" :loading="loading" :pagination="pagination">
      <template v-slot:top>
        <q-space/>
        <span>
                    <q-btn icon="add" @click="this.showUserAdd = true"/>
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

      <template v-slot:body-cell-deleted="props">
        <q-td :props="props">
          <q-icon name="toggle_on" color="primary" size="md" v-if="!props.row.deleted">
            <q-tooltip class="bg-primary">User is Active</q-tooltip>
          </q-icon>
          <q-icon name="toggle_off" color="grey-4" size="md" v-if="props.row.deleted">
            <q-tooltip class="bg-grey-4 text-black">User is NOT Active</q-tooltip>
          </q-icon>
        </q-td>
      </template>

      <template v-slot:body-cell-actions="props">
        <q-td :props="props">
          <q-btn size="sm" icon="delete" v-if="!props.row.deleted" @click="deactivateUser(props.row.displayName, props.row.id)">
            <q-tooltip>Deactivate User</q-tooltip>
          </q-btn>
          <q-btn size="sm" icon="restore_from_trash" v-if="props.row.deleted" @click="reactivateUser(props.row.id)">
            <q-tooltip>Reactivate User</q-tooltip>
          </q-btn>
        </q-td>
      </template>
    </q-table>

    <!-- TODO: Update this dialog to incorporate third-party user lookups when we have built that capability -->
    <q-dialog v-model="showUserAdd">
      <q-card>
        <q-card-section>
          <div class="text-h5">Add new user</div>
        </q-card-section>
        <q-card-section>
          <div class="row q-mb-md">
            <q-input dense style="min-width: 500px" v-model="user.firstName" label="First Name"
                     @keyup="updateDisplayName" :rules="[val => !!val || 'First Name is required']"/>
          </div>
          <div class="row q-mb-md">
            <q-input dense style="min-width: 500px" v-model="user.lastName" label="Last Name" @keyup="updateDisplayName"
                     :rules="[val => !!val || 'Last Name is required']"/>
          </div>
          <div class="row q-mb-md">
            <q-input dense style="min-width: 500px" v-model="user.displayName" label="Display Name"
                     :rules="[val => !!val || 'Display Name is required']"/>
          </div>
          <div class="row q-mb-md">
            <q-input dense style="min-width: 500px" v-model="user.username" label="Username"
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

<script setup lang="ts">

import { onMounted, ref } from 'vue';
import {fromNow, formatDate} from 'src/utils/time';
import { useQuasar } from 'quasar';
import {api} from 'boot/axios';
import {notifyError} from 'src/utils/alerts';
import {AxiosResponse} from 'axios';

const $q = useQuasar();

const users = ref([]);
const loading = ref(false);
const pagination = ref({
  rowsPerPage: 20
});
const showUserAdd = ref(false);
const user = ref({
  firstName: '',
    lastName: '',
    displayName: '',
    username: ''
});

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
      name: 'deleted',
      label: 'Status',
      align: 'left'
    },
    {
      name: 'actions',
      label: 'Actions',
      align: 'left'
    }
];

const loadUsers = () => {
  loading.value = true;
  api.get('/users', {
    params: {
      'includeDeleted': true
    }
  })
    .then((response: AxiosResponse) => {
      users.value = response.data.content;
    })
    .catch((error: Error) => notifyError('Unable to load users', error))
    .finally(() => {
      loading.value = false;
    });
};

const updateDisplayName = () => {
  user.value.displayName = `${user.value.firstName} ${user.value.lastName}`;
};

const createUser = () => {
  showUserAdd.value = false;

  const userData = {
    firstName: user.value.firstName,
    lastName: user.value.lastName,
    displayName: user.value.displayName,
    systemIdentifier: user.value.username
  };

  api.post('/users', userData)
    .then(() => loadUsers())
    .catch((error: Error) => notifyError('Unable to create user', error));
};

const deactivateUser = (name: string, id: number) => {
  $q.dialog({
    title: 'Hold Up!',
    message: `Are you sure you want to deactivate user ${name}?`,
    cancel: true,
    persistent: true
  }).onOk(() => {
    api.delete(`/users/${id}`)
      .then(() => loadUsers())
      .catch((error: Error) => notifyError('Failed to deactivate user', error));
  });
};

const reactivateUser = (id: number) => {
  api.put(`/users/${id}/reactivate`)
    .then(() => loadUsers())
    .catch((error: Error) => notifyError('Failed to reactivate user', error));
};

onMounted(() => {
  loadUsers();
});
</script>
