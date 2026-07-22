<template>
    <div class="login-page">
        <Header />

        <div class="login-wrapper">
            <div class="login-container">
                <div class="login-header">
                    <h2>ĐĂNG NHẬP</h2>
                    <p>Chào mừng trở lại với thế giới thời gian của Velora</p>
                </div>

                <form @submit.prevent="handleLogin" class="login-form">

                    <div class="input-group">
                        <label for="email">Email</label>
                        <div class="input-wrapper">
                            <i class="far fa-envelope input-icon"></i>
                            <input type="email" id="email" v-model="email" placeholder="Nhập địa chỉ email của bạn"
                                required />
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="password">Mật khẩu</label>
                        <div class="input-wrapper">
                            <i class="fas fa-lock input-icon"></i>
                            <input :type="showPassword ? 'text' : 'password'" id="password" v-model="password"
                                placeholder="Nhập mật khẩu" required />
                            <i class="fas toggle-pw-icon" :class="showPassword ? 'fa-eye-slash' : 'fa-eye'"
                                @click="togglePassword"></i>
                        </div>
                    </div>

                    <div class="form-actions">
                        <label class="remember-me">
                            <input type="checkbox" v-model="rememberMe" />
                            Ghi nhớ đăng nhập
                        </label>
                        <router-link to="/quen-mat-khau" class="forgot-password">Quên mật khẩu?</router-link>
                    </div>

                    <button type="submit" class="btn-submit">
                        ĐĂNG NHẬP
                    </button>

                    <div class="register-redirect">
                        <span>Bạn chưa có tài khoản?</span>
                        <router-link to="/dang-ky" class="register-link">Đăng ký ngay</router-link>
                    </div>
                </form>
            </div>
        </div>
        <Footer />
    </div>
    <ToastPopup 
    :visible="showToast" 
    :message="toastMsg" 
    :type="toastType"
    :loading="loading" 
/>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/Header.vue' 
import Footer from '../components/Footer.vue'
import ToastPopup from '../components/ToastPopup.vue'

const router = useRouter()

const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const showToast = ref(false)
const toastMsg = ref('')
const toastType = ref('success')
const loading = ref(false)

const togglePassword = () => {
    showPassword.value = !showPassword.value
}

const handleLogin = async () => {
    // 1. Khởi tạo trạng thái Loading
    loading.value = true;
    showToast.value = true;
    toastMsg.value = 'Đang xác thực...';
    toastType.value = 'loading'; // Tùy chỉnh CSS để hiện icon xoay

    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email.value, password: password.value })
        });

        // 2. Ép buộc chờ 2 giây để hiển thị hiệu ứng xoay (dù server phản hồi nhanh hay chậm)
        await new Promise(resolve => setTimeout(resolve, 4000));

        if (!response.ok) {
            // Lấy lỗi từ backend
            const errorData = await response.json().catch(() => ({ message: 'Lỗi hệ thống!' }));
            throw new Error(errorData.message || 'Sai email hoặc mật khẩu!');
        }

        // Đăng nhập thành công
        const userData = await response.json();
        localStorage.setItem('user', JSON.stringify(userData));

        // 3. Chuyển sang trạng thái thành công (Tick xanh)
        loading.value = false;
        toastMsg.value = 'Đăng nhập thành công!';
        toastType.value = 'success';

        // Đợi 1.5 giây sau khi hiện tick rồi mới chuyển trang
        setTimeout(() => {
            window.location.href = '/';
        }, 1500);

    } catch (error) {
        // 4. Chuyển sang trạng thái thất bại (Dấu chéo X)
        loading.value = false;
        toastMsg.value = error.message;
        toastType.value = 'error';

        // Tự tắt thông báo sau 3 giây
        setTimeout(() => { 
            showToast.value = false; 
        }, 3000);
    }
}
</script>
<style scoped>
@import "./CSS/Login.css";
</style>