import { setActivePinia, createPinia } from 'pinia'
import { useTaskStore } from 'src/stores/taskStore'
import { vi } from "vitest";
import { api } from "src/boot/axios";

beforeEach(() => {
  setActivePinia(createPinia())

  vi.mock('src/boot/axios', () => {
    const api = {
      get: vi.fn().mockImplementation(() => Promise.resolve({data: [{ summary: 'Something to do' }]})),
      post: vi.fn().mockImplementation(() => Promise.resolve(true)),
      delete: vi.fn().mockImplementation(() => Promise.resolve(true)),
      put: vi.fn().mockImplementation(() => Promise.resolve(true))
    }

    return { api }
  })
})

describe('load', () => {

  it('should load tasks for release', async () => {
    const taskStore = useTaskStore()

    await taskStore.load(1)

    expect(api.get).toHaveBeenCalled()
    expect(api.get).toHaveBeenCalledWith('/manual/deployment/tasks/releases/1')
  })

})

describe('create', () => {
  it('should make call to create task', async () => {
    const taskStore = useTaskStore()

    await taskStore.create({ summary: 'Do it' })

    expect(api.post).toHaveBeenCalled()
    expect(api.post).toHaveBeenCalledWith('/manual/deployment/tasks', { summary: 'Do it' })
  })
})

describe('deleteTask', () => {
  it('should make call to delete task', async () => {
    const taskStore = useTaskStore()

    await taskStore.deleteTask(1, 2)

    expect(api.delete).toHaveBeenCalled()
    expect(api.delete).toHaveBeenCalledWith('/manual/deployment/tasks/1')
  })
})

describe('updateStatus', () => {
  it('should make call to update task status', async () => {
    const taskStore = useTaskStore()

    await taskStore.updateStatus(1, 'COMPLETE')

    expect(api.put).toHaveBeenCalled()
    expect(api.put).toHaveBeenCalledWith('/manual/deployment/tasks/1/COMPLETE')
  })
})

describe('taskForRelease', () => {
  it('should return tasks for a given release', async () => {
    const taskStore = useTaskStore()
    await taskStore.load(1)

    const tasks = taskStore.tasksForRelease(1)

    expect(tasks).toEqual([{summary: 'Something to do'}])
  })
})

describe('loadingForTasks', () => {
  it('should return loading indicator for tasks for a given release', async () => {
    const taskStore = useTaskStore()
    await taskStore.load(1)

    const loading = taskStore.loadingForTasks(1)

    expect(loading).toBeFalsy()
  })
})
