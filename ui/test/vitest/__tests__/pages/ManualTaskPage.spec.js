import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import ManualTaskPage from 'pages/ManualTaskPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { useEnvStore } from 'stores/envStore'
import { useReleaseStageStore } from 'src/stores/releaseStageStore'
import { useReleaseStore } from 'stores/releaseStore'
import { useTaskStore } from 'stores/taskStore'
import { notifyError } from "src/utils/alerts";

installQuasar()

vi.mock('src/utils/alerts', () => {
  const notifyError = vi.fn()

  return { notifyError }
})

const pinia = createTestingPinia()
const envStore = useEnvStore(pinia)
const releaseStore = useReleaseStore(pinia)
const releaseStageStore = useReleaseStageStore(pinia)
const taskStore = useTaskStore(pinia)

releaseStageStore.load.mockImplementation(() => Promise.resolve(true))
releaseStageStore.stages = ['PRE', 'POST']

envStore.load.mockImplementation(() => Promise.resolve(true))
envStore.envs = [{name: 'dev', id: 1, deleted: false}]

releaseStore.load.mockImplementation(() => Promise.resolve(true))

const wrapper = shallowMount(ManualTaskPage, {
  global: {
    plugins: [pinia]
  }
})

afterEach(() => {
  vi.clearAllMocks()
  vi.resetAllMocks()
})

describe('ManualTaskPage', () => {
  it('should mount the manual task page without errors', () => {
    expect(wrapper).toBeTruthy()
  })
})

describe('createRelease', () => {
  it('should call the release store to create a new release', () => {
    releaseStore.create.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.release.releaseNumber = '2023.42'

    wrapper.vm.createRelease()

    expect(releaseStore.create).toHaveBeenCalledWith({ releaseNumber: '2023.42' })
  })

  it('should call display validation error when releaseNumber is missing', () => {
    wrapper.vm.release.releaseNumber = ''

    wrapper.vm.createRelease()

    expect(releaseStore.create).not.toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalledWith('Release Number is required')
  })
})

describe('handleExpansion', () => {
  it('should only change the prop value when collapsing', () => {
    const props = { expand: true }

    wrapper.vm.handleExpansion(props)

    expect(props.expand).toBeFalsy()
    expect(taskStore.load).not.toHaveBeenCalled()
  })

  it('should set to expanded and load tasks', () => {
    const props = { expand: false, row: { id: 1 }}
    taskStore.load.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.handleExpansion(props)

    expect(props.expand).toBeTruthy()
    expect(taskStore.load).toHaveBeenCalled()
    expect(taskStore.load).toHaveBeenCalledWith(1)
  })
})

describe('isInStatus', () => {
  it('should return true when env is in given status', () => {
    envStore.getEnvIdForName.mockImplementation(() => 1)

    const release = {
      environmentStatus: {
        1: { status: 'PENDING' }
      }
    }

    const result = wrapper.vm.isInStatus('dev', release, 'PENDING')

    expect(result).toBeTruthy()
  })

  it('should return false when env is not in given status', () => {
    envStore.getEnvIdForName.mockImplementation(() => 1)

    const release = {
      environmentStatus: {
        1: { status: 'COMPLETE' }
      }
    }

    const result = wrapper.vm.isInStatus('dev', release, 'PENDING')

    expect(result).toBeFalsy()
  })
})

describe('startStatusUpdate', () => {
  it('should setup the model for status update dialog', () => {
    envStore.getEnvIdForName.mockImplementation(() => 1)

    const row = {
      id: 3,
      releaseId: 2,
      environmentStatus: {
        1: { id: 4, status: 'COMPLETE' }
      }
    }

    wrapper.vm.startStatusUpdate('TASK', row, 'dev')

    expect(wrapper.vm.update.type).toEqual('TASK')
    expect(wrapper.vm.update.env).toEqual('dev')
    expect(wrapper.vm.update.envId).toEqual(1)
    expect(wrapper.vm.update.id).toEqual(4)
    expect(wrapper.vm.update.status).toEqual('COMPLETE')
    expect(wrapper.vm.update.releaseId).toEqual(2)
    expect(wrapper.vm.showUpdateStatusDialog).toBeTruthy()
  })
})

describe('updateStatus', () => {
  it('should call the release store to update the status of a release', async () => {
    releaseStore.updateStatus.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.update = {
      status: 'COMPLETE',
      type: 'RELEASE',
      id: 1
    }

    await wrapper.vm.updateStatus()

    expect(releaseStore.updateStatus).toHaveBeenCalledWith(1, 'COMPLETE')
  })

  it('should call the task store to update the status of a task', async () => {
    taskStore.updateStatus.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.update = {
      status: 'COMPLETE',
      type: 'TASK',
      id: 1,
      releaseId: 1
    }

    await wrapper.vm.updateStatus()

    expect(taskStore.updateStatus).toHaveBeenCalledWith(1, 'COMPLETE')
  })

  it('should call display validation error when status is missing', () => {
    wrapper.vm.update.status = ''

    wrapper.vm.updateStatus()

    expect(releaseStore.updateStatus).not.toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalledWith('Status is required')
  })
})

describe('startAddTask', () => {
  it('should set the release id and display the new task form', () => {
    const release = {
      id: 1
    }

    wrapper.vm.startAddTask(release)

    expect(wrapper.vm.task.releaseId).toEqual(1)
    expect(wrapper.vm.showAddTaskDialog).toBeTruthy()
  })
})

describe('createTask', () => {
  it('should call the task store to create a new task', () => {
    taskStore.create.mockImplementation(() => Promise.resolve(1))

    wrapper.vm.task = {
      stage: 'PRE',
      summary: 'Do it',
      component: 'foo-service',
      releaseId: 1
    }

    wrapper.vm.createTask()

    expect(taskStore.create).toHaveBeenCalledWith({ stage: 'PRE', summary: 'Do it', component: 'foo-service', releaseId: 1 })
  })

  it('should call display validation error when stage is missing', () => {
    wrapper.vm.task.stage = ''

    wrapper.vm.createTask()

    expect(taskStore.create).not.toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalledWith('Stage is required')
  })

  it('should call display validation error when summary is missing', () => {
    wrapper.vm.task.stage = 'PRE'
    wrapper.vm.task.summary = ''

    wrapper.vm.createTask()

    expect(taskStore.create).not.toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalledWith('Summary is required')
  })

  it('should call display validation error when stage is missing', () => {
    wrapper.vm.task.stage = 'PRE'
    wrapper.vm.task.summary = 'Do it'
    wrapper.vm.task.component = ''

    wrapper.vm.createTask()

    expect(taskStore.create).not.toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalled()
    expect(notifyError).toHaveBeenCalledWith('Component is required')
  })
})

describe('deleteRelease', () => {
  it('should call delete on the release store', () => {
    wrapper.vm.deleteRelease({ id: 1 })

    expect(releaseStore.deleteRelease).toHaveBeenCalled()
    expect(releaseStore.deleteRelease).toHaveBeenCalledWith(1)
  })
})

describe('deleteTask', () => {
  it('should call delete on the task store', () => {
    wrapper.vm.deleteTask({ id: 1, releaseId: 2 })

    expect(taskStore.deleteTask).toHaveBeenCalled()
    expect(taskStore.deleteTask).toHaveBeenCalledWith(1, 2)
  })
})
