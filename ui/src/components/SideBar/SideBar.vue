<template>
  <nav class="md:left-0 md:block md:fixed md:top-0 md:bottom-0 md:overflow-y-auto md:flex-row md:flex-nowrap md:overflow-hidden shadow-xl bg-white flex flex-wrap items-center justify-between relative md:w-64 z-10 py-4 px-6">
    <div class="md:flex-col md:items-stretch md:min-h-full md:flex-nowrap px-0 flex flex-wrap items-center justify-between w-full mx-auto">
      <!-- Toggler -->
      <button
          class="cursor-pointer text-black opacity-50 md:hidden px-3 py-1 text-xl leading-none bg-transparent rounded border border-solid border-transparent"
          type="button"
          v-on:click="toggleCollapseShow('bg-white m-2 py-3 px-6')"
      >
        <i class="fas fa-bars"></i>
      </button>

      <!-- Brand -->
      <router-link
          class="md:block text-left md:pb-2 text-blueGray-600 mr-0 inline-block whitespace-nowrap text-sm uppercase font-bold p-4 px-0"
          to="/"
      >
        Champagne
      </router-link>

      <!-- User -->
      <ul class="md:hidden items-center flex flex-wrap list-none">
        <li class="inline-block relative">
          <user-dropdown />
        </li>
      </ul>

      <!-- Collapse -->
      <div
          class="md:flex md:flex-col md:items-stretch md:opacity-100 md:relative md:mt-4 md:shadow-none shadow absolute top-0 left-0 right-0 z-40 overflow-y-auto overflow-x-hidden h-auto items-center flex-1 rounded"
          v-bind:class="collapseShow"
      >
        <!-- Collapse header -->
        <div
            class="md:min-w-full md:hidden block pb-4 mb-4 border-b border-solid border-blueGray-200"
        >
          <div class="flex flex-wrap">
            <div class="w-6/12">
              <router-link
                  class="md:block text-left md:pb-2 text-blueGray-600 mr-0 inline-block whitespace-nowrap text-sm uppercase font-bold p-4 px-0"
                  to="/"
              >
                Champagne
              </router-link>
            </div>
            <div class="w-6/12 flex justify-end">
              <button
                type="button"
                class="cursor-pointer text-black opacity-50 md:hidden px-3 py-1 text-xl leading-none bg-transparent rounded border border-solid border-transparent"
                v-on:click="toggleCollapseShow('hidden')"
              >
                <i class="fas fa-times"></i>
              </button>
            </div>
          </div>
        </div>

        <h6 class="md:min-w-full text-blueGray-500 text-xs uppercase font-bold block pt-1 pb-4 no-underline">
          Active Deployable System
        </h6>

        <form>
          <select v-model="selectedDeployableSystem" class="block w-full py-2.5 px-0 mb-3 text-xs text-gray-500 bg-transparent border-0 border-b-2 border-gray-200 appearance-none peer focus:outline-none focus:ring-0 focus:border-gray-200">
            <option v-for="deployableSystem in currentUserStore.deployableSystems" :key="deployableSystem.id" :value="deployableSystem.name">
              {{ deployableSystem.name }}
            </option>
          </select>
        </form>

        <!-- Divider -->
        <hr class="my-4 md:min-w-full" />

        <!-- Heading -->
        <h6 class="md:min-w-full text-blueGray-500 text-xs uppercase font-bold block pt-1 pb-4 no-underline">
          Observability
        </h6>

        <ul class="md:flex-col md:min-w-full flex flex-col list-none">
          <li class="items-center">
            <router-link to="/hosts" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-server mr-2 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Hosts
              </a>
            </router-link>
          </li>
          <li class="items-center">
            <router-link to="/components" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-cubes mr-2 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Components
              </a>
            </router-link>
          </li>
          <li class="items-center">
            <router-link to="/builds" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-trowel-bricks mr-2 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Builds
              </a>
            </router-link>
          </li>
          <li class="items-center">
            <router-link to="/environments" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-seedling mr-2 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Environments
              </a>
            </router-link>
          </li>
          <li class="items-center">
            <router-link to="/tasks" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-list-check mr-2 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Manual Tasks
              </a>
            </router-link>
          </li>
          <li class="items-center">
            <router-link to="/systemUsers" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-users-gear mr-1 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Users
              </a>
            </router-link>
          </li>
          <li class="items-center">
            <router-link to="/errors" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-shield-virus mr-2 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Errors
              </a>
            </router-link>
          </li>
        </ul>

        <!-- Divider -->
        <hr class="my-4 md:min-w-full" v-if="currentUserStore.isDeployableSystemAdmin"/>

        <!-- Heading -->
        <h6 class="md:min-w-full text-blueGray-500 text-xs uppercase font-bold block pt-1 pb-4 no-underline" v-if="currentUserStore.isDeployableSystemAdmin">
          System Administration
        </h6>

        <ul class="md:flex-col md:min-w-full flex flex-col list-none" v-if="currentUserStore.isDeployableSystemAdmin">
          <li class="items-center">
            <router-link to="/manageSystem" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-bars-progress mr-2 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Manage System
              </a>
            </router-link>
          </li>
          <li class="items-center">
            <router-link to="/tags" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-tags mr-2 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Tags
              </a>
            </router-link>
          </li>
        </ul>

        <!-- Divider -->
        <hr class="my-4 md:min-w-full" v-if="currentUserStore.isChampagneAdmin"/>

        <!-- Heading -->
        <h6 class="md:min-w-full text-blueGray-500 text-xs uppercase font-bold block pt-1 pb-4 no-underline" v-if="currentUserStore.isChampagneAdmin">
          Champagne Administration
        </h6>

        <ul class="md:flex-col md:min-w-full flex flex-col list-none" v-if="currentUserStore.isChampagneAdmin">
          <li class="items-center">
            <router-link to="/" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-clipboard mr-2 text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Audit
              </a>
            </router-link>
          </li>
          <li class="items-center">
            <router-link to="/" v-slot="{ href, navigate, isActive }">
              <a
                  :href="href"
                  @click="navigate"
                  class="text-xs uppercase py-3 font-bold block"
                  :class="[
                      isActive
                        ? 'text-emerald-500 hover:text-emerald-600'
                        : 'text-blueGray-700 hover:text-blueGray-500'
                  ]"
              >
                <i class="fas fa-truck-fast text-sm" :class="[isActive ? 'opacity-75' : 'text-blueGray-300']"></i>
                Systems
              </a>
            </router-link>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import UserDropdown from "@/components/Dropdowns/UserDropdown.vue";

import {ref, watch} from 'vue';
import {useRouter} from "vue-router";
import {useCurrentUserStore} from "@/stores/currentUser";

const currentUserStore = useCurrentUserStore();

const collapseShow = ref('hidden');
const selectedDeployableSystem = ref(currentUserStore.activeDeployableSystem?.name);

watch(selectedDeployableSystem, changeDeployableSystem)

const router = useRouter();

function changeDeployableSystem(currentVal) {
  const system = currentUserStore.deployableSystemWithName(currentVal);
  currentUserStore.makeDeployableSystemActive(system);
  router.go(0);
}

function toggleCollapseShow(classes) {
  collapseShow.value = classes;
}
</script>