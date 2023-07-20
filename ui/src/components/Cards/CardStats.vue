<template>
  <div class="relative flex flex-col min-w-0 break-words bg-white rounded mb-6 xl:mb-0 shadow-lg">
    <div class="flex-auto p-4">
      <div class="flex flex-wrap">
        <div class="relative w-full pr-4 max-w-full flex-grow flex-1">
          <h5 class="text-gray-400 uppercase font-bold text-xs">
            {{ props.statSubtitle }}
          </h5>
          <span class="font-semibold text-xl text-gray-700">
            {{ props.statTitle }}
          </span>
        </div>
        <div class="relative w-auto pl-4 flex-initial">
          <div class="text-white p-3 text-center inline-flex items-center justify-center w-12 h-12 shadow-lg rounded-full" :class="[props.statIconColor]">
            <i :class="[props.statIconName]"></i>
          </div>
        </div>
      </div>
      <p class="mt-9" v-if="props.statPercent === undefined"></p>
      <p class="text-sm text-gray-400 mt-4" v-if="props.statPercent !== undefined">
        <span class="mr-2" :class="[statPercentColor]">
          <i :class="statArrow"></i>
          {{ props.statPercent }}%
        </span>
        <span class="whitespace-nowrap" :class="[statPercentColor]">{{ props.statDescription }}</span>
      </p>
    </div>
  </div>
</template>

<script setup>

import {computed} from "vue";

const props = defineProps(['statSubtitle', 'statTitle', 'statPercent', 'statDescription', 'statIconName', 'statIconColor']);

const statArrow = computed(() => {
  if (props.statPercent > 0) {
    return 'fas fa-arrow-up';
  } else if (props.statPercent < 0) {
    return 'fas fa-arrow-down';
  }

  return 'fas fa-minus'
});

const statPercentColor = computed(() => {
  if (props.statPercent > 0) {
    return 'text-emerald-500';
  } else if (props.statPercent < 0) {
    return 'text-red-500';
  }

  return 'text-gray-300'
});
</script>
