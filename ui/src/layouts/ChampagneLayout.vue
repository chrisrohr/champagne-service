<template>
  <div>
    <side-bar />
    <div class="relative md:ml-64 bg-gray-100">
      <admin-navbar />
      <header-stats />
      <div class="px-4 md:px-10 mx-auto w-full -m-24">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import SideBar from "../components/SideBar/SideBar.vue";
import AdminNavbar from "../components/Navbars/AdminNavbar.vue";
import HeaderStats from "@/components/Headers/HeaderStats.vue";
</script>
