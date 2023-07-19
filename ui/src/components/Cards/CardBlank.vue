<template>
  <div class="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-gray-100 border-0">
    <div class="rounded-t bg-white mb-0 px-6 py-6" v-if="props.title">
      <div class="text-center flex justify-between">
        <h6 class="text-gray-700 text-xl font-bold">
          {{ props.title }}
        </h6>
      </div>
    </div>
    <div class="flex-auto px-4 lg:px-10 py-10 pt-0">
      <slot name="content"></slot>
    </div>
  </div>
</template>

<script setup>
const props = defineProps(['title']);
</script>
