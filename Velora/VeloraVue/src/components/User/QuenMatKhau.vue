<template>
  <div class="page-wrapper">
    <Header />

    <div class="forgot-password-container">
      <div class="forgot-password-card">
        <div class="logo-wrapper">
          <img src="https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png" alt="Velora Clock Logo" class="brand-logo" />
          <p class="subtitle">Khôi phục quyền truy cập</p>
        </div>

        <!-- Thông báo chung -->
        <div v-if="message" :class="['notification', statusType]">
          {{ message }}
        </div>

        <!-- BƯỚC 1: NHẬP EMAIL -->
        <form v-if="step === 1" @submit.prevent="handleSendOtp" class="form-content">
          <div class="form-group">
            <label>Email đã đăng ký</label>
            <input 
              type="email" 
              v-model="email" 
              placeholder="Nhập email của quý khách..." 
              required 
              :disabled="isLoading"
            />
          </div>
          <button type="submit" class="btn-submit" :disabled="isLoading">
            <span v-if="isLoading">ĐANG GỬI MÃ...</span>
            <span v-else>GỬI MÃ XÁC NHẬN</span>
          </button>
        </form>

        <!-- BƯỚC 2: NHẬP MÃ OTP -->
        <form v-if="step === 2" @submit.prevent="handleVerifyOtp" class="form-content">
          <p class="guide-text">Mã xác nhận đã được gửi đến email <b>{{ email }}</b></p>
          <div class="form-group">
            <label>Mã xác nhận (OTP)</label>
            <input 
              type="text" 
              v-model="otp" 
              placeholder="Nhập mã 6 số..." 
              required 
              maxlength="6"
              :disabled="isLoading"
            />
          </div>
          <button type="submit" class="btn-submit" :disabled="isLoading">
            <span v-if="isLoading">ĐANG KIỂM TRA...</span>
            <span v-else>XÁC NHẬN MÃ</span>
          </button>
        </form>

        <!-- BƯỚC 3: NHẬP MẬT KHẨU MỚI -->
        <form v-if="step === 3" @submit.prevent="handleResetPassword" class="form-content">
          <div class="form-group">
            <label>Mật khẩu mới</label>
            <input 
              type="password" 
              v-model="newPassword" 
              placeholder="Nhập mật khẩu mới..." 
              required 
              :disabled="isLoading"
            />
          </div>
          <div class="form-group">
            <label>Nhập lại mật khẩu</label>
            <input 
              type="password" 
              v-model="confirmPassword" 
              placeholder="Xác nhận lại mật khẩu..." 
              required 
              :disabled="isLoading"
            />
          </div>
          <button type="submit" class="btn-submit" :disabled="isLoading">
            <span v-if="isLoading">ĐANG LƯU...</span>
            <span v-else>ĐỔI MẬT KHẨU</span>
          </button>
        </form>

        <div class="card-footer" v-if="step !== 4">
          <router-link to="/dang-nhap" class="back-link">
            &larr; Quay lại Đăng nhập
          </router-link>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import Header from '../Header.vue'
import Footer from '../Footer.vue'
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();

// State quản lý
const step = ref(1);
const isLoading = ref(false);
const message = ref('');
const statusType = ref('');

// Data form
const email = ref('');
const otp = ref('');
const newPassword = ref('');
const confirmPassword = ref('');

const clearMessage = () => {
  message.value = '';
  statusType.value = '';
};

// Hàm 1: Gửi mã OTP
const handleSendOtp = async () => {
  clearMessage();
  isLoading.value = true;
  try {
    await axios.post('http://localhost:8080/api/auth/forgot-password/send-otp', { email: email.value });
    step.value = 2; // Chuyển sang bước nhập mã
    statusType.value = 'success';
    message.value = 'Đã gửi mã xác nhận. Vui lòng kiểm tra email!';
  } catch (error) {
    statusType.value = 'error';
    message.value = error.response?.data || "Lỗi hệ thống. Vui lòng thử lại.";
  } finally {
    isLoading.value = false;
  }
};

// Hàm 2: Kiểm tra OTP
const handleVerifyOtp = async () => {
  clearMessage();
  isLoading.value = true;
  try {
    await axios.post('http://localhost:8080/api/auth/forgot-password/verify-otp', { 
      email: email.value, 
      otp: otp.value 
    });
    step.value = 3; // Chuyển sang bước đổi mật khẩu
    statusType.value = 'success';
    message.value = 'Mã hợp lệ. Vui lòng tạo mật khẩu mới.';
  } catch (error) {
    statusType.value = 'error';
    message.value = error.response?.data || "Mã xác nhận sai hoặc hết hạn.";
  } finally {
    isLoading.value = false;
  }
};

// Hàm 3: Lưu mật khẩu mới
const handleResetPassword = async () => {
  clearMessage();
  if (newPassword.value !== confirmPassword.value) {
    statusType.value = 'error';
    message.value = 'Mật khẩu xác nhận không khớp!';
    return;
  }

  isLoading.value = true;
  try {
    await axios.post('http://localhost:8080/api/auth/forgot-password/reset', { 
      email: email.value, 
      otp: otp.value,
      newPassword: newPassword.value 
    });
    
    // Thành công
    statusType.value = 'success';
    message.value = 'Đổi mật khẩu thành công! Tự động chuyển về trang đăng nhập...';
    step.value = 4; // Ẩn form đi
    
    // Đợi 2.5s rồi chuyển hướng
    setTimeout(() => {
      router.push('/dang-nhap');
    }, 2500);

  } catch (error) {
    statusType.value = 'error';
    message.value = error.response?.data || "Đổi mật khẩu thất bại.";
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* =========================================
   LUXURY THEME STYLING
========================================= */
.forgot-password-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #121212;
  font-family: 'Arial', sans-serif;
}

.forgot-password-card {
  background-color: #1e1e1e;
  width: 100%;
  max-width: 450px;
  padding: 40px;
  border-radius: 8px;
  border: 1px solid #333;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  border-top: 3px solid #d1aa68;
}

.logo-wrapper {
  text-align: center;
  margin-bottom: 25px;
}

.brand-logo {
  max-width: 180px;
  height: auto;
  margin-bottom: 5px;
}

.subtitle {
  color: #888;
  font-size: 14px;
  margin-top: 8px;
}

.guide-text {
  color: #d1aa68;
  font-size: 13px;
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  color: #d1aa68;
  margin-bottom: 8px;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  background-color: #2a2a2a;
  border: 1px solid #444;
  color: #fff;
  border-radius: 4px;
  font-size: 15px;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #d1aa68;
  box-shadow: 0 0 5px rgba(209, 170, 104, 0.3);
}

.btn-submit {
  width: 100%;
  padding: 14px;
  background-color: #d1aa68;
  color: #1e1e1e;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.1s ease;
  letter-spacing: 1px;
}

.btn-submit:hover:not(:disabled) {
  background-color: #e5bf7e;
}

.btn-submit:active:not(:disabled) {
  transform: scale(0.98);
}

.btn-submit:disabled {
  background-color: #555;
  color: #888;
  cursor: not-allowed;
}

.card-footer {
  text-align: center;
  margin-top: 25px;
}

.back-link {
  color: #888;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.back-link:hover {
  color: #d1aa68;
}

.notification {
  padding: 12px 15px;
  margin-bottom: 20px;
  border-radius: 4px;
  font-size: 14px;
  text-align: center;
}

.notification.success {
  background-color: rgba(46, 204, 113, 0.1);
  color: #2ecc71;
  border: 1px solid #2ecc71;
}

.notification.error {
  background-color: rgba(231, 76, 60, 0.1);
  color: #e74c3c;
  border: 1px solid #e74c3c;
}
</style>