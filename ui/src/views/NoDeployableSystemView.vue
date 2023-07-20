<template>
  <div class="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-gray-100 border-0">
    <div class="rounded-t bg-white mb-0 px-6 py-6">
      <div class="text-center flex justify-between">
        <h6 class="text-gray-700 text-xl font-bold">Something is not right!</h6>
      </div>
    </div>
    <div class="text-white mx-4 my-4 px-6 py-4 border-0 rounded relative bg-amber-500">
      <span class="text-xl inline-block mr-5 align-middle">
        <i class="fas fa-bell"></i>
      </span>
      <span class="inline-block align-middle mr-8">
        You must select a deployable system first!
      </span>
    </div>
  </div>
</template>

<script setup>
import {usePageInfoStore} from "@/stores/pageInfo";
import {onMounted} from "vue";

const pageInfoStore = usePageInfoStore();

onMounted(() => {
  pageInfoStore.setPageTitle('');
});
</script>
