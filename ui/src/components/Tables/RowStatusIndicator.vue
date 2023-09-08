<template>
  <span @click="$emit('row-status-change', rowId, subId, status, rowData)" class="cursor-pointer">
    <span class="fa-stack" :class="props.statusOptions[props.status].color">
      <i class="far fa-circle fa-stack-2x"></i>
      <i class="fas fa-stack-1x" :class="props.statusOptions[props.status].icon"></i>
    </span>
  </span>
</template>

<script setup>
const props = defineProps(['rowId', 'subId', 'status', 'statusOptions', 'rowData']);
</script>
