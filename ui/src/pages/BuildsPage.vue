<template>
  <q-page class="q-pa-md">
    <q-table title="Recent Builds" :columns="buildColumns" :rows="builds" :loading="loading" v-model:pagination="pagination" @request="loadBuilds">

      <template v-slot:top-right>
        <q-select outlined emit-value map-options v-model="filterType" :options="filterTypes" label="Filter By" dense style="min-width: 150px"/>
        <q-input outlined v-model="filter" debounce="300" label="Type value to search" placeholder="Search" dense class="q-ml-xs" style="min-width: 250px">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>

      <template v-slot:body-cell-changeLog="props">
        <q-td :props="props">
          <div v-for="line in props.value.split('\n')" v-bind:key="line" v-html="formatChangeLog(line, props.row)"/>
        </q-td>
      </template>

      <template v-slot:body-cell-released="props">
        <q-td :props="props">
          <q-icon name="pending" class="text-grey-5" size="sm" v-if="!props.row.released">
            <q-tooltip class="bg-grey-5 text-black">Build is available for promotion</q-tooltip>
          </q-icon>
          <q-icon name="thumb_up" class="text-lime" size="sm" v-if="props.row.released">
            <q-tooltip class="bg-lime text-black">Build has been promoted</q-tooltip>
          </q-icon>
        </q-td>
      </template>

      <template v-slot:body-cell-createdAt="props">
        <q-td :props="props">
          <span>
            {{ fromNow(props.row.createdAt) }}
            <q-tooltip class="bg-grey-5 text-black">
              {{ formatDate(props.row.createdAt) }}
            </q-tooltip>
          </span>
        </q-td>
      </template>

      <template v-slot:body-cell-actions="props">
        <q-td :props="props">
          <q-btn size="sm" icon="military_tech" @click="startPromotionOfBuild(props.row)">
            <q-tooltip>Promote build</q-tooltip>
          </q-btn>
          <q-btn size="sm" icon="preview" @click="startPreviewPromotion(props.row)">
            <q-tooltip>Preview possible promotion</q-tooltip>
          </q-btn>
        </q-td>
      </template>

    </q-table>
  </q-page>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { formatDate, fromNow } from '../utils/time'
import { api } from 'boot/axios'

// Reactive data
const builds = ref([])
const loading = ref(false)
const pagination = ref({
  page: 1,
  rowsPerPage: 20,
  rowsNumber: 0
})
const filter = ref('')
const filterType = ref('componentIdentifierFilter')

// Constant data
const buildColumns = [
  {
    name: 'componentIdentifier',
    label: 'Component',
    field: 'componentIdentifier',
    align: 'left'
  },
  {
    name: 'componentVersion',
    label: 'Version',
    field: 'componentVersion',
    align: 'left'
  },
  {
    name: 'sourceBranch',
    label: 'Source',
    field: 'sourceBranch',
    align: 'left'
  },
  {
    name: 'commitUser',
    label: 'Committer',
    field: 'commitUser',
    align: 'left'
  },
  {
    name: 'changeLog',
    label: 'Change Log',
    align: 'left'
  },
  {
    name: 'released',
    label: 'Released?',
    align: 'left'
  },
  {
    name: 'createdAt',
    label: 'Created At',
    align: 'left'
  },
  {
    name: 'actions',
    label: 'Actions',
    align: 'left'
  }
]

const filterTypes = [
  {
    label: 'Component',
    value: 'componentIdentifierFilter'
  },
  {
    label: 'Version',
    value: 'componentVersionFilter'
  }
]

// Methods
function loadBuilds (props) {
  loading.value = true

  let { page, rowsPerPage } = pagination.value

  if (props !== undefined) {
    page = props.pagination.page
    rowsPerPage = props.pagination.rowsPerPage
  }

  const params = {
    pageNumber: page,
    pageSize: rowsPerPage
  }

  if (filter.value !== '') {
    params[filterType.value] = filter.value
  }

  api.get('/build', { params })
    .then((response) => {
      const { data } = response
      builds.value = data.content
      pagination.value.page = page
      pagination.value.rowsPerPage = rowsPerPage
      pagination.value.rowsNumber = data.totalElements
    })
    .finally(() => { loading.value = false })
}

function formatChangeLog (line, build) {
  // TODO: Need to track which git provider is being used so we can create links to the commit ref
  return line
}

function startPromotionOfBuild (build) {
  alert('Coming soon')
}

function startPreviewPromotion (build) {
  alert('Comming soon')
}

watch(filter, () => {
  loadBuilds()
})

onMounted(() => {
  loadBuilds()
})
</script>
