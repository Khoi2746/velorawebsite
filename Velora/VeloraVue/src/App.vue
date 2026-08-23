<template>
  <div id="app">
    <router-view />
    <ChatbotAI v-if="!isAdminRoute" class="no-print" />
    <GlobalAlert class="no-print" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ChatbotAI from './components/AI/ChatBotAI.vue'
import GlobalAlert from './components/GlobalAlert.vue'

const route = useRoute()

const isAdminRoute = computed(() => {
  return route.path.startsWith('/admin')
})
</script>

<style>
body {
  margin: 0;
  background-color: #f4f1ea;
  font-family: sans-serif;
  color: #333;
}

@media print {
  .no-print {
    display: none !important;
    opacity: 0 !important;
    visibility: hidden !important;
  }
}
</style>