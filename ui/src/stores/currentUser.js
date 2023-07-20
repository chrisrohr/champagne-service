import {computed, ref} from 'vue'
import {defineStore} from 'pinia'
import {api} from "@/plugins/axios";
import _ from "lodash";

export const useCurrentUserStore = defineStore('currentUser', () => {
  const info = ref( null);
  const deployableSystems = ref([]);
  const activeDeployableSystem = ref(null);

  const isLoggedIn = computed(() => info.value !== null);
  const isLoggedInAndSystemChosen = computed(() => info.value !== null && activeDeployableSystem.value !== null);
  const isChampagneAdmin = computed(() => info.value?.admin);
  const isDeployableSystemAdmin = computed(() => activeDeployableSystem.value?.admin);
  const avatar = computed(() => {
    if (info.value === null) {
      return null;
    }
    return `${info.value.firstName?.charAt(0)}${info.value.lastName?.charAt(0)}`;
  });

  function deployableSystemWithName(systemName) {
    const systems = deployableSystems.value || [];

    return _.first(systems.filter(system => system.name === systemName));
  }

  function deployableSystemWithId(id) {
    const systems = deployableSystems.value || [];
    return _.find(systems, (system => system.id === id));
  }

  async function loadDeployableSystems() {
    const response = await api.get('/systems');
    deployableSystems.value = response?.data || [];

    const activeDeployableSystemId = sessionStorage.getItem('champagne-current-system');
    if (activeDeployableSystemId) {
      activeDeployableSystem.value = deployableSystemWithId(parseInt(activeDeployableSystemId, 10));
    }
  }

  async function loadUserInfo() {
    const response = await api.get('/users/current');
    info.value = response?.data;
  }

  async function login(username, password) {
    const response = await api.post('/auth/login', { username: username, password: password });
    info.value = response.data;
    await this.router.push({ name: 'root' });
  }

  function makeDeployableSystemActive(deployableSystem) {
    activeDeployableSystem.value = deployableSystem;
    sessionStorage.setItem('champagne-current-system', deployableSystem.id);
  }

  async function logout() {
    await api.delete('/auth/logout');
    sessionStorage.removeItem('champagne-current-system');
    info.value = null;
    activeDeployableSystem.value = null;
  }

  return {
    info,
    deployableSystems,
    activeDeployableSystem,
    login,
    makeDeployableSystemActive,
    logout,
    isLoggedIn,
    isChampagneAdmin,
    avatar,
    loadDeployableSystems,
    isLoggedInAndSystemChosen,
    deployableSystemWithName,
    isDeployableSystemAdmin,
    loadUserInfo
  }
});
