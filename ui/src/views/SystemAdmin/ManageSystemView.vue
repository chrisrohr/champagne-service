<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-blank :title="title">
        <template #content>
          <form>
            <h6 class="text-gray-400 text-sm mt-3 mb-6 font-bold uppercase">
              Initial Environment
            </h6>
            <div class="flex flex-wrap">
              <div class="w-full lg:w-6/12 px-4">
                <div class="relative w-full mb-3">
                  <label class="block uppercase text-gray-600 text-xs font-bold mb-2">Select Initial Environment</label>
                  <select v-model="initialEnvironment" class="block w-full py-2.5 px-3 mb-3 text-xs text-gray-500 bg-white border border-gray-200 appearance-none peer focus:outline-none focus:ring-0 focus:border-gray-200">
                    <option v-for="env in environmentStore.environmentsForSystem" :key="env.id" :value="env.id">
                      {{ env.name }}
                    </option>
                  </select>
                </div>
              </div>
            </div>
            <h6 class="text-gray-400 text-sm mt-3 mb-6 font-bold uppercase">
              Environment Deployment Order
            </h6>
            <div class="flex flex-wrap">
              <div class="w-full lg:w-6/12 px-4">
                <div class="relative w-full mb-3">
                  <label class="block uppercase text-gray-600 text-xs font-bold mb-2">Move environments into order of promotion</label>
                  <draggable v-model="envOrder" @start="drag=true" @end="drag=false" item-key="id">
                    <template #item="{element}">
                      <div class="block w-full py-2.5 px-3 text-xs text-gray-500 bg-white border border-gray-200 hover:cursor-grab active:cursor-grabbing">{{element.name}}</div>
                    </template>
                  </draggable>
                </div>
              </div>
            </div>
          </form>
        </template>
      </card-blank>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import {useCurrentUserStore} from "@/stores/currentUser";
import {useEnvironmentStore} from "@/stores/environments";
import CardBlank from "@/components/Cards/CardBlank.vue";
import draggable from 'vuedraggable';
import {api} from "@/plugins/axios";

const pageInfoStore = usePageInfoStore();
const currentUserStore = useCurrentUserStore();
const environmentStore = useEnvironmentStore();

const title = computed(() => `Manage ${currentUserStore.activeDeployableSystem?.name}`);
const initialEnvironment = ref(currentUserStore.activeDeployableSystem?.devEnvironmentId);
const envOrder = ref(environmentStore.environmentsFromCSV(currentUserStore.activeDeployableSystem?.environmentPromotionOrder || []));
const drag = ref(false);

watch(initialEnvironment, storeInitialEnvironment);
watch(envOrder, storeEnvironmentPromotionOrder);

function storeInitialEnvironment(newEnvId) {
  api.put(`/systems/${currentUserStore.activeDeployableSystem.id}/dev/${newEnvId}`)
      .then(() => currentUserStore.loadDeployableSystems());
}

function storeEnvironmentPromotionOrder(newOrder) {
  api.put(`/systems/${currentUserStore.activeDeployableSystem.id}/order`, newOrder.map(env => env.id))
      .then(() => currentUserStore.loadDeployableSystems());
}

onMounted(() => {
  pageInfoStore.setPageTitle('Manage System');
});
</script>
