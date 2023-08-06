<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of recent builds" :columns="buildColumns" :rows="buildRows" row-id="id" title="Builds" :loading="loading" :pagination="pagination" table-key="builds" @table-page="changePage" @table-page-size="changePageSize">
        <template #header>
          <h3 class="font-semibold text-lg text-gray-700">
            Builds
            <i class="far fa-refresh fa-spin" v-if="loading"></i>
            <input v-model="componentFilter" placeholder="Component Filter" class="float-right w-150 py-1 mb-3 text-xs placeholder-gray-300 text-gray-500 bg-white border border-gray-200 appearance-none peer focus:outline-none focus:ring-0 focus:border-gray-200"/>
          </h3>
        </template>

        <template #body-cell-actions>
          <button type="button" v-if="currentUserStore.isDeployableSystemAdmin" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-medal"></i>
          </button>
          <button type="button" v-if="currentUserStore.isDeployableSystemAdmin" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-magnifying-glass"></i>
          </button>
          <button type="button" v-if="currentUserStore.isDeployableSystemAdmin" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-rectangle-list"></i>
          </button>
        </template>
      </card-table>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref, watch} from "vue";

import {usePageInfoStore} from "@/stores/pageInfo";
import {useCurrentUserStore} from "@/stores/currentUser";

import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import debounce from "@/utils/debounce";

const pageInfo = usePageInfoStore();
const currentUserStore = useCurrentUserStore();

const buildRows = ref([]);
const loading = ref(false);
const pagination = ref({
  rowsPerPage: 10,
  page: 1,
  totalElements: 0
});
const componentFilter = ref('');

const debounceComponentFilter = debounce(loadBuilds, 300);

watch(componentFilter, (...args) => {
  debounceComponentFilter(...args);
});

const buildColumns = [
  {
    name: 'componentIdentifier',
    label: 'Component',
    field: 'componentIdentifier',
    align: 'left'
  },
  {
    name: 'componentVersion',
    label: 'Version',
    field: 'componentVersion',
    align: 'left'
  },
  {
    name: 'sourceBranch',
    label: 'Source',
    field: 'sourceBranch',
    align: 'left'
  },
  {
    name: 'commitRef',
    label: 'Commit Ref',
    field: 'commitRef',
    align: 'left'
  },
  {
    name: 'commitUser',
    label: 'Committer',
    field: 'commitUser',
    align: 'left'
  },
  {
    name: 'released',
    label: 'Released?',
    align: 'left'
  },
  {
    name: 'createdAt',
    label: 'Created At',
    align: 'left'
  },
  {
    name: 'actions',
    label: 'Actions',
    align: 'left'
  }
];

function loadBuilds() {
  loading.value = true;
  api.get(`/build`, { params: { pageNumber: pagination.value.page, pageSize: pagination.value.rowsPerPage, componentFilter: componentFilter.value }})
      .then(response => {
        const data = response.data;
        buildRows.value = data.content;
        pagination.value.totalElements = data.totalElements;
        loading.value = false;
      });
}

function changePage(tableKey, pageNum) {
  pagination.value.page = pageNum;
  loadBuilds();
}

function changePageSize(tableKey, pageSize) {
  pagination.value.rowsPerPage = pageSize;
  loadBuilds();
}

onMounted(() => {
  pageInfo.setPageTitle("Builds");
  loadBuilds();
})
</script>
