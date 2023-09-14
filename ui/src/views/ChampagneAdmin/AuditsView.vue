<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of system audits" :columns="auditColumns" :rows="auditRows" row-id="id" title="Audits" :loading="loading" :pagination="auditPagination" table-key="audits" @table-page="changePage" @table-page-size="changePageSize">
        <template #body-cell-time="props">
          {{ fromNow(props.row.timestamp) }} ({{ formatDate(props.row.timestamp) }})
        </template>
      </card-table>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import {formatDate, fromNow} from "@/utils/time";

const pageInfoStore = usePageInfoStore();

const loading = ref(false);

const auditPagination = ref({
  rowsPerPage: 10,
  page: 1,
  totalElements: 0
});

const auditRows = ref({});

const auditColumns = [
  {
    name: 'time',
    label: 'Audit Time',
  },
  {
    name: 'user',
    label: 'User',
    field: 'userSystemIdentifier'
  },
  {
    name: 'action',
    label: 'Action',
    field: 'action'
  },
  {
    name: 'recordType',
    label: 'Record Type',
    field: 'recordType'
  },
  {
    name: 'recordId',
    label: 'Record ID',
    field: 'recordId'
  },
  {
    name: 'system',
    label: 'Deployable System',
    field: 'deployableSystemId'
  }
];

function changePage(tableKey, pageNum) {
  auditPagination.value.page = pageNum;
  loadAudits();
}

function changePageSize(tableKey, pageSize) {
    auditPagination.value.rowsPerPage = pageSize;
    loadAudits();
}

function loadAudits() {
  return api.get(`/audit/all`, {
    params: {
      pageNumber: auditPagination.value.page,
      pageSize: auditPagination.value.rowsPerPage
    }
  })
      .then(response => {
        const data = response.data;
        auditRows.value = data.content;
        auditPagination.value.totalElements = data.totalElements;
        loading.value = false;
      });
}

onMounted(() => {
  pageInfoStore.setPageTitle('All Audits');

  loadAudits();
});
</script>
