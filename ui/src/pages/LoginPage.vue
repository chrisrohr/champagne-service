<template>
    <q-page class="">
      <div class="column q-pa-lg fixed-center">
        <div class="row">
          <q-card square class="shadow-24" style="width:300px;height:485px;">
            <q-card-section class="bg-deep-purple-7">
              <h4 class="text-h5 text-white q-my-md">Login</h4>
            </q-card-section>
            <q-card-section>
              <q-form class="q-px-sm q-pt-xl">
                <q-input square clearable v-model="userLogin.username" type="text" label="Username">
                  <template v-slot:prepend>
                    <q-icon name="badge" />
                  </template>
                </q-input>
                <q-input square clearable v-model="userLogin.password" type="password" label="Password">
                  <template v-slot:prepend>
                    <q-icon name="lock" />
                  </template>
                </q-input>
              </q-form>
            </q-card-section>
            <q-card-section>
              <div class="text-center q-pa-md q-gutter-md">
                <q-btn round color="indigo-7">
                  <q-icon name="fab fa-google" size="1.2rem" />
                </q-btn>
                <q-btn round color="red-8">
                  <q-icon name="fab fa-aws" size="1.2rem" />
                </q-btn>
                <q-btn round color="light-blue-5">
                  <q-icon name="fab fa-gitlab" size="1.2rem" />
                </q-btn>
                <q-btn round color="light-blue-5">
                  <q-icon name="fab fa-github" size="1.2rem" />
                </q-btn>
              </div>
            </q-card-section>
            <q-card-actions class="q-px-lg">
              <q-btn unelevated size="lg" color="purple-4" class="full-width text-white" label="Sign In" @click="login"/>
            </q-card-actions>
          </q-card>
        </div>
      </div>
    </q-page>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from 'stores/auth'

const auth = useAuthStore()

const userLogin = ref({
  username: '',
  password: ''
})

function login () {
  auth.login(userLogin.value.username, userLogin.value.password)
}
</script>
