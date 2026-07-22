// src/composables/useAlert.js
import { reactive } from 'vue';

// State lưu trữ dữ liệu của popup
export const alertState = reactive({
  show: false,
  message: '',
  type: 'success' // 'success', 'warning', 'error'
});

let alertTimeout = null;

// Hàm mở popup
export const showAlert = (message, type = 'success', duration = 3000) => {
  alertState.message = message;
  alertState.type = type;
  alertState.show = true;

  // Xóa timeout cũ nếu người dùng bấm liên tục
  if (alertTimeout) clearTimeout(alertTimeout);

  // Tự động đóng sau khoảng thời gian duration
  alertTimeout = setTimeout(() => {
    closeAlert();
  }, duration);
};

// Hàm đóng popup
export const closeAlert = () => {
  alertState.show = false;
  if (alertTimeout) clearTimeout(alertTimeout);
};