<template>
  <div class="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded bg-white">
    <div class="rounded mb-0 px-4 py-3 border-0" v-if="props.title || !!slots.header">
      <div class="relative w-full px-4 max-w-full flex-grow flex-1">
        <slot name="header">
          <h3 class="font-semibold text-lg text-gray-700">
            {{ props.title }}
            <i class="far fa-refresh fa-spin" v-if="props.loading"></i>
          </h3>
        </slot>
      </div>
    </div>
    <div class="block w-full overflow-visible">
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
      <div class="flex items-center justify-center w-full py-2 align-middle text-xs border-t border-t-gray-100" v-if="props.pagination && props.pagination.totalElements > 0">
        <ul class="flex items-center -space-x-px h-8">
          <li>
            <button type="button" @click="$emit('table-page', props.tableKey, 1)" :disabled="props.pagination.page === 1" class="justify-center px-3 h-8 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-l-lg hover:bg-gray-100 hover:text-gray-700 disabled:text-gray-200 disabled:hover:bg-white">
              <span class="sr-only">Previous</span>
              <i class="fas fa-chevron-left"></i>
            </button>
          </li>
          <li v-for="n in (Math.ceil(props.pagination.totalElements/props.pagination.rowsPerPage))" :key="n">
            <button type="button" @click="$emit('table-page', props.tableKey, n)" class="justify-center px-3 h-8 leading-tight text-gray-500 border border-gray-300 hover:bg-gray-100 hover:text-gray-700" :class="n === props.pagination.page ? 'bg-emerald-300 font-bold' : 'bg-white'">{{ n }}</button>
          </li>
          <li>
            <button type="button" @click="$emit('table-page', props.tableKey, Math.ceil(props.pagination.totalElements/props.pagination.rowsPerPage))" :disabled="props.pagination.page === (Math.ceil(props.pagination.totalElements/props.pagination.rowsPerPage))" class="justify-center px-3 h-8 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-r-lg hover:bg-gray-100 hover:text-gray-700 disabled:text-gray-200 disabled:hover:bg-white">
              <span class="sr-only">Next</span>
              <i class="fas fa-chevron-right"></i>
            </button>
          </li>
        </ul>
        <div class="flex w-28 ml-2 text-xs">
          <Multiselect v-model="tablePagination.rowsPerPage" :options="rowsPerPageOptions" @change="handlePageSizeChange"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {toRef, useSlots} from "vue";
import Multiselect from "@vueform/multiselect";

const props = defineProps(['title', 'columns', 'caption', 'rows', 'rowId', 'loading', 'noData', 'pagination', 'tableKey']);
const emits = defineEmits(['table-page-size']);

const slots = useSlots();

const rowsPerPageOptions = [5, 10, 20, 50, 100];
const tablePagination = toRef(props, 'pagination');

function cellSlotName(columnName) {
  return `body-cell-${columnName}`;
}

function handlePageSizeChange(value) {
  emits('table-page-size', props.tableKey, value);
}
</script>

<style>
@import '@vueform/multiselect/themes/tailwind.css';

.multiselect-wrapper {
  font-size: 0.75rem !important;
  line-height: 1rem !important;
}

.multiselect-option {
  font-size: 0.75rem !important;
  line-height: 1rem !important;
}
</style>
