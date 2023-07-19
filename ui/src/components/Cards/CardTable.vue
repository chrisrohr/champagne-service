<template>
  <div class="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded bg-white">
    <div class="rounded mb-0 px-4 py-3 border-0" v-if="props.title">
      <div class="relative w-full px-4 max-w-full flex-grow flex-1">
        <slot name="header">
          <h3 class="font-semibold text-lg text-gray-700">
            {{ props.title }}
            <i class="far fa-refresh fa-spin" v-if="props.loading"></i>
          </h3>
        </slot>
      </div>
    </div>
    <div class="block w-full overflow-x-auto">
      <table class="items-center w-full bg-transparent border-collapse">
        <caption class="hidden">{{ props.caption }}</caption>
        <thead>
          <tr>
            <th v-for="column in props.columns" :key="column.name" class="px-6 align-middle border border-solid py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left bg-gray-50 text-gray-500 border-gray-100">
              {{ column.label }}
            </th>
          </tr>
        </thead>
        <tbody>
          <template v-for="row in props.rows" :key="row[props.rowId]">
            <slot name="body" v-if="!!slots.body" :row="row"/>
            <tr class="hover:bg-gray-200" v-else>
              <td v-for="column in props.columns" :key="column.name" class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                <slot :name="cellSlotName(column.name)" :row="row">
                  {{ row[column.field] }}
                </slot>
              </td>
            </tr>
          </template>
          <tr v-if="props.rows.length === 0">
            <td :colspan="props.columns.length" class="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
              {{ props.noData || 'There are no rows found' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import {useSlots} from "vue";

const props = defineProps(['title', 'columns', 'caption', 'rows', 'rowId', 'loading', 'noData']);

const slots = useSlots();

function cellSlotName(columnName) {
  return `body-cell-${columnName}`;
}
</script>
