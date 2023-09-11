<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of users for the current system" :columns="userColumns" :rows="userRows" row-id="id" title="Users in System" :loading="loading" :pagination="pagination" table-key="systemUsers" @table-page="changePage" @table-page-size="changePageSize">
        <template #body-cell-systemAdmin="props">
          <i class="fas fa-check-square text-lime-400 text-lg" v-if="props.row.systemAdmin"></i>
          <i class="fas fa-xmark-square text-red-400 text-lg" v-if="!props.row.systemAdmin"></i>
        </template>
        <template #body-cell-actions="props">
          <table-actions-dropdown :action-list="userActions" :row="props.row" v-if="userActions.length > 0"/>
        </template>
      </card-table>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from "vue";

import {usePageInfoStore} from "@/stores/pageInfo";
import {useCurrentUserStore} from "@/stores/currentUser";

import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import TableActionsDropdown from "@/components/Dropdowns/TableActionsDropdown.vue";

const pageInfo = usePageInfoStore();
const currentUserStore = useCurrentUserStore();

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
    name: 'systemAdmin',
    label: 'System Admin?'
  },
  {
    name: 'actions',
  }
];

const userActions = computed(() => allUserActions.filter(action => action.permission === undefined || action.permission()));

const allUserActions = [
  {
    label: 'Remove User',
    icon: 'fa-trash',
    onClick: removeUserFromSystem,
    permission: () => {
      return currentUserStore.isDeployableSystemAdmin;
    }
  },
  {
    label: 'Become Admin',
    icon: 'fa-user-gear',
    onClick: updateUserAdminStatus,
    permission: () => {
      return currentUserStore.isDeployableSystemAdmin;
    },
    enabled: (user) => {
      return !user.systemAdmin;
    }
  },
  {
    label: 'Remove Admin',
    icon: 'fa-user-gear',
    onClick: updateUserAdminStatus,
    permission: () => {
      return currentUserStore.isDeployableSystemAdmin;
    },
    enabled: (user) => {
      return user.systemAdmin;
    }
  }
];

function loadSystemUsers() {
  loading.value = true;
  api.get('/users', { params: { pageNumber: pagination.value.page, pageSize: pagination.value.rowsPerPage }})
      .then(response => {
        const data = response.data;
        userRows.value = data.content;
        pagination.value.totalElements = data.totalElements;
        loading.value = false;
      });
}

function changePage(tableKey, pageNum) {
  pagination.value.page = pageNum;
  loadSystemUsers();
}

function changePageSize(tableKey, pageSize) {
  pagination.value.rowsPerPage = pageSize;
  loadSystemUsers();
}

function removeUserFromSystem(user) {
  api.delete(`/users/system/${user.id}`)
      .then(loadSystemUsers);
}

function updateUserAdminStatus(user) {
  api.put(`/users/system/${user.id}`, { admin: !user.systemAdmin })
      .then(loadSystemUsers);
}

onMounted(() => {
  pageInfo.setPageTitle("System Users");
  loadSystemUsers();
})
</script>
