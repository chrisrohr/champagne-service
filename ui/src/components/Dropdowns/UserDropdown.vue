<template>
  <div>
    <a class="text-gray-500 block" href="#" ref="btnDropdownRef" v-on:click="toggleDropdown($event)">
      <div class="items-center flex">
        <span class="w-12 h-12 text-sm text-gray-500 bg-gray-200 inline-flex items-center justify-center rounded-full">
          <i class="fas fa-user" v-if="!currentUserStore.isLoggedIn"></i>
          <span v-if="currentUserStore.isLoggedIn">{{ currentUserStore.avatar }}</span>
        </span>
      </div>
    </a>
    <div ref="popoverDropdownRef" class="bg-white text-base z-50 float-left py-2 list-none text-left rounded shadow-lg min-w-48" :class="{ hidden: !dropdownPopoverShow, block: dropdownPopoverShow }">
      <a href="javascript:void(0)" @click="logout" class="text-sm py-2 px-4 font-normal block w-full whitespace-nowrap bg-transparent text-gray-700">Logout</a>
    </div>
  </div>
</template>

<script setup>
import {ref} from "vue";
import {createPopper} from "@popperjs/core";

import {useCurrentUserStore} from "@/stores/currentUser";

const currentUserStore = useCurrentUserStore();

const dropdownPopoverShow = ref(false);

const popoverDropdownRef = ref(null);
const btnDropdownRef = ref(null);

function logout() {
  currentUserStore.logout();
}

function toggleDropdown(event) {
  event.preventDefault();

  if (dropdownPopoverShow.value) {
    dropdownPopoverShow.value = false;
  } else {
    dropdownPopoverShow.value = true;
    createPopper(btnDropdownRef.value, popoverDropdownRef.value, { placement: 'bottom-start' });
  }
}
</script>
