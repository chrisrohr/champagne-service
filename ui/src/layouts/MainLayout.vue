<template>
  <q-layout view="hHh lpR fFf">
    <q-header elevated style="background: linear-gradient(15deg, #9400d3, #000000)">
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
          class="text-grey-4"
        />

        <q-avatar>
          <q-icon name="fa-solid fa-champagne-glasses" size="26px" color="grey-4"/>
        </q-avatar>

        <q-toolbar-title>
          <div class="text-h4 text-grey-4">Champagne</div>
          <div class="text-caption text-grey-4">Toasting successful deployments</div>
        </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      bordered
      :width="200"
      :breakpoint="800"
      side="left"
    >
    <q-scroll-area style="height: 100%; border-right: 1px solid #ddd">
        <q-list padding>
          <q-item to="/" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="dashboard"/>
            </q-item-section>
            <q-item-section>
              Dashboard
            </q-item-section>
          </q-item>

          <q-item to="/hostConfig" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="precision_manufacturing"/>
            </q-item-section>
            <q-item-section>
              Host Config
            </q-item-section>
          </q-item>

          <q-item to="/builds" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="build"/>
            </q-item-section>
            <q-item-section>
              Builds
            </q-item-section>
          </q-item>

          <q-item to="/environments" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="fa-solid fa-server"/>
            </q-item-section>
            <q-item-section>
              Environments
            </q-item-section>
          </q-item>

          <q-item to="/tasks" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="task"/>
            </q-item-section>
            <q-item-section>
              Manual Tasks
            </q-item-section>
          </q-item>

          <q-item to="/users" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="manage_accounts"/>
            </q-item-section>
            <q-item-section>
              Users
            </q-item-section>
          </q-item>

          <q-item to="/audits" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="assignment"/>
            </q-item-section>
            <q-item-section>
              Audit
            </q-item-section>
          </q-item>
        </q-list>
      </q-scroll-area>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from 'stores/auth'

const leftDrawerOpen = ref(false)
const authStore = useAuthStore()

function toggleLeftDrawer () {
  leftDrawerOpen.value = !leftDrawerOpen.value
}

onMounted(() => {
  leftDrawerOpen.value = authStore.isLoggedIn
})
</script>
