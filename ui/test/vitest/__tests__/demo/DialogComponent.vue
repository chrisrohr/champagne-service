<template>
  <q-btn @click="onClick"> Click me! </q-btn>
</template>

<script setup>
import { Dialog } from 'quasar';

function onClick() {
  Dialog.create({ message: 'Hello there!'});
}
</script>
