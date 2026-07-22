<!-- src/components/GlobalAlert.vue -->
<template>
  <transition name="fade">
    <div class="custom-popup-overlay" v-if="alertState.show" @click="closeAlert">
      <div class="custom-popup-box" :class="alertState.type" @click.stop>
        <div class="popup-icon">
          <i v-if="alertState.type === 'success'" class="fas fa-check-circle"></i>
          <i v-else-if="alertState.type === 'warning'" class="fas fa-exclamation-triangle"></i>
          <i v-else class="fas fa-times-circle"></i>
        </div>
        <div class="popup-content">
          <h3>
            {{ alertState.type === 'success' ? 'THÀNH CÔNG' : alertState.type === 'warning' ? 'CHÚ Ý' : 'LỖI' }}
          </h3>
          <p>{{ alertState.message }}</p>
        </div>
        <button class="popup-close-btn" @click="closeAlert">ĐÓNG</button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { alertState, closeAlert } from '../composables/useAlert';
</script>

<style scoped>
/* Bê nguyên đoạn CSS cho .custom-popup-overlay và .custom-popup-box của em vào đây */
.custom-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 99999; /* Z-index cực to để đè lên mọi thứ */
}

.custom-popup-box {
  background: #1e1e1e;
  border: 1px solid #d1aa68;
  border-radius: 8px;
  padding: 30px 40px;
  width: 90%;
  max-width: 420px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.popup-icon i { font-size: 55px; }
.custom-popup-box.success .popup-icon i { color: #2ecc71; }
.custom-popup-box.warning .popup-icon i { color: #f39c12; }
.custom-popup-box.error .popup-icon i { color: #e74c3c; }

.popup-content h3 {
  color: #d1aa68;
  margin: 0 0 10px 0;
  font-size: 20px;
  letter-spacing: 2px;
  font-weight: bold;
}

.popup-content p {
  color: #e0e0e0;
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
}

.popup-close-btn {
  margin-top: 15px;
  background: #d1aa68;
  color: #111;
  border: none;
  padding: 12px 30px;
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 1px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
}

.popup-close-btn:hover {
  background: #b8955b;
  transform: translateY(-2px);
}

/* Hiệu ứng chuyển động mượt mà */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>