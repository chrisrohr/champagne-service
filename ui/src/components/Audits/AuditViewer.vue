<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table :caption="props.caption" :columns="auditColumns" :rows="auditRows" row-id="id" :title="props.title" :loading="loading" :pagination="auditPagination" table-key="audits" @table-page="changePage" @table-page-size="changePageSize">
        <template #body-cell-time="props">
          {{ fromNow(props.row.timestamp) }} ({{ formatDate(props.row.timestamp) }})
        </template>
      </card-table>
    </div>
  </div>
</template>

<script setup>
import {formatDate, fromNow} from "@/utils/time";
import CardTable from "@/components/Cards/CardTable.vue";
import {onMounted, ref} from "vue";
import {api} from "@/plugins/axios";

const props = defineProps(['caption', 'additionalColumns', 'auditUrl', 'title']);

const loading = ref(false);

const auditPagination = ref({
  rowsPerPage: 10,
  page: 1,
  totalElements: 0
});

const auditRows = ref({});

const auditColumns = ref([
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
  }
]);

function changePage(tableKey, pageNum) {
  auditPagination.value.page = pageNum;
  loadAudits();
}

function changePageSize(tableKey, pageSize) {
  auditPagination.value.rowsPerPage = pageSize;
  loadAudits();
}

function loadAudits() {
  return api.get(props.auditUrl, {
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
  if (props.additionalColumns) {
    auditColumns.value.push(...props.additionalColumns);
  }

  loadAudits();
});

</script>
