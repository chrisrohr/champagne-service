<template>
  <!-- Header -->
  <div class="relative bg-emerald-600 md:pt-32 pb-32 pt-12">
    <div class="px-4 md:px-10 mx-auto w-full">
      <div>
        <div class="flex flex-wrap" v-if="currentUser.activeDeployableSystem">
          <div class="w-full lg:w-6/12 xl:w-3/12 px-4">
            <card-stats
              stat-subtitle="BUILDS"
              :stat-title="stats.currentMonthBuilds"
              :stat-percent="stats.buildsChangePercent"
              stat-description="Since last month"
              stat-icon-name="far fa-trowel-bricks"
              stat-icon-color="bg-blue-500"/>
          </div>
          <div class="w-full lg:w-6/12 xl:w-3/12 px-4">
            <card-stats
                stat-subtitle="DEPLOYMENTS"
                :stat-title="stats.currentMonthDeployments"
                :stat-percent="stats.deploymentsChangePercent"
                stat-description="Since last month"
                stat-icon-name="far fa-cubes"
                stat-icon-color="bg-green-500"/>
          </div>
          <div class="w-full lg:w-6/12 xl:w-3/12 px-4">
            <card-stats
                stat-subtitle="RUNNING"
                :stat-title="stats.activeDeployments"
                stat-icon-name="far fa-refresh fa-spin"
                stat-icon-color="bg-gray-500"/>
          </div>
          <div class="w-full lg:w-6/12 xl:w-3/12 px-4">
            <card-stats
                stat-subtitle="FAILURES"
                :stat-title="stats.currentMonthFailures"
                :stat-percent="stats.failuresChangePercent"
                stat-description="Since last month"
                stat-icon-name="far fa-circle-exclamation"
                stat-icon-color="bg-red-500"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import CardStats from "@/components/Cards/CardStats.vue";
import {useStatsStore} from "@/stores/stats";
import {useCurrentUserStore} from "@/stores/currentUser";

const stats = useStatsStore();
const currentUser = useCurrentUserStore();
</script>
