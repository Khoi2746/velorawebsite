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
.theme-toggle-wrapper {
  cursor: pointer;
  display: inline-block;
}

/* Thanh ray trượt */
.toggle-track {
  width: 54px;
  height: 28px;
  background-color: #e0e0e0; /* Màu nền lúc trắng */
  border-radius: 30px;
  position: relative;
  transition: all 0.4s ease;
  border: 1px solid #ccc;
}

/* Thanh ray trượt lúc sang nền Nâu */
.toggle-track.is-brown {
  background-color: #24201D;
  border-color: #d1aa68;
}

/* Cục tròn trượt qua lại */
.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 22px;
  height: 22px;
  background-color: #ffffff;
  border-radius: 50%;
  transition: all 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}

.toggle-track.is-brown .toggle-thumb {
  transform: translateX(26px);
  background-color: #d1aa68; /* Cục tròn màu vàng kim lúc nền Nâu */
}

/* Icon bên trong */
.icon-sun {
  color: #f39c12;
  font-size: 12px;
}
.icon-moon {
  color: #24201D;
  font-size: 12px;
}
</style>