<template>
  <div class="theme-toggle-wrapper" @click="toggleTheme" :title="isBrown ? 'Chuyển sang nền Trắng' : 'Chuyển sang nền Nâu'">
    <div class="toggle-track" :class="{ 'is-brown': isBrown }">
      <div class="toggle-thumb">
        <i v-if="!isBrown" class="fas fa-sun icon-sun"></i>
        <i v-else class="fas fa-moon icon-moon"></i>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const isBrown = ref(false)

// Hàm xử lý đổi màu
const toggleTheme = () => {
  isBrown.value = !isBrown.value
  
  if (isBrown.value) {
    document.body.classList.add('theme-brown')
    localStorage.setItem('velora-theme', 'brown') // Lưu lại cho lần sau vào web
  } else {
    document.body.classList.remove('theme-brown')
    localStorage.setItem('velora-theme', 'white')
  }
}

// Khi vừa load trang, kiểm tra xem khách hàng trước đó chọn màu gì
onMounted(() => {
  const savedTheme = localStorage.getItem('velora-theme')
  if (savedTheme === 'brown') {
    isBrown.value = true
    document.body.classList.add('theme-brown')
  } else {
    document.body.classList.remove('theme-brown')
  }
})
</script>

<style scoped>
@import "./CSS/ThemeToggle.css";
</style>