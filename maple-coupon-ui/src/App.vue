<script setup>
import { RouterView } from 'vue-router'
import Footer from '@/components/Footer.vue'
</script>

<template>
  <div id="app">
    <router-view v-slot="{ Component }">
      <keep-alive>
        <component :is="Component" :key="$route.fullPath" />
      </keep-alive>
    </router-view>
    <Footer />
  </div>
</template>

<style>
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  width: 100%;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

#app {
  width: 100%;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

body {
  background: linear-gradient(to bottom, #87CEEB, #E6E6FA);
}

/* 确保自定义元素不显示 */
tongyi-web-extension {
  display: none !important;
}

/* 美化 el-main 和 content-area */
.el-main {
  padding: 20px;
  height: 100%;
}

.content-area {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  min-height: calc(100vh - 140px);
  overflow-y: auto;
  transition: all 0.3s ease;
}

.content-area:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* 自定义滚动条样式 */
.content-area::-webkit-scrollbar {
  width: 8px;
}

.content-area::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.content-area::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

.content-area::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
