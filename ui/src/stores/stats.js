import {computed, ref} from 'vue'
import {defineStore} from 'pinia'

export const useStatsStore = defineStore('stats', () => {
  const currentMonthBuilds = ref(0);
  const previousMonthBuilds = ref(0);

  const currentMonthDeployments = ref(0);
  const previousMonthDeployments = ref(0);

  const activeDeployments = ref(0);

  const currentMonthFailures = ref(0);
  const previousMonthFailures = ref(0);

  const buildsChangePercent = computed(() => {
    const divisor = previousMonthBuilds.value === 0 ? 1 : previousMonthBuilds.value;
    return ((currentMonthBuilds.value - previousMonthBuilds.value) / (divisor * 1.0) * 100).toFixed(2);
  });

  const deploymentsChangePercent = computed(() => {
    const divisor = previousMonthDeployments.value === 0 ? 1 : previousMonthDeployments.value;
    return ((currentMonthDeployments.value - previousMonthDeployments.value) / (divisor * 1.0) * 100).toFixed(2);
  });

  const failuresChangePercent = computed(() => {
    const divisor = previousMonthFailures.value === 0 ? 1 : previousMonthFailures.value;
    return ((currentMonthFailures.value - previousMonthFailures.value) / (divisor * 1.0) * 100).toFixed(2);
  });

  function loadStats() {
    currentMonthBuilds.value = 50;
    previousMonthBuilds.value = 45;

    currentMonthDeployments.value = 38;
    previousMonthDeployments.value = 45;

    activeDeployments.value = 5;

    currentMonthFailures.value = 0;
    previousMonthFailures.value = 0;
  }

  setTimeout(loadStats, 500);
  setInterval(loadStats, 10_000);

  return { currentMonthBuilds, buildsChangePercent, currentMonthDeployments, deploymentsChangePercent, activeDeployments, currentMonthFailures, failuresChangePercent }
});
