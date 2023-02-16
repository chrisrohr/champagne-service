<template>
  <q-page class="q-pa-md">
    <q-table :columns="hostColumns" :rows="hosts" :loading="hostLoading" :pagination="pagination" title="Hosts" hide-pagination>
      <template v-slot:top>
        <q-select outlined dense v-model="environmentFilter" :options="environments" label="Environment" style="min-width: 150px" :display-value="`${environmentFilter.label}`"/>
        <q-input outlined dense v-model="componentFilter" debounce="300" label="Type component name to search" class="q-ml-xs" style="min-width: 300px">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>

      <template v-slot:body="props">
        <q-tr :props="props">
          <q-td>
            <q-btn round size="sm" color="primary" :icon="props.expand ? 'remove' : 'add'" @click="handleHostExpansion(props)"/>
          </q-td>
          <q-td key="hostname" :props="props">
            {{ props.row.hostname }}
          </q-td>
          <q-td key="info" :props="props">
            Coming soon
          </q-td>
          <q-td key="tags" :props="props">
            {{ tagListAsCsv(props.row.tags) }}
          </q-td>
          <q-td key="source" :props="props">
            {{ props.row.source }}
          </q-td>
          <q-td key="actions" :props="props">
            <q-btn size="sm" icon="delete" @click="deleteHost(props.row)">
              <q-tooltip>Remove host</q-tooltip>
            </q-btn>
          </q-td>
        </q-tr>
        <q-tr v-show="props.expand" :props="props">
          <q-td colspan="100%">
            <q-table :columns="componentColumns" :rows="componentsByHost[props.row.id]" :pagination="pagination" :loading="componentsByHostLoading[props.row.id]" hide-pagination>
              <template v-slot:body-cell-currentVersion="props">
                <q-td :props="props">
                  Coming Soon
                </q-td>
              </template>

              <template v-slot:body-cell-actions="props">
                <q-td :props="props">
                  <q-btn size="sm" icon="delete" @click="deleteComponent(props.row)">
                    <q-tooltip>Remove component</q-tooltip>
                  </q-btn>
                </q-td>
              </template>
            </q-table>
          </q-td>
        </q-tr>
      </template>
    </q-table>

    <q-page-sticky position="bottom-right" :offset="[18, 18]">
      <q-fab icon="add" direction="up" color="accent">
        <q-fab-action @click="showHostAdd = true" color="primary" icon="computer">
          <q-tooltip anchor="top left">New Host</q-tooltip>
        </q-fab-action>
        <q-fab-action @click="showComponentAdd = true" color="primary" icon="fa-solid fa-cube">
          <q-tooltip anchor="top left">New Component</q-tooltip>
        </q-fab-action>
      </q-fab>
    </q-page-sticky>

    <q-dialog v-model="showHostAdd">
      <q-card>
        <q-card-section>
          <div class="text-h6">Add New Host</div>
        </q-card-section>
        <q-card-section class="row">
          <q-input v-model="host.hostname" type="text" label="Hostname" outlined dense style="min-width: 205px"/>
        </q-card-section>
        <q-card-section class="row">
          <q-input v-model="host.tag" type="text" label="Tags" debounce="300" @keyup.enter="addTag" outlined dense style="min-width: 205px"/>
        </q-card-section>
        <q-card-section class="row q-pt-none">
          <q-chip removable @remove="deleteTag(tag)" v-for="tag in host.tags" v-bind:key="tag">
            {{tag}}
          </q-chip>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" v-close-popup/>
          <q-btn flat label="Save" class="text-primary" @click="createHost"/>
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showComponentAdd">
      <q-card>
        <q-card-section>
          <div class="text-h6">Add New Component</div>
        </q-card-section>
        <q-card-section class="row">
          <q-input v-model="component.componentName" type="text" label="Name" outlined dense style="min-width: 205px"/>
        </q-card-section>
        <q-card-section class="row">
          <q-input v-model="component.tag" type="text" label="Tag" outlined dense style="min-width: 205px"/>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" v-close-popup/>
          <q-btn flat label="Save" class="text-primary" @click="createComponent"/>
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useEnvStore } from 'src/stores/deploymentEnvironment'
import { api } from 'src/boot/axios'
import { useQuasar } from 'quasar'
import { _ } from 'lodash'

const $q = useQuasar()

// Stores
const envs = useEnvStore()

// Reactive data
const hosts = ref([])
const hostLoading = ref(false)
const environmentFilter = ref('')
const componentFilter = ref('')
const host = ref({
  hostname: '',
  tag: '',
  tags: []
})
const showHostAdd = ref(false)
const componentsByHost = ref({})
const componentsByHostLoading = ref({})
const showComponentAdd = ref(false)
const component = ref({
  componentName: '',
  tag: ''
})

// Computed data
const environments = computed(() => envs.getActiveEnvs.map(e => { return { label: e.name, value: e.id } }))

// Constant data
const pagination = {
  rowsPerPage: 10000
}
const hostColumns = [
  {
    name: 'expand',
    field: 'expand'
  },
  {
    name: 'hostname',
    label: 'Hostname',
    align: 'left'
  },
  {
    name: 'info',
    label: 'Info',
    align: 'left'
  },
  {
    name: 'tags',
    label: 'Tags',
    align: 'left'
  },
  {
    name: 'source',
    label: 'Source',
    align: 'left'
  },
  {
    name: 'actions',
    label: 'Actions',
    align: 'left'
  }
]
const componentColumns = [
  {
    name: 'componentName',
    label: 'Component Name',
    field: 'componentName',
    align: 'left'
  },
  {
    name: 'currentVersion',
    label: 'Current Version',
    align: 'left'
  },
  {
    name: 'tag',
    label: 'Tag',
    field: 'tag',
    align: 'left'
  },
  {
    name: 'actions',
    label: 'Actions',
    align: 'left'
  }
]

// Methods
function loadHosts () {
  hostLoading.value = true

  api.get(`/host/${environmentFilter.value.value}`, { componentFilter: componentFilter.value })
    .then((response) => {
      hosts.value = response.data
    })
    .finally(() => {
      hostLoading.value = false
    })
}

function addTag () {
  host.value.tags.push(host.value.tag)
  host.value.tag = ''
}

function deleteTag (tag) {
  host.value.tags = _.without(host.value.tags, tag)
}

function createHost () {
  const hostData = {
    hostname: host.value.hostname,
    tags: host.value.tags,
    source: 'CHAMPAGNE',
    environmentId: environmentFilter.value.value
  }

  api.post('/host', hostData)
    .then(() => {
      showHostAdd.value = false
      loadHosts()
    })
}

function tagListAsCsv (tagList) {
  return _.join(tagList, ', ')
}

function deleteHost (host) {
  $q.dialog({
    title: 'Hold Up!',
    message: `Are you sure you want to delete host ${host.hostname}?`,
    cancel: true,
    persistent: true
  }).onOk(() => {
    api.delete(`/host/${host.id}`)
      .then(() => loadHosts())
  })
}

function handleHostExpansion (props) {
  props.expand = !props.expand

  if (props.expand) {
    componentsByHostLoading.value[props.row.id] = true

    api.get(`/host/${props.row.id}/components`)
      .then(response => {
        componentsByHost.value[props.row.id] = response.data
      })
      .finally(() => {
        componentsByHostLoading.value[props.row.id] = false
      })
  }
}

function createComponent () {
  api.post('/host/component', component.value)
    .then(() => {
      showComponentAdd.value = false
      loadHosts()
    })
}

function deleteComponent (component) {
  $q.dialog({
    title: 'Hold Up!',
    message: `Are you sure you want to delete component ${component.componentName}?`,
    cancel: true,
    persistent: true
  }).onOk(() => {
    api.delete(`/host/component/${component.id}`)
      .then(() => loadHosts())
  })
}

watch(environmentFilter, () => {
  loadHosts()
})

onMounted(() => {
  envs.load()
    .then(() => {
      environmentFilter.value = { label: envs.getActiveEnvs[0].name, value: envs.getActiveEnvs[0].id }
    })
})
</script>
