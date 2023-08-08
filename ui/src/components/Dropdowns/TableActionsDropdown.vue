<template>
<div class="relative">
  <button type="button" class="text-gray-500 py-1 px-3" ref="actionBtnRef" @click.stop="toggleDropdown">
    <i class="fas fa-ellipsis-v"></i>
  </button>
  <div ref="actionListRef" class="absolute right-0 bg-white text-base z-50 py-2 list-none text-left rounded shadow-lg min-w-48" :class="{ hidden: !dropdownPopoverShow, block: dropdownPopoverShow }">
    <a v-for="action in props.actionList" :key="action.label" href="javascript:void(0);" class="text-sm py-2 px-4 font-normal block w-full whitespace-nowrap bg-transparent hover:bg-gray-100" :class="action.enabled === undefined || action.enabled(props.row) ? 'text-emerald-500' : 'text-gray-200 pointer-events-none'" @click="action.onClick(props.row)">
      <i class="fas mr-3" :class="action.icon"></i>
      {{ action.label }}
    </a>
  </div>
</div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref} from "vue";

const props = defineProps(['actionList', 'row']);

const actionListRef = ref(null);
const dropdownPopoverShow = ref(false);

function toggleDropdown() {
  dropdownPopoverShow.value = !dropdownPopoverShow.value;
}

function close(e) {
  if (!actionListRef.value.contains(e.target)) {
    dropdownPopoverShow.value = false;
  }
}

onMounted(() => {
  document.addEventListener('click', close);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', close);
})
</script>
