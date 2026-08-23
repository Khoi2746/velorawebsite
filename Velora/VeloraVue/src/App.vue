<template>
  <div id="app">
    <router-view />
    <ChatbotAI v-if="!isAdminRoute" class="no-print" />
    <GlobalAlert class="no-print" />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ChatbotAI from './components/AI/ChatBotAI.vue'
import GlobalAlert from './components/GlobalAlert.vue'

const route = useRoute()

const isAdminRoute = computed(() => {
  return route.path.startsWith('/admin')
})

onMounted(() => {
  // 🔥 Dọn sạch ký hiệu #_=_ do Facebook tự động gắn vào URL
  if (window.location.hash.includes('_=_')) {
    window.location.hash = '';
    window.history.replaceState('', document.title, window.location.pathname + window.location.search);
  }

  // 🔥 Xử lý bắt thông tin user nếu được truyền thẳng qua query từ OAuth2 Backend
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('login_success') === 'true') {
    const name = urlParams.get('name');
    const email = urlParams.get('email');
    if (name && email) {
      const tempUser = { hoTen: decodeURIComponent(name), email: email };
      localStorage.setItem('user', JSON.stringify(tempUser));
      // Dọn sạch query trên URL cho đẹp
      window.history.replaceState({}, document.title, window.location.pathname);
      // Tải lại nhẹ trang chủ để header nhận diện user ngay lập tức
      window.location.reload();
    }
  }
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