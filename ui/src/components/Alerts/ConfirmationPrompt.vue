<template>
  <div>
    <div class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-xl font-semibold">
              {{ props.title }}
            </h5>
            <button class="p-1 ml-auto bg-transparent border-0 text-black opacity-30 float-right text-3xl leading-none font-semibold outline-none focus:outline-none" @click="$emit('cancelConfirm')">
              <span class="bg-transparent text-black opacity-30 h-6 w-6 text-2xl block outline-none focus:outline-none">
                x
              </span>
            </button>
          </div>
          <div class="relative p-6 flex-auto">
            <p class="my-4 text-gray-500 text-lg leading-relaxed">
              {{ props.message }}
            </p>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="$emit('cancelConfirm')">
              No
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="$emit('acceptConfirm')">
              Yes
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="opacity-25 fixed inset-0 z-40 bg-black"></div>
  </div>
</template>

<script setup>
const props = defineProps(['title', 'message']);
</script>
