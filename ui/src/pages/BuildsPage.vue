<template>
  <q-page class="q-pa-md">
    <q-table title="Recent Builds" :columns="buildColumns" :rows="buildStore.builds" :loading="buildStore.loading" v-model:pagination="buildStore.pagination" @request="buildStore.load">

      <template v-slot:top-right>
        <q-select outlined emit-value map-options v-model="buildStore.filterType" :options="filterTypes" label="Filter By" dense style="min-width: 150px" @update:model-value="buildStore.load()"/>
        <q-input outlined v-model="buildStore.filter" debounce="300" label="Type value to search" placeholder="Search" dense class="q-ml-xs" style="min-width: 250px" @update:model-value="buildStore.load()">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>

      <template v-slot:body-cell-commitRef="props">
        <q-td :props="props">
          <q-icon :name="iconForProvider(props.row)" class="on-left" size="sm" :color="iconColorForProvider(props.row)"/>
          <span v-if="props.row.gitProvider !== 'OTHER'">
            <a :href="urlForCommitRef(props.row)" target="_blank" >{{ _.truncate(props.row.commitRef, { length: 11 }) }}</a>
          </span>
          <span v-if="props.row.gitProvider === 'OTHER'">{{ _.truncate(props.row.commitRef, { length: 11 }) }}</span>
        </q-td>
      </template>

      <template v-slot:body-cell-changeLog="props">
        <q-td :props="props">
          <div v-for="line in props.row.changeLog.split('\n')" v-bind:key="line" v-html="formatChangeLog(line, props.row)"/>
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
import { onMounted } from 'vue'
import { formatDate, fromNow } from '../utils/time'
import { useBuildStore } from 'stores/buildStore'
import { _ } from 'lodash'

// Stores
const buildStore = useBuildStore()

// Reactive data

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
    name: 'commitRef',
    label: 'Commit Ref',
    field: 'commitRef',
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
function formatChangeLog (line, build) {
  // TODO: Need to track which git provider is being used so we can create links to the commit ref
  return line
}

function startPromotionOfBuild (build) {
  alert('Coming soon')
}

function startPreviewPromotion (build) {
  alert('Coming soon')
}

function urlForCommitRef (build) {
  switch (build.gitProvider) {
    case 'GITHUB':
      return `https://github.com/${build.repoNamespace}/${build.repoName}/commit/${build.commitRef}`
    case 'GITLAB':
      return `https://gitlab.com/${build.repoNamespace}/${build.repoName}/-/commit/${build.commitRef}`
    case 'BITBUCKET':
      return `https://bitbucket.org/${build.repoNamespace}/${build.repoName}/commits/${build.commitRef}`
  }

  return ''
}

function iconForProvider (build) {
  let providerIcon = 'fa-git-alt'

  if (build.gitProvider !== 'OTHER') {
    providerIcon = `fa-${_.toLower(build.gitProvider)}`
  }

  return `fa-brands ${providerIcon}`
}

function iconColorForProvider (build) {
  switch (build.gitProvider) {
    case 'GITLAB':
      return 'orange'
    case 'BITBUCKET':
      return 'blue'
  }

  return 'black'
}

onMounted(() => {
  buildStore.load()
})
</script>
