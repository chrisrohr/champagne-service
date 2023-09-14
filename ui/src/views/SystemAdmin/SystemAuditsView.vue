<template>
  <audit-viewer caption="List of system audits" title="Audits" audit-url="/audit" />
</template>

<script setup>
import {onMounted} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import AuditViewer from "@/components/Audits/AuditViewer.vue";

const pageInfoStore = usePageInfoStore();

onMounted(() => {
  pageInfoStore.setPageTitle('System Audits');
});
</script>
