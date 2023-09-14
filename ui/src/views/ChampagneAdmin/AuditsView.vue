<template>
  <audit-viewer caption="List of all audits" title="Audits" audit-url="/audit/all" :additional-columns="auditColumns"/>
</template>

<script setup>
import {onMounted} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import AuditViewer from "@/components/Audits/AuditViewer.vue";

const pageInfoStore = usePageInfoStore();
const auditColumns = [
  {
    name: 'system',
    label: 'Deployable System',
    field: 'deployableSystemId'
  }
];

onMounted(() => {
  pageInfoStore.setPageTitle('All Audits');
});
</script>
