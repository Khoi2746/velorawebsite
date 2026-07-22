<template>
  <div id="app">
    <router-view />
    <ChatbotAI />
    <GlobalAlert />
  </div>
</template>

<script setup>
import ChatbotAI from './components/AI/ChatBotAI.vue'
import GlobalAlert from './components/GlobalAlert.vue';
</script>

<style>
body {
  margin: 0;
  background-color: #f4f1ea;
  font-family: sans-serif;
  color: #333;
}
</style>