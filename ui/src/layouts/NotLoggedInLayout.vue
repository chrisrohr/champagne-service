<template>
  <div>
    <navbar />
    <main>
      <section class="relative w-full h-full py-40 min-h-screen">
        <div class="absolute top-0 w-full h-full bg-gray-800 bg-no-repeat bg-full">
          <router-view />
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import Navbar from '@/components/Navbars/AuthNavbar.vue';
</script>
