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
          @click="drawer.value = !drawer.value"
          class="text-grey-4"
          v-if="authStore.isLoggedIn"
        />

        <q-avatar>
          <q-icon name="fa-solid fa-champagne-glasses" size="26px" color="grey-4"/>
        </q-avatar>

        <q-toolbar-title>
          <div class="text-h4 text-grey-4">Champagne</div>
          <div class="text-caption text-grey-4">Toasting successful deployments</div>
        </q-toolbar-title>

        <q-space />

        <q-select dense outlined filled v-model="systemStore.currentSystem" :options="systemStore.systems" style="min-width:205px; background-color: white;" label="Select a System" option-value="id" option-label="name"/>
        <q-btn round flat v-if="authStore.isLoggedIn">
          <q-avatar size="26px" class="bg-grey-4 text-grey-9">
            {{ authStore.loggedInUserAvatar }}
          </q-avatar>
          <q-menu>
            <q-list>
              <q-item clickable v-close-popup @click="authStore.logout()">
                <q-item-section>Logout</q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-btn>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="drawer"
      show-if-above

      :mini="miniState"
      @mouseover="miniState = false"
      @mouseout="miniState = true"

      bordered
      :width="200"
      :breakpoint="800"
      v-if="authStore.isLoggedIn"
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

          <q-item to="/errors" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="gpp_bad"/>
            </q-item-section>
            <q-item-section>
              Errors
            </q-item-section>
          </q-item>

          <q-separator />

          <q-item to="/audits" exact clickable v-ripple v-if="authStore.isAdmin">
            <q-item-section avatar>
              <q-icon name="assignment"/>
            </q-item-section>
            <q-item-section>
              Audit
            </q-item-section>
          </q-item>

          <q-item to="/adminSystems" exact clickable v-ripple v-if="authStore.isAdmin">
            <q-item-section avatar>
              <q-icon name="conveyor_belt"/>
            </q-item-section>
            <q-item-section>
              Systems
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
import { ref, onMounted } from 'vue'
import { useAuthStore } from 'stores/auth'
import { useSystemStore } from 'stores/systemStore'

// Stores
const authStore = useAuthStore()
const systemStore = useSystemStore()

// Reactive data
const drawer = ref(false)
const miniState = ref(true)

onMounted(() => {
  systemStore.load()
})
</script>
