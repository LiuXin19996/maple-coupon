<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <router-view v-slot="{ Component }">
    <keep-alive>
      <component :is="Component" :key="$route.fullPath" />
    </keep-alive>
  </router-view>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
}

#app {
  width: 100%;
  min-height: 100vh;
}

body {
  margin: 0;
  min-height: 100vh;
  background: linear-gradient(to bottom, #87CEEB, #E6E6FA);
}
</style>
