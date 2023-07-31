<template>
  <div class="flex flex-wrap mt-4">
    <div class="w-full mb-12 px-4">
      <card-table caption="List of tags" :columns="tagColumns" :rows="tagRows" row-id="id" title="Tags" :loading="loading">
        <template #body-cell-actions="props">
          <button type="button" @click="startUpdate(props.row)" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-edit"></i>
          </button>
          <button type="button" @click="confirmDeleteTag(props.row)" class="text-emerald-500 bg-transparent border border-solid border-emerald-500 hover:bg-emerald-500 hover:text-white active:bg-emerald-600 font-bold uppercase text-xs px-4 py-2 rounded outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150">
            <i class="fas fa-trash"></i>
          </button>
        </template>
      </card-table>
    </div>

    <confirmation-prompt title="Hold up!" :message="deleteTagConfirmationMessage" @cancelConfirm="deleteTagConfirmationState = false" @acceptConfirm="deleteTag" v-if="deleteTagConfirmationState"/>

    <button class="fixed bottom-3 right-14 bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase text-xs w-10 h-10 p-0 rounded-full shadow hover:shadow-md outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="startCreate">
      <i class="fas fa-plus"></i>
    </button>

    <div v-if="showAddTag" class="overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none justify-center items-center flex">
      <div class="relative w-auto my-6 mx-auto max-w-sm">
        <div class="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
          <div class="flex items-start justify-between p-5 border-b border-solid border-gray-200 rounded-t">
            <h5 class="text-lg font-semibold uppercase">
              <span v-if="tagToCreate.id === null">Add new tag</span>
              <span v-else>Update tag</span>
            </h5>
          </div>
          <div class="relative p-6 flex-auto">
            <div class="mb-3 pt-0">
              <input type="text" placeholder="Name" v-model="tagToCreate.name" class="px-2 py-1 placeholder-gray-300 text-gray-600 relative bg-white rounded text-sm border border-gray-300 outline-none focus:outline-none focus:shadow-outline w-full"/>
            </div>
            <div class="mb-3 pt-0 text-red-600 text-xs" v-if="tagToCreate.showNameError">
              Name is required!
            </div>
          </div>
          <div class="flex items-center justify-end p-6 border-t border-solid border-gray-200 rounded-b">
            <button class="text-red-500 bg-transparent font-bold uppercase px-6 py-2 text-sm outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="showAddTag = false">
              Cancel
            </button>
            <button class="bg-emerald-500 text-white active:bg-emerald-600 font-bold uppercase px-6 py-3 text-sm rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 ease-linear transition-all duration-150" type="button" @click="tagToCreate.id === null ? createTag() : updateTag()">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddTag" class="opacity-25 fixed inset-0 z-40 bg-black"></div>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {usePageInfoStore} from "@/stores/pageInfo";
import CardTable from "@/components/Cards/CardTable.vue";
import {api} from "@/plugins/axios";
import ConfirmationPrompt from "@/components/Alerts/ConfirmationPrompt.vue";

const pageInfoStore = usePageInfoStore();

const deleteTagConfirmationMessage = ref('');
const deleteTagConfirmationState = ref(false);
const currentTag = ref(null);

const loading = ref(false);

const showAddTag = ref(false);
const tagToCreate = ref({
  id: null,
  name: '',
  showNameError: false
});

const tagRows = ref({});

const tagColumns = [
  {
    name: 'name',
    label: 'Tag Name',
    field: 'name'
  },
  {
    name: 'actions',
    label: 'Actions'
  }
];

function loadTags() {
  return api.get(`/tag`)
      .then(response => {
        tagRows.value = response.data;
      });
}

function confirmDeleteTag(tag) {
  deleteTagConfirmationMessage.value = `Are you sure you want to delete tag ${tag.name}?`;
  currentTag.value = tag;
  deleteTagConfirmationState.value = true;
}

function deleteTag() {
  deleteTagConfirmationState.value = false;
  api.delete(`/tag/${currentTag.value.id}`)
      .then(() => {
        loadTags();
      });
}

function startCreate() {
  tagToCreate.value.id = null;
  tagToCreate.value.name = '';
  tagToCreate.value.showNameError = false;
  showAddTag.value = true;
}

function startUpdate(tag) {
  tagToCreate.value.id = tag.id;
  tagToCreate.value.name = tag.name;
  tagToCreate.value.showNameError = false;
  showAddTag.value = true;
}

function createTag() {
  if (tagToCreate.value.name === '') {
    tagToCreate.value.showNameError = true;
  } else {
    showAddTag.value = false;
    api.post('/tag', {
      name: tagToCreate.value.name
    })
        .then(() => {
          loadTags();
        });
  }
}

function updateTag() {
  if (tagToCreate.value.name === '') {
    tagToCreate.value.showNameError = true;
  } else {
    showAddTag.value = false;
    api.put(`/tag/${tagToCreate.value.id}`, {
      name: tagToCreate.value.name
    })
        .then(() => {
          loadTags();
        });
  }
}

onMounted(() => {
  pageInfoStore.setPageTitle('Tags');

  loadTags();
});
</script>
