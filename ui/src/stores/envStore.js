import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import _ from 'lodash'
import { computed, ref } from 'vue'

export const useEnvStore = defineStore('env', () => {
  const envs = ref([])
  const loading = ref(false)

  const getActiveEnvs = computed(() => {
    return envs.value.filter((env) => !env.deleted)
  })

  const envsAsOptions = computed(() => {
    return getActiveEnvs.value.map(e => { return { label: e.name, value: e.id } })
  })

  function getEnvIdForName (name) {
    return _.first(getActiveEnvs.value.filter((env) => env.name === name)).id
  }

  async function load () {
    loading.value = true
    const response = await api.get('/environments')
    envs.value = response.data
    loading.value = false
  }

  function create (envData) {
    api.post('/environments', envData)
      .then(() => load())
  }

  function deactivate (envId) {
    api.delete(`/environments/${envId}/deactivate`)
      .then(() => load())
  }

  function activate (envId) {
    api.put(`/environments/${envId}/activate`)
      .then(() => load())
  }

  function deleteEnv (envId) {
    api.delete(`/environments/${envId}/delete`)
      .then(() => load())
  }

  return { envs, loading, getActiveEnvs, envsAsOptions, getEnvIdForName, load, create, deactivate, activate, deleteEnv }
})
