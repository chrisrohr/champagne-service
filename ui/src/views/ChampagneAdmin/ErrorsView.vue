<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of recent errors" :columns="errorColumns" :rows="errorRows" row-id="id" title="Application Errors" :loading="loading" :pagination="pagination" table-key="errors" @table-page="changePage" @table-page-size="changePageSize">
        <template #header>
          <h3 class="font-semibold text-lg text-gray-700">
            Application Errors
            <i class="far fa-refresh fa-spin" v-if="loading"></i>
            <button type="button" @click="resolveAll" class="float-right text-xs text-gray-500 bg-transparent border border-solid rounded border-gray-500 px-2 py-1 mr-2" title="Resolve All">
              <i class="fas fa-flag-checkered"></i>
            </button>
          </h3>
        </template>

        <template #body-cell-createdAt="props">
          {{ formatDate(props.row.createdAt) }}
        </template>

        <template #body-cell-updatedAt="props">
          {{ formatDate(props.row.updatedAt) }}
        </template>

        <template #body-cell-actions="props">
          <table-actions-dropdown :action-list="errorActions" :row="props.row"/>
        </template>
      </card-table>
    </div>

    <div v-if="showErrorDetails" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              Error Details
            </h5>
            <div class="caption text-gray-300 float-right ml-4">{{ fromNow(currentError.updatedAt) }}</div>
          </div>
          <div class="relative p-6 flex-auto">
            <dl class="max-w-fit text-gray-900 divide-y divide-gray-200 max-h-fit">
              <div class="flex flex-col pb-3">
                <dt class="mb-1 text-gray-400 text-sm uppercase">Description</dt>
                <dd class="text-md font-semibold">{{ currentError.description }}</dd>
              </div>
              <div class="flex flex-col py-3" v-if="currentError.exceptionType">
                <dt class="mb-1 text-gray-400 text-sm uppercase">Exception Type</dt>
                <dd class="text-md font-semibold">{{ currentError.exceptionType }}</dd>
              </div>
              <div class="flex flex-col py-3" v-if="currentError.exceptionMessage">
                <dt class="mb-1 text-gray-400 text-sm uppercase">Exception Message</dt>
                <dd class="text-md font-semibold">{{ currentError.exceptionMessage }}</dd>
              </div>
              <div class="flex flex-col py-3" v-if="currentError.exceptionCauseType">
                <dt class="mb-1 text-gray-400 text-sm uppercase">Cause Type</dt>
                <dd class="text-md font-semibold">{{ currentError.exceptionCauseType }}</dd>
              </div>
              <div class="flex flex-col py-3" v-if="currentError.exceptionCauseMessage">
                <dt class="mb-1 text-gray-400 text-sm uppercase">Cause Message</dt>
                <dd class="text-md font-semibold">{{ currentError.exceptionCauseMessage }}</dd>
              </div>
              <div class="flex flex-col pt-3 overflow-auto max-h-48" v-if="currentError.stackTrace">
                <dt class="mb-1 text-gray-400 text-sm uppercase">Stack Trace</dt>
                <dd class="text-xs font-semibold"><pre>{{ currentError.stackTrace }}</pre></dd>
              </div>
            </dl>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showErrorDetails = false">
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showErrorDetails" class="opacity-25 fixed inset-0 z-40 bg-black"></div>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";

import {usePageInfoStore} from "@/stores/pageInfo";

import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import TableActionsDropdown from "@/components/Dropdowns/TableActionsDropdown.vue";
import {formatDate, fromNow} from "@/utils/time";

const pageInfo = usePageInfoStore();

const showErrorDetails = ref(false);
const currentError = ref(null);

const errorRows = ref([]);
const loading = ref(false);
const pagination = ref({
  rowsPerPage: 10,
  page: 1,
  totalElements: 0
});

const errorColumns = [
  {
    name: 'description',
    label: 'Description',
    field: 'description',
  },
  {
    name: 'numTimesOccurred',
    label: '# Times Occurred',
    field: 'numTimesOccurred',
  },
  {
    name: 'createdAt',
    label: 'First Occurred'
  },
  {
    name: 'updatedAt',
    label: 'Last Occurred'
  },
  {
    name: 'actions',
  }
];

const errorActions = [
  {
    label: 'View Details',
    icon: 'fa-eye',
    onClick: viewDetails
  },
  {
    label: 'Resolve',
    icon: 'fa-flag-checkered',
    onClick: resolveError
  }
];

function loadErrors() {
  loading.value = true;
  api.get(`/errors`, { params: { pageNumber: pagination.value.page, pageSize: pagination.value.rowsPerPage }})
      .then(response => {
        const data = response.data;
        console.log(data);
        errorRows.value = data.items;
        pagination.value.totalElements = data.totalCount;
        loading.value = false;
      });
}

function resolveError(error) {
  api.put(`/errors/resolve/${error.id}`)
      .then(loadErrors);
}

function resolveAll() {
  api.put('/errors/resolve')
      .then(loadErrors);
}

function viewDetails(error) {
  currentError.value = error;
  showErrorDetails.value = true;
}

function changePage(tableKey, pageNum) {
  pagination.value.page = pageNum;
  loadErrors();
}

function changePageSize(tableKey, pageSize) {
  pagination.value.rowsPerPage = pageSize;
  loadErrors();
}

onMounted(() => {
  pageInfo.setPageTitle("Application Errors");
  loadErrors();
})
</script>
