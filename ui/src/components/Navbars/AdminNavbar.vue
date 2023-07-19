<template>
  <!-- Navbar -->
  <nav class="absolute top-0 left-0 w-full z-10 bg-transparent md:flex-row md:flex-nowrap md:justify-start flex items-center p-4">
    <div class="w-full mx-auto items-center flex justify-between md:flex-nowrap flex-wrap md:px-10 px-4">
      <!-- Brand -->
      <a class="text-white text-sm uppercase hidden lg:inline-block font-semibold" href="javascript:void(0)">{{ pageInfoStore.pageTitle }}</a>
    </div>
    <ul class="flex-col md:flex-row list-none items-center hidden md:flex">
      <user-dropdown />
    </ul>
  </nav>
</template>

<script setup>
import UserDropdown from "@/components/Dropdowns/UserDropdown.vue";
import {usePageInfoStore} from "@/stores/pageInfo";

const pageInfoStore = usePageInfoStore();
</script>
