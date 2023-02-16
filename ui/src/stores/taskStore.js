import { defineStore } from 'pinia'
import { api } from 'src/boot/axios'
import { computed, ref } from 'vue'

export const useTaskStore = defineStore('task', () => {
  const tasks = ref({})
  const loading = ref({})

  const tasksForRelease = computed(() => {
    return (releaseId) => tasks.value[releaseId]
  })
  const loadingForTasks = computed(() => {
    return (releaseId) => loading.value[releaseId]
  })

  async function load (releaseId) {
    loading.value[releaseId] = true

    const response = await api.get(`/manual/deployment/tasks/releases/${releaseId}`)
    tasks.value[releaseId] = response.data
    loading.value[releaseId] = false
  }

  function create (taskData, callback = () => {}) {
    api.post('/manual/deployment/tasks', taskData)
      .then(() => load(taskData.releaseId))
      .then(callback)
  }

  function deleteTask (id, releaseId) {
    api.delete(`/manual/deployment/tasks/${id}`)
      .then(() => load(releaseId))
  }

  function updateStatus (statusId, status, callback = () => {}) {
    api.put(`/manual/deployment/tasks/${statusId}/${status}`)
      .then(callback)
  }

  return { tasksForRelease, loadingForTasks, load, create, deleteTask, updateStatus }
})
