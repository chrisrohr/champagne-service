import {ref} from 'vue'
import {defineStore} from 'pinia'
import {api} from "@/plugins/axios"
import _ from 'lodash';

export const useEnvironmentStore = defineStore('env', () => {
  const environmentsForSystem = ref([]);
  const loading = ref(false);

  async function loadEnvironments() {
    loading.value = true;
    const response = await api.get('/environments');
    environmentsForSystem.value = response.data;
    loading.value = false;
  }

  function environmentsFromCSV(csv) {
    const envs = csv.split(',')
      .map(envId => _.find(environmentsForSystem.value.filter(env => env.id === envId)))
      .filter(env => env !== undefined);

    const missingEnvs = environmentsForSystem.value.filter(env => _.find(envs, e => e.id === env.id) === undefined);
    return _.concat(envs, missingEnvs);
  }

  return {
    loadEnvironments,
    environmentsForSystem,
    loading,
    environmentsFromCSV
  }
});
