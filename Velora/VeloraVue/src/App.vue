<template>
  <div id="app">
    <router-view />
  </div>
  <ChatbotAI />
</template>

<script setup>
import ChatbotAI from './components/AI/ChatBotAI.vue'
</script>

<style>
body {
  margin: 0;
  background-color: #0d0d0d;
  font-family: sans-serif;
}
</style>