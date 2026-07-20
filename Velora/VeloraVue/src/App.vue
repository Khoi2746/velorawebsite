<template>
  <div id="app">
    <router-view />
    <ChatbotAI />
  </div>
</template>

<script setup>
import ChatbotAI from './components/AI/ChatBotAI.vue'
</script>

<style>
body {
  margin: 0;
  background-color: #f4f1ea;
  font-family: sans-serif;
  color: #333;
}
</style>